package com.sunnao.spring.ddd.template.adaptor.system;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.sunnao.spring.ddd.template.adaptor.system.dict.input.DictController;
import com.sunnao.spring.ddd.template.adaptor.system.file.input.FileController;
import com.sunnao.spring.ddd.template.adaptor.system.log.input.LogController;
import com.sunnao.spring.ddd.template.adaptor.system.online.input.OnlineController;
import com.sunnao.spring.ddd.template.adaptor.system.role.input.RoleController;
import com.sunnao.spring.ddd.template.adaptor.system.user.input.UserController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RoleAuthorizationTest {

    @Test
    void managementControllersShouldRequireAdminRole() {
        assertRole(UserController.class, new String[]{"admin"}, SaMode.AND);
        assertRole(RoleController.class, new String[]{"admin"}, SaMode.AND);
        assertRole(LogController.class, new String[]{"admin"}, SaMode.AND);
        assertRole(OnlineController.class, new String[]{"admin"}, SaMode.AND);
    }

    @Test
    void fileControllerShouldAllowAdminOrUserRole() {
        assertRole(FileController.class, new String[]{"admin", "user"}, SaMode.OR);
    }

    @Test
    void dictionaryWritesShouldRequireAdminRole() {
        assertMethodRole("createDictType", new String[]{"admin"}, SaMode.AND);
        assertMethodRole("updateDictType", new String[]{"admin"}, SaMode.AND);
        assertMethodRole("deleteDictType", new String[]{"admin"}, SaMode.AND);
        assertMethodRole("createDictData", new String[]{"admin"}, SaMode.AND);
        assertMethodRole("updateDictData", new String[]{"admin"}, SaMode.AND);
        assertMethodRole("deleteDictData", new String[]{"admin"}, SaMode.AND);
    }

    @Test
    void dictionaryReadsShouldAllowAdminOrUserRole() {
        assertMethodRole("queryDictTypePage", new String[]{"admin", "user"}, SaMode.OR);
        assertMethodRole("queryDictDataList", new String[]{"admin", "user"}, SaMode.OR);
        assertMethodRole("queryAllDictDataList", new String[]{"admin", "user"}, SaMode.OR);
    }

    private void assertMethodRole(String methodName, String[] roles, SaMode mode) {
        Method method = Arrays.stream(DictController.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
        assertRole(method.getAnnotation(SaCheckRole.class), roles, mode, method.toString());
    }

    private void assertRole(Class<?> controller, String[] roles, SaMode mode) {
        assertRole(controller.getAnnotation(SaCheckRole.class), roles, mode, controller.getName());
    }

    private void assertRole(SaCheckRole annotation, String[] roles, SaMode mode, String source) {
        assertNotNull(annotation, source);
        assertArrayEquals(roles, annotation.value(), source);
        assertEquals(mode, annotation.mode(), source);
    }
}
