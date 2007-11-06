/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpttype;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.policycmpttype.PcTypePage;
import org.faktorips.devtools.core.ui.wizards.type.TypePage;


/**
 * An IpsObjectPage for the IpsObjectType ProductCmptType. 
 */
public class ProductCmptTypePage extends TypePage {
    
    private TextButtonField pcTypeField;
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public ProductCmptTypePage(IStructuredSelection selection, PcTypePage pcTypePage) throws JavaModelException {
        super(IpsObjectType.PRODUCT_CMPT_TYPE_V2, selection, Messages.ProductCmptTypePage_title);
        this.pageOfAssociatedType = pcTypePage;
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewProductCmptTypeWizard.png")); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit) {
        return toolkit.createProductCmptTypeRefControl(null, container, false);
    }
    
    /**
     * Sets default values to the fields of this page.
     */
    public void pageEntered() throws CoreException{
        if(pageOfAssociatedType == null){
            return;
        }
        if(!isAlreadyBeenEntered()){
            if(StringUtils.isEmpty(getPackage())){
                setPackage(pageOfAssociatedType.getPackage());
            }
            if(StringUtils.isEmpty(getSourceFolder())){
                setSourceFolder(pageOfAssociatedType.getSourceFolder());
            }
            if(StringUtils.isEmpty(getSuperType())){
                if(!StringUtils.isEmpty(pageOfAssociatedType.getSuperType())){
                    IPolicyCmptType superPcType = getIpsProject().findPolicyCmptType(pageOfAssociatedType.getSuperType());
                    if(superPcType != null) {
                        setSuperType(superPcType.getProductCmptType());
                    }
                }
            }
            if(StringUtils.isEmpty(getIpsObjectName())){
                String postfix = IpsPlugin.getDefault().getIpsPreferences().getDefaultProductCmptTypePostfix();
                if(!StringUtils.isEmpty(postfix)){
                    setIpsObjectName(pageOfAssociatedType.getIpsObjectName() + postfix);
                }
            }
        }
        super.pageEntered();
    }
    
    /**
     * Returns true if the policy component type is configurable
     */
    public boolean canCreateIpsSrcFile(){
        if(pageOfAssociatedType == null){
            return true;
        }
        return ((PcTypePage)pageOfAssociatedType).isPolicyCmptTypeConfigurable();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);
        if(pageOfAssociatedType == null){
            toolkit.createFormLabel(nameComposite, Messages.ProductCmptTypePage_labelConfigures);
            IpsObjectRefControl pcTypeControl = toolkit.createPcTypeRefControl(null, nameComposite);
            pcTypeField = new TextButtonField(pcTypeControl);
            pcTypeField.addChangeListener(this);
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        if(pageOfAssociatedType == null){
            IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
            if (root!=null) {
                ((IpsObjectRefControl)pcTypeField.getControl()).setIpsProject(root.getIpsProject());
            } else {
                ((IpsObjectRefControl)pcTypeField.getControl()).setIpsProject(null);
            }
        }
    }
    
    /**
     * Calles the super class method and additionally validates if the supertype of the product
     * component type is within the super type hierarchy of the product component type of the
     * supertype of the policy component type.
     */
    protected void validatePageExtension() throws CoreException {
        super.validatePageExtension();
        if (pageOfAssociatedType != null) {
            if (!StringUtils.isEmpty(pageOfAssociatedType.getSuperType())) {
                IPolicyCmptType superPcType = getIpsProject().findPolicyCmptType(pageOfAssociatedType.getSuperType());
                if(superPcType != null){
                    IProductCmptType policyCmptProductCmptSuperType = superPcType.findProductCmptType(getIpsProject());
                    IProductCmptType superType = null;
                    if(!StringUtils.isEmpty(getSuperType())) {
                        superType = getIpsProject().findProductCmptType(getSuperType());
                    }
                    validateSupertype(superType, policyCmptProductCmptSuperType);
                }
            }
        }
        
        if(pageOfAssociatedType == null && 
           !StringUtils.isEmpty((String)pcTypeField.getValue())){
            IPolicyCmptType configuableType = getIpsProject().findPolicyCmptType((String)pcTypeField.getValue());
            if(configuableType == null){
                setErrorMessage(Messages.ProductCmptTypePage_msgPcTypeDoesNotExist);
            } else if(!StringUtils.isEmpty(configuableType.getProductCmptType())){
                setErrorMessage(Messages.ProductCmptTypePage_msgPcTypeAlreadyConfigured);                
            } else {
                FindNextConfiguredSuperType finder = new FindNextConfiguredSuperType(getIpsProject());
                IProductCmptType superType = getIpsProject().findProductCmptType(getSuperType());
                finder.start(superType);
                if(finder.nextConfiguringSupertype != null){
                    IPolicyCmptType superTypePolicyCmptType = getIpsProject().findPolicyCmptType(finder.qualifiedNameOfConfiguredType);
                    if(superTypePolicyCmptType != null){
                        if(configuableType != null){
                            IPolicyCmptType superPcType = (IPolicyCmptType)configuableType.findSupertype(getIpsProject());
                            if(superPcType == null || !superPcType.equals(superTypePolicyCmptType)){
                                setErrorMessage(NLS.bind(Messages.ProductCmptTypePage_msgPolicyCmptSuperTypeNeedsToBeX, superTypePolicyCmptType.getQualifiedName()));
                            }
                        }
                    }
                }
            }
        }
        
        if (pageOfAssociatedType != null) {
            if(!StringUtils.isEmpty(getSuperType())) {
                IProductCmptType superType = getIpsProject().findProductCmptType(getSuperType());
                if(superType != null && superType.isConfigurationForPolicyCmptType()){
                    String msg = NLS.bind(Messages.ProductCmptTypePage_msgPolicyCmptSuperTypeNeedsToBeX, superType.getPolicyCmptType());
                    if(StringUtils.isEmpty(pageOfAssociatedType.getSuperType())){
                        setErrorMessage(msg);
                        return;
                    }
                    IPolicyCmptType policyCmptType = getIpsProject().findPolicyCmptType(pageOfAssociatedType.getSuperType());
                    if(policyCmptType == null){
                        setErrorMessage(msg);
                        return;
                    }
                    if(!superType.getPolicyCmptType().equals(policyCmptType.getQualifiedName())){
                        setErrorMessage(msg);
                        return;
                    }
                }
            }
        }
    }

    private void validateSupertype(final IProductCmptType superType,
            final IProductCmptType productCmptTypeOfPolicyCmptSupertype) throws CoreException {
        if(productCmptTypeOfPolicyCmptSupertype == null){
            return;
        }
        String msg = NLS.bind(Messages.ProductCmptTypePage_msgSupertypeMustBeInHierarchy, productCmptTypeOfPolicyCmptSupertype.getQualifiedName());
        if(superType == null){
            setErrorMessage(msg);
            return;
        }
        if (superType != null) {
            final Boolean[] holder = new Boolean[] { Boolean.FALSE };
            if (productCmptTypeOfPolicyCmptSupertype != null) {
                new TypeHierarchyVisitor(getIpsProject()) {
                    protected boolean visit(IType currentType) throws CoreException {
                        if (currentType.equals(productCmptTypeOfPolicyCmptSupertype)) {
                            holder[0] = Boolean.TRUE;
                            return false;
                        }
                        return true;
                    }
                }.start(superType);
                if (Boolean.FALSE.equals(holder[0])) {
                    setErrorMessage(msg);
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void finishIpsObjects(IIpsObject newIpsObject, List modifiedIpsObjects) throws CoreException {
        super.finishIpsObjects(newIpsObject, modifiedIpsObjects);
        IProductCmptType productCmptType = (IProductCmptType)newIpsObject;
        if (pageOfAssociatedType != null) {
            if (((PcTypePage)pageOfAssociatedType).isPolicyCmptTypeConfigurable()) {
                productCmptType.setConfigurationForPolicyCmptType(true);
                productCmptType.setPolicyCmptType(pageOfAssociatedType.getQualifiedIpsObjectName());
                return;
            }
        } else {
            String configuredpcType = (String)pcTypeField.getValue();
            if(!StringUtils.isEmpty(configuredpcType)){
                IPolicyCmptType policyCmptType = getIpsProject().findPolicyCmptType(configuredpcType);
                if(policyCmptType != null){
                    policyCmptType.setConfigurableByProductCmptType(true);
                    policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
                    modifiedIpsObjects.add(policyCmptType);
                    productCmptType.setConfigurationForPolicyCmptType(true);
                    productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
                }
                return;
            }
        }
        productCmptType.setConfigurationForPolicyCmptType(false);
    }

    
    private static class FindNextConfiguredSuperType extends TypeHierarchyVisitor{
    
        private IProductCmptType nextConfiguringSupertype;
        private String qualifiedNameOfConfiguredType;

        public FindNextConfiguredSuperType(IIpsProject ipsProject) {
            super(ipsProject);
        }
        
        protected boolean visit(IType currentType) throws CoreException {
            IProductCmptType superType = (IProductCmptType)currentType;
            if(!StringUtils.isEmpty(superType.getPolicyCmptType())){
                nextConfiguringSupertype = superType;
                qualifiedNameOfConfiguredType = superType.getPolicyCmptType();
                return false;
            }
            return true;
        }
        
    }
    
}
