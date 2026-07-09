package com.sunnao.spring.ddd.template.application.system.online.scenario;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.client.system.online.OnlineQueryAppService;
import com.sunnao.spring.ddd.template.client.system.online.res.OnlineUserDTO;
import com.sunnao.spring.ddd.template.client.system.online.req.QueryOnlineUserPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.online.res.QueryOnlineUserPageResponseDTO;
import com.sunnao.spring.ddd.template.common.context.LoginSessionKeys;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 在线用户查询应用服务实现（读模式）
 * <p>
 * 数据源为 Sa-Token Redis 会话（非数据库），无领域层；Sa-Token 调用收敛在应用层。
 * 先全量扫描 token 键并过滤无效会话，再在有效列表上做内存分页，
 * 确保 total 与当页数据一致；适用于中小规模会话量。
 */
@Slf4j
@Service
public class OnlineQueryAppServiceImpl implements OnlineQueryAppService {

    @Override
    public ResultDO<QueryOnlineUserPageResponseDTO> queryOnlineUserPage(QueryOnlineUserPageRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 全量扫描 token 键，逐个过滤无效会话，得到有效在线用户列表
            List<String> allTokenKeys = StpUtil.searchTokenValue("", 0, -1, false);
            List<OnlineUserDTO> validOnlineUsers = new ArrayList<>();
            if (allTokenKeys != null) {
                for (String tokenKey : allTokenKeys) {
                    OnlineUserDTO dto = buildOnlineUser(stripTokenKeyPrefix(tokenKey));
                    if (dto != null) {
                        validOnlineUsers.add(dto);
                    }
                }
            }

            // 3. 在有效列表上做内存分页
            int startIndex = (requestDTO.getPageNum() - 1) * requestDTO.getPageSize();
            int endIndex = Math.min(startIndex + requestDTO.getPageSize(), validOnlineUsers.size());
            List<OnlineUserDTO> pageOnlineUsers = startIndex < validOnlineUsers.size()
                    ? validOnlineUsers.subList(startIndex, endIndex)
                    : new ArrayList<>();

            // 4. 组装响应 DTO
            QueryOnlineUserPageResponseDTO responseDTO = new QueryOnlineUserPageResponseDTO();
            responseDTO.setTotal((long) validOnlineUsers.size());
            responseDTO.setOnlineUsers(pageOnlineUsers);
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("分页查询在线用户失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * searchTokenValue 返回的是完整 Redis 键（含前缀），剥离出裸 token 值
     */
    private String stripTokenKeyPrefix(String tokenKey) {
        String prefix = StpUtil.stpLogic.splicingKeyTokenValue("");
        return tokenKey.startsWith(prefix) ? tokenKey.substring(prefix.length()) : tokenKey;
    }

    /**
     * 按 token 组装在线会话信息；token 已失效返回 null（调用方跳过），
     * 会话附加信息缺失（如登录时未写入）时仅返回基础字段。
     */
    private OnlineUserDTO buildOnlineUser(String tokenValue) {
        Object loginId = StpUtil.getLoginIdByToken(tokenValue);
        if (loginId == null) {
            return null;
        }

        OnlineUserDTO dto = new OnlineUserDTO();
        dto.setTokenValue(tokenValue);
        dto.setUserId(Convert.toLong(loginId));

        SaSession tokenSession = StpUtil.stpLogic.getTokenSessionByToken(tokenValue, false);
        if (tokenSession != null) {
            dto.setEmail(tokenSession.getString(LoginSessionKeys.EMAIL));
            dto.setNickname(tokenSession.getString(LoginSessionKeys.NICKNAME));
            dto.setIp(tokenSession.getString(LoginSessionKeys.IP));
            dto.setUserAgent(tokenSession.getString(LoginSessionKeys.USER_AGENT));
            String loginTime = tokenSession.getString(LoginSessionKeys.LOGIN_TIME);
            if (StrUtil.isNotBlank(loginTime)) {
                dto.setLoginTime(LocalDateTime.parse(loginTime));
            }
        }
        return dto;
    }
}
