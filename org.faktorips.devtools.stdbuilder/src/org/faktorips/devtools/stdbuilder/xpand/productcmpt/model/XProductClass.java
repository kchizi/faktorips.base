/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.XDerivedUnionAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.XType;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;

public abstract class XProductClass extends XType {

    public XProductClass(IProductCmptType ipsObjectPartContainer, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(ipsObjectPartContainer, modelContext, modelService);
    }

    @Override
    public boolean isValidForCodeGeneration() {
        try {
            if (!getType().isValid(getIpsProject())) {
                return false;
            } else {
                if (isConfigurationForPolicyCmptType()) {
                    return getPolicyCmptClass().getType().isValid(getIpsProject());
                } else {
                    return true;
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public IProductCmptType getIpsObjectPartContainer() {
        return (IProductCmptType)super.getIpsObjectPartContainer();
    }

    @Override
    public IProductCmptType getType() {
        return getIpsObjectPartContainer();
    }

    @Override
    public XProductClass getSupertype() {
        return (XProductClass)super.getSupertype();
    }

    /**
     * Returns true if this class represents a container that handles properties which changes over
     * time, otherwise false.
     * <p>
     * In other words. True for product generations, false for product component class.
     * 
     */
    public abstract boolean isChangeOverTimeClass();

    @Override
    public Set<XProductAttribute> getAttributes() {
        if (isCached(XProductAttribute.class)) {
            return getCachedObjects(XProductAttribute.class);
        } else {
            Set<XProductAttribute> nodesForParts = initNodesForParts(getAttributesInternal(isChangeOverTimeClass()),
                    XProductAttribute.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    /**
     * Returns the list of attributes. With the parameter you could specify whether you want the
     * attributes that change over time or attributes not changing over time.
     * 
     * @param changableAttributes True to get attributes that change over time, false to get all
     *            other attributes
     * @return the list of attributes defined in this type
     */
    protected Set<IProductCmptTypeAttribute> getAttributesInternal(boolean changableAttributes) {
        Set<IProductCmptTypeAttribute> resultingAttributes = new LinkedHashSet<IProductCmptTypeAttribute>();
        List<IProductCmptTypeAttribute> allAttributes = getType().getProductCmptTypeAttributes();
        for (IProductCmptTypeAttribute attr : allAttributes) {
            if (changableAttributes == attr.isChangingOverTime()) {
                resultingAttributes.add(attr);
            }
        }
        return resultingAttributes;
    }

    public Set<XPolicyAttribute> getConfiguredAttributes() {
        if (isCached(XPolicyAttribute.class)) {
            return getCachedObjects(XPolicyAttribute.class);
        } else {
            Set<XPolicyAttribute> nodesForParts = getConfiguredAttributesInternal();
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    /**
     * Returns the list of configured policy attributes. With the parameter you could specify
     * whether you want the attributes that change over time or attributes not changing over time.
     * <p>
     * This method needs to be final because it may be called in constructor
     * 
     * @return the list of policy attributes configured by this product component.
     */
    protected Set<XPolicyAttribute> getConfiguredAttributesInternal() {
        Set<XPolicyAttribute> resultingAttributes = new LinkedHashSet<XPolicyAttribute>();
        if (isConfigurationForPolicyCmptType() && isChangeOverTimeClass()) {
            XPolicyCmptClass policyCmptClass = getPolicyCmptClass();
            if (!policyCmptClass.isConfiguredBy(getType().getQualifiedName())) {
                return resultingAttributes;
            }
            Set<XPolicyAttribute> allAttributes = policyCmptClass.getAttributes();
            for (XPolicyAttribute attr : allAttributes) {
                if (attr.isProductRelevant()) {
                    if (attr.isGenerateGetAllowedValuesFor()) {
                        resultingAttributes.add(attr);
                    }
                }
            }
            return resultingAttributes;
        } else {
            return resultingAttributes;
        }
    }

    @Override
    public Set<XProductAssociation> getAssociations() {
        if (isCached(XProductAssociation.class)) {
            return getCachedObjects(XProductAssociation.class);
        } else {
            Set<XProductAssociation> nodesForParts = initNodesForParts(
                    getAssociationsInternal(isChangeOverTimeClass()), XProductAssociation.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    /**
     * Getting the list of associations defined in this type. With the parameter
     * changableAssociations you could specify whether you want the associations that are changeable
     * over time or not changeable (sometimes called static) associations.
     * <p>
     * 
     * @param changableAssociations true if you want only associations changeable over time, false
     *            to get only not changeable over time associations
     * @return The list of associations without derived unions
     */
    protected Set<IProductCmptTypeAssociation> getAssociationsInternal(boolean changableAssociations) {
        Set<IProductCmptTypeAssociation> resultingAssociations = new LinkedHashSet<IProductCmptTypeAssociation>();
        List<IProductCmptTypeAssociation> allAssociations = getType().getProductCmptTypeAssociations();
        for (IProductCmptTypeAssociation assoc : allAssociations) {
            if (changableAssociations == assoc.isChangingOverTime()) {
                resultingAssociations.add(assoc);
            }
        }
        return resultingAssociations;
    }

    @Override
    public Set<XDerivedUnionAssociation> getSubsettedDerivedUnions() {
        return findSubsettedDerivedUnions(getAssociations());
    }

    public Set<XTableUsage> getTables() {
        if (isCached(XTableUsage.class)) {
            return getCachedObjects(XTableUsage.class);
        } else {
            Set<XTableUsage> nodesForParts = initNodesForParts(getType().getTableStructureUsages(), XTableUsage.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    public boolean isContainsTables() {
        return !getTables().isEmpty();
    }

    /**
     * Returns true if this type is marked as configured and there is a policy component type that
     * could be configured.
     */
    public boolean isConfigurationForPolicyCmptType() {
        return getType().isConfigurationForPolicyCmptType();
    }

    public String getPolicyInterfaceName() {
        return getPolicyClassName(BuilderAspect.INTERFACE);
    }

    public String getPolicyImplClassName() {
        return getPolicyClassName(BuilderAspect.IMPLEMENTATION);
    }

    protected String getPolicyClassName(BuilderAspect aspect) {
        XPolicyCmptClass xPolicyCmptClass = getPolicyCmptClass();
        return xPolicyCmptClass.getSimpleName(aspect);
    }

    public XPolicyCmptClass getPolicyCmptClass() {
        IPolicyCmptType policyCmptType;
        try {
            policyCmptType = getType().findPolicyCmptType(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        if (policyCmptType == null) {
            throw new NullPointerException("No policy found for " + getName());
        }
        XPolicyCmptClass xPolicyCmptClass = getModelNode(policyCmptType, XPolicyCmptClass.class);
        return xPolicyCmptClass;
    }

    @Override
    public abstract Set<? extends XProductClass> getClassHierarchy();

    /**
     * Returns true if there is at least one association that is not a derived union or the inverse
     * of a derived union.
     * 
     */
    public boolean isContainsNotDerivedAssociations() {
        for (XAssociation association : getAssociations()) {
            if (!association.isDerived()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether we need to generate the cretePolicyCmpt method for the specified policy
     * component class or not
     * 
     * @param policyCmptClass The policy component class for which we want to generate a create
     *            method
     * 
     * @return true if we need to generate the create method
     */
    public boolean isGenerateMethodCreatePolicyCmpt(XPolicyCmptClass policyCmptClass) {
        return isConfigurationForPolicyCmptType() && !getPolicyCmptClass().isAbstract()
                && !policyCmptClass.isAbstract();
    }

    /**
     * Check whether to generate the generic <code>createPolicyComponent</code> method.
     * <p>
     * If this product component class does not configure any policy component and has no super type
     * we generate the method with <code>return null;</code> If it does configure a policy component
     * than this policy component needs to be not abstract and must configure this product
     * component.
     * 
     * @return True if we need to generate the generic <code>createPolicyComponent</code> method
     */
    public boolean isGenerateMethodGenericCreatePolicyComponent() {
        if (!isConfigurationForPolicyCmptType()) {
            if (!hasSupertype()) {
                return true;
            } else {
                return false;
            }
        } else {
            XPolicyCmptClass policyCmptClass = getPolicyCmptClass();
            return !policyCmptClass.isAbstract() && policyCmptClass.isConfiguredBy(getType().getQualifiedName());
        }
    }

    /**
     * Returns the class hierarchy of the corresponding policy component type.
     * 
     * @return The policy component class hierarchy
     */
    public Set<XPolicyCmptClass> getPolicyTypeClassHierarchy() {
        if (isConfigurationForPolicyCmptType()) {
            XPolicyCmptClass policyCmptClass = getPolicyCmptClass();
            Set<XPolicyCmptClass> result = policyCmptClass.getClassHierarchy();
            return result;
        } else {
            return new LinkedHashSet<XPolicyCmptClass>();
        }
    }

    /**
     * Returns the variable or parameter name for the effetiveDate.
     * 
     */
    public String getVarNameEffectiveDate() {
        IChangesOverTimeNamingConvention convention = getChangesOverTimeNamingConvention();
        Locale locale = getLanguageUsedInGeneratedSourceCode();
        String conceptName = convention.getEffectiveDateConceptName(locale);
        return StringUtils.uncapitalize(conceptName);
    }

    public String getGenerationConceptNameSingular() {
        return getChangesOverTimeNamingConvention().getGenerationConceptNameSingular(
                getLanguageUsedInGeneratedSourceCode(), true);
    }

    private IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention() {
        IChangesOverTimeNamingConvention convention = getIpsProject()
                .getChangesInTimeNamingConventionForGeneratedCode();
        return convention;
    }

}