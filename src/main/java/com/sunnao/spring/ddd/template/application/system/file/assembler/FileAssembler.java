package com.sunnao.spring.ddd.template.application.system.file.assembler;

import com.sunnao.spring.ddd.template.client.system.file.model.FileDTO;
import com.sunnao.spring.ddd.template.client.system.file.req.DeleteFileRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.req.QueryFilePageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.req.UploadFileRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.DownloadFileResponseDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.QueryFilePageResponseDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.UploadFileResponseDTO;
import com.sunnao.spring.ddd.template.domain.system.file.model.aggregate.FileAggregate;
import com.sunnao.spring.ddd.template.domain.system.file.model.entity.FileEntity;
import com.sunnao.spring.ddd.template.domain.system.file.model.param.CreateFileParam;
import com.sunnao.spring.ddd.template.domain.system.file.model.param.DeleteFileParam;
import com.sunnao.spring.ddd.template.domain.system.file.model.param.FileQuery;

import java.util.Collections;
import java.util.List;

/**
 * 文件转换器
 * 负责 RequestDTO/ResponseDTO 与领域对象之间的转换
 */
public class FileAssembler {

    private FileAssembler() {
    }

    /**
     * 上传 RequestDTO + 存储结果转领域 Param（操作人由应用层从当前用户上下文获取）
     */
    public static CreateFileParam toCreateParam(UploadFileRequestDTO requestDTO, String path,
                                                String storageType, Long operatorId) {
        CreateFileParam param = new CreateFileParam();
        param.setOriginalName(requestDTO.getOriginalName());
        param.setPath(path);
        param.setSize((long) requestDTO.getContent().length);
        param.setContentType(requestDTO.getContentType());
        param.setStorageType(storageType);
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 删除文件 RequestDTO 转领域 Param
     */
    public static DeleteFileParam toDeleteParam(DeleteFileRequestDTO requestDTO, Long operatorId) {
        DeleteFileParam param = new DeleteFileParam();
        param.setFileId(requestDTO.getFileId());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 分页查询 RequestDTO 转领域查询条件
     */
    public static FileQuery toFileQuery(QueryFilePageRequestDTO requestDTO) {
        FileQuery query = new FileQuery();
        query.setOriginalName(requestDTO.getOriginalName());
        query.setUploadBy(requestDTO.getUploadBy());
        return query;
    }

    /**
     * 聚合根转上传 ResponseDTO
     */
    public static UploadFileResponseDTO toUploadFileResponseDTO(FileAggregate aggregate) {
        FileEntity entity = aggregate.getFileEntity();
        UploadFileResponseDTO responseDTO = new UploadFileResponseDTO();
        responseDTO.setFileId(entity.getId());
        responseDTO.setOriginalName(entity.getOriginalName());
        responseDTO.setSize(entity.getSize());
        return responseDTO;
    }

    /**
     * 聚合根 + 物理内容转下载 ResponseDTO
     */
    public static DownloadFileResponseDTO toDownloadFileResponseDTO(FileAggregate aggregate, byte[] content) {
        FileEntity entity = aggregate.getFileEntity();
        DownloadFileResponseDTO responseDTO = new DownloadFileResponseDTO();
        responseDTO.setFileId(entity.getId());
        responseDTO.setOriginalName(entity.getOriginalName());
        responseDTO.setContentType(entity.getContentType());
        responseDTO.setSize(entity.getSize());
        responseDTO.setContent(content);
        return responseDTO;
    }

    /**
     * 聚合根转 FileDTO（不含物理内容与存储路径）
     */
    public static FileDTO toFileDTO(FileAggregate aggregate) {
        if (aggregate == null || aggregate.getFileEntity() == null) {
            return null;
        }
        FileEntity entity = aggregate.getFileEntity();
        FileDTO dto = new FileDTO();
        dto.setId(entity.getId());
        dto.setOriginalName(entity.getOriginalName());
        dto.setSize(entity.getSize());
        dto.setContentType(entity.getContentType());
        if (entity.getStorageType() != null) {
            dto.setStorageType(entity.getStorageType().getCode());
        }
        dto.setUploadBy(entity.getCreateBy());
        dto.setCreateAt(entity.getCreateAt());
        return dto;
    }

    /**
     * 聚合根列表转分页 ResponseDTO
     */
    public static QueryFilePageResponseDTO toQueryFilePageResponseDTO(long total, List<FileAggregate> aggregates) {
        QueryFilePageResponseDTO responseDTO = new QueryFilePageResponseDTO();
        responseDTO.setTotal(total);
        if (aggregates == null || aggregates.isEmpty()) {
            responseDTO.setFiles(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setFiles(aggregates.stream().map(FileAssembler::toFileDTO).toList());
        return responseDTO;
    }
}
