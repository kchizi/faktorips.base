/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import java.math.BigDecimal;

import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueClassDatatype;
import org.faktorips.values.Money;

/**
 * Datatype for <code>Money</code>.
 * 
 * @author Jan Ortmann
 */
public class MoneyDatatype extends ValueClassDatatype implements NumericDatatype {

	public MoneyDatatype() {
		super(Money.class);
	}

	public MoneyDatatype(String name) {
		super(Money.class, name);
	}
	
	/**
     * {@inheritDoc}
	 */
	public Object getValue(String s) {
		return Money.valueOf(s);
	}

    /**
     * {@inheritDoc}
     */
    public boolean hasNullObject() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String subtract(String minuend, String subtrahend) {
        if (minuend == null || subtrahend == null) {
            throw new NullPointerException("Minuend and subtrahend both can not be null.");
        }
        return Money.valueOf(minuend).subtract(Money.valueOf(subtrahend)).toString();
    }

    /**
     * {@inheritDoc}
     */
    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null.");
        }
        Money a = Money.valueOf(dividend);
        Money b = Money.valueOf(divisor);
        try {
            a.getAmount().divide(b.getAmount(), 0, BigDecimal.ROUND_UNNECESSARY);
        }
        catch (ArithmeticException e) {
            return false;
        }
        return true;
    }
}
