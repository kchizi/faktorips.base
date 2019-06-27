/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableRows;
import org.faktorips.devtools.core.model.tablecontents.Messages;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Operation to import ipstablecontents from an excel-file.
 * 
 * @author Thorsten Guenther, Alexander Weickmann
 */
public class ExcelTableImportOperation extends AbstractExcelImportOperation {

    /**
     * The table structure the imported table content is bound to
     */
    private ITableStructure structure;

    /**
     * Generation of the table contents the import has to be inserted.
     */
    private ITableRows targetGeneration;

    public ExcelTableImportOperation(ITableStructure structure, String sourceFile, ITableRows targetGeneration,
            ExcelTableFormat format, String nullRepresentationString, boolean ignoreColumnHeaderRow, MessageList list,
            boolean importIntoExisting) {

        super(sourceFile, format, nullRepresentationString, ignoreColumnHeaderRow, list, importIntoExisting);
        this.structure = structure;
        this.targetGeneration = targetGeneration;
        initDatatypes();
    }

    @Override
    protected void initDatatypes() {
        IColumn[] columns = structure.getColumns();
        datatypes = new Datatype[columns.length];
        for (int i = 0; i < columns.length; i++) {
            datatypes[i] = structure.getIpsProject().findDatatype(columns[i].getDatatype());
        }
    }

    @Override
    public void run(IProgressMonitor monitorParameter) throws CoreException {
        IProgressMonitor monitor;
        if (monitorParameter == null) {
            monitor = new NullProgressMonitor();
        } else {
            monitor = monitorParameter;
        }
        try {
            initWorkbookAndSheet();
            monitor.beginTask(Messages.ExcelTableImportOperation_labelImportFile + sourceFile,
                    targetGeneration.getNumOfRows() + 2);

            // Update datatypes because the structure might be altered if this operation is reused.
            initDatatypes();
            monitor.worked(1);
            fillGeneration(targetGeneration, getSheet(), monitor);

            if (monitor.isCanceled()) {
                targetGeneration.getIpsObject().getIpsSrcFile().discardChanges();
            }

            monitor.worked(1);
            monitor.done();
        } catch (IOException e) {
            throw new CoreException(
                    new IpsStatus(NLS.bind(Messages.AbstractXlsTableImportOperation_errRead, sourceFile), e));
        }
    }

    private void fillGeneration(ITableRows generation, Sheet sheet, IProgressMonitor monitor) {
        // Row 0 is the header if ignoreColumnHeaderRow is true, otherwise row 0 contains data.
        int startRow = ignoreColumnHeaderRow ? 1 : 0;
        for (int i = startRow;; i++) {
            Row sheetRow = sheet.getRow(i);
            if (sheetRow == null) {
                // No more rows, we are finished whit this sheet.
                break;
            }
            writeRow(sheetRow, i, generation);

            if (monitor.isCanceled()) {
                return;
            }
            monitor.worked(1);
        }
    }

    private void writeRow(Row sheetRow, int rowIndex, ITableRows generation) {
        IRow genRow = generation.newRow();
        for (int j = 0; j < structure.getNumOfColumns(); j++) {
            Cell cell = sheetRow.getCell(j);
            if (cell == null) {
                if (StringUtils.isNotEmpty(nullRepresentationString)) {
                    String msg = NLS.bind(Messages.ExcelTableImportOperation_msgImportEscapevalue, new Object[] {
                            rowIndex, j, IpsPlugin.getDefault().getIpsPreferences().getNullPresentation() });
                    messageList.add(new Message("", msg, Message.WARNING)); //$NON-NLS-1$
                }
                genRow.setValue(j, null);
            } else {
                genRow.setValue(j, readCell(cell, datatypes[j]));
            }
        }
    }
}
