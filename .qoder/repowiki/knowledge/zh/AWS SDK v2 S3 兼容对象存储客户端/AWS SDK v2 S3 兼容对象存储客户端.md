---
kind: external_dependency
name: AWS SDK v2 S3 兼容对象存储客户端
slug: aws-sdk-v2-s3-兼容对象存储客户端
category: external_dependency
scope:
    - '**'
---

### AWS SDK v2 S3 兼容对象存储客户端
- **角色定位**：通用 S3 协议客户端，用于实现跨厂商对象存储服务适配，支持阿里云 OSS、腾讯云 COS、MinIO、七牛云 Kodo 等
- **配置方式**：通过 `app.file.s3.*` 配置项注入连接参数（endpoint、region、access-key、secret-key、bucket、path-style-access）
- **存储类型切换**：通过 `app.file.storage-type` 配置在本地磁盘（local）和 S3 存储（s3）间切换，均实现 application 层 `FileStorage` 接口
- **兼容性约束**：不同厂商的 endpoint 格式、区域命名、路径风格访问存在差异，如 MinIO 需要 `path-style-access=true`，阿里云 OSS 使用虚拟主机风格
- **文件元数据**：上传时存储类型随文件元数据落库（`sys_file.storage_type`），切换存储实现后存量文件需保证原存储仍可访问
- **验证参考**：各厂商具体 endpoint 格式和区域命名需对照官方文档确认