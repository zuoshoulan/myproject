package com.zwk.su_netty.netty_server;

import com.zwk.su_netty.netty_server.http.HttpProxyServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

/**
 * @author zhengweikang
 * @date 2024/8/16 00:36
 */
@Service
public class NettyMain {

    @PostConstruct
    public void start() {
        Thread thread = new Thread(() -> {
            test();
        });
        thread.setDaemon(false);
        thread.start();

    }

    @SneakyThrows
    private void test() {
        int port = 8080; // 可以根据需要更改端口

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpProxyServerInitializer());

            ChannelFuture f = b.bind(port).sync();

            System.out.println("HTTP server started at http://localhost:" + port);

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


}
