package com.sunnao.spring.ddd.template.domain.system.file.service;

import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.DomainService;
import com.sunnao.spring.ddd.template.domain.system.file.model.aggregate.FileAggregate;
import com.sunnao.spring.ddd.template.domain.system.file.model.param.CreateFileParam;
import com.sunnao.spring.ddd.template.domain.system.file.model.param.DeleteFileParam;

/**
 * 文件领域服务接口（写模式）
 * <p>
 * 封装文件元数据的核心业务逻辑；物理文件读写由应用层通过 FileStorage 编排。
 */
public interface FileDomainService extends DomainService {

    /**
     * 登记文件元数据（物理文件已由应用层存储成功）
     *
     * @param param 创建参数
     * @return 文件聚合根（含回填的文件ID）
     */
    ResultDO<FileAggregate> createFile(CreateFileParam param);

    /**
     * 删除文件元数据（逻辑删除）
     *
     * @param param 删除参数
     * @return 被删除的文件聚合根（供应用层清理物理文件）
     */
    ResultDO<FileAggregate> deleteFile(DeleteFileParam param);
}
