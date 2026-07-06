package com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.po;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 操作日志持久化对象
 * 与 sys_oper_log 表一一对应，仅用于 Infrastructure 层内部；
 * 日志只增不改、无逻辑删除与审计更新字段，不继承 BasePO
 */
@Getter
@Setter
@ToString
@Table("sys_oper_log")
public class OperLogPO {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 业务模块
     */
    private String module;

    /**
     * 操作动作
     */
    private String action;

    /**
     * 请求 URI
     */
    private String uri;

    /**
     * 请求参数摘要
     */
    private String params;

    /**
     * 结果码
     */
    private String resultCode;

    /**
     * 耗时（毫秒）
     */
    private Long costMs;

    /**
     * 客户端IP
     */
    private String ip;

    /**
     * 操作时间
     */
    private LocalDateTime createAt;
}
