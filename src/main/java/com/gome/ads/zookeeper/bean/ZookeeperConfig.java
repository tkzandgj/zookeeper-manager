package com.gome.ads.zookeeper.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 * zookeeper的配置
 */
@Component
@ConfigurationProperties(prefix = "zookeeper")
public class ZookeeperConfig {

    private String address;

    private Integer sessionTimeouts;

    private Integer connectionTimeouts;

    private String path;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getSessionTimeouts() {
        return sessionTimeouts;
    }

    public void setSessionTimeouts(Integer sessionTimeouts) {
        this.sessionTimeouts = sessionTimeouts;
    }

    public Integer getConnectionTimeouts() {
        return connectionTimeouts;
    }

    public void setConnectionTimeouts(Integer connectionTimeouts) {
        this.connectionTimeouts = connectionTimeouts;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "ZookeeperConfig{" +
                "address='" + address + '\'' +
                ", sessionTimeouts=" + sessionTimeouts +
                ", connectionTimeouts=" + connectionTimeouts +
                ", path='" + path + '\'' +
                '}';
    }
}
