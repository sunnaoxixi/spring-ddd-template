package com.sunnao.spring.ddd.template.adaptor.system.file.output;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 存储路径生成器（Output Adaptor 内部工具）
 * <p>
 * 生成 S3 对象 key：yyyy/MM/dd/{uuid}.{ext}，
 * 日期分目录避免单目录文件过多，UUID 保证路径唯一。
 */
final class StoragePathGenerator {

    private static final DateTimeFormatter DATE_DIR_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private StoragePathGenerator() {
    }

    /**
     * 生成存储路径：yyyy/MM/dd/{uuid}.{ext}
     *
     * @param originalName 原始文件名（仅用于提取扩展名）
     * @return S3 对象 key
     */
    static String generate(String originalName) {
        return LocalDate.now().format(DATE_DIR_FORMATTER)
                + "/" + IdUtil.fastSimpleUUID() + extractExtension(originalName);
    }

    /**
     * 提取文件扩展名（含点，无扩展名返回空串；扩展名仅保留常规字符，防止注入特殊路径字符）
     */
    static String extractExtension(String originalName) {
        if (StrUtil.isBlank(originalName)) {
            return "";
        }
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalName.length() - 1) {
            return "";
        }
        String ext = originalName.substring(dotIndex);
        return ext.matches("\\.[A-Za-z0-9]{1,10}") ? ext.toLowerCase() : "";
    }
}
