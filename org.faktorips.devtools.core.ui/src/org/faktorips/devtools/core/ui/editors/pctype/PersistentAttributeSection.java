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

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

public class PersistentAttributeSection extends SimpleIpsPartsSection {

    public PersistentAttributeSection(IPolicyCmptType ipsObject, Composite parent, UIToolkit toolkit) {
        super(ipsObject, parent, "Attributes", toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new PersistenceAttributesComposite(getIpsObject(), parent, toolkit);
    }

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

    class PersistenceAttributesComposite extends PersistenceComposite {

        @Override
        public String[] getColumnHeaders() {
            return new String[] { "Attribute Name", "Column Name", "Converter", "Unique", "Nullable", "Size",
                    "Precision", "Scale" };

        }

        public PersistenceAttributesComposite(IIpsObject ipsObject, Composite parent, UIToolkit toolkit) {
            super(ipsObject, parent, toolkit);
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new PersistentAttributeContentProvider();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new PersistentAttributeEditDialog((IPolicyCmptTypeAttribute)part, shell);
        }

        @Override
        protected IIpsObjectPart newIpsPart() throws CoreException {
            return ((IPolicyCmptType)getIpsObject()).newPolicyCmptTypeAttribute();
        }

        private class PersistentAttributeContentProvider implements IStructuredContentProvider {
            public Object[] getElements(Object inputElement) {
                IPolicyCmptTypeAttribute[] pcAttributes = ((IPolicyCmptType)getIpsObject())
                        .getPolicyCmptTypeAttributes();
                List<IPolicyCmptTypeAttribute> persistableAttributes = new ArrayList<IPolicyCmptTypeAttribute>();
                for (IPolicyCmptTypeAttribute pcAttribute : pcAttributes) {
                    AttributeType attributeType = pcAttribute.getAttributeType();
                    if (attributeType == AttributeType.CHANGEABLE
                            || attributeType == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL) {
                        persistableAttributes.add(pcAttribute);
                    }
                }
                return persistableAttributes.toArray();
            }

            public void dispose() {
                // nothing todo
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing todo
            }
        }

        private class PersistentAttributeLabelProvider extends LabelProvider implements ITableLabelProvider {

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)element;
                IPersistentAttributeInfo attributeInfo = attribute.getPersistenceAttributeInfo();

                String result = "";
                switch (columnIndex) {
                    case 0:
                        result = attribute.getName();
                        break;
                    case 1:
                        String rawTableColumnName = attributeInfo.getTableColumnName();
                        result = attributeInfo.getIpsProject().getTableNamingStrategy()
                                .getTableName(rawTableColumnName);
                        break;
                    case 2:
                        // TODO: converter
                        result = "";
                        break;
                    case 3:
                        result = String.valueOf(attributeInfo.getTableColumnUnique());
                        break;
                    case 4:
                        result = String.valueOf(attributeInfo.getTableColumnNullable());
                        break;
                    case 5:
                        result = String.valueOf(attributeInfo.getTableColumnSize());
                        break;
                    case 6:
                        result = String.valueOf(attributeInfo.getTableColumnPrecision());
                        break;
                    case 7:
                        result = String.valueOf(attributeInfo.getTableColumnScale());
                        break;

                    default:
                        result = "";
                }
                return (result == null ? "" : result);
            }
        }

        @Override
        public ILabelProvider createLabelProvider() {
            return new PersistentAttributeLabelProvider();
        }

    }

    public class PersistentAttributeEditDialog extends EditDialog {

        public PersistentAttributeEditDialog(IPolicyCmptTypeAttribute part, Shell shell) {
            super(shell, "Edit Attribute", true);
        }

        @Override
        protected Composite createWorkArea(Composite parent) throws CoreException {
            TabFolder folder = (TabFolder)parent;

            TabItem page = new TabItem(folder, SWT.NONE);
            page.setText("Persistence");
            Label control = new Label(parent, SWT.NONE);
            control.setText("Sample Content");
            page.setControl(control);

            return folder;
        }
    }

}
