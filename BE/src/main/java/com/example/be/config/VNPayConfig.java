package com.example.be.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Configuration
@Getter
public class VNPayConfig {
    @Value("${vnpay.url}")
    private String vnp_PayUrl;
    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;
    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;
    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;
    @Value("${vnpay.version}")
    private String vnp_Version;
    @Value("${vnpay.command}")
    private String vnp_Command;

    public Map<String, String> getVNPayConfig() {
        Map<String, String> vnpParamsMap = new HashMap<>();
        vnpParamsMap.put("vnp_Version", vnp_Version);
        vnpParamsMap.put("vnp_Command", vnp_Command);
        vnpParamsMap.put("vnp_TmnCode", vnp_TmnCode);
        vnpParamsMap.put("vnp_CurrCode", "VND");
        vnpParamsMap.put("vnp_TxnRef", String.valueOf(System.currentTimeMillis()));
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan don hang:" + String.valueOf(System.currentTimeMillis()));
        vnpParamsMap.put("vnp_OrderType", "other");
        vnpParamsMap.put("vnp_Locale", "vn");
        vnpParamsMap.put("vnp_ReturnUrl", vnp_ReturnUrl);
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnpParamsMap.put("vnp_CreateDate", vnp_CreateDate);
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnpParamsMap.put("vnp_ExpireDate", vnp_ExpireDate);
        return vnpParamsMap;
    }
}
