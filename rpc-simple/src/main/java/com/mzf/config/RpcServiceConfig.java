package com.mzf.config;

import lombok.*;

/**
 * @author 好大雨
 * @create 2021/9/20 15:44
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    private String version = "";
    //当接口有多个实现类时，按组区分
    private String group = "";
    private Object service;

    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
