package com.gome.ads.zookeeper.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component
@ConfigurationProperties(prefix = "kafka")
@Data
public class KafkaConfig {

    private String servers;

    private String topic;

}
