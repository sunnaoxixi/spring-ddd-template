---
alwaysApply: true
---

# DDD Infrastructure 层开发规范

## 一、基础规范（所有模式通用）

### 1.1 包结构规范

infrastructure 层的标准包结构如下，按业务领域划分顶层包，领域下的子包结构保持不变：

```plain
infrastructure/
├── {业务名}/              # 按业务领域划分（如：order、booking、refund）
│   ├── repository/        # 仓储实现
│   │   └── {业务名}RepositoryImpl    # 仓储实现类，与对应 DomainService 的业务名前缀保持一致
│   ├── mysql/             # 数据库访问
│   │   ├── po/            # 持久化对象（PO）
│   │   └── mapper/        # MyBatis Mapper 接口
│   └── converter/         # 数据转换器（聚合根 ↔ PO 互转）
```

### 1.2 核心定位

**Infrastructure 层是技术实现层，负责将领域层定义的 Repository 接口落地为具体的技术实现。**

- Infrastructure 层实现 domain 层定义的 Repository 接口，对 domain 层屏蔽所有技术细节（数据库、缓存、消息等）
- Infrastructure 层仅做**纯技术转换**（聚合根 ↔ PO），**禁止包含任何业务逻辑**
- 业务逻辑属于 domain 层（聚合根方法、DomainService），Infrastructure 层只负责"存"和"取"

#### 与其他层的关系

| 关系                  | 说明                                                  |
|---------------------|-----------------------------------------------------|
| **与 domain 层**      | 实现 domain 层定义的 Repository 接口，依赖 domain 模块           |
| **与 application 层** | 无直接依赖关系，Application 层通过 domain 层的 Repository 接口间接调用 |
| **与 client 层**      | 无依赖关系                                               |
| **与 model 层**       | 可依赖 model 模块（共享枚举、通用业务概念）                           |

### 1.3 Repository 实现类命名规范

| 规则项    | 规范                            | 示例                                                                            |
|--------|-------------------------------|-------------------------------------------------------------------------------|
| 实现类命名  | `{业务名}RepositoryImpl`         | `OrderRepositoryImpl`                                                         |
| 命名对应关系 | 与对应 DomainService 的业务名前缀保持一致  | DomainService 为 `OrderDomainService` → RepositoryImpl 为 `OrderRepositoryImpl` |
| 注解     | 使用 `@Component`               | —                                                                             |
| 实现接口   | domain 层定义的 `{业务名}Repository` | `implements OrderRepository`                                                  |

### 1.4 允许与禁止规范

Infrastructure 层可以**直接使用中间件技术**，但**禁止访问第三方外部服务**。

| 维度       | ✅ 允许                                                                                | ❌ 禁止                                        |
|----------|-------------------------------------------------------------------------------------|---------------------------------------------|
| **模块依赖** | `domain`、`model`                                                                    | `application`、`client`、其他业务系统的二方包           |
| **技术使用** | MyBatis、Redis、Tair、RocketMQ、MetaQ、分布式锁、Diamond、Nacos 等中间件                           | HSF、HTTP Client 等第三方外部服务调用                  |
| **代码职责** | 实现 Repository 接口、聚合根与 PO 的纯技术转换（Converter）、数据库操作（Mapper）、缓存读写、发送领域事件消息、构建分布式锁、写操作日志 | 包含业务逻辑、在 Converter 中编写业务判断、暴露技术细节给 domain 层 |
| **调用方式** | 通过 domain 层定义的 Repository 接口被调用                                                     | 直接被 Application 层以实现类方式调用                   |

### 1.5 异常处理规范

- Repository 实现类中的异常处理应与 domain 层保持一致
- 数据库操作异常应被捕获并转换为 `ResultDO` 返回，**禁止**直接向上层抛出技术异常（如 `SQLException`）
- 日志记录应包含关键业务参数，便于排查问题

```java
// ✅ 正确：捕获异常，返回 ResultDO
@Override
public ResultDO<OrderAggregate> query(OrderQuery query) {
    try {
        OrderPO po = orderMapper.selectById(query.getId());
        return ResultDO.buildSuccessResult(OrderConverter.toAggregate(po));
    } catch (Exception e) {
        log.error("查询订单失败, query: {}", query, e);
        return ResultDO.buildFailResult("DB_QUERY_ERROR", "查询订单数据异常");
    }
}

// ❌ 错误：直接抛出技术异常
@Override
public ResultDO<OrderAggregate> query(OrderQuery query) {
    OrderPO po = orderMapper.selectById(query.getId()); // 异常直接抛出，上层无法优雅处理
    return ResultDO.buildSuccessResult(OrderConverter.toAggregate(po));
}
```

### 1.6 PO（Persistent Object）规范

PO 是与数据库表一一对应的持久化对象，仅用于 Infrastructure 层内部，**禁止**暴露给 domain 层或 application 层。

#### 命名规范

| 规则项  | 规范                               | 示例                      |
|------|----------------------------------|-------------------------|
| 类名   | `{表对应业务名}PO`                     | `OrderPO`、`OrderItemPO` |
| 存放位置 | `infrastructure/{业务名}/mysql/po/` | —                       |
| 字段命名 | 与数据库表字段对应，使用驼峰命名                 | `orderId`、`orderStatus` |

#### 设计原则

- PO 是**纯数据载体**，只包含字段和 getter/setter，**禁止**包含业务方法
- PO 与数据库表结构一一对应，字段类型应与数据库列类型匹配
- PO **禁止**被 domain 层、application 层、client 层引用，仅在 Infrastructure 层内部流转

#### 代码模板

```java
/**
 * 订单持久化对象
 * 与 t_order 表一一对应，仅用于 Infrastructure 层内部
 */
@Data
public class OrderPO {

    /** 主键ID */
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 买家ID */
    private Long buyerId;

    /** 商品ID */
    private String productId;

    /** 订单状态（数据库存储值，如 0-待支付、1-已支付、2-已取消） */
    private Integer orderStatus;

    /** 订单金额（分） */
    private Long amount;

    /** 物流编号 */
    private String logisticsNo;

    /** 创建时间 */
    private Date gmtCreate;

    /** 修改时间 */
    private Date gmtModified;
}
```

### 1.7 Mapper 接口规范

Mapper 是 MyBatis 数据库访问接口，负责执行 SQL 操作。

#### 命名规范

| 规则项  | 规范                                   | 示例                                                     |
|------|--------------------------------------|--------------------------------------------------------|
| 接口名  | `{表对应业务名}Mapper`                     | `OrderMapper`、`OrderItemMapper`                        |
| 存放位置 | `infrastructure/{业务名}/mysql/mapper/` | —                                                      |
| 方法命名 | 使用动词，体现操作意图                          | `insert`、`updateById`、`selectById`、`selectByCondition` |

#### 设计原则

- Mapper 仅被同业务域的 RepositoryImpl 调用，**禁止**被 domain 层、application 层直接调用
- Mapper 方法的参数和返回值必须使用 PO 对象或基础数据类型，**禁止**使用聚合根或 DTO

#### 代码模板

```java
/**
 * 订单 Mapper 接口
 * 仅被 OrderRepositoryImpl 调用
 */
public interface OrderMapper {

    /** 新增订单 */
    int insert(OrderPO po);

    /** 根据ID更新订单 */
    int updateById(OrderPO po);

    /** 根据ID查询订单 */
    OrderPO selectById(Long id);

    /** 根据订单编号查询订单 */
    OrderPO selectByOrderNo(String orderNo);

    /** 根据条件查询订单列表 */
    List<OrderPO> selectByCondition(OrderQueryCondition condition);
}
```

### 1.8 Converter 转换类规范

Converter 负责聚合根与 PO 之间的**纯技术转换**，是 Infrastructure 层的核心组件之一。

#### 命名规范

| 规则项  | 规范                                       | 示例               |
|------|------------------------------------------|------------------|
| 类名   | `{业务名}Converter`                         | `OrderConverter` |
| 存放位置 | `infrastructure/{业务名}/converter/`        | —                |
| 方法命名 | `toAggregate`（PO → 聚合根）、`toPO`（聚合根 → PO） | —                |
| 方法类型 | 静态方法                                     | —                |

#### 设计原则

- Converter 仅做**字段映射**，**禁止**包含业务判断逻辑
- 需要处理聚合根内部的 Entity、Value Object 与 PO 之间的转换
- 枚举转换：PO 中存储数据库值（如 Integer），聚合根中使用领域枚举（如 `OrderStatusEnum`），Converter 负责互转
- 空值处理：入参为 null 时直接返回 null，避免 NPE

#### 与 Assembler 的区别

| 维度       | Converter（Infrastructure） | Assembler（Application）                |
|----------|---------------------------|---------------------------------------|
| **转换对象** | 聚合根 ↔ PO                  | RequestDTO/ResponseDTO ↔ 领域 Param/聚合根 |
| **所在层**  | Infrastructure 层          | Application 层                         |
| **职责**   | 技术层面的持久化转换                | 业务层面的 DTO 与领域对象转换                     |

#### 完整代码示例

```java
/**
 * 订单数据转换器
 * 职责：聚合根与 PO 之间的纯技术转换，无业务逻辑
 */
public class OrderConverter {

    /**
     * PO 转换为聚合根
     * 包含内部 Entity、Value Object 的转换
     */
    public static OrderAggregate toAggregate(OrderPO po) {
        if (po == null) {
            return null;
        }
        OrderAggregate aggregate = new OrderAggregate();
        aggregate.setId(po.getId());
        aggregate.setOrderNo(po.getOrderNo());
        aggregate.setBuyerId(po.getBuyerId());
        aggregate.setAmount(po.getAmount());
        aggregate.setLogisticsNo(po.getLogisticsNo());
        // 枚举转换：数据库 Integer → 领域枚举
        aggregate.setStatus(OrderStatusEnum.getByCode(po.getOrderStatus()));
        return aggregate;
    }

    /**
     * 聚合根转换为 PO
     */
    public static OrderPO toPO(OrderAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }
        OrderPO po = new OrderPO();
        po.setId(aggregate.getId());
        po.setOrderNo(aggregate.getOrderNo());
        po.setBuyerId(aggregate.getBuyerId());
        po.setAmount(aggregate.getAmount());
        po.setLogisticsNo(aggregate.getLogisticsNo());
        // 枚举转换：领域枚举 → 数据库 Integer
        if (aggregate.getStatus() != null) {
            po.setOrderStatus(aggregate.getStatus().getCode());
        }
        return po;
    }

    /**
     * PO 列表转换为聚合根列表
     */
    public static List<OrderAggregate> toAggregateList(List<OrderPO> poList) {
        if (CollectionUtils.isEmpty(poList)) {
            return Collections.emptyList();
        }
        return poList.stream()
            .map(OrderConverter::toAggregate)
            .collect(Collectors.toList());
    }
}
```

---

## 二、写模式 infrastructure 规范

### 2.1 核心职责

写模式下 RepositoryImpl 的核心职责：

- **save 方法**：处理聚合根的新增和更新，需判断是 insert 还是 update
- **query 方法**：为 DomainService 加载聚合根提供数据支持
- **buildLock 方法**：构建分布式锁，保证并发安全
- **可选操作**：发送领域事件消息、写操作日志

### 2.2 返回值规范

与 domain 层 Repository 接口定义保持一致：

| 方法类型      | 返回值               | 说明                |
|-----------|-------------------|-------------------|
| save      | `ResultDO<Void>`  | 持久化成功返回成功，失败返回错误码 |
| query     | `ResultDO<聚合根类型>` | 查询成功返回聚合根，失败返回错误码 |
| buildLock | `LevelLock`       | 返回分布式锁对象          |

### 2.3 完整代码示例

#### Repository 接口（定义在 domain 层，此处列出以便对照）

```java
/**
 * 订单仓储接口（定义在 domain 层）
 */
public interface OrderRepository extends AggregateRepository<OrderAggregate, Long> {

    /** 保存聚合根（新增或更新） */
    ResultDO<Void> save(OrderAggregate aggregate);

    /** 构建分布式锁 */
    LevelLock buildLock(String lockKey);

    /** 根据查询条件加载聚合根 */
    ResultDO<OrderAggregate> query(OrderQuery query);
}
```

#### RepositoryImpl 实现（写模式）

```java
/**
 * 订单仓储实现类（写模式）
 * 职责：聚合根的持久化和查询，纯技术转换，无业务逻辑
 * 命名与 OrderDomainService 的业务名前缀保持一致
 */
@Slf4j
@Component
public class OrderRepositoryImpl implements OrderRepository {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public ResultDO<Void> save(OrderAggregate aggregate) {
        try {
            // 1. 聚合根转换为 PO
            OrderPO orderPO = OrderConverter.toPO(aggregate);

            // 2. 判断新增还是更新
            if (aggregate.getId() == null) {
                orderMapper.insert(orderPO);
                // 回填ID到聚合根
                aggregate.setId(orderPO.getId());
            } else {
                int affectedRows = orderMapper.updateById(orderPO);
                if (affectedRows == 0) {
                    return ResultDO.buildFailResult("UPDATE_FAIL", "更新订单失败，数据可能已被修改");
                }
            }

            // 3. 保存子实体（如订单项）
            if (CollectionUtils.isNotEmpty(aggregate.getItems())) {
                for (OrderItemEntity item : aggregate.getItems()) {
                    OrderItemPO itemPO = OrderItemConverter.toPO(item, aggregate.getId());
                    if (item.getId() == null) {
                        orderItemMapper.insert(itemPO);
                        item.setId(itemPO.getId());
                    } else {
                        orderItemMapper.updateById(itemPO);
                    }
                }
            }

            // 4. 发送领域事件（可选）
            // domainEventPublisher.publish(aggregate.getDomainEvents());

            // 5. 写操作日志（可选）
            // operationLogService.log(aggregate);

            return ResultDO.buildSuccessResult(null);
        } catch (Exception e) {
            log.error("保存订单失败, aggregateId: {}", aggregate.getId(), e);
            return ResultDO.buildFailResult("DB_SAVE_ERROR", "保存订单数据异常");
        }
    }

    @Override
    public LevelLock buildLock(String lockKey) {
        return new LevelLock(lockKey);
    }

    @Override
    public ResultDO<OrderAggregate> query(OrderQuery query) {
        try {
            // 1. 查询主表 PO
            OrderPO orderPO = orderMapper.selectById(query.getId());
            if (orderPO == null) {
                return ResultDO.buildSuccessResult(null);
            }

            // 2. 查询子表 PO（如订单项）
            List<OrderItemPO> itemPOList = orderItemMapper.selectByOrderId(orderPO.getId());

            // 3. PO 转换为聚合根（包含内部 Entity）
            OrderAggregate aggregate = OrderConverter.toAggregate(orderPO);
            aggregate.setItems(OrderItemConverter.toEntityList(itemPOList));

            return ResultDO.buildSuccessResult(aggregate);
        } catch (Exception e) {
            log.error("查询订单失败, query: {}", query, e);
            return ResultDO.buildFailResult("DB_QUERY_ERROR", "查询订单数据异常");
        }
    }
}
```

---

## 三、读模式 infrastructure 规范

### 3.1 核心职责

读模式下 RepositoryImpl 的核心职责：

- **仅做数据查询和格式转换**，无业务逻辑
- 查询方法返回聚合根（作为数据载体）或 Result 对象
- 支持单条查询和列表查询

### 3.2 返回值规范

| 方法类型 | 返回值                     | 说明                                                   |
|------|-------------------------|------------------------------------------------------|
| 单条查询 | `ResultDO<聚合根类型>`       | 查询成功返回聚合根，未找到时返回 `ResultDO.buildSuccessResult(null)` |
| 列表查询 | `ResultDO<List<聚合根类型>>` | 查询成功返回聚合根列表，无数据时返回空列表                                |

### 3.3 完整代码示例

#### Repository 接口（定义在 domain 层）

```java
/**
 * 订单仓储接口 - 读模式（定义在 domain 层）
 */
public interface OrderRepository extends AggregateRepository<OrderAggregate, Long> {

    /** 查询订单详情 */
    ResultDO<OrderAggregate> getOrderDetail(Long orderId);

    /** 查询订单列表 */
    ResultDO<List<OrderAggregate>> queryOrderList(QueryOrderListQuery query);
}
```

#### RepositoryImpl 实现（读模式）

```java
/**
 * 订单仓储实现类（读模式）
 * 职责：纯数据查询和格式转换，无业务逻辑
 */
@Slf4j
@Component
public class OrderRepositoryImpl implements OrderRepository {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public ResultDO<OrderAggregate> getOrderDetail(Long orderId) {
        try {
            // 1. 查询 PO
            OrderPO po = orderMapper.selectById(orderId);
            if (po == null) {
                return ResultDO.buildSuccessResult(null);
            }

            // 2. PO 转换为聚合根
            OrderAggregate aggregate = OrderConverter.toAggregate(po);

            return ResultDO.buildSuccessResult(aggregate);
        } catch (Exception e) {
            log.error("查询订单详情失败, orderId: {}", orderId, e);
            return ResultDO.buildFailResult("DB_QUERY_ERROR", "查询订单详情异常");
        }
    }

    @Override
    public ResultDO<List<OrderAggregate>> queryOrderList(QueryOrderListQuery query) {
        try {
            // 1. 查询 PO 列表
            List<OrderPO> poList = orderMapper.selectByCondition(query);

            // 2. PO 列表转换为聚合根列表
            List<OrderAggregate> aggregateList = OrderConverter.toAggregateList(poList);

            return ResultDO.buildSuccessResult(aggregateList);
        } catch (Exception e) {
            log.error("查询订单列表失败, query: {}", query, e);
            return ResultDO.buildFailResult("DB_QUERY_ERROR", "查询订单列表异常");
        }
    }
}
```

---

## 四、规则+计算模式 infrastructure 规范

### 4.1 核心职责

规则+计算模式下 RepositoryImpl 的核心职责：

- **加载规则聚合根集合**：从数据库或配置中心加载所有规则数据，转换为规则聚合根
- 规则数据通常是**只读**的，不涉及写操作
- 规则聚合根由 DomainService 用于匹配和计算，**不修改规则聚合根的状态**

### 4.2 返回值规范

| 方法类型    | 返回值                       | 说明          |
|---------|---------------------------|-------------|
| 查询所有规则  | `ResultDO<List<规则聚合根类型>>` | 返回所有规则聚合根列表 |
| 按条件查询规则 | `ResultDO<List<规则聚合根类型>>` | 按业务条件筛选规则   |

### 4.3 完整代码示例

#### Repository 接口（定义在 domain 层）

```java
/**
 * 补贴规则仓储接口（定义在 domain 层）
 */
public interface BonusRuleRepository extends AggregateRepository<BonusRuleAggregate, Long> {

    /** 查询所有补贴规则 */
    ResultDO<List<BonusRuleAggregate>> queryAllRule();
}
```

#### RepositoryImpl 实现（规则+计算模式）

```java
/**
 * 补贴规则仓储实现类（规则+计算模式）
 * 职责：加载规则数据并转换为规则聚合根，纯技术转换，无业务逻辑
 * 命名与 CalculateBonusDomainService 的业务名前缀保持一致
 */
@Slf4j
@Component
public class BonusRuleRepositoryImpl implements BonusRuleRepository {

    @Autowired
    private BonusRuleMapper bonusRuleMapper;

    @Autowired
    private BonusRuleDetailMapper bonusRuleDetailMapper;

    @Override
    public ResultDO<List<BonusRuleAggregate>> queryAllRule() {
        try {
            // 1. 查询所有规则主表 PO
            List<BonusRulePO> rulePOList = bonusRuleMapper.selectAll();
            if (CollectionUtils.isEmpty(rulePOList)) {
                return ResultDO.buildSuccessResult(Collections.emptyList());
            }

            // 2. 查询规则明细 PO
            List<Long> ruleIds = rulePOList.stream()
                .map(BonusRulePO::getId)
                .collect(Collectors.toList());
            List<BonusRuleDetailPO> detailPOList = bonusRuleDetailMapper.selectByRuleIds(ruleIds);

            // 3. 按规则ID分组
            Map<Long, List<BonusRuleDetailPO>> detailMap = detailPOList.stream()
                .collect(Collectors.groupingBy(BonusRuleDetailPO::getRuleId));

            // 4. PO 转换为规则聚合根（包含内部 Entity）
            List<BonusRuleAggregate> aggregateList = rulePOList.stream()
                .map(rulePO -> {
                    BonusRuleAggregate aggregate = BonusRuleConverter.toAggregate(rulePO);
                    List<BonusRuleDetailPO> details = detailMap.getOrDefault(rulePO.getId(), Collections.emptyList());
                    aggregate.setBonusRuleEntity(BonusRuleConverter.toEntity(details));
                    return aggregate;
                })
                .collect(Collectors.toList());

            return ResultDO.buildSuccessResult(aggregateList);
        } catch (Exception e) {
            log.error("查询补贴规则失败", e);
            return ResultDO.buildFailResult("DB_QUERY_ERROR", "查询补贴规则数据异常");
        }
    }
}
```

#### Converter 示例（规则+计算模式）

```java
/**
 * 补贴规则数据转换器
 * 职责：规则 PO 与规则聚合根/实体之间的纯技术转换
 */
public class BonusRuleConverter {

    /**
     * 规则主表 PO 转换为规则聚合根
     */
    public static BonusRuleAggregate toAggregate(BonusRulePO po) {
        if (po == null) {
            return null;
        }
        BonusRuleAggregate aggregate = new BonusRuleAggregate();
        aggregate.setId(po.getId());
        aggregate.setRuleName(po.getRuleName());
        aggregate.setRulePriority(po.getRulePriority());
        return aggregate;
    }

    /**
     * 规则明细 PO 列表转换为规则 Entity
     */
    public static BonusRuleEntity toEntity(List<BonusRuleDetailPO> detailPOList) {
        if (CollectionUtils.isEmpty(detailPOList)) {
            return null;
        }
        // 取第一条明细构建 Entity（根据实际业务调整）
        BonusRuleDetailPO detailPO = detailPOList.get(0);
        BonusRuleEntity entity = new BonusRuleEntity();
        entity.setId(detailPO.getId());
        entity.setBizType(detailPO.getBizType());
        entity.setCondition(detailPO.getCondition());
        entity.setBaseValue(detailPO.getBaseValue());
        return entity;
    }
}
```
