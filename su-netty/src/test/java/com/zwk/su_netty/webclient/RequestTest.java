//package com.zwk.su_netty.webclient;
//
//import org.springframework.http.HttpHeaders;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.function.Consumer;
//
///**
// *
// * @author zhengweikang
// * @date 2024/8/17 00:34
// */
//public class RequestTest {
////    curl 'http://azul.com:8094/controller-ProfitService/querySite?platform=ebay' \
////            -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:129.0) Gecko/20100101 Firefox/129.0' \
////            -H 'Accept: */*' \
////            -H 'Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2' \
////            -H 'Accept-Encoding: gzip, deflate' \
////            -H 'Referer: http://sem-dev.vevor-internal.net/' \
////            -H 'Authorization: Bearer e81831e3-091a-4d5a-ab44-74aa0f53c7fd' \
////            -H 'Origin: http://sem-dev.vevor-internal.net' \
////            -H 'Connection: keep-alive' \
////            -H 'Priority: u=4'
//    public void testGet(){
//        WebClient webClient = WebClient.create();
//        webClient.get().uri("http://azul.com:8094/controller-ProfitService/querySite?platform=ebay")
//                .headers(new Consumer<HttpHeaders>() {
//                    @Override
//                    public void accept(HttpHeaders httpHeaders) {
//                        httpHeaders.add("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:129.0) Gecko/20100101 Firefox/129.0");
//                        httpHeaders.add("Accept","*/*");
//                        httpHeaders.add("Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
//                        httpHeaders.add("Accept-Encoding","gzip, deflate");
//                        httpHeaders.add("Referer","http://sem-dev.vevor-internal.net/");
//                        httpHeaders.add("Authorization","Bearer e81831e3-091a-4d5a-ab44-74aa0f53c7fd");
//                        httpHeaders.add("Origin","http://sem-dev.vevor-internal.net");
//                        httpHeaders.add("Connection","keep-alive");
//                        httpHeaders.add("Priority","u=4");
//                    }
//                });
//        webClient.
//
//
//    }
//}
