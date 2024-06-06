package com.itgr.zhaojCodeSandbox;

import com.itgr.zhaojCodeSandbox.model.ExecuteCodeRequest;
import com.itgr.zhaojCodeSandbox.model.ExecuteCodeResponse;

/**
 * @author ygking
 * 代码沙箱接口
 */
public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
