package com.gome.ads.zookeeper.channelinitializer;


import com.gome.ads.zookeeper.handler.RequestChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component
public class ServerChannelInitializer extends ChannelInitializer {

    @Autowired
    private RequestChannelHandler requestChannelHandler;

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline ch = channel.pipeline();
        ch.addLast(requestChannelHandler);
    }
}
