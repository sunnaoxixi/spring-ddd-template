package com.sunnao.spring.ddd.template.adaptor.system.role.input;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.sunnao.spring.ddd.template.client.system.role.RoleAppService;
import com.sunnao.spring.ddd.template.client.system.role.RoleQueryAppService;
import com.sunnao.spring.ddd.template.client.system.role.req.*;
import com.sunnao.spring.ddd.template.client.system.role.res.*;
import com.sunnao.spring.ddd.template.common.annotation.OperLog;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理 Controller（Input Adaptor）
 * 职责：接收 HTTP 请求，转换参数后调用应用层服务，禁止编写业务逻辑
 * <p>
 * 仅管理员角色可访问。
 */
@Tag(name = "角色管理", description = "角色增删改查、给用户授角色（需 admin 角色）")
@SaCheckRole("admin")
@RestController
@RequestMapping("/api/system/roles")
public class RoleController {

    @Resource
    private RoleAppService roleAppService;

    @Resource
    private RoleQueryAppService roleQueryAppService;

    /**
     * 创建角色
     */
    @Operation(summary = "创建角色")
    @OperLog(module = "role", action = "创建角色")
    @PostMapping
    public ResultDO<CreateRoleResponseDTO> createRole(@RequestBody CreateRoleRequestDTO requestDTO) {
        return roleAppService.createRole(requestDTO);
    }

    /**
     * 修改角色（名称/状态/备注）
     */
    @Operation(summary = "修改角色", description = "名称/状态/备注，roleKey 不可变更")
    @OperLog(module = "role", action = "修改角色")
    @PutMapping("/{id}")
    public ResultDO<UpdateRoleResponseDTO> updateRole(@PathVariable("id") Long id,
                                                      @RequestBody UpdateRoleRequestDTO requestDTO) {
        requestDTO.setRoleId(id);
        return roleAppService.updateRole(requestDTO);
    }

    /**
     * 删除角色（逻辑删除，内置角色不可删除）
     */
    @Operation(summary = "删除角色", description = "逻辑删除，内置角色（admin/user）不可删除")
    @OperLog(module = "role", action = "删除角色")
    @DeleteMapping("/{id}")
    public ResultDO<DeleteRoleResponseDTO> deleteRole(@PathVariable("id") Long id) {
        DeleteRoleRequestDTO requestDTO = new DeleteRoleRequestDTO();
        requestDTO.setRoleId(id);
        return roleAppService.deleteRole(requestDTO);
    }

    /**
     * 给用户授予角色（全量覆盖）
     */
    @Operation(summary = "给用户授角色", description = "全量覆盖用户的角色集合")
    @OperLog(module = "role", action = "给用户授角色")
    @PutMapping("/users/{userId}")
    public ResultDO<AssignUserRoleResponseDTO> assignUserRoles(@PathVariable("userId") Long userId,
                                                               @RequestBody AssignUserRoleRequestDTO requestDTO) {
        requestDTO.setUserId(userId);
        return roleAppService.assignUserRoles(requestDTO);
    }

    /**
     * 获取角色详情
     */
    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public ResultDO<GetRoleDetailResponseDTO> getRoleDetail(@PathVariable("id") Long id) {
        GetRoleDetailRequestDTO requestDTO = new GetRoleDetailRequestDTO();
        requestDTO.setRoleId(id);
        return roleQueryAppService.getRoleDetail(requestDTO);
    }

    /**
     * 分页查询角色列表
     */
    @Operation(summary = "分页查询角色列表")
    @GetMapping("/page")
    public ResultDO<QueryRolePageResponseDTO> queryRolePage(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "roleKey", required = false) String roleKey,
            @RequestParam(value = "roleName", required = false) String roleName,
            @RequestParam(value = "status", required = false) Integer status) {
        QueryRolePageRequestDTO requestDTO = new QueryRolePageRequestDTO();
        requestDTO.setPageNum(pageNum);
        requestDTO.setPageSize(pageSize);
        requestDTO.setRoleKey(roleKey);
        requestDTO.setRoleName(roleName);
        requestDTO.setStatus(status);
        return roleQueryAppService.queryRolePage(requestDTO);
    }
}
