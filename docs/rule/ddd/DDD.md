# DDD 架构总览

## 一、六边形架构核心设计原则

![六边形架构图](六边形架构图.jpg)

### 1.1 分层与依赖关系

**调用顺序**（自外向内）：

```plain
inAdaptor适配器 → 应用层 → 领域层 → 仓储接口（数据库/缓存/消息等）
                       → outAdaptor适配器（第三方外部服务）
```

> **注意**：Repository 不依赖 Adaptor。Repository 处理本领域的数据库操作、缓存操作、发消息等；Adaptor 处理非本领域的第三方外部服务调用，由 Application 层调用。

**依赖规则**（通过依赖倒置实现解耦）：

- **适配器层**（`inAdaptor/outAdaptor`）：Input Adaptor 调用应用层服务；Output Adaptor 实现**应用层**定义的 Adaptor 接口
- **应用层**（`application`）依赖 `domain`、`client`、`model` 模块
- **基础设施层**（`infrastructure`）实现领域层定义的接口（如仓储接口），依赖 `domain`、`model` 模块
- **领域层**（`domain`）可依赖 `model` 模块（共享枚举、通用业务概念）
- **client 层**：对外提供 RPC 服务的接口定义层，会被外部系统依赖，**禁止依赖 `model` 层**，DTO 必须自包含
- **model 层**：内部共享模型层，`domain`、`application`、`adaptor`、`infrastructure` 均可依赖，但 `client` 禁止依赖
- **关键点**：所有内部模块最终依赖领域层，确保技术细节与业务逻辑分离

### 1.2 领域层的隔离性

- 领域层（`domain`）仅包含纯业务代码，**禁止**直接引用技术框架或第三方库
- 技术实现（如数据库、API调用）通过接口抽象，由外层适配器提供具体实现

## 二、工程结构

```plain
包路径
├── domain/                    # 领域层
│   ├── {业务名}/              # 例如，预订业务
│   │   ├── model/             # 领域模型
│   │   │   ├── aggregate/     # 聚合根
│   │   │   ├── entity/        # 实体
│   │   │   ├── value/         # 值对象
│   │   │   ├── param/         # 参数对象
│   │   │   └── result/        # 计算服务返回对象
│   │   ├── service/           # 领域服务
│   │   └── repository/        # 仓储接口
├── application/               # 应用层
│   ├── {业务名}/              # 例如，预订应用服务实现
│   │   ├── scenario/          # 场景编排
│   │   ├── assembler/         # 对象映射，DTO转换领域对象
│   │   └── model/             # 仅应用层内部使用的DTO，不对外暴露
├── client/                    # 应用层接口定义，对外提供服务
│   ├── {业务名}/
│   │   ├── enums/             # 枚举
│   │   ├── model/             # 复用引用对象（行业概念DTO，被req/res引用）
│   │   ├── req/               # 入参DTO
│   │   └── res/               # 返回对象DTO
├── model/                     # 共享模型，所有模块都可以用的模型
├── infrastructure/            # 基础设施层
│   ├── {业务名}/              # 按业务领域划分（如：order、booking、refund）
│   │   ├── repository/        # 仓储实现
│   │   ├── mysql/             # 数据库访问
│   │   └── converter/         # 数据转换器（聚合根 ↔ PO 互转）
├── adaptor/                   # 防腐层
│   ├── {业务名}/              # 按业务领域划分（如：order、booking、refund）
│   │   ├── input/             # 提供服务，controller，mtop等
│   │   ├── output/            # 适配调用外部第三方接口
│   │   └── inner/             # 内部复用的adaptor
```

## 三、四种 DDD 开发模式

### 3.1 模式对比表

| 模式              | 聚合根/实体        | 业务逻辑位置     | 状态修改 | 典型场景                     |
| ----------------- | ------------------ | ---------------- | -------- | ---------------------------- |
| **写模式**        | 有                 | 聚合根/实体方法  | 是       | 订单创建、状态变更           |
| **读模式**        | 有（作为数据载体） | 无（仅数据转换） | 否       | 订单查询                     |
| **规则+计算模式** | 有                 | 聚合根/实体方法  | 否       | 补贴规则匹配与计算           |
| **纯计算模式**    | 无                 | DomainService    | 否       | 搜索控制、费用计算、视图渲染 |

### 3.2 模式选择决策树

```plain
业务场景分析
    │
    ├── 是否需要修改数据状态？
    │   ├── 是 → 写模式
    │   │   特征：有聚合根/实体，业务逻辑在聚合根方法中，方法会修改实体属性
    │   │
    │   └── 否 → 继续判断
    │       │
    │       ├── 是否有业务逻辑需要处理？
    │       │   ├── 否 → 读模式
    │       │   │   特征：纯查询，聚合根仅作为数据载体，无业务逻辑
    │       │   │
    │       │   └── 是 → 继续判断
    │       │       │
    │       │       ├── 业务逻辑是否基于规则？
    │       │       │   ├── 是 → 规则+计算模式
    │       │       │   │   特征：规则建模为聚合根，通过聚合根方法匹配和计算，不修改状态
    │       │       │   │
    │       │       │   └── 否 → 纯计算模式
    │       │       │       特征：无聚合根，业务逻辑在DomainService中，基于输入参数计算
```

### 3.3 各模式的调用链

**写模式调用链：**

```plain
input adaptor → AppService → DomainService → Aggregate方法 → Repository.save
```

**读模式调用链（查询本领域数据）：**

```plain
input adaptor → QueryAppService → Repository.query → 返回聚合根/Result → 转换为DTO
```

**读模式调用链（查询外部数据）：**

```plain
input adaptor → QueryAppService → Adaptor → 返回第三方DTO → 直接返回或简单裁剪
```

**纯计算模式调用链：**

```plain
input adaptor → QueryAppService → DomainService → 计算并返回Result
```

**规则+计算模式调用链：**

```plain
input adaptor → QueryAppService → DomainService → Repository查询规则聚合根
                                                → 聚合根.matchRule/calculate → 返回Result
```

## 四、规范文件组合使用指南

### 4.1 写模式开发

需要阅读的规范文件：

1. [`ddd-domain-layer.md`](ddd-domain-layer.md) → 基础规范 + 写模式 domain 规范
2. [`ddd-application-layer.md`](ddd-application-layer.md) → 基础规范 + 写模式 application 规范
3. [`ddd-adaptor-layer.md`](ddd-adaptor-layer.md) → 基础规范 + 写模式 adaptor 规范
4. [`ddd-infrastructure-layer.md`](ddd-infrastructure-layer.md) → 基础规范 + 写模式 infrastructure 规范
5. [`ddd-client-layer.md`](ddd-client-layer.md) → client 层规范
6. [`ddd-model-layer.md`](ddd-model-layer.md) → model 层规范

### 4.2 读模式开发

需要阅读的规范文件：

1. [`ddd-domain-layer.md`](ddd-domain-layer.md) → 基础规范 + 读模式 domain 规范
2. [`ddd-application-layer.md`](ddd-application-layer.md) → 基础规范 + 读模式 application 规范
3. [`ddd-adaptor-layer.md`](ddd-adaptor-layer.md) → 基础规范 + 读模式 adaptor 规范
4. [`ddd-infrastructure-layer.md`](ddd-infrastructure-layer.md) → 基础规范 + 读模式 infrastructure 规范
5. [`ddd-client-layer.md`](ddd-client-layer.md) → client 层规范
6. [`ddd-model-layer.md`](ddd-model-layer.md) → model 层规范

### 4.3 纯计算模式开发

需要阅读的规范文件：

1. [`ddd-domain-layer.md`](ddd-domain-layer.md) → 基础规范 + 纯计算模式 domain 规范
2. [`ddd-application-layer.md`](ddd-application-layer.md) → 基础规范 + 纯计算模式 application 规范
3. [`ddd-adaptor-layer.md`](ddd-adaptor-layer.md) → 基础规范 + 纯计算模式 adaptor 规范
4. [`ddd-client-layer.md`](ddd-client-layer.md) → client 层规范
5. [`ddd-model-layer.md`](ddd-model-layer.md) → model 层规范

> **注意**：纯计算模式不需要 Infrastructure 层，业务逻辑完全由 DomainService 承载，无聚合根和实体，不涉及数据持久化。

### 4.4 规则+计算模式开发

需要阅读的规范文件：

1. [`ddd-domain-layer.md`](ddd-domain-layer.md) → 基础规范 + 规则+计算模式 domain 规范
2. [`ddd-application-layer.md`](ddd-application-layer.md) → 基础规范 + 规则+计算模式 application 规范
3. [`ddd-adaptor-layer.md`](ddd-adaptor-layer.md) → 基础规范
4. [`ddd-infrastructure-layer.md`](ddd-infrastructure-layer.md) → 基础规范 + 规则+计算模式 infrastructure 规范
5. [`ddd-client-layer.md`](ddd-client-layer.md) → client 层规范
6. [`ddd-model-layer.md`](ddd-model-layer.md) → model 层规范
