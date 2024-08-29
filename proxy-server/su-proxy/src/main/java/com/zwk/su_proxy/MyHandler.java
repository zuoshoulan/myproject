package com.zwk.su_proxy;


import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @author zhengweikang
 * @date 2024/8/14 16:49
 */
@Controller
@RequestMapping
public class MyHandler {

    @RequestMapping(path = "/test")
    public Mono<ServerResponse> handleRequest() {
//
//        ServerHttpRequest req = request.exchange().getRequest(); // 获取请求对象
//        ServerHttpResponse res = request.exchange().getResponse(); // 获取响应对象
//
//        // 在这里你可以访问 req 和 res 对象
//        // 例如打印请求的 URI
//        System.out.println("Request URI: " + req.getURI());

        // 返回一个简单的响应
        return ServerResponse.ok()
                .bodyValue("Hello from WebFlux!");
    }
}
