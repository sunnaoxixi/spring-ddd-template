package com.sunnao.spring.ddd.template.integration;

import cn.hutool.core.util.IdUtil;
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
 * 认证注册集成测试（匿名注册 → 自动登录 → 当前用户）
 * <p>
 * 依赖云上 PostgreSQL / Redis（application-test.yaml 环境变量占位，Flyway 自动建表），
 * 未配置 TEST_PG_URL / TEST_REDIS_HOST 时自动跳过。
 * 注册接口已在 SaTokenConfigure 放行，无需登录态即可访问。
 */
@EnabledIfEnvironmentVariable(named = "TEST_PG_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "TEST_REDIS_HOST", matches = ".+")
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class AuthRegisterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("匿名注册成功 → 自动登录 → 携带 token 查询当前用户（默认 user 角色）")
    void registerThenAutoLoginFlow() throws Exception {
        String email = "reg-" + IdUtil.fastSimpleUUID().substring(0, 8) + "@example.com";

        // 1. 匿名调用注册接口（无需登录态）
        String registerBody = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.createObj()
                                .set("email", email)
                                .set("nickname", "注册测试用户")
                                .set("password", "reg123456")
                                .set("confirmPassword", "reg123456")
                                .toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONObject registerJson = JSONUtil.parseObj(registerBody);
        assertTrue(registerJson.getBool("success"), "注册应成功: " + registerBody);
        JSONObject data = registerJson.getJSONObject("data");
        String tokenName = data.getStr("tokenName");
        String tokenValue = data.getStr("tokenValue");
        assertNotNull(tokenName);
        assertNotNull(tokenValue);
        assertNotNull(data.getLong("userId"), "应返回用户ID: " + registerBody);
        assertTrue(data.getJSONArray("roles").contains("user"), "应默认授予 user 角色: " + registerBody);

        // 2. 注册返回的 token 可直接访问需登录态接口（验证自动登录生效）
        String meBody = mockMvc.perform(get("/api/auth/me").header(tokenName, tokenValue))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONObject meJson = JSONUtil.parseObj(meBody);
        assertTrue(meJson.getBool("success"), "查询当前用户应成功: " + meBody);
        assertEquals(email, meJson.getJSONObject("data").getStr("email"));
    }

    @Test
    @DisplayName("重复邮箱注册失败：返回 EMAIL_DUPLICATE")
    void registerShouldFailWhenEmailDuplicated() throws Exception {
        String email = "dup-" + IdUtil.fastSimpleUUID().substring(0, 8) + "@example.com";
        String reqBody = JSONUtil.createObj()
                .set("email", email)
                .set("nickname", "重复注册用户")
                .set("password", "reg123456")
                .set("confirmPassword", "reg123456")
                .toString();

        // 首次注册成功
        String firstBody = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(JSONUtil.parseObj(firstBody).getBool("success"), "首次注册应成功: " + firstBody);

        // 再次以同一邮箱注册应失败
        String secondBody = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONObject secondJson = JSONUtil.parseObj(secondBody);
        assertEquals(Boolean.FALSE, secondJson.getBool("success"));
        assertEquals(ErrorCodeEnum.EMAIL_DUPLICATE.getCode(), secondJson.getStr("code"));
    }

    @Test
    @DisplayName("两次密码不一致：参数自校验失败")
    void registerShouldFailWhenPasswordMismatch() throws Exception {
        String email = "mis-" + IdUtil.fastSimpleUUID().substring(0, 8) + "@example.com";
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.createObj()
                                .set("email", email)
                                .set("nickname", "密码不一致用户")
                                .set("password", "reg123456")
                                .set("confirmPassword", "reg654321")
                                .toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONObject json = JSONUtil.parseObj(body);
        assertEquals(Boolean.FALSE, json.getBool("success"));
        assertEquals(ErrorCodeEnum.PARAM_ERROR.getCode(), json.getStr("code"));
    }
}
