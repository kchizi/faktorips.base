/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) d�rfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung � Version 0.1 (vor Gr�ndung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

/**
 * 
 * @author Jan Ortmann
 */
public class AttributesSection extends SimpleIpsPartsSection {

    public AttributesSection(IProductCmptType type, Composite parent, UIToolkit toolkit) {
        super(type, parent, "Attributes", toolkit);
    }

    /**
     * {@inheritDoc}
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new AttributesComposite((IProductCmptType)getIpsObject(), parent, toolkit);
    }
    
    /**
     * A composite that shows a policy component's attributes in a viewer and 
     * allows to edit attributes in a dialog, create new attributes and delete attributes.
     */
    public class AttributesComposite extends IpsPartsComposite {
        
        private Button candidatesButton;

        public AttributesComposite(IProductCmptType type, Composite parent,
                UIToolkit toolkit) {
            super(type, parent, toolkit);
        }
        
        /**
         * {@inheritDoc}
         */
        public void setDataChangeable(boolean flag) {
            super.setDataChangeable(flag);
            candidatesButton.setEnabled(flag);
        }
        
        public IProductCmptType getProductCmptType() {
            return (IProductCmptType)getIpsObject();
        }
        
        protected ILabelProvider createLabelProvider() {
            return new DefaultLabelProvider();
        }
        
        /**
         * {@inheritDoc}
         */
        protected IStructuredContentProvider createContentProvider() {
            return new AttributeContentProvider();
        }

        /**
         * {@inheritDoc}
         */
        protected IIpsObjectPart newIpsPart() {
            return getProductCmptType().newAttribute();
        }

        /**
         * {@inheritDoc}
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new AttributeEditDialog((IProductCmptTypeAttribute)part, shell);
        }
        
        protected int[] moveParts(int[] indexes, boolean up) {
            return indexes;
        }
        
        private class AttributeContentProvider implements IStructuredContentProvider {
            public Object[] getElements(Object inputElement) {
                 return getProductCmptType().getAttributes();
            }
            public void dispose() {
                // nothing todo
            }
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing todo
            }
        }

    }
    

}
