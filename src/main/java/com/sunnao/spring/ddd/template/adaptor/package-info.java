/**
 * 适配器层（Adaptor Layer，防腐层 ACL）
 * <p>
 * 包结构（按业务领域划分顶层包，如 order、booking、refund）：
 * <pre>
 * adaptor/
 * ├── {业务名}/
 * │   ├── input/    输入适配：Controller（{业务名}Controller）、MQ 消费者、定时任务等，
 * │   │             调用 client 层定义的 AppService 接口
 * │   ├── output/   输出适配：{业务能力}AdaptorImpl，实现 application 层定义的 Adaptor 接口，
 * │   │             封装第三方外部服务调用；{业务名}Converter 负责第三方格式与业务 DTO 互转
 * │   └── inner/    内部复用适配器（如公共认证、日志拦截器），仅被其他适配器调用
 * </pre>
 * <p>
 * 核心规则：
 * <ul>
 * <li>Input Adaptor 只做参数转换与调用应用层服务，禁止编写业务逻辑、禁止绕过应用层直接调用领域层</li>
 * <li>Output Adaptor 接口按 Application 层业务需要定义（定义在 application 层），
 * 实现在本层，第三方接口差异由 Converter 在实现类内部消化</li>
 * <li>允许使用设计模式做技术适配（如按渠道 ID 路由不同第三方接口）</li>
 * <li>返回值统一 ResultDO&lt;T&gt;，仅包含 Application 需要的字段（防腐简化）</li>
 * </ul>
 * <p>
 * 详细规范参见 docs/rule/ddd/ddd-adaptor-layer.md
 */
package com.sunnao.spring.ddd.template.adaptor;
