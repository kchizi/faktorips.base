/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.policycmpttype;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.policycmpttype.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String NewPcTypePage_configuredByProductCmptType;
    public static String NewPcTypePage_infoConfigurateByProductCmptType;
    public static String NewPcTypePage_title;

    public static String NewProductCmptTypePage_title;
    public static String NewProductCmptTypePage_msgPolicyCmptSuperTypeNeedsToBeX;

}
