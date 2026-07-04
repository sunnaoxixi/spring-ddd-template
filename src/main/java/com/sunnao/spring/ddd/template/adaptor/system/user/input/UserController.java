package com.sunnao.spring.ddd.template.adaptor.system.user.input;

import com.sunnao.spring.ddd.template.client.system.user.UserAppService;
import com.sunnao.spring.ddd.template.client.system.user.UserQueryAppService;
import com.sunnao.spring.ddd.template.client.system.user.req.ChangeUserStatusRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.CreateUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.DeleteUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.GetUserDetailRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.QueryUserPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.UpdateUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.ChangeUserStatusResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.CreateUserResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.DeleteUserResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.GetUserDetailResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.QueryUserPageResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.UpdateUserResponseDTO;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理 Controller（Input Adaptor）
 * 职责：接收 HTTP 请求，转换参数后调用应用层服务，禁止编写业务逻辑
 * <p>
 * 用户管理仅管理员可访问（Sa-Token 角色鉴权）。
 */
@Tag(name = "用户管理", description = "系统用户增删改查（仅管理员）")
@SaCheckRole("admin")
@RestController
@RequestMapping("/api/system/users")
public class UserController {

    @Resource
    private UserAppService userAppService;

    @Resource
    private UserQueryAppService userQueryAppService;

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户")
    @PostMapping
    public ResultDO<CreateUserResponseDTO> createUser(@RequestBody CreateUserRequestDTO requestDTO) {
        return userAppService.createUser(requestDTO);
    }

    /**
     * 修改用户资料
     */
    @Operation(summary = "修改用户资料")
    @PutMapping("/{id}")
    public ResultDO<UpdateUserResponseDTO> updateUser(@PathVariable Long id,
                                                      @RequestBody UpdateUserRequestDTO requestDTO) {
        requestDTO.setUserId(id);
        return userAppService.updateUser(requestDTO);
    }

    /**
     * 变更用户状态（启用/禁用）
     */
    @Operation(summary = "变更用户状态", description = "启用/禁用")
    @PutMapping("/{id}/status")
    public ResultDO<ChangeUserStatusResponseDTO> changeUserStatus(@PathVariable("id") Long id,
                                                                  @RequestBody ChangeUserStatusRequestDTO requestDTO) {
        requestDTO.setUserId(id);
        return userAppService.changeUserStatus(requestDTO);
    }

    /**
     * 删除用户（逻辑删除）
     */
    @Operation(summary = "删除用户", description = "逻辑删除")
    @DeleteMapping("/{id}")
    public ResultDO<DeleteUserResponseDTO> deleteUser(@PathVariable("id") Long id) {
        DeleteUserRequestDTO requestDTO = new DeleteUserRequestDTO();
        requestDTO.setUserId(id);
        return userAppService.deleteUser(requestDTO);
    }

    /**
     * 获取用户详情
     */
    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public ResultDO<GetUserDetailResponseDTO> getUserDetail(@PathVariable("id") Long id) {
        GetUserDetailRequestDTO requestDTO = new GetUserDetailRequestDTO();
        requestDTO.setUserId(id);
        return userQueryAppService.getUserDetail(requestDTO);
    }

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public ResultDO<QueryUserPageResponseDTO> queryUserPage(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "status", required = false) Integer status) {
        QueryUserPageRequestDTO requestDTO = new QueryUserPageRequestDTO();
        requestDTO.setPageNum(pageNum);
        requestDTO.setPageSize(pageSize);
        requestDTO.setEmail(email);
        requestDTO.setNickname(nickname);
        requestDTO.setStatus(status);
        return userQueryAppService.queryUserPage(requestDTO);
    }
}
