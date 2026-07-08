package com.sunnao.spring.ddd.template.application.auth.assembler;

import com.sunnao.spring.ddd.template.client.auth.req.LoginRequestDTO;
import com.sunnao.spring.ddd.template.client.auth.req.RegisterRequestDTO;
import com.sunnao.spring.ddd.template.client.auth.res.GetLoginUserResponseDTO;
import com.sunnao.spring.ddd.template.client.auth.res.LoginResponseDTO;
import com.sunnao.spring.ddd.template.client.auth.res.RegisterResponseDTO;
import com.sunnao.spring.ddd.template.domain.auth.model.param.LoginParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.entity.UserEntity;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.CreateUserParam;
import com.sunnao.spring.ddd.template.model.system.user.UserStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Collections;

/**
 * 认证转换器
 * 负责 RequestDTO/ResponseDTO 与领域对象之间的转换
 */
@Mapper(componentModel = "spring")
public interface AuthAssembler {

    /**
     * 登录 RequestDTO 转领域 Param
     */
    LoginParam toLoginParam(LoginRequestDTO requestDTO);

    /**
     * 注册 RequestDTO 转创建用户领域 Param
     * <p>
     * 自助注册：无头像、默认 user 角色（roleIds 为空）、无操作人（operatorId 为空）；
     * confirmPassword 仅用于 DTO 自校验，不传入领域层。
     */
    default CreateUserParam toCreateUserParam(RegisterRequestDTO requestDTO) {
        CreateUserParam param = new CreateUserParam();
        param.setEmail(requestDTO.getEmail());
        param.setNickname(requestDTO.getNickname());
        param.setPassword(requestDTO.getPassword());
        return param;
    }

    /**
     * 聚合根 + token 信息转注册 ResponseDTO（角色标识由 createUser 已填充）
     */
    default RegisterResponseDTO toRegisterResponseDTO(UserAggregate aggregate, String tokenName, String tokenValue) {
        UserEntity entity = aggregate.getUserEntity();
        RegisterResponseDTO responseDTO = new RegisterResponseDTO();
        responseDTO.setTokenName(tokenName);
        responseDTO.setTokenValue(tokenValue);
        responseDTO.setUserId(entity.getId());
        responseDTO.setNickname(entity.getNickname());
        responseDTO.setRoles(entity.getRoles() == null ? Collections.emptyList() : entity.getRoles());
        return responseDTO;
    }

    /**
     * 聚合根 + token 信息转登录 ResponseDTO（model 枚举 → client 角色码）
     */
    default LoginResponseDTO toLoginResponseDTO(UserAggregate aggregate, String tokenName, String tokenValue) {
        UserEntity entity = aggregate.getUserEntity();
        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setTokenName(tokenName);
        responseDTO.setTokenValue(tokenValue);
        responseDTO.setUserId(entity.getId());
        responseDTO.setNickname(entity.getNickname());
        responseDTO.setRoles(entity.getRoles() == null ? Collections.emptyList() : entity.getRoles());
        return responseDTO;
    }

    /**
     * 聚合根转当前登录用户信息 ResponseDTO（不含密码）
     */
    default GetLoginUserResponseDTO toGetLoginUserResponseDTO(UserAggregate aggregate) {
        if (aggregate == null || aggregate.getUserEntity() == null) {
            return null;
        }
        UserEntity entity = aggregate.getUserEntity();
        GetLoginUserResponseDTO responseDTO = new GetLoginUserResponseDTO();
        responseDTO.setUserId(entity.getId());
        responseDTO.setEmail(entity.getEmail());
        responseDTO.setNickname(entity.getNickname());
        responseDTO.setAvatar(entity.getAvatar());
        responseDTO.setRoles(entity.getRoles() == null ? Collections.emptyList() : entity.getRoles());
        if (entity.getStatus() != null) {
            responseDTO.setStatus(entity.getStatus().getCode());
        }
        return responseDTO;
    }

    // ========== 枚举转换辅助方法 ==========

    @Named("userStatusToInt")
    default Integer userStatusToInt(UserStatusEnum status) {
        return status == null ? null : status.getCode();
    }
}
