package com.venas.hizligiris;

import android.content.Context;
import android.content.SharedPreferences;

final class OtpStore {
    private OtpStore() {}

    private static final String PREF = "venas_hizli_giris";
    private static final long TTL_MS = 180_000L;

    static void save(Context context, String otp, String reference, long timestamp) {
        prefs(context).edit()
                .putString("otp", otp == null ? "" : otp)
                .putString("reference", reference == null ? "" : reference)
                .putLong("timestamp", timestamp)
                .apply();
    }

    static Entry current(Context context) {
        SharedPreferences p = prefs(context);
        String otp = p.getString("otp", "");
        String ref = p.getString("reference", "");
        long ts = p.getLong("timestamp", 0L);
        if (otp == null || otp.length() != 6 || ts <= 0L) return null;
        if (System.currentTimeMillis() - ts > TTL_MS) {
            clear(context);
            return null;
        }
        return new Entry(otp, ref == null ? "" : ref, ts);
    }

    static void clear(Context context) {
        prefs(context).edit().remove("otp").remove("reference").remove("timestamp").apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    static final class Entry {
        final String otp;
        final String reference;
        final long timestamp;

        Entry(String otp, String reference, long timestamp) {
            this.otp = otp;
            this.reference = reference;
            this.timestamp = timestamp;
        }
    }
}
