-- 权限码功能已移除，鉴权仅依赖角色与用户-角色关联。
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_permission;
