package com.sunnao.spring.ddd.template.domain.system.user.model.aggregate;

import com.sunnao.spring.ddd.template.common.exception.AggregateException;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.ChangeUserStatusParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.CreateUserParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.UpdateUserParam;
import com.sunnao.spring.ddd.template.model.system.user.UserStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户聚合根单元测试
 * <p>
 * 纯领域逻辑测试，不依赖 Spring 容器与外部资源。
 */
class UserAggregateTest {

    private static final String ENCODED_PASSWORD = "$2a$10$encoded";

    private CreateUserParam buildCreateParam() {
        CreateUserParam param = new CreateUserParam();
        param.setEmail("test@example.com");
        param.setNickname("测试用户");
        param.setPassword("plain123456");
        param.setOperatorId(1L);
        return param;
    }

    @Test
    @DisplayName("创建用户：合法参数默认启用状态")
    void createShouldInitEnabledUser() throws AggregateException {
        UserAggregate aggregate = UserAggregate.create(buildCreateParam(), ENCODED_PASSWORD);

        assertNotNull(aggregate.getUserEntity());
        assertEquals("test@example.com", aggregate.getUserEntity().getEmail());
        assertEquals("测试用户", aggregate.getUserEntity().getNickname());
        assertEquals(ENCODED_PASSWORD, aggregate.getUserEntity().getPassword());
        assertEquals(UserStatusEnum.ENABLED, aggregate.getUserEntity().getStatus());
        assertEquals(1L, aggregate.getUserEntity().getCreateBy());
    }

    @Test
    @DisplayName("创建用户：邮箱为空抛出聚合异常")
    void createShouldRejectBlankEmail() {
        CreateUserParam param = buildCreateParam();
        param.setEmail(" ");

        AggregateException e = assertThrows(AggregateException.class,
                () -> UserAggregate.create(param, ENCODED_PASSWORD));
        assertEquals("PARAM_ERROR", e.getCode());
    }

    @Test
    @DisplayName("创建用户：加密密码为空抛出聚合异常")
    void createShouldRejectBlankPassword() {
        AggregateException e = assertThrows(AggregateException.class,
                () -> UserAggregate.create(buildCreateParam(), " "));
        assertEquals("PARAM_ERROR", e.getCode());
    }

    @Test
    @DisplayName("修改资料：更新昵称与头像并记录操作人")
    void updateProfileShouldChangeNicknameAndAvatar() throws AggregateException {
        UserAggregate aggregate = UserAggregate.create(buildCreateParam(), ENCODED_PASSWORD);

        UpdateUserParam param = new UpdateUserParam();
        param.setNickname("新昵称");
        param.setAvatar("https://example.com/avatar.png");
        param.setOperatorId(2L);
        aggregate.updateProfile(param);

        assertEquals("新昵称", aggregate.getUserEntity().getNickname());
        assertEquals("https://example.com/avatar.png", aggregate.getUserEntity().getAvatar());
        assertEquals(2L, aggregate.getUserEntity().getUpdateBy());
    }

    @Test
    @DisplayName("修改资料：昵称与头像同时为空抛出聚合异常")
    void updateProfileShouldRejectAllBlank() throws AggregateException {
        UserAggregate aggregate = UserAggregate.create(buildCreateParam(), ENCODED_PASSWORD);

        UpdateUserParam param = new UpdateUserParam();
        param.setOperatorId(2L);

        AggregateException e = assertThrows(AggregateException.class,
                () -> aggregate.updateProfile(param));
        assertEquals("PARAM_ERROR", e.getCode());
    }

    @Test
    @DisplayName("变更状态：启用用户可被禁用")
    void changeStatusShouldDisableEnabledUser() throws AggregateException {
        UserAggregate aggregate = UserAggregate.create(buildCreateParam(), ENCODED_PASSWORD);

        ChangeUserStatusParam param = new ChangeUserStatusParam();
        param.setTargetStatus(UserStatusEnum.DISABLED);
        param.setOperatorId(2L);
        aggregate.changeStatus(param);

        assertEquals(UserStatusEnum.DISABLED, aggregate.getUserEntity().getStatus());
    }

    @Test
    @DisplayName("变更状态：重复启用抛出聚合异常")
    void changeStatusShouldRejectEnablingEnabledUser() throws AggregateException {
        UserAggregate aggregate = UserAggregate.create(buildCreateParam(), ENCODED_PASSWORD);

        ChangeUserStatusParam param = new ChangeUserStatusParam();
        param.setTargetStatus(UserStatusEnum.ENABLED);
        param.setOperatorId(2L);

        AggregateException e = assertThrows(AggregateException.class,
                () -> aggregate.changeStatus(param));
        assertEquals("STATUS_INVALID", e.getCode());
    }

    @Test
    @DisplayName("重置密码：更新密文并记录操作人")
    void resetPasswordShouldChangePassword() throws AggregateException {
        UserAggregate aggregate = UserAggregate.create(buildCreateParam(), ENCODED_PASSWORD);

        aggregate.resetPassword("$2a$10$newEncoded", 3L);

        assertEquals("$2a$10$newEncoded", aggregate.getUserEntity().getPassword());
        assertEquals(3L, aggregate.getUserEntity().getUpdateBy());
    }

    @Test
    @DisplayName("重置密码：密文为空抛出聚合异常")
    void resetPasswordShouldRejectBlankPassword() throws AggregateException {
        UserAggregate aggregate = UserAggregate.create(buildCreateParam(), ENCODED_PASSWORD);

        AggregateException e = assertThrows(AggregateException.class,
                () -> aggregate.resetPassword(" ", 3L));
        assertEquals("PARAM_ERROR", e.getCode());
    }
}
