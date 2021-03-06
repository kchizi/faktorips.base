/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

import java.util.Set;

/**
 * Special case of value set that represents an unrestricted value set. This is a value set that
 * does not restrict the values allowed by a datatype (Integer, String, etc.).
 *
 * @author Jan Ortmann
 */
public class UnrestrictedValueSet<T> implements ValueSet<T> {

    private static final long serialVersionUID = 1L;

    private final boolean containsNull;

    /**
     * Constructs a new set, which acts as if it contains every possible value (including
     * {@code null}).
     */
    public UnrestrictedValueSet() {
        this(true);
    }

    /**
     * Constructs a new set, which acts as if it contains every possible value (potentially
     * including {@code null}, depending on the given parameter).
     *
     * @param containsNull whether {@code null} is included in this set
     */
    public UnrestrictedValueSet(boolean containsNull) {
        this.containsNull = containsNull;
    }

    /**
     * Returns {@code true} if the given value is not {@code null}. The return value for
     * {@code null} is dependent on the constructor.
     */
    public boolean contains(T value) {
        if (value == null) {
            return containsNull;
        } else {
            return true;
        }
    }

    public boolean containsNull() {
        return containsNull;
    }

    public Set<T> getValues(boolean excludeNull) {
        throw new IllegalStateException();
    }

    public boolean isDiscrete() {
        return false;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean isRange() {
        return false;
    }

    public int size() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String toString() {
        return "UnrestrictedValueSet";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (containsNull ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof UnrestrictedValueSet
                && (this.containsNull() == ((UnrestrictedValueSet<?>)o).containsNull()));
    }
}
