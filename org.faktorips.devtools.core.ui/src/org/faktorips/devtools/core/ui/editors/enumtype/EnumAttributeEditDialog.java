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

package org.faktorips.devtools.core.ui.editors.enumtype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;

/**
 * Dialog to edit an <code>IEnumAttribute</code> of an <code>IEnumType</code>.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttributeEditDialog extends IpsPartEditDialog2 {

    /** The enum attribute being edited. */
    private IEnumAttribute enumAttribute;

    /** The extension property factory that may extend the controls. */
    private ExtensionPropertyControlFactory extFactory;

    /** The ui control to set the <code>datatype</code> property. */
    private DatatypeRefControl datatypeControl;

    /** The ui control to set the <code>useAsLiteralName</code> property. */
    private Checkbox useAsLiteralNameCheckbox;

    /** The ui control to set the <code>uniqueIdentifier</code> property. */
    private Checkbox uniqueIdentifierCheckbox;

    /**
     * Creates a new <code>EnumAttributeEditDialog</code> for the user to edit the given enum
     * attribute with.
     * 
     * @param part The enum attribute to edit with the dialog.
     * @param parentShell The parent ui shell.
     */
    public EnumAttributeEditDialog(IEnumAttribute enumAttribute, Shell parentShell) {
        super(enumAttribute, parentShell, Messages.EnumAttributeEditDialog_title, true);

        this.enumAttribute = enumAttribute;
        this.extFactory = new ExtensionPropertyControlFactory(enumAttribute.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder tabFolder = (TabFolder)parent;

        TabItem page = new TabItem(tabFolder, SWT.NONE);
        page.setText(Messages.EnumAttributeEditDialog_generalTitle);
        page.setControl(createGeneralPage(tabFolder));

        createDescriptionTabItem(tabFolder);
        
        // TODO aw: this does not work properly
        updateEnabledStates();

        return tabFolder;
    }

    /**
     * Updates the enabled states of the <code>datatypeControl</code>,
     * <code>useAsLiteralNameCheckbox</code> and <code>uniqueIdentifierCheckbox</code> ui controls
     * based on the <code>inherited</code> property of the enum attribute.
     */
    private void updateEnabledStates() {
        boolean enabled = !(enumAttribute.isInherited());
        datatypeControl.setEnabled(enabled);
        useAsLiteralNameCheckbox.setEnabled(enabled);
        uniqueIdentifierCheckbox.setEnabled(enabled);
    }

    /** Creates the general tab. */
    private Control createGeneralPage(TabFolder tabFolder) {
        Composite control = createTabItemComposite(tabFolder, 1, false);
        Composite workArea = uiToolkit.createLabelEditColumnComposite(control);

        // Create extension properties on position top
        extFactory.createControls(workArea, uiToolkit, enumAttribute, IExtensionPropertyDefinition.POSITION_TOP);

        // Name
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelName);
        Text nameText = uiToolkit.createText(workArea);
        bindingContext.bindContent(nameText, enumAttribute, IEnumAttribute.PROPERTY_NAME);

        // Datatype
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelDatatype);
        datatypeControl = uiToolkit.createDatatypeRefEdit(enumAttribute.getIpsProject(), workArea);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setPrimitivesAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(true);
        bindingContext.bindContent(datatypeControl, enumAttribute, IEnumAttribute.PROPERTY_DATATYPE);

        // Identifier
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelUseAsLiteralName);
        useAsLiteralNameCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(useAsLiteralNameCheckbox, enumAttribute, IEnumAttribute.PROPERTY_LITERAL_NAME);

        // Unique Identifier
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelUniqueIdentifier);
        uniqueIdentifierCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(uniqueIdentifierCheckbox, enumAttribute, IEnumAttribute.PROPERTY_UNIQUE_IDENTIFIER);

        // Inherited
        uiToolkit.createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelIsInherited);
        Checkbox inheritedCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(inheritedCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED);

        // Create extension properties on position bottom
        extFactory.createControls(workArea, uiToolkit, enumAttribute, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(bindingContext);

        // Set the focus into the name field for better usability.
        nameText.setFocus();

        return control;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contentsChanged(ContentChangeEvent event) {
        super.contentsChanged(event);

        if (event.getPart().equals(getIpsPart())) {
            updateEnabledStates();
        }
    }

}
