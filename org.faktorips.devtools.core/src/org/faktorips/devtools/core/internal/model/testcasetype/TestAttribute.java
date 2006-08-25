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

package org.faktorips.devtools.core.internal.model.testcasetype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.testcase.Messages;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test attribute class. 
 * Defines an attribute for a specific policy component parameter class within a test case type definition.
 * 
 * @author Joerg Ortmann
 */
public class TestAttribute extends IpsObjectPart implements ITestAttribute {

	/* Tags */
	static final String TAG_NAME = "TestAttribute"; //$NON-NLS-1$
	
	private String attribute = ""; //$NON-NLS-1$
    
	private boolean deleted = false;
    
	private TestParameterRole role = TestParameterRole.UNKNOWN;
	
	public TestAttribute(IIpsObject parent, int id) {
		super(parent, id);
	}

	public TestAttribute(IIpsObjectPart parent, int id) {
		super(parent, id);
	}
	
    /**
     * {@inheritDoc}
     */
    public void setName(String newName) {
        this.name = newName;
    }
    
	/**
	 * {@inheritDoc}
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAttribute(String newAttribute) {
		String oldAttribute = this.attribute;
		this.attribute = newAttribute;
		valueChanged(oldAttribute, newAttribute);
	}

	/**
	 * {@inheritDoc}
	 */
	public IAttribute findAttribute() throws CoreException {
        if (StringUtils.isEmpty(attribute)) {
            return null;
        }
        IPolicyCmptType pctype = ((TestPolicyCmptTypeParameter)getParent()).findPolicyCmptType();
        if (pctype == null)
            return null;
        
        ITypeHierarchy hierarchy = pctype.getSupertypeHierarchy();
		IAttribute[] attributes = hierarchy.getAllAttributes(pctype);
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i].getName().equals(attribute)) {
				return attributes[i];
			}
		}
        return null;
	}

    /**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
		attribute = element.getAttribute(PROPERTY_ATTRIBUTE);
		role = TestParameterRole.getTestParameterRole(element.getAttribute(PROPERTY_TEST_ATTRIBUTE_ROLE));
	}

    /**
     * {@inheritDoc}
     */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
		element.setAttribute(PROPERTY_ATTRIBUTE, attribute);
		element.setAttribute(PROPERTY_TEST_ATTRIBUTE_ROLE, role.toString());
	}  
	
    /** 
     * Overridden.
     */
    public void delete() {
        ((TestPolicyCmptTypeParameter)getIpsObject()).removeTestAttribute(this);
        updateSrcFile();
        deleted = true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }    
    
    /** 
     * {@inheritDoc}
     */
    public Image getImage() {
		return null;
    }
    
    /**
     * This object has no parts, therfore an exception will be thrown.
     */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type: " + partType); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isExpextedResultAttribute() {
		return role == TestParameterRole.EXPECTED_RESULT;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInputAttribute() {
		return role == TestParameterRole.INPUT;
	}

    /**
     * Sets the role of the test attribute. The following roles could be set.
     * <p><ul>
     * <li>INPUT: the test attribute specifies test attribute input objects
     * <li>EXPECTED_RESULT: the test attribute specifies test attribute expected result objects
     * </ul>
     */
	public void setTestAttributeRole(TestParameterRole role) {
		// assert that the given role is an input or an expected result role,
		// because attributes could have the role combined (input and expected result together)
		ArgumentCheck.isTrue(role.equals(TestParameterRole.INPUT) ||
				role.equals(TestParameterRole.EXPECTED_RESULT));
		this.role = role;
	}
    
    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList messageList) throws CoreException {
        super.validateThis(messageList);
        
        // check if the attribute exists
        IAttribute attribute = findAttribute();
        if (attribute == null){
            String text = NLS.bind(Messages.TestAttributeValue_ValidateError_AttributeNotFound, getAttribute());
            Message msg = new Message(MSGCODE_ATTRIBUTE_NOT_FOUND, text, Message.WARNING, this, ITestAttribute.PROPERTY_ATTRIBUTE);
            messageList.add(msg);
        }
        
        // check the correct role
        if (! isInputAttribute() && ! isExpextedResultAttribute()){
            String text = NLS.bind("Role {0} not allowed for test attribuet {1}", role, name);
            Message msg = new Message(MSGCODE_WRONG_ROLE, text, Message.ERROR, this, PROPERTY_TEST_ATTRIBUTE_ROLE);
            messageList.add(msg);
        }
    }    
}
