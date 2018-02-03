package com.gome.ads.zookeeper.serviceInvoker;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "serviceInvoker.serverConfig")
@Lazy(true)
public class ServerConfig {

    private List<ServerProperties> servers;

    public List<ServerProperties> getServers() {
        return servers;
    }

    public void setServers(List<ServerProperties> servers) {
        this.servers = servers;
    }
}
