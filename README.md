serviceInvoker

提供了1对多的http服务调用
服务方在指定的zookeeper上注册自己的ip:port这样一个字符串，客户端会通过zookeeper监听服务对应的实例。调用的时候使用ServiceInvoke，目前支持httpGet，和httpPostJson两个方法。需要传入serverName(这是和server端约定好的zookeeper的注册name）。同时需要指定两个告警ID，一个是没有服务实例的时候的告警ID，一个是调用出错的告警ID
ResponseChecker定义了对返回Response的检查，DefaultResponseChecker会默认的检查,这个需要我们定义http调用对规范，目前还没有实现，也可以自行定义。
使用前需要指定ParallelTaskExecutor，因为现在使用的都是并发执行的。

配置例子：

serviceInvoker:
  zookeeper:
    hosts: 127.0.0.1:2181,127.0.0.1:2181,127.0.0.1:2181
    path: /cncnc/servers
    connectionTimeout: 1000
    sessionTimeout: 1000
  serverConfig:
    servers:
      - name: player
      
目前只实现了异步，也不回调，会重试指定次数，然后错误后发告警。
可以参考率例子：com.gome.ad.utils.serviceInvoker.ServiceInvokerTest
