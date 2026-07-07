package com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.po;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 登录日志持久化对象
 * 与 sys_login_log 表一一对应，仅用于 Infrastructure 层内部；
 * 日志只增不改、无逻辑删除与审计更新字段，不继承 BasePO
 */
@Getter
@Setter
@ToString
@Table("sys_login_log")
public class LoginLogPO {

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
     * 用户ID（登录失败时可为空）
     */
    private Long userId;

    /**
     * 登录邮箱
     */
    private String email;

    /**
     * 是否登录成功
     */
    private Boolean success;

    /**
     * 结果码（SUCCESS 或错误码）
     */
    private String code;

    /**
     * 结果说明（失败原因）
     */
    private String msg;

    /**
     * 客户端IP
     */
    private String ip;

    /**
     * 客户端 User-Agent
     */
    private String userAgent;

    /**
     * 登录时间
     */
    private LocalDateTime createAt;
}
