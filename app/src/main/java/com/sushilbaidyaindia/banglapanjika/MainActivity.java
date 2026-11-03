package com.sushilbaidyaindia.banglapanjika;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import java.util.Calendar;
import java.util.Locale;

/** Step 1: the shell. Three tabs, the day page (partly dummy), copy and read-aloud. */
public class MainActivity extends AppCompatActivity {

    private static final int BG = 0xFFFFF8EF;
    private static final int INK = 0xFF2B1B0E;
    private static final int MUTED = 0xFF5A4636;
    private static final int ACCENT = 0xFF8E1B1B;
    private static final int CARD = 0xFFFFFFFF;
    private static final String[] TAB_NAMES = {"আজ", "সার্চ", "উৎসব ও সেটিংস"};

    private SharedPreferences prefs;
    private final Calendar shown = Calendar.getInstance();

    private final View[] pages = new View[3];
    private final TextView[] tabs = new TextView[3];
    private final TextView[] pageHeads = new TextView[3];
    private int tab = 0;

    private LinearLayout dayBox;
    private Button readBtn;

    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean reading = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("panjika", MODE_PRIVATE);
        if (b != null) tab = b.getInt("tab", 0);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        FrameLayout content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        pages[0] = buildDayPage();
        pages[1] = buildSearchPage();
        pages[2] = buildSettingsPage();
        for (View p : pages) content.addView(p, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            TextView t = new TextView(this);
            t.setText(TAB_NAMES[i]);
            t.setTextSize(17);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setGravity(Gravity.CENTER);
            t.setMinHeight(dp(60));
            t.setClickable(true);
            t.setFocusable(true);
            t.setOnClickListener(v -> select(idx, true));
            tabs[i] = t;
            bar.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        root.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(root);

        renderDay();
        select(tab, false);

        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.SUCCESS) return;
            int r = tts.setLanguage(new Locale("bn", "IN"));
            if (bad(r)) r = tts.setLanguage(new Locale("bn", "BD"));
            if (bad(r)) r = tts.setLanguage(new Locale("bn"));
            ttsReady = !bad(r);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String id) { }
                @Override public void onDone(String id) { runOnUiThread(() -> setReading(false)); }
                @Override public void onError(String id) { runOnUiThread(() -> setReading(false)); }
            });
        });
    }

    private static boolean bad(int r) {
        return r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED;
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt("tab", tab);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            try { tts.stop(); tts.shutdown(); } catch (Throwable ignored) { }
        }
        super.onDestroy();
    }

    // ------------------------------------------------------------------ tabs

    private void select(int i, boolean moveFocus) {
        tab = i;
        for (int k = 0; k < 3; k++) {
            boolean on = k == i;
            pages[k].setVisibility(on ? View.VISIBLE : View.GONE);
            tabs[k].setBackgroundColor(on ? ACCENT : CARD);
            tabs[k].setTextColor(on ? Color.WHITE : ACCENT);
            tabs[k].setSelected(on);
            tabs[k].setContentDescription(TAB_NAMES[k] + ", ট্যাব " + BanglaText.digits(k + 1) + " এর ৩" + (on ? ", নির্বাচিত" : ""));
        }
        if (moveFocus && pageHeads[i] != null) {
            final TextView h = pageHeads[i];
            h.post(() -> h.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null));
        }
    }

    // ------------------------------------------------------------------ tab 1: the day page

    private View buildDayPage() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);

        ScrollView sv = new ScrollView(this);
        dayBox = new LinearLayout(this);
        dayBox.setOrientation(LinearLayout.VERTICAL);
        dayBox.setPadding(dp(16), dp(16), dp(16), dp(8));
        sv.addView(dayBox);
        page.addView(sv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(dp(12), dp(8), dp(12), dp(8));
        readBtn = button("পড়ে শোনাও");
        readBtn.setOnClickListener(v -> toggleRead());
        Button copy = button("কপি");
        copy.setOnClickListener(v -> copyDay());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(4), 0, dp(4), 0);
        actions.addView(readBtn, lp);
        actions.addView(copy, lp);
        page.addView(actions, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return page;
    }

    private DayInfo currentDay() {
        return new DayInfo(shown, prefs.getString("method", DayInfo.DRIK));
    }

    private void renderDay() {
        DayInfo d = currentDay();
        dayBox.removeAllViews();

        TextView head = text(d.headline(), 24, true, ACCENT);
        ViewCompat.setAccessibilityHeading(head, true);
        head.setPadding(0, 0, 0, dp(12));
        dayBox.addView(head);
        pageHeads[0] = head;

        for (String[] row : d.rows()) {
            SpannableStringBuilder sb = new SpannableStringBuilder(row[0] + ": ");
            sb.setSpan(new StyleSpan(Typeface.BOLD), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.append(row[1]);
            TextView t = text("", 18, false, INK);
            t.setText(sb);
            t.setPadding(dp(14), dp(12), dp(14), dp(12));
            t.setBackground(card());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(8));
            dayBox.addView(t, lp);
        }
    }

    private void copyDay() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("পঞ্জিকা", currentDay().toText()));
        Toast.makeText(this, "কপি হয়েছে", Toast.LENGTH_SHORT).show();
    }

    private void toggleRead() {
        if (reading) {
            if (tts != null) tts.stop();
            setReading(false);
            return;
        }
        if (tts == null || !ttsReady) {
            noVoiceDialog();
            return;
        }
        setReading(true);
        tts.speak(currentDay().toText(), TextToSpeech.QUEUE_FLUSH, null, "day");
    }

    private void setReading(boolean on) {
        reading = on;
        readBtn.setText(on ? "থামান" : "পড়ে শোনাও");
    }

    private void noVoiceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("বাংলা ভয়েস পাওয়া যায়নি")
                .setMessage("এই ফোনের টেক্সট-টু-স্পিচে বাংলা ভয়েস নেই বা এখনো তৈরি হয়নি। ভয়েস ইনস্টল করে আবার চেষ্টা করুন।")
                .setPositiveButton("ভয়েস ইনস্টল করুন", (dlg, w) -> {
                    try {
                        startActivity(new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, "ফোনের সেটিংসে ভাষা → টেক্সট-টু-স্পিচ-এ গিয়ে বাংলা ভয়েস নিন", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("থাক", null)
                .show();
    }

    // ------------------------------------------------------------------ tab 2: search (step 2 makes it work)

    private View buildSearchPage() {
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(16), dp(16), dp(16));
        sv.addView(box);

        TextView head = text("তারিখ বা উৎসব খুঁজুন", 24, true, ACCENT);
        ViewCompat.setAccessibilityHeading(head, true);
        box.addView(head);
        pageHeads[1] = head;

        final EditText q = new EditText(this);
        q.setHint("যেমন: আজ, 17 March, ১৭ কার্তিক, দুর্গা");
        q.setTextSize(18);
        q.setInputType(InputType.TYPE_CLASS_TEXT);
        q.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        q.setMinHeight(dp(56));
        box.addView(q, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final TextView status = text("সার্চ পরের ধাপে চালু হবে। এখন আজকের পাতা দেখতে \"আজ\" ট্যাবে যান।", 16, false, MUTED);
        status.setPadding(0, dp(12), 0, 0);

        Button go = button("খুঁজুন");
        go.setOnClickListener(v -> status.setText("সার্চ এখনো চালু হয়নি, এটা দ্বিতীয় ধাপে আসছে।"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, 0);
        box.addView(go, lp);
        box.addView(status);
        return sv;
    }

    // ------------------------------------------------------------------ tab 3: festivals + settings

    private View buildSettingsPage() {
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(16), dp(16), dp(16));
        sv.addView(box);

        TextView head = text("উৎসব ও সেটিংস", 24, true, ACCENT);
        ViewCompat.setAccessibilityHeading(head, true);
        box.addView(head);
        pageHeads[2] = head;

        box.addView(heading("পঞ্জিকা পদ্ধতি"));
        RadioGroup rg = new RadioGroup(this);
        final RadioButton drik = radio("দৃকসিদ্ধান্ত");
        final RadioButton surya = radio("সূর্যসিদ্ধান্ত");
        rg.addView(drik);
        rg.addView(surya);
        if (DayInfo.SURYA.equals(prefs.getString("method", DayInfo.DRIK))) surya.setChecked(true);
        else drik.setChecked(true);
        rg.setOnCheckedChangeListener((g, id) -> {
            String m = id == surya.getId() ? DayInfo.SURYA : DayInfo.DRIK;
            prefs.edit().putString("method", m).apply();
            renderDay();
            Toast.makeText(this, "পঞ্জিকা পদ্ধতি: " + DayInfo.methodName(m), Toast.LENGTH_SHORT).show();
        });
        box.addView(rg);

        box.addView(heading("উৎসবের তালিকা"));
        box.addView(text("বছরের প্রধান উৎসব আর পূজার তালিকা পরের ধাপগুলোতে আসবে।", 17, false, INK));

        box.addView(heading("অ্যাপ সম্পর্কে"));
        box.addView(text("বাংলা পঞ্জিকা, সংস্করণ ০.১ (প্রথম ধাপ)। তারিখ, তিথি আর উৎসব ইন্টারনেট ছাড়াই চলবে। উইকিপিডিয়ার লিংক শুধু অনলাইনে খুলবে।", 17, false, INK));
        return sv;
    }

    // ------------------------------------------------------------------ small helpers

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private TextView text(String s, int sp, boolean bold, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private TextView heading(String s) {
        TextView t = text(s, 20, true, INK);
        ViewCompat.setAccessibilityHeading(t, true);
        t.setPadding(0, dp(20), 0, dp(6));
        return t;
    }

    private Button button(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(18);
        b.setMinHeight(dp(56));
        b.setAllCaps(false);
        return b;
    }

    private RadioButton radio(String s) {
        RadioButton r = new RadioButton(this);
        r.setId(View.generateViewId());
        r.setText(s);
        r.setTextSize(18);
        r.setMinHeight(dp(56));
        return r;
    }

    private GradientDrawable card() {
        GradientDrawable g = new GradientDrawable();
        g.setColor(CARD);
        g.setCornerRadius(dp(12));
        g.setStroke(dp(1), 0xFFE6D8C6);
        return g;
    }
}
