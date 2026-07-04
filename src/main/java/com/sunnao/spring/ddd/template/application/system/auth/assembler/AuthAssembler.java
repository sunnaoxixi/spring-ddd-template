package com.sunnao.spring.ddd.template.application.system.auth.assembler;

import com.sunnao.spring.ddd.template.client.system.auth.req.LoginRequestDTO;
import com.sunnao.spring.ddd.template.client.system.auth.res.GetLoginUserResponseDTO;
import com.sunnao.spring.ddd.template.client.system.auth.res.LoginResponseDTO;
import com.sunnao.spring.ddd.template.domain.system.auth.model.param.LoginParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.entity.UserEntity;

/**
 * 认证转换器
 * 负责 RequestDTO/ResponseDTO 与领域对象之间的转换
 */
public class AuthAssembler {

    private AuthAssembler() {
    }

    /**
     * 登录 RequestDTO 转领域 Param
     */
    public static LoginParam toLoginParam(LoginRequestDTO requestDTO) {
        LoginParam param = new LoginParam();
        param.setEmail(requestDTO.getEmail());
        param.setPassword(requestDTO.getPassword());
        return param;
    }

    /**
     * 聚合根 + token 信息转登录 ResponseDTO（model 枚举 → client 角色码）
     */
    public static LoginResponseDTO toLoginResponseDTO(UserAggregate aggregate, String tokenName, String tokenValue) {
        UserEntity entity = aggregate.getUserEntity();
        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setTokenName(tokenName);
        responseDTO.setTokenValue(tokenValue);
        responseDTO.setUserId(entity.getId());
        responseDTO.setNickname(entity.getNickname());
        if (entity.getRole() != null) {
            responseDTO.setRole(entity.getRole().getCode());
        }
        return responseDTO;
    }

    /**
     * 聚合根转当前登录用户信息 ResponseDTO（不含密码）
     */
    public static GetLoginUserResponseDTO toGetLoginUserResponseDTO(UserAggregate aggregate) {
        if (aggregate == null || aggregate.getUserEntity() == null) {
            return null;
        }
        UserEntity entity = aggregate.getUserEntity();
        GetLoginUserResponseDTO responseDTO = new GetLoginUserResponseDTO();
        responseDTO.setUserId(entity.getId());
        responseDTO.setEmail(entity.getEmail());
        responseDTO.setNickname(entity.getNickname());
        responseDTO.setAvatar(entity.getAvatar());
        if (entity.getRole() != null) {
            responseDTO.setRole(entity.getRole().getCode());
        }
        if (entity.getStatus() != null) {
            responseDTO.setStatus(entity.getStatus().getCode());
        }
        return responseDTO;
    }
}
