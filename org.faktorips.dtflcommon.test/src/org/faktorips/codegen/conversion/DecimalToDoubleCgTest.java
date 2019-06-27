/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class DecimalToDoubleCgTest extends AbstractSingleConversionCgTest {

    private DecimalToDoubleCg converter;

    @Before
    public void setUp() throws Exception {
        converter = new DecimalToDoubleCg();
    }

    @Test
    public void testGetConversionCode() throws Exception {
        assertEquals("decimal.doubleValue()", getConversionCode(converter, "decimal"));
    }
}
