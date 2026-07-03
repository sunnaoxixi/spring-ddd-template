package com.sunnao.spring.ddd.template.common.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public abstract class BaseEntity implements Entity {

    private Long id;

    private LocalDateTime createAt;

    private Long createBy;

    private LocalDateTime updateAt;

    private Long updateBy;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BaseEntity that)) {
            return false;
        }
        if (this.getId() == null || that.getId() == null) {
            return false;
        }
        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return super.hashCode();
        }
        return this.getId().hashCode();
    }
}
