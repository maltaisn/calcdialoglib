/*
 * Copyright (c) 2018 Nicolas Maltais
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.nmaltais.calcdialoglib;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.nmaltais.calcdialog.CalcDialog;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity implements CalcDialog.CalcDialogCallback {

    private TextView valueTextv;
    private CheckBox signCheck;
    private CheckBox signHideBtn;

    private @Nullable BigDecimal value;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        if (state != null) {
            String valueStr = state.getString("value");
            if (valueStr != null) {
                value = new BigDecimal(valueStr);
            }
        }

        final CalcDialog calcDialog = new CalcDialog();

        signCheck = findViewById(R.id.check_sign);
        if (value == null) signCheck.setEnabled(false);
        signHideBtn = findViewById(R.id.check_sign_hide_btn);

        final CheckBox showAnswerCheck = findViewById(R.id.check_answer_btn);
        final CheckBox clearOnOpCheck = findViewById(R.id.check_clear_operation);
        final CheckBox showZeroCheck = findViewById(R.id.check_show_zero);
        final CheckBox stripZeroCheck = findViewById(R.id.check_strip_zeroes);

        // Max value
        final CheckBox maxValCheck = findViewById(R.id.check_max_value);
        final EditText maxValEditt = findViewById(R.id.editt_max_value);
        maxValCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maxValEditt.setEnabled(isChecked);
            }
        });
        maxValEditt.setEnabled(maxValCheck.isChecked());
        maxValEditt.setText(String.valueOf(10000000000L));

        // Max integer digits
        final CheckBox maxIntCheck = findViewById(R.id.check_max_int);
        final EditText maxIntEditt = findViewById(R.id.editt_max_int);
        maxIntCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maxIntEditt.setEnabled(isChecked);
            }
        });
        maxIntEditt.setEnabled(maxIntCheck.isChecked());
        maxIntEditt.setText(String.valueOf(10));

        // Max fractional digits
        final CheckBox maxFracCheck = findViewById(R.id.check_max_frac);
        final EditText maxFracEditt = findViewById(R.id.editt_max_frac);
        maxIntCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maxFracEditt.setEnabled(isChecked);
            }
        });
        maxFracEditt.setEnabled(maxFracCheck.isChecked());
        maxFracEditt.setText(String.valueOf(8));

        // Value display
        valueTextv = findViewById(R.id.textv_result);
        valueTextv.setText(value == null ? getString(R.string.result_value_none) : value.toPlainString());

        // Open dialog button
        Button openBtn = findViewById(R.id.btn_open);
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean signCanBeChanged = !signCheck.isEnabled() || signCheck.isChecked();

                String maxValueStr = maxValEditt.getText().toString();
                BigDecimal maxValue = maxValCheck.isChecked() && !maxValueStr.isEmpty() ?
                        new BigDecimal(maxValueStr) : null;

                String maxIntStr = maxIntEditt.getText().toString();
                int maxInt = maxIntCheck.isChecked() && !maxIntStr.isEmpty() ?
                        Integer.valueOf(maxIntStr) : CalcDialog.MAX_DIGITS_UNLIMITED;

                String maxFracStr = maxFracEditt.getText().toString();
                int maxFrac = maxFracCheck.isChecked() && !maxFracStr.isEmpty() ?
                        Integer.valueOf(maxFracStr) : CalcDialog.MAX_DIGITS_UNLIMITED;

                // Set settings and value
                calcDialog.setValue(value)
                        .setShowAnswerButton(showAnswerCheck.isChecked())
                        .setHideSignButton(signHideBtn.isChecked())
                        .setSignCanBeChanged(signCanBeChanged, signCanBeChanged ? 0 : value.signum())
                        .setClearDisplayOnOperation(clearOnOpCheck.isChecked())
                        .setShowZeroWhenNoValue(showZeroCheck.isChecked())
                        .setStripTrailingZeroes(stripZeroCheck.isChecked())
                        .setMaxValue(maxValue)
                        .setMaxDigits(maxInt, maxFrac);

                FragmentManager fm = getSupportFragmentManager();
                if (fm.findFragmentByTag("calc_dialog") == null) {
                    calcDialog.show(fm, "calc_dialog");
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        if (value != null) {
            state.putString("value", value.toString());
        }
    }

    @Override
    public void onValueEntered(int reference, BigDecimal value) {
        this.value = value;

        valueTextv.setText(value.toPlainString());
        signCheck.setEnabled(value.compareTo(BigDecimal.ZERO) != 0);
    }
}

