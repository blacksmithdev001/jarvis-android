# JARVIS Android

Android için ilk JARVIS prototipi (v0.1.0).

## Özellikler
- Türkçe ses tanıma (cihazda yüklü konuşma tanıma servisi gerekir)
- Türkçe metin-ses yanıtı (cihaz TTS servisi gerekir)
- Google web araması ve tarayıcı açma
- Basit yerel not saklama ve okuma
- Saat söyleme

Bu sürüm **tam çevrimdışı yapay zekâ, sürekli dinleme, sistem düzeyi kontrol veya sınırsız Chrome erişimi içermez**. Mikrofon sadece düğmeye basılınca çalışır.

## APK oluşturma
1. Kaynak dosyaları GitHub deposunun köküne, klasör yapısını koruyarak yükle.
2. GitHub > Actions > Android APK > Run workflow (veya main dalına push).
3. Başarılı derlemenin Artifacts bölümünden `jarvis-debug-apk` dosyasını indir.
4. ZIP içindeki `app-debug.apk` dosyasını telefona kur.

GitHub Actions derlemesi henüz doğrulanmadı. Android projesi Gradle 8.9, Android Gradle Plugin 8.7.3 ve Java 17 kullanır.
