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

package org.faktorips.devtools.core.ui;

import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.ui.actions.IpsTestAction;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

/**
 * Lauch shortcut to run ips test cases.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestLaunchShortcut implements ILaunchShortcut {

    /**
     * {@inheritDoc}
     */
    public void launch(IEditorPart editor, String mode) {
        if (editor instanceof IpsObjectEditor){
            IIpsObject objectInEditor = ((IpsObjectEditor)editor).getIpsObject();
            IpsTestAction runTestAction = new IpsTestAction(null, mode);
            runTestAction.run(new StructuredSelection(objectInEditor));        
        }
    }

    /**
     * {@inheritDoc}
     */
    public void launch(ISelection selection, String mode) {
        IpsTestAction runTestAction = new IpsTestAction(null, mode);
        runTestAction.run(new StructuredSelection(selection));
    }
}
