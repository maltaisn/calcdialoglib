package com.maltaisn.calcdialoglib.demo

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.ArrayRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.maltaisn.calcdialog.CalcDialog
import com.maltaisn.calcdialog.CalcDialog.CalcDialogCallback
import com.maltaisn.calcdialog.CalcNumpadLayout
import com.maltaisn.calcdialoglib.demo.databinding.FragmentMainBinding
import java.math.BigDecimal
import java.text.NumberFormat


class MainFragment : Fragment(), CalcDialogCallback {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var value: BigDecimal? = null
    private var nbFmt = NumberFormat.getInstance()


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        val fm = childFragmentManager
        val calcDialog = fm.findFragmentByTag(DIALOG_TAG) as? CalcDialog ?: CalcDialog()

        // Dark theme
        binding.darkThemeChk.setOnCheckedChangeListener { _, _ ->
            updateTheme()
        }

        // Min value
        val minValChk: CheckBox = binding.minValueChk
        val minValueInput: EditText = binding.minValueInput
        minValChk.setOnCheckedChangeListener { _, isChecked ->
            minValueInput.isEnabled = isChecked
        }
        minValueInput.isEnabled = minValChk.isChecked

        // Max value
        val maxValChk = binding.maxValueChk
        val maxValInput: EditText = binding.maxValueInput
        maxValChk.setOnCheckedChangeListener { _, isChecked ->
            maxValInput.isEnabled = isChecked
        }
        maxValInput.isEnabled = maxValChk.isChecked

        // Numpad layout
        var numpadLayout = CalcNumpadLayout.CALCULATOR
        setupDropdown(binding.numpadLayoutDropdown, R.array.numpad_layout_dropdown_values) {
            numpadLayout = when (it) {
                0 -> CalcNumpadLayout.CALCULATOR
                1 -> CalcNumpadLayout.PHONE
                else -> error("Invalid numpad layout dropdown index")
            }
        }

        // Number format
        setupDropdown(binding.nbfmtDropdown, R.array.nbfmt_dropdown_values) {
            nbFmt = when (it) {
                0 -> NumberFormat.getInstance()
                1 -> NumberFormat.getCurrencyInstance()
                else -> error("Invalid number format dropdown index")
            }
            updateNumberFormat()
        }

        // Max integer digits
        val maxIntDigitsChk: CheckBox = binding.maxIntDigitsChk
        val maxIntDigitsInput: EditText = binding.maxIntDigitsInput
        maxIntDigitsChk.setOnCheckedChangeListener { _, isChecked ->
            maxIntDigitsInput.isEnabled = isChecked
            updateNumberFormat()
        }
        maxIntDigitsInput.doAfterTextChanged {
            updateNumberFormat()
        }
        maxIntDigitsInput.isEnabled = maxIntDigitsChk.isChecked

        // Max fractional digits
        val maxFracDigitsChk = binding.maxFracDigitsChk
        val maxFracDigitsInput: EditText = binding.maxFracDigitsInput
        maxFracDigitsChk.setOnCheckedChangeListener { _, isChecked ->
            maxFracDigitsInput.isEnabled = isChecked
            updateNumberFormat()
        }
        maxFracDigitsInput.doAfterTextChanged {
            updateNumberFormat()
        }
        maxFracDigitsInput.isEnabled = maxFracDigitsChk.isChecked

        // Open dialog click listener
        val openDialogClickListener = View.OnClickListener {
            if (fm.findFragmentByTag(DIALOG_TAG) != null) {
                // Dialog is already shown.
                return@OnClickListener
            }

            // Get settings from views
            val minValue = if (minValChk.isChecked && minValueInput.length() > 0) {
                BigDecimal(minValueInput.text.toString())
            } else {
                null
            }
            var maxValue = if (maxValChk.isChecked && maxValInput.length() > 0) {
                BigDecimal(maxValInput.text.toString())
            } else {
                null
            }
            if (minValue != null && maxValue != null && minValue > maxValue) {
                // Min can't be greater than max, disable max value.
                maxValChk.isChecked = false
                maxValue = null
            }

            // Update dialog settings
            calcDialog.settings.let {
                it.initialValue = value
                it.isExpressionShown = binding.showExprChk.isChecked
                it.isExpressionEditable = binding.exprEditableChk.isChecked
                it.isAnswerBtnShown = binding.showAnswerBtnChk.isChecked
                it.isSignBtnShown = binding.showSignBtnChk.isChecked
                it.isOrderOfOperationsApplied = binding.orderOperationChk.isChecked
                it.isShouldEvaluateOnOperation = binding.evalOperationChk.isChecked
                it.isZeroShownWhenNoValue = binding.showZeroChk.isChecked
                it.minValue = minValue
                it.maxValue = maxValue
                it.numpadLayout = numpadLayout
                it.numberFormat = nbFmt
            }

            // Show the dialog
            calcDialog.show(fm, DIALOG_TAG)
        }
        binding.selectedForegroundView.setOnClickListener(openDialogClickListener)
        binding.fab.setOnClickListener(openDialogClickListener)

        if (state == null) {
            // Set initial state
            minValueInput.setText(BigDecimal("-1E10").toPlainString())
            maxValInput.setText(BigDecimal("1E10").toPlainString())
            maxIntDigitsInput.setText(10.toString())
            maxFracDigitsInput.setText(8.toString())
            binding.numpadLayoutDropdown.setSelection(0)
            binding.nbfmtDropdown.setSelection(0)
            updateNumberFormat()
            updateTheme()

        } else {
            // Restore state
            value = if (Build.VERSION.SDK_INT >= 33) {
                state.getSerializable("value", BigDecimal::class.java)
            } else {
                state.getSerializable("value") as BigDecimal?
            }
        }
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        if (value != null) {
            state.putSerializable("value", value)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onValueEntered(requestCode: Int, value: BigDecimal?) {
        if (requestCode == DIALOG_REQUEST_CODE) {
            this.value = value
            updateSelectedValueText()
        }
    }

    private fun updateTheme() {
        AppCompatDelegate.setDefaultNightMode(if (binding.darkThemeChk.isChecked) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        })
    }

    private fun updateNumberFormat() {
        nbFmt.maximumIntegerDigits = binding.maxIntDigitsInput.text.toString().toIntOrNull().takeIf {
            binding.maxIntDigitsChk.isChecked
        } ?: Integer.MAX_VALUE
        nbFmt.maximumFractionDigits = binding.maxFracDigitsInput.text.toString().toIntOrNull().takeIf {
            binding.maxFracDigitsChk.isChecked
        } ?: Integer.MAX_VALUE
        updateSelectedValueText()
    }

    private fun updateSelectedValueText() {
        val valueTxv = binding.selectedValueTxv
        if (value == null) {
            valueTxv.setText(R.string.selection_value_none)
            valueTxv.alpha = 0.5f
        } else {
            valueTxv.text = nbFmt.format(value)
            valueTxv.alpha = 1.0f
        }
    }

    private inline fun setupDropdown(dropdown: AutoCompleteTextView, @ArrayRes items: Int,
                                     crossinline onItemSelected: (pos: Int) -> Unit = {}) {
        val context = requireContext()
        val adapter = DropdownAdapter(context, context.resources.getStringArray(items).toList())
        dropdown.setAdapter(adapter)
        dropdown.setOnItemClickListener { _, _, pos, _ ->
            dropdown.requestLayout()
            onItemSelected(pos)
        }
    }

    /**
     * Custom AutoCompleteTextView adapter to disable filtering since we want it to act like a spinner.
     */
    private class DropdownAdapter(context: Context, items: List<String> = mutableListOf()) :
            ArrayAdapter<String>(context, R.layout.item_dropdown, items) {

        override fun getFilter() = object : Filter() {
            override fun performFiltering(constraint: CharSequence?) = null
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) = Unit
        }
    }

    companion object {
        private const val DIALOG_REQUEST_CODE = 0
        private const val DIALOG_TAG = "calc_dialog"
    }
}
