package com.sunnao.spring.ddd.template.adaptor.system.file.input;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.sunnao.spring.ddd.template.client.system.file.FileAppService;
import com.sunnao.spring.ddd.template.client.system.file.FileQueryAppService;
import com.sunnao.spring.ddd.template.client.system.file.req.DeleteFileRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.req.DownloadFileRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.req.QueryFilePageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.req.UploadFileRequestDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.DeleteFileResponseDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.DownloadFileResponseDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.QueryFilePageResponseDTO;
import com.sunnao.spring.ddd.template.client.system.file.res.UploadFileResponseDTO;
import com.sunnao.spring.ddd.template.common.annotation.OperLog;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * 文件管理 Controller（Input Adaptor）
 * 职责：接收 HTTP 请求，MultipartFile 在本层转换为 DTO 后调用应用层服务，禁止编写业务逻辑
 * <p>
 * 管理员和普通用户角色均可访问。
 */
@Slf4j
@Tag(name = "文件管理", description = "上传、下载、删除和分页查询（需 admin 或 user 角色）")
@SaCheckRole(value = {"admin", "user"}, mode = SaMode.OR)
@RestController
@RequestMapping("/api/system/files")
public class FileController {

    @Resource
    private FileAppService fileAppService;

    @Resource
    private FileQueryAppService fileQueryAppService;

    /**
     * 上传文件（multipart）
     */
    @Operation(summary = "上传文件", description = "multipart/form-data，字段名 file")
    @OperLog(module = "file", action = "上传文件")
    @PostMapping
    public ResultDO<UploadFileResponseDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        // MultipartFile 不越过 adaptor 层，此处转换为自包含 DTO
        UploadFileRequestDTO requestDTO = new UploadFileRequestDTO();
        try {
            requestDTO.setOriginalName(file.getOriginalFilename());
            requestDTO.setContentType(file.getContentType());
            requestDTO.setContent(file.getBytes());
        } catch (Exception e) {
            log.error("读取上传文件内容失败, name: {}", file.getOriginalFilename(), e);
            return ResultDO.buildFailResult(ErrorCodeEnum.FILE_READ_ERROR, "读取上传文件内容失败");
        }
        return fileAppService.uploadFile(requestDTO);
    }

    /**
     * 下载文件
     */
    @Operation(summary = "下载文件", description = "返回文件二进制流")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable("id") Long id) {
        DownloadFileRequestDTO requestDTO = new DownloadFileRequestDTO();
        requestDTO.setFileId(id);
        ResultDO<DownloadFileResponseDTO> result = fileQueryAppService.downloadFile(requestDTO);
        if (!result.isSuccess()) {
            HttpStatus status = ErrorCodeEnum.FILE_NOT_FOUND.getCode().equals(result.getCode())
                    ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }

        DownloadFileResponseDTO data = result.getData();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(data.getOriginalName(), StandardCharsets.UTF_8).build());
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            if (data.getContentType() != null) {
                mediaType = MediaType.parseMediaType(data.getContentType());
            }
        } catch (Exception ignored) {
            // 非法 contentType 回退为二进制流
        }
        headers.setContentType(mediaType);
        return ResponseEntity.ok().headers(headers).body(data.getContent());
    }

    /**
     * 删除文件（逻辑删除元数据 + 清理物理文件）
     */
    @Operation(summary = "删除文件", description = "逻辑删除元数据并清理物理文件")
    @OperLog(module = "file", action = "删除文件")
    @DeleteMapping("/{id}")
    public ResultDO<DeleteFileResponseDTO> deleteFile(@PathVariable("id") Long id) {
        DeleteFileRequestDTO requestDTO = new DeleteFileRequestDTO();
        requestDTO.setFileId(id);
        return fileAppService.deleteFile(requestDTO);
    }

    /**
     * 分页查询文件列表
     */
    @Operation(summary = "分页查询文件列表")
    @GetMapping("/page")
    public ResultDO<QueryFilePageResponseDTO> queryFilePage(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "originalName", required = false) String originalName,
            @RequestParam(value = "uploadBy", required = false) Long uploadBy) {
        QueryFilePageRequestDTO requestDTO = new QueryFilePageRequestDTO();
        requestDTO.setPageNum(pageNum);
        requestDTO.setPageSize(pageSize);
        requestDTO.setOriginalName(originalName);
        requestDTO.setUploadBy(uploadBy);
        return fileQueryAppService.queryFilePage(requestDTO);
    }
}
