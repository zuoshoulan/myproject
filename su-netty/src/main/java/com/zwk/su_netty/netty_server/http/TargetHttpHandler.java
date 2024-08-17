package com.zwk.su_netty.netty_server.http;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;

import java.net.URI;

/**
 * @author zhengweikang
 * @date 2024/8/17 01:33
 */
public class TargetHttpHandler extends ChannelDuplexHandler {

    private final ChannelHandlerContext clientCtx;
    private final URI uri;

    public TargetHttpHandler(ChannelHandlerContext clientCtx, URI uri) {
        this.clientCtx = clientCtx;
        this.uri = uri;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            response.headers().add("proxy", "zwk");
            clientCtx.write(response);
        } else {
            clientCtx.write(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        clientCtx.flush();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            request.headers().set(HttpHeaderNames.HOST, uri.getHost());


        }
        ctx.write(msg, promise);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        clientCtx.close();
    }
}
