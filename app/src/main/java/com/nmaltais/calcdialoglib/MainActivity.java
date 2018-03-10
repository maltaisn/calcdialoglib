package com.nmaltais.calcdialoglib;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    private BigDecimal value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        value = null;

        final CalcDialog calcDialog = new CalcDialog();

        signCheck = findViewById(R.id.check_sign);
        if (value == null) signCheck.setEnabled(false);

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

                // Set settings and value
                calcDialog.setValue(value)
                        .setSignCanBeChanged(signCanBeChanged, signCanBeChanged ? 0 : value.signum())
                        .setClearDisplayOnOperation(clearOnOpCheck.isChecked())
                        .setShowZeroWhenNoValue(showZeroCheck.isChecked())
                        .setStripTrailingZeroes(stripZeroCheck.isChecked())
                        .setMaxValue(maxValCheck.isChecked() ? new BigDecimal(maxValEditt.getText().toString()) : null)
                        .setMaxDigits(maxIntCheck.isChecked() ? Integer.valueOf(maxIntEditt.getText().toString()) : CalcDialog.MAX_DIGITS_UNLIMITED,
                                maxFracCheck.isChecked() ? Integer.valueOf(maxFracEditt.getText().toString()) : CalcDialog.MAX_DIGITS_UNLIMITED);

                calcDialog.show(getFragmentManager(), "calc_dialog");
            }
        });
    }

    @Override
    public void onValueEntered(BigDecimal value) {
        this.value = value;

        valueTextv.setText(value.toPlainString());
        signCheck.setEnabled(value.compareTo(BigDecimal.ZERO) != 0);
    }
}

