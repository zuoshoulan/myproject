//package com.zwk.su_proxy2.config;
//
//import org.apache.catalina.filters.CorsFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
///**
// * @author zhengweikang
// * @date 2024/8/14 15:07
// */
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**") // 配置所有路径
//                .allowedOrigins("http://sem-dev.vevor-internal.net") // 允许任何来源
//
//                .allowedHeaders("authorization") // 允许任何头
//                .allowedMethods("POST", "GET") // 允许的方法
//                .allowCredentials(true) // 是否允许 cookie 凭据
//                .exposedHeaders("*")
//                .maxAge(18000L); // 预检请求的有效期
//    }
//
////    @Bean
////    public CorsFilter corsFilter() {
////        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
////        CorsConfiguration config = new CorsConfiguration();
////        config.setAllowCredentials(true);
////        config.addAllowedOrigin("*");
////        config.addAllowedHeader("*");
////        config.addAllowedMethod("*");
////        source.registerCorsConfiguration("/**", config);
////        CorsFilter corsFilter = new CorsFilter(source);
////        return corsFilter;
////    }
//}
