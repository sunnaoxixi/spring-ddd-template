/**
 * 通用技术基类包
 * <p>
 * 存放各层共享的技术基类与标记接口，不承载任何业务概念：
 * <pre>
 * common/
 * ├── base/        DDD 基类（BaseAggregate、BaseEntity、BaseValue、BaseParam、
 * │                BaseResult、BaseDto、AggregateRepository）
 * ├── exception/   异常（AggregateException、BizException）
 * ├── lock/        分级锁（LevelLock）
 * ├── result/      统一结果对象（ResultDO）
 * └── service/     应用服务标记接口（ApplicationCmdService、ApplicationQueryService）
 * </pre>
 * <p>
 * 注意：与 model 层（共享业务模型）的区别在于，本包仅存放技术抽象；
 * 跨模块共享的枚举、通用业务概念应放在 model 层。
 */
package com.sunnao.spring.ddd.template.common;
