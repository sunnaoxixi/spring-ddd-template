package com.sunnao.spring.ddd.template.adaptor.system.log.input;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.sunnao.spring.ddd.template.client.system.log.LogQueryAppService;
import com.sunnao.spring.ddd.template.client.system.log.req.QueryLoginLogPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.log.req.QueryOperLogPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.log.res.QueryLoginLogPageResponseDTO;
import com.sunnao.spring.ddd.template.client.system.log.res.QueryOperLogPageResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 系统日志 Controller（Input Adaptor）
 * 职责：接收 HTTP 请求，转换参数后调用应用层服务，禁止编写业务逻辑
 * <p>
 * 日志仅管理员可查询；操作日志由 @OperLog 切面、登录日志由登录流程事件异步写入，不提供写接口。
 */
@Tag(name = "系统日志", description = "操作日志 / 登录日志分页查询（仅管理员）")
@SaCheckRole("admin")
@RestController
@RequestMapping("/api/system/logs")
public class LogController {

    @Resource
    private LogQueryAppService logQueryAppService;

    /**
     * 分页查询操作日志
     */
    @Operation(summary = "分页查询操作日志", description = "按操作时间倒序，支持模块/操作人/时间范围过滤")
    @GetMapping("/page")
    public ResultDO<QueryOperLogPageResponseDTO> queryOperLogPage(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "operatorId", required = false) Long operatorId,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        QueryOperLogPageRequestDTO requestDTO = new QueryOperLogPageRequestDTO();
        requestDTO.setPageNum(pageNum);
        requestDTO.setPageSize(pageSize);
        requestDTO.setModule(module);
        requestDTO.setOperatorId(operatorId);
        requestDTO.setStartTime(startTime);
        requestDTO.setEndTime(endTime);
        return logQueryAppService.queryOperLogPage(requestDTO);
    }

    /**
     * 分页查询登录日志
     */
    @Operation(summary = "分页查询登录日志", description = "按登录时间倒序，支持邮箱/用户/成功标识/时间范围过滤")
    @GetMapping("/login/page")
    public ResultDO<QueryLoginLogPageResponseDTO> queryLoginLogPage(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "success", required = false) Boolean success,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        QueryLoginLogPageRequestDTO requestDTO = new QueryLoginLogPageRequestDTO();
        requestDTO.setPageNum(pageNum);
        requestDTO.setPageSize(pageSize);
        requestDTO.setEmail(email);
        requestDTO.setUserId(userId);
        requestDTO.setSuccess(success);
        requestDTO.setStartTime(startTime);
        requestDTO.setEndTime(endTime);
        return logQueryAppService.queryLoginLogPage(requestDTO);
    }
}
