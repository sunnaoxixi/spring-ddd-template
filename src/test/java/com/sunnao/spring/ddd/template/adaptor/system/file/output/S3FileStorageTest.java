package com.sunnao.spring.ddd.template.adaptor.system.file.output;

import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.model.system.file.FileStorageTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * S3 对象存储实现单元测试
 * <p>
 * Mockito mock S3Client，验证 store/read/delete 的成功分支与失败分支（不依赖真实对象存储服务）。
 */
@ExtendWith(MockitoExtension.class)
class S3FileStorageTest {

    private static final String BUCKET = "test-bucket";

    @Mock
    private S3Client s3Client;

    private S3FileStorage s3FileStorage;

    @BeforeEach
    void setUp() {
        s3FileStorage = new S3FileStorage();
        ReflectionTestUtils.setField(s3FileStorage, "bucket", BUCKET);
        ReflectionTestUtils.setField(s3FileStorage, "maxSize", DataSize.ofMegabytes(10));
        ReflectionTestUtils.setField(s3FileStorage, "s3Client", s3Client);
    }

    @Test
    @DisplayName("存储成功：key 格式 yyyy/MM/dd/{uuid}.{ext}，contentType 写入对象元数据")
    void storeSuccess() {
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);

        ResultDO<String> result = s3FileStorage.store("test.TXT", "text/plain", content);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().matches("\\d{4}/\\d{2}/\\d{2}/[0-9a-f]{32}\\.txt"));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals(BUCKET, captor.getValue().bucket());
        assertEquals(result.getData(), captor.getValue().key());
        assertEquals("text/plain", captor.getValue().contentType());
    }

    @Test
    @DisplayName("存储成功：contentType 为空时回退 application/octet-stream")
    void storeWithBlankContentType() {
        ResultDO<String> result = s3FileStorage.store("data.bin", null,
                new byte[]{1, 2, 3});

        assertTrue(result.isSuccess());
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("application/octet-stream", captor.getValue().contentType());
    }

    @Test
    @DisplayName("存储失败：内容为空返回 FILE_EMPTY，不调用 S3")
    void storeEmptyContent() {
        ResultDO<String> result = s3FileStorage.store("test.txt", "text/plain", new byte[0]);

        assertFalse(result.isSuccess());
        assertEquals(ErrorCodeEnum.FILE_EMPTY.getCode(), result.getCode());
        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("存储失败：超过大小上限返回 FILE_TOO_LARGE")
    void storeTooLarge() {
        ReflectionTestUtils.setField(s3FileStorage, "maxSize", DataSize.ofBytes(2));

        ResultDO<String> result = s3FileStorage.store("test.txt", "text/plain", new byte[]{1, 2, 3});

        assertFalse(result.isSuccess());
        assertEquals(ErrorCodeEnum.FILE_TOO_LARGE.getCode(), result.getCode());
        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("存储失败：S3 异常返回 FILE_STORE_ERROR")
    void storeS3Error() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("access denied").build());

        ResultDO<String> result = s3FileStorage.store("test.txt", "text/plain", new byte[]{1});

        assertFalse(result.isSuccess());
        assertEquals(ErrorCodeEnum.FILE_STORE_ERROR.getCode(), result.getCode());
    }

    @Test
    @DisplayName("读取成功：返回对象字节内容")
    void readSuccess() {
        byte[] content = "content".getBytes(StandardCharsets.UTF_8);
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), content));

        ResultDO<byte[]> result = s3FileStorage.read("2026/07/07/abc.txt");

        assertTrue(result.isSuccess());
        assertArrayEquals(content, result.getData());
    }

    @Test
    @DisplayName("读取失败：对象不存在返回 FILE_NOT_FOUND")
    void readNotFound() {
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        ResultDO<byte[]> result = s3FileStorage.read("2026/07/07/missing.txt");

        assertFalse(result.isSuccess());
        assertEquals(ErrorCodeEnum.FILE_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    @DisplayName("删除成功：调用 S3 DeleteObject（幂等）")
    void deleteSuccess() {
        ResultDO<Void> result = s3FileStorage.delete("2026/07/07/abc.txt");

        assertTrue(result.isSuccess());
        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertEquals(BUCKET, captor.getValue().bucket());
        assertEquals("2026/07/07/abc.txt", captor.getValue().key());
    }

    @Test
    @DisplayName("删除失败：S3 异常返回 FILE_DELETE_ERROR")
    void deleteS3Error() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("network error").build());

        ResultDO<Void> result = s3FileStorage.delete("2026/07/07/abc.txt");

        assertFalse(result.isSuccess());
        assertEquals(ErrorCodeEnum.FILE_DELETE_ERROR.getCode(), result.getCode());
    }

    @Test
    @DisplayName("存储类型标识为 s3")
    void getStorageType() {
        assertEquals(FileStorageTypeEnum.S3.getCode(), s3FileStorage.getStorageType());
    }

    @Test
    @DisplayName("初始化失败：连接配置不完整时抛出异常（启动即失败）")
    void initWithIncompleteConfig() {
        S3FileStorage storage = new S3FileStorage();
        ReflectionTestUtils.setField(storage, "endpoint", "");
        ReflectionTestUtils.setField(storage, "region", "us-east-1");
        ReflectionTestUtils.setField(storage, "accessKey", "ak");
        ReflectionTestUtils.setField(storage, "secretKey", "sk");
        ReflectionTestUtils.setField(storage, "bucket", "bucket");

        assertThrows(IllegalStateException.class, storage::init);
    }
}
