package com.solux31th.audiya.global.sms;

// 로그용 전화번호 마스킹. 01012345678 -> 010****5678
public final class PhoneMasker {

    private PhoneMasker() {
    }

    public static String mask(String phone) {
        if (phone == null || phone.length() < 8) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}