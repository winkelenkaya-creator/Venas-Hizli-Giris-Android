# Venas Hızlı Giriş Android v1.1.0

Telefonun bildirim erişimini kullanarak İş Turkcell Hızlı Giriş mesajını tanır, `Hızlı Giriş şifreniz` ifadesinden sonraki 6 haneli kodu ve `Referans:` bilgisini telefonda gösterir.

## Güvenlik tasarımı
- OTP ve referans cihazdan dışarı gönderilmez.
- Kod en fazla 180 saniye yerel SharedPreferences içinde tutulur, sonra otomatik temizlenir.
- `Kodu Kopyala` ile panoya alınan kod 60 saniye sonra, pano hâlâ aynı kodu içeriyorsa temizlenir.
- `onqam.com` düğmesi yalnız `https://onqam.com/wp-json/venas-otp/v1/status` adresine GET yaparak erişilebilirliği test eder.
- SMS okuma izni istemez; Notification Access kullanır.

## İlk kullanım
1. APK'yı yükleyin.
2. `Bildirim Erişimini Aç` düğmesine dokunun.
3. Android ayarlarında `Venas Hızlı Giriş` için bildirim erişimine izin verin.
4. İş Turkcell Hızlı Giriş SMS bildirimi geldiğinde uygulamayı açın.
5. 6 haneli kod ve referans görünür; `Kodu Kopyala` kullanılabilir.

## GitHub Actions
`.github/workflows/build-apk.yml` her `main` push'unda ve manuel çalıştırmada installable debug APK üretir ve artifact olarak yükler.
