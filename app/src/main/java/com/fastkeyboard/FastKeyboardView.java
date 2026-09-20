package com.fastkeyboard;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

/**
 * Main keyboard surface for Reset 13.
 * Main-screen layout is intentionally fixed; extra functions live in Settings drawer.
 */
public class FastKeyboardView extends android.view.View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final InputConnection ic;
    private final int BLUE = Color.rgb(18, 86, 205);
    private final int CREAM = Color.rgb(255, 253, 245);
    private final ArrayList<String> history = new ArrayList<>();
    private boolean hideTop = false;
    private float transparency = 1f;

    public FastKeyboardView(Context context, InputConnection connection) {
        super(context);
        ic = connection;
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        c.drawColor(CREAM);
        final float w = getWidth();
        final float h = getHeight();

        // Reset 13 main layout: no inter-key spacing.
        float y = 0f;
        if (!hideTop) {
            float toolbarH = h * 0.155f;
            drawToolbar(c, y, toolbarH, w);
            y += toolbarH;
            float numberH = h * 0.135f;
            drawNumberRow(c, y, numberH, w);
            y += numberH;
        }

        float rowH = (h - y) / 4f;
        drawQRow(c, y, rowH, w); y += rowH;
        drawARow(c, y, rowH, w); y += rowH;
        drawZRow(c, y, rowH, w); y += rowH;
        drawBottom(c, y, h - y, w);
    }

    private void key(Canvas c, float l, float t, float r, float b, String text, float size) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(CREAM);
        p.setShadowLayer(2.2f, 0f, 1f, 0x33000000);
        c.drawRoundRect(new RectF(l, t, r, b), 8f, 8f, p);
        p.clearShadowLayer();
        text(c, text, (l + r) / 2f, (t + b) / 2f, size);
    }

    private void drawToolbar(Canvas c, float y, float rh, float w) {
        String[] a = {"Copy All", "Copy", "Paste", "Esc", "Undo", "Redo", "Copy\nScreen", "Settings", "12:30", "Hide\nTop Rows"};
        float cw = w / a.length;
        for (int i = 0; i < a.length; i++) key(c, i * cw, y, (i + 1) * cw, y + rh, a[i], 15f);
    }

    private void drawNumberRow(Canvas c, float y, float rh, float w) {
        String[] a = {"~\n`", "!\n1", "@\n2", "#\n3", "$\n4", "%\n5", "^\n6", "&\n7", "*\n8", "(\n9", ")\n0", "-\n_", "=\n+", "⌫"};
        float cw = w / a.length;
        for (int i = 0; i < a.length; i++) key(c, i * cw, y, (i + 1) * cw, y + rh, a[i], 20f);
    }

    private void drawQRow(Canvas c, float y, float rh, float w) {
        String[] a = {"Q","W","E","R","T","Y","U","I","O","P","{\n[","}\n]"};
        drawEqual(c, a, y, rh, w, 24f);
    }

    private void drawARow(Canvas c, float y, float rh, float w) {
        String[] a = {"A","S","D","F","G","H","J","K","L",";\n:", "\"\n'", "↵\nEnter"};
        drawEqual(c, a, y, rh, w, 24f);
    }

    private void drawZRow(Canvas c, float y, float rh, float w) {
        String[] a = {"↑","Z","X","C","V","B","N","M","<\n,", ">\n.", "?\n/", "↑"};
        drawEqual(c, a, y, rh, w, 24f);
    }

    private void drawEqual(Canvas c, String[] a, float y, float rh, float w, float size) {
        float cw = w / a.length;
        for (int i = 0; i < a.length; i++) key(c, i * cw, y, (i + 1) * cw, y + rh, a[i], size);
    }

    private void drawBottom(Canvas c, float y, float rh, float w) {
        // Symbols, globe, emoji, Space, four arrows. No transparency control on main page.
        float[] widths = {0.095f, 0.095f, 0.095f, 0.405f, 0.0775f, 0.0775f, 0.0775f, 0.0775f};
        String[] a = {"!@#\n$%^&*", "🌐", "Emoji", "Space", "←", "→", "↓", "↑"};
        float x = 0f;
        for (int i = 0; i < a.length; i++) {
            float nx = x + widths[i] * w;
            key(c, x, y, nx, y + rh, a[i], i == 3 ? 24f : 19f);
            x = nx;
        }
    }

    private void text(Canvas c, String s, float x, float y, float size) {
        p.setColor(BLUE);
        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        p.setTextSize(size * getResources().getDisplayMetrics().scaledDensity);
        p.setTextAlign(Paint.Align.CENTER);
        p.setAlpha(Math.max(1, Math.min(255, Math.round(255f * transparency))));
        String[] lines = s.split("\\n");
        float line = p.getTextSize() * 0.78f;
        float start = y - ((lines.length - 1) * line / 2f) - (p.ascent() + p.descent()) / 2f;
        for (String v : lines) {
            c.drawText(v, x, start, p);
            start += line;
        }
        p.setAlpha(255);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() != MotionEvent.ACTION_UP) return true;
        float x = e.getX(), y = e.getY(), w = getWidth(), h = getHeight();
        float toolbarH = hideTop ? 0f : h * 0.155f;
        float numberH = hideTop ? 0f : h * 0.135f;

        if (!hideTop && y < toolbarH) {
            int i = Math.min(9, (int) (x / (w / 10f)));
            switch (i) {
                case 0: copyAll(); break;
                case 1: copy(); break;
                case 2: paste(); break;
                case 3: toast("Esc"); break;
                case 4: undo(); break;
                case 5: redo(); break;
                case 6: toast("Copy Screen"); break;
                case 7: openDrawer(); break;
                case 9: hideTop = true; invalidate(); break;
            }
            return true;
        }

        if (!hideTop && y < toolbarH + numberH) {
            int i = Math.min(13, (int) (x / (w / 14f)));
            if (i == 13) backspace();
            else {
                String[] symbols = {"~","!","@","#","$","%","^","&","*","(",")","-","="};
                type(symbols[i]);
            }
            return true;
        }

        float rowH = (h - toolbarH - numberH) / 4f;
        int row = (int) ((y - toolbarH - numberH) / rowH);
        if (row < 0 || row > 3) return true;
        int i;
        if (row == 0) {
            i = Math.min(11, (int) (x / (w / 12f)));
            if (i < 10) type("QWERTYUIOP".substring(i, i + 1));
            else type(i == 10 ? "{" : "}");
        } else if (row == 1) {
            i = Math.min(11, (int) (x / (w / 12f)));
            if (i < 9) type("ASDFGHJKL".substring(i, i + 1));
            else if (i == 9) type(";");
            else if (i == 10) type("\"");
            else enter();
        } else if (row == 2) {
            i = Math.min(11, (int) (x / (w / 12f)));
            if (i == 0 || i == 11) toast("Shift");
            else if (i <= 7) type("ZXCVBNM".substring(i - 1, i));
            else if (i == 8) type("<");
            else if (i == 9) type(">");
            else type("?");
        } else {
            handleBottom(x, w);
        }
        return true;
    }

    private void handleBottom(float x, float w) {
        float[] widths = {0.095f, 0.095f, 0.095f, 0.405f, 0.0775f, 0.0775f, 0.0775f, 0.0775f};
        float s = 0f;
        for (int i = 0; i < widths.length; i++) {
            float n = s + widths[i] * w;
            if (x >= s && x < n) {
                if (i == 3) type(" ");
                else if (i >= 4) type(new String[]{"←","→","↓","↑"}[i - 4]);
                else if (i == 2) showEmoji();
                else if (i == 1) toast("زبان");
                else toast("علائم");
                return;
            }
            s = n;
        }
    }

    private void type(String s) { if (ic != null) ic.commitText(s, 1); }
    private void enter() { if (ic != null) ic.sendKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER)); }
    private void backspace() { if (ic != null) ic.deleteSurroundingText(1, 0); }
    private void undo() { if (ic != null) ic.performContextMenuAction(android.R.id.undo); }
    private void redo() { if (ic != null) ic.performContextMenuAction(android.R.id.redo); }

    private void copy() {
        if (ic == null) return;
        CharSequence s = ic.getSelectedText(0);
        if (s != null) saveHistory(s.toString());
    }

    private void copyAll() {
        if (ic == null) return;
        ic.performContextMenuAction(android.R.id.selectAll);
        CharSequence s = ic.getSelectedText(0);
        if (s != null) saveHistory(s.toString());
    }

    private void paste() {
        if (ic == null) return;
        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null && cm.hasPrimaryClip()) ic.commitText(cm.getPrimaryClip().getItemAt(0).coerceToText(getContext()), 1);
    }

    private void saveHistory(String s) {
        if (s == null || s.isEmpty()) return;
        history.remove(s);
        history.add(0, s);
        while (history.size() > 100) history.remove(100);
        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("Fast Keyboard", s));
    }

    /** Settings is the existing main key; it opens the drawer without adding any main-page control. */
    private void openDrawer() {
        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(16, 8, 16, 8);
        TextView title = new TextView(getContext());
        title.setText("گزینه‌های کشویی");
        title.setTextColor(BLUE);
        title.setTextSize(19);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        box.addView(title);

        Button transparencyButton = new Button(getContext());
        transparencyButton.setText("Transparency Roller 1–100%");
        box.addView(transparencyButton);
        transparencyButton.setOnClickListener(v -> showTransparency());

        Button historyButton = new Button(getContext());
        historyButton.setText("Copy History — 100 آخر کلیپ‌بورد");
        box.addView(historyButton);
        historyButton.setOnClickListener(v -> showHistory());

        Button emojiButton = new Button(getContext());
        emojiButton.setText("Make Emoji");
        box.addView(emojiButton);
        emojiButton.setOnClickListener(v -> showEmoji());

        Button carButton = new Button(getContext());
        carButton.setText("فرمان ماشین — Widget");
        box.addView(carButton);
        carButton.setOnClickListener(v -> toast("فرمان ماشین از Widget اجرا می‌شود"));

        new AlertDialog.Builder(getContext())
                .setView(box)
                .setNegativeButton("بستن", null)
                .show();
    }

    private void showTransparency() {
        SeekBar sb = new SeekBar(getContext());
        sb.setMax(99);
        sb.setProgress(Math.max(0, Math.min(99, Math.round(transparency * 99f))));
        new AlertDialog.Builder(getContext())
                .setTitle("Transparency 1–100%")
                .setMessage("رول را بالا و پایین حرکت دهید")
                .setView(sb)
                .setPositiveButton("OK", (d, which) -> {
                    transparency = (sb.getProgress() + 1) / 100f;
                    invalidate();
                })
                .setNegativeButton("لغو", null)
                .show();
    }

    private void showHistory() {
        String[] a = history.toArray(new String[0]);
        if (a.length == 0) a = new String[]{"(خالی)"};
        new AlertDialog.Builder(getContext()).setTitle("Copy History — 100 آخر")
                .setItems(a, (d, which) -> { if (which < history.size() && ic != null) ic.commitText(history.get(which), 1); })
                .setNegativeButton("بستن", null).show();
    }

    private void showEmoji() {
        new AlertDialog.Builder(getContext()).setTitle("Make Emoji")
                .setMessage("پنجره ساخت Emoji؛ انتخاب تصویر، گرفتن عکس یا افزودن تصویر در این بخش قرار می‌گیرد.")
                .setPositiveButton("بستن", null).show();
    }

    private void toast(String s) { Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show(); }
}
