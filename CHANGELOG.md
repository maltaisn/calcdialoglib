### v2.2.3
- Update dependencies to latest versions.
- Fix maximum integer digits not accounting for minus (#41).

### v2.2.2
(skipped, accidentally uploaded a version identical as 2.2.1 to Maven Central, can't be removed)

### v2.2.1
- Fixed NPE when erasing after presenter is detached. (#28)

## v2.2.0
- Google's Material Components now used for UI.
- Lowered min SDK to 14.
- Added content description for the erase button.
- Initial value can now be negated. Previous behavior was to clear the value.
- Breaking changes in styling attributes:
    - Removed `calcHeaderStyle` and `calcHeaderElevationDrawable` attributes, they were replaced
        by `calcHeaderColor` and `calcHeaderElevationColor`. Note that the latter only applies to
        APIs below 21, otherwise default elevation color is used.
    - Renamed `calcNumberBtnStyle` to `calcDigitBtnStyle`, `calcNumberBtnColor` to `calcDigitBtnColor`,
      and `calcDialogSepColor` to `calcDividerColor`.
- Fixed crashes due to NumberFormat deserialization bug during unparcelization.

### v2.1.2
- Fixed many NPE on some devices.
- Fixed `Expression` parcel implementation.

### v2.1.1
- Disabled RTL layout in dialog.

## v2.1.0
- Added Hebrew translation.

### v2.0.1
- Fixed crash in writeToParcel on Android 5.0 with RTL language (#21).
- Japenese translation improvements.

# v2.0.0
- Changed package name to `com.maltaisn.calcdialoglib`.
- To change the dialog settings, `CalcDialog.getSettings()` must now be used.
- Order of operations is now applied by default. This can be changed with `setOrderOfOperationsApplied(Boolean)`.
- Added a setting to show the typed expression (`setExpressionShown(Boolean)`), which can also be edited (`setExpressionEditable(Boolean)`).
- Added a setting for choosing the numpad layout: like a phone (top row 123) or a calculator (top row 789).
- Added a min value setting separate from max value. As a consequence, `setSignCanBeChanged` setting was removed, use a minimum or maximum of 0 instead. If the minimum or the maximum is 0, special error messages like "Result must be positive" will still be shown.
- Added `calcHeaderElevation`, `calcHeaderStyle`, `calcExpressionStyle` and `calcExpressionScrollViewStyle` style attributes.
- Added getters to `CalcSettings` for better Kotlin interoperability.
- Added a `calcHeaderElevationDrawable` attribute, needed to customize the header before API 21.
- Changed `setValue(BigDecimal)` to `setInitialValue(BigDecimal)`.
- Changed `setClearOnOperation(Boolean)` to `setShouldEvaluateOnOperation(Boolean)`.
- Changed `setShowZeroWhenNoValue(Boolean)` to `setZeroShownWhenNoValue(Boolean)`.
- Changed minus sign from hyphen `-` to unicode symbol `−`.
- Changed "Out of bounds" error behavior, now only shown when OK is pressed.
- Changed default maximum dialog height from 400dp to 500dp.
- Replaced all formatting settings with NumberFormat:
    - `setMaximumIntegerDigits`: maximum integer digits than can be typed, but more could be displayed.
    - `setMaximumFractionDigits`: maximum fraction digits than can be typed and displayed.
    - `setDecimalFormatSymbols`: change decimal separator, negative sign, grouping symbol, etc.
    - `setGroupingUsed`: enable or disable grouping.
    - `setRoundingMode`: change the rounding mode, also used for division by the calculator.
    - More settings available like prefix, suffix and minimum digits.
- Removed `calcHeaderHeight` attribute, use `android:layout_height` in `calcValueStyle` instead.
- Removed `com.google.android.material` dependency.

### v1.4.1
- Improved material components compatibility.
- Fixed custom button style not working.

## v1.4.0
- Migrated to AndroidX.

### v1.3.1
- Fixed library dependencies not being added to the project

## v1.3.0
- Removed setting that allowed keeping trailing zeroes (breaking change)
- Fixed leading zeroes being allowed
- Fixed "-0" displayed when erasing from "-0.2" for example

## v1.2.0
- Added a request code when creating dialog and on callback (breaking change)
- Backported to API 16
- Added translations in arabic, chinese, german, hindi, italian, japenese, korean, lithuanian, polish, portuguese, russian, spanish, turkish and vietnamese
- Added option to hide the sign button
- Removed `setRetainInstance(true)` on dialog and to manually save state instead
- Fixed OK button not behaving like the equal button

### v1.1.2
- Fixed sign can be changed not being the default behavior
- Fixed display not being saved on configuration change

### v1.1.1
- Forced LTR layout on the dialog

## v1.1.0
- Added option to show ANS button to reuse past answer
- Improved erase button view
    - New attribute to erase all when held
    - Prefixed attributes with "calc" to prevent mixing them
    - Added haptic feedback when held

- Fixed minor bug with decimal point button

### v1.0.2
- Fixed wrong design: result can't be edited now
- If maximum fraction digits is 0, decimal point button will be disabled
- Fixed bug where wrong sign error were inversed
- Changed `setFormatChars` to `setFormatSymbols`

### v1.0.1
- Fixed issues with custom format characters
- Added documentation, optimized imports, added licenses on files, etc

# v1.0.0
- Initial release
- Customizable styles for nearly all views
- Arbitrary precision calculations using BigDecimal
- Many options for the calculator
  - Maximum value
  - Maximum digits for the integer and fractional part
  - Rounding mode
  - Strip trailing zeroes or not
  - Sign can be changed or not
  - Clear display or operation button click or not
  - Show zero when there is no value or not
  - Formatting characters for group and decimal separators
  - Grouping size

- Dialog is restored automatically on configuration change
- Erase button can be held to erase more quickly
- Display text size is adapted to the value to display
- Errors for division by zero, out of bounds and wrong sign
