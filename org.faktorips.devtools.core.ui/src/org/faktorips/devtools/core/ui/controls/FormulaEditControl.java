/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.productcmpt.FormulaEditDialog;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Control to edit the value of an formula. A textfeld followed by a button is provided. If the
 * button is clicked, an special Editor for editing the formula with support is opened.
 * 
 * @author Thorsten Guenther
 */
public class FormulaEditControl extends TextButtonControl implements IDataChangeableReadWriteAccess {

    private IFormula formula;
    private Shell shell;
    private IpsSection parentSection;

    private UIToolkit uiToolkit;

    private boolean dataChangeable = true;

    public FormulaEditControl(Composite parent, UIToolkit toolkit, IFormula formula, Shell shell,
            IpsSection parentSection) {

        super(parent, toolkit, "...", true, 15); //$NON-NLS-1$
        this.formula = formula;
        this.shell = shell;
        this.parentSection = parentSection;
        this.uiToolkit = toolkit;
    }

    @Override
    protected void buttonClicked() {
        try {
            EditDialog dialog = new FormulaEditDialog(formula, shell);
            dialog.setDataChangeable(isDataChangeable());
            if (dialog.open() == Window.OK) {
                if (parentSection != null) {
                    parentSection.refresh();
                }
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.getTextControl().setEnabled(enabled);
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        /*
         * set changeable for the text control, the button control will be always enabled, because
         * formula tests could be executes if changeable or not changeable
         */
        this.dataChangeable = changeable;
        uiToolkit.setDataChangeable(getTextControl(), changeable);
    }

    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

}
