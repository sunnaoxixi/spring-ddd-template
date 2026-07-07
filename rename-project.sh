#!/usr/bin/env bash
#
# 一键改包脚本：将模板项目重命名为你自己的项目
#
# 用法:
#   ./rename-project.sh <new-groupId> <new-artifactId> [new-package]
#
# 示例:
#   ./rename-project.sh com.acme order-center
#     -> groupId    = com.acme
#     -> artifactId = order-center
#     -> 包名默认    = com.acme.order.center (groupId + artifactId 横线转点)
#
#   ./rename-project.sh com.acme order-center com.acme.ordercenter
#     -> 显式指定包名 = com.acme.ordercenter
#
# 不带参数运行则进入交互模式。
#
set -euo pipefail

# ---------- 模板当前值（如模板自身变更请同步维护） ----------
OLD_GROUP="com.sunnao"
OLD_ARTIFACT="spring-ddd-template"
OLD_PACKAGE="com.sunnao.spring.ddd.template"
OLD_MAIN_CLASS="SpringDddTemplateApplication"

SCRIPT_NAME="$(basename "$0")"
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

# ---------- 输入 ----------
if [ $# -eq 0 ]; then
    printf "新 groupId   (如 com.acme): "
    read -r NEW_GROUP
    printf "新 artifactId (如 order-center): "
    read -r NEW_ARTIFACT
    printf "新包名 [回车默认 groupId + artifactId 横线转点]: "
    read -r NEW_PACKAGE
elif [ $# -ge 2 ]; then
    NEW_GROUP="$1"
    NEW_ARTIFACT="$2"
    NEW_PACKAGE="${3:-}"
else
    echo "用法: ./$SCRIPT_NAME <new-groupId> <new-artifactId> [new-package]" >&2
    exit 1
fi

# ---------- 校验 ----------
if ! echo "$NEW_GROUP" | grep -Eq '^[a-z][a-z0-9]*(\.[a-z][a-z0-9_]*)*$'; then
    echo "错误: groupId 非法: $NEW_GROUP (要求小写字母开头的点分包名，如 com.acme)" >&2
    exit 1
fi
if ! echo "$NEW_ARTIFACT" | grep -Eq '^[a-z][a-z0-9]*(-[a-z0-9]+)*$'; then
    echo "错误: artifactId 非法: $NEW_ARTIFACT (要求小写字母/数字/中划线，如 order-center)" >&2
    exit 1
fi
if [ -z "$NEW_PACKAGE" ]; then
    NEW_PACKAGE="${NEW_GROUP}.$(echo "$NEW_ARTIFACT" | tr '-' '.')"
fi
if ! echo "$NEW_PACKAGE" | grep -Eq '^[a-z][a-z0-9]*(\.[a-z][a-z0-9_]*)+$'; then
    echo "错误: 包名非法: $NEW_PACKAGE (要求至少两级的点分包名，如 com.acme.ordercenter)" >&2
    exit 1
fi

# ---------- 派生值 ----------
NEW_SNAKE="$(echo "$NEW_ARTIFACT" | tr '-' '_')"       # 数据库名等: order_center
OLD_SNAKE="$(echo "$OLD_ARTIFACT" | tr '-' '_')"
NEW_PASCAL="$(echo "$NEW_ARTIFACT" | awk -F- '{for(i=1;i<=NF;i++) printf "%s%s", toupper(substr($i,1,1)), substr($i,2)}')"
NEW_MAIN_CLASS="${NEW_PASCAL}Application"              # 启动类: OrderCenterApplication
OLD_PACKAGE_PATH="$(echo "$OLD_PACKAGE" | tr '.' '/')"
NEW_PACKAGE_PATH="$(echo "$NEW_PACKAGE" | tr '.' '/')"

echo ""
echo "即将执行以下重命名:"
echo "  groupId    : $OLD_GROUP  ->  $NEW_GROUP"
echo "  artifactId : $OLD_ARTIFACT  ->  $NEW_ARTIFACT"
echo "  包名        : $OLD_PACKAGE  ->  $NEW_PACKAGE"
echo "  启动类      : $OLD_MAIN_CLASS  ->  $NEW_MAIN_CLASS"
echo "  数据库名等  : $OLD_SNAKE  ->  $NEW_SNAKE"
echo ""
printf "确认执行? [y/N] "
read -r CONFIRM
case "$CONFIRM" in
    y|Y|yes|YES) ;;
    *) echo "已取消。"; exit 0 ;;
esac

# ---------- 全局文本替换 ----------
# 在所有文本文件中替换固定字符串（跳过 .git/target/.idea 及本脚本）
replace_all() {
    local old="$1" new="$2"
    grep -rIlF \
        --exclude-dir=.git --exclude-dir=target --exclude-dir=.idea \
        --exclude-dir=node_modules --exclude="$SCRIPT_NAME" \
        -- "$old" . 2>/dev/null | while IFS= read -r f; do
        OLD="$old" NEW="$new" perl -pi -e 's/\Q$ENV{OLD}\E/$ENV{NEW}/g' "$f"
    done
}

echo ""
echo "[1/3] 替换文件内容..."
replace_all "$OLD_PACKAGE"      "$NEW_PACKAGE"        # Java 包名（含 pom/README 引用）
replace_all "$OLD_PACKAGE_PATH" "$NEW_PACKAGE_PATH"   # 包路径（README 项目结构等）
replace_all "$OLD_MAIN_CLASS"   "$NEW_MAIN_CLASS"     # 启动类名（含 Tests 后缀引用）
replace_all "$OLD_SNAKE"        "$NEW_SNAKE"          # 数据库名（application-dev/docker-compose）
replace_all "$OLD_ARTIFACT"     "$NEW_ARTIFACT"       # artifactId/应用名/容器名
replace_all "$OLD_GROUP"        "$NEW_GROUP"          # pom groupId（此时仅剩独立出现处）

# ---------- 移动源码目录 ----------
echo "[2/3] 移动源码目录..."
move_package_dir() {
    local src_root="$1"   # 如 src/main/java
    local old_dir="$src_root/$OLD_PACKAGE_PATH"
    local new_dir="$src_root/$NEW_PACKAGE_PATH"
    [ -d "$old_dir" ] || return 0
    local tmp_dir
    tmp_dir="$(mktemp -d "$ROOT_DIR/.rename-tmp.XXXXXX")"
    mv "$old_dir" "$tmp_dir/pkg"
    # 清理旧包遗留的空目录
    find "$src_root" -type d -empty -delete 2>/dev/null || true
    mkdir -p "$(dirname "$new_dir")"
    mv "$tmp_dir/pkg" "$new_dir"
    rmdir "$tmp_dir"
    echo "  $old_dir -> $new_dir"
}
move_package_dir "src/main/java"
move_package_dir "src/test/java"

# ---------- 重命名启动类文件 ----------
echo "[3/3] 重命名启动类文件..."
find src -type f -name "${OLD_MAIN_CLASS}*.java" 2>/dev/null | while IFS= read -r f; do
    new_f="$(dirname "$f")/$(basename "$f" | sed "s/^${OLD_MAIN_CLASS}/${NEW_MAIN_CLASS}/")"
    mv "$f" "$new_f"
    echo "  $f -> $new_f"
done

# ---------- 收尾 ----------
echo ""
echo "改包完成！后续建议:"
echo "  1. 数据库名已变为 ${NEW_SNAKE}，如本地已有旧容器请重建: docker compose down -v && docker compose up -d"
echo "  2. 验证构建: ./mvnw clean test"
echo "  3. 按需修改 pom.xml 中的 <description> 与 README.md 中的项目介绍"
echo ""
printf "是否删除本脚本 ($SCRIPT_NAME)? [y/N] "
read -r DEL
case "$DEL" in
    y|Y|yes|YES) rm -f "$ROOT_DIR/$SCRIPT_NAME"; echo "已删除 $SCRIPT_NAME。" ;;
    *) echo "已保留 $SCRIPT_NAME。" ;;
esac
