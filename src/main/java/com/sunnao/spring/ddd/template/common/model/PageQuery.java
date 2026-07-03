package com.sunnao.spring.ddd.template.common.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageQuery<Q> {

    private int startIndex = 0;

    private int pageSize = 10;

    private Q query;

    public static <Q> PageQuery<Q> build(Q query) {
        PageQuery<Q>
                pageQuery = new PageQuery<>();
        pageQuery.query = query;
        return pageQuery;
    }
}