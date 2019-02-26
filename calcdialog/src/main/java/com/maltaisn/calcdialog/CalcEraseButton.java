/*
 * Copyright 2019 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.calcdialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * ImageView that triggers erase events when held down
 * Attributes:
 * - eraseBtnHoldDelay: Time view has to be held down to trigger quick erase (in ms)
 * Default value is 750ms. Use -1 for no quick erase and 0 for no delay
 * - eraseBtnHoldSpeed: Time after which an erase event is triggered in quick erase mode (in ms)
 * Default value is 100ms
 * - eraseAllOnHold: If true, holding button will trigger an erase all event instead of quick
 * erase mode if false. By default this is false.
 */
class CalcEraseButton extends AppCompatImageView {

    private static final String TAG = CalcEraseButton.class.getSimpleName();

    private static final int NO_HOLD_ERASE = -1;

    private int eraseHoldDelay;
    private int eraseHoldSpeed;
    private boolean eraseAllOnHold;

    private final Handler eraseHandler;
    private final Runnable eraseRunnable;
    private boolean clickingDown;

    private @Nullable EraseListener listener;

    public CalcEraseButton(Context context) {
        this(context, null, 0);
    }

    public CalcEraseButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalcEraseButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Get speed attributes
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CalcEraseButton);
        eraseHoldDelay = ta.getInt(R.styleable.CalcEraseButton_calcEraseBtnHoldDelay, 750);
        eraseHoldSpeed = ta.getInt(R.styleable.CalcEraseButton_calcEraseBtnHoldSpeed, 100);
        eraseAllOnHold = ta.getBoolean(R.styleable.CalcEraseButton_calcEraseAllOnHold, false);
        ta.recycle();

        eraseHandler = new Handler();
        eraseRunnable = new Runnable() {
            @Override
            public void run() {
                if (listener != null && clickingDown) {
                    if (eraseAllOnHold) {
                        listener.onEraseAll();
                    } else {
                        listener.onErase();
                        eraseHandler.postDelayed(eraseRunnable, eraseHoldSpeed);
                    }
                }
            }
        };
    }

    public interface EraseListener {
        void onErase();
        void onEraseAll();
    }

    public void setOnEraseListener(@Nullable EraseListener listener) {
        this.listener = listener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean superReturn = super.onTouchEvent(event);  // does performClick(), so ignore warning

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (listener != null && eraseHoldDelay != NO_HOLD_ERASE) {
                eraseHandler.removeCallbacks(eraseRunnable);
            }
            clickingDown = false;
            return true;

        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            clickingDown = true;

            if (listener != null) {
                if (eraseHoldDelay != NO_HOLD_ERASE) {
                    eraseHandler.postDelayed(eraseRunnable, eraseHoldDelay);
                    eraseHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (clickingDown) {
                                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            }
                        }
                    }, eraseHoldDelay);
                }

                if (eraseHoldDelay != 0) {
                    listener.onErase();
                }
            }
            return true;
        }

        return superReturn;
    }

}
