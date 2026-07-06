---
alwaysApply: true
---

# adaptor 层开发规范

## 一、基础规范（所有模式通用）

### 1.1 角色定位

- 防腐层（ACL）：隔离外部技术细节（第三方服务、中间件、框架），防止污染领域层和应用层
- 双向适配：
    - 输入适配（Input Adaptor）：将外部请求（如HTTP、消息队列）转换为应用层可理解的指令
    - 输出适配（Output Adaptor）：将领域层的数据格式转换为外部服务所需的协议（如API调用）

### 1.2 包结构规范

adaptor 层的标准包结构如下，按业务领域划分顶层包，领域下的 `input/`、`output/`、`inner/` 子包结构保持不变：

```plain
adaptor/
├── {业务名}/              # 按业务领域划分（如：order、booking、refund）
│   ├── input/             # 提供服务，controller，mtop等
│   ├── output/            # 适配调用外部第三方接口
│   └── inner/             # 内部复用的adaptor
```

### 1.3 分层与职责

| 包路径                     | 职责                                       | 依赖关系                                                                              |
|-------------------------|------------------------------------------|-----------------------------------------------------------------------------------|
| `adaptor/{业务名}/input/`  | 处理外部入口请求（如Controller、MQ消费者、定时任务），调用应用层服务 | 调用 AppService 接口（如BookAppService，接口定义在 client 层、实现在 application 层），禁止绕过应用层直接调用领域层 |
| `adaptor/{业务名}/output/` | 实现外部第三方服务调用（如RPC、HTTP、支付网关），封装技术细节       | 实现application层定义的Adaptor接口（见 1.4 节）                                               |
| `adaptor/{业务名}/inner/`  | 适配器内部复用逻辑（如公共认证、日志拦截器），避免重复代码            | 仅被其他适配器调用，禁止被领域层或应用层直接依赖                                                          |

### 1.4 Adaptor 接口定义核心原则

⚠️ **最重要的原则：Adaptor 接口定义必须基于 Application 层的业务需要来定义，而不是根据第三方的接口来定义。**

- Adaptor 接口的方法签名、参数、返回值必须反映**调用方（Application）的业务语义**，而非外部服务的技术细节
- 接口命名和方法命名应体现**调用方的业务意图**，而非被调用方的 API 名称
- 即使底层调用的是同一个第三方接口，如果业务意图不同，也应定义为不同的 Adaptor 方法
- Adaptor 内部可以**组合调用多个第三方服务**，也可以只调用一个，对调用方透明
- 外部服务的请求/响应格式差异由 Adaptor 实现类内部通过 Converter 处理，不暴露给调用方
- **核心价值**：第三方接口变化时，只需修改 Adaptor 实现类内部的转换逻辑，不影响 Application 层的调用

**接口定义权归属：**

- Adaptor 接口定义在 Application 模块，方法签名由 Application 层的业务需要决定
- **Repository 直接操作自己领域的数据库（通过 Mapper/DAO），不通过 Adaptor**
- 非自己领域的外部服务调用，统一在 Application 层通过 Adaptor 完成

**参数与返回值规则：**

- **输入参数**：可以是 Application 方法的 RequestDTO 对象，也可以是基础数据类型（如 `long orderId`、`String logisticsNo`）
    - 参数**确定且稳定**时（如订单编号、物流编号等业务标识），优先使用基础数据类型，提高 Adaptor 的复用性
    - 参数**较多或需要组合**时，直接使用 Application 方法的 RequestDTO 对象作为 Adaptor 参数，无需为 Adaptor 单独定义新的
      DTO
- **返回值**：统一返回 `ResultDO<T>`，泛型为 ResponseDTO 对象，仅包含 Application 需要的字段（防腐简化）

### 1.5 实现约束

- 所有适配器实现必须位于 Adaptor 层，与接口定义分离
- 禁止在适配器中编写业务逻辑，仅做协议转换
- Adaptor 实现类内部负责将业务语义的参数/返回值与第三方接口格式互相转换

### 1.6 Adaptor 层允许使用设计模式做技术适配

与领域层**禁止使用设计模式**不同（详见 `ddd-domain-layer.md` 1.3 节），Adaptor 层**允许使用设计模式**来处理技术适配问题。

#### 允许原因

Adaptor 层的职责是**技术适配和防腐**，不是表达业务规则。当需要基于渠道ID、供应商类型等技术维度路由到不同的第三方接口时，这属于
**技术层面的路由选择**，使用策略模式或路由模式是合理的。

#### 典型场景

| 场景                       | 做法                                     | 说明                 |
|--------------------------|----------------------------------------|--------------------|
| 基于渠道ID路由不同的第三方报价接口       | Output Adaptor 实现类中用 `if/else` 或策略模式路由 | 这是技术适配，不是业务逻辑      |
| 基于供应商类型调用不同的下单接口         | Output Adaptor 实现类中按供应商类型选择不同的 Client  | 对 Application 层透明  |
| 不同协议的接口适配（HTTP/HSF/gRPC） | Output Adaptor 实现类中按协议类型选择不同的调用方式      | 技术细节封装在 Adaptor 内部 |

#### 与领域层的区分

| 维度       | 领域层（Domain）              | 适配器层（Adaptor）                 |
|----------|--------------------------|-------------------------------|
| **设计模式** | ❌ 禁止使用                   | ✅ 允许使用                        |
| **分支依据** | 业务规则（如国内/国际、乘客类型）        | 技术维度（如渠道ID、供应商类型、协议类型）        |
| **处理方式** | 直接用 `if/else` 在领域对象中内聚处理 | 可用 `if/else` 或策略模式路由到不同的第三方服务 |
| **核心区别** | 业务逻辑必须内聚可见               | 技术细节可以隔离封装                    |

### 1.7 调用链控制

**流程1**（纯领域操作）：

```plain
input adaptor(可选) → application → domain → infrastructure（直接操作自己的数据库）
```

**流程2**（需获取外部数据后再执行领域逻辑）：

```plain
input adaptor(可选) → application → output adaptor（获取外部数据）
                                  → domain → infrastructure
```

**流程3**（纯外部调用）：

```plain
input adaptor(可选) → application → output adaptor
```

### 1.8 命名规则

- 接口名：`{业务能力}Adaptor`（如 `LogisticsAdaptor`）
- 实现类名：`{业务能力}AdaptorImpl`（如 `LogisticsAdaptorImpl`）
- 方法名：动词（如 `queryLogistics`）

### 1.9 转换类规范

- 类名：`{业务名}Converter`（如 `LogisticsConverter`），负责第三方接口格式与业务DTO的互转

### 1.10 Input Adaptor 入口类型

Input Adaptor 支持以下四种入口类型，所有模式通用：

| 入口类型         | 说明                  | 类命名                    |
|--------------|---------------------|------------------------|
| Controller   | HTTP 接口（Spring MVC） | `{业务名}Controller`      |
| HSF 服务       | 阿里巴巴 HSF RPC 服务提供者  | `{业务名}HsfServiceImpl`  |
| RocketMQ 消费者 | RocketMQ 消息消费       | `{业务名}MessageConsumer` |
| ScheduleX 任务 | ScheduleX 分布式任务调度   | `{业务名}ScheduleTask`    |

**通用规则：**

- 所有入口类型的 Input Adaptor 职责相同：接收外部请求，转换参数，调用 Application 层服务
- 禁止在 Input Adaptor 中编写业务逻辑
- 不同模式调用的 Application 服务不同（见各模式章节）

各模式的完整代码模板见第二~五章。

## 二、写模式 adaptor 规范

### 2.1 调用链

```plain
Input Adaptor → {聚合根名}AppService → DomainService → Repository（直接操作自己的数据库）
```

### 2.2 Input Adaptor 规范

写模式 Input Adaptor 调用 `{聚合根名}AppService`（继承 `ApplicationCmdService`）。

### 2.3 Output Adaptor 规范

写模式的核心是自己领域模型的状态变更。但在实际场景中，写操作前可能需要通过 Output Adaptor 调用外部服务进行前置校验（如校验库存、验价等），这属于
Application 层的**场景编排**职责。Adaptor 参数与返回值遵循 1.4 节核心规则。

> 参见 `ddd-application-layer.md` 1.2 节示例：`createOrderOnline` 场景在调用 DomainService 前，先通过 `InventoryAdaptor` 和
`PriceAdaptor` 进行前置校验。

---

## 三、读模式 adaptor 规范

### 3.1 调用链

**领域内查询：**

```plain
Input Adaptor → {聚合根名}QueryAppService → Repository（直接操作自己的数据库）
```

**跨领域查询：**

```plain
Input Adaptor → {聚合根名}QueryAppService → Output Adaptor
```

### 3.2 Input Adaptor 规范

读模式 Input Adaptor 调用 `{聚合根名}QueryAppService`（继承 `ApplicationQueryService`）。

### 3.3 Output Adaptor 规范

Adaptor 参数与返回值遵循 1.4 节核心规则。

#### Output Adaptor 接口定义示例（读模式）

```java
/**
 * 物流信息适配器接口
 * 参数使用基础数据类型（物流编号确定且稳定），提高复用性
 * 返回值按 Application 需要的模型定义
 */
public interface LogisticsAdaptor {

    ResultDO<LogisticsInfoResponseDTO> queryLogistics(String logisticsNo);
}
```

#### Output Adaptor 实现示例（读模式）

```java
/**
 * 物流信息适配器实现
 * 内部封装第三方物流服务调用，可组合调用多个第三方
 * 第三方接口变化时只需修改此实现类，不影响 Application 层
 */
@Component
public class LogisticsAdaptorImpl implements LogisticsAdaptor {

    @Autowired
    private ThirdPartyLogisticsClient logisticsClient;

    @Override
    public ResultDO<LogisticsInfoResponseDTO> queryLogistics(String logisticsNo) {
        try {
            // 1. 调用第三方物流服务
            ThirdPartyLogisticsResponse response = logisticsClient.track(logisticsNo);
            // 2. 仅返回 Application 需要的字段（防腐简化）
            return ResultDO.buildSuccessResult(LogisticsConverter.toLogisticsInfoResponseDTO(response));
        } catch (Exception e) {
            log.error("查询物流信息失败, logisticsNo: {}", logisticsNo, e);
            return ResultDO.buildFailResult("LOGISTICS_QUERY_FAIL", "查询物流信息失败");
        }
    }
}
```

#### Application 调用 Output Adaptor 示例（读模式）

```java
/**
 * 订单查询应用服务实现
 * 通过 Adaptor 跨领域查询物流信息，组装订单详情
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
        // 1. 查询本领域订单数据
        ResultDO<OrderAggregate> queryResult = orderRepository.findById(requestDTO.getOrderId());
        if (!queryResult.isSuccess()) {
            return ResultDO.buildFailResult(queryResult.getMsg());
        }
        OrderAggregate order = queryResult.getData();
        if (order == null) {
            return ResultDO.buildFailResult("ORDER_NOT_FOUND", "订单不存在");
        }

        // 2. 通过 Adaptor 跨领域查询物流信息（参数为基础数据类型，复用性高）
        ResultDO<LogisticsInfoResponseDTO> logisticsResult = logisticsAdaptor.queryLogistics(order.getLogisticsNo());
        if (!logisticsResult.isSuccess()) {
            return ResultDO.buildFailResult(logisticsResult.getCode(), logisticsResult.getMsg());
        }

        // 3. 组装响应DTO
        GetOrderDetailResponseDTO responseDTO = OrderAssembler.toGetOrderDetailResponseDTO(order, logisticsResult.getData());

        return ResultDO.buildSuccessResult(responseDTO);
    }
}
```

---

## 四、纯计算模式 adaptor 规范

### 4.1 调用链

```plain
Input Adaptor → {动词}QueryAppService → DomainService → 计算并返回Result
```

### 4.2 Input Adaptor 规范

纯计算模式 Input Adaptor 调用 `{动词}QueryAppService`（继承 `ApplicationQueryService`），注意命名以**动词**开头，不以聚合根命名。

### 4.3 Output Adaptor 规范

纯计算模式的业务逻辑完全由 DomainService 承载，通常不需要 Output Adaptor。但当 Application 层需要**跨领域查询外部数据**
作为计算输入时，需通过 Output Adaptor 获取。Adaptor 参数与返回值遵循 1.4 节核心规则。

#### Output Adaptor 接口定义示例（纯计算模式）

```java
/**
 * 汇率适配器接口
 * 参数使用基础数据类型（币种确定且稳定），提高复用性
 * 返回值按 Application 需要的模型定义
 */
public interface ExchangeRateAdaptor {

    ResultDO<ExchangeRateResponseDTO> queryExchangeRate(String currency);
}
```

#### Output Adaptor 实现示例（纯计算模式）

```java
/**
 * 汇率适配器实现
 * 内部封装第三方汇率服务调用
 * 第三方接口变化时只需修改此实现类，不影响 Application 层
 */
@Component
public class ExchangeRateAdaptorImpl implements ExchangeRateAdaptor {

    @Autowired
    private ThirdPartyExchangeRateClient exchangeRateClient;

    @Override
    public ResultDO<ExchangeRateResponseDTO> queryExchangeRate(String currency) {
        try {
            // 1. 调用第三方汇率服务
            ThirdPartyRateResponse response = exchangeRateClient.getRate(currency);
            // 2. 仅返回 Application 需要的字段（防腐简化）
            return ResultDO.buildSuccessResult(ExchangeRateConverter.toExchangeRateResponseDTO(response));
        } catch (Exception e) {
            log.error("查询汇率失败, currency: {}", currency, e);
            return ResultDO.buildFailResult("EXCHANGE_RATE_QUERY_FAIL", "查询汇率失败");
        }
    }
}
```

#### Application 调用 Output Adaptor 示例（纯计算模式）

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
        // 1. 通过 Adaptor 跨领域查询汇率（参数为基础数据类型，复用性高）
        ResultDO<ExchangeRateResponseDTO> rateResult = exchangeRateAdaptor.queryExchangeRate(requestDTO.getCurrency());
        if (!rateResult.isSuccess()) {
            return ResultDO.buildFailResult(rateResult.getCode(), rateResult.getMsg());
        }

        // 2. 组装计算参数
        FeeCalculateParam param = FeePreCalculateAssembler.toParam(requestDTO, rateResult.getData());

        // 3. 调用 DomainService 进行计算
        ResultDO<FeeCalculateResult> domainResult = feeCalculateDomainService.calculate(param);
        if (!domainResult.isSuccess()) {
            return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
        }

        // 4. 转换为响应DTO
        return ResultDO.buildSuccessResult(FeePreCalculateAssembler.toResponseDTO(domainResult.getData()));
    }
}
```

---

## 五、规则+计算模式 adaptor 规范

### 5.1 调用链

```plain
Input Adaptor → {动词}QueryAppService → DomainService → Repository查询规则聚合根
                                                     → 聚合根.matchRule/calculate → 返回Result
```

### 5.2 Input Adaptor 规范

规则+计算模式 Input Adaptor 调用 `{动词}QueryAppService`（继承 `ApplicationQueryService`），注意命名以**动词**开头，不以聚合根命名。

### 5.3 Output Adaptor 规范

规则+计算模式的规则数据通过 Repository 从数据库或配置中心加载，通常不需要 Output Adaptor。但当 Application 层需要*
*跨领域查询外部数据**作为计算输入时，需通过 Output Adaptor 获取。Adaptor 参数与返回值遵循 1.4 节核心规则。

#### Output Adaptor 接口定义示例（规则+计算模式）

```java
/**
 * 用户等级适配器接口
 * 参数使用基础数据类型（用户ID确定且稳定），提高复用性
 * 返回值按 Application 需要的模型定义
 */
public interface UserLevelAdaptor {

    ResultDO<UserLevelResponseDTO> queryUserLevel(long userId);
}
```

#### Output Adaptor 实现示例（规则+计算模式）

```java
/**
 * 用户等级适配器实现
 * 内部封装第三方用户服务调用，可组合调用多个第三方
 * 第三方接口变化时只需修改此实现类，不影响 Application 层
 */
@Component
public class UserLevelAdaptorImpl implements UserLevelAdaptor {

    @Autowired
    private ThirdPartyUserClient userClient;

    @Override
    public ResultDO<UserLevelResponseDTO> queryUserLevel(long userId) {
        try {
            // 1. 调用第三方用户服务
            ThirdPartyUserResponse response = userClient.getUserInfo(userId);
            // 2. 仅返回 Application 需要的字段（防腐简化）
            return ResultDO.buildSuccessResult(UserLevelConverter.toUserLevelResponseDTO(response));
        } catch (Exception e) {
            log.error("查询用户等级失败, userId: {}", userId, e);
            return ResultDO.buildFailResult("USER_LEVEL_QUERY_FAIL", "查询用户等级失败");
        }
    }
}
```

#### Application 调用 Output Adaptor 示例（规则+计算模式）

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
        // 1. 通过 Adaptor 跨领域查询用户等级（参数为基础数据类型，复用性高）
        ResultDO<UserLevelResponseDTO> levelResult = userLevelAdaptor.queryUserLevel(requestDTO.getUserId());
        if (!levelResult.isSuccess()) {
            return ResultDO.buildFailResult(levelResult.getCode(), levelResult.getMsg());
        }

        // 2. 组装计算参数
        SubsidyCalculateParam param = SubsidyPreCalculateAssembler.toParam(requestDTO, levelResult.getData());

        // 3. 调用 DomainService 匹配规则并计算
        ResultDO<SubsidyCalculateResult> domainResult = subsidyCalculateDomainService.calculate(param);
        if (!domainResult.isSuccess()) {
            return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
        }

        // 4. 转换为响应DTO
        return ResultDO.buildSuccessResult(SubsidyPreCalculateAssembler.toResponseDTO(domainResult.getData()));
    }
}
```
