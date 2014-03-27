/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

public class GeneralInfoSection extends IpsSection {

    private IPolicyCmptType policyCmptType;

    private ExtensionPropertyControlFactory extFactory;

    public GeneralInfoSection(IPolicyCmptType policyCmptType, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(policyCmptType);

        this.policyCmptType = policyCmptType;
        extFactory = new ExtensionPropertyControlFactory(policyCmptType);

        initControls();
        setText(Messages.GeneralInfoSection_title);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);

        Hyperlink link = toolkit.createHyperlink(composite, Messages.GeneralInfoSection_linkSuperclass);
        link.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent event) {
                try {
                    IPolicyCmptType supertype = (IPolicyCmptType)policyCmptType.findSupertype(policyCmptType
                            .getIpsProject());
                    if (supertype != null) {
                        IpsUIPlugin.getDefault().openEditor(supertype);
                    }
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }

            }

        });

        PcTypeRefControl supertypeRefControl = toolkit
                .createPcTypeRefControl(policyCmptType.getIpsProject(), composite);
        getBindingContext().bindContent(supertypeRefControl, policyCmptType, IType.PROPERTY_SUPERTYPE);

        extFactory.createControls(composite, toolkit, policyCmptType, IExtensionPropertyDefinition.POSITION_TOP);
        extFactory.bind(getBindingContext());

        Composite modifyerComposite = toolkit.createGridComposite(client, 1, false, false);

        // Abstract flag
        Checkbox abstractCheckbox = toolkit.createCheckbox(modifyerComposite,
                Messages.GeneralInfoSection_labelAbstractClass);
        getBindingContext().bindContent(abstractCheckbox, policyCmptType, IType.PROPERTY_ABSTRACT);

        // Reference to ProductCmptType
        Composite refComposite = toolkit.createGridComposite(client, 1, true, false);
        // the text field should be directly beneath the checkbox
        ((GridLayout)refComposite.getLayout()).verticalSpacing = 0;

        Checkbox refCheckbox = toolkit.createCheckbox(refComposite, Messages.GeneralInfoSection_labelProduct);
        getBindingContext().bindContent(refCheckbox, policyCmptType,
                IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);

        Composite productCmptTypeComposite = toolkit.createGridComposite(refComposite, 2, false, false);
        ((GridLayout)productCmptTypeComposite.getLayout()).marginLeft = 16;

        Hyperlink refLink = toolkit.createHyperlink(productCmptTypeComposite, Messages.GeneralInfoSection_labelType);
        refLink.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent event) {
                try {
                    IProductCmptType productCmptType = policyCmptType.findProductCmptType(policyCmptType
                            .getIpsProject());
                    if (productCmptType != null) {
                        IpsUIPlugin.getDefault().openEditor(productCmptType);
                    }
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }

            }

        });
        getBindingContext().bindEnabled(refLink, policyCmptType,
                IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);

        ProductCmptType2RefControl productCmptTypeRefControl = new ProductCmptType2RefControl(
                policyCmptType.getIpsProject(), productCmptTypeComposite, toolkit, false);
        getBindingContext().bindContent(productCmptTypeRefControl, policyCmptType,
                IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE);
        getBindingContext().bindEnabled(productCmptTypeRefControl, policyCmptType,
                IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);

        extFactory.createControls(composite, toolkit, policyCmptType, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(getBindingContext());
    }

}
