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

## v1.0.0
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
