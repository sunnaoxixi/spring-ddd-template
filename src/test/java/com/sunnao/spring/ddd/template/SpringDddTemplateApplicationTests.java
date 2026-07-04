package com.sunnao.spring.ddd.template;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring 上下文冒烟测试
 * <p>
 * 需要真实 PostgreSQL / Redis（Flyway 启动即迁移），
 * 未配置 TEST_PG_URL / TEST_REDIS_HOST 环境变量时自动跳过。
 */
@EnabledIfEnvironmentVariable(named = "TEST_PG_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "TEST_REDIS_HOST", matches = ".+")
@ActiveProfiles("test")
@SpringBootTest
class SpringDddTemplateApplicationTests {

    @Test
    void contextLoads() {
    }

}
