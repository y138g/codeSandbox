package com.itgr.zhaojCodeSandbox.controller;

import com.itgr.zhaojCodeSandbox.JavaDockerCodeSandbox;
import com.itgr.zhaojCodeSandbox.model.ExecuteCodeRequest;
import com.itgr.zhaojCodeSandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author ygking
 */
@RestController("/")
public class MainController {

    @Resource
    private JavaDockerCodeSandbox javaDockerCodeSandbox;

    /**
     * 提供代码沙箱开放 API
     * @param request
     * @return
     */
    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest request) {
        if (request == null){
            throw new IllegalArgumentException("请求参数异常");
        }
        return javaDockerCodeSandbox.executeCode(request);
    }
}
