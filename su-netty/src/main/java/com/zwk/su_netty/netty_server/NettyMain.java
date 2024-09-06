package com.zwk.su_netty.netty_server;

import com.zwk.su_netty.netty_server.forward.ForwardProxyServerInitialzer;
import com.zwk.su_netty.netty_server.http.HttpProxyServerInitializer;
import com.zwk.su_netty.netty_server.socket.SocketProxyServerInitialzer;
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

    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    @PostConstruct
    public void start() {
        Thread thread01 = new Thread(() -> {
            httpProxyServerStart();
        });
        thread01.setDaemon(false);
        thread01.start();

        Thread thread02 = new Thread(() -> {
            forwardProxyServerStart();
        });
        thread02.setDaemon(false);
        thread02.start();
    }

    @SneakyThrows
    private void httpProxyServerStart() {
        int port = 8080; // 可以根据需要更改端口
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

    @SneakyThrows
    private void forwardProxyServerStart() {
        int port = 8088;
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ForwardProxyServerInitialzer());

            ChannelFuture f = b.bind(port).sync();

            System.out.println("SOCKET server started at http://localhost:" + port);

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


}
