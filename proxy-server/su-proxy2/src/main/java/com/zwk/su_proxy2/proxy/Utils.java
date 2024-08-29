package com.zwk.su_proxy2.proxy;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;

/**
 * @author zhengweikang
 * @date 2024/8/14 11:52
 */
public class Utils {

    @SneakyThrows
    public static URI getReplacedUri(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest originalRequest = attributes.getRequest();

        // 获取原始请求的路径

        // 获取原始请求的查询字符串
        String queryString = originalRequest.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            requestURL.append("?" + queryString);
        }

        URI uri = new URI(requestURL.toString());
        if (uri.getHost().equalsIgnoreCase("127.0.0.1") || uri.getHost().equalsIgnoreCase("localhost")) {
            uri = new URI(uri.getScheme(), uri.getUserInfo(), "localhost", uri.getPort(), "/", uri.getQuery(), uri.getFragment());
        }
        String redirectHost = "zwk.com";
        String calculatePrefix = "/cost-calculate-service";
        if (StrUtil.startWith(uri.getPath(), calculatePrefix)) {
            int post = 8094;
            String redirectPath = StrUtil.subAfter(uri.getPath(), calculatePrefix, false);

            uri = new URI(uri.getScheme(), uri.getUserInfo(), redirectHost, post, redirectPath, uri.getQuery(), uri.getFragment());
        }
        String centerPrefix = "/cost-center-service";
        if (StrUtil.startWith(uri.getPath(), centerPrefix)) {
            int post = 8092;
            String redirectPath = StrUtil.subAfter(uri.getPath(), centerPrefix, false);
            uri = new URI(uri.getScheme(), uri.getUserInfo(), redirectHost, post, redirectPath, uri.getQuery(), uri.getFragment());
        }
        return uri;
    }

    public static byte[] readRequestBodyAsBytes(InputStream inputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024); // 分配缓冲区
        int bytesRead;
        byte[] allBytes = new byte[0]; // 存储所有读取的字节

        while ((bytesRead = inputStream.read(buffer.array())) != -1) {
            byte[] temp = new byte[bytesRead];
            System.arraycopy(buffer.array(), 0, temp, 0, bytesRead);
            allBytes = concatenateByteArrays(allBytes, temp);
            buffer.clear(); // 清空缓冲区，以便下一次读取
        }

        return allBytes;
    }

    public static byte[] concatenateByteArrays(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
