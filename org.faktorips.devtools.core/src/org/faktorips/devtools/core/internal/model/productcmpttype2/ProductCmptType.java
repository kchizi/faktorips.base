/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype2;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.IpsObjectPartCollection;
import org.faktorips.devtools.core.internal.model.type.Type;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IAttribute;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype2.IRelation;
import org.faktorips.devtools.core.model.productcmpttype2.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype2.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Implementation of IProductCmptType.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptType extends Type implements IProductCmptType {

    private String policyCmptType = "";
    
    private IpsObjectPartCollection attributes = new IpsObjectPartCollection(this, Attribute.class, "Attribute");
    private IpsObjectPartCollection relations = new IpsObjectPartCollection(this, Relation.class, "Relation");
    private IpsObjectPartCollection tableStructureUsages = new IpsObjectPartCollection(this, TableStructureUsage.class, "TableStructureUsage");
    
    public ProductCmptType(IIpsSrcFile file) {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    protected IpsObjectPartCollection createCollectionForMethods() {
        return new IpsObjectPartCollection(this, ProductCmptTypeMethod.class, "Method");
    }

    /**
     * {@inheritDoc}
     */
    public IType findSupertype(IIpsProject project) throws CoreException {
        if (!hasSupertype()) {
            return null;
        }
        IProductCmptType supertype = findSuperProductCmptType(project);
        if (supertype!=null) {
            return supertype;
        }
        return null; // TODO hier muessen auch policy component types gefunden werden! 
    }


    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT_TYPE2;
    }

    /**
     * {@inheritDoc}
     */
    public String getPolicyCmptType() {
        return policyCmptType;
    }

    /**
     * {@inheritDoc}
     */
    public void setPolicyCmptType(String newType) {
        String oldType = policyCmptType;
        policyCmptType = newType;
        valueChanged(oldType, newType);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConfigurationForPolicyCmptType() {
        return !StringUtils.isEmpty(policyCmptType);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType findPolicyCmptType(IIpsProject project) throws CoreException {
        return project.findPolicyCmptType(policyCmptType);
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptType findSuperProductCmptType(IIpsProject project) throws CoreException {
        return (IProductCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE2, getSupertype());
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute newAttribute() {
        return (IAttribute)attributes.newPart();
    }
    
    /**
     * {@inheritDoc}
     */
    public IAttribute getAttribute(String name) {
        return (IAttribute)attributes.getPartByName(name);
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute[] getAttributes() {
        return (IAttribute[])attributes.toArray(new IAttribute[attributes.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfAttributes() {
        return attributes.size();
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveAttributes(int[] indexes, boolean up) {
        return attributes.moveParts(indexes, up);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        policyCmptType = XmlUtil.getAttributeConvertEmptyStringToNull(element, PROPERTY_POLICY_CMPT_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        XmlUtil.setAttributeConvertNullToEmptyString(element, PROPERTY_POLICY_CMPT_TYPE, policyCmptType);
    }

    /**
     * {@inheritDoc}
     */
    public IRelation newRelation() {
        return (IRelation)relations.newPart();
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfRelations() {
        return relations.size();
    }

    /**
     * {@inheritDoc}
     */
    public IRelation getRelation(String name) {
        return (IRelation)attributes.getPartByName(name);
    }
    
    /**
     * {@inheritDoc}
     */
    public IRelation findRelationInSupertypeHierarchy(String name, boolean includeSelf, IIpsProject project) throws CoreException {
        RelationFinder finder = new RelationFinder(project, name);
        finder.start( includeSelf ? this : findSuperProductCmptType(project));
        return finder.relation;
    }

    /**
     * {@inheritDoc}
     */
    public IRelation[] getRelations() {
        return (IRelation[])relations.toArray(new IRelation[relations.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveRelations(int[] indexes, boolean up) {
        return relations.moveParts(indexes, up);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITableStructureUsage findTableStructureUsageInSupertypeHierarchy(String roleName, boolean includeSelf, IIpsProject project) throws CoreException {
        TableStructureUsageFinder finder = new TableStructureUsageFinder(project, roleName);
        finder.start( includeSelf ? this : findSuperProductCmptType(project));
        return finder.tsu;
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfTableStructureUsages() {
        return tableStructureUsages.size();
    }

    /**
     * {@inheritDoc}
     */
    public ITableStructureUsage getTableStructureUsage(String roleName) {
        return (ITableStructureUsage)tableStructureUsages.getPartByName(roleName);
    }

    /**
     * {@inheritDoc}
     */
    public ITableStructureUsage[] getTableStructureUsages() {
        return (ITableStructureUsage[])tableStructureUsages.toArray(new ITableStructureUsage[tableStructureUsages.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveTableStructureUsage(int[] indexes, boolean up) {
        return tableStructureUsages.moveParts(indexes, up);
    }

    /**
     * {@inheritDoc}
     */
    public ITableStructureUsage newTableStructureUsage() {
        return (ITableStructureUsage)tableStructureUsages.newPart();    
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeMethod[] getProductCmptTypeMethods() {
        return (IProductCmptTypeMethod[])methods.toArray(new IProductCmptTypeMethod[methods.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeMethod newProductCmptTypeMethod() {
        return (IProductCmptTypeMethod)methods.newPart();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        if (isConfigurationForPolicyCmptType()) {
            validatePolicyCmptTypeReference(getIpsProject(), list);
        }
    }
    
    private void validatePolicyCmptTypeReference(IIpsProject ipsProject, MessageList list) throws CoreException {
        IPolicyCmptType typeObj = findPolicyCmptType(ipsProject);
        if (typeObj==null) {
            String text = "The policy component type " + policyCmptType + " does not exist.";
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST, text, Message.ERROR, this, PROPERTY_POLICY_CMPT_TYPE));
        }
    }

    class RelationFinder extends ProductCmptTypeHierarchyVisitor {

        private String relationName;
        private IRelation relation = null;
        
        public RelationFinder(IIpsProject project, String relationName) {
            super(project);
            this.relationName = relationName;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IProductCmptType currentType) {
            relation = currentType.getRelation(relationName);
            return relation==null;
        }
        
    }

    class TableStructureUsageFinder extends ProductCmptTypeHierarchyVisitor {

        private String tsuName;
        private ITableStructureUsage tsu = null;
        
        public TableStructureUsageFinder(IIpsProject project, String tsuName) {
            super(project);
            this.tsuName = tsuName;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IProductCmptType currentType) {
            tsu = currentType.getTableStructureUsage(tsuName);
            return tsu==null;
        }
        
    }

}
