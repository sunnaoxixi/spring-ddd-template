package com.sunnao.spring.ddd.template.common.config;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import com.sunnao.spring.ddd.template.common.context.CurrentUserContext;
import com.sunnao.spring.ddd.template.common.model.BasePO;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Flex 全局配置
 * <p>
 * 为所有继承 BasePO 的持久化对象注册审计字段自动填充监听器：
 * 插入时填充 createAt/updateAt/createBy/updateBy，更新时填充 updateAt/updateBy；
 * 操作人取自 CurrentUserContext，已显式赋值的操作人字段不覆盖。
 */
@Configuration
public class MybatisFlexConfigure implements MyBatisFlexCustomizer {

    @Override
    public void customize(FlexGlobalConfig globalConfig) {
        globalConfig.registerInsertListener(new AuditInsertListener(), BasePO.class);
        globalConfig.registerUpdateListener(new AuditUpdateListener(), BasePO.class);
    }

    /**
     * 插入审计监听器
     */
    static class AuditInsertListener implements InsertListener {

        @Override
        public void onInsert(Object entity) {
            if (!(entity instanceof BasePO po)) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            if (po.getCreateAt() == null) {
                po.setCreateAt(now);
            }
            if (po.getUpdateAt() == null) {
                po.setUpdateAt(now);
            }
            Long userId = CurrentUserContext.getUserId();
            if (po.getCreateBy() == null) {
                po.setCreateBy(userId);
            }
            if (po.getUpdateBy() == null) {
                po.setUpdateBy(userId);
            }
        }
    }

    /**
     * 更新审计监听器
     */
    static class AuditUpdateListener implements UpdateListener {

        @Override
        public void onUpdate(Object entity) {
            if (!(entity instanceof BasePO po)) {
                return;
            }
            po.setUpdateAt(LocalDateTime.now());
            if (po.getUpdateBy() == null) {
                po.setUpdateBy(CurrentUserContext.getUserId());
            }
        }
    }
}
