package com.fastkeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup;

public class FastInputMethodService extends InputMethodService {
    private View keyboardView;

    // Keep the IME in the normal keyboard window instead of fullscreen/extract mode.
    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Window w = getWindow().getWindow();
        if (w != null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private int getKeyboardHeight() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        return Math.round(screenHeight * 0.34f);
    }

    @Override
    public View onCreateInputView() {
        FastKeyboardView view = new FastKeyboardView(this, getCurrentInputConnection());
        keyboardView = view;
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, getKeyboardHeight()));
        return view;
    }

    @Override
    public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        // InputMethodService does not provide getInputView(). Keep the reference
        // returned by onCreateInputView() instead.
        if (keyboardView != null) {
            ViewGroup.LayoutParams lp = keyboardView.getLayoutParams();
            if (lp == null) {
                lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, getKeyboardHeight());
            } else {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = getKeyboardHeight();
            }
            keyboardView.setLayoutParams(lp);
        }
    }
}
