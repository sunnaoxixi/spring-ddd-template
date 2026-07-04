package com.sunnao.spring.ddd.template.adaptor.system.file.output;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.application.system.file.FileStorage;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.model.system.file.FileStorageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 本地磁盘文件存储实现（Output Adaptor）
 * <p>
 * 实现 application 层定义的 FileStorage 接口（依赖倒置）。
 * 存储根目录与单文件大小上限走配置（app.file.local.base-path / app.file.max-size）；
 * 存储路径格式：yyyy/MM/dd/{uuid}.{ext}，日期分目录避免单目录文件过多。
 * OSS 等云存储可另行实现 FileStorage 并通过 @ConditionalOnProperty 等方式切换。
 */
@Slf4j
@Component
public class LocalFileStorage implements FileStorage {

    private static final DateTimeFormatter DATE_DIR_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /** 存储根目录 */
    @Value("${app.file.local.base-path:./data/files}")
    private String basePath;

    /** 单文件大小上限 */
    @Value("${app.file.max-size:10MB}")
    private DataSize maxSize;

    @Override
    public ResultDO<String> store(String originalName, byte[] content) {
        try {
            if (content == null || content.length == 0) {
                return ResultDO.buildFailResult("FILE_EMPTY", "文件内容不能为空");
            }
            if (content.length > maxSize.toBytes()) {
                return ResultDO.buildFailResult("FILE_TOO_LARGE",
                        "文件大小超过上限（" + maxSize + "）");
            }

            // 生成相对路径：yyyy/MM/dd/{uuid}.{ext}
            String relativePath = LocalDate.now().format(DATE_DIR_FORMATTER)
                    + "/" + IdUtil.fastSimpleUUID() + extractExtension(originalName);

            Path target = resolveSafely(relativePath);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return ResultDO.buildSuccessResult(relativePath);
        } catch (Exception e) {
            log.error("存储文件失败, originalName: {}", originalName, e);
            return ResultDO.buildFailResult("FILE_STORE_ERROR", "文件存储失败");
        }
    }

    @Override
    public ResultDO<byte[]> read(String path) {
        try {
            Path target = resolveSafely(path);
            if (!Files.exists(target)) {
                return ResultDO.buildFailResult("FILE_NOT_FOUND", "物理文件不存在");
            }
            return ResultDO.buildSuccessResult(Files.readAllBytes(target));
        } catch (IllegalArgumentException e) {
            log.warn("读取文件路径不合法, path: {}", path, e);
            return ResultDO.buildFailResult("FILE_PATH_INVALID", "文件路径不合法");
        } catch (Exception e) {
            log.error("读取文件失败, path: {}", path, e);
            return ResultDO.buildFailResult("FILE_READ_ERROR", "文件读取失败");
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
            return ResultDO.buildFailResult("FILE_PATH_INVALID", "文件路径不合法");
        } catch (Exception e) {
            log.error("删除文件失败, path: {}", path, e);
            return ResultDO.buildFailResult("FILE_DELETE_ERROR", "文件删除失败");
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

    /**
     * 提取文件扩展名（含点，无扩展名返回空串）
     */
    private String extractExtension(String originalName) {
        if (StrUtil.isBlank(originalName)) {
            return "";
        }
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalName.length() - 1) {
            return "";
        }
        String ext = originalName.substring(dotIndex);
        // 扩展名仅保留常规字符，防止注入特殊路径字符
        return ext.matches("\\.[A-Za-z0-9]{1,10}") ? ext.toLowerCase() : "";
    }
}
