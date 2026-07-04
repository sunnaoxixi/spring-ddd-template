package com.sunnao.spring.ddd.template.client.system.file;

import com.sunnao.spring.ddd.template.client.system.file.req.DownloadFileRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.req.QueryFilePageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.DownloadFileResponseDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.QueryFilePageResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.ApplicationQueryService;

/**
 * 文件查询应用服务接口（读模式）
 * 职责：定义文件下载/分页查询接口
 */
public interface FileQueryAppService extends ApplicationQueryService {

    /**
     * 下载文件（元数据 + 物理内容）
     *
     * @param requestDTO 请求参数
     * @return 文件内容
     */
    ResultDO<DownloadFileResponseDTO> downloadFile(DownloadFileRequestDTO requestDTO);

    /**
     * 分页查询文件列表
     *
     * @param requestDTO 请求参数
     * @return 分页结果
     */
    ResultDO<QueryFilePageResponseDTO> queryFilePage(QueryFilePageRequestDTO requestDTO);
}
