/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.testcasetype;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;

/**
 * Creates a new test case type.
 * 
 * @author Joerg Ortmann
 */
public class NewTestCaseTypeWizard extends NewIpsObjectWizard {

    private TestCaseTypePage typePage;

    public NewTestCaseTypeWizard() {
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewTestCaseTypeWizard.png")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        typePage = new TestCaseTypePage(selection);
        return typePage;
    }
}
