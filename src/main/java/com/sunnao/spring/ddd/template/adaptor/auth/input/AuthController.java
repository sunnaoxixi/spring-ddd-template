package com.sunnao.spring.ddd.template.adaptor.auth.input;

import com.sunnao.spring.ddd.template.client.auth.AuthAppService;
import com.sunnao.spring.ddd.template.client.auth.AuthQueryAppService;
import com.sunnao.spring.ddd.template.client.auth.req.LoginRequestDTO;
import com.sunnao.spring.ddd.template.client.auth.res.GetLoginUserResponseDTO;
import com.sunnao.spring.ddd.template.client.auth.res.LoginResponseDTO;
import com.sunnao.spring.ddd.template.common.annotation.OperLog;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 认证 Controller（Input Adaptor�?
 * 职责：接�?HTTP 请求，转换参数后调用应用层服务，禁止编写业务逻辑
 */
@Tag(name = "认证", description = "登录 / 登出 / 当前用户信息")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private AuthAppService authAppService;

    @Resource
    private AuthQueryAppService authQueryAppService;

    /**
     * 登录
     */
    @Operation(summary = "登录", description = "邮箱 + 密码登录，返�?satoken")
    @OperLog(module = "auth", action = "登录")
    @PostMapping("/login")
    public ResultDO<LoginResponseDTO> login(@RequestBody LoginRequestDTO requestDTO) {
        return authAppService.login(requestDTO);
    }

    /**
     * 登出
     */
    @Operation(summary = "登出")
    @PostMapping("/logout")
    public ResultDO<Void> logout() {
        return authAppService.logout();
    }

    /**
     * 获取当前登录用户信息
     */
    @Operation(summary = "当前登录用户信息")
    @GetMapping("/me")
    public ResultDO<GetLoginUserResponseDTO> getLoginUserInfo() {
        return authQueryAppService.getLoginUserInfo();
    }
}
