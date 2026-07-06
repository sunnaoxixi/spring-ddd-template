---
alwaysApply: true
---

# client 层开发规范

## 一、包结构规范

```plain
client/
├── {业务名}/
│   ├── enums/             # 对外枚举（client 禁止依赖 model，需独立定义）
│   ├── model/             # 复用引用对象（行业概念DTO，如航段、乘机人等）
│   │   └── {分类名}/      # 按分类建包（如：passenger/、segment/）
│   ├── req/               # 入参DTO（RequestDTO）
│   │   └── {分类名}/      # 按分类建包
│   └── res/               # 返回对象DTO（ResponseDTO）
│       └── {分类名}/      # 按分类建包
```

**包内分类规则**：`req/`、`res/`、`model/` 下的类**禁止完全平铺**，必须按分类名建子包。例如：将乘机人相关的类（乘机人信息、证件信息、地址信息等）统一放到
`passenger/` 包下。

## 二、依赖关系规范

client 层是对外提供 RPC 服务的接口定义层，会被外部系统依赖。

**依赖约束：**

| 规则                            | 说明                                                                  |
|-------------------------------|---------------------------------------------------------------------|
| ❌ **禁止依赖 `model` 层**          | model 是内部共享模型层，如果 client 依赖 model，会导致外部调用方被迫传递依赖 model 包，造成不必要的依赖扩散 |
| ❌ **禁止依赖 `domain` 层**         | 领域层是内部核心业务逻辑，不应暴露给外部                                                |
| ❌ **禁止依赖 `application` 层**    | 应用层是内部场景编排，不应暴露给外部                                                  |
| ❌ **禁止依赖 `infrastructure` 层** | 基础设施层是内部技术实现，不应暴露给外部                                                |

**关键原则**：client 层的所有 DTO 必须**自包含**，不能引用其他内部模块的类。如果 client 和 model 中存在相似的概念，应在
client 层独立定义，通过 Assembler 在 application 层完成转换。

## 三、AppService 接口定义规范

client 层定义应用层对外提供的服务接口，application 层提供实现。

### 3.1 写模式接口规范

| 规则项  | 规范                                                                 |
|------|--------------------------------------------------------------------|
| 接口命名 | `{聚合根名}AppService`（如 `OrderAppService`）                            |
| 继承规范 | 必须继承 `ApplicationCmdService`                                       |
| 方法命名 | 使用业务动词（如 `createOrder`、`confirmPayment`），禁止技术动词（如 `save`、`update`） |
| 参数命名 | `{方法名}RequestDTO`（如 `CreateOrderRequestDTO`）                       |
| 返回值  | `ResultDO<{方法名}ResponseDTO>`（如 `ResultDO<CreateOrderResponseDTO>`） |

```java
/**
 * {聚合根名} 应用服务接口（写模式）
 * 职责：定义{聚合根名}相关的写操作接口
 */
public interface {聚合根名}AppService extends ApplicationCmdService {

    /**
     * {方法描述}
     * @param requestDTO 请求参数
     * @return 操作结果
     */
    ResultDO<{方法名}ResponseDTO> {方法名}({方法名}RequestDTO requestDTO);
}
```

### 3.2 读模式接口规范

| 规则项  | 规范                                                                    |
|------|-----------------------------------------------------------------------|
| 接口命名 | `{聚合根名}QueryAppService`（如 `OrderQueryAppService`）                     |
| 继承规范 | 必须继承 `ApplicationQueryService`                                        |
| 方法命名 | 使用动词明确查询意图（如 `queryOrderList`、`getOrderDetail`），避免抽象词汇（如 `process`）   |
| 参数命名 | `{方法名}RequestDTO`（如 `QueryOrderListRequestDTO`）                       |
| 返回值  | `ResultDO<{方法名}ResponseDTO>`（如 `ResultDO<QueryOrderListResponseDTO>`） |

```java
/**
 * {聚合根名} 查询应用服务接口（读模式）
 * 职责：定义{聚合根名}相关的查询操作接口
 */
public interface {聚合根名}QueryAppService extends ApplicationQueryService {

    /**
     * 查询{聚合根名}列表
     * @param requestDTO 查询参数
     * @return 查询结果
     */
    ResultDO<Query{聚合根名}ListResponseDTO> query{聚合根名}List(Query{聚合根名}ListRequestDTO requestDTO);

    /**
     * 获取{聚合根名}详情
     * @param requestDTO 查询参数
     * @return 详情结果
     */
    ResultDO<Get{聚合根名}DetailResponseDTO> get{聚合根名}Detail(Get{聚合根名}DetailRequestDTO requestDTO);
}
```

### 3.3 纯计算模式接口规范

| 规则项  | 规范                                                           |
|------|--------------------------------------------------------------|
| 接口命名 | `{动词}QueryAppService`（如 `RefundPreCalculateQueryAppService`） |
| 继承规范 | 必须继承 `ApplicationQueryService`                               |
| 特殊要求 | **一个计算方法一个类**                                                |
| 方法命名 | 使用动词                                                         |
| 参数命名 | `{方法名}RequestDTO`                                            |
| 返回值  | `ResultDO<{方法名}ResponseDTO>`                                 |

```java
/**
 * {计算动词} 查询应用服务接口（纯计算模式）
 * 职责：定义{计算描述}的计算接口
 * 注意：一个计算方法一个类
 */
public interface {计算动词}QueryAppService extends ApplicationQueryService {

    /**
     * {计算描述}
     * @param requestDTO 计算参数
     * @return 计算结果
     */
    ResultDO<{计算动词}ResponseDTO> {计算动词小写}({计算动词}RequestDTO requestDTO);
}
```

### 3.4 规则+计算模式接口规范

同纯计算模式接口规范。

## 四、model 包复用对象规范

### 4.1 定位与用途

model 包存放的是 RequestDTO 和 ResponseDTO 中**复用引用的对象**，这类对象通常具备行业概念，如航段（Segment）、乘机人（Passenger）、票价（Fare）等。

### 4.2 命名与继承规范

- 命名后缀：统一以 `DTO` 结尾（如 `PassengerDTO`、`SegmentDTO`、`CredentialDTO`）
- 命名应体现行业概念，具有业务含义
- 继承规范：必须继承 `BaseDto`

### 4.3 复用注意事项

复用 model 对象时需要注意**避免过度复用**导致的问题：

- **入参膨胀**：如果复用导致 RequestDTO 中包含了调用方不需要填写的多余字段，会增加调用者的入参复杂度，降低接口友好性。此时应为该场景定义独立的精简
  DTO，而非强行复用
- **出参冗余**：如果复用导致 ResponseDTO 中输出了不必要的字段，会暴露多余信息。此时应裁剪为该场景专用的 DTO，仅包含需要输出的字段

**判断标准**：当复用导致存在明显的冗余字段或引入了不相关的字段时，应考虑定义独立的 DTO 而非复用。

### 4.4 代码模板

```java
/**
 * {业务概念名} DTO
 * 复用引用对象，被多个 RequestDTO/ResponseDTO 引用
 */
public class {业务概念名}DTO extends BaseDto {

    private static final long serialVersionUID = 1L;

    /**
     * {字段描述}
     */
    private {字段类型} {字段名};

    // getter/setter
}
```

## 五、RequestDTO 规范

- 命名：`{方法名}RequestDTO`，必须以 `RequestDTO` 作为后缀
- 继承规范：必须继承 `BaseDto`
- 必须使用 RequestDTO 封装参数，**禁止**使用原始类型或 Map
- 每个方法对应一个独立的 RequestDTO

```java
/**
 * {方法名} 请求DTO
 */
public class {方法名}RequestDTO extends BaseDto {

    private static final long serialVersionUID = 1L;

    /**
     * {字段描述}
     */
    private {字段类型} {字段名};

    // getter/setter
}
```

## 六、ResponseDTO 规范

- 命名：`{方法名}ResponseDTO`，必须以 `ResponseDTO` 作为后缀
- 继承规范：必须继承 `BaseDto`
- 统一通过 `ResultDO<ResponseDTO>` 封装返回
- 错误码通过 ResultDO 封装，**禁止**直接抛出异常

```java
/**
 * {方法名} 响应DTO
 */
public class {方法名}ResponseDTO extends BaseDto {

    private static final long serialVersionUID = 1L;

    /**
     * {字段描述}
     */
    private {字段类型} {字段名};

    // getter/setter
}
```
