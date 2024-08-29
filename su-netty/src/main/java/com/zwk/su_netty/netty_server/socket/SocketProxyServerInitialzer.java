package com.zwk.su_netty.netty_server.socket;

import com.zwk.su_netty.log.LogConstant;
import com.zwk.su_netty.netty_server.http.HttpProxyServerHandler;
import com.zwk.su_netty.netty_server.http.TargetHttpHandler;
import com.zwk.su_netty.netty_server.http.TargetHttpRequestInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.ReferenceCountUtil;

import java.net.URI;

/**
 * @author zhengweikang
 * @date 2024/8/24 23:22
 */
public class SocketProxyServerInitialzer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
//        ch.pipeline().addLast(new SocketProxyHandler());
    }

    public static final io.netty.util.AttributeKey<Channel> outboundChannelKey = io.netty.util.AttributeKey.newInstance("outboundChannel");

//    static class SocketProxyHandler extends ChannelDuplexHandler {
//
//        @Override
//        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            LogConstant.accessLogger.info("access msg:{}\n", msg);
//            if (ctx.channel().attr(outboundChannelKey).get() != null) {
//                Channel outboundChannel = ctx.channel().attr(outboundChannelKey).get();
//                outboundChannel.writeAndFlush(msg);
//                ReferenceCountUtil.release(msg);
//            } else {
//
//
//                Bootstrap b = new Bootstrap();
//                b.group(ctx.channel().eventLoop())
//                        .channel(NioSocketChannel.class)
//                        .handler(new ChannelInitializer<SocketChannel>() {
//                            @Override
//                            protected void initChannel(SocketChannel ch) throws Exception {
//                                ch.pipeline().addLast(new TargetSocketHandler(clientCtx, uri, method, finalMyRouter));
//                            }
//                        });
//
//                ChannelFuture targetFuture = b.connect(targetHost, targetPort);
//
//                targetFuture.addListener((ChannelFutureListener) future -> {
//                    if (future.isSuccess()) {
//                        Channel targetOutboundChannel = future.channel();
//                        ctx.channel().attr(HttpProxyServerHandler.outboundChannelKey).set(targetOutboundChannel);
//
//                        targetOutboundChannel.writeAndFlush(msg);
//
//                        ReferenceCountUtil.release(msg);
//                    } else {
//                        future.cause().printStackTrace();
//                        ctx.close();
//                    }
//                });
//
//
//            }
//
//        }
//    }


    static class TargetSocketHandler {

    }


}
