package com.zwk.su_netty.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;

/**
 * @author zhengweikang
 * @date 2024/9/8 00:17
 */
@Slf4j
public class UriUtil {

    private static URI createValidURI(String uriString, boolean ishttps) {
        if (!uriString.startsWith("http://") && !uriString.startsWith("https://")) {
            // Add http:// as default protocol
            if (ishttps) {
                uriString = "https://" + uriString;
            } else {
                uriString = "http://" + uriString;
            }
        }
        try {
            return new URI(uriString);
        } catch (Exception e) {
            log.error("Failed to create URI from string: {}", uriString, e);
            throw new RuntimeException("Invalid URI format", e);
        }
    }
}
