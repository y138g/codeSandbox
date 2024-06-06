package com.itgr.zhaojCodeSandbox.security;

import java.security.Permission;

/**
 * 默认安全管理器
 * @author ygking
 */
public class DefaultSecurityManager extends SecurityManager{
    @Override
    public void checkPermission(Permission perm) {
        System.out.println("默认不做任何限制");
        System.out.println(perm);
    }
}
