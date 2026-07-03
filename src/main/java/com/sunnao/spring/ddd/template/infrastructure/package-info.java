/**
 * 基础设施层（Infrastructure Layer）
 * <p>
 * 包结构（按业务领域划分顶层包，如 order、booking、refund）：
 * <pre>
 * infrastructure/
 * ├── {业务名}/
 * │   ├── repository/   仓储实现：{业务名}RepositoryImpl，实现 domain 层 Repository 接口
 * │   ├── mysql/        数据库访问
 * │   │   ├── po/       持久化对象：{表对应业务名}PO，纯数据载体
 * │   │   └── mapper/   数据库访问接口：{表对应业务名}Mapper
 * │   └── converter/    数据转换器：{业务名}Converter（聚合根 ↔ PO 纯技术转换，静态方法）
 * </pre>
 * <p>
 * 核心规则：
 * <ul>
 * <li>实现 domain 层定义的 Repository 接口，对 domain 层屏蔽技术细节（数据库、缓存、消息等）</li>
 * <li>仅做纯技术转换，禁止包含任何业务逻辑</li>
 * <li>允许依赖 domain、model 层；禁止依赖 application、client 层</li>
 * <li>允许直接使用中间件（数据库、Redis、消息队列、分布式锁等）；禁止调用第三方外部服务</li>
 * <li>PO、Mapper 仅在本层内部流转，禁止暴露给 domain / application 层</li>
 * <li>数据库异常需捕获并转换为 ResultDO 返回，禁止向上层抛出技术异常</li>
 * </ul>
 * <p>
 * 详细规范参见 docs/rule/ddd/ddd-infrastructure-layer.md
 */
package com.sunnao.spring.ddd.template.infrastructure;
