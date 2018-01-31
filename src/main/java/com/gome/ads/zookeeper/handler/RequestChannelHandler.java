package com.gome.ads.zookeeper.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@ChannelHandler.Sharable
@Component
public class RequestChannelHandler extends ChannelInboundHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest){
            FullHttpRequest request = (FullHttpRequest)msg;

        }
        super.channelRead(ctx, msg);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        super.exceptionCaught(ctx, cause);
    }
}
