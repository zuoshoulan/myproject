//package com.zwk.su_proxy;
//
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.client.reactive.ClientHttpRequest;
//import org.springframework.web.reactive.function.BodyInserter;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.server.ServerRequest;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import reactor.core.publisher.Mono;
//
//import java.util.function.Consumer;
//
//public class ProxyHandler {
//
//    private final WebClient webClient;
//
//    public ProxyHandler(WebClient.Builder webClientBuilder) {
//        this.webClient = webClientBuilder.build();
//    }
//
//    public Mono<ServerResponse> proxyRequest(ServerRequest request) {
//        String originalUri = request.uri().toString();
//        String path = request.uri().getPath();
//        String newUri = "http://zzz.com" + path;
//
//        org.springframework.http.server.reactive.ServerHttpRequest serverHttpRequest = request.exchange().getRequest();
//        // 从 ServerHttpRequest 获取 ContentType
//
//        WebClient.RequestBodySpec newRequest = webClient.method(request.method())
//                .uri(newUri)
//                .headers(headers -> {
//                    // 迭代请求头部
//                    request.headers().asHttpHeaders().forEach((name, values) -> {
//                        values.forEach(value -> headers.add(name, value));
//                    });
//                });
//
//        // 检查是否有 body
//        Mono<?> bodyMono = request.bodyToMono(Void.class);
//        boolean hasBody = bodyMono.hasElement().block();
//
//        if (hasBody) {
//            // 如果有 body，则使用 bodyToMono 获取 body
//            newRequest.body((BodyInserter<?, ? super ClientHttpRequest>) request.bodyToMono(String.class));
//        } else {
//            newRequest.get();
//        }
//
//        return newRequest.retrieve()
//                .toBodilessEntity()
//                .flatMap(entity -> ServerResponse.status(entity.getStatusCode())
//                        .headers(new Consumer<HttpHeaders>() {
//                            @Override
//                            public void accept(HttpHeaders httpHeaders) {
//                                entity.getHeaders();
//                            }
//                        })
//                        .build());
//    }
//}
