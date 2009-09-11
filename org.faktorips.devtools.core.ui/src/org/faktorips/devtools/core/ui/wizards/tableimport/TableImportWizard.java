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

package org.faktorips.devtools.core.ui.wizards.tableimport;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizardPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ResultDisplayer;
import org.faktorips.devtools.core.ui.wizards.ipsimport.ImportPreviewPage;
import org.faktorips.devtools.core.ui.wizards.ipsimport.IpsObjectImportWizard;
import org.faktorips.devtools.core.ui.wizards.tablecontents.TableContentsPage;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.MessageList;

/**
 * Wizard to import external tables into ipstablecontents.
 * 
 * @author Thorsten Waertel, Thorsten Guenther
 */
public class TableImportWizard extends IpsObjectImportWizard {

    protected static String ID = "org.faktorips.devtools.core.ui.wizards.tableimport.TableImportWizard"; //$NON-NLS-1$
    protected final static String DIALOG_SETTINGS_KEY = "TableImportWizard"; //$NON-NLS-1$

    private TableContentsPage newTableContentsPage;
    private SelectTableContentsPage selectContentsPage;
    private ImportPreviewPage tablePreviewPage;

    public TableImportWizard() {
        setWindowTitle(Messages.TableImport_title);
        setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("wizards/TableImportWizard.png")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPages() {
        try {
            // create pages
            startingPage = new SelectFileAndImportMethodPage(null);
            addPage(startingPage);
            newTableContentsPage = new TableContentsPage(selection);
            addPage(newTableContentsPage);
            selectContentsPage = new SelectTableContentsPage(selection);
            addPage(selectContentsPage);

            startingPage.setImportIntoExisting(importIntoExisting);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performFinish() {
        try {
            final String filename = startingPage.getFilename();
            final ITableFormat format = startingPage.getFormat();
            final ITableStructure structure = getTableStructure();
            ITableContents contents = getTableContents();
            final ITableContentsGeneration generation = (ITableContentsGeneration)contents
                    .getGenerationsOrderedByValidDate()[0];
            final String nullRepresentation = startingPage.getNullRepresentation();

            // no append, so remove any existing content
            if (!startingPage.isImportExistingAppend()) {
                generation.clear();
            }

            final MessageList messageList = new MessageList();
            final boolean ignoreColumnHeader = startingPage.isImportIgnoreColumnHeaderRow();

            IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    format.executeTableImport(structure, new Path(filename), generation, nullRepresentation,
                            ignoreColumnHeader, messageList, startingPage.isImportIntoExisting());
                }
            };
            IIpsModel model = IpsPlugin.getDefault().getIpsModel();
            model.runAndQueueChangeEvents(runnable, null);

            if (!messageList.isEmpty()) {
                getShell().getDisplay().syncExec(
                        new ResultDisplayer(getShell(), Messages.TableImportWizard_operationName, messageList));
            }

            // save the dialog settings
            if (hasNewDialogSettings) {
                IDialogSettings workbenchSettings = IpsPlugin.getDefault().getDialogSettings();
                IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
                section = workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
                setDialogSettings(section);
            }

            IpsUIPlugin.getDefault().openEditor(contents.getIpsSrcFile());
        } catch (Exception e) {
            Throwable throwable = e;
            if (e instanceof InvocationTargetException) {
                throwable = ((InvocationTargetException)e).getCause();
            }
            IpsPlugin.logAndShowErrorDialog(new IpsStatus("An error occurred during the import process.", throwable)); //$NON-NLS-1$
        } finally {
            selectContentsPage.saveWidgetValues();
            startingPage.saveWidgetValues();
        }

        // this implementation of this method should always return true since this causes the wizard
        // dialog to close. in either case if an exception arises or not it doesn't make sense to
        // keep the dialog up
        return true;
    }

    /**
     * @return the table-structure the imported table content has to follow.
     */
    private ITableStructure getTableStructure() {
        try {
            if (startingPage.isImportIntoExisting()) {
                return selectContentsPage.getTableContents().findTableStructure(
                        selectContentsPage.getTableContents().getIpsProject());
            } else {
                return newTableContentsPage.getTableStructure();
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * @return The table contents to import into.
     */
    private ITableContents getTableContents() throws CoreException {
        if (startingPage.isImportIntoExisting()) {
            return selectContentsPage.getTableContents();
        } else {
            IIpsSrcFile ipsSrcFile = newTableContentsPage.createIpsSrcFile(new NullProgressMonitor());
            newTableContentsPage.finishIpsObjects(ipsSrcFile.getIpsObject(), new ArrayList<IIpsObject>());
            return newTableContentsPage.getCreatedTableContents();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWizardPage getStartingPage() {
        return startingPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        saveDataToWizard();
        if (page == startingPage) {
            /*
             * Set the completed state on the opposite page to true so that the wizard can finish
             * normally.
             */
            selectContentsPage.setPageComplete(!startingPage.isImportIntoExisting());
            newTableContentsPage.setPageComplete(startingPage.isImportIntoExisting());
            /*
             * Validate the returned Page so that finished state is already set to true if all
             * default settings are correct.
             */
            if (startingPage.isImportIntoExisting()) {
                selectContentsPage.validatePage();
                return selectContentsPage;
            }
            try {
                newTableContentsPage.validatePage();
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            return newTableContentsPage;
        }

        if (page == selectContentsPage || page == newTableContentsPage) {
            ITableStructure tableStructure = getTableStructure();
            if (tablePreviewPage == null) {
                tablePreviewPage = new ImportPreviewPage(this, startingPage.getFilename(), startingPage.getFormat(),
                        tableStructure, startingPage.isImportIgnoreColumnHeaderRow());

                addPage(tablePreviewPage);
            } else {
                tablePreviewPage.reinit(startingPage.getFilename(), startingPage.getFormat(), tableStructure,
                        startingPage.isImportIgnoreColumnHeaderRow());
            }
            tablePreviewPage.validatePage();

            return tablePreviewPage;
        }

        return null;
    }

    @Override
    public boolean canFinish() {
        if (isExcelTableFormatSelected()) {
            if (getContainer().getCurrentPage() == selectContentsPage) {
                if (selectContentsPage.isPageComplete()) {
                    return true;
                }
            }
            if (getContainer().getCurrentPage() == newTableContentsPage) {
                if (newTableContentsPage.isPageComplete()) {
                    return true;
                }
            }
        }
        return super.canFinish();
    }

}
