/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.datatype.joda;

import junit.framework.TestCase;

import org.junit.Test;

public class LocalDateDatatypeTest extends TestCase {

    private LocalDateDatatype datatype;

    @Test
    public void testIsParsable() {
        datatype = new LocalDateDatatype();
        assertTrue(datatype.isParsable(null));
        assertTrue(datatype.isParsable(""));
        assertTrue(datatype.isParsable("2013-11-13"));
        assertFalse(datatype.isParsable("13-11-2013"));
    }
}