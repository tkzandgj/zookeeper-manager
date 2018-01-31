package com.gome.ads.zookeeper.mq;

import com.gome.ads.zookeeper.bean.KafkaConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * @author Administrator
 */
@Component
public class KafkaProducer {

    private static Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    @Autowired
    private KafkaConfig kafkaConfig;

    private Producer<String, String> producer;

    @PostConstruct
    private void init(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getServers());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 1);
        // 避免消息乱序
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new org.apache.kafka.clients.producer.KafkaProducer<String, String>(props);
    }


    /**
     * 发送kafka消息
     */
    public void sendMessage(String message){
        try {
            producer.send(new ProducerRecord<String, String>(message, kafkaConfig.getTopic()));
        } catch (Exception e) {
            logger.error("send kafka message exception is :{}", e);
        }
    }
}
