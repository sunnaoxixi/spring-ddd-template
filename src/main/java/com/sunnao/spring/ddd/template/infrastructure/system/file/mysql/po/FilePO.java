package com.sunnao.spring.ddd.template.infrastructure.system.file.mysql.po;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sunnao.spring.ddd.template.common.model.BasePO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件持久化对象
 * 与 sys_file 表一一对应，仅用于 Infrastructure 层内部；
 * 审计字段继承自 BasePO，由全局监听器自动填充
 */
@Getter
@Setter
@ToString
@Table("sys_file")
public class FilePO extends BasePO {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储路径（相对存储根目录）
     */
    private String path;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 文件 MIME 类型
     */
    private String contentType;

    /**
     * 逻辑删除：0-正常，1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer deleted;
}
