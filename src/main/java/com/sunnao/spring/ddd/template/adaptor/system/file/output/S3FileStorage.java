package com.sunnao.spring.ddd.template.adaptor.system.file.output;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.application.system.file.FileStorage;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;

/**
 * S3 兼容对象存储实现（Output Adaptor）
 * <p>
 * 实现 application 层定义的 FileStorage 接口（依赖倒置）。
 * 基于 AWS SDK v2 通用 S3 协议客户端，通过 endpoint 切换服务商，兼容：
 * 阿里云 OSS、腾讯云 COS、MinIO、七牛云 Kodo 等（参考 yudao S3FileClient 方案）。
 * 连接配置走 app.file.s3.*（endpoint/region/access-key/secret-key/bucket/path-style-access）；
 * 对象 key 格式：yyyy/MM/dd/{uuid}.{ext}。
 */
@Slf4j
@Component
public class S3FileStorage implements FileStorage {

    /**
     * 服务端点（阿里云 https://oss-cn-hangzhou.aliyuncs.com、腾讯云 https://cos.ap-shanghai.myqcloud.com、MinIO http://127.0.0.1:9000 等）
     */
    @Value("${app.file.s3.endpoint:}")
    private String endpoint;

    /**
     * 区域（阿里云 oss-cn-hangzhou、腾讯云 ap-shanghai；MinIO 等无区域概念的服务商填任意值如 us-east-1）
     */
    @Value("${app.file.s3.region:us-east-1}")
    private String region;

    /**
     * 访问密钥 ID（阿里云 AccessKeyId、腾讯云 SecretId）
     */
    @Value("${app.file.s3.access-key:}")
    private String accessKey;

    /**
     * 访问密钥 Secret（阿里云 AccessKeySecret、腾讯云 SecretKey）
     */
    @Value("${app.file.s3.secret-key:}")
    private String secretKey;

    /**
     * 存储桶名称（需预先创建）
     */
    @Value("${app.file.s3.bucket:}")
    private String bucket;

    /**
     * 是否路径风格访问：MinIO 需 true；阿里云 OSS / 腾讯云 COS 用虚拟主机风格（false，默认）
     */
    @Value("${app.file.s3.path-style-access:false}")
    private boolean pathStyleAccess;

    /**
     * 单文件大小上限
     */
    @Value("${app.file.max-size:10MB}")
    private DataSize maxSize;

    /**
     * S3 客户端（线程安全，初始化后复用）
     */
    private S3Client s3Client;

    /**
     * 初始化 S3 客户端（配置不完整时启动即失败，避免运行期才暴露问题）
     */
    @PostConstruct
    void init() {
        if (StrUtil.hasBlank(endpoint, region, accessKey, secretKey, bucket)) {
            throw new IllegalStateException(
                    "S3 存储配置不完整，请检查 app.file.s3.endpoint/region/access-key/secret-key/bucket");
        }
        s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                // 阿里云 OSS/腾讯云 COS 走虚拟主机风格（bucket.endpoint），MinIO 等需路径风格
                .forcePathStyle(pathStyleAccess)
                // 关闭 AWS SDK 2.30+ 默认强制的 CRC 完整性校验（第三方 S3 兼容服务普遍不支持）
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
        log.info("S3FileStorage 初始化完成, endpoint: {}, bucket: {}, pathStyleAccess: {}",
                endpoint, bucket, pathStyleAccess);
    }

    /**
     * 应用关闭时释放客户端连接资源
     */
    @PreDestroy
    void destroy() {
        if (s3Client != null) {
            s3Client.close();
        }
    }

    @Override
    public ResultDO<String> store(String originalName, String contentType, byte[] content) {
        try {
            if (content == null || content.length == 0) {
                return ResultDO.buildFailResult(ErrorCodeEnum.FILE_EMPTY);
            }
            if (content.length > maxSize.toBytes()) {
                return ResultDO.buildFailResult(ErrorCodeEnum.FILE_TOO_LARGE,
                        "文件大小超过上限（" + maxSize + "）");
            }

            // 生成对象 key：yyyy/MM/dd/{uuid}.{ext}
            String key = StoragePathGenerator.generate(originalName);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(StrUtil.blankToDefault(contentType, "application/octet-stream"))
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(content));
            return ResultDO.buildSuccessResult(key);
        } catch (Exception e) {
            log.error("S3 存储文件失败, originalName: {}, bucket: {}", originalName, bucket, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.FILE_STORE_ERROR);
        }
    }

    @Override
    public ResultDO<byte[]> read(String path) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build();
            return ResultDO.buildSuccessResult(s3Client.getObjectAsBytes(request).asByteArray());
        } catch (NoSuchKeyException e) {
            log.warn("S3 对象不存在, path: {}, bucket: {}", path, bucket);
            return ResultDO.buildFailResult(ErrorCodeEnum.FILE_NOT_FOUND, "物理文件不存在");
        } catch (Exception e) {
            log.error("S3 读取文件失败, path: {}, bucket: {}", path, bucket, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.FILE_READ_ERROR);
        }
    }

    @Override
    public ResultDO<Void> delete(String path) {
        try {
            // S3 DeleteObject 天然幂等，对象不存在也返回成功
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build();
            s3Client.deleteObject(request);
            return ResultDO.buildSuccessResult();
        } catch (Exception e) {
            log.error("S3 删除文件失败, path: {}, bucket: {}", path, bucket, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.FILE_DELETE_ERROR);
        }
    }

}
