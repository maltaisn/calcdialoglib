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
package com.maltaisn.calcdialoglib.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    //    private static final String TAG = MainActivity.class.getSimpleName();
    //
    //    private TextView valueTxv;
    //
    //    @Nullable
    //    private BigDecimal value;
    //
    //    @NonNull
    //    private NumberFormat nbFmt = NumberFormat.getInstance();
    //
    //    @Override
    //    protected void onCreate(Bundle state) {
    //        super.onCreate(state);
    //        setContentView(R.layout.activity_main);
    //
    //        if (state != null && state.containsKey("value")) {
    //            value = (BigDecimal) state.getSerializable("value");
    //        }
    //
    //        final CalcDialog calcDialog = new CalcDialog();
    //
    //        // CALCULATOR SETTINGS
    //        final CheckBox showExprChk = findViewById(R.id.chk_show_expr);
    //        final CheckBox exprEditableChk = findViewById(R.id.chk_expr_editable);
    //        final CheckBox showAnswerBtnChk = findViewById(R.id.chk_show_answer_btn);
    //        final CheckBox showSignBtnChk = findViewById(R.id.chk_show_sign_btn);
    //        final CheckBox applyOrderOpChk = findViewById(R.id.chk_order_operation);
    //        final CheckBox evalOnOperationChk = findViewById(R.id.chk_eval_operation);
    //        final CheckBox showZeroChk = findViewById(R.id.chk_show_zero);
    //
    //        // Min value
    //        final CheckBox minValChk = findViewById(R.id.chk_min_value);
    //        final EditText minValEdt = findViewById(R.id.edt_min_value);
    //        minValChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    //            @Override
    //            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    //                minValEdt.setEnabled(isChecked);
    //            }
    //        });
    //        minValEdt.setEnabled(minValChk.isChecked());
    //        minValEdt.setText(new BigDecimal("-1E10").toPlainString());
    //
    //        // Max value
    //        final CheckBox maxValChk = findViewById(R.id.chk_max_value);
    //        final EditText maxValEdt = findViewById(R.id.edt_max_value);
    //        maxValChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    //            @Override
    //            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    //                maxValEdt.setEnabled(isChecked);
    //            }
    //        });
    //        maxValEdt.setEnabled(maxValChk.isChecked());
    //        maxValEdt.setText(new BigDecimal("1E10").toPlainString());
    //
    //        // Numpad layout
    //        final RadioGroup numpadLayoutGroup = findViewById(R.id.radiogroup_numpad);
    //
    //        // NUMBER FORMAT SETTINGS
    //        final RadioGroup nbFmtTypeGroup = findViewById(R.id.radiogroup_nbfmt);
    //
    //        // Max integer digits
    //        final CheckBox maxIntChk = findViewById(R.id.chk_max_int);
    //        final EditText maxIntEdt = findViewById(R.id.edt_max_int);
    //        maxIntChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    //            @Override
    //            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    //                maxIntEdt.setEnabled(isChecked);
    //            }
    //        });
    //        maxIntEdt.setEnabled(maxIntChk.isChecked());
    //        maxIntEdt.setText(String.valueOf(10));
    //
    //        // Max fractional digits
    //        final CheckBox maxFracChk = findViewById(R.id.chk_max_frac);
    //        final EditText maxFracEdt = findViewById(R.id.edt_max_frac);
    //        maxFracChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    //            @Override
    //            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    //                maxFracEdt.setEnabled(isChecked);
    //            }
    //        });
    //        maxFracEdt.setEnabled(maxFracChk.isChecked());
    //        maxFracEdt.setText(String.valueOf(8));
    //
    //        // Dialog views
    //        valueTxv = findViewById(R.id.txv_result);
    //        final Button openBtn = findViewById(R.id.btn_open);
    //        openBtn.setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View v) {
    //                // Set dialog settings
    //                if (nbFmtTypeGroup.getCheckedRadioButtonId() == R.id.radio_nbfmt_default) {
    //                    nbFmt = NumberFormat.getInstance();
    //                } else {
    //                    nbFmt = NumberFormat.getCurrencyInstance();
    //                }
    //                nbFmt.setMaximumIntegerDigits(maxIntChk.isChecked() ?
    //                        Integer.valueOf(maxIntEdt.getText().toString()) : Integer.MAX_VALUE);
    //                nbFmt.setMaximumFractionDigits(maxFracChk.isChecked() && minValEdt.length() > 0 ?
    //                        Integer.valueOf(maxFracEdt.getText().toString()) : Integer.MAX_VALUE);
    //
    //                BigDecimal minValue = minValChk.isChecked() && minValEdt.length() > 0 ?
    //                        new BigDecimal(minValEdt.getText().toString()) : null;
    //                BigDecimal maxValue = maxValChk.isChecked() && maxValEdt.length() > 0 ?
    //                        new BigDecimal(maxValEdt.getText().toString()) : null;
    //                CalcNumpadLayout numpadLayout = numpadLayoutGroup.getCheckedRadioButtonId() == R.id.radio_numpad_calc ?
    //                        CalcNumpadLayout.CALCULATOR : CalcNumpadLayout.PHONE;
    //
    //                if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
    //                    // Min can't be greater than max, disable max value.
    //                    maxValChk.setChecked(false);
    //                    maxValue = null;
    //                }
    //
    //                calcDialog.getSettings()
    //                        .setInitialValue(value)
    //                        .setExpressionShown(showExprChk.isChecked())
    //                        .setExpressionEditable(exprEditableChk.isChecked())
    //                        .setAnswerBtnShown(showAnswerBtnChk.isChecked())
    //                        .setSignBtnShown(showSignBtnChk.isChecked())
    //                        .setOrderOfOperationsApplied(applyOrderOpChk.isChecked())
    //                        .setShouldEvaluateOnOperation(evalOnOperationChk.isChecked())
    //                        .setZeroShownWhenNoValue(showZeroChk.isChecked())
    //                        .setMinValue(minValue)
    //                        .setMaxValue(maxValue)
    //                        .setNumpadLayout(numpadLayout)
    //                        .setNumberFormat(nbFmt);
    //
    //                FragmentManager fm = getSupportFragmentManager();
    //                if (fm.findFragmentByTag("calc_dialog") == null) {
    //                    calcDialog.show(fm, "calc_dialog");
    //                }
    //            }
    //        });
    //
    //        updateValueText();
    //    }
    //
    //    @Override
    //    protected void onSaveInstanceState(Bundle state) {
    //        super.onSaveInstanceState(state);
    //
    //        if (value != null) {
    //            state.putSerializable("value", value);
    //        }
    //    }
    //
    //    @Override
    //    public void onValueEntered(int requestCode, @Nullable BigDecimal value) {
    //        this.value = value;
    //        updateValueText();
    //    }
    //
    //    private void updateValueText() {
    //        if (value == null) {
    //            valueTxv.setText(R.string.result_value_none);
    //        } else {
    //            valueTxv.setText(nbFmt.format(value));
    //        }
    //    }
}
