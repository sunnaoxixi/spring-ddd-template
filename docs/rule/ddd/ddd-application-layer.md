---
alwaysApply: true
---

# DDD Application 层开发规范

## 一、基础规范（所有模式通用）

### 1.1 包结构规范

application 层的标准包结构如下，按业务领域划分顶层包，领域下的子包结构保持不变：

```plain
application/
├── {业务名}/              # 按业务领域划分（如：order、booking、refund）
│   ├── scenario/          # 场景编排（AppService 实现类）
│   ├── assembler/         # 对象映射，DTO转换领域对象
│   └── model/             # 仅应用层内部使用的DTO（见 1.5 节）
```

### 1.2 Application 层核心定位

**Application 层是场景编排层，是不稳定的；领域层是核心业务逻辑层，是稳定的。**

- Application 层负责**按业务场景编排**调用领域服务、Adaptor、Repository 等，是需求变更最频繁的地方
- 领域层（DomainService、聚合根）封装**稳定的核心业务规则**，不随场景变化而变化
- 同一个领域服务方法可以被**多个 Application 场景方法**复用，不同场景的差异体现在 Application 层的编排逻辑上

#### 示例：线上创建订单 vs 手工补单

领域服务只有一个稳定的 `createOrder` 方法，但 Application 层有两个不同的场景方法：

```java
/**
 * 订单应用服务
 * 同一个领域服务的 createOrder 被两个场景方法复用
 * 场景差异体现在 Application 层的编排逻辑上
 */
@Slf4j
@Service
public class OrderAppServiceImpl implements OrderAppService {

    @Autowired
    private OrderDomainService orderDomainService;

    @Autowired
    private InventoryAdaptor inventoryAdaptor;

    @Autowired
    private PriceAdaptor priceAdaptor;

    /**
     * 场景1：线上创建订单
     * 需要验库存、验价后再创建，编排逻辑复杂
     */
    @Override
    public ResultDO<CreateOrderResponseDTO> createOrderOnline(CreateOrderOnlineRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 通过 Adaptor 验库存
            ResultDO<InventoryCheckResponseDTO> inventoryResult = inventoryAdaptor.checkInventory(
                requestDTO.getProductId(), requestDTO.getQuantity());
            if (!inventoryResult.isSuccess()) {
                return ResultDO.buildFailResult(inventoryResult.getCode(), inventoryResult.getMsg());
            }
            if (!inventoryResult.getData().isSufficient()) {
                return ResultDO.buildFailResult("INVENTORY_NOT_ENOUGH", "库存不足");
            }

            // 3. 通过 Adaptor 验价
            ResultDO<PriceCheckResponseDTO> priceResult = priceAdaptor.checkPrice(
                requestDTO.getProductId(), requestDTO.getPrice());
            if (!priceResult.isSuccess()) {
                return ResultDO.buildFailResult(priceResult.getCode(), priceResult.getMsg());
            }
            if (!priceResult.getData().isValid()) {
                return ResultDO.buildFailResult("PRICE_INVALID", "价格已变更，请刷新重试");
            }

            // 4. 组装 Param，调用领域服务创建订单（核心业务逻辑稳定不变）
            CreateOrderParam param = OrderAssembler.toParam(requestDTO);
            ResultDO<OrderAggregate> domainResult = orderDomainService.createOrder(param);
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            return ResultDO.buildSuccessResult(OrderAssembler.toResponseDTO(domainResult.getData()));
        } catch (Exception e) {
            log.error("线上创建订单系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    /**
     * 场景2：手工补单
     * 无需验库存和验价，直接调用领域服务创建订单，编排逻辑简单
     */
    @Override
    public ResultDO<CreateOrderResponseDTO> createOrderManual(CreateOrderManualRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 直接调用领域服务创建订单（复用同一个领域方法）
            CreateOrderParam param = OrderAssembler.toParam(requestDTO);
            ResultDO<OrderAggregate> domainResult = orderDomainService.createOrder(param);
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            return ResultDO.buildSuccessResult(OrderAssembler.toResponseDTO(domainResult.getData()));
        } catch (Exception e) {
            log.error("手工补单系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }
}
```

**关键理解**：

- `orderDomainService.createOrder()` 是**稳定的领域方法**，封装了创建订单的核心业务规则
- `createOrderOnline` 和 `createOrderManual` 是**不稳定的场景方法**，随业务需求变化而变化
- 未来如果新增"导入批量创建订单"场景，只需在 Application 层新增方法，领域服务无需改动

### 1.3 CQRS 分层规范（读写分离）

Application 层采用 CQRS（Command Query Responsibility Segregation）模式进行分层：

- **Command 层（写模式）**：`{聚合根名}AppService`，继承 `ApplicationCmdService`，仅处理业务变更
- **Query 层（读模式）**：`{聚合根名}QueryAppService`，继承 `ApplicationQueryService`，仅处理数据查询

**禁止交叉调用**：Query 层禁止调用 Command 层接口。

### 1.4 参数与返回值规则

| 规则项  | 规范                                                                         |
|------|----------------------------------------------------------------------------|
| 输入参数 | 必须使用 `RequestDTO` 封装参数，**禁止**使用原始类型或 Map                                   |
| 参数校验 | 通过 `requestDTO.check()` 方法实现参数自校验，返回 `ResultDO`，**禁止**在 AppService 中编写校验逻辑 |
| 返回值  | 统一返回 `ResultDO<T>`，泛型为响应 DTO                                               |
| 错误处理 | AppService 方法的返回值统一通过 `ResultDO` 封装错误码，**禁止**向调用方直接抛出异常                    |

```java
// 参数自校验（由 RequestDTO 自身负责，返回 ResultDO）
ResultDO checkResult = requestDTO.check();
if (!checkResult.isSuccess()) {
    return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
}

// 成功返回
return ResultDO.buildSuccessResult(responseDTO);

// 错误返回
return ResultDO.buildFailResult("ORDER_NOT_FOUND", "订单不存在");
```

### 1.5 DTO 存放位置规则

RequestDTO 和 ResponseDTO 的存放位置取决于其**使用范围**：

| 场景                  | 存放位置                           | 说明                                           |
|---------------------|--------------------------------|----------------------------------------------|
| 作为接口服务对外提供          | `client` 模块的 `req/` 和 `res/` 包 | 外部调用方需要依赖这些 DTO，必须定义在 client 中               |
| 仅 Application 层内部使用 | `application` 模块的 `model/` 包   | 不对外暴露，仅用于 Application 内部编排，定义在 application 中 |

**判断标准**：该 DTO 是否会被 Input Adaptor（Controller、HSF 等）直接透传给外部调用方，如果是放 `client`；如果仅在 Application 内部流转，放 `application/model/`。

#### 示例对比

**场景**：一个 AppService 方法需要先查订单、再查汇率、最后计算费用。其中"查订单详情"的接口 DTO 对外暴露，而"内部费用计算"的中间 DTO 仅在 Application 层流转。

```java
// ✅ 对外提供的接口 DTO → 定义在 client 中
package com.example.client.order.req;

/**
 * 创建订单请求DTO
 * 作为 HSF/Controller 接口参数，定义在 client 中
 */
@Data
public class CreateOrderRequestDTO {

    private String productId;
    private Integer quantity;
    private String buyerName;

    public ResultDO check() {
        if (productId == null || productId.isEmpty()) {
            return ResultDO.buildFailResult("PARAM_ERROR", "商品ID不能为空");
        }
        if (quantity == null || quantity <= 0) {
            return ResultDO.buildFailResult("PARAM_ERROR", "购买数量必须大于0");
        }
        return ResultDO.buildSuccessResult();
    }
}
```

```java
// ✅ 仅 Application 内部使用的 DTO → 定义在 application 的 model 包中
package com.example.application.order.model;

/**
 * 订单费用计算内部DTO
 * 仅用于 Application 层内部编排多个 Adaptor 结果，不对外暴露
 */
@Data
public class OrderFeeCalcInternalDTO {

    private Long orderId;
    private String currency;
    private BigDecimal baseAmount;
    private BigDecimal exchangeRate;
}
```

### 1.6 依赖管理规范

**允许依赖**：

- `domain` 模块
- `client` 模块
- `model` 模块
- 无 IO 操作的 Utils 工具类

**禁止依赖**：

- 其他业务域的二方包
- 中间件（消息中间件、OSS 等）

### 1.7 行为约束

**允许的行为**：

- 调用领域服务（DomainService）
- 使用仓储接口（Repository）
- 调用 Adaptor 获取外部数据（跨领域查询）
- DTO 与领域对象转换（通过 Assembler）

**禁止的行为**：

- 直接访问数据库（应通过 Repository）
- 直接访问外部服务（应通过 Adaptor）
- 包含核心业务逻辑（应放在 DomainService 或聚合根中）

**注意区分**：Application 层允许包含**编排流程控制**（如根据 Adaptor 返回结果决定是否继续、提前返回错误码等），这属于场景编排职责，不属于业务逻辑。核心业务逻辑指的是聚合根状态变更规则、业务计算公式、规则匹配等，这些必须放在领域层。

### 1.8 Assembler 转换类规范

**命名规范**：

- 使用 `Assembler` 后缀
- 命名格式：`{聚合根名}Assembler` 或 `{业务场景}Assembler`

**代码示例**：

```java
/**
 * 订单转换器
 * 负责 RequestDTO/ResponseDTO 与领域对象之间的转换
 */
public class OrderAssembler {

    /**
     * 将 RequestDTO 转换为领域 Param
     */
    public static CreateOrderParam toParam(CreateOrderRequestDTO requestDTO) {
        CreateOrderParam param = new CreateOrderParam();
        param.setProductId(requestDTO.getProductId());
        param.setQuantity(requestDTO.getQuantity());
        return param;
    }

    /**
     * 将聚合根转换为 ResponseDTO
     */
    public static CreateOrderResponseDTO toResponseDTO(OrderAggregate order) {
        CreateOrderResponseDTO responseDTO = new CreateOrderResponseDTO();
        responseDTO.setOrderId(order.getId());
        responseDTO.setStatus(order.getStatus());
        return responseDTO;
    }
}
```

---

## 二、写模式 Application 规范

### 2.1 命名规范

| 规则项    | 规范                                   | 示例                             |
|--------|--------------------------------------|--------------------------------|
| 接口命名   | `{聚合根名}AppService`                   | `OrderAppService`              |
| 实现类命名  | `{聚合根名}AppServiceImpl`               | `OrderAppServiceImpl`          |
| 继承规范   | 必须继承 `ApplicationCmdService`         | —                              |
| 方法命名   | 使用**业务动词**，禁止技术动词（如 `save`、`update`） | `createOrder`、`confirmPayment` |
| 请求 DTO | `{方法名}RequestDTO`                    | `CreateOrderRequestDTO`        |
| 响应 DTO | `{方法名}ResponseDTO`                   | `CreateOrderResponseDTO`       |

### 2.2 调用链

**简单场景**（如手工补单）：

```plain
Input Adaptor → AppService → DomainService → Repository
```

**复杂场景**（如线上创建订单，需调用外部服务校验）：

```plain
Input Adaptor → AppService → Adaptor（验库存/验价等）
                           → DomainService → Repository
```

Application 层在写模式中的职责是**场景编排**，不包含核心业务逻辑：

1. 参数自校验（`requestDTO.check()` 返回 `ResultDO`，校验不通过直接返回）
2. 通过 Adaptor 获取外部数据或校验前置条件（按场景需要）
3. DTO 转换为领域 Param（通过 Assembler）
4. 调用 DomainService 处理核心业务逻辑（DomainService 内部完成加载聚合根、执行业务方法、持久化）
5. 构建响应 DTO 并返回

### 2.3 完整代码示例

写模式的完整场景编排示例参见 **1.2 节**（线上创建订单 vs 手工补单），此处不再重复。

---

## 三、读模式 Application 规范

### 3.1 命名规范

| 规则项   | 规范                             | 示例                                |
|-------|--------------------------------|-----------------------------------|
| 接口命名  | `{聚合根名}QueryAppService`        | `OrderQueryAppService`            |
| 实现类命名 | `{聚合根名}QueryAppServiceImpl`    | `OrderQueryAppServiceImpl`        |
| 继承规范  | 必须继承 `ApplicationQueryService` | —                                 |
| 方法命名  | 使用动词明确查询意图，避免抽象词汇（如 `process`） | `queryOrderList`、`getOrderDetail` |

### 3.2 查询数据来源

| 场景    | 数据来源                 | 说明                                                  |
|-------|----------------------|-----------------------------------------------------|
| 领域内查询 | Repository           | 调用 Repository 获取聚合根或 Result 对象，通过 Assembler 转换为 DTO |
| 跨领域查询 | Adaptor              | 调用 Adaptor 获取第三方数据，直接返回或简单裁剪                        |
| 混合查询  | Repository + Adaptor | 先查本领域数据，再通过 Adaptor 补充外部数据，组装后返回                    |

### 3.3 完整代码示例

#### 接口定义

```java
/**
 * 订单查询应用服务接口（读模式）
 */
public interface OrderQueryAppService extends ApplicationQueryService {

    /**
     * 获取订单详情
     *
     * @param requestDTO 查询参数
     * @return 订单详情
     */
    ResultDO<GetOrderDetailResponseDTO> getOrderDetail(GetOrderDetailRequestDTO requestDTO);
}
```

#### 实现类（混合查询场景：本领域 + 跨领域）

> **说明**：领域内查询是混合查询的简化版（去掉 Adaptor 调用即可），此处以混合查询为例展示完整流程。

```java
/**
 * 订单查询应用服务实现
 * 混合查询：本领域订单数据 + 跨领域物流数据
 */
@Slf4j
@Service
public class OrderQueryAppServiceImpl implements OrderQueryAppService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private LogisticsAdaptor logisticsAdaptor;

    @Override
    public ResultDO<GetOrderDetailResponseDTO> getOrderDetail(GetOrderDetailRequestDTO requestDTO) {
        try {
            // 1. 参数自校验（规范见 1.4 节）
            ResultDO checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 查询本领域订单数据
            ResultDO<OrderAggregate> queryResult = orderRepository.findById(requestDTO.getOrderId());
            if (!queryResult.isSuccess()) {
                return ResultDO.buildFailResult(queryResult.getMsg());
            }
            OrderAggregate order = queryResult.getData();
            if (order == null) {
                return ResultDO.buildFailResult("ORDER_NOT_FOUND", "订单不存在");
            }

            // 3. 通过 Adaptor 跨领域查询物流信息
            ResultDO<LogisticsInfoResponseDTO> logisticsResult = logisticsAdaptor.queryLogistics(order.getLogisticsNo());
            if (!logisticsResult.isSuccess()) {
                return ResultDO.buildFailResult(logisticsResult.getCode(), logisticsResult.getMsg());
            }

            // 4. 组装响应 DTO
            GetOrderDetailResponseDTO responseDTO = OrderAssembler.toGetOrderDetailResponseDTO(order, logisticsResult.getData());

            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("获取订单详情失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }
}
```

---

## 四、纯计算模式 Application 规范

### 4.1 核心原则

- **一个计算方法一个类**：每个计算方法独立成一个类，职责单一
- 命名以**动词**开头，不以聚合根命名

### 4.2 命名规范

| 规则项   | 规范                             | 示例                                   |
|-------|--------------------------------|--------------------------------------|
| 接口命名  | `{动词}QueryAppService`          | `FeePreCalculateQueryAppService`     |
| 实现类命名 | `{动词}QueryAppServiceImpl`      | `FeePreCalculateQueryAppServiceImpl` |
| 继承规范  | 必须继承 `ApplicationQueryService` | —                                    |
| 方法命名  | 使用动词                           | `feePreCalculate`                    |

### 4.3 调用链

```plain
Input Adaptor → {动词}QueryAppService → DomainService → 计算并返回 Result
```

Application 层在纯计算模式中的职责：

1. 参数自校验（`requestDTO.check()` 返回 `ResultDO`，校验不通过直接返回）
2. 通过 Adaptor 获取外部数据（如需要）
3. 组装 Param，调用 DomainService 进行计算
4. 转换 Result 为 ResponseDTO 并返回

### 4.4 完整代码示例

#### 接口定义

```java
/**
 * 费用预计算 查询应用服务接口（纯计算模式）
 * 一个计算方法一个类
 */
public interface FeePreCalculateQueryAppService extends ApplicationQueryService {

    /**
     * 费用预计算
     *
     * @param requestDTO 计算参数
     * @return 计算结果
     */
    ResultDO<FeePreCalculateResponseDTO> feePreCalculate(FeePreCalculateRequestDTO requestDTO);
}
```

#### 实现类

```java
/**
 * 费用预计算 查询应用服务实现
 * 通过 Adaptor 获取外部汇率数据，再调用 DomainService 计算
 */
@Slf4j
@Service
public class FeePreCalculateQueryAppServiceImpl implements FeePreCalculateQueryAppService {

    @Autowired
    private ExchangeRateAdaptor exchangeRateAdaptor;

    @Autowired
    private FeeCalculateDomainService feeCalculateDomainService;

    @Override
    public ResultDO<FeePreCalculateResponseDTO> feePreCalculate(FeePreCalculateRequestDTO requestDTO) {
        try {
            // 1. 参数自校验（规范见 1.4 节）
            ResultDO checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 通过 Adaptor 跨领域查询汇率
            ResultDO<ExchangeRateResponseDTO> rateResult = exchangeRateAdaptor.queryExchangeRate(requestDTO.getCurrency());
            if (!rateResult.isSuccess()) {
                return ResultDO.buildFailResult(rateResult.getCode(), rateResult.getMsg());
            }

            // 3. 组装计算参数
            FeeCalculateParam param = FeePreCalculateAssembler.toParam(requestDTO, rateResult.getData());

            // 4. 调用 DomainService 进行计算
            ResultDO<FeeCalculateResult> domainResult = feeCalculateDomainService.calculate(param);
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 5. 转换为响应 DTO
            return ResultDO.buildSuccessResult(FeePreCalculateAssembler.toResponseDTO(domainResult.getData()));
        } catch (Exception e) {
            log.error("费用预计算失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }
}
```

---

## 五、规则+计算模式 Application 规范

### 5.1 核心原则

- 同纯计算模式：**一个计算方法一个类**，职责单一
- 命名以**动词**开头，不以聚合根命名

### 5.2 命名规范

| 规则项   | 规范                             | 示例                                       |
|-------|--------------------------------|------------------------------------------|
| 接口命名  | `{动词}QueryAppService`          | `SubsidyPreCalculateQueryAppService`     |
| 实现类命名 | `{动词}QueryAppServiceImpl`      | `SubsidyPreCalculateQueryAppServiceImpl` |
| 继承规范  | 必须继承 `ApplicationQueryService` | —                                        |
| 方法命名  | 使用动词                           | `subsidyPreCalculate`                    |

### 5.3 调用链

```plain
Input Adaptor → {动词}QueryAppService → DomainService → Repository查询规则聚合根
                                                     → 调用聚合根.matchRule/calculate → 返回Result
```

Application 层在规则+计算模式中的职责：

1. 参数自校验（`requestDTO.check()` 返回 `ResultDO`，校验不通过直接返回）
2. 通过 Adaptor 获取外部数据（如需要）
3. 组装 Param，调用 DomainService 匹配规则并计算
4. 转换 Result 为 ResponseDTO 并返回

### 5.4 完整代码示例

#### 接口定义

```java
/**
 * 补贴预计算 查询应用服务接口（规则+计算模式）
 * 一个计算方法一个类
 */
public interface SubsidyPreCalculateQueryAppService extends ApplicationQueryService {

    /**
     * 补贴预计算
     *
     * @param requestDTO 计算参数
     * @return 计算结果
     */
    ResultDO<SubsidyPreCalculateResponseDTO> subsidyPreCalculate(SubsidyPreCalculateRequestDTO requestDTO);
}
```

#### 实现类

```java
/**
 * 补贴预计算 查询应用服务实现
 * 通过 Adaptor 获取外部用户等级数据，再调用 DomainService 匹配规则并计算
 */
@Slf4j
@Service
public class SubsidyPreCalculateQueryAppServiceImpl implements SubsidyPreCalculateQueryAppService {

    @Autowired
    private UserLevelAdaptor userLevelAdaptor;

    @Autowired
    private SubsidyCalculateDomainService subsidyCalculateDomainService;

    @Override
    public ResultDO<SubsidyPreCalculateResponseDTO> subsidyPreCalculate(SubsidyPreCalculateRequestDTO requestDTO) {
        try {
            // 1. 参数自校验（规范见 1.4 节）
            ResultDO checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 通过 Adaptor 跨领域查询用户等级
            ResultDO<UserLevelResponseDTO> levelResult = userLevelAdaptor.queryUserLevel(requestDTO.getUserId());
            if (!levelResult.isSuccess()) {
                return ResultDO.buildFailResult(levelResult.getCode(), levelResult.getMsg());
            }

            // 3. 组装计算参数
            SubsidyCalculateParam param = SubsidyPreCalculateAssembler.toParam(requestDTO, levelResult.getData());

            // 4. 调用 DomainService 匹配规则并计算
            ResultDO<SubsidyCalculateResult> domainResult = subsidyCalculateDomainService.calculate(param);
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 5. 转换为响应 DTO
            return ResultDO.buildSuccessResult(SubsidyPreCalculateAssembler.toResponseDTO(domainResult.getData()));
        } catch (Exception e) {
            log.error("补贴预计算失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }
}
```
