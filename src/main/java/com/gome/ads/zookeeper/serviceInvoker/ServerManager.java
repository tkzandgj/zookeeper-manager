package com.gome.ads.zookeeper.serviceInvoker;

import com.gome.ads.zookeeper.utils.JsonUtil;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Lazy(true)
public class ServerManager {
    private static final Logger logger = LoggerFactory.getLogger(ServerManager.class);

    private static final String ZK_PATH_SEP = "/";
    @Autowired
    private ZookeeperConfig zookeeperConfig;

    @Autowired
    private ServerConfig serverConfig;

    private Map<String, List<String>> serverHostMap = new HashMap<>();

    private Map<String, String> serverInvokeAlertMap = new HashMap<>();

    private Map<String, String> serverNoHostAlertMap = new HashMap<>();

    private ZkClient zkClient = null;

    protected void init() {
        zkClient = new ZkClient(zookeeperConfig.getHosts(),
                zookeeperConfig.getConnectionTimeout(), zookeeperConfig.getSessionTimeout());
        if (!zkClient.exists(zookeeperConfig.getPath())) {
            zkClient.createPersistent(zookeeperConfig.getPath(), true);
        }
        for(ServerProperties serverProperties : serverConfig.getServers()) {
            String serverPath = zookeeperConfig.getPath() + ZK_PATH_SEP + serverProperties.getName();
            if (!zkClient.exists(serverPath)) {
                zkClient.createPersistent(serverPath, true);
            } else {
                List<String> hosts = zkClient.getChildren(serverPath);
                serverHostMap.put(serverProperties.getName(), hosts);
            }

            zkClient.subscribeChildChanges(serverPath, new IZkChildListener() {
                @Override
                public void handleChildChange(String s, List<String> list) throws Exception {
                    logger.info("{} child changed, {}", s, JsonUtil.objectToString(list));
                    serverHostMap.put(serverProperties.getName(), list);
                }
            });
            serverInvokeAlertMap.put(serverProperties.getName(), serverProperties.getInvokeAlert());
            serverNoHostAlertMap.put(serverProperties.getName(), serverProperties.getNoHostAlert());
        }
    }

    @PreDestroy
    protected void shutdown() {
        this.zkClient.close();
    }

    protected List<String> getServerHosts(String serverName) {
        return serverHostMap.get(serverName);
    }

    protected String getServerInvokeAlertId(String serverName) {
        return serverInvokeAlertMap.get(serverName);
    }

    protected String getServerNoHostAlertId(String serverName) {
        return serverNoHostAlertMap.get(serverName);
    }


}
