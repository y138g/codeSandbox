package com.itgr.zhaojCodeSandbox;

import com.itgr.zhaojCodeSandbox.model.ExecuteCodeRequest;
import com.itgr.zhaojCodeSandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * java 原生实现，直接调用父类
 * @author ygking
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
