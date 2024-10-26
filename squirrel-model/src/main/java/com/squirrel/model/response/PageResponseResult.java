package com.squirrel.model.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 分页查询结果返回类
 * @param <T>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageResponseResult<T> extends ResponseResult<T> implements Serializable {

    /**
     * 当前页
     */
    private Integer currentPage;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总哦概述
     */
    private Integer total;
}
