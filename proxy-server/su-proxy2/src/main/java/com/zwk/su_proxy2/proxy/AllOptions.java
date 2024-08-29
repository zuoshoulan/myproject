package com.zwk.su_proxy2.proxy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author zhengweikang
 * @date 2024/8/14 15:05
 */
@Controller
public class AllOptions {

    @RequestMapping(value = {
            "/**",
            "/controller-costItemConfig/queryDropdownList",
            "/controller-costItemConfig/costList"
    }, method = RequestMethod.OPTIONS)
    public void handleOptionsRequest(HttpServletRequest request, HttpServletResponse httpResponse) {

        // 获取原始请求的路径
        String newUrl = Utils.getReplacedUri(request).toString();

        Method method = Method.valueOf(request.getMethod().toUpperCase());
        HttpRequest httpRequest = HttpUtil.createRequest(method, newUrl);

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
