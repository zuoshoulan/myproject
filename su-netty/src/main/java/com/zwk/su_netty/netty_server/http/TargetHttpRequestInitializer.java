package com.zwk.su_netty.netty_server.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;

/**
 * @author zhengweikang
 * @date 2024/8/17 01:31
 */
public class TargetHttpRequestInitializer {

    @SneakyThrows
    public static void connectToTargetServer(ChannelHandlerContext clientCtx, FullHttpRequest request) {
        request.retain();
        HttpMethod method = request.method();
        URI uri = new URI(request.uri());
        String targetHost = uri.getHost();
        int targetPort = uri.getPort() != -1 ? uri.getPort() : 80;
        String targetPath = request.uri();
        String calculatePrefix = "/cost-calculate-service";
        if (method.compareTo(HttpMethod.OPTIONS) != 0 && StringUtils.startsWith(uri.getPath(), calculatePrefix)) {
            targetPath = StringUtils.substringAfter(targetPath, calculatePrefix);
            targetHost = "localhost";
            targetPort = 8094;
        }
        String centerPrefix = "/cost-center-service";
        if (method.compareTo(HttpMethod.OPTIONS) != 0 && StringUtils.startsWith(uri.getPath(), centerPrefix)) {
            targetPath = StringUtils.substringAfter(targetPath, centerPrefix);
            targetHost = "localhost";
            targetPort = 8092;
        }


        final String finalTargetPath = targetPath;

        SslContext sslCtx = null;
        if (uri.getScheme().equalsIgnoreCase("https")) {
            try {
                sslCtx = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        final SslContext finalSslCtx = sslCtx;


        Bootstrap b = new Bootstrap();
        b.group(clientCtx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        if (finalSslCtx != null) {
                            ch.pipeline().addLast(finalSslCtx.newHandler(ch.alloc()));
                        }
                        ch.pipeline().addLast(new HttpClientCodec());
                        ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
                        ch.pipeline().addLast(new ChunkedWriteHandler());
                        ch.pipeline().addLast(new TargetHttpHandler(clientCtx, uri));
                    }
                });

        ChannelFuture targetFuture = b.connect(targetHost, targetPort);

        targetFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                Channel targetOutboundChannel = future.channel();
                clientCtx.channel().attr(HttpProxyServerHandler.outboundChannelKey).set(targetOutboundChannel);

                FullHttpRequest fullRequest = new DefaultFullHttpRequest(
                        HttpVersion.HTTP_1_1,
                        method,
                        finalTargetPath,
                        request.content().copy()
                );

                for (CharSequence name : request.headers().names()) {
                    for (CharSequence value : request.headers().getAll(name)) {
                        fullRequest.headers().add(name, value);
                    }
                }

                // 释放原始的请求体
                ReferenceCountUtil.release(request.content());

                targetOutboundChannel.writeAndFlush(fullRequest);
            } else {
                future.cause().printStackTrace();
                clientCtx.close();
            }
        });
    }
}
