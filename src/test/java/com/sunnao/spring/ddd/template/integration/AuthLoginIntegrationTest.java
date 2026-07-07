package com.sunnao.spring.ddd.template.integration;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证登录集成测试（登录 → 当前用户 → 登出全流程）
 * <p>
 * 依赖云上 PostgreSQL / Redis（application-test.yaml 环境变量占位，Flyway 自动建表），
 * 未配置 TEST_PG_URL / TEST_REDIS_HOST 时自动跳过。
 * 使用 V1 迁移种子管理员 admin@example.com / admin123456。
 */
@EnabledIfEnvironmentVariable(named = "TEST_PG_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "TEST_REDIS_HOST", matches = ".+")
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class AuthLoginIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "admin123456";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("登录 → 查询当前用户 → 登出 → 会话失效")
    void loginMeLogoutFlow() throws Exception {
        // 1. 登录获取 token
        String loginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.createObj()
                                .set("email", ADMIN_EMAIL)
                                .set("password", ADMIN_PASSWORD)
                                .toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONObject loginJson = JSONUtil.parseObj(loginBody);
        assertTrue(loginJson.getBool("success"), "登录应成功: " + loginBody);
        String tokenName = loginJson.getJSONObject("data").getStr("tokenName");
        String tokenValue = loginJson.getJSONObject("data").getStr("tokenValue");
        assertNotNull(tokenName);
        assertNotNull(tokenValue);

        // 2. 携带 token 查询当前登录用户
        String meBody = mockMvc.perform(get("/api/auth/me").header(tokenName, tokenValue))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONObject meJson = JSONUtil.parseObj(meBody);
        assertTrue(meJson.getBool("success"), "查询当前用户应成功: " + meBody);
        assertEquals(ADMIN_EMAIL, meJson.getJSONObject("data").getStr("email"));

        // 3. 登出
        String logoutBody = mockMvc.perform(post("/api/auth/logout").header(tokenName, tokenValue))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(JSONUtil.parseObj(logoutBody).getBool("success"), "登出应成功: " + logoutBody);

        // 4. 登出后会话失效（全局异常处理器返回 401）
        mockMvc.perform(get("/api/auth/me").header(tokenName, tokenValue))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("密码错误：登录失败且不暴露账号是否存在")
    void loginShouldFailWithWrongPassword() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.createObj()
                                .set("email", ADMIN_EMAIL)
                                .set("password", "wrong-password")
                                .toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONObject json = JSONUtil.parseObj(body);
        assertEquals(Boolean.FALSE, json.getBool("success"));
        assertEquals(ErrorCodeEnum.AUTH_FAIL.getCode(), json.getStr("code"));
    }
}
