package com.gome.ads.zookeeper.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component
@ConfigurationProperties(prefix = "server")
@Data
public class ServerConfig {

    private Integer port;

    private String bossGroupCount;

    private String workGroupCount;

}
