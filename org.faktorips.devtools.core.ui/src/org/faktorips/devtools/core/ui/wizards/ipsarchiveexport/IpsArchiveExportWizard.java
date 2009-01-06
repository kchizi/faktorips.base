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

package org.faktorips.devtools.core.ui.wizards.ipsarchiveexport;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;

public class IpsArchiveExportWizard extends Wizard implements IExportWizard {
    public static String ID = "org.faktorips.devtools.core.ui.wizards.ipsarchiveexport.IpsArchiveExportWizard"; //$NON-NLS-1$
    private static String DIALOG_SETTINGS_KEY = "IpsArchiveExportWizard"; //$NON-NLS-1$
    
    private IpsArchivePackageWizardPage ipsArPackageWizardPage;
    
    private boolean hasNewDialogSettings;
    
    /**
     * The selection this wizard is called on.
     */
    private IStructuredSelection selection;
    
    /**
     * Create a new IpsArExportWizard
     */
    public IpsArchiveExportWizard() {
        super();
        setWindowTitle(Messages.IpsArchiveExportWizard_Export);
        this.setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("wizards/ExportIpsArchiveWizard.png")); //$NON-NLS-1$
        
        IDialogSettings workbenchSettings= IpsPlugin.getDefault().getDialogSettings();
        IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY); //$NON-NLS-1$
        if (section == null)
            hasNewDialogSettings = true;
        else {
            hasNewDialogSettings = false;
            setDialogSettings(section);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
        setNeedsProgressMonitor(true);
    }

    /**
     * {@inheritDoc}
     */
    public void addPages() {
        super.addPages();
        ipsArPackageWizardPage = new IpsArchivePackageWizardPage(selection);
        addPage(ipsArPackageWizardPage);
    }

    /**
     * {@inheritDoc}
     */
    public boolean performFinish() {
        if (!executeArchiveOperation()){
            return false;
    }
        
        // save the dialog settings
        if (hasNewDialogSettings) {
            IDialogSettings workbenchSettings = IpsPlugin.getDefault().getDialogSettings();
            IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
            section = workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
            setDialogSettings(section);
        }
        ipsArPackageWizardPage.saveWidgetValues();
        
        return true;
    }

    private boolean executeArchiveOperation() {
        Object[] checkedObjects = ipsArPackageWizardPage.getCheckedElements();
        List checkedRoots = new ArrayList(checkedObjects.length);
        for (int i = 0; i < checkedObjects.length; i++) {
            if (checkedObjects[i] instanceof IIpsPackageFragmentRoot) {
                checkedRoots.add(checkedObjects[i]);
            }
        }

        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation((IIpsPackageFragmentRoot[])checkedRoots
                .toArray(new IIpsPackageFragmentRoot[checkedRoots.size()]), ipsArPackageWizardPage.getDestinationFile());
        op.setInclJavaBinaries(ipsArPackageWizardPage.isInclJavaBinaries());
        op.setInclJavaSources(ipsArPackageWizardPage.isInclJavaSources());

        WorkbenchRunnableAdapter workbenchRunnableAdapter = new WorkbenchRunnableAdapter(op);
        try {
            getContainer().run(true, true, workbenchRunnableAdapter);
        } catch (InterruptedException e) {
            return false;
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        return true;
    }
}
