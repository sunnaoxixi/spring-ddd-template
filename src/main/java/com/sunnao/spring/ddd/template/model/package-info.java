/**
 * Model 层（内部共享模型层）
 * <p>
 * 存放所有内部模块都可以使用的共享模型：
 * <ul>
 * <li>跨模块共享的枚举（如 DomesticIntlEnum），命名规范：{枚举名}Enum</li>
 * <li>通用业务概念（如航段 Segment、乘机人 Passenger），实现 Serializable</li>
 * <li>跨模块共享的常量定义</li>
 * </ul>
 * <p>
 * 依赖规则：
 * <ul>
 * <li>domain、application、adaptor、infrastructure 层允许依赖本层</li>
 * <li>client 层禁止依赖本层（避免依赖扩散到外部调用方）</li>
 * </ul>
 * <p>
 * 详细规范参见 docs/rule/ddd/ddd-model-layer.md
 */
package com.sunnao.spring.ddd.template.model;
