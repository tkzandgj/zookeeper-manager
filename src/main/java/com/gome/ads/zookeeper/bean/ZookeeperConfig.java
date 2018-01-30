package com.gome.ads.zookeeper.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 * zookeeper的配置
 */
@Component
@ConfigurationProperties(prefix = "zookeeper")
@Data
public class ZookeeperConfig {

    private String address;

    private Integer sessionTimeouts;

    private Integer connectionTimeouts;

    private String path;

}
