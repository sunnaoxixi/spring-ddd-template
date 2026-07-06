package com.sunnao.spring.ddd.template.common.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 统一结果对象
 * <p>
 * 各层方法（DomainService、AppService、Repository、Adaptor）统一通过 ResultDO 封装返回值与错误码，
 * 禁止向调用方直接抛出异常。
 *
 * @param <T> 数据类型
 */
@Getter
@Setter
@ToString
public class ResultDO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 默认失败错误码
     */
    private static final String DEFAULT_FAIL_CODE = "FAIL";

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 构建成功结果（无数据）
     */
    public static <T> ResultDO<T> buildSuccessResult() {
        return buildSuccessResult(null);
    }

    /**
     * 构建成功结果
     *
     * @param data 返回数据
     */
    public static <T> ResultDO<T> buildSuccessResult(T data) {
        ResultDO<T> result = new ResultDO<>();
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    /**
     * 构建失败结果（使用默认错误码）
     *
     * @param msg 错误信息
     */
    public static <T> ResultDO<T> buildFailResult(String msg) {
        return buildFailResult(DEFAULT_FAIL_CODE, msg);
    }

    /**
     * 构建失败结果
     *
     * @param code 错误码
     * @param msg  错误信息
     */
    public static <T> ResultDO<T> buildFailResult(String code, String msg) {
        ResultDO<T> result = new ResultDO<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
