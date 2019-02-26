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

/**
 * A layout type for the calculator dialog digit buttons.
 */
public enum CalcNumpadLayout {

    /**
     * Numpad layout like on a phone numpad,
     * top row is 123, middle 456, bottom 789 and 0 below.
     */
    PHONE(R.id.calc_btn_24, R.id.calc_btn_11, R.id.calc_btn_21, R.id.calc_btn_31,
            R.id.calc_btn_12, R.id.calc_btn_22, R.id.calc_btn_32,
            R.id.calc_btn_13, R.id.calc_btn_23, R.id.calc_btn_33),

    /**
     * Numpad layout like on a calculator,
     * top row is 789, middle 456, bottom 123 and 0 below.
     */
    CALCULATOR(R.id.calc_btn_24, R.id.calc_btn_13, R.id.calc_btn_23, R.id.calc_btn_33,
            R.id.calc_btn_12, R.id.calc_btn_22, R.id.calc_btn_32,
            R.id.calc_btn_11, R.id.calc_btn_21, R.id.calc_btn_31);


    int[] buttonIds;

    CalcNumpadLayout(int... ids) {
        buttonIds = ids;
    }

}
