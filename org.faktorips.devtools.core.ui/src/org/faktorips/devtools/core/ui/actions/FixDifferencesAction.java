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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.fixdifferences.OpenFixDifferencesToModelWizardAction;

/**
 * 
 * @author Daniel Hohenberger
 */
public class FixDifferencesAction extends Action {
    private IWorkbenchWindow window;
    private IStructuredSelection selection;

    public FixDifferencesAction(IWorkbenchWindow window, IStructuredSelection selection) {
        super();
        this.window = window;
        this.selection = selection;
        setText(Messages.FixDifferencesAction_text);
        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("FixDifferencesToModel.gif")); //$NON-NLS-1$
        this.setEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        OpenFixDifferencesToModelWizardAction action = new OpenFixDifferencesToModelWizardAction();
        action.init(window);
        action.selectionChanged(this, selection);
        action.run(this);
    }
}
