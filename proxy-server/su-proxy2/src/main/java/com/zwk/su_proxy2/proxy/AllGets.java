package com.zwk.su_proxy2.proxy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.PrintWriter;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author zhengweikang
 * @date 2024/8/14 00:07
 */
//@RestController
public class AllGets {

    @RequestMapping
    public String test() {
        return "这是个代理服务，不要直接调本机host，请使用curl -x\n";
    }

    @SneakyThrows
    @GetMapping(path = "/**")
    public void get(HttpServletRequest request, HttpServletResponse httpResponse) {
        // 获取原始请求的路径
        URI newUri = Utils.getReplacedUri(request);

        HttpRequest httpRequest = HttpUtil.createGet(newUri.toString());
        //10分钟超时时间
        httpRequest.timeout(10 * 60 * 1000);

        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(new Consumer<String>() {
            @Override
            public void accept(String headerKey) {
                Enumeration<String> headersEnum = request.getHeaders(headerKey);
                String join = StrUtil.join(";", headersEnum.asIterator());
                headers.put(headerKey, join);
            }
        });
        httpRequest.clearHeaders();
        httpRequest.addHeaders(headers);

        // 发送请求
        HttpResponse remoteResponse = httpRequest.execute();

        // 设置响应的状态码
        httpResponse.setStatus(httpResponse.getStatus());

        // 设置响应的头部
        remoteResponse.headers().forEach((name, values) -> {
            for (String value : values) {
                httpResponse.addHeader(name, value);
            }
        });
        httpResponse.addHeader("proxy", "zwk");

        // 将响应体写入 HttpServletResponse
//        httpResponse.setContentType(remoteResponse.contentType());
        httpResponse.setCharacterEncoding(remoteResponse.charset());
        try {
            PrintWriter writer = httpResponse.getWriter();
            writer.write(remoteResponse.body());
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
