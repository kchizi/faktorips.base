/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.testcasetype;

/**
 * Specification of a test rule parameter.
 * 
 * @author Joerg Ortmann
 */
public interface ITestRuleParameter extends ITestParameter {

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTRULEPARAMETER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the validation rule must have the expected result
     * type.
     */
    public final static String MSGCODE_NOT_EXPECTED_RESULT = MSGCODE_PREFIX + "NotExpectedResult"; //$NON-NLS-1$

}
