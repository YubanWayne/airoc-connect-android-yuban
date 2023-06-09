/*
 * Copyright 2014-2022, Cypress Semiconductor Corporation (an Infineon company) or
 * an affiliate of Cypress Semiconductor Corporation.  All rights reserved.
 *
 * This software, including source code, documentation and related
 * materials ("Software") is owned by Cypress Semiconductor Corporation
 * or one of its affiliates ("Cypress") and is protected by and subject to
 * worldwide patent protection (United States and foreign),
 * United States copyright laws and international treaty provisions.
 * Therefore, you may use this Software only as provided in the license
 * agreement accompanying the software package from which you
 * obtained this Software ("EULA").
 * If no EULA applies, Cypress hereby grants you a personal, non-exclusive,
 * non-transferable license to copy, modify, and compile the Software
 * source code solely for use in connection with Cypress's
 * integrated circuit products.  Any reproduction, modification, translation,
 * compilation, or representation of this Software except as specified
 * above is prohibited without the express written permission of Cypress.
 *
 * Disclaimer: THIS SOFTWARE IS PROVIDED AS-IS, WITH NO WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, NONINFRINGEMENT, IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. Cypress
 * reserves the right to make changes to the Software without notice. Cypress
 * does not assume any liability arising out of the application or use of the
 * Software or any product or circuit described in the Software. Cypress does
 * not authorize its products for use in any products where a malfunction or
 * failure of the Cypress product may reasonably be expected to result in
 * significant property damage, injury or death ("High Risk Product"). By
 * including Cypress's product in a High Risk Product, the manufacturer
 * of such system or application assumes all risk of such use and in doing
 * so agrees to indemnify Cypress against all liability.
 */

package com.infineon.airocbluetoothconnect.wearable.model.environment;

import com.infineon.airocbluetoothconnect.wearable.model.ValueWithUnit;

// Unit is in concentration count per cubic meter with a resolution of 1/m^3
public class PollenConcentration extends ValueWithUnit<PollenConcentration, PollenConcentration.Unit> {

    private static final Unit DEFAULT_UNIT = Unit.COUNT_PER_CUBIC_METER;
    private static final int EXPONENT = 0;
    private static final int CM3_IN_1_M3 = 1000000;

    public PollenConcentration() {
        super(DEFAULT_UNIT);
        mScale = 8; // TODO
    }

    public static class Unit extends ValueWithUnit.Unit {

        public static final Unit COUNT_PER_CUBIC_METER = new Unit("1/\u006D\u00B3");//\u33A5");
        public static final Unit COUNT_PER_CUBIC_CENTIMETER = new Unit("1/\u0063\u006D\u00B3");//\u33A4");
        private static final Unit[] ALL_UNITS = {COUNT_PER_CUBIC_METER, COUNT_PER_CUBIC_CENTIMETER};

        private Unit(String text) {
            super(text);
        }
    }

    @Override
    public Unit[] getSupportedUnits() {
        return Unit.ALL_UNITS;
    }

    @Override
    protected double convert(double value, Unit from, Unit to) {
        if (from == Unit.COUNT_PER_CUBIC_METER) {
            value /= CM3_IN_1_M3;
        } else {
            value *= CM3_IN_1_M3;
        }
        return value;
    }

    @Override
    protected Unit getDefaultUnit() {
        return DEFAULT_UNIT;
    }

    @Override
    protected double getExponent() {
        return EXPONENT;
    }
}
