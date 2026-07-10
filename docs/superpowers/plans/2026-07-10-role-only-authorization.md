# Role-Only Authorization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove permission-code authorization and permission assignment features so protected APIs authorize users only by role.

**Architecture:** Keep `sys_role` and `sys_user_role` as the authorization source, and keep Sa-Token's `StpInterface` role lookup. Remove permission DTOs, domain models, repository methods, mappers, endpoints, and role-detail fields. Preserve the existing built-in access matrix by mapping management APIs to `admin`, dictionary reads and all file APIs to `admin OR user`, and dictionary writes to `admin`.

**Tech Stack:** Java 25, Spring Boot 4.1, Sa-Token 1.45, MyBatis-Flex, Flyway, JUnit 5, Maven

## Global Constraints

- Follow `docs/rule/ddd/DDD.md` and all six layer-specific rules under `docs/rule/ddd/`.
- Preserve the existing `ResultDO` error flow and Assembler/Converter boundaries.
- Do not edit the already-applied `V2__init_rbac.sql`; add `V7__drop_permission_tables.sql` so existing databases migrate without a Flyway checksum mismatch.
- Keep `admin` access to every protected system endpoint.
- Keep `user` access to dictionary reads and file upload/download/delete/query, matching the old seed permission matrix.
- Remove `PUT /api/system/roles/{id}/permissions` and `GET /api/system/roles/permissions`.
- Remove `permKeys` from the role-detail response.

---

### Task 1: Replace Permission Annotations With Role Annotations

**Files:**
- Modify: `src/main/java/com/sunnao/spring/ddd/template/adaptor/system/user/input/UserController.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/adaptor/system/role/input/RoleController.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/adaptor/system/dict/input/DictController.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/adaptor/system/file/input/FileController.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/adaptor/system/log/input/LogController.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/adaptor/common/GlobalExceptionHandler.java`
- Test: `src/test/java/com/sunnao/spring/ddd/template/adaptor/system/RoleAuthorizationTest.java`

**Interfaces:**
- Consumes: Sa-Token `@SaCheckRole` and `SaMode.OR`.
- Produces: Controller authorization metadata with no `@SaCheckPermission` usage.

- [x] **Step 1: Add a reflection-based authorization test**

```java
class RoleAuthorizationTest {

    @Test
    void managementControllersShouldRequireAdminRole() {
        assertRole(UserController.class, new String[]{"admin"}, SaMode.AND);
        assertRole(RoleController.class, new String[]{"admin"}, SaMode.AND);
        assertRole(LogController.class, new String[]{"admin"}, SaMode.AND);
    }

    @Test
    void fileControllerShouldAllowAdminOrUserRole() {
        assertRole(FileController.class, new String[]{"admin", "user"}, SaMode.OR);
    }
}
```

- [x] **Step 2: Run the focused test and confirm it fails before implementation**

Run: `./mvnw -Dtest=RoleAuthorizationTest test`

Expected: FAIL because the controllers still declare `@SaCheckPermission`.

- [x] **Step 3: Apply role annotations**

```java
@SaCheckRole("admin")
public class UserController {
}

@SaCheckRole(value = {"admin", "user"}, mode = SaMode.OR)
public class FileController {
}
```

For `DictController`, annotate write methods with `@SaCheckRole("admin")` and read methods with `@SaCheckRole(value = {"admin", "user"}, mode = SaMode.OR)`.

- [x] **Step 4: Remove the unused permission exception handler**

Delete the `NotPermissionException` import and `handleNotPermission` method; retain `NotRoleException` mapping to `NO_PERMISSION`.

- [x] **Step 5: Run the focused test**

Run: `./mvnw -Dtest=RoleAuthorizationTest test`

Expected: PASS.

### Task 2: Remove Permission APIs And Domain Contracts

**Files:**
- Modify: `src/main/java/com/sunnao/spring/ddd/template/adaptor/system/role/input/RoleController.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/client/system/role/RoleAppService.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/client/system/role/RoleQueryAppService.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/client/system/role/res/GetRoleDetailResponseDTO.java`
- Delete: `src/main/java/com/sunnao/spring/ddd/template/client/system/role/req/AssignPermissionRequestDTO.java`
- Delete: `src/main/java/com/sunnao/spring/ddd/template/client/system/role/res/AssignPermissionResponseDTO.java`
- Delete: `src/main/java/com/sunnao/spring/ddd/template/client/system/role/res/PermissionDTO.java`
- Delete: `src/main/java/com/sunnao/spring/ddd/template/client/system/role/res/QueryPermissionListResponseDTO.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/application/system/role/assembler/RoleAssembler.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/application/system/role/scenario/RoleAppServiceImpl.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/application/system/role/scenario/RoleQueryAppServiceImpl.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/domain/system/role/model/aggregate/RoleAggregate.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/domain/system/role/repository/RoleRepository.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/domain/system/role/service/RoleDomainService.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/domain/system/role/service/RoleDomainServiceImpl.java`
- Delete: `src/main/java/com/sunnao/spring/ddd/template/domain/system/role/model/entity/PermissionEntity.java`
- Delete: `src/main/java/com/sunnao/spring/ddd/template/domain/system/role/model/param/AssignPermissionParam.java`
- Delete: `src/main/java/com/sunnao/spring/ddd/template/domain/system/role/model/value/PermissionKeysValue.java`

**Interfaces:**
- Consumes: Existing role CRUD and user-role assignment contracts.
- Produces: Role CRUD/detail/page and user-role assignment contracts without permission fields or methods.

- [x] **Step 1: Remove role-permission HTTP methods and client service methods**

`RoleAppService` retains `createRole`, `updateRole`, `deleteRole`, and `assignUserRoles`. `RoleQueryAppService` retains `getRoleDetail` and `queryRolePage`.

- [x] **Step 2: Simplify role detail to role data only**

```java
public class GetRoleDetailResponseDTO extends BaseDto {
    private RoleDTO role;
}
```

- [x] **Step 3: Remove application-layer permission orchestration and mappings**

`RoleAssembler.toGetRoleDetailResponseDTO` must only call `responseDTO.setRole(toRoleDTO(aggregate))`.

- [x] **Step 4: Remove permission state and behavior from the domain**

`RoleAggregate` retains only `RoleEntity roleEntity`; `RoleDomainService` no longer declares `assignPermissions`; `RoleRepository` no longer exposes permission queries or role-permission persistence.

- [x] **Step 5: Compile the main sources**

Run: `./mvnw -DskipTests compile`

Expected: PASS after all removed contracts have no callers.

### Task 3: Remove Permission Persistence And Migrate Existing Databases

**Files:**
- Modify: `src/main/java/com/sunnao/spring/ddd/template/infrastructure/auth/StpInterfaceImpl.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/role/converter/RoleConverter.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/role/repository/RoleRepositoryImpl.java`
- Delete: `src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/role/mysql/mapper/PermissionMapper.java`
- Delete: `src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/role/mysql/mapper/RolePermissionMapper.java`
- Delete: `src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/role/mysql/po/PermissionPO.java`
- Delete: `src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/role/mysql/po/RolePermissionPO.java`
- Create: `src/main/resources/db/migration/V7__drop_permission_tables.sql`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/common/result/ErrorCodeEnum.java`

**Interfaces:**
- Consumes: Sa-Token `StpInterface`, which still requires a `getPermissionList` method.
- Produces: Role lookup backed only by `sys_role` and `sys_user_role`; permission lookup always returns an empty immutable list.

- [x] **Step 1: Make Sa-Token permission lookup inert**

```java
@Override
public List<String> getPermissionList(Object loginId, String loginType) {
    return Collections.emptyList();
}
```

- [x] **Step 2: Remove permission persistence dependencies and queries**

Delete permission mapper fields, role-permission cleanup, permission query methods, `queryPermKeysByUserId`, and role-detail permission filling. Keep user-role persistence and enabled-role queries unchanged.

- [x] **Step 3: Add the forward-only Flyway cleanup migration**

```sql
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_permission;
```

- [x] **Step 4: Remove `PERMISSION_NOT_FOUND`**

Keep `NO_PERMISSION` because it is the generic 403 code used by role authorization.

- [x] **Step 5: Compile the application**

Run: `./mvnw -DskipTests compile`

Expected: PASS with MapStruct-generated sources rebuilt.

### Task 4: Align Documentation And Verify The Repository

**Files:**
- Modify: `README.md`
- Modify: `docs/frontend-development-guide.md`
- Modify: `docs/handover/spring-ddd-template-fixes-handover.md`

**Interfaces:**
- Consumes: Final controller access matrix and remaining role APIs.
- Produces: Documentation that describes role-only authorization and no permission assignment workflow.

- [x] **Step 1: Update module and development documentation**

Document `admin`-only user/role/log/online APIs, `admin OR user` file APIs and dictionary reads, and `admin` dictionary writes. Remove permission-list, permission-assignment, permission error-code, and `permKeys` references.

- [x] **Step 2: Run a source scan**

Run: `rg -n "SaCheckPermission|AssignPermission|PermissionDTO|PermissionEntity|PermissionPO|RolePermission|permKeys|system:[a-z]+:(read|write)" src/main src/test README.md docs/frontend-development-guide.md`

Expected: no matches. Historical permission DDL remains only in the immutable `V2__init_rbac.sql`; `V7__drop_permission_tables.sql` removes it from deployed schemas.

- [x] **Step 3: Run all tests**

Run: `./mvnw test`

Expected: PASS; environment-dependent integration tests may be skipped when PostgreSQL or Redis variables are absent.

- [x] **Step 4: Build the executable artifact**

Run: `./mvnw clean package`

Expected: PASS and an executable JAR under `target/`.

- [x] **Step 5: Review the final diff**

Run: `git diff --check && git status --short && git diff --stat`

Expected: no whitespace errors; only role authorization, permission removal, migration, test, plan, and documentation files are changed.
