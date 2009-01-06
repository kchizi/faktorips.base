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

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Validator class to check the child selection of tree elements showing the same product component.
 * If there are more tree elements representing the same product component then their child
 * selection must be the same. E.g. it is not possible to leave the old table contents on a product
 * component and on the same time switch to a new table contents for a product component which is linked by
 * another product component, because in the end it is the same product component.
 * 
 * @author Joerg Ortmann
 */
public class SameOperationValidator {
    private static String OPERATION_UNCHECKED = "Unchecked"; //$NON-NLS-1$
    private static String OPERATION_CHECKED = "Checked"; //$NON-NLS-1$
    private static String[] ALL_OPERATIONS = new String[]{OPERATION_UNCHECKED, OPERATION_CHECKED};
    
    private CheckboxTreeViewer tree;
    private IProductCmptTreeStructure structure;
    
    private class ParentProductCmptChildOperation {
        private IProductCmpt parentProductCmpt;
        private String operation;
        private Object child;
        
        public ParentProductCmptChildOperation(IProductCmpt parentProductCmpt, String operation, Object child) {
            this.parentProductCmpt = parentProductCmpt;
            this.operation = operation;
            this.child = child;
        }
        
        public ParentProductCmptChildOperation(IProductCmpt parentProductCmpt, String operation,
                IProductCmptTypeAssociation relation, IProductCmpt productCmpt) {
            this(parentProductCmpt, operation, relation.getName()+"#"+productCmpt.getQualifiedName()); //$NON-NLS-1$
        }

        public IProductCmpt getParentProductCmpt() {
            return parentProductCmpt;
        }
        public String getOperation() {
            return operation;
        }
        public Object getChild() {
            return child;
        }

        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((child == null) ? 0 : child.hashCode());
            result = prime * result + ((operation == null) ? 0 : operation.hashCode());
            result = prime * result + ((parentProductCmpt == null) ? 0 : parentProductCmpt.hashCode());
            return result;
        }
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final ParentProductCmptChildOperation other = (ParentProductCmptChildOperation)obj;
            if (child == null) {
                if (other.child != null)
                    return false;
            } else if (!child.equals(other.child))
                return false;
            if (operation == null) {
                if (other.operation != null)
                    return false;
            } else if (!operation.equals(other.operation))
                return false;
            if (parentProductCmpt == null) {
                if (other.parentProductCmpt != null)
                    return false;
            } else if (!parentProductCmpt.equals(other.parentProductCmpt))
                return false;
            return true;
        }
    }
    
    public SameOperationValidator(CheckboxTreeViewer tree, IProductCmptTreeStructure structure) {
        this.tree = tree;
        this.structure = structure;
    }

    public void validateSameOperation(MessageList messageList){
        Map objectOperations = new HashMap();
        
        IProductCmptReference root = structure.getRoot();
        storeOperations(root.getStructure().getChildProductCmptReferences(root), objectOperations, messageList);
        storeOperations(root.getStructure().getChildProductCmptStructureTblUsageReference(root), objectOperations, messageList);
    }
    
    private boolean storeOperations(IProductCmptStructureReference[] productCmptStructureReferences, Map objectOperations, MessageList result){
        for (int i = 0; i < productCmptStructureReferences.length; i++) {
            ParentProductCmptChildOperation childOperation = null;
            String errorMsg = ""; //$NON-NLS-1$
            
            String operation = tree.getChecked(productCmptStructureReferences[i])?OPERATION_CHECKED:OPERATION_UNCHECKED;
            
            if (productCmptStructureReferences[i] instanceof IProductCmptReference){
                storeOperations(structure.getChildProductCmptReferences(productCmptStructureReferences[i]), objectOperations, result);
                storeOperations(structure.getChildProductCmptStructureTblUsageReference(productCmptStructureReferences[i]), objectOperations, result);

                IProductCmptTypeRelationReference parent = (IProductCmptTypeRelationReference)((IProductCmptReference)productCmptStructureReferences[i]).getParent();
                if (parent == null){
                    continue;
                }
                IProductCmptReference parentProductCmptReference = (IProductCmptReference)parent.getParent();
                if (!tree.getChecked(parentProductCmptReference)){
                    // if the parent isn't checked, we don't need to validate its child
                    continue;
                }
                
                childOperation = new ParentProductCmptChildOperation(parentProductCmptReference.getProductCmpt(), operation, parent.getRelation(), ((IProductCmptReference)productCmptStructureReferences[i]).getProductCmpt());
                errorMsg = NLS.bind(Messages.SameOperationValidator_errorMsgInvalidSelectionOfProductCmpt,((IProductCmptReference)productCmptStructureReferences[i]).getProductCmpt().getName(), parentProductCmptReference.getProductCmpt().getName());
            } else if (productCmptStructureReferences[i] instanceof IProductCmptStructureTblUsageReference){
                if (! tree.getChecked(productCmptStructureReferences[i].getParent())){
                    // if the parent isn't checked, we don't need to validate its child
                    continue;
                }
                
                IProductCmptReference parentProductCmptReference = (IProductCmptReference)((IProductCmptStructureTblUsageReference)productCmptStructureReferences[i]).getParent();
                IProductCmptStructureTblUsageReference tblUsageReference = (IProductCmptStructureTblUsageReference)productCmptStructureReferences[i];
                childOperation = new ParentProductCmptChildOperation(parentProductCmptReference.getProductCmpt(), operation, tblUsageReference.getTableContentUsage());
                errorMsg = NLS.bind(Messages.SameOperationValidator_errorMsgInvalidSelectionOfProductCmpt,StringUtil.unqualifiedName(tblUsageReference.getTableContentUsage().getTableContentName()),parentProductCmptReference.getProductCmpt().getName());
            }
            
            IProductCmptStructureReference otherOperationInitiator = otherOperationOnSameObjectExists(objectOperations, childOperation);
            if (otherOperationInitiator != null){
                result.add(new Message("", errorMsg, Message.ERROR, productCmptStructureReferences[i])); //$NON-NLS-1$
                result.add(new Message("", errorMsg, Message.ERROR, otherOperationInitiator)); //$NON-NLS-1$
                return true;
            }
            if (childOperation!=null){
                objectOperations.put(childOperation, productCmptStructureReferences[i]);
            }     
        }
        return false;
    }
    
    private IProductCmptStructureReference otherOperationOnSameObjectExists(Map listOfPerformedOperation, ParentProductCmptChildOperation performedOperation){
        for (int i = 0; i < ALL_OPERATIONS.length; i++) {
            if (ALL_OPERATIONS[i].equals(performedOperation.getOperation())){
                continue;
            }
            
            IProductCmptStructureReference productCmptStructureReference = (IProductCmptStructureReference)listOfPerformedOperation.get(new ParentProductCmptChildOperation(performedOperation.getParentProductCmpt(), ALL_OPERATIONS[i], performedOperation.getChild()));
            if (productCmptStructureReference != null){
                return productCmptStructureReference;
            }
        }
        return null;
    }
}
