package com.sunnao.spring.ddd.template.client.system.user;

import com.sunnao.spring.ddd.template.client.system.user.req.ChangeUserStatusRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.CreateUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.DeleteUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.UpdateUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.ChangeUserStatusResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.CreateUserResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.DeleteUserResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.UpdateUserResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.ApplicationCmdService;

/**
 * 用户应用服务接口（写模式）
 * 职责：定义用户相关的写操作接口
 */
public interface UserAppService extends ApplicationCmdService {

    /**
     * 创建用户
     *
     * @param requestDTO 请求参数
     * @return 创建结果
     */
    ResultDO<CreateUserResponseDTO> createUser(CreateUserRequestDTO requestDTO);

    /**
     * 修改用户资料
     *
     * @param requestDTO 请求参数
     * @return 修改结果
     */
    ResultDO<UpdateUserResponseDTO> updateUser(UpdateUserRequestDTO requestDTO);

    /**
     * 变更用户状态（启用/禁用）
     *
     * @param requestDTO 请求参数
     * @return 变更结果
     */
    ResultDO<ChangeUserStatusResponseDTO> changeUserStatus(ChangeUserStatusRequestDTO requestDTO);

    /**
     * 删除用户（逻辑删除）
     *
     * @param requestDTO 请求参数
     * @return 删除结果
     */
    ResultDO<DeleteUserResponseDTO> deleteUser(DeleteUserRequestDTO requestDTO);
}
