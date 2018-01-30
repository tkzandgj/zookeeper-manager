package com.gome.ads.zookeeper.service;


import com.gome.ads.zookeeper.bean.ZookeeperConfig;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author tukangzheng
 * zookeeper的zkclient客户端测试
 */
public class ZookeeperZkClientService {

    private static Logger logger = LoggerFactory.getLogger(ZookeeperZkClientService.class);

    @Autowired
    private ZookeeperConfig zookeeperConfig;

    private ZkClient zkClient;

    @PostConstruct
    public void init(){
        zkClient = new ZkClient(new ZkConnection(zookeeperConfig.getAddress(), zookeeperConfig.getSessionTimeouts()));
    }


    //@Scheduled(cron = "${scheduled.cron}")
    private void testChildChanges() throws InterruptedException{
        zkClient.subscribeChildChanges(zookeeperConfig.getPath(), new IZkChildListener(){
            @Override
            public void handleChildChange(String s, List<String> list) throws Exception {
                logger.info("发送改变的节点路径为：" + s);
                for (String str : list){
                    logger.info("发送改变的节点为：" + str);
                }
            }
        });

        Thread.sleep(3000);
        if (!zkClient.exists(zookeeperConfig.getPath())){
            zkClient.createPersistent(zookeeperConfig.getPath());
            zkClient.createPersistent(zookeeperConfig.getPath() + "/c1", "内容一");
        }

    }

    //@Scheduled(cron = "${scheduled.cron}")
    private void testDataChanges() throws InterruptedException{
        zkClient.subscribeDataChanges(zookeeperConfig.getPath(), new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {
                logger.info("发生数据变化的节点路径为：" + s + ", 变化的数据为：" + o);
            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                logger.info("发送数据删除的节点的路径为：" + s);
            }
        });

        if (!zkClient.exists(zookeeperConfig.getPath())){
            zkClient.createPersistent(zookeeperConfig.getPath(), "1234");
            zkClient.writeData(zookeeperConfig.getPath(), "4567", -1);
            Thread.sleep(3000);

            zkClient.delete(zookeeperConfig.getPath());
        }
    }
}
