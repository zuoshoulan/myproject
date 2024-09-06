package com.zwk.su_netty.netty_server.forward;

import com.zwk.su_netty.log.LogConstant;
import com.zwk.su_netty.netty_server.http.HttpProxyServerHandler;
import com.zwk.su_netty.netty_server.http.TargetHttpHandler;
import com.zwk.su_netty.netty_server.http.TargetHttpRequestInitializer;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengweikang
 * @date 2024/9/7 02:47
 */
@Slf4j
public class ForwardProxyServerInitialzer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
        ch.pipeline().addLast(new ChunkedWriteHandler());
        ch.pipeline().addLast(new ForwardProxyServerHandler());

    }


    public static class ForwardProxyServerHandler extends ChannelDuplexHandler {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            LogConstant.accessLogger.info("access msg:{}\n", msg);

            log.info("access msg:{}\n", msg);

            if (ctx.channel().attr(targetChannelAttributeKey).get() != null) {
                Channel outboundChannel = ctx.channel().attr(targetChannelAttributeKey).get();
                outboundChannel.writeAndFlush(msg);
            } else if (msg instanceof FullHttpRequest) {
                FullHttpRequest request = (FullHttpRequest) msg;
                URI uri = createValidURI(request.uri());
                if ("CONNECT".equalsIgnoreCase(request.method().name())) {
//                    handleConnectRequest(ctx, request, uri);
                } else {
                    connectToTargetServer(ctx.channel(), request);
                }
            }
            ReferenceCountUtil.release(msg);
        }
    }

    public static final io.netty.util.AttributeKey<Channel> targetChannelAttributeKey = io.netty.util.AttributeKey.newInstance("targetChannel");

    private static URI createValidURI(String uriString) {
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

    @SneakyThrows
    public static void connectToTargetServer(Channel serverChannel, FullHttpRequest request) {
        request.retain();
        final HttpMethod method = request.method();
        URI uri = new URI(request.uri());
        String targetHost = uri.getHost();
        int targetPort = uri.getPort() != -1 ? uri.getPort() : 80;
        String targetPath = request.uri();

        HttpVersion httpVersion = request.protocolVersion();

        final String finalTargetPath = targetPath;


        Bootstrap b = new Bootstrap();
        b.group(serverChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpClientCodec());
                        ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
                        ch.pipeline().addLast(new ChunkedWriteHandler());
                        ch.pipeline().addLast(new TargetHttpHandler(serverChannel));
                    }
                });

        ChannelFuture targetFuture = b.connect(targetHost, targetPort);

        targetFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                Channel targetChannel = future.channel();
                serverChannel.attr(targetChannelAttributeKey).set(targetChannel);

                FullHttpRequest fullRequest = new DefaultFullHttpRequest(
                        httpVersion,
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

                targetChannel.writeAndFlush(fullRequest);
            } else {
                future.cause().printStackTrace();
                serverChannel.close();
            }
        });
    }

    public static class TargetHttpHandler extends ChannelDuplexHandler {

        private final Channel serverChannel;


        public TargetHttpHandler(Channel serverChannel) {
            this.serverChannel = serverChannel;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof FullHttpResponse) {
                FullHttpResponse response = (FullHttpResponse) msg;
                response.headers().add("proxy", "zwk");
            }
            serverChannel.write(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            serverChannel.flush();
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            ctx.write(msg, promise);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            serverChannel.close();
        }
    }
}
