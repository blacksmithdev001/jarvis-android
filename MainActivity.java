package com.tugra.jarvis;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int SPEECH_REQUEST = 10;
    private static final int MIC_REQUEST = 11;
    private TextToSpeech tts;
    private TextView output;
    private EditText commandInput;
    private SharedPreferences memory;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        memory = getSharedPreferences("jarvis_memory", MODE_PRIVATE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(12, 18, 30));
        root.setPadding(28, 38, 28, 28);

        TextView title = new TextView(this);
        title.setText("JARVIS 3.0");
        title.setTextSize(30);
        title.setTextColor(Color.CYAN);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Sesli asistan • İlk Android sürümü");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);

        output = new TextView(this);
        output.setText("Merhaba! Mikrofona dokun veya bir komut yaz.\n\nÖrnek: 'Google'da uzay haberlerini ara' veya 'not al yarın kitap oku'.");
        output.setTextColor(Color.WHITE);
        output.setTextSize(17);
        output.setPadding(0, 35, 0, 24);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(output);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        commandInput = new EditText(this);
        commandInput.setHint("Komutunu yaz...");
        commandInput.setHintTextColor(Color.GRAY);
        commandInput.setTextColor(Color.WHITE);
        root.addView(commandInput);

        Button send = new Button(this);
        send.setText("KOMUTU ÇALIŞTIR");
        send.setOnClickListener(v -> { handle(commandInput.getText().toString()); commandInput.setText(""); });
        root.addView(send);

        Button listen = new Button(this);
        listen.setText("🎙 SESLİ KOMUT");
        listen.setOnClickListener(v -> listen());
        root.addView(listen);

        setContentView(root);
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(new Locale("tr", "TR"));
        });
    }

    private void listen() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQUEST);
            return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Seni dinliyorum");
        try { startActivityForResult(intent, SPEECH_REQUEST); }
        catch (ActivityNotFoundException e) { respond("Telefonunda ses tanıma servisi bulunamadı. Yazılı komut kullanabilirsin."); }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == MIC_REQUEST && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) listen();
        else if (requestCode == MIC_REQUEST) respond("Mikrofon izni verilmedi.");
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) handle(matches.get(0));
        }
    }

    private void respond(String message) {
        output.setText(message);
        if (tts != null) tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis-reply");
    }

    private void handle(String original) {
        String command = original.trim();
        String lower = command.toLowerCase(new Locale("tr", "TR"));
        if (command.isEmpty()) { respond("Lütfen bir komut söyle."); return; }
        if (lower.startsWith("not al ")) {
            String note = command.substring(7).trim();
            memory.edit().putString("last_note", note).apply();
            respond("Notunu kaydettim: " + note);
        } else if (lower.contains("notumu oku") || lower.contains("notlarımı oku")) {
            respond("Son notun: " + memory.getString("last_note", "Henüz bir notun yok."));
        } else if (lower.startsWith("google'da ") || lower.startsWith("google da ") || lower.startsWith("ara ")) {
            String query = command.replaceFirst("(?i)^(google'da |google da |ara )", "").replaceFirst("(?i) ara$", "").trim();
            if (query.isEmpty()) { respond("Ne aramamı istersin?"); return; }
            openUrl("https://www.google.com/search?q=" + Uri.encode(query));
            respond("İnternette arıyorum: " + query);
        } else if (lower.contains("youtube")) {
            openUrl("https://www.youtube.com");
            respond("YouTube'u açıyorum.");
        } else if (lower.contains("chrome") || lower.contains("tarayıcı")) {
            openUrl("https://www.google.com");
            respond("Tarayıcıyı açıyorum.");
        } else if (lower.contains("merhaba") || lower.contains("selam")) {
            respond("Merhaba! Ben JARVIS. Nasıl yardımcı olabilirim?");
        } else if (lower.contains("saat kaç")) {
            respond("Saat " + new java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(new java.util.Date()));
        } else {
            respond("Bu komutu henüz doğrudan çalıştıramıyorum. İnternet araması için 'ara ' ile başlayabilirsin.");
        }
    }

    private void openUrl(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (ActivityNotFoundException e) { respond("Bu bağlantıyı açacak uygulama bulunamadı."); }
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
