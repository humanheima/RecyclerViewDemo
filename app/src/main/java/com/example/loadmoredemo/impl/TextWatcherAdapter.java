package com.example.loadmoredemo.impl;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

/**
 * Created by dumingwei on 2017/9/1.
 */
public class TextWatcherAdapter implements TextWatcher {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.e(TAG, "afterTextChanged s=" + s);
    }
}
