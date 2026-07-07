package com.sunnao.spring.ddd.template.adaptor.system.user.input;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.sunnao.spring.ddd.template.client.system.user.UserAppService;
import com.sunnao.spring.ddd.template.client.system.user.UserQueryAppService;
import com.sunnao.spring.ddd.template.client.system.user.req.*;
import com.sunnao.spring.ddd.template.client.system.user.res.*;
import com.sunnao.spring.ddd.template.common.annotation.OperLog;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理 Controller（Input Adaptor）
 * 职责：接收 HTTP 请求，转换参数后调用应用层服务，禁止编写业务逻辑
 * <p>
 * 按权限点鉴权（Sa-Token）：查询需 system:user:read，写操作需 system:user:write。
 */
@Tag(name = "用户管理", description = "系统用户增删改查（需 system:user:read / system:user:write 权限）")
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
    @OperLog(module = "user", action = "创建用户")
    @SaCheckPermission("system:user:write")
    @PostMapping
    public ResultDO<CreateUserResponseDTO> createUser(@RequestBody CreateUserRequestDTO requestDTO) {
        return userAppService.createUser(requestDTO);
    }

    /**
     * 修改用户资料
     */
    @Operation(summary = "修改用户资料")
    @OperLog(module = "user", action = "修改用户资料")
    @SaCheckPermission("system:user:write")
    @PutMapping("/{id}")
    public ResultDO<UpdateUserResponseDTO> updateUser(@PathVariable("id") Long id,
                                                      @RequestBody UpdateUserRequestDTO requestDTO) {
        requestDTO.setUserId(id);
        return userAppService.updateUser(requestDTO);
    }

    /**
     * 变更用户状态（启用/禁用）
     */
    @Operation(summary = "变更用户状态", description = "启用/禁用")
    @OperLog(module = "user", action = "变更用户状态")
    @SaCheckPermission("system:user:write")
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
    @OperLog(module = "user", action = "删除用户")
    @SaCheckPermission("system:user:write")
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
    @SaCheckPermission("system:user:read")
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
    @SaCheckPermission("system:user:read")
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
