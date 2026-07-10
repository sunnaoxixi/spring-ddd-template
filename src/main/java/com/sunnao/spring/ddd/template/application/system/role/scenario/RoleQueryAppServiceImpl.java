package com.sunnao.spring.ddd.template.application.system.role.scenario;

import com.sunnao.spring.ddd.template.application.system.role.assembler.RoleAssembler;
import com.sunnao.spring.ddd.template.client.system.role.RoleQueryAppService;
import com.sunnao.spring.ddd.template.client.system.role.req.GetRoleDetailRequestDTO;
import com.sunnao.spring.ddd.template.client.system.role.req.QueryRolePageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.role.res.GetRoleDetailResponseDTO;
import com.sunnao.spring.ddd.template.client.system.role.res.QueryRolePageResponseDTO;
import com.sunnao.spring.ddd.template.common.model.PageQuery;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.role.model.aggregate.RoleAggregate;
import com.sunnao.spring.ddd.template.domain.system.role.model.param.RoleQuery;
import com.sunnao.spring.ddd.template.domain.system.role.repository.RoleRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * 角色查询应用服务实现（读模式）
 * 职责：领域内查询，通过 Repository 获取聚合根后经 Assembler 转换为 DTO
 */
@Slf4j
@Service
public class RoleQueryAppServiceImpl implements RoleQueryAppService {

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private RoleAssembler roleAssembler;

    @Override
    public ResultDO<GetRoleDetailResponseDTO> getRoleDetail(GetRoleDetailRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 查询本领域角色数据
            RoleAggregate aggregate = roleRepository.query(requestDTO.getRoleId());
            if (aggregate == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.ROLE_NOT_FOUND);
            }

            // 3. 组装响应 DTO
            return ResultDO.buildSuccessResult(roleAssembler.toGetRoleDetailResponseDTO(aggregate));
        } catch (Exception e) {
            log.error("获取角色详情失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public ResultDO<QueryRolePageResponseDTO> queryRolePage(QueryRolePageRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 组装分页查询条件（pageNum 从1开始 → startIndex）
            PageQuery<RoleQuery> pageQuery = PageQuery.build(roleAssembler.toRoleQuery(requestDTO));
            pageQuery.setStartIndex((requestDTO.getPageNum() - 1) * requestDTO.getPageSize());
            pageQuery.setPageSize(requestDTO.getPageSize());

            // 3. 查询本领域角色分页数据
            Page<RoleAggregate> page = roleRepository.queryPage(pageQuery);

            // 4. 组装响应 DTO
            return ResultDO.buildSuccessResult(
                    roleAssembler.toQueryRolePageResponseDTO(page.getTotalElements(), page.getContent()));
        } catch (Exception e) {
            log.error("分页查询角色失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }
}
