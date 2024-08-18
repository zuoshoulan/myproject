package com.zwk.su_netty.netty_server.http;

import com.zwk.su_netty.log.LogConstant;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhengweikang
 * @date 2024/8/17 00:52
 */
@Slf4j
public class HttpProxyServerHandler extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LogConstant.accessLogger.info("access msg:{}\n", msg);
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            TargetHttpRequestInitializer.connectToTargetServer(ctx, request);
        } else if (ctx.channel().attr(outboundChannelKey).get() != null) {
            Channel outboundChannel = ctx.channel().attr(outboundChannelKey).get();
            outboundChannel.writeAndFlush(msg);
        } else {

        }
        ReferenceCountUtil.release(msg);
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    // 定义一个 AttributeKey 用于存储 outboundChannel
    public static final io.netty.util.AttributeKey<Channel> outboundChannelKey = io.netty.util.AttributeKey.newInstance("outboundChannel");
}