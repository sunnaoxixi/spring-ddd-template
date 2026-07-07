package com.sunnao.spring.ddd.template.application.system.user.scenario;

import com.sunnao.spring.ddd.template.application.system.user.assembler.UserAssembler;
import com.sunnao.spring.ddd.template.client.system.user.UserQueryAppService;
import com.sunnao.spring.ddd.template.client.system.user.req.GetUserDetailRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.QueryUserPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.GetUserDetailResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.QueryUserPageResponseDTO;
import com.sunnao.spring.ddd.template.common.model.PageQuery;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.role.repository.RoleRepository;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.UserQuery;
import com.sunnao.spring.ddd.template.domain.system.user.repository.UserRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户查询应用服务实现（读模式）
 * 职责：领域内查询，通过 Repository 获取聚合根后经 Assembler 转换为 DTO
 */
@Slf4j
@Service
public class UserQueryAppServiceImpl implements UserQueryAppService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RoleRepository roleRepository;

    @Override
    public ResultDO<GetUserDetailResponseDTO> getUserDetail(GetUserDetailRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 查询本领域用户数据
            UserAggregate aggregate = userRepository.query(requestDTO.getUserId());
            if (aggregate == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.USER_NOT_FOUND);
            }

            // 3. 填充角色标识（RBAC，取自 role 领域）后组装响应 DTO
            aggregate.getUserEntity().setRoles(
                    roleRepository.queryRoleKeysByUserId(requestDTO.getUserId()));
            return ResultDO.buildSuccessResult(UserAssembler.toGetUserDetailResponseDTO(aggregate));
        } catch (Exception e) {
            log.error("获取用户详情失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public ResultDO<QueryUserPageResponseDTO> queryUserPage(QueryUserPageRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 组装分页查询条件（pageNum 从1开始 → startIndex）
            PageQuery<UserQuery> pageQuery = PageQuery.build(UserAssembler.toUserQuery(requestDTO));
            pageQuery.setStartIndex((requestDTO.getPageNum() - 1) * requestDTO.getPageSize());
            pageQuery.setPageSize(requestDTO.getPageSize());

            // 3. 查询本领域用户分页数据
            Page<UserAggregate> page = userRepository.queryPage(pageQuery);

            // 4. 批量填充角色标识（RBAC，取自 role 领域，避免逐条查询）
            List<UserAggregate> aggregates = page.getContent();
            if (!aggregates.isEmpty()) {
                List<Long> userIds = aggregates.stream()
                        .map(aggregate -> aggregate.getUserEntity().getId()).toList();
                Map<Long, List<String>> roleKeysByUserId = roleRepository.queryRoleKeysByUserIds(userIds);
                aggregates.forEach(aggregate -> aggregate.getUserEntity().setRoles(
                        roleKeysByUserId.getOrDefault(aggregate.getUserEntity().getId(),
                                Collections.emptyList())));
            }

            // 5. 组装响应 DTO
            return ResultDO.buildSuccessResult(
                    UserAssembler.toQueryUserPageResponseDTO(page.getTotalElements(), aggregates));
        } catch (Exception e) {
            log.error("分页查询用户失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }
}
