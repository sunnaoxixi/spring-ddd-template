package com.sunnao.spring.ddd.template.common.result;

import lombok.Getter;

/**
 * 统一错误码枚举
 * <p>
 * 全局错误码收敛入口：所有层构建失败 ResultDO 或抛出 BizException 时统一引用本枚举，
 * 禁止散落字符串字面量。每个错误码附带默认文案，可在调用处覆写更具体的提示。
 */
@Getter
public enum ErrorCodeEnum {

    // ==================== 通用 ====================

    /**
     * 默认失败
     */
    FAIL("FAIL", "操作失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR("PARAM_ERROR", "参数错误"),

    /**
     * 系统异常
     */
    SYSTEM_ERROR("SYSTEM_ERROR", "系统异常"),

    /**
     * 获取锁失败
     */
    LOCK_FAIL("LOCK_FAIL", "获取锁失败，请稍后重试"),

    /**
     * 未登录或登录已过期
     */
    NOT_LOGIN("NOT_LOGIN", "未登录或登录已过期"),

    /**
     * 无权限访问
     */
    NO_PERMISSION("NO_PERMISSION", "无权限访问"),

    /**
     * 请求不合法（格式/类型错误）
     */
    BAD_REQUEST("BAD_REQUEST", "请求不合法"),

    /**
     * 请求资源不存在
     */
    NOT_FOUND("NOT_FOUND", "请求资源不存在"),

    /**
     * 数据异常（数据缺失/不完整）
     */
    DATA_ERROR("DATA_ERROR", "数据异常"),

    /**
     * 状态不合法（状态机流转冲突）
     */
    STATUS_INVALID("STATUS_INVALID", "状态不合法"),

    // ==================== 持久层 ====================

    /**
     * 数据库查询异常
     */
    DB_QUERY_ERROR("DB_QUERY_ERROR", "数据库查询异常"),

    /**
     * 数据库保存异常
     */
    DB_SAVE_ERROR("DB_SAVE_ERROR", "数据库保存异常"),

    /**
     * 数据库删除异常
     */
    DB_DELETE_ERROR("DB_DELETE_ERROR", "数据库删除异常"),

    // ==================== 认证 ====================

    /**
     * 认证失败（用户不存在与密码错误统一提示，防账号枚举）
     */
    AUTH_FAIL("AUTH_FAIL", "邮箱或密码错误"),

    /**
     * 登录失败次数过多，暂时锁定（防爆破）
     */
    AUTH_LOCKED("AUTH_LOCKED", "登录失败次数过多，请稍后重试"),

    /**
     * 账号已被禁用
     */
    USER_DISABLED("USER_DISABLED", "账号已被禁用，请联系管理员"),

    // ==================== 用户 ====================

    /**
     * 用户不存在
     */
    USER_NOT_FOUND("USER_NOT_FOUND", "用户不存在"),

    /**
     * 邮箱已被注册
     */
    EMAIL_DUPLICATE("EMAIL_DUPLICATE", "邮箱已被注册"),

    // ==================== 角色 ====================

    /**
     * 角色不存在
     */
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "角色不存在"),

    /**
     * 角色标识已存在
     */
    ROLE_KEY_DUPLICATE("ROLE_KEY_DUPLICATE", "角色标识已存在"),

    /**
     * 内置角色不允许该操作
     */
    ROLE_BUILT_IN("ROLE_BUILT_IN", "内置角色不允许该操作"),

    // ==================== 字典 ====================

    /**
     * 字典类型不存在
     */
    DICT_TYPE_NOT_FOUND("DICT_TYPE_NOT_FOUND", "字典类型不存在"),

    /**
     * 字典数据不存在
     */
    DICT_DATA_NOT_FOUND("DICT_DATA_NOT_FOUND", "字典数据不存在"),

    /**
     * 同类型下字典值已存在
     */
    DICT_VALUE_DUPLICATE("DICT_VALUE_DUPLICATE", "同类型下字典值已存在"),

    /**
     * 字典类型键已存在
     */
    TYPE_KEY_DUPLICATE("TYPE_KEY_DUPLICATE", "字典类型键已存在"),

    // ==================== 文件 ====================

    /**
     * 文件不存在
     */
    FILE_NOT_FOUND("FILE_NOT_FOUND", "文件不存在"),

    /**
     * 文件内容为空
     */
    FILE_EMPTY("FILE_EMPTY", "文件内容不能为空"),

    /**
     * 文件大小超出限制
     */
    FILE_TOO_LARGE("FILE_TOO_LARGE", "文件大小超出限制"),

    /**
     * 文件读取失败
     */
    FILE_READ_ERROR("FILE_READ_ERROR", "文件读取失败"),

    /**
     * 文件存储失败
     */
    FILE_STORE_ERROR("FILE_STORE_ERROR", "文件存储失败"),

    /**
     * 文件删除失败
     */
    FILE_DELETE_ERROR("FILE_DELETE_ERROR", "文件删除失败"),

    /**
     * 文件路径不合法
     */
    FILE_PATH_INVALID("FILE_PATH_INVALID", "文件路径不合法"),
    ;

    /**
     * 错误码
     */
    private final String code;

    /**
     * 默认错误文案
     */
    private final String defaultMsg;

    ErrorCodeEnum(String code, String defaultMsg) {
        this.code = code;
        this.defaultMsg = defaultMsg;
    }
}
