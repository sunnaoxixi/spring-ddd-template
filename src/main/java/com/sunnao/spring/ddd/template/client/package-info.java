/**
 * Client 层（对外接口定义层）
 * <p>
 * 包结构（按业务领域划分顶层包，如 order、booking、refund）：
 * <pre>
 * client/
 * ├── {业务名}/
 * │   ├── enums/           对外枚举（禁止依赖 model 层，需独立定义）
 * │   ├── model/           复用引用对象（行业概念 DTO，如航段、乘机人）
 * │   │   └── {分类名}/    按分类建包（如 passenger/、segment/）
 * │   ├── req/             入参 DTO：{方法名}RequestDTO，继承 BaseDto
 * │   │   └── {分类名}/    按分类建包，禁止完全平铺
 * │   └── res/             返回 DTO：{方法名}ResponseDTO，继承 BaseDto
 * │       └── {分类名}/    按分类建包，禁止完全平铺
 * </pre>
 * <p>
 * 核心规则：
 * <ul>
 * <li>对外提供 RPC 服务的接口定义层，会被外部系统依赖，所有 DTO 必须自包含</li>
 * <li>禁止依赖 model、domain、application、infrastructure 层</li>
 * <li>与 model 层存在相似概念时，在本层独立定义，由 application 层 Assembler 完成转换</li>
 * <li>AppService 接口定义在本层：写模式 {聚合根名}AppService 继承 ApplicationCmdService；
 * 查询类 {聚合根名}QueryAppService / {动词}QueryAppService 继承 ApplicationQueryService</li>
 * <li>返回值统一 ResultDO&lt;{方法名}ResponseDTO&gt;，错误码通过 ResultDO 封装，禁止抛出异常</li>
 * </ul>
 * <p>
 * 详细规范参见 docs/rule/ddd/ddd-client-layer.md
 */
package com.sunnao.spring.ddd.template.client;
