package com.knowprocess.bpm;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.cfg.MailServerInfo;
import org.activiti.spring.boot.ActivitiProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@ConfigurationProperties(prefix = "spring.activiti.multiTenant")
public class MultiTenantActivitiProperties extends ActivitiProperties {
    private Map<String, MailServerInfo> servers = new HashMap<String, MailServerInfo>();

    public Map<String, MailServerInfo> getServers() {
        return this.servers;
    }

}
