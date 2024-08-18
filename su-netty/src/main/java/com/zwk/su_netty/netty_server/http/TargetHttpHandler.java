package com.zwk.su_netty.netty_server.http;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengweikang
 * @date 2024/8/17 01:33
 */
public class TargetHttpHandler extends ChannelDuplexHandler {

    private final ChannelHandlerContext clientCtx;
    private final URI uri;

    private final HttpMethod method;

    private final boolean myRouter;

    public TargetHttpHandler(ChannelHandlerContext clientCtx, URI uri, HttpMethod method, boolean myRouter) {
        this.clientCtx = clientCtx;
        this.uri = uri;
        this.method = method;
        this.myRouter = myRouter;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;

            response.headers().add("proxy", "zwk");
            if (myRouter) {
                fillCorsrHeaders(response);
            }


            clientCtx.write(response);
        } else {
            clientCtx.write(msg);
        }
    }

    private void fillCorsrHeaders(FullHttpResponse response) {
        Map<String, Object> map = new HashMap();
        map.put("Access-Control-Allow-Credentials", true);
        map.put("Access-Control-Allow-Headers", "authorization");

        map.put("Access-Control-Allow-Origin", "http://sem-dev.vevor-internal.net");
        map.put("Access-Control-Expose-Headers", "*");
        map.put("Access-Control-Max-Age", 18000L);


        map.put("Access-Control-Allow-Methods", "GET");

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (StringUtils.isEmpty(response.headers().get(key))) {
                response.headers().add(key, value);
            }
        }

        String accessControlAllowCredentials = "Access-Control-Allow-Credentials";
        if (StringUtils.isEmpty(response.headers().get(accessControlAllowCredentials))) {
            response.headers().add(accessControlAllowCredentials, true);
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
