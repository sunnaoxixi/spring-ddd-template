package com.sunnao.spring.ddd.template.integration;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户管理 CRUD 集成测试（管理员创建 → 查询 → 修改 → 禁用 → 删除全流程）
 * <p>
 * 依赖云上 PostgreSQL / Redis（application-test.yaml 环境变量占位，Flyway 自动建表），
 * 未配置 TEST_PG_URL / TEST_REDIS_HOST 时自动跳过。
 */
@EnabledIfEnvironmentVariable(named = "TEST_PG_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "TEST_REDIS_HOST", matches = ".+")
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class UserCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String tokenName;

    private String tokenValue;

    @BeforeEach
    void loginAsAdmin() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.createObj()
                                .set("email", "admin@example.com")
                                .set("password", "admin123456")
                                .toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONObject json = JSONUtil.parseObj(body);
        assertTrue(json.getBool("success"), "管理员登录应成功: " + body);
        tokenName = json.getJSONObject("data").getStr("tokenName");
        tokenValue = json.getJSONObject("data").getStr("tokenValue");
    }

    @Test
    @DisplayName("创建 → 详情 → 改资料 → 禁用 → 删除 → 确认不存在")
    void userCrudFlow() throws Exception {
        String email = "it-" + IdUtil.fastSimpleUUID().substring(0, 8) + "@example.com";

        // 1. 创建用户（未指定角色，默认授 user 角色）
        String createBody = mockMvc.perform(post("/api/system/users")
                        .header(tokenName, tokenValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.createObj()
                                .set("email", email)
                                .set("nickname", "集成测试用户")
                                .set("password", "test123456")
                                .toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONObject createJson = JSONUtil.parseObj(createBody);
        assertTrue(createJson.getBool("success"), "创建用户应成功: " + createBody);
        Long userId = createJson.getJSONObject("data").getLong("userId");
        assertNotNull(userId);

        // 2. 查询详情（含默认 user 角色）
        String detailBody = mockMvc.perform(get("/api/system/users/" + userId)
                        .header(tokenName, tokenValue))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONObject detailJson = JSONUtil.parseObj(detailBody);
        assertTrue(detailJson.getBool("success"), "查询详情应成功: " + detailBody);
        JSONObject user = detailJson.getJSONObject("data").getJSONObject("user");
        assertEquals(email, user.getStr("email"));
        assertEquals("集成测试用户", user.getStr("nickname"));
        assertTrue(user.getJSONArray("roles").contains("user"), "应默认授予 user 角色: " + detailBody);

        // 3. 修改资料
        String updateBody = mockMvc.perform(put("/api/system/users/" + userId)
                        .header(tokenName, tokenValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.createObj().set("nickname", "改名用户").toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(JSONUtil.parseObj(updateBody).getBool("success"), "修改资料应成功: " + updateBody);

        // 4. 禁用用户
        String disableBody = mockMvc.perform(put("/api/system/users/" + userId + "/status")
                        .header(tokenName, tokenValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.createObj().set("status", 0).toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(JSONUtil.parseObj(disableBody).getBool("success"), "禁用用户应成功: " + disableBody);

        // 5. 删除用户（逻辑删除）
        String deleteBody = mockMvc.perform(delete("/api/system/users/" + userId)
                        .header(tokenName, tokenValue))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(JSONUtil.parseObj(deleteBody).getBool("success"), "删除用户应成功: " + deleteBody);

        // 6. 删除后详情返回 USER_NOT_FOUND
        String afterDeleteBody = mockMvc.perform(get("/api/system/users/" + userId)
                        .header(tokenName, tokenValue))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONObject afterDeleteJson = JSONUtil.parseObj(afterDeleteBody);
        assertEquals(Boolean.FALSE, afterDeleteJson.getBool("success"));
        assertEquals(ErrorCodeEnum.USER_NOT_FOUND.getCode(), afterDeleteJson.getStr("code"));
    }

    @Test
    @DisplayName("分页查询：能查到种子管理员")
    void queryUserPageShouldContainAdmin() throws Exception {
        String body = mockMvc.perform(get("/api/system/users/page")
                        .header(tokenName, tokenValue)
                        .param("email", "admin@example.com"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONObject json = JSONUtil.parseObj(body);
        assertTrue(json.getBool("success"), "分页查询应成功: " + body);
        assertTrue(json.getJSONObject("data").getLong("total") >= 1, "应至少存在种子管理员: " + body);
    }
}
