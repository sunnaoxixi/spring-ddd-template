package com.sunnao.spring.ddd.template.adaptor.system.dict.input;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.sunnao.spring.ddd.template.client.system.dict.DictAppService;
import com.sunnao.spring.ddd.template.client.system.dict.DictQueryAppService;
import com.sunnao.spring.ddd.template.client.system.dict.req.CreateDictDataRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.CreateDictTypeRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.DeleteDictDataRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.DeleteDictTypeRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.QueryDictDataListRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.QueryDictTypePageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.UpdateDictDataRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.UpdateDictTypeRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.CreateDictDataResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.CreateDictTypeResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.DeleteDictDataResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.DeleteDictTypeResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.QueryDictDataListResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.QueryDictTypePageResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.UpdateDictDataResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.UpdateDictTypeResponseDTO;
import com.sunnao.spring.ddd.template.common.annotation.OperLog;
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
 * 字典管理 Controller（Input Adaptor）
 * 职责：接收 HTTP 请求，转换参数后调用应用层服务，禁止编写业务逻辑
 * <p>
 * 写操作与管理端查询仅管理员可用；按 typeKey 查询启用数据所有登录用户可用（走 Redis 缓存）。
 */
@Tag(name = "字典管理", description = "字典类型/数据 CRUD（管理员），按类型键查询（登录用户）")
@RestController
@RequestMapping("/api/system/dicts")
public class DictController {

    @Resource
    private DictAppService dictAppService;

    @Resource
    private DictQueryAppService dictQueryAppService;

    /**
     * 创建字典类型
     */
    @Operation(summary = "创建字典类型")
    @OperLog(module = "dict", action = "创建字典类型")
    @SaCheckRole("admin")
    @PostMapping("/types")
    public ResultDO<CreateDictTypeResponseDTO> createDictType(@RequestBody CreateDictTypeRequestDTO requestDTO) {
        return dictAppService.createDictType(requestDTO);
    }

    /**
     * 修改字典类型（名称/状态/备注）
     */
    @Operation(summary = "修改字典类型", description = "名称/状态/备注，typeKey 不可变更")
    @OperLog(module = "dict", action = "修改字典类型")
    @SaCheckRole("admin")
    @PutMapping("/types/{id}")
    public ResultDO<UpdateDictTypeResponseDTO> updateDictType(@PathVariable("id") Long id,
                                                              @RequestBody UpdateDictTypeRequestDTO requestDTO) {
        requestDTO.setTypeId(id);
        return dictAppService.updateDictType(requestDTO);
    }

    /**
     * 删除字典类型（逻辑删除，同时删除其下字典数据）
     */
    @Operation(summary = "删除字典类型", description = "逻辑删除，同时删除其下字典数据")
    @OperLog(module = "dict", action = "删除字典类型")
    @SaCheckRole("admin")
    @DeleteMapping("/types/{id}")
    public ResultDO<DeleteDictTypeResponseDTO> deleteDictType(@PathVariable("id") Long id) {
        DeleteDictTypeRequestDTO requestDTO = new DeleteDictTypeRequestDTO();
        requestDTO.setTypeId(id);
        return dictAppService.deleteDictType(requestDTO);
    }

    /**
     * 分页查询字典类型列表（管理端）
     */
    @Operation(summary = "分页查询字典类型列表")
    @SaCheckRole("admin")
    @GetMapping("/types/page")
    public ResultDO<QueryDictTypePageResponseDTO> queryDictTypePage(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "typeKey", required = false) String typeKey,
            @RequestParam(value = "typeName", required = false) String typeName,
            @RequestParam(value = "status", required = false) Integer status) {
        QueryDictTypePageRequestDTO requestDTO = new QueryDictTypePageRequestDTO();
        requestDTO.setPageNum(pageNum);
        requestDTO.setPageSize(pageSize);
        requestDTO.setTypeKey(typeKey);
        requestDTO.setTypeName(typeName);
        requestDTO.setStatus(status);
        return dictQueryAppService.queryDictTypePage(requestDTO);
    }

    /**
     * 创建字典数据
     */
    @Operation(summary = "创建字典数据")
    @OperLog(module = "dict", action = "创建字典数据")
    @SaCheckRole("admin")
    @PostMapping("/data")
    public ResultDO<CreateDictDataResponseDTO> createDictData(@RequestBody CreateDictDataRequestDTO requestDTO) {
        return dictAppService.createDictData(requestDTO);
    }

    /**
     * 修改字典数据（标签/值/排序/状态/备注）
     */
    @Operation(summary = "修改字典数据", description = "标签/值/排序/状态/备注，typeKey 不可变更")
    @OperLog(module = "dict", action = "修改字典数据")
    @SaCheckRole("admin")
    @PutMapping("/data/{id}")
    public ResultDO<UpdateDictDataResponseDTO> updateDictData(@PathVariable("id") Long id,
                                                              @RequestBody UpdateDictDataRequestDTO requestDTO) {
        requestDTO.setDataId(id);
        return dictAppService.updateDictData(requestDTO);
    }

    /**
     * 删除字典数据（逻辑删除）
     */
    @Operation(summary = "删除字典数据", description = "逻辑删除")
    @OperLog(module = "dict", action = "删除字典数据")
    @SaCheckRole("admin")
    @DeleteMapping("/data/{id}")
    public ResultDO<DeleteDictDataResponseDTO> deleteDictData(@PathVariable("id") Long id) {
        DeleteDictDataRequestDTO requestDTO = new DeleteDictDataRequestDTO();
        requestDTO.setDataId(id);
        return dictAppService.deleteDictData(requestDTO);
    }

    /**
     * 按类型键查询启用状态的字典数据列表（所有登录用户，走 Redis 缓存）
     */
    @Operation(summary = "按类型键查询字典数据", description = "仅启用项，走 Redis 缓存，所有登录用户可用")
    @GetMapping("/data")
    public ResultDO<QueryDictDataListResponseDTO> queryDictDataList(
            @RequestParam("typeKey") String typeKey) {
        QueryDictDataListRequestDTO requestDTO = new QueryDictDataListRequestDTO();
        requestDTO.setTypeKey(typeKey);
        return dictQueryAppService.queryDictDataList(requestDTO);
    }

    /**
     * 按类型键查询全部字典数据列表（管理端，含禁用项，不走缓存）
     */
    @Operation(summary = "按类型键查询全部字典数据", description = "含禁用项，不走缓存（管理端）")
    @SaCheckRole("admin")
    @GetMapping("/data/all")
    public ResultDO<QueryDictDataListResponseDTO> queryAllDictDataList(
            @RequestParam("typeKey") String typeKey) {
        QueryDictDataListRequestDTO requestDTO = new QueryDictDataListRequestDTO();
        requestDTO.setTypeKey(typeKey);
        return dictQueryAppService.queryAllDictDataList(requestDTO);
    }
}
