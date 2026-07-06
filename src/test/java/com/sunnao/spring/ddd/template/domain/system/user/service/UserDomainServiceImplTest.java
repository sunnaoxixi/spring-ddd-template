package com.sunnao.spring.ddd.template.domain.system.user.service;

import com.sunnao.spring.ddd.template.common.event.DomainEventPublisher;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.role.model.aggregate.RoleAggregate;
import com.sunnao.spring.ddd.template.domain.system.role.model.entity.RoleEntity;
import com.sunnao.spring.ddd.template.domain.system.role.repository.RoleRepository;
import com.sunnao.spring.ddd.template.domain.system.user.event.UserCreatedEvent;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.entity.UserEntity;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.ChangeUserStatusParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.CreateUserParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.DeleteUserParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.UpdateUserParam;
import com.sunnao.spring.ddd.template.domain.system.user.repository.UserRepository;
import com.sunnao.spring.ddd.template.model.system.user.UserStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户领域服务单元测试
 * <p>
 * Mockito mock 仓储与事件发布器，验证写模式标准流程（锁 → 聚合根 → 持久化）与失败分支。
 */
@ExtendWith(MockitoExtension.class)
class UserDomainServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private LevelLock levelLock;

    @InjectMocks
    private UserDomainServiceImpl userDomainService;

    @BeforeEach
    void setUp() {
        when(userRepository.buildLock(anyString())).thenReturn(levelLock);
    }

    private CreateUserParam buildCreateParam() {
        CreateUserParam param = new CreateUserParam();
        param.setEmail("new@example.com");
        param.setNickname("新用户");
        param.setPassword("plain123456");
        param.setOperatorId(1L);
        return param;
    }

    private RoleAggregate buildRole(Long roleId, String roleKey) {
        RoleEntity entity = new RoleEntity();
        entity.setId(roleId);
        entity.setRoleKey(roleKey);
        RoleAggregate aggregate = new RoleAggregate();
        aggregate.setRoleEntity(entity);
        return aggregate;
    }

    private UserAggregate buildExistingUser(Long userId) {
        UserEntity entity = new UserEntity();
        entity.setId(userId);
        entity.setEmail("exist@example.com");
        entity.setNickname("已有用户");
        entity.setStatus(UserStatusEnum.ENABLED);
        UserAggregate aggregate = new UserAggregate();
        aggregate.setUserEntity(entity);
        return aggregate;
    }

    @Test
    @DisplayName("创建用户：成功回填ID、默认授 user 角色并发布事件")
    void createUserShouldSucceedWithDefaultRole() throws Exception {
        when(levelLock.tryLock()).thenReturn(true);
        when(userRepository.queryByEmail("new@example.com")).thenReturn(null);
        when(roleRepository.queryByRoleKey("user")).thenReturn(buildRole(2L, "user"));
        doAnswer(invocation -> {
            UserAggregate aggregate = invocation.getArgument(0);
            aggregate.getUserEntity().setId(100L);
            return null;
        }).when(userRepository).save(any(UserAggregate.class));

        ResultDO<UserAggregate> result = userDomainService.createUser(buildCreateParam());

        assertTrue(result.isSuccess());
        assertEquals(100L, result.getData().getUserEntity().getId());
        assertEquals(List.of("user"), result.getData().getUserEntity().getRoles());
        verify(roleRepository).saveUserRoles(100L, List.of(2L));
        verify(domainEventPublisher).publish(any(UserCreatedEvent.class));
        verify(levelLock).unlock();
    }

    @Test
    @DisplayName("创建用户：邮箱已存在返回 EMAIL_DUPLICATE")
    void createUserShouldFailWhenEmailDuplicated() throws Exception {
        when(levelLock.tryLock()).thenReturn(true);
        when(userRepository.queryByEmail("new@example.com")).thenReturn(buildExistingUser(9L));

        ResultDO<UserAggregate> result = userDomainService.createUser(buildCreateParam());

        assertFalse(result.isSuccess());
        assertEquals("EMAIL_DUPLICATE", result.getCode());
        verify(userRepository, never()).save(any(UserAggregate.class));
        verify(levelLock).unlock();
    }

    @Test
    @DisplayName("创建用户：获取锁失败返回 LOCK_FAIL")
    void createUserShouldFailWhenLockUnavailable() throws Exception {
        when(levelLock.tryLock()).thenReturn(false);

        ResultDO<UserAggregate> result = userDomainService.createUser(buildCreateParam());

        assertFalse(result.isSuccess());
        assertEquals("LOCK_FAIL", result.getCode());
        verify(userRepository, never()).save(any(UserAggregate.class));
    }

    @Test
    @DisplayName("创建用户：指定角色存在无效ID返回 ROLE_NOT_FOUND")
    void createUserShouldFailWhenRoleInvalid() throws Exception {
        when(levelLock.tryLock()).thenReturn(true);
        when(userRepository.queryByEmail("new@example.com")).thenReturn(null);
        when(roleRepository.queryByIds(List.of(1L, 99L))).thenReturn(List.of(buildRole(1L, "admin")));

        CreateUserParam param = buildCreateParam();
        param.setRoleIds(List.of(1L, 99L));
        ResultDO<UserAggregate> result = userDomainService.createUser(param);

        assertFalse(result.isSuccess());
        assertEquals("ROLE_NOT_FOUND", result.getCode());
        verify(userRepository, never()).save(any(UserAggregate.class));
    }

    @Test
    @DisplayName("修改资料：用户不存在返回 USER_NOT_FOUND")
    void updateUserShouldFailWhenUserMissing() throws Exception {
        when(levelLock.tryLock()).thenReturn(true);
        when(userRepository.query(404L)).thenReturn(null);

        UpdateUserParam param = new UpdateUserParam();
        param.setUserId(404L);
        param.setNickname("改名");
        ResultDO<Void> result = userDomainService.updateUser(param);

        assertFalse(result.isSuccess());
        assertEquals("USER_NOT_FOUND", result.getCode());
    }

    @Test
    @DisplayName("修改资料：成功持久化变更")
    void updateUserShouldSucceed() throws Exception {
        when(levelLock.tryLock()).thenReturn(true);
        UserAggregate existing = buildExistingUser(9L);
        when(userRepository.query(9L)).thenReturn(existing);

        UpdateUserParam param = new UpdateUserParam();
        param.setUserId(9L);
        param.setNickname("改名");
        param.setOperatorId(1L);
        ResultDO<Void> result = userDomainService.updateUser(param);

        assertTrue(result.isSuccess());
        assertEquals("改名", existing.getUserEntity().getNickname());
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("变更状态：禁用成功")
    void changeUserStatusShouldDisableUser() throws Exception {
        when(levelLock.tryLock()).thenReturn(true);
        UserAggregate existing = buildExistingUser(9L);
        when(userRepository.query(9L)).thenReturn(existing);

        ChangeUserStatusParam param = new ChangeUserStatusParam();
        param.setUserId(9L);
        param.setTargetStatus(UserStatusEnum.DISABLED);
        param.setOperatorId(1L);
        ResultDO<Void> result = userDomainService.changeUserStatus(param);

        assertTrue(result.isSuccess());
        assertEquals(UserStatusEnum.DISABLED, existing.getUserEntity().getStatus());
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("变更状态：非法流转返回聚合异常错误码")
    void changeUserStatusShouldFailOnInvalidTransition() throws Exception {
        when(levelLock.tryLock()).thenReturn(true);
        when(userRepository.query(9L)).thenReturn(buildExistingUser(9L));

        ChangeUserStatusParam param = new ChangeUserStatusParam();
        param.setUserId(9L);
        param.setTargetStatus(UserStatusEnum.ENABLED);
        param.setOperatorId(1L);
        ResultDO<Void> result = userDomainService.changeUserStatus(param);

        assertFalse(result.isSuccess());
        assertEquals("STATUS_INVALID", result.getCode());
        verify(userRepository, never()).save(any(UserAggregate.class));
    }

    @Test
    @DisplayName("删除用户：成功并清空用户角色关联")
    void deleteUserShouldSucceedAndClearRoles() throws Exception {
        when(levelLock.tryLock()).thenReturn(true);
        when(userRepository.query(9L)).thenReturn(buildExistingUser(9L));

        DeleteUserParam param = new DeleteUserParam();
        param.setUserId(9L);
        param.setOperatorId(1L);
        ResultDO<Void> result = userDomainService.deleteUser(param);

        assertTrue(result.isSuccess());
        verify(userRepository).delete(9L, 1L);
        verify(roleRepository).saveUserRoles(eq(9L), eq(List.of()));
    }

    @Test
    @DisplayName("删除用户：仓储异常返回错误码不抛异常")
    void deleteUserShouldConvertRepositoryException() throws Exception {
        when(levelLock.tryLock()).thenReturn(true);
        when(userRepository.query(9L))
                .thenThrow(new com.sunnao.spring.ddd.template.common.exception.RepositoryException(
                        "DB_QUERY_ERROR", "查询用户数据异常"));

        DeleteUserParam param = new DeleteUserParam();
        param.setUserId(9L);
        ResultDO<Void> result = userDomainService.deleteUser(param);

        assertFalse(result.isSuccess());
        assertEquals("DB_QUERY_ERROR", result.getCode());
        assertNotNull(result.getMsg());
    }
}
