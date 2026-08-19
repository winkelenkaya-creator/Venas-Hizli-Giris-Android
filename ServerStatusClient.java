package com.venas.hizligiris;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

final class ServerStatusClient {
    private ServerStatusClient() {}

    static final String STATUS_URL = "https://onqam.com/wp-json/venas-otp/v1/status";

    static int check() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(STATUS_URL).openConnection();
        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);
            conn.setUseCaches(false);
            conn.setRequestProperty("User-Agent", "VenasHizliGiris-Android/1.1.0");
            int code = conn.getResponseCode();
            drain(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
            return code;
        } finally {
            conn.disconnect();
        }
    }

    private static void drain(InputStream input) {
        if (input == null) return;
        try (InputStream in = input) {
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) { }
        } catch (Exception ignored) { }
    }
}
