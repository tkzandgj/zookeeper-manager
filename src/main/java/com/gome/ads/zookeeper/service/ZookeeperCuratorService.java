package com.gome.ads.zookeeper.service;

import com.gome.ads.zookeeper.bean.ZookeeperConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Administrator
 * zookeeper的curator客户端测试
 */
@Component
public class ZookeeperCuratorService {

    @Autowired
    private ZookeeperConfig zookeeperConfig;

    private CuratorFramework client;

    @PostConstruct
    public void init(){
        client = CuratorFrameworkFactory.newClient(zookeeperConfig.getAddress(), zookeeperConfig.getSessionTimeouts(),
                zookeeperConfig.getConnectionTimeouts(), new ExponentialBackoffRetry(1000, 3));

        client.start();
    }



    // 同步创建节点并赋值
    //@Scheduled(cron = "${scheduled.cron}")
    public void CuratorCreateNode() throws Exception{
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

    // 异步创建节点
    //@Scheduled(cron = "${scheduled.cron}")
    public void asyncCuratorCreateNode() throws Exception{

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

    // 时间监听   只是对子节点的数据变化进行监听
    //@Scheduled(cron = "${scheduled.cron}")
    public void addListenerChange() throws Exception{

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


    // Master选举
    //@Scheduled(cron = "${scheduled.cron}")
    public void leaderMaster() throws Exception{
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


    // 分布式锁
    //@Scheduled(cron = "${scheduled.cron}")
    public void zookeeperMutexLock(){
        final InterProcessMutex lock = new InterProcessMutex(client, zookeeperConfig.getPath());
        for (int i = 0; i < 30; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        semaphore.await();
                        lock.acquire();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss|SSS");
                    String orderNum = sdf.format(new Date());
                    System.out.println("生成的订单号是: " + orderNum);

                    try {
                        lock.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        semaphore.countDown();
    }

    @Scheduled(cron = "${scheduled.cron}")
    public void distributedCount() throws Exception{

        DistributedAtomicInteger atomicInteger = new DistributedAtomicInteger(client,
                zookeeperConfig.getPath(), new RetryNTimes(3, 1000));

        AtomicValue<Integer> rc = atomicInteger.add(8);

        System.out.println("Result : " + rc.succeeded());
    }
}
