package com.zwk.su_netty.netty_server.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.SneakyThrows;

import java.net.URI;

/**
 * @author zhengweikang
 * @date 2024/8/17 01:31
 */
public class TargetHttpRequestInitializer {

    @SneakyThrows
    public static void connectToTargetServer(ChannelHandlerContext clientCtx, HttpRequest request) {
        URI uri = new URI(request.uri());
        String targetHost = uri.getHost();
        int targetPort = uri.getPort() != -1 ? uri.getPort() : 80;

        Bootstrap b = new Bootstrap();
        b.group(clientCtx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpClientCodec());
                        ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
                        ch.pipeline().addLast(new ChunkedWriteHandler());
                        ch.pipeline().addLast(new TargetHttpHandler(clientCtx, uri));
                    }
                });

        ChannelFuture outboundFuture = b.connect(targetHost, targetPort);

        outboundFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                Channel targetOutboundChannel = future.channel();
                clientCtx.channel().attr(HttpProxyServerHandler.outboundChannelKey).set(targetOutboundChannel);

                FullHttpRequest fullRequest = new DefaultFullHttpRequest(
                        HttpVersion.HTTP_1_1,
                        request.method(),
                        uri.getPath());

                for (CharSequence name : request.headers().names()) {
                    for (CharSequence value : request.headers().getAll(name)) {
                        fullRequest.headers().add(name, value);
                    }
                }

                targetOutboundChannel.writeAndFlush(fullRequest);
            } else {
                future.cause().printStackTrace();
                clientCtx.close();
            }
        });
    }
}
