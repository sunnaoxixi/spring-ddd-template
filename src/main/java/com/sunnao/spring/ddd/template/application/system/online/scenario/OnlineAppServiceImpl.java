package com.sunnao.spring.ddd.template.application.system.online.scenario;

import cn.dev33.satoken.stp.StpUtil;
import com.sunnao.spring.ddd.template.client.system.online.OnlineAppService;
import com.sunnao.spring.ddd.template.client.system.online.req.KickOnlineUserByTokenRequestDTO;
import com.sunnao.spring.ddd.template.client.system.online.req.KickOnlineUserByUserRequestDTO;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 在线用户应用服务实现（写模式）
 * <p>
 * 强制下线基于 Sa-Token kickout：被踢会话的 token 标记为已被踢下线，
 * 对应用户下次携带该 token 请求时收到未登录响应。Sa-Token 调用收敛在应用层。
 */
@Slf4j
@Service
public class OnlineAppServiceImpl implements OnlineAppService {

    @Override
    public ResultDO<Void> kickByToken(KickOnlineUserByTokenRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 校验会话有效性（已过期/不存在的 token 直接提示）
            if (StpUtil.getLoginIdByToken(requestDTO.getTokenValue()) == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.NOT_FOUND, "会话不存在或已失效");
            }

            // 3. 踢下线该会话
            StpUtil.kickoutByTokenValue(requestDTO.getTokenValue());
            return ResultDO.buildSuccessResult();
        } catch (Exception e) {
            log.error("按会话踢人失败", e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public ResultDO<Void> kickByUser(KickOnlineUserByUserRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 校验该用户是否存在在线会话
            List<String> tokens = StpUtil.getTokenValueListByLoginId(requestDTO.getUserId());
            if (tokens == null || tokens.isEmpty()) {
                return ResultDO.buildFailResult(ErrorCodeEnum.NOT_FOUND, "该用户当前不在线");
            }

            // 3. 踢下线该用户全部会话
            StpUtil.kickout(requestDTO.getUserId());
            return ResultDO.buildSuccessResult();
        } catch (Exception e) {
            log.error("按用户踢人失败, userId: {}", requestDTO.getUserId(), e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }
}
