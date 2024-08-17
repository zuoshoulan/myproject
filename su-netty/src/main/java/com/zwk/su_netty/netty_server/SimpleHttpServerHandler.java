package com.zwk.su_netty.netty_server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

/**
 * @author zhengweikang
 * @date 2024/8/16 00:45
 */
@Slf4j
public class SimpleHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }


        HttpMethod method = request.method();
        String hostHeader = request.headers().get(HttpHeaderNames.HOST);
        URI uri = new URI(request.uri());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Welcome to the Netty HTTP Server!\n");
        stringBuilder.append(uri.toString() + "\n");
        stringBuilder.append("hostHeader:" + hostHeader + "\n");
        stringBuilder.append("protocolName:" + request.protocolVersion().protocolName() + "\n");
        stringBuilder.append("\n");
        String responseContent = stringBuilder.toString();

        log.info("method:{}", request.method());
        log.info("{}://{}{}", StringUtils.lowerCase(request.protocolVersion().protocolName()), hostHeader, request.uri());


        ByteBuf content = request.content();

        // 读取 ByteBuf 中的数据
        byte[] bodyBytes = new byte[content.readableBytes()];
        content.readBytes(bodyBytes);

        // 将字节数组转换为字符串
        String bodyContent = new String(bodyBytes, StandardCharsets.UTF_8);
        System.out.println("Body content: " + bodyContent);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(responseContent, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        sendHttpResponse(ctx, request, response);
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // 返回应答给客户端
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        // 如果是非 Keep-Alive，关闭连接
        boolean keepAlive = HttpUtil.isKeepAlive(req);
        ChannelFuture future = ctx.channel().writeAndFlush(res);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
