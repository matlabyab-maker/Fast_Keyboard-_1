package com.fastkeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class FastInputMethodService extends InputMethodService {
    // Keep the IME in the normal keyboard window instead of fullscreen/extract mode.
    @Override public boolean onEvaluateFullscreenMode() {
        return false;
    }

    @Override public void onCreate() {
        super.onCreate();
        Window w = getWindow().getWindow();
        if (w != null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override public View onCreateInputView() {
        FastKeyboardView view = new FastKeyboardView(this, getCurrentInputConnection());
        // Use a normal keyboard height: about 34% of the usable display height.
        // This prevents the custom view from expanding over the whole screen.
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int keyboardHeight = Math.round(screenHeight * 0.34f);
        view.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight));
        return view;
    }

    @Override public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        View view = getInputView();
        if (view != null) {
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int keyboardHeight = Math.round(screenHeight * 0.34f);
            android.view.ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp != null) {
                lp.height = keyboardHeight;
                lp.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
                view.setLayoutParams(lp);
            }
        }
    }
}
