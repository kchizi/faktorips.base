/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public class OpenEditorHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);

        TypedSelection<IAdaptable> typedSelection = new TypedSelection<IAdaptable>(IAdaptable.class, selection);
        IAdaptable firstElement = typedSelection.getFirstElement();

        // Modify selection as necessary
        IStructuredSelection modifiedSelection = new StructuredSelection(firstElement.getAdapter(IFile.class));
        if (firstElement instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)firstElement;
            modifiedSelection = new StructuredSelection(getTargetProductCmptGeneration(link));
        }

        // Open editor with appropriate selection
        OpenEditorAction action = new OpenEditorAction(null);
        action.openEditor(modifiedSelection);

        return null;
    }

    private IProductCmptGeneration getTargetProductCmptGeneration(IProductCmptLink link) {
        IProductCmptGeneration targetProductCmptGeneration = null;
        try {
            IProductCmpt targetProductCmpt = link.findTarget(link.getIpsProject());
            if (targetProductCmpt != null) {
                targetProductCmptGeneration = targetProductCmpt.getGenerationEffectiveOn(link
                        .getProductCmptGeneration().getValidFrom());
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return targetProductCmptGeneration;
    }

}
