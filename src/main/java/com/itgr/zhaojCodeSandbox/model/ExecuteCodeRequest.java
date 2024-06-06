package com.itgr.zhaojCodeSandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ygking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {
    /**
     * 输入用例组
     */
    private List<String> inputList;

    /**
     * 输入代码
     */
    private String code;

    /**
     * 编程语言
     */
    private String language;

}
