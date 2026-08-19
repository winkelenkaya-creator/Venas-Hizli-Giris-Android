package com.venas.hizligiris;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class OtpParser {
    private OtpParser() {}

    private static final Pattern OTP_PATTERN = Pattern.compile(
            "h[ıi]zl[ıi]\\s+giri[sş]\\s+[sş]ifreniz\\s*[:\\-]?\\s*(\\d{6})",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern REF_PATTERN = Pattern.compile(
            "referans\\s*:\\s*([A-Z0-9]{4,12}(?:\\s+B\\d{3})?)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    static Result parse(String text) {
        if (text == null) return null;

        Matcher otpMatcher = OTP_PATTERN.matcher(text);
        if (!otpMatcher.find()) return null;

        String otp = otpMatcher.group(1);
        String reference = "";
        Matcher refMatcher = REF_PATTERN.matcher(text);
        if (refMatcher.find()) {
            reference = refMatcher.group(1).trim().toUpperCase();
        }
        return new Result(otp, reference);
    }

    static final class Result {
        final String otp;
        final String reference;

        Result(String otp, String reference) {
            this.otp = otp;
            this.reference = reference;
        }
    }
}
