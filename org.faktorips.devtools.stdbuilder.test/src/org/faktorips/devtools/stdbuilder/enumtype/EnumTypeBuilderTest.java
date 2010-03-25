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

package org.faktorips.devtools.stdbuilder.enumtype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

public class EnumTypeBuilderTest extends AbstractStdBuilderTest {

    private final static String ENUM_TYPE_NAME = "TestEnumType";

    private EnumTypeBuilder builder;

    private IEnumType enumType;

    private IEnumAttribute idAttribute;

    private IEnumAttribute nameAttribute;

    private IEnumAttribute inheritedAttribute;

    private IType javaEnum;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        builder = new EnumTypeBuilder(builderSet);

        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        superEnumType.setAbstract(true);
        IEnumAttribute superEnumAttribute = superEnumType.newEnumAttribute();
        superEnumAttribute.setName("superAttribute");
        superEnumAttribute.setDatatype(Datatype.BOOLEAN.getQualifiedName());

        enumType = newEnumType(ipsProject, ENUM_TYPE_NAME);
        enumType.setAbstract(true);
        enumType.setContainingValues(true);
        enumType.setEnumContentName("DoesNotMatter");
        enumType.setSuperEnumType(superEnumType.getQualifiedName());
        idAttribute = enumType.newEnumAttribute();
        idAttribute.setName("id");
        idAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        idAttribute.setUnique(true);
        idAttribute.setIdentifier(true);
        nameAttribute = enumType.newEnumAttribute();
        nameAttribute.setName("name");
        nameAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        nameAttribute.setUsedAsNameInFaktorIpsUi(true);
        inheritedAttribute = enumType.newEnumAttribute();
        inheritedAttribute.setName(superEnumAttribute.getName());
        inheritedAttribute.setInherited(true);

        javaEnum = getGeneratedJavaType(enumType, false, false, ENUM_TYPE_NAME);
    }

    private void setUpUseJava5Enums(boolean useFeature) throws CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        IIpsArtefactBuilderSetConfigModel builderConfig = properties.getBuilderSetConfig();
        String booleanLiteral = useFeature ? "true" : "false";
        builderConfig.setPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_USE_ENUMS, booleanLiteral, null);
        ipsProject.setProperties(properties);
    }

    public void testGetGeneratedJavaElementsForType() {
        generatedJavaElements = builder.getGeneratedJavaElements(enumType);
        assertTrue(generatedJavaElements.contains(javaEnum));
    }

    public void testGetGeneratedJavaElementsForAttributeAbstractAndUseEnums() throws CoreException {
        setUpUseJava5Enums(true);

        generatedJavaElements = builder.getGeneratedJavaElements(idAttribute);
        expectMemberVar(idAttribute, false);
        expectGetterMethod(idAttribute, true);
        expectGetValueByMethod(idAttribute, false);
        expectIsValueByMethod(idAttribute, false);

        generatedJavaElements = builder.getGeneratedJavaElements(nameAttribute);
        expectMemberVar(nameAttribute, false);
        expectGetterMethod(nameAttribute, true);
        expectGetValueByMethod(nameAttribute, false);
        expectIsValueByMethod(nameAttribute, false);
    }

    public void testGetGeneratedJavaElementsForAttributeAbstractAndNotUseEnums() throws CoreException {
        setUpUseJava5Enums(false);

        generatedJavaElements = builder.getGeneratedJavaElements(idAttribute);
        expectMemberVar(idAttribute, true);
        expectGetterMethod(idAttribute, true);
        expectGetValueByMethod(idAttribute, false);
        expectIsValueByMethod(idAttribute, false);

        generatedJavaElements = builder.getGeneratedJavaElements(nameAttribute);
        expectMemberVar(nameAttribute, true);
        expectGetterMethod(nameAttribute, true);
        expectGetValueByMethod(nameAttribute, false);
        expectIsValueByMethod(nameAttribute, false);
    }

    public void testGetGeneratedJavaElementsForAttributeNotAbstractAndUseEnums() throws CoreException {
        enumType.setAbstract(false);
        setUpUseJava5Enums(true);

        generatedJavaElements = builder.getGeneratedJavaElements(idAttribute);
        expectMemberVar(idAttribute, true);
        expectGetterMethod(idAttribute, true);
        expectGetValueByMethod(idAttribute, true);
        expectIsValueByMethod(idAttribute, true);

        generatedJavaElements = builder.getGeneratedJavaElements(nameAttribute);
        expectMemberVar(nameAttribute, true);
        expectGetterMethod(nameAttribute, true);
        expectGetValueByMethod(nameAttribute, false);
        expectIsValueByMethod(nameAttribute, false);
    }

    public void testGetGeneratedJavaElementsForAttributeNotAbstractAndNotUseEnums() throws CoreException {
        enumType.setAbstract(false);
        setUpUseJava5Enums(false);

        generatedJavaElements = builder.getGeneratedJavaElements(idAttribute);
        expectMemberVar(idAttribute, true);
        expectGetterMethod(idAttribute, true);
        expectGetValueByMethod(idAttribute, true);
        expectIsValueByMethod(idAttribute, true);

        generatedJavaElements = builder.getGeneratedJavaElements(nameAttribute);
        expectMemberVar(nameAttribute, true);
        expectGetterMethod(nameAttribute, true);
        expectGetValueByMethod(nameAttribute, false);
        expectIsValueByMethod(nameAttribute, false);

        generatedJavaElements = builder.getGeneratedJavaElements(inheritedAttribute);
        expectMemberVar(inheritedAttribute, false);
        expectGetterMethod(inheritedAttribute, false);
        expectGetValueByMethod(inheritedAttribute, false);
        expectIsValueByMethod(inheritedAttribute, false);
    }

    private void expectMemberVar(IEnumAttribute enumAttribute, boolean shallExist) {
        String memberVarName = builder.getMemberVarName(enumAttribute.getName());
        IField memberVar = javaEnum.getField(memberVarName);
        if (shallExist) {
            assertTrue(generatedJavaElements.contains(memberVar));
        } else {
            assertFalse(generatedJavaElements.contains(memberVar));
        }
    }

    private void expectGetterMethod(IEnumAttribute enumAttribute, boolean shallExist) throws CoreException {
        String methodName = builder.getMethodNameGetter(enumAttribute);
        IMethod getterMethod = javaEnum.getMethod(methodName, new String[0]);
        if (shallExist) {
            assertTrue(generatedJavaElements.contains(getterMethod));
        } else {
            assertFalse(generatedJavaElements.contains(getterMethod));
        }
    }

    private void expectGetValueByMethod(IEnumAttribute enumAttribute, boolean shallExist) throws CoreException {
        String methodName = builder.getMethodNameGetValueBy(enumAttribute);
        ValueDatatype datatype = enumAttribute.findDatatype(ipsProject);
        IMethod method = javaEnum.getMethod(methodName, new String[] { "Q" + datatype.getQualifiedName() + ";" });
        if (shallExist) {
            assertTrue(generatedJavaElements.contains(method));
        } else {
            assertFalse(generatedJavaElements.contains(method));
        }
    }

    private void expectIsValueByMethod(IEnumAttribute enumAttribute, boolean shallExist) throws CoreException {
        String methodName = builder.getMethodNameIsValueBy(enumAttribute);
        ValueDatatype datatype = enumAttribute.findDatatype(ipsProject);
        IMethod method = javaEnum.getMethod(methodName, new String[] { "Q" + datatype.getQualifiedName() + ";" });
        if (shallExist) {
            assertTrue(generatedJavaElements.contains(method));
        } else {
            assertFalse(generatedJavaElements.contains(method));
        }
    }

}
