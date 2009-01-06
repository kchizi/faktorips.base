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

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.TableCellEditor;
import org.faktorips.util.ArgumentCheck;

/**
 * A control factory for the datytpes boolean and primitve boolean.
 * 
 * @author Joerg Ortmann
 */
public class BooleanControlFactory extends ValueDatatypeControlFactory {

    private IpsPreferences preferences;

    public BooleanControlFactory(IpsPreferences preferences) {
        super();
        ArgumentCheck.notNull(preferences, this);
        this.preferences = preferences;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFactoryFor(ValueDatatype datatype) {
        return Datatype.BOOLEAN.equals(datatype) || Datatype.PRIMITIVE_BOOLEAN.equals(datatype);
    }

    /**
     * {@inheritDoc}
     */
    public EditField createEditField(UIToolkit toolkit, Composite parent, ValueDatatype datatype, IValueSet valueSet) {
        return new BooleanComboField((Combo)createControl(toolkit, parent, datatype, valueSet), preferences
                .getDatatypeFormatter().getBooleanTrueDisplay(), preferences.getDatatypeFormatter()
                .getBooleanFalseDisplay());

    }

    /**
     * {@inheritDoc}
     */
    public Control createControl(UIToolkit toolkit, Composite parent, ValueDatatype datatype, IValueSet valueSet) {
        return toolkit.createComboForBoolean(parent, !datatype.isPrimitive(), preferences.getDatatypeFormatter()
                .getBooleanTrueDisplay(), preferences.getDatatypeFormatter().getBooleanFalseDisplay());
    }

    /**
     * Creates a <code>ComboCellEditor</code> containig a <code>Combo</code> using
     * {@link #createControl(UIToolkit, Composite, ValueDatatype, IValueSet)}. {@inheritDoc}
     */
    public TableCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            ValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex) {
        Combo comboControl = (Combo)createControl(toolkit, tableViewer.getTable(), dataType, valueSet);
        TableCellEditor tableCellEditor = new ComboCellEditor(tableViewer, columnIndex, comboControl);
        // stores the boolean datatype object as data object in the combo,
        // to indicate that the to be displayed data will be mapped as boolean
        if (Datatype.PRIMITIVE_BOOLEAN.equals(dataType)) {
            comboControl.setData(new PrimitiveBooleanDatatype());
        } else {
            comboControl.setData(new BooleanDatatype());
        }
        return tableCellEditor;
    }
}
