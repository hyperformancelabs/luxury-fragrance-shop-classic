//package com.hyperformancelabs.backend.service.impl;
//
//import com.hyperformancelabs.backend.config.VnPayConfig;
//import com.hyperformancelabs.backend.model.Order;
//import com.hyperformancelabs.backend.repository.OrderRepository;
//import com.hyperformancelabs.backend.service.VnPayService;
//import com.hyperformancelabs.backend.util.VnPayUtil;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.io.UnsupportedEncodingException;
//import java.math.BigDecimal;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//public class VnPayServiceImpl implements VnPayService {
//
//    private final VnPayConfig vnPayConfig;
//    private final OrderRepository orderRepository;
//
//    @Override
//    public String createVnPayPaymentURL(Order order, HttpServletRequest request) {
//        long amount = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
//        String txnRef = UUID.randomUUID().toString().replace("-", "").substring(0, 15);
//        String orderInfo = "Order:" + order.getOrderId();
//        String ip = request.getRemoteAddr();
//
//        Map<String, String> params = vnPayConfig.buildBaseParams(txnRef, orderInfo, ip);
//        params.put("vnp_Amount", String.valueOf(amount));
//
//        String hashData = VnPayUtil.buildQuery(params, false);
//        String secureHash = VnPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
//
//        String finalUrl = vnPayConfig.getUrl() + "?" + VnPayUtil.buildQuery(params, true)
//                + "&vnp_SecureHash=" + secureHash;
//
//        return finalUrl;
//    }
//}
