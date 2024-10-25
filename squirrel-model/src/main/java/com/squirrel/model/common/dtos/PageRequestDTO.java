package com.squirrel.model.common.dtos;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 分页查询 DTO
 */
@Data
@Slf4j
public class PageRequestDTO {

    /**
     * 限制条数
     */
    protected Integer size;

    /**
     * 开始页数
     */
    protected Integer page;

    /**
     * 校验参数，给定默认值
     */
    public void checkParam() {
        if (this.page == null || this.page < 0) {
            setPage(1);
        }
        if (this.size == null || this.size < 0 || this.size > 100) {
            setSize(10);
        }
    }
}
