package com.sunnao.spring.ddd.template.domain.system.file.model.aggregate;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.common.exception.AggregateException;
import com.sunnao.spring.ddd.template.common.model.BaseAggregate;
import com.sunnao.spring.ddd.template.domain.system.file.model.entity.FileEntity;
import com.sunnao.spring.ddd.template.domain.system.file.model.param.CreateFileParam;
import com.sunnao.spring.ddd.template.model.system.file.FileStorageTypeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件聚合根
 * <p>
 * 不直接持有文件属性，仅包含 FileEntity 实体（文件元数据）；
 * 物理文件内容由应用层通过 FileStorage 抽象管理，领域层不感知存储技术细节。
 */
@Getter
@Setter
public class FileAggregate extends BaseAggregate {

    /** 文件实体 */
    private FileEntity fileEntity;

    /**
     * 创建文件聚合根（登记文件元数据）
     *
     * @param param 创建参数
     * @return 文件聚合根
     * @throws AggregateException 校验失败
     */
    public static FileAggregate create(CreateFileParam param) throws AggregateException {
        if (param == null) {
            throw new AggregateException("PARAM_ERROR", "创建参数不能为空");
        }
        if (StrUtil.isBlank(param.getOriginalName())) {
            throw new AggregateException("PARAM_ERROR", "原始文件名不能为空");
        }
        if (StrUtil.isBlank(param.getPath())) {
            throw new AggregateException("PARAM_ERROR", "存储路径不能为空");
        }
        if (param.getSize() == null || param.getSize() <= 0) {
            throw new AggregateException("PARAM_ERROR", "文件大小必须大于0");
        }
        if (param.getStorageType() == null) {
            throw new AggregateException("PARAM_ERROR", "存储类型不能为空");
        }

        FileEntity entity = new FileEntity();
        entity.setOriginalName(param.getOriginalName());
        entity.setPath(param.getPath());
        entity.setSize(param.getSize());
        entity.setContentType(param.getContentType());
        entity.setStorageType(FileStorageTypeEnum.getByCode(param.getStorageType()));
        if (entity.getStorageType() == null) {
            throw new AggregateException("PARAM_ERROR", "存储类型不合法");
        }
        entity.setCreateBy(param.getOperatorId());
        entity.setUpdateBy(param.getOperatorId());

        FileAggregate aggregate = new FileAggregate();
        aggregate.setFileEntity(entity);
        return aggregate;
    }
}
