package com.sunnao.spring.ddd.template.infrastructure.system.dict.mysql.po;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sunnao.spring.ddd.template.common.model.BasePO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 字典数据持久化对象
 * 与 sys_dict_data 表一一对应，仅用于 Infrastructure 层内部；
 * 审计字段继承自 BasePO，由全局监听器自动填充。
 * 注意：value 是 PostgreSQL 关键字，列名为 dict_value
 */
@Getter
@Setter
@ToString
@Table("sys_dict_data")
public class DictDataPO extends BasePO {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 字典类型键
     */
    private String typeKey;

    /**
     * 字典标签
     */
    private String label;

    /**
     * 字典值
     */
    @Column("dict_value")
    private String dictValue;

    /**
     * 排序（升序）
     */
    private Integer sort;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除：0-正常，1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer deleted;
}
