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

package org.faktorips.devtools.core.internal.model.pctype.refactor;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;

public class RenamePolicyCmptTypeAttributeTest extends AbstractIpsRefactoringTest {

    private static final String POLICY_CMPT_TYPE_ATTRIBUTE_NAME = "policyAttribute";

    private IPolicyCmptType policyCmptType;

    private IPolicyCmptTypeAttribute policyCmptTypeAttribute;

    private IProductCmptType productCmptType;

    private ITestCaseType testCaseType;

    private ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter;

    private ITestAttribute testAttribute;

    private IProductCmpt productCmpt;

    private IProductCmptGeneration productCmptGeneration;

    private IConfigElement productCmptGenerationConfigElement;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Create a policy component type and a product component type.
        policyCmptType = newPolicyCmptType(ipsProject, "Policy");
        productCmptType = newProductCmptType(ipsProject, "Product");
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());

        // Create a policy component type attribute.
        policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.setName(POLICY_CMPT_TYPE_ATTRIBUTE_NAME);
        policyCmptTypeAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        policyCmptTypeAttribute.setModifier(Modifier.PUBLISHED);
        policyCmptTypeAttribute.setAttributeType(AttributeType.CHANGEABLE);
        policyCmptTypeAttribute.setProductRelevant(true);

        // Create a test case type with a test attribute.
        testCaseType = newTestCaseType(ipsProject, "TestCaseType");
        testPolicyCmptTypeParameter = testCaseType.newCombinedPolicyCmptTypeParameter();
        testPolicyCmptTypeParameter.setPolicyCmptType(policyCmptType.getQualifiedName());
        testAttribute = testPolicyCmptTypeParameter.newInputTestAttribute();
        testAttribute.setAttribute(policyCmptTypeAttribute);
        testAttribute.setName("someTestAttribute");
        testAttribute.setDatatype(Datatype.STRING.getQualifiedName());

        // Create a product component based on the product component type.
        productCmpt = newProductCmpt(productCmptType, "ExampleProduct");
        productCmptGeneration = (IProductCmptGeneration)productCmpt.newGeneration();
        productCmptGenerationConfigElement = productCmptGeneration.newConfigElement(policyCmptTypeAttribute);
    }

    public void testRenamePolicyCmptTypeAttribute() throws CoreException {
        String newAttributeName = "test";
        runRenameRefactoring(policyCmptTypeAttribute, newAttributeName);

        // Check for changed attribute name.
        assertNull(policyCmptType.getAttribute(POLICY_CMPT_TYPE_ATTRIBUTE_NAME));
        assertNotNull(policyCmptType.getAttribute(newAttributeName));
        assertTrue(policyCmptTypeAttribute.getName().equals(newAttributeName));

        // Check for test attribute update.
        assertEquals(1, testPolicyCmptTypeParameter.getTestAttributes(newAttributeName).length);
        assertTrue(testAttribute.getAttribute().equals(newAttributeName));

        // Check for product component configuration element update.
        assertNull(productCmptGeneration.getConfigElement(POLICY_CMPT_TYPE_ATTRIBUTE_NAME));
        assertNotNull(productCmptGeneration.getConfigElement(newAttributeName));
        assertEquals(newAttributeName, productCmptGenerationConfigElement.getPolicyCmptTypeAttribute());
    }

    /**
     * Creates another <tt>IPolicyCmptType</tt> that has an attribute that corresponds exactly to
     * the attribute of the already existing <tt>IPolicyCmptType</tt>.
     * <p>
     * This new <tt>IPolicyCmptType</tt> is configured by a new <tt>IProductCmptType</tt>. Based on
     * that <tt>IProductCmptType</tt> exists an <tt>IProductCmpt</tt>. The refactoring of the
     * original <tt>IPolicyCmptTypeAttribute</tt> may not cause modifications to this new
     * <tt>IProductCmpt</tt>'s <tt>IConfigElement</tt>s.
     * <p>
     * Also creates another <tt>ITestCaseType</tt> based on the new <tt>IPolicyCmptType</tt> /
     * <tt>IPolicyCmptTypeAttribute</tt>. The new <tt>ITestCaseType</tt> may not be modified by the
     * refactoring, too.
     */
    public void testRenamePolicyCmptTypeAttributeSameNames() throws CoreException {
        // Create the other policy component type.
        IPolicyCmptType otherPolicyCmptType = newPolicyCmptType(ipsProject, "OtherPolicy");
        otherPolicyCmptType.setConfigurableByProductCmptType(true);

        // Create an attribute corresponding to the attribute of the original policy component type.
        IPolicyCmptTypeAttribute otherAttribute = otherPolicyCmptType.newPolicyCmptTypeAttribute();
        otherAttribute.setName(POLICY_CMPT_TYPE_ATTRIBUTE_NAME);
        otherAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        otherAttribute.setModifier(Modifier.PUBLISHED);
        otherAttribute.setAttributeType(AttributeType.CHANGEABLE);
        otherAttribute.setProductRelevant(true);

        // Create the other product component type.
        IProductCmptType otherProductCmptType = newProductCmptType(ipsProject, "OtherProduct");
        otherProductCmptType.setConfigurationForPolicyCmptType(true);
        otherProductCmptType.setPolicyCmptType(otherPolicyCmptType.getQualifiedName());
        otherPolicyCmptType.setProductCmptType(otherProductCmptType.getQualifiedName());

        // Create a product component on that new product component type.
        IProductCmpt otherProductCmpt = newProductCmpt(otherProductCmptType, "OtherExampleProduct");
        IProductCmptGeneration otherGeneration = (IProductCmptGeneration)otherProductCmpt.newGeneration();
        IConfigElement otherConfigElement = otherGeneration.newConfigElement(otherAttribute);

        // Create another test case type based on the new policy component type.
        ITestCaseType otherTestCaseType = newTestCaseType(ipsProject, "OtherTestCaseType");
        ITestPolicyCmptTypeParameter otherPolicyParameter = otherTestCaseType.newCombinedPolicyCmptTypeParameter();
        otherPolicyParameter.setPolicyCmptType(otherPolicyCmptType.getQualifiedName());
        ITestAttribute otherTestAttribute = otherPolicyParameter.newInputTestAttribute();
        otherTestAttribute.setAttribute(otherAttribute);
        otherTestAttribute.setName("someOtherTestAttribute");
        otherTestAttribute.setDatatype(Datatype.STRING.getQualifiedName());

        // Run the refactoring.
        String newAttributeName = "test";
        runRenameRefactoring(policyCmptTypeAttribute, newAttributeName);

        // The new configuration element may not have been modified.
        assertEquals(POLICY_CMPT_TYPE_ATTRIBUTE_NAME, otherConfigElement.getName());
        assertNull(otherGeneration.getConfigElement(newAttributeName));

        // The new test attribute may not have been modified.
        assertEquals(POLICY_CMPT_TYPE_ATTRIBUTE_NAME, otherTestAttribute.getAttribute());
        assertNull(otherPolicyParameter.getTestAttribute(newAttributeName));
    }

    /**
     * Test to rename an <tt>IPolicyCmptTypeAttribute</tt> from an <tt>IPolicyCmptType</tt> that is
     * a super type of another <tt>IPolicyCmptType</tt>.
     */
    public void testRenamePolicyCmptTypeAttributeInheritance() throws CoreException {
        // Create a super policy component type.
        IPolicyCmptType superPolicyCmptType = newPolicyCmptType(ipsProject, "SuperPolicy");
        superPolicyCmptType.setAbstract(true);
        superPolicyCmptType.setConfigurableByProductCmptType(true);

        // Create an attribute in the super policy component type.
        IPolicyCmptTypeAttribute superAttribute = superPolicyCmptType.newPolicyCmptTypeAttribute();
        superAttribute.setName("superAttribute");
        superAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        superAttribute.setModifier(Modifier.PUBLISHED);
        superAttribute.setAttributeType(AttributeType.CHANGEABLE);
        superAttribute.setProductRelevant(true);

        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());

        // Create a super product component type.
        IProductCmptType superProductCmptType = newProductCmptType(ipsProject, "SuperProduct");
        superProductCmptType.setAbstract(true);
        superProductCmptType.setConfigurationForPolicyCmptType(true);
        superProductCmptType.setPolicyCmptType(superPolicyCmptType.getQualifiedName());
        superPolicyCmptType.setProductCmptType(superProductCmptType.getQualifiedName());
        productCmptType.setSupertype(superProductCmptType.getQualifiedName());

        // Create a test attribute for this new attribute.
        ITestAttribute superTestAttribute = testPolicyCmptTypeParameter.newInputTestAttribute();
        superTestAttribute.setAttribute(superAttribute);
        superTestAttribute.setName("someSuperTestAttribute");
        superTestAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());

        // Create a configuration element for this new attribute.
        IConfigElement superConfigElement = productCmptGeneration.newConfigElement(superAttribute);

        // Run the refactoring.
        String newAttributeName = "test";
        runRenameRefactoring(superAttribute, newAttributeName);

        // Check for test attribute update.
        assertEquals(1, testPolicyCmptTypeParameter.getTestAttributes(POLICY_CMPT_TYPE_ATTRIBUTE_NAME).length);
        assertEquals(1, testPolicyCmptTypeParameter.getTestAttributes(newAttributeName).length);
        assertTrue(superTestAttribute.getAttribute().equals(newAttributeName));

        // Check for product component configuration element update.
        assertNotNull(productCmptGeneration.getConfigElement(POLICY_CMPT_TYPE_ATTRIBUTE_NAME));
        assertNull(productCmptGeneration.getConfigElement("superAttribute"));
        assertNotNull(productCmptGeneration.getConfigElement(newAttributeName));
        assertEquals(newAttributeName, superConfigElement.getPolicyCmptTypeAttribute());
    }

}
