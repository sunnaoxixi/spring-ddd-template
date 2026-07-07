package com.sunnao.spring.ddd.template.adaptor.system.online.input;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.sunnao.spring.ddd.template.client.system.online.OnlineAppService;
import com.sunnao.spring.ddd.template.client.system.online.OnlineQueryAppService;
import com.sunnao.spring.ddd.template.client.system.online.req.KickOnlineUserByTokenRequestDTO;
import com.sunnao.spring.ddd.template.client.system.online.req.KickOnlineUserByUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.online.req.QueryOnlineUserPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.online.res.QueryOnlineUserPageResponseDTO;
import com.sunnao.spring.ddd.template.common.annotation.OperLog;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在线用户 Controller（Input Adaptor）
 * 职责：接收 HTTP 请求，转换参数后调用应用层服务，禁止编写业务逻辑
 * <p>
 * 在线用户管理仅管理员可访问（Sa-Token 角色鉴权）。
 */
@Tag(name = "在线用户", description = "在线会话查询与强制下线（仅管理员）")
@SaCheckRole("admin")
@RestController
@RequestMapping("/api/system/online")
public class OnlineController {

    @Resource
    private OnlineQueryAppService onlineQueryAppService;

    @Resource
    private OnlineAppService onlineAppService;

    /**
     * 分页查询在线用户（会话）
     */
    @Operation(summary = "分页查询在线用户", description = "一个用户多端登录对应多条会话记录")
    @GetMapping("/page")
    public ResultDO<QueryOnlineUserPageResponseDTO> queryOnlineUserPage(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        QueryOnlineUserPageRequestDTO requestDTO = new QueryOnlineUserPageRequestDTO();
        requestDTO.setPageNum(pageNum);
        requestDTO.setPageSize(pageSize);
        return onlineQueryAppService.queryOnlineUserPage(requestDTO);
    }

    /**
     * 按会话 token 踢下线
     */
    @Operation(summary = "按会话踢下线", description = "仅影响单个会话")
    @OperLog(module = "online", action = "按会话踢下线")
    @DeleteMapping("/tokens/{tokenValue}")
    public ResultDO<Void> kickByToken(@PathVariable("tokenValue") String tokenValue) {
        KickOnlineUserByTokenRequestDTO requestDTO = new KickOnlineUserByTokenRequestDTO();
        requestDTO.setTokenValue(tokenValue);
        return onlineAppService.kickByToken(requestDTO);
    }

    /**
     * 按用户踢下线全部会话
     */
    @Operation(summary = "按用户踢下线", description = "踢下线该用户全部在线会话")
    @OperLog(module = "online", action = "按用户踢下线")
    @DeleteMapping("/users/{userId}")
    public ResultDO<Void> kickByUser(@PathVariable("userId") Long userId) {
        KickOnlineUserByUserRequestDTO requestDTO = new KickOnlineUserByUserRequestDTO();
        requestDTO.setUserId(userId);
        return onlineAppService.kickByUser(requestDTO);
    }
}
