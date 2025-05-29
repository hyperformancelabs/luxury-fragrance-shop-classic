package com.hyperformancelabs.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "payment.vnpay")
@Getter
@Setter
public class VnPayConfig {
    private String url;
    private String returnUrl;
    private String tmnCode;
    private String secretKey;
    private String version;
    private String command;
    private String orderType;

    public Map<String, String> buildBaseParams(String txnRef, String orderInfo, String ipAddress) {
        Map<String, String> map = new HashMap<>();
        map.put("vnp_Version", version);
        map.put("vnp_Command", command);
        map.put("vnp_TmnCode", tmnCode);
        map.put("vnp_OrderType", orderType);
        map.put("vnp_Locale", "vn");
        map.put("vnp_CurrCode", "VND");
        map.put("vnp_TxnRef", txnRef);
        map.put("vnp_OrderInfo", orderInfo);
        map.put("vnp_ReturnUrl", returnUrl);
        map.put("vnp_IpAddr", ipAddress);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        map.put("vnp_CreateDate", now.format(formatter));
        map.put("vnp_ExpireDate", now.plusMinutes(15).format(formatter));

        return map;
    }
}


