/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.productcmpt.AddProductCmptLinkCommand;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.wizards.productcmpt.AddNewProductCmptCommand;
import org.faktorips.devtools.core.ui.wizards.tablecontents.AddNewTableContentsHandler;
import org.faktorips.devtools.core.ui.wizards.tablecontents.SelectExistingTableContentsHandler;

/**
 * This contribution factory creates menu contributions for adding new and existing product
 * components.
 * 
 * @author dirmeier
 */
public class ProductStructureExplorerContributionFactory extends ExtensionContributionFactory {

    public ProductStructureExplorerContributionFactory() {
        super();
    }

    @Override
    public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
        ISelectionService selectionService = (ISelectionService)serviceLocator.getService(ISelectionService.class);
        ISelection selection = selectionService.getSelection();
        createAddNewProductCmpt(serviceLocator, additions, selection);
        createAddExistingProductCmpt(serviceLocator, additions, selection);
        createAddTableMenu(serviceLocator, additions, selection);
    }

    private void createAddNewProductCmpt(IServiceLocator serviceLocator,
            IContributionRoot additions,
            ISelection selection) {
        TypedSelection<IProductCmptStructureReference> typedSelection = new TypedSelection<IProductCmptStructureReference>(
                IProductCmptStructureReference.class, selection);
        if (typedSelection.isValid()
                && (typedSelection.getFirstElement() instanceof IProductCmptReference || typedSelection
                        .getFirstElement() instanceof IProductCmptTypeAssociationReference)) {
            IProductCmptStructureReference reference = typedSelection.getFirstElement();
            IProductCmptTypeAssociationReference[] children;
            if (reference instanceof IProductCmptReference) {
                children = reference.getStructure().getChildProductCmptTypeAssociationReferences(reference);
            } else {
                children = new IProductCmptTypeAssociationReference[] { (IProductCmptTypeAssociationReference)reference };
            }
            if (children.length == 0) {
                // also adding the command knowing that it is disabled because the handler is
                // disabled
                addNewProductCmptCommand(serviceLocator, additions, null);
            } else if (children.length == 1) {
                IProductCmptTypeAssociationReference associationReference = children[0];
                IProductCmptTypeAssociation association = associationReference.getAssociation();
                String label = NLS.bind(Messages.ProductStructureExplorerContributionFactory_addNewProductCmpt_for,
                        IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(association));
                addNewProductCmptCommand(serviceLocator, additions, label);
            } else {
                createMultipleAddNewItem(serviceLocator, additions, children);
            }
        }
    }

    private void createMultipleAddNewItem(IServiceLocator serviceLocator,
            IContributionRoot additions,
            IProductCmptTypeAssociationReference[] children) {
        MenuManager menuManager = new MenuManager(
                Messages.ProductStructureExplorerContributionFactory_label_addNewProductCmpt,
                getAddNewProductCmptImageDescriptor(), null);
        additions.addContributionItem(menuManager, null);
        for (IProductCmptStructureReference child : children) {
            if (child instanceof IProductCmptTypeAssociationReference) {
                IProductCmptTypeAssociationReference associationReference = (IProductCmptTypeAssociationReference)child;
                CommandContributionItemParameter itemParameter = new CommandContributionItemParameter(serviceLocator,
                        StringUtils.EMPTY, AddNewProductCmptCommand.COMMAND_ID, SWT.PUSH);
                itemParameter.label = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(
                        associationReference.getAssociation());
                HashMap<String, String> parameters = new HashMap<String, String>();
                parameters.put(AddNewProductCmptCommand.PARAMETER_SELECTED_ASSOCIATION, associationReference
                        .getAssociation().getName());
                itemParameter.parameters = parameters;
                CommandContributionItem item = new CommandContributionItem(itemParameter);
                item.setVisible(true);
                menuManager.add(item);
            }
        }
    }

    private void addNewProductCmptCommand(IServiceLocator serviceLocator, IContributionRoot additions, String label) {
        CommandContributionItemParameter parameters = new CommandContributionItemParameter(serviceLocator,
                StringUtils.EMPTY, AddNewProductCmptCommand.COMMAND_ID, SWT.PUSH);
        parameters.icon = getAddNewProductCmptImageDescriptor();
        parameters.label = label;
        CommandContributionItem item = new CommandContributionItem(parameters);
        item.setVisible(true);
        additions.addContributionItem(item, null);
    }

    private void createAddExistingProductCmpt(IServiceLocator serviceLocator,
            IContributionRoot additions,
            ISelection selection) {
        TypedSelection<IProductCmptStructureReference> typedSelection = new TypedSelection<IProductCmptStructureReference>(
                IProductCmptStructureReference.class, selection);
        if (typedSelection.isValid()
                && (typedSelection.getFirstElement() instanceof IProductCmptReference || typedSelection
                        .getFirstElement() instanceof IProductCmptTypeAssociationReference)) {
            // when label is null, the default label is used
            String label = getLabelForAddExisting(typedSelection.getFirstElement());

            CommandContributionItemParameter parameters = new CommandContributionItemParameter(serviceLocator,
                    StringUtils.EMPTY, AddProductCmptLinkCommand.COMMAND_ID, SWT.PUSH);
            parameters.icon = getAddProductCmptImageDescriptor();
            parameters.label = label;
            CommandContributionItem item = new CommandContributionItem(parameters);
            item.setVisible(true);
            additions.addContributionItem(item, null);
        }
    }

    /**
     * Returns the label for the add existing command if there is only one kind of product component
     * that could be added. Would return null if there are multiple kinds to get the default label.
     * 
     * @param reference the selected reference
     * @return the label or null for default label
     */
    private String getLabelForAddExisting(IProductCmptStructureReference reference) {
        IAssociation association = null;
        if (reference instanceof IProductCmptTypeAssociationReference) {
            IProductCmptTypeAssociationReference associationReference = (IProductCmptTypeAssociationReference)reference;
            association = associationReference.getAssociation();
        }
        if (reference instanceof IProductCmptReference) {
            IProductCmptReference productCmptRef = (IProductCmptReference)reference;
            IProductCmptTypeAssociationReference[] typeAssociationReferences = productCmptRef.getStructure()
                    .getChildProductCmptTypeAssociationReferences(productCmptRef);
            if (typeAssociationReferences.length == 1) {
                association = typeAssociationReferences[0].getAssociation();
            }

        }
        if (association != null) {
            return NLS.bind(Messages.ProductStructureExplorerContributionFactory_addExistingProductCmpt_for, IpsPlugin
                    .getMultiLanguageSupport().getLocalizedLabel(association));
        } else {
            return null;
        }
    }

    private ITableContentUsage[] getTableContentUsages(IProductCmptGeneration productCmptGen) {
        List<ITableContentUsage> usages = productCmptGen
                .getPropertyValuesIncludingProductCmpt(ITableContentUsage.class);
        return usages.toArray(new ITableContentUsage[usages.size()]);
    }

    private void createAddTableMenu(IServiceLocator serviceLocator, IContributionRoot additions, ISelection selection) {
        TypedSelection<IProductCmptReference> typedSelection = new TypedSelection<IProductCmptReference>(
                IProductCmptReference.class, selection);
        if (typedSelection.isValid()) {
            IProductCmptReference productCmptRef = typedSelection.getFirstElement();
            IProductCmpt productCmpt = productCmptRef.getProductCmpt();
            IProductCmptGeneration productCmptGen = productCmpt.getGenerationEffectiveOn(productCmptRef.getStructure()
                    .getValidAt());
            ITableContentUsage[] tableContentUsages = getTableContentUsages(productCmptGen);
            if (tableContentUsages.length == 0) {
                CommandContributionItem itemNew = createAddNewTableCommand(serviceLocator, null, null, true);
                additions.addContributionItem(itemNew, null);
                CommandContributionItem itemExisting = createAddExistingTableCommand(serviceLocator, null, null, true);
                additions.addContributionItem(itemExisting, null);
            } else if (tableContentUsages.length == 1) {
                ITableContentUsage tableContentUsage = tableContentUsages[0];
                CommandContributionItem itemNew = createAddNewTableCommand(serviceLocator, tableContentUsage, NLS.bind(
                        Messages.ProductStructureExplorerContributionFactory_label_newTableContent_for, IpsPlugin
                                .getMultiLanguageSupport().getLocalizedCaption(tableContentUsage)), true);
                additions.addContributionItem(itemNew, null);
                CommandContributionItem itemExisting = createAddExistingTableCommand(serviceLocator, tableContentUsage,
                        NLS.bind(Messages.ProductStructureExplorerContributionFactory_label_selectTableFor, IpsPlugin
                                .getMultiLanguageSupport().getLocalizedCaption(tableContentUsage)), true);
                additions.addContributionItem(itemExisting, null);
            } else {
                MenuManager menuManagerNew = new MenuManager(
                        Messages.ProductStructureExplorerContributionFactory_label_newTableContent,
                        getNewTableContentsImageDescriptor(), null);
                MenuManager menuManagerExisting = new MenuManager(
                        Messages.ProductStructureExplorerContributionFactory_label_selectTable,
                        getExistingTableContentsImageDescriptor(), null);
                additions.addContributionItem(menuManagerNew, null);
                additions.addContributionItem(menuManagerExisting, null);
                for (ITableContentUsage tableContentUsage : tableContentUsages) {
                    CommandContributionItem itemNew = createAddNewTableCommand(serviceLocator, tableContentUsage,
                            IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(tableContentUsage), false);
                    menuManagerNew.add(itemNew);
                    CommandContributionItem itemExisting = createAddExistingTableCommand(serviceLocator,
                            tableContentUsage,
                            IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(tableContentUsage), false);
                    menuManagerExisting.add(itemExisting);
                }
            }
        }
    }

    private CommandContributionItem createAddNewTableCommand(IServiceLocator serviceLocator,
            ITableContentUsage tableContentUsage,
            String label,
            boolean icon) {
        CommandContributionItemParameter itemParameter = new CommandContributionItemParameter(serviceLocator,
                StringUtils.EMPTY, AddNewTableContentsHandler.COMMAND_ID, SWT.PUSH);
        itemParameter.label = label;
        HashMap<String, String> parameters = new HashMap<String, String>();
        if (tableContentUsage != null) {
            parameters.put(AddNewTableContentsHandler.PARAMETER_TABLE_USAGE, tableContentUsage.getPropertyName());
        }
        itemParameter.parameters = parameters;
        if (icon) {
            itemParameter.icon = getNewTableContentsImageDescriptor();
        }
        CommandContributionItem item = new CommandContributionItem(itemParameter);
        item.setVisible(true);
        return item;
    }

    private CommandContributionItem createAddExistingTableCommand(IServiceLocator serviceLocator,
            ITableContentUsage tableContentUsage,
            String label,
            boolean icon) {
        CommandContributionItemParameter itemParameter = new CommandContributionItemParameter(serviceLocator,
                StringUtils.EMPTY, SelectExistingTableContentsHandler.COMMAND_ID, SWT.PUSH);
        itemParameter.label = label;
        HashMap<String, String> parameters = new HashMap<String, String>();
        if (tableContentUsage != null) {
            parameters.put(SelectExistingTableContentsHandler.PARAMETER_TABLE_USAGE,
                    tableContentUsage.getPropertyName());
        }
        itemParameter.parameters = parameters;
        if (icon) {
            itemParameter.icon = getExistingTableContentsImageDescriptor();
        }
        CommandContributionItem item = new CommandContributionItem(itemParameter);
        item.setVisible(true);
        return item;
    }

    private ImageDescriptor getAddNewProductCmptImageDescriptor() {
        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("Add_new.gif", true); //$NON-NLS-1$
        return imageDescriptor;
    }

    private ImageDescriptor getAddProductCmptImageDescriptor() {
        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("Add.gif", true); //$NON-NLS-1$
        return imageDescriptor;
    }

    private ImageDescriptor getNewTableContentsImageDescriptor() {
        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(
                "NewTableContentsWizard.gif", true); //$NON-NLS-1$
        return imageDescriptor;
    }

    private ImageDescriptor getExistingTableContentsImageDescriptor() {
        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(
                "TableContents.gif", true); //$NON-NLS-1$
        return imageDescriptor;
    }

}
