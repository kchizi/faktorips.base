/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.controls.valuesets;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetSpecificationControl.ValueSetPmo;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ValueSetSpecificationControlTest extends AbstractIpsPluginTest {
    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private ProductCmptType productCmptType;
    private ProductCmptType superProductCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
        superProductCmptType = newProductCmptType(ipsProject, "SuperProduct");
        productCmptType = (ProductCmptType)policyCmptType.findProductCmptType(ipsProject);
        productCmptType.setSupertype(superProductCmptType.getQualifiedName());
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);
        productCmpt.getIpsSrcFile().save(true, null);
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });
    }

    @Test
    public void testValidatePMO_configElement() throws CoreException {

        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attr");
        attr.setDatatype(ValueDatatype.INTEGER.getQualifiedName());
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.getValueSet().setContainsNull(false);

        IConfigElement configElement = generation.newConfigElement();
        configElement.setPolicyCmptTypeAttribute("attr");
        configElement.setValueSetType(ValueSetType.UNRESTRICTED);
        configElement.getValueSet().setAbstract(false);
        configElement.getValueSet().setContainsNull(true);

        assertHasNullNotAllowedMessage(configElement);
    }

    @Test
    public void testValidatePMO_overwrittenAttribute() throws CoreException {
        IProductCmptTypeAttribute attr = superProductCmptType.newProductCmptTypeAttribute("attr");
        IProductCmptTypeAttribute overwritingAttr = productCmptType.newProductCmptTypeAttribute("attr");
        attr.setDatatype("Integer");
        overwritingAttr.setDatatype("Integer");
        overwritingAttr.setOverwrite(true);

        List<String> listWithNull = list(null, "1", "2", "3", "4");
        List<String> normalValues = list("1", "9", "99", "999");
        attr.setValueSetCopy(new EnumValueSet(attr, normalValues, "partId"));
        overwritingAttr.setValueSetCopy(new EnumValueSet(overwritingAttr, listWithNull, "partId"));

        assertHasNullNotAllowedMessage(overwritingAttr);
    }

    private void assertHasNullNotAllowedMessage(IValueSetOwner valueSetOwner) throws CoreException {
        MessageList messageList = new ValueSetSpecificationControl.ValueSetPmo(valueSetOwner).validate(ipsProject);
        assertNotNull(messageList.getMessageByCode(ValueSetPmo.MSG_CODE_NULL_NOT_ALLOWED));
    }

    private List<String> list(String... values) {
        return Arrays.asList(values);
    }
}