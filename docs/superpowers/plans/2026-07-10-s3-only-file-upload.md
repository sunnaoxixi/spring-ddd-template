# S3-Only File Upload Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Simplify file management so every upload, download, and delete uses the configured S3-compatible object storage and no local file storage path remains.

**Architecture:** Keep `FileStorage` as the Application-owned Output Adaptor interface so AWS SDK details remain isolated in the Adaptor layer. Make `S3FileStorage` the only implementation, remove multi-storage routing metadata from the domain and persistence models, and use a Flyway migration to remove the obsolete database column without rewriting migration history.

**Tech Stack:** Java 25, Spring Boot 4.1, AWS SDK v2 S3, Flyway, JUnit 5, Mockito, Maven

## Global Constraints

- Production code stays under the existing six-layer DDD package structure.
- Application code must not depend directly on AWS SDK or other storage middleware.
- Request validation remains in DTO `check()` methods and errors remain wrapped in `ResultDO`.
- S3 connection values come from `app.file.s3.*` and environment variables; credentials are never committed.
- Existing Flyway migrations remain immutable; schema cleanup is introduced as `V8__drop_file_storage_type.sql`.

---

### Task 1: Prove File Metadata Is Storage-Agnostic

**Files:**
- Create: `src/test/java/com/sunnao/spring/ddd/template/domain/system/file/model/aggregate/FileAggregateTest.java`
- Modify: `src/test/java/com/sunnao/spring/ddd/template/adaptor/system/file/output/S3FileStorageTest.java`

**Interfaces:**
- Consumes: `FileAggregate.create(CreateFileParam)` and `S3FileStorage.store/read/delete`.
- Produces: A regression test showing `CreateFileParam` no longer needs a storage type and S3 remains the only tested physical storage behavior.

- [ ] **Step 1: Write the failing aggregate test**

```java
@Test
void createShouldNotRequireStorageType() {
    CreateFileParam param = new CreateFileParam();
    param.setOriginalName("report.pdf");
    param.setPath("2026/07/10/report.pdf");
    param.setSize(128L);
    param.setContentType("application/pdf");
    param.setOperatorId(1L);

    FileAggregate aggregate = FileAggregate.create(param);

    assertEquals("report.pdf", aggregate.getFileEntity().getOriginalName());
    assertEquals("2026/07/10/report.pdf", aggregate.getFileEntity().getPath());
}
```

- [ ] **Step 2: Run the aggregate test and verify it fails**

Run: `./mvnw -Dtest=FileAggregateTest test`

Expected: FAIL because the current aggregate rejects a missing storage type.

- [ ] **Step 3: Remove the obsolete S3 storage-type assertion**

Delete `S3FileStorageTest.getStorageType()` and its `FileStorageTypeEnum` import. The remaining tests continue to cover S3 upload validation, object metadata, reads, deletes, failures, and required configuration.

- [ ] **Step 4: Run the S3 unit test**

Run: `./mvnw -Dtest=S3FileStorageTest test`

Expected: PASS.

### Task 2: Make S3 the Only Storage Implementation

**Files:**
- Delete: `src/main/java/com/sunnao/spring/ddd/template/adaptor/system/file/output/LocalFileStorage.java`
- Delete: `src/main/java/com/sunnao/spring/ddd/template/model/system/file/FileStorageTypeEnum.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/application/system/file/FileStorage.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/adaptor/system/file/output/S3FileStorage.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/adaptor/system/file/output/StoragePathGenerator.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/application/system/file/scenario/FileAppServiceImpl.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/application/system/file/assembler/FileAssembler.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/domain/system/file/model/param/CreateFileParam.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/domain/system/file/model/aggregate/FileAggregate.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/domain/system/file/model/entity/FileEntity.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/file/mysql/po/FilePO.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/file/converter/FileConverter.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/client/system/file/res/FileDTO.java`
- Modify: `src/main/java/com/sunnao/spring/ddd/template/common/result/ErrorCodeEnum.java`

**Interfaces:**
- Consumes: `FileStorage.store(String, String, byte[])`, `read(String)`, and `delete(String)`.
- Produces: One unconditional `S3FileStorage` bean and storage-agnostic file metadata containing name, S3 object key, size, MIME type, and audit fields.

- [ ] **Step 1: Reduce the Application-owned storage interface**

```java
public interface FileStorage {
    ResultDO<String> store(String originalName, String contentType, byte[] content);
    ResultDO<byte[]> read(String path);
    ResultDO<Void> delete(String path);
}
```

Remove `getStorageType()` because there is no implementation selection or type metadata after this change.

- [ ] **Step 2: Make S3 unconditional and remove local storage**

Delete `LocalFileStorage`. Remove `@ConditionalOnProperty` and `getStorageType()` from `S3FileStorage`; keep the existing `@PostConstruct` validation so a missing endpoint, region, access key, secret key, or bucket stops application startup with a clear error. Update `StoragePathGenerator` documentation to describe S3 object keys only.

- [ ] **Step 3: Remove storage type from application orchestration**

Change the assembler signature to:

```java
CreateFileParam toCreateParam(UploadFileRequestDTO requestDTO, String path, Long operatorId)
```

Call it from `FileAppServiceImpl` without `fileStorage.getStorageType()`.

- [ ] **Step 4: Remove storage type from domain metadata**

Delete `CreateFileParam.storageType`, `FileEntity.storageType`, and the corresponding `FileAggregate.create` validation and conversion. Delete `FileStorageTypeEnum` after its final references are gone.

- [ ] **Step 5: Remove storage type from persistence and response mapping**

Delete `FilePO.storageType`, remove MapStruct enum conversion methods and mappings from `FileConverter`, and delete `FileDTO.storageType` plus its `FileAssembler` assignment. Remove the now-unused local path validation error code `FILE_PATH_INVALID`.

- [ ] **Step 6: Run focused tests**

Run: `./mvnw -Dtest=FileAggregateTest,S3FileStorageTest test`

Expected: PASS with no reference to local storage or `FileStorageTypeEnum`.

### Task 3: Align Schema, Configuration, and Documentation

**Files:**
- Create: `src/main/resources/db/migration/V8__drop_file_storage_type.sql`
- Modify: `src/main/resources/application.yaml`
- Modify: `.env.example`
- Modify: `.env.prod.example`
- Modify: `README.md`
- Modify: `docker-compose.prod.yaml`
- Modify: `docs/deployment/docker-compose.md`
- Modify: `docs/frontend-development-guide.md`

**Interfaces:**
- Consumes: Existing `sys_file` schema created by V5 and S3 environment variables from `.env.example` / `.env.prod.example`.
- Produces: A schema without `storage_type`, configuration without a local/S3 selector, and setup documentation stating that S3 configuration is required.

- [ ] **Step 1: Add the Flyway cleanup migration**

```sql
ALTER TABLE sys_file
    DROP COLUMN storage_type;
```

- [ ] **Step 2: Remove local and selector configuration**

Keep this shape under `app.file`:

```yaml
file:
  max-size: 10MB
  s3:
    endpoint: ${S3_ENDPOINT:}
    region: ${S3_REGION:us-east-1}
    access-key: ${S3_ACCESS_KEY:}
    secret-key: ${S3_SECRET_KEY:}
    bucket: ${S3_BUCKET:}
    path-style-access: ${S3_PATH_STYLE_ACCESS:false}
```

- [ ] **Step 3: Make production S3 configuration explicitly required**

Mark S3 configuration as required in `.env.example` and replace the optional/local fallback comment in `.env.prod.example` with a statement that all S3 values must be configured before startup.

- [ ] **Step 4: Update README behavior and migration inventory**

Document `S3FileStorage` as the sole Output Adaptor, remove the local/S3 switching table, state that missing S3 configuration fails startup, and include V8 in the migration list. Remove the production Compose local upload volume, its backup instructions, and the obsolete frontend error-code reference.

- [ ] **Step 5: Document the upgrade requirement**

State that existing local files must be copied to the configured S3 bucket with their current `sys_file.path` values as object keys before deploying this change.

- [ ] **Step 6: Verify obsolete branches are absent**

Run: `rg -n "LocalFileStorage|FileStorageTypeEnum|storage-type|app\\.file\\.local|storage_type" src README.md .env.prod.example`

Expected: No matches except historical references inside older Flyway migration V5 if the search includes it.

- [ ] **Step 7: Run the complete test suite**

Run: `./mvnw test`

Expected: All unit tests pass; environment-gated PostgreSQL/Redis integration tests skip when their required variables are absent.
