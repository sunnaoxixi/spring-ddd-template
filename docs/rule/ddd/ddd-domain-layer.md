---
alwaysApply: true
---

# domain 层开发规范

## 一、基础规范（所有模式通用）

### 1.1 包结构规范

domain 层的标准包结构如下，按业务领域划分顶层包，领域下的子包结构保持不变：

```plain
domain/
├── {业务名}/              # 按业务领域划分（如：order、booking、refund）
│   ├── model/
│   │   ├── aggregate/      # 聚合根
│   │   ├── entity/         # 实体
│   │   ├── value/          # 值对象
│   │   ├── param/          # 参数对象
│   │   └── result/         # 返回结果对象
│   ├── service/            # 领域服务
│   └── repository/         # 仓储接口
```

**包规范约束**：domain 层只能包含上述类型的代码（聚合根、实体、值对象、Param、Result、DomainService、Repository，以及配合懒加载使用的函数式接口 Provider，见 1.8 节），禁止出现 Utils、常量类、配置类等非领域概念的代码。

### 1.2 领域层隔离性规则

- domain 层仅包含纯业务代码，**禁止直接引用技术框架或第三方库（spring和静态工具包例外）**
- domain 层可依赖 `model` 模块（共享枚举、通用业务概念）
- Repository 接口定义在 domain 层，实现在 infrastructure 层
- Repository 管理**领域内**的数据库操作、缓存操作、发消息操作等，不依赖 Adaptor

### 1.3 领域层禁止使用设计模式

**禁止在领域层（domain）使用任何设计模式**，包括但不限于：策略模式（Strategy）、工厂模式（Factory）、模板方法模式（Template Method）、责任链模式（Chain of Responsibility）等。

#### 禁止原因

设计模式是**技术手段**而非**业务语义**，在领域层引入设计模式会导致：

- **业务逻辑碎片化**：核心计算逻辑分散在多个 Strategy 实现类或 Handler 中，阅读和维护时需要在多个类之间跳转，无法在一个方法内看清完整的业务规则
- **过度抽象**：为了适配设计模式的接口而强行拆分逻辑，破坏业务的内聚性
- **违反领域层定位**：领域层应该直接表达业务规则，而不是技术架构

#### 核心原则

**行业业务是可穷举的，不需要为扩展性引入设计模式。** 我们是行业业务系统，不是中台模式，不需要支持 N 种不同的行业接入。领域内的业务分支（如国内/国际、成人/儿童/婴儿、不同计价规则等）数量有限且可穷举，直接用 `if/else` 或 `switch` 在 DomainService、聚合根、实体、值对象中处理即可。

#### 正确做法

| 场景                | 正确做法                                    | 错误做法                            |
|-------------------|-----------------------------------------|---------------------------------|
| 不同类型的价格计算（如国内/国际） | 在 DomainService 或聚合根方法中用 `if/else` 直接处理 | 定义 `PriceStrategy` 接口 + 多个实现类   |
| 不同乘客类型的费用规则       | 在聚合根方法中用 `if/else` 按乘客类型分支              | 定义 `FeeCalculateStrategy` + 工厂类 |
| 不同状态下的业务校验        | 在聚合根方法中用状态枚举判断                          | 定义状态模式 + 多个 State 实现类           |

```java
// ✅ 正确：业务逻辑直接写在 DomainService/聚合根中，一目了然
public FeeCalculateResult calculateFee(FeeCalculateParam param) {
    if (DomesticIntlEnum.DOMESTIC.equals(param.getDomesticIntl())) {
        // 国内费用计算逻辑
        return calculateDomesticFee(param);
    } else {
        // 国际费用计算逻辑
        return calculateInternationalFee(param);
    }
}

// ❌ 错误：引入策略模式，业务逻辑分散在多个类中
public FeeCalculateResult calculateFee(FeeCalculateParam param) {
    FeeStrategy strategy = feeStrategyFactory.getStrategy(param.getDomesticIntl());
    return strategy.calculate(param);
}
```

#### 设计模式的正确归属

设计模式不属于领域层，但**允许在 Adaptor 层使用**。例如基于不同渠道ID路由接入不同的第三方接口，这属于**技术适配**职责，可以在 Output Adaptor 实现类中使用策略模式或路由模式。详见 `ddd-adaptor-layer.md` 1.6 节。

### 1.4 Param 对象规范

| 规则项 | 规范                            |
|-----|-------------------------------|
| 类名  | `{方法名}Param`                  |
| 继承  | 必须继承 `BaseParam` 基类           |
| 用途  | DomainService 方法入参、聚合根/实体方法入参 |

```java
/**
 * 确认支付参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfirmPaymentParam extends BaseParam {

    private Long orderId;
    private String paymentChannel;
    private BigDecimal amount;
}
```

### 1.5 Result 对象规范

| 规则项 | 规范                |
|-----|-------------------|
| 类名  | `{方法名}Result`     |
| 继承  | 必须继承 `BaseResult` |
| 特点  | 可以有充血方法           |

**使用场景**：

1. DomainService 方法返回值
2. Repository 查询返回值（Application 通过 Repository 查询本领域数据）

```java
/**
 * 费用计算结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FeeCalculateResult extends BaseResult {

    private BigDecimal totalFee;
    private String currency;

    /** 充血方法：判断是否免费 */
    public boolean isFree() {
        return totalFee == null || totalFee.compareTo(BigDecimal.ZERO) == 0;
    }
}
```

### 1.6 异常处理规范

| 异常类型                 | 使用位置          | 说明              |
|----------------------|---------------|-----------------|
| `AggregateException` | 聚合根、实体        | 聚合根/实体内部校验失败时抛出 |
| `BizException`       | DomainService | 业务逻辑校验失败时使用     |

### 1.7 Repository 接口定义规范

| 规则项  | 规范                                                     | 示例                                                                    |
|------|--------------------------------------------------------|-----------------------------------------------------------------------|
| 接口名称 | `{业务名}Repository`，与对应 DomainService 的业务名前缀保持一致，方便查找和对应 | DomainService 为 `OrderDomainService`，则 Repository 为 `OrderRepository` |
| 继承   | 必须继承 `AggregateRepository` 基类                          | —                                                                     |
| 定义位置 | domain 层                                               | —                                                                     |
| 实现位置 | infrastructure 层                                       | —                                                                     |
| 方法命名 | 必须为动词（如 `save`、`query`）                                | —                                                                     |

**核心职责**：

- Repository 管理**领域内**的数据库操作、缓存操作、发消息操作等技术实现
- Repository **不依赖 Adaptor**，不调用第三方外部服务（第三方服务调用由 Application 层通过 Adaptor 完成）

### 1.8 DomainService 按需查询外部数据的懒加载模式

**场景**：DomainService 在循环处理业务逻辑时需要按需查询外部数据（如循环计算每个商品按销售渠道币种的销售价，需要逐个查询汇率），但 DomainService 只能访问自己领域的 Repository，**禁止直接依赖 Adaptor**。

**解决方案**：在 Param 对象中定义一个**函数式接口属性**，由 Application 层在构造 Param 时注入 Adaptor 调用的实现。DomainService 通过调用该接口按需获取外部数据，无需感知 Adaptor 的存在。

**核心价值**：

- DomainService 保持纯领域逻辑，不依赖任何外部服务
- 外部数据的获取逻辑由 Application 层控制，符合分层架构原则
- 支持按需查询（懒加载），避免一次性加载所有数据

**适用场景**：仅适用于**循环中按需查询**的场景。简单场景（非循环查询）由 Application 层在调用 DomainService 前通过 Adaptor 获取数据后传入 Param 即可。

#### 步骤1：在 domain 层定义函数式接口

```java
package com.example.domain.model.param;

/**
 * 汇率查询接口
 * 定义在 domain 层，DomainService 通过此接口按需获取汇率
 * 不依赖任何外部服务实现
 */
@FunctionalInterface
public interface ExchangeRateProvider {

    /**
     * 根据币种获取汇率
     *
     * @param currency 币种编码
     * @return 汇率值
     */
    BigDecimal getExchangeRate(String currency);
}
```

#### 步骤2：在 Param 中引用该接口

```java
package com.example.domain.model.param;

/**
 * 销售价计算参数
 * 包含汇率查询接口，支持 DomainService 按需查询汇率
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CalculateSalePriceParam extends BaseParam {

    private List<ProductItem> productItems;

    /**
     * 汇率查询接口（懒加载）
     * 由 Application 层注入 Adaptor 调用的实现
     */
    private ExchangeRateProvider exchangeRateProvider;
}
```

#### 步骤3：DomainService 通过接口按需查询

```java
@Slf4j
@Service
public class SalePriceDomainServiceImpl implements SalePriceDomainService {

    @Override
    public ResultDO<CalculateSalePriceResult> calculateSalePrice(CalculateSalePriceParam param) {
        try {
            List<SalePriceItem> resultItems = new ArrayList<>();

            for (ProductItem item : param.getProductItems()) {
                // 通过 Param 中的接口按需查询汇率，DomainService 不依赖 Adaptor
                BigDecimal rate = param.getExchangeRateProvider().getExchangeRate(item.getCurrency());
                BigDecimal salePrice = item.getBasePrice().multiply(rate);

                resultItems.add(new SalePriceItem(item.getProductId(), salePrice, item.getCurrency()));
            }

            CalculateSalePriceResult result = new CalculateSalePriceResult();
            result.setSalePriceItems(resultItems);
            return ResultDO.buildSuccessResult(result);
        } catch (Exception e) {
            log.error("销售价计算异常, param: {}", param, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "销售价计算异常");
        }
    }
}
```

#### 步骤4：Application 层构造 Param 时注入 Adaptor 实现

```java
@Slf4j
@Service
public class SalePriceCalculateQueryAppServiceImpl implements SalePriceCalculateQueryAppService {

    @Autowired
    private ExchangeRateAdaptor exchangeRateAdaptor;

    @Autowired
    private SalePriceDomainService salePriceDomainService;

    @Override
    public ResultDO<CalculateSalePriceResponseDTO> calculateSalePrice(CalculateSalePriceRequestDTO requestDTO) {
        // 1. 构造 Param，注入 Adaptor 调用作为懒加载实现
        CalculateSalePriceParam param = new CalculateSalePriceParam();
        param.setProductItems(SalePriceAssembler.toProductItems(requestDTO));
        param.setExchangeRateProvider(currency -> {
            ResultDO<ExchangeRateResponseDTO> rateResult = exchangeRateAdaptor.queryExchangeRate(currency);
            if (!rateResult.isSuccess()) {
                throw new RuntimeException("查询汇率失败: " + rateResult.getMsg());
            }
            return rateResult.getData().getRate();
        });

        // 2. 调用 DomainService 计算（内部会按需调用汇率接口）
        ResultDO<CalculateSalePriceResult> domainResult = salePriceDomainService.calculateSalePrice(param);
        if (!domainResult.isSuccess()) {
            return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
        }

        // 3. 转换为响应DTO
        return ResultDO.buildSuccessResult(SalePriceAssembler.toResponseDTO(domainResult.getData()));
    }
}
```

---

## 二、写模式 domain 规范

### 2.1 DomainService 规范

#### 命名规范

| 规则项   | 规范                                   | 示例                             |
|-------|--------------------------------------|--------------------------------|
| 接口命名  | `{聚合根名}DomainService`                | `OrderDomainService`           |
| 实现类命名 | `{聚合根名}DomainServiceImpl`            | `OrderDomainServiceImpl`       |
| 方法命名  | 使用业务动词，禁止技术动词                        | `confirmPayment`、`cancelOrder` |
| 参数    | `{方法名}Param`，继承 `BaseParam`          | `ConfirmPaymentParam`          |
| 返回值   | `ResultDO<Void>` 或 `ResultDO<聚合根类型>` | `ResultDO<Void>`               |

#### 角色定位

- **核心职责**：封装领域业务逻辑，维护聚合根的完整性和一致性
- **禁止事项**：直接访问数据库或外部服务、包含非领域的业务逻辑

#### 标准流程

1. 获取分布式锁
2. 加载聚合根
3. 执行业务逻辑（通过聚合根方法）
4. 持久化变更（通过 Repository，内部可能包含数据库操作、缓存操作、发消息等）
5. 释放锁

#### 行为约束

- ✅ **允许调用**：本领域聚合根方法、仓储接口（Repository）
- ❌ **禁止调用**：应用层服务、基础设施层实现、外部服务适配器、其他领域服务、其他领域聚合根

#### 异常处理

- **必须捕获**所有异常
- **业务异常**：使用 `BizException` 并记录错误日志
- **系统异常**：捕获 `Throwable` 并记录错误日志
- **禁止**直接抛出未处理的异常
- 异常统一通过 `ResultDO` 返回，不向上层抛出

#### 代码模板

```java
@Slf4j
@Service
public class OrderDomainServiceImpl implements OrderDomainService {

    @Resource
    private OrderRepository orderRepository;

    @Override
    public ResultDO<Void> confirmPayment(ConfirmPaymentParam param) {
        // 1. 获取锁
        LevelLock levelLock = orderRepository.buildLock("order:confirmPayment:" + param.getOrderId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult("LOCK_FAIL", "获取锁失败，请稍后重试");
        }
        try {

            // 2. 加载聚合根
            OrderQuery query = new OrderQuery();
            query.setId(param.getOrderId());
            ResultDO<OrderAggregate> queryResult = orderRepository.query(query);
            if (!queryResult.isSuccess()) {
                return ResultDO.buildFailResult(queryResult.getMsg());
            }
            OrderAggregate orderAggregate = queryResult.getData();
            if (orderAggregate == null) {
                return ResultDO.buildFailResult("ORDER_NOT_FOUND", "订单不存在");
            }

            // 3. 执行业务逻辑（通过聚合根方法）
            orderAggregate.confirmPayment(param);

            // 4. 持久化变更
            orderRepository.save(orderAggregate);

        } catch (BizException e) {
            log.error("确认支付业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMsg());
        } catch (Throwable e) {
            log.error("确认支付系统异常, param: {}", param, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        } finally {
            levelLock.unlock();
        }
        return ResultDO.buildSuccessResult(null);
    }
}
```

### 2.2 Aggregate 聚合根规范

#### 核心定义

聚合根是领域模型的核心对象，负责维护一组相关实体和值对象的业务一致性和完整性边界。外部只能通过聚合根提供的方法来访问内部对象。

#### 设计原则

1. **单一职责**：一个聚合根只负责一个核心业务概念
2. **强一致性**：聚合内的所有修改必须保持事务一致性
3. **小聚合**：尽量设计小的聚合，避免过大复杂度
4. **通过标识引用**：聚合之间通过 ID 引用而非对象引用
5. **封装性**：只能通过业务方法访问聚合根，不可通过 getter 遍历内部结构和对象

#### 命名规范

| 规则项  | 规范                                 | 示例                             |
|------|------------------------------------|--------------------------------|
| 类名   | `{名词}Aggregate`，继承 `BaseAggregate` | `OrderAggregate`               |
| 方法命名 | 必须是动词                              | `confirmPayment`、`cancelOrder` |
| 参数   | `{方法名}Param` 继承 `BaseParam`，或基础类型  | `ConfirmPaymentParam`          |
| 返回值  | 基础数据类型                             | `void`、`boolean`、`Long`        |

#### 四种方法类型

**1. 写操作类方法**（修改状态）：参数校验 + 业务规则校验 + 修改状态

```java
public void cancelOrder(CancelOrderParam param) {
    // 参数校验
    if (param == null) {
        throw new AggregateException("参数不能为空");
    }
    // 业务规则校验
    if (!this.status.canCancel()) {
        throw new AggregateException("当前状态不可取消");
    }
    // 修改状态
    this.status = OrderStatus.CANCELLED;
}
```

**2. 计算类方法**（不修改状态）：

```java
public BigDecimal calculateTotalAmount() {
    return items.stream()
        .map(item -> item.getPrice().multiply(item.getQuantity()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

**3. 查找类方法**（不修改状态）：

```java
@JSONField(serialize = false)
public OrderItemEntity findItemById(Long itemId) {
    return this.items.stream()
        .filter(item -> item.getId().equals(itemId))
        .findFirst()
        .orElse(null);
}
```

**4. 判断类方法**（不修改状态）：

```java
@JSONField(serialize = false)
public boolean isPaymentConfirmed() {
    return OrderStatus.PAID.equals(this.status);
}
```

**序列化注意**：如果聚合根需要被序列化，计算类、查找类、判断类方法上需要加 `@JSONField(serialize = false)` 注解。

#### 方法膨胀控制策略

**1. 状态相关写操作的稳定性**

- **特点**：状态机驱动的写操作（如订单状态流转）通常稳定，方法数量不易膨胀
- **控制原则**：新增方法前需验证业务合理性，避免技术视角的冗余方法。状态推进是核心逻辑，方法应严格对应业务状态变化

**2. 非状态相关写操作的抽象**

- **问题**：补充数据类操作（如更新银行卡号、费用信息）可能因字段差异导致方法膨胀
- **解决方案**：通过**业务语义抽象**合并同类操作为业务概念（如 `回填费用` 代替多个字段的 `update` 方法），避免纯技术意义的通用 `update`，方法名需体现业务意图（如 `更新支付信息`）

**3. 读操作与判断方法的拆分**

- **问题**：查询类方法（如 `findXxx`、`isValid`）易膨胀，增加聚合根复杂度
- **解决方案**：
  - **代码分层**：拆分为基类（写操作）和子类（读操作），提升可读性
  - **延迟暴露**：通过聚合根提供查找方法，让调用方直接访问实体/值对象的方法（减少聚合根方法数量）

#### 异常处理

- 抛出 `AggregateException`

### 2.3 Entity 实体规范

Entity 遵循写模式聚合根开发规范，核心差异如下：

| 规则项  | 规范                                | 示例                |
|------|-----------------------------------|-------------------|
| 类名   | `{名词}Entity`，继承 `BaseEntity`      | `OrderItemEntity` |
| 方法命名 | 必须是动词                             | `updatePrice`     |
| 参数   | `{方法名}Param` 继承 `BaseParam`，或基础类型 | —                 |
| 返回值  | 基础数据类型                            | —                 |
| 属性类型 | 使用普通类型（如 `Long`、`String`、领域枚举）    | —                 |
| 异常处理 | 抛出 `AggregateException`           | —                 |

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderItemEntity extends BaseEntity<Long> {

    private Long orderId;
    private String productName;
    private Long price;
    private DomesticIntlEnum domesticIntl;

    public void updatePrice(Long newPrice) {
        if (newPrice == null || newPrice <= 0) {
            throw new AggregateException("价格必须大于0");
        }
        this.price = newPrice;
    }
}
```

### 2.4 Value 值对象规范

#### 核心特征

| 特性   | Value Object | Entity  |
|------|--------------|---------|
| 标识   | 无            | 有唯一 ID  |
| 相等性  | 属性值相等        | ID 相等   |
| 生命周期 | 可自由创建销毁      | 有明确生命周期 |
| 可变性  | **不可变**      | 可变（写模式） |

#### 命名规范

| 规则项  | 规范                                | 示例                       |
|------|-----------------------------------|--------------------------|
| 类名   | `{名词}Value`，继承 `BaseValue`        | `PassengerValue`         |
| 方法命名 | 必须是动词，且为**计算类、判断类**操作             | `passengerAge`、`isAdult` |
| 参数   | `{方法名}Param` 继承 `BaseParam`，或基础类型 | —                        |
| 返回值  | 基础数据类型                            | —                        |

#### 在架构中的位置

- 属于 domain 层核心模型
- 可出现在：Aggregate 内部（作为属性）、DomainService 方法参数/返回值、Repository 查询结果（聚合根的一部分）

```java
public class PassengerValue extends BaseValue {

    private Integer age;
    private Date birthday;

    /** 计算乘机人年龄，不合法用0兜底 */
    public Integer passengerAge() {
        if (this.age != null) {
            return this.age;
        }
        if (this.birthday == null) {
            return 0;
        }
        int calculatedAge = DateUtil.ageOfNow(this.birthday);
        return (calculatedAge < 0 || calculatedAge > 120) ? 0 : calculatedAge;
    }
}
```

### 2.5 Repository 写模式规范

| 规则项  | 规范                                               |
|------|--------------------------------------------------|
| 方法命名 | 使用动词，如 `save`、`query`                            |
| 参数   | 聚合根对象（如 `OrderAggregate`）或查询对象                   |
| 返回值  | `ResultDO<Void>`（save）或 `ResultDO<聚合根类型>`（query） |

```java
public interface OrderRepository extends AggregateRepository<OrderAggregate, Long> {

    /** 保存聚合根（新增或更新） */
    ResultDO<Void> save(OrderAggregate aggregate);

    /** 构建分布式锁 */
    LevelLock buildLock(String lockKey);

    /** 根据查询条件加载聚合根 */
    ResultDO<OrderAggregate> query(OrderQuery query);
}
```

---

## 三、读模式 domain 规范

读模式是**纯查询服务**，不包含业务逻辑，仅负责数据查询与格式转换。聚合根在读模式中作为**数据载体**存在，不包含业务方法。

**特点**：

- 无 DomainService（读模式不经过领域服务）
- 聚合根仅作为数据载体，无业务逻辑
- 不修改任何领域模型状态

### 3.1 Repository 读模式规范

| 规则项  | 规范                                      | 示例                                                          |
|------|-----------------------------------------|-------------------------------------------------------------|
| 方法命名 | 使用动词明确查询意图，避免抽象词汇                       | `queryOrderList`、`getOrderDetail`                           |
| 参数   | `{方法名}Query` 或基础数据类型                    | `QueryOrderListQuery`、`Long`                                |
| 返回值  | `ResultDO<聚合根>` 或 `ResultDO<List<聚合根>>` | `ResultDO<OrderAggregate>`、`ResultDO<List<OrderAggregate>>` |

```java
public interface OrderRepository extends AggregateRepository<OrderAggregate, Long> {

    /** 查询订单详情 */
    ResultDO<OrderAggregate> getOrderDetail(Long orderId);

    /** 查询订单列表 */
    ResultDO<List<OrderAggregate>> queryOrderList(QueryOrderListQuery query);
}
```

---

## 四、纯计算模式 domain 规范

### 4.1 DomainService 规范

#### 定义与核心特征

- **无聚合根和实体**：业务逻辑完全由 DomainService 承载
- **无状态性**：不修改任何内部状态，仅依赖输入参数进行逻辑处理（类似函数式编程）
- **业务逻辑限制**：仅允许在 DomainService 中实现，禁止泄漏到 Application 或 Infrastructure 层
- **无状态设计**：避免在 DomainService 中引入成员变量，确保方法幂等性

**典型场景**：搜索控制（黑白名单、QPS 限流）、视图处理（多语言支持）、费用计算

#### 命名规范

| 规则项   | 规范                      | 示例                               |
|-------|-------------------------|----------------------------------|
| 接口命名  | `{动词}DomainService`     | `SearchControlDomainService`     |
| 实现类命名 | `{动词}DomainServiceImpl` | `SearchControlDomainServiceImpl` |
| 方法命名  | 动词                      | `searchControl`                  |
| 参数    | `{方法名}Param`            | `SearchControlParam`             |
| 返回值   | `ResultDO<{方法名}Result>` | `ResultDO<SearchControlResult>`  |

#### 代码模板

```java
@Slf4j
@Service
public class SearchControlDomainServiceImpl implements SearchControlDomainService {

    @Override
    public ResultDO<SearchControlResult> searchControl(SearchControlParam param) {
        SearchControlResult result = new SearchControlResult();
        SearchControlRuleValue ruleValue = param.getSearchControlRuleValue();

        result.setMaxSearchWaitMilliSeconds(ruleValue.getMaxSearchWaitMilliSeconds());
        result.setWhiteAgentIds(ruleValue.getWhiteAgentIds());

        // 仅缓存模式开启，则只能走缓存
        if (ruleValue.isQueryCacheOnly()) {
            result.setRealSearch(false);
            return ResultDO.buildSuccessResult(result);
        }

        // 当前OD不支持实时搜索，则只能走缓存
        if (!ruleValue.isRealTimeSearchOD(param.getAirLegSet())) {
            result.setRealSearch(false);
            return ResultDO.buildSuccessResult(result);
        }

        result.setRealSearch(true);
        return ResultDO.buildSuccessResult(result);
    }
}
```

### 4.2 Result 规范

同基础规范 1.5 节，命名为 `{动作}Result`，继承 `BaseResult`，可以有充血方法。

---

## 五、规则+计算模式 domain 规范

### 5.1 DomainService 规范

#### 定义与核心特征

- **有聚合根和实体**：规则本身建模为聚合根
- **业务逻辑由聚合根和实体承载**：DomainService 负责编排，不包含计算逻辑
- **无状态性**：实体方法仅基于输入参数计算并返回结果，**不修改聚合根或实体的内部状态**

#### 命名规范

| 规则项   | 规范                      | 示例                                |
|-------|-------------------------|-----------------------------------|
| 接口命名  | `{动词}DomainService`     | `CalculateBonusDomainService`     |
| 实现类命名 | `{动词}DomainServiceImpl` | `CalculateBonusDomainServiceImpl` |
| 方法命名  | 动词                      | `calculateBonus`                  |
| 参数    | `{方法名}Param`            | `CalculateBonusParam`             |
| 返回值   | `ResultDO<{方法名}Result>` | `ResultDO<CalculateBonusResult>`  |

#### 方法实现流程

1. 通过参数查询规则得到规则聚合根集合
2. 通过参数匹配到规则聚合根
3. 调用规则聚合根的计算方法

#### 代码模板

```java
@Slf4j
@Service
public class CalculateBonusDomainServiceImpl implements CalculateBonusDomainService {

    @Resource
    private BonusRuleRepository bonusRuleRepository;

    @Override
    public ResultDO<List<ItemCalculateResult>> calculateBonus(CalculateBonusParam param) {
        try {
            // 1. 查询规则聚合根集合
            ResultDO<List<BonusRuleAggregate>> ruleResult = bonusRuleRepository.queryAllRule();
            if (!ruleResult.isSuccess()) {
                return ResultDO.buildFailResult(ruleResult.getMsg());
            }
            List<BonusRuleAggregate> ruleAggregates = ruleResult.getData();
            if (CollectionUtils.isEmpty(ruleAggregates)) {
                return ResultDO.buildFailResult("RULE_IS_EMPTY", "补贴规则为空");
            }

            // 2. 按优先级排序
            ruleAggregates.sort(Comparator.comparingInt(BonusRuleAggregate::getRulePriority).reversed());

            // 3. 匹配规则并计算
            List<ItemCalculateResult> results = new ArrayList<>();
            List<ItemParam> remainItems = new ArrayList<>(param.getItems());

            for (BonusRuleAggregate ruleAggregate : ruleAggregates) {
                if (CollectionUtils.isEmpty(remainItems)) {
                    break;
                }
                // 匹配规则
                BonusRuleMatchResult matchResult = ruleAggregate.matchRule(remainItems, param);
                // 计算匹配到的商品补贴金额
                if (!matchResult.getMatchedItems().isEmpty()) {
                    results.addAll(ruleAggregate.calculateBonus(matchResult.getMatchedItems(), param));
                }
                remainItems = matchResult.getWaitMatchedItems();
            }

            return ResultDO.buildSuccessResult(results);
        } catch (AggregateException e) {
            log.error("补贴计算聚合根异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMsg());
        } catch (Throwable e) {
            log.error("补贴计算系统异常, param: {}", param, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }
}
```

### 5.2 规则 Aggregate 规范

#### 聚合根职责

- 将业务规则封装为聚合根（如 `BonusRuleAggregate`）
- 提供**无状态计算方法**（如 `matchRule`、`calculateBonus`），仅基于输入参数计算并返回结果
- **不修改聚合根或实体的内部状态**（方法无副作用）

#### 业务逻辑封装

- 逻辑必须内聚在聚合根/实体中，**禁止将规则匹配或计算逻辑泄露到 Service 层**
- 每个方法聚焦单一功能（如匹配规则、计算金额），方法名需明确表达业务意图

#### 命名规范

| 规则项 | 规范                          |
|-----|-----------------------------|
| 参数  | `{方法名}Param`，继承 `BaseParam` |
| 返回值 | `Result` 类型，`{方法名}Result`   |

#### 代码模板

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class BonusRuleAggregate extends BaseAggregate<Long> {

    private String ruleName;
    private Integer rulePriority;
    private BonusRuleEntity bonusRuleEntity;

    /** 匹配规则（无副作用，不修改状态） */
    public BonusRuleMatchResult matchRule(List<ItemParam> items, CalculateBonusParam param) {
        return this.bonusRuleEntity.matchItems(items, param);
    }

    /** 计算补贴金额（无副作用，不修改状态） */
    public List<ItemCalculateResult> calculateBonus(List<ItemParam> matchedItems, CalculateBonusParam param) {
        return matchedItems.stream()
            .map(item -> this.bonusRuleEntity.calculateItemBonus(item, param))
            .collect(Collectors.toList());
    }
}
```

#### 规则 Entity 模板

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class BonusRuleEntity extends BaseEntity<Long> {

    private String bizType;
    private String condition;
    private Long baseValue;

    /** 匹配商品（无副作用） */
    public BonusRuleMatchResult matchItems(List<ItemParam> items, CalculateBonusParam param) {
        List<ItemParam> matched = new ArrayList<>();
        List<ItemParam> waitMatched = new ArrayList<>();
        for (ItemParam item : items) {
            if (matchCondition(item, param)) {
                matched.add(item);
            } else {
                waitMatched.add(item);
            }
        }
        return new BonusRuleMatchResult(matched, waitMatched);
    }

    /** 计算单个商品补贴（无副作用） */
    public ItemCalculateResult calculateItemBonus(ItemParam item, CalculateBonusParam param) {
        Long bonusAmount = this.baseValue + item.getBasePrice();
        return ItemCalculateResult.of().setItemParam(item).setBonusAmount(bonusAmount);
    }

    private boolean matchCondition(ItemParam item, CalculateBonusParam param) {
        return Objects.equals(this.bizType, param.getDomesticIntl());
    }
}
```
