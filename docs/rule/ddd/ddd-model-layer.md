---

alwaysApply: true

---

# model 层开发规范
## 一、共享模型定义规范
model 层存放所有模块都可以使用的共享模型，包括：

+ **枚举**：跨模块共享的枚举定义
+ **通用业务概念**：如航段（Segment）、乘机人（Passenger）等通用业务对象

## 二、依赖关系规范
model 层是内部共享模型层，**仅限项目内部模块使用**。

**允许依赖 model 层的模块：**

| 模块 | 是否可依赖 model | 说明 |
| --- | --- | --- |
| `domain` | ✅ 允许 | 领域层可使用共享枚举、通用业务概念 |
| `application` | ✅ 允许 | 应用层可使用共享模型进行场景编排 |
| `adaptor` | ✅ 允许 | 适配器层可使用共享模型进行协议转换 |
| `infrastructure` | ✅ 允许 | 基础设施层可使用共享模型进行数据转换 |
| `client` | ❌ **禁止** | client 是对外提供 RPC 服务的接口定义层，会被外部系统依赖。如果 client 依赖 model，会导致外部调用方被迫传递依赖 model 包，造成不必要的依赖扩散 |


**关键原则**：client 层的 DTO 必须自包含，不能引用 model 层的类。如果 client 和 model 中存在相似的概念，应在 client 层独立定义，通过 Assembler 在 application 层完成转换。

## 三、适用场景
| 场景 | 说明 |
| --- | --- |
| 跨模块枚举 | 多个业务模块共用的枚举（如国内/国际枚举 DomesticIntlEnum） |
| 通用业务概念 | 不属于某个特定领域但被多个领域使用的业务对象 |
| 共享常量 | 跨模块共享的常量定义 |


## 四、代码模板
### 4.1 共享枚举模板
```java
package {包路径}.model.{业务名};

/**
 * {枚举描述}
 * 共享模型：所有模块都可以使用
 */
public enum {枚举名}Enum {

    {枚举值1}("{code1}", "{描述1}"),
    {枚举值2}("{code2}", "{描述2}"),
    ;

    private final String code;
    private final String description;

    {枚举名}Enum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
```

### 4.2 通用业务概念模板
```java
package {包路径}.model.{业务名};

import java.io.Serializable;

/**
 * {业务概念名} 共享模型
 * 说明：跨模块共享的通用业务概念
 */
public class {业务概念名} implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * {字段描述}
     */
    private {字段类型} {字段名};

    // getter/setter
}
```
