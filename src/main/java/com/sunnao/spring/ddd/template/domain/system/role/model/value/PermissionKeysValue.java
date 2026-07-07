package com.sunnao.spring.ddd.template.domain.system.role.model.value;

import com.sunnao.spring.ddd.template.common.model.BaseValue;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 权限标识集合（值对象）
 * <p>
 * 封装角色所拥有的权限 key 列表，保证不可变性。
 */
@Getter
public class PermissionKeysValue extends BaseValue {

    /**
     * -- GETTER --
     * 获取权限 key 列表（不可变）
     */
    private final List<String> keys;

    private PermissionKeysValue(List<String> keys) {
        this.keys = keys == null ? Collections.emptyList() : Collections.unmodifiableList(keys);
    }

    public static PermissionKeysValue of(List<String> keys) {
        return new PermissionKeysValue(keys);
    }

    public static PermissionKeysValue empty() {
        return new PermissionKeysValue(Collections.emptyList());
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    public int size() {
        return keys.size();
    }
}
