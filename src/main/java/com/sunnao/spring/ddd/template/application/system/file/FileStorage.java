package com.sunnao.spring.ddd.template.application.system.file;

import com.sunnao.spring.ddd.template.common.result.ResultDO;

/**
 * 文件存储抽象（outAdaptor 接口）
 * <p>
 * 依赖倒置：接口定义在 application 层，S3 具体实现在 adaptor 层。
 * 所有方法返回 ResultDO，不向调用方抛出异常。
 */
public interface FileStorage {

    /**
     * 存储文件
     *
     * @param originalName 原始文件名（用于提取扩展名，存储路径由实现生成保证唯一）
     * @param contentType  文件 MIME 类型（可为空；写入 S3 对象元数据）
     * @param content      文件内容
     * @return S3 对象 key
     */
    ResultDO<String> store(String originalName, String contentType, byte[] content);

    /**
     * 读取文件内容
     *
     * @param path S3 对象 key
     * @return 文件内容
     */
    ResultDO<byte[]> read(String path);

    /**
     * 删除物理文件（幂等，文件不存在视为成功）
     *
     * @param path S3 对象 key
     * @return 删除结果
     */
    ResultDO<Void> delete(String path);
}
