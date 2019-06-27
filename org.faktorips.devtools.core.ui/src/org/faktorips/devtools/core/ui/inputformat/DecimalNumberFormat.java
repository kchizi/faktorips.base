/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.values.Decimal;

/**
 * Format for floating point number input. This formatter is valid for {@link Decimal},
 * {@link Double} and {@link BigDecimal}
 * 
 * @author Stefan Widmaier
 */
public class DecimalNumberFormat extends AbstractNumberFormat {

    private DecimalFormat numberFormat;
    private final ValueDatatype datatype;

    public static DecimalNumberFormat newInstance(ValueDatatype datatype) {
        DecimalNumberFormat bigDecimalFormat = new DecimalNumberFormat(datatype);
        bigDecimalFormat.initFormat();
        return bigDecimalFormat;
    }

    protected DecimalNumberFormat(ValueDatatype datatype) {
        this.datatype = datatype;
    }

    /**
     * String that is an example of a valid input string.
     */
    private String exampleString;

    @Override
    protected void initFormat(Locale locale) {
        numberFormat = (DecimalFormat)NumberFormat.getNumberInstance(locale);
        numberFormat.setGroupingUsed(true);
        numberFormat.setGroupingSize(3);
        numberFormat.setParseIntegerOnly(false);
        numberFormat.setMaximumFractionDigits(Integer.MAX_VALUE);
        numberFormat.setRoundingMode(RoundingMode.HALF_UP);
        numberFormat.setParseBigDecimal(true);
        exampleString = numberFormat.format(-1000.2);
    }

    @Override
    protected String formatInternal(String value) {
        Object valueAsObject = datatype.getValue(value);
        if (valueAsObject instanceof Decimal) {
            Decimal decimalValue = (Decimal)valueAsObject;
            if (decimalValue.isNull()) {
                return null;
            } else {
                valueAsObject = decimalValue.bigDecimalValue();
            }
        }
        String stringToBeDisplayed = numberFormat.format(valueAsObject);
        return stringToBeDisplayed;
    }

    @Override
    protected String getExampleString() {
        return exampleString;
    }

    @Override
    public DecimalFormat getNumberFormat() {
        return numberFormat;
    }

}
