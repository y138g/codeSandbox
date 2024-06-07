package com.itgr.zhaojCodeSandbox.model.DTO;

import lombok.Data;

import java.io.File;

/**
 *
 * @author ygking
 */
@Data
public class FileResponse {

    /**
     * 用户代码存储父路径
     */
    private String userCodeParentPath;

    /**
     * 用户代码 class 文件
     */
    private File userCodeFile;

    /**
     * 用户代码文件目录
     */
    private String userCodePath;
}
