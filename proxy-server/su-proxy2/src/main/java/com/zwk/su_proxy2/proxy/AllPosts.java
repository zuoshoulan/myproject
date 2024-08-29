package com.zwk.su_proxy2.proxy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author zhengweikang
 * @date 2024/8/14 11:49
 */
//@RestController
public class AllPosts {

    @SneakyThrows
    @PostMapping(path = "/**")
    public void post(HttpServletRequest request, HttpServletResponse httpResponse) {
        // 获取原始请求的路径
        String newUrl = Utils.getReplacedUri(request).toString();

        byte[] requestBody = Utils.readRequestBodyAsBytes(request.getInputStream());

        HttpRequest httpRequest = HttpUtil.createRequest(Method.POST, newUrl);
        httpRequest.body(requestBody);
//        HttpRequest post = HttpUtil.createPost(newUrl);
//        post.body(requestBody);

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
