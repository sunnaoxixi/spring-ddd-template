package com.sunnao.spring.ddd.template.client.system.file;

import com.sunnao.spring.ddd.template.client.system.file.req.DeleteFileRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.req.UploadFileRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.DeleteFileResponseDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.UploadFileResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.ApplicationCmdService;

/**
 * 文件应用服务接口（写模式）
 * 职责：定义文件上传/删除接口
 */
public interface FileAppService extends ApplicationCmdService {

    /**
     * 上传文件（存储物理文件 + 登记元数据）
     *
     * @param requestDTO 请求参数
     * @return 上传结果
     */
    ResultDO<UploadFileResponseDTO> uploadFile(UploadFileRequestDTO requestDTO);

    /**
     * 删除文件（逻辑删除元数据 + 清理物理文件）
     *
     * @param requestDTO 请求参数
     * @return 删除结果
     */
    ResultDO<DeleteFileResponseDTO> deleteFile(DeleteFileRequestDTO requestDTO);
}
