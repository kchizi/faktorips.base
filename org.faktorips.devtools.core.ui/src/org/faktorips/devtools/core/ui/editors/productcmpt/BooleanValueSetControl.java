/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.ControlComposite;

/**
 * Control to define a boolean value set in the product component editor. Three check boxes are
 * provided, allowing the user to select which of the boolean values (true, false, and null), are
 * allowed.
 */
public class BooleanValueSetControl extends ControlComposite implements IDataChangeableReadWriteAccess {

    /** true if the value set can be edited, false if it read-only. */
    private boolean dataChangeable;

    /**
     * Provider for source and target enum value set.
     */
    private IEnumValueSetProvider enumValueSetProvider;

    private Checkbox trueBox;
    private Checkbox falseBox;
    private Checkbox nullBox;

    private final IPolicyCmptTypeAttribute property;

    /**
     * Creates a new control to show and edit the value set owned by the config element.
     * 
     * @param parent The parent composite to add this control to.
     * @param toolkit The toolkit used to create controls.
     * @param configElement The config element that contains the value set.
     */
    public BooleanValueSetControl(Composite parent, UIToolkit toolkit, IPolicyCmptTypeAttribute property,
            IConfigElement configElement) {
        super(parent, SWT.NONE);
        this.property = property;

        setEnumValueSetProvider(new DefaultEnumValueSetProvider(configElement));
        GridLayout layout = new GridLayout(3, false);
        layout.horizontalSpacing = 10;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        UIDatatypeFormatter datatypeFormatter = IpsUIPlugin.getDefault().getDatatypeFormatter();
        IValueSet valueSet = configElement.getValueSet();
        ValueDatatype valueDatatype = valueSet instanceof ValueSet ? ((ValueSet)valueSet).getValueDatatype() : null;

        if (valueDatatype != null) {
            trueBox = toolkit
                    .createCheckbox(this, datatypeFormatter.formatValue(valueDatatype, Boolean.toString(true)));
            falseBox = toolkit.createCheckbox(this,
                    datatypeFormatter.formatValue(valueDatatype, Boolean.toString(false)));
            if (!valueDatatype.isPrimitive()) {
                nullBox = toolkit
                        .createCheckbox(this, IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
            }
        } else {
            // Fallback to default formatting for true/false
            trueBox = toolkit.createCheckbox(this, "True"); //$NON-NLS-1$
            falseBox = toolkit.createCheckbox(this, "False"); //$NON-NLS-1$
            nullBox = toolkit.createCheckbox(this, IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
        }

    }

    @Override
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;
        Set<String> allowedValues = new HashSet<String>();
        if (property.getValueSet() instanceof IEnumValueSet) {
            IEnumValueSet policyCmptRestrictions = (IEnumValueSet)property.getValueSet();
            for (String s : policyCmptRestrictions.getValues()) {
                allowedValues.add(s);
            }
        }
        if (allowedValues.isEmpty()) {
            // Either, the property is unrestricted, or no restriction values are defined. In both
            // cases, all values are allowed.
            allowedValues.add(Boolean.toString(true));
            allowedValues.add(Boolean.toString(false));
            allowedValues.add(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
        }
        trueBox.setEnabled(dataChangeable && allowedValues.contains(Boolean.toString(true)));
        falseBox.setEnabled(dataChangeable && allowedValues.contains(Boolean.toString(false)));
        if (nullBox != null) {
            nullBox.setEnabled(false);
        }
    }

    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    public void setEnumValueSetProvider(IEnumValueSetProvider enumValueSetProvider) {
        this.enumValueSetProvider = enumValueSetProvider;
    }

    public IEnumValueSetProvider getEnumValueSetProvider() {
        return enumValueSetProvider;
    }

    public Checkbox getTrueCheckBox() {
        return trueBox;
    }

    public Checkbox getFalseCheckBox() {
        return falseBox;
    }

    public Checkbox getNullCheckBox() {
        return nullBox;
    }
}