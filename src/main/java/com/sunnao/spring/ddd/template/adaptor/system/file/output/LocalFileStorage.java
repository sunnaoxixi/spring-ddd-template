package com.sunnao.spring.ddd.template.adaptor.system.file.output;

import com.sunnao.spring.ddd.template.application.system.file.FileStorage;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.model.system.file.FileStorageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地磁盘文件存储实现（Output Adaptor）
 * <p>
 * 实现 application 层定义的 FileStorage 接口（依赖倒置）。
 * 存储根目录与单文件大小上限走配置（app.file.local.base-path / app.file.max-size）；
 * 存储路径格式：yyyy/MM/dd/{uuid}.{ext}，日期分目录避免单目录文件过多。
 * 通过 app.file.storage-type 切换存储实现：local-本实现（默认）｜s3-S3FileStorage。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.file.storage-type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorage implements FileStorage {

    /**
     * 存储根目录
     */
    @Value("${app.file.local.base-path:./data/files}")
    private String basePath;

    /**
     * 单文件大小上限
     */
    @Value("${app.file.max-size:10MB}")
    private DataSize maxSize;

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

            // 生成相对路径：yyyy/MM/dd/{uuid}.{ext}（contentType 本地存储不使用）
            String relativePath = StoragePathGenerator.generate(originalName);

            Path target = resolveSafely(relativePath);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return ResultDO.buildSuccessResult(relativePath);
        } catch (Exception e) {
            log.error("存储文件失败, originalName: {}", originalName, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.FILE_STORE_ERROR);
        }
    }

    @Override
    public ResultDO<byte[]> read(String path) {
        try {
            Path target = resolveSafely(path);
            if (!Files.exists(target)) {
                return ResultDO.buildFailResult(ErrorCodeEnum.FILE_NOT_FOUND, "物理文件不存在");
            }
            return ResultDO.buildSuccessResult(Files.readAllBytes(target));
        } catch (IllegalArgumentException e) {
            log.warn("读取文件路径不合法, path: {}", path, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.FILE_PATH_INVALID);
        } catch (Exception e) {
            log.error("读取文件失败, path: {}", path, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.FILE_READ_ERROR);
        }
    }

    @Override
    public ResultDO<Void> delete(String path) {
        try {
            Path target = resolveSafely(path);
            Files.deleteIfExists(target);
            return ResultDO.buildSuccessResult();
        } catch (IllegalArgumentException e) {
            log.warn("删除文件路径不合法, path: {}", path, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.FILE_PATH_INVALID);
        } catch (Exception e) {
            log.error("删除文件失败, path: {}", path, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.FILE_DELETE_ERROR);
        }
    }

    @Override
    public String getStorageType() {
        return FileStorageTypeEnum.LOCAL.getCode();
    }

    /**
     * 解析相对路径为绝对路径，并校验未逃逸出存储根目录（防路径穿越）
     */
    private Path resolveSafely(String relativePath) {
        Path root = Paths.get(basePath).toAbsolutePath().normalize();
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("路径逃逸出存储根目录: " + relativePath);
        }
        return target;
    }
}
