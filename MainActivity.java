package com.venas.hizligiris;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView listenerStatus;
    private TextView otpView;
    private TextView refView;
    private TextView timeView;
    private TextView serverView;
    private Button copyButton;
    private String visibleOtp = "";

    private final Runnable refresher = new Runnable() {
        @Override public void run() {
            refresh();
            handler.postDelayed(this, 1000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildUi());
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(refresher);
        handler.post(refresher);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(refresher);
        super.onPause();
    }

    private View buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(5, 7, 10));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(28));
        scroll.addView(root);

        ImageView logo = new ImageView(this);
        logo.setImageResource(com.venas.hizligiris.R.drawable.venas_logo);
        logo.setAdjustViewBounds(true);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(118), dp(118));
        logoLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(logo, logoLp);

        TextView title = text("Venas Hızlı Giriş", 26, true, Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(10), 0, dp(4));
        root.addView(title, matchWrap());

        TextView subtitle = text("İş Turkcell Hızlı Giriş kod yardımcısı", 15, false, Color.rgb(170, 182, 201));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, dp(22));
        root.addView(subtitle, matchWrap());

        listenerStatus = text("", 15, true, Color.WHITE);
        root.addView(card("Bildirim erişimi", listenerStatus));

        Button access = button("BİLDİRİM ERİŞİMİNİ AÇ");
        access.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, "Bildirim erişimi ayarları açılamadı.", Toast.LENGTH_LONG).show();
            }
        });
        root.addView(access, buttonLp());

        TextView codeLabel = text("Son Hızlı Giriş Kodu", 14, true, Color.rgb(170, 182, 201));
        codeLabel.setPadding(dp(16), dp(18), dp(16), dp(2));
        root.addView(codeLabel, matchWrap());

        otpView = text("------", 42, true, Color.WHITE);
        otpView.setGravity(Gravity.CENTER);
        otpView.setLetterSpacing(0.12f);
        otpView.setPadding(dp(10), dp(10), dp(10), dp(10));
        root.addView(otpView, matchWrap());

        refView = text("Referans: —", 16, true, Color.rgb(84, 155, 255));
        refView.setGravity(Gravity.CENTER);
        root.addView(refView, matchWrap());

        timeView = text("Yeni kod bekleniyor", 13, false, Color.rgb(150, 160, 176));
        timeView.setGravity(Gravity.CENTER);
        timeView.setPadding(0, dp(6), 0, dp(14));
        root.addView(timeView, matchWrap());

        copyButton = button("KODU KOPYALA");
        copyButton.setEnabled(false);
        copyButton.setOnClickListener(v -> copyOtp());
        root.addView(copyButton, buttonLp());

        serverView = text("Henüz test edilmedi", 14, false, Color.rgb(170, 182, 201));
        root.addView(card("onqam.com durumu", serverView));

        Button test = button("SUNUCUYU TEST ET");
        test.setOnClickListener(v -> testServer());
        root.addView(test, buttonLp());

        TextView security = text(
                "Güvenlik: 6 haneli kod yalnızca bu telefonda tutulur. Uygulama OTP kodunu veya referansı internete göndermez. onqam.com testi yalnızca durum adresine GET isteği yapar.",
                13, false, Color.rgb(145, 156, 174));
        security.setPadding(dp(8), dp(18), dp(8), 0);
        root.addView(security, matchWrap());

        return scroll;
    }

    private void refresh() {
        listenerStatus.setText(isListenerEnabled() ? "Aktif ✓" : "Kapalı — izin gerekli");
        listenerStatus.setTextColor(isListenerEnabled() ? Color.rgb(89, 219, 143) : Color.rgb(255, 178, 85));

        OtpStore.Entry entry = OtpStore.current(this);
        if (entry == null) {
            visibleOtp = "";
            otpView.setText("------");
            refView.setText("Referans: —");
            timeView.setText("Yeni kod bekleniyor");
            copyButton.setEnabled(false);
            return;
        }

        visibleOtp = entry.otp;
        otpView.setText(entry.otp);
        refView.setText(TextUtils.isEmpty(entry.reference) ? "Referans: bulunamadı" : "Referans: " + entry.reference);
        String stamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(entry.timestamp));
        long left = Math.max(0L, 180L - ((System.currentTimeMillis() - entry.timestamp) / 1000L));
        timeView.setText("Algılandı: " + stamp + "  •  " + left + " sn sonra temizlenir");
        copyButton.setEnabled(true);
    }

    private void copyOtp() {
        if (visibleOtp.length() != 6) return;
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null) return;
        cm.setPrimaryClip(ClipData.newPlainText("Venas Hızlı Giriş", visibleOtp));
        Toast.makeText(this, "Kod panoya kopyalandı.", Toast.LENGTH_SHORT).show();

        final String copied = visibleOtp;
        handler.postDelayed(() -> {
            try {
                if (cm.hasPrimaryClip() && cm.getPrimaryClip() != null && cm.getPrimaryClip().getItemCount() > 0) {
                    CharSequence text = cm.getPrimaryClip().getItemAt(0).coerceToText(this);
                    if (copied.contentEquals(text)) cm.clearPrimaryClip();
                }
            } catch (Exception ignored) { }
        }, 60_000L);
    }

    private void testServer() {
        serverView.setText("Test ediliyor…");
        new Thread(() -> {
            try {
                int code = ServerStatusClient.check();
                runOnUiThread(() -> {
                    if (code >= 200 && code < 300) {
                        serverView.setText("Erişilebilir ✓  HTTP " + code);
                        serverView.setTextColor(Color.rgb(89, 219, 143));
                    } else {
                        serverView.setText("Yanıt alındı: HTTP " + code);
                        serverView.setTextColor(Color.rgb(255, 178, 85));
                    }
                });
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg == null || msg.trim().isEmpty()) msg = e.getClass().getSimpleName();
                final String finalMsg = msg;
                runOnUiThread(() -> {
                    serverView.setText("Bağlantı hatası: " + finalMsg);
                    serverView.setTextColor(Color.rgb(255, 113, 113));
                });
            }
        }, "VenasStatusTest").start();
    }

    private boolean isListenerEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (enabled == null) return false;
        ComponentName me = new ComponentName(this, OtpNotificationListener.class);
        return enabled.contains(me.flattenToString()) || enabled.contains(getPackageName());
    }

    private LinearLayout card(String label, TextView value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(14), dp(16), dp(14));
        box.setBackgroundColor(Color.rgb(18, 24, 34));

        TextView labelView = text(label, 12, true, Color.rgb(137, 150, 171));
        box.addView(labelView, matchWrap());
        value.setPadding(0, dp(5), 0, 0);
        box.addView(value, matchWrap());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, dp(8));
        box.setLayoutParams(lp);
        return box;
    }

    private TextView text(String text, int sp, boolean bold, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return tv;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(35, 104, 224)));
        b.setAllCaps(false);
        return b;
    }

    private LinearLayout.LayoutParams buttonLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        lp.setMargins(0, dp(8), 0, dp(10));
        return lp;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
