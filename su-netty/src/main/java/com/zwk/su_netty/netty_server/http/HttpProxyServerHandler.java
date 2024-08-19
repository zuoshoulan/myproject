package com.zwk.su_netty.netty_server.http;

import com.zwk.su_netty.log.LogConstant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

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
            URI uri = createValidURI(request.uri());
            if ("CONNECT".equalsIgnoreCase(request.method().name())) {
                handleConnectRequest(ctx, request, uri);
            } else {
                TargetHttpRequestInitializer.connectToTargetServer(ctx, request);
            }
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

    private void handleConnectRequest(ChannelHandlerContext ctx, FullHttpRequest request, URI uri) {
        String targetHost = uri.getHost();
        int targetPort = uri.getPort() != -1 ? uri.getPort() : 443;

        // Create a new bootstrap for the TLS connection.
        Bootstrap tlsBootstrap = new Bootstrap()
                .group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        SslContext sslCtx = SslContextBuilder.forClient()
                                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                .build();
                        ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
                        ch.pipeline().addLast(new TargetHttpHandler(ctx, uri, request.method(), false));
                    }
                });

        // Connect to the target host and port.
        tlsBootstrap.connect(targetHost, targetPort).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                Channel outboundChannel = future.channel();
                ctx.channel().attr(HttpProxyServerHandler.outboundChannelKey).set(outboundChannel);
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
            } else {
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY));
                ctx.close();
            }
        });
    }

    private URI createValidURI(String uriString) {
        if (!uriString.startsWith("http://") && !uriString.startsWith("https://")) {
            // Add http:// as default protocol
            uriString = "http://" + uriString;
        }
        try {
            return new URI(uriString);
        } catch (Exception e) {
            log.error("Failed to create URI from string: {}", uriString, e);
            throw new RuntimeException("Invalid URI format", e);
        }
    }

    // 定义一个 AttributeKey 用于存储 outboundChannel
    public static final io.netty.util.AttributeKey<Channel> outboundChannelKey = io.netty.util.AttributeKey.newInstance("outboundChannel");
}