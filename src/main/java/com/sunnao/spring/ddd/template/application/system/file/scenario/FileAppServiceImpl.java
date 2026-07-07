package com.sunnao.spring.ddd.template.application.system.file.scenario;

import com.sunnao.spring.ddd.template.application.system.file.FileStorage;
import com.sunnao.spring.ddd.template.application.system.file.assembler.FileAssembler;
import com.sunnao.spring.ddd.template.client.system.file.FileAppService;
import com.sunnao.spring.ddd.template.client.system.file.req.DeleteFileRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.req.UploadFileRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.DeleteFileResponseDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.UploadFileResponseDTO;
import com.sunnao.spring.ddd.template.common.context.CurrentUserContext;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.file.model.aggregate.FileAggregate;
import com.sunnao.spring.ddd.template.domain.system.file.service.FileDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 文件应用服务实现（写模式）
 * 职责：场景编排，参数自校验 → FileStorage 存物理文件 → 领域服务登记元数据 → 组装响应；
 * 元数据登记失败时回滚物理文件（尽力而为）
 */
@Slf4j
@Service
public class FileAppServiceImpl implements FileAppService {

    @Resource
    private FileDomainService fileDomainService;

    @Resource
    private FileAssembler fileAssembler;

    @Resource
    private FileStorage fileStorage;

    @Override
    public ResultDO<UploadFileResponseDTO> uploadFile(UploadFileRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 存储物理文件（outAdaptor，路径由存储实现生成）
            ResultDO<String> storeResult = fileStorage.store(
                    requestDTO.getOriginalName(), requestDTO.getContentType(), requestDTO.getContent());
            if (!storeResult.isSuccess()) {
                return ResultDO.buildFailResult(storeResult.getCode(), storeResult.getMsg());
            }
            String path = storeResult.getData();

            // 3. 调用领域服务登记元数据（操作人取自当前登录用户）
            ResultDO<FileAggregate> domainResult = fileDomainService.createFile(
                    fileAssembler.toCreateParam(requestDTO, path,
                            fileStorage.getStorageType(), CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                // 元数据登记失败，回滚物理文件（尽力而为，失败仅记录日志）
                fileStorage.delete(path);
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 4. 组装响应
            return ResultDO.buildSuccessResult(
                    fileAssembler.toUploadFileResponseDTO(domainResult.getData()));
        } catch (Exception e) {
            log.error("上传文件系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public ResultDO<DeleteFileResponseDTO> deleteFile(DeleteFileRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务逻辑删除元数据（操作人取自当前登录用户）
            ResultDO<FileAggregate> domainResult = fileDomainService.deleteFile(
                    fileAssembler.toDeleteParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 清理物理文件（尽力而为，失败仅记录日志，不影响删除结果）
            ResultDO<Void> deleteResult = fileStorage.delete(
                    domainResult.getData().getFileEntity().getPath());
            if (!deleteResult.isSuccess()) {
                log.warn("清理物理文件失败, fileId: {}, path: {}, msg: {}", requestDTO.getFileId(),
                        domainResult.getData().getFileEntity().getPath(), deleteResult.getMsg());
            }

            // 4. 组装响应
            DeleteFileResponseDTO responseDTO = new DeleteFileResponseDTO();
            responseDTO.setFileId(requestDTO.getFileId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("删除文件系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }
}
