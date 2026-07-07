package com.sunnao.spring.ddd.template.application.system.file.scenario;

import com.sunnao.spring.ddd.template.application.system.file.FileStorage;
import com.sunnao.spring.ddd.template.application.system.file.assembler.FileAssembler;
import com.sunnao.spring.ddd.template.client.system.file.FileQueryAppService;
import com.sunnao.spring.ddd.template.client.system.file.req.DownloadFileRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.req.QueryFilePageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.DownloadFileResponseDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.QueryFilePageResponseDTO;
import com.sunnao.spring.ddd.template.common.model.PageQuery;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.file.model.aggregate.FileAggregate;
import com.sunnao.spring.ddd.template.domain.system.file.model.param.FileQuery;
import com.sunnao.spring.ddd.template.domain.system.file.repository.FileRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * 文件查询应用服务实现（读模式）
 * 职责：元数据经 Repository 查询，物理内容经 FileStorage 读取，Assembler 组装 DTO
 */
@Slf4j
@Service
public class FileQueryAppServiceImpl implements FileQueryAppService {

    @Resource
    private FileRepository fileRepository;

    @Resource
    private FileStorage fileStorage;

    @Override
    public ResultDO<DownloadFileResponseDTO> downloadFile(DownloadFileRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 查询文件元数据
            FileAggregate aggregate = fileRepository.query(requestDTO.getFileId());
            if (aggregate == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.FILE_NOT_FOUND);
            }

            // 3. 读取物理内容（outAdaptor）
            ResultDO<byte[]> readResult = fileStorage.read(aggregate.getFileEntity().getPath());
            if (!readResult.isSuccess()) {
                return ResultDO.buildFailResult(readResult.getCode(), readResult.getMsg());
            }

            // 4. 组装响应 DTO
            return ResultDO.buildSuccessResult(
                    FileAssembler.toDownloadFileResponseDTO(aggregate, readResult.getData()));
        } catch (Exception e) {
            log.error("下载文件失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public ResultDO<QueryFilePageResponseDTO> queryFilePage(QueryFilePageRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 组装分页查询条件（pageNum 从1开始 → startIndex）
            PageQuery<FileQuery> pageQuery = PageQuery.build(FileAssembler.toFileQuery(requestDTO));
            pageQuery.setStartIndex((requestDTO.getPageNum() - 1) * requestDTO.getPageSize());
            pageQuery.setPageSize(requestDTO.getPageSize());

            // 3. 查询本领域文件分页数据
            Page<FileAggregate> page = fileRepository.queryPage(pageQuery);

            // 4. 组装响应 DTO
            return ResultDO.buildSuccessResult(
                    FileAssembler.toQueryFilePageResponseDTO(page.getTotalElements(), page.getContent()));
        } catch (Exception e) {
            log.error("分页查询文件失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }
}
