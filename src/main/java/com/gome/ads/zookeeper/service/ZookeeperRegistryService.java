package com.gome.ads.zookeeper.service;

import com.gome.ads.zookeeper.bean.ZookeeperConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Administrator
 */
@Component
public class ZookeeperRegistryService {

    @Autowired
    private ZookeeperConfig zookeeperConfig;

    private CuratorFramework client;

    //@Scheduled(cron = "${scheduled.cron}")
    public void CuratorCreateNode() throws Exception{
        client = CuratorFrameworkFactory.newClient(zookeeperConfig.getAddress(), zookeeperConfig.getSessionTimeouts(),
                zookeeperConfig.getConnectionTimeouts(), new ExponentialBackoffRetry(1000, 3));

        client.start();

        // 创建节点（若父节点不存在的话先创建父节点）
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(zookeeperConfig.getPath(), "zookeeper test".getBytes());


        Stat stat = new Stat();
        // 获取节点内容
        byte[] data = client.getData().storingStatIn(stat).forPath(zookeeperConfig.getPath());
        System.out.println("node content is : " + new String(data));

        // 删除节点  guaranteed()保证Curator在后台持续进行删除操作 直到节点删除成功
        client.delete().guaranteed().deletingChildrenIfNeeded()
                .withVersion(stat.getVersion()).forPath(zookeeperConfig.getPath());


        // 更新节点
        client.setData().withVersion(stat.getVersion()).forPath(zookeeperConfig.getPath()).getVersion();
    }


    private CountDownLatch semaphore = new CountDownLatch(2);
    private ExecutorService tp = Executors.newFixedThreadPool(2);

    //@Scheduled(cron = "${scheduled.cron}")
    public void asyncCuratorCreateNode() throws Exception{
        client = CuratorFrameworkFactory.newClient(zookeeperConfig.getAddress(), zookeeperConfig.getSessionTimeouts(),
                zookeeperConfig.getConnectionTimeouts(), new ExponentialBackoffRetry(1000, 3));
        client.start();

        // 异步创建节点  单独开启线程池去处理创建节点的操作
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                        System.out.println("event[code:" + curatorEvent.getResultCode()
                                + ", type: " + curatorEvent.getType() + "]");

                        System.out.println("Thread of processResult: " + Thread.currentThread().getName());

                        semaphore.countDown();
                    }
                }, tp).forPath(zookeeperConfig.getPath(), "java test".getBytes());

        // 没有用到线程池
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                        System.out.println("event[code:" + curatorEvent.getResultCode()
                                + ", type: " + curatorEvent.getType() + "]");

                        System.out.println("Thread of processResult: " + Thread.currentThread().getName());

                        semaphore.countDown();
                    }
                }).forPath(zookeeperConfig.getPath(), "java test".getBytes());

        semaphore.await();
        tp.shutdown();
    }

    //@Scheduled(cron = "${scheduled.cron}")
    public void addListenerChange() throws Exception{
        client = CuratorFrameworkFactory.newClient(zookeeperConfig.getAddress(), zookeeperConfig.getSessionTimeouts(),
                zookeeperConfig.getConnectionTimeouts(), new ExponentialBackoffRetry(1000, 3));
        client.start();

        PathChildrenCache cache = new PathChildrenCache(client, zookeeperConfig.getPath(), true);
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        // 对子节点的变更   会触发相应的输出    但是path本身是不会触发变更通知的
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curator, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()){
                    case CHILD_ADDED:
                        System.out.println("CHILD_ADDED, " + event.getData().getPath());
                        break;
                    case CHILD_UPDATED:
                        System.out.println("CHILD_UPDATED, " + event.getData().getPath());
                        break;
                    case CHILD_REMOVED:
                        System.out.println("CHILD_REMOVED, " + event.getData().getPath());
                        break;
                    default:
                        break;

                }
            }
        });

        client.create().withMode(CreateMode.PERSISTENT).forPath(zookeeperConfig.getPath());
        Thread.sleep(1000);

        client.create().withMode(CreateMode.PERSISTENT).forPath(zookeeperConfig.getPath() + "/c1");
        Thread.sleep(1000);

        client.delete().forPath(zookeeperConfig.getPath() + "/c1");
        Thread.sleep(1000);

        client.delete().forPath(zookeeperConfig.getPath());
        Thread.sleep(1000);
    }


    @Scheduled(cron = "${scheduled.cron}")
    public void leaderMaster() throws Exception{
        client = CuratorFrameworkFactory.newClient(zookeeperConfig.getAddress(), zookeeperConfig.getSessionTimeouts(),
                zookeeperConfig.getConnectionTimeouts(), new ExponentialBackoffRetry(1000, 3));
        client.start();

        LeaderSelector selector = new LeaderSelector(client, zookeeperConfig.getPath(), new LeaderSelectorListener() {
            @Override
            public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                System.out.println("成为Master角色");
                Thread.sleep(1000);

                System.out.println("完成Master角色, 释放Master权利");
            }

            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {

            }
        });

        selector.autoRequeue();
        selector.start();

        Thread.sleep(1000);
    }
}
