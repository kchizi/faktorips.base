/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlphaNumericComparatorTest {

    @InjectMocks
    private AlphaNumericComparator comparator;

    @Test
    public void testCompare_equalString() throws Exception {
        assertEquals(0, comparator.compare("a", "a"));
    }

    @Test
    public void testCompare_simpleString() throws Exception {
        assertTrue(comparator.compare("a", "b") < 0);
        assertTrue(comparator.compare("b", "a") > 0);
    }

    @Test
    public void testCompare_equalNum() throws Exception {
        assertEquals(0, comparator.compare("1234", "1234"));
    }

    @Test
    public void testCompare_simpleNum() throws Exception {
        assertTrue(comparator.compare("3", "1234") < 0);
        assertTrue(comparator.compare("1234", "3") > 0);
    }

    @Test
    public void testCompare_equalAlphaNum() throws Exception {
        assertEquals(0, comparator.compare("1.1.abc", "1.1.abc"));
    }

    @Test
    public void testCompare_alphaNum1() throws Exception {
        assertTrue(comparator.compare("1.3.xxx", "1.10.xxx") < 0);
        assertTrue(comparator.compare("1.10.xxx", "1.3.xxx") > 0);
    }

    @Test
    public void testCompare_alphaNum2() throws Exception {
        assertTrue(comparator.compare("1.3.aaa", "1.3.xxx") < 0);
        assertTrue(comparator.compare("1.3.xxx", "1.3.aaa") > 0);
    }

    @Test
    public void testCompare_alphaNum3() throws Exception {
        assertTrue(comparator.compare("a3", "aa3") < 0);
        assertTrue(comparator.compare("aa3", "a3") > 0);
    }

    @Test
    public void testCompare_alphaNum4() throws Exception {
        assertTrue(comparator.compare("a+3", "a3") < 0);
        assertTrue(comparator.compare("a3", "a+3") > 0);
    }

    @Test
    public void testCompare_alphaNum5() throws Exception {
        assertTrue(comparator.compare("a 3", "aa3") < 0);
        assertTrue(comparator.compare("aa3", "a 3") > 0);
    }

    @Test
    public void testCompare_alphaNum6() throws Exception {
        assertTrue(comparator.compare("1.12.xxx", "1.4.xxx") > 0);
        assertTrue(comparator.compare("1.4.xxx", "1.12.xxx") < 0);
    }

    @Test
    public void testCompare_alphaNum7() throws Exception {
        assertTrue(comparator.compare("abcasdfasf sadfasdf asdf asd 12340", "abcasdfasf sadfasdf asdf asd 1234") > 0);
        assertTrue(comparator.compare("abcasdfasf sadfasdf asdf asd 1234", "abcasdfasf sadfasdf asdf asd 12340") < 0);
    }

    @Test
    public void testCompare_leadingZeroNum1() throws Exception {
        assertTrue(comparator.compare("a01a", "a1b") < 0);
        assertTrue(comparator.compare("a1b", "a01a") > 0);
        assertTrue(comparator.compare("a01c", "a1b") > 0);
        assertTrue(comparator.compare("a1b", "a01c") < 0);
    }

    @Test
    public void testCompare_leadingZeroNum2() throws Exception {
        assertTrue(comparator.compare("a01", "a1") < 0);
        assertTrue(comparator.compare("a1", "a01") > 0);
        assertTrue(comparator.compare("a1xxx0001", "a001xxx1") < 0);
        assertTrue(comparator.compare("a001xxx1", "a1xxx0001") > 0);
    }

}
