/**
 * 应用层（Application Layer）
 * <p>
 * 包结构（按业务领域划分顶层包，如 order、booking、refund）：
 * <pre>
 * application/
 * ├── {业务名}/
 * │   ├── scenario/    场景编排：AppService 实现类、Adaptor 接口定义
 * │   ├── assembler/   对象映射：{聚合根名}Assembler，DTO 与领域对象互转
 * │   └── model/       仅应用层内部使用的 DTO，不对外暴露
 * </pre>
 * <p>
 * 核心规则：
 * <ul>
 * <li>场景编排层，负责编排调用 DomainService、Repository、Adaptor，不包含核心业务逻辑</li>
 * <li>CQRS 读写分离：写实现 {聚合根名}AppServiceImpl；查询实现 {聚合根名}QueryAppServiceImpl
 * （纯计算/规则+计算模式为 {动词}QueryAppServiceImpl，一个计算方法一个类）</li>
 * <li>允许依赖 domain、client、model 层；禁止依赖其他业务域二方包、中间件</li>
 * <li>入参统一 RequestDTO 并通过 requestDTO.check() 自校验；返回值统一 ResultDO&lt;ResponseDTO&gt;</li>
 * <li>Adaptor 接口定义在本层（按 Application 的业务需要定义），实现在 adaptor 层</li>
 * <li>禁止直接访问数据库（走 Repository）、直接访问外部服务（走 Adaptor）</li>
 * </ul>
 * <p>
 * 详细规范参见 docs/rule/ddd/ddd-application-layer.md
 */
package com.sunnao.spring.ddd.template.application;
