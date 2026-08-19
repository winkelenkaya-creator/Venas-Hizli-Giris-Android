package com.venas.hizligiris;

import android.app.Notification;
import android.content.ComponentName;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class OtpNotificationListener extends NotificationListenerService {
    private String lastFingerprint = "";
    private long lastFingerprintAt = 0L;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return;
        if (getPackageName().equals(sbn.getPackageName())) return;

        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;

        String combined = collectText(extras);
        OtpParser.Result result = OtpParser.parse(combined);
        if (result == null) return;

        long now = System.currentTimeMillis();
        String fingerprint = result.otp + "|" + result.reference;
        if (fingerprint.equals(lastFingerprint) && now - lastFingerprintAt < 90_000L) {
            return;
        }

        lastFingerprint = fingerprint;
        lastFingerprintAt = now;
        OtpStore.save(this, result.otp, result.reference, now);
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        try {
            requestRebind(new ComponentName(this, OtpNotificationListener.class));
        } catch (Exception ignored) {
        }
    }

    private static String collectText(Bundle extras) {
        StringBuilder out = new StringBuilder();
        append(out, extras.getCharSequence(Notification.EXTRA_TITLE));
        append(out, extras.getCharSequence(Notification.EXTRA_TEXT));
        append(out, extras.getCharSequence(Notification.EXTRA_BIG_TEXT));

        CharSequence[] lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
        if (lines != null) {
            for (CharSequence line : lines) append(out, line);
        }
        return out.toString();
    }

    private static void append(StringBuilder out, CharSequence value) {
        if (value == null || value.length() == 0) return;
        if (out.length() > 0) out.append('\n');
        out.append(value);
    }
}
