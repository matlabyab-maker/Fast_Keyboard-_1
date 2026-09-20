package com.fastkeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;

public class FastInputMethodService extends InputMethodService {
    @Override public View onCreateInputView() {
        return new FastKeyboardView(this, getCurrentInputConnection());
    }
    @Override public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
    }
}
