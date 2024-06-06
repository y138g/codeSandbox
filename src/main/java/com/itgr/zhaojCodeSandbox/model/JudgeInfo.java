package com.itgr.zhaojCodeSandbox.model;

import lombok.Data;

/**
 * @author ygking
 */
@Data
public class JudgeInfo {


    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 运行时间（ms）
     */
    private Long time;

    /**
     * 内存占用（kb）
     */
    private Long memory;

    /**
     * 栈占用(kb)
     */
    private Long stack;
}
