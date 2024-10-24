package com.squirrel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务类型枚举
 */
@Getter
@AllArgsConstructor
public enum TaskTypeEnum {

    NEW_SCAN_TIME(1001,1,"文章定时审核"),
    REMOTEERROR(1002,2,"第三方接口调用失败，重试");

    /**
     * 对应具体业务
     */
    private final int taskType;

    /**
     * 业务不同级别
     */
    private final int priority;

    /**
     * 描述信息
     */
    private final String desc;
}
