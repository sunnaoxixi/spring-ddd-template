/**
 * 领域层（Domain Layer）
 * <p>
 * 包结构（按业务领域划分顶层包，如 order、booking、refund）：
 * <pre>
 * domain/
 * ├── {业务名}/
 * │   ├── model/
 * │   │   ├── aggregate/   聚合根：{名词}Aggregate，继承 BaseAggregate
 * │   │   ├── entity/      实体：{名词}Entity，继承 BaseEntity
 * │   │   ├── value/       值对象：{名词}Value，继承 BaseValue，不可变
 * │   │   ├── param/       参数对象：{方法名}Param，继承 BaseParam
 * │   │   └── result/      计算结果：{方法名}Result，继承 BaseResult
 * │   ├── service/         领域服务：{业务名}DomainService + {业务名}DomainServiceImpl
 * │   └── repository/      仓储接口：{业务名}Repository，继承 AggregateRepository
 * </pre>
 * <p>
 * 核心规则：
 * <ul>
 * <li>仅包含纯业务代码，禁止直接引用技术框架或第三方库（Spring 与静态工具包例外）</li>
 * <li>可依赖 model 层（共享枚举、通用业务概念）</li>
 * <li>禁止使用设计模式（策略、工厂、模板方法等），业务分支直接用 if/else 内聚处理</li>
 * <li>Repository 接口定义在本层，实现在 infrastructure 层，不依赖 Adaptor</li>
 * <li>禁止出现 Utils、常量类、配置类等非领域概念代码</li>
 * <li>聚合根/实体校验失败抛 AggregateException，DomainService 统一捕获并转换为 ResultDO</li>
 * </ul>
 * <p>
 * 详细规范参见 docs/rule/ddd/ddd-domain-layer.md
 */
package com.sunnao.spring.ddd.template.domain;
