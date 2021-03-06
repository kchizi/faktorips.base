/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.valueset;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveIntegerDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RangeValueSetTest extends AbstractIpsPluginTest {

    private IPolicyCmptTypeAttribute attr;
    private IConfiguredValueSet cValueSet;
    private IConfiguredValueSet intEl;

    private IIpsProject ipsProject;
    private IProductCmptGeneration generation;

    private IPolicyCmptType policyCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "test.Base", "test.Product");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);
        attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attr");
        attr.setDatatype(Datatype.MONEY.getQualifiedName());

        IProductCmpt cmpt = newProductCmpt(productCmptType, "test.Product");
        generation = (IProductCmptGeneration)cmpt.newGeneration(new GregorianCalendar(20006, 4, 26));

        cValueSet = generation.newPropertyValue(attr, IConfiguredValueSet.class);

        IPolicyCmptTypeAttribute attr2 = policyCmptType.newPolicyCmptTypeAttribute();
        attr2.setName("test");
        attr2.setDatatype(Datatype.INTEGER.getQualifiedName());
        attr2.setProductRelevant(true);

        intEl = generation.newPropertyValue(attr2, IConfiguredValueSet.class);
    }

    @Test
    public void testCreateFromXml() {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();

        // old format
        Element element = XmlUtil.getFirstElement(root);
        IRangeValueSet range = new RangeValueSet(cValueSet, "1");
        range.initFromXml(element);
        assertEquals("42", range.getLowerBound());
        assertEquals("trulala", range.getUpperBound());
        assertEquals("4", range.getStep());
        assertTrue(range.isContainsNull());

        // new format
        element = XmlUtil.getElement(root, 1);
        range = new RangeValueSet(cValueSet, "1");
        range.initFromXml(element);
        assertEquals("1", range.getLowerBound());
        assertEquals("10", range.getUpperBound());
        assertEquals("2", range.getStep());
        assertTrue(range.isContainsNull());

    }

    @Test
    public void testToXml() {
        IRangeValueSet range = new RangeValueSet(cValueSet, "1");
        range.setLowerBound("10");
        range.setUpperBound("100");
        range.setStep("10");
        range.setContainsNull(true);
        Element element = range.toXml(newDocument());
        IRangeValueSet r2 = new RangeValueSet(cValueSet, "1");
        r2.initFromXml(element);
        assertEquals(range.getLowerBound(), r2.getLowerBound());
        assertEquals(range.getUpperBound(), r2.getUpperBound());
        assertEquals(range.getStep(), r2.getStep());
        assertEquals(range.isContainsNull(), r2.isContainsNull());
    }

    @Test
    public void testContainsValue() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "50");
        range.setLowerBound("20");
        range.setUpperBound("25");
        assertTrue(range.containsValue("20", ipsProject));
        assertTrue(range.containsValue("22", ipsProject));
        assertTrue(range.containsValue("25", ipsProject));

        assertFalse(range.containsValue("19", ipsProject));
        assertFalse(range.containsValue("26", ipsProject));
        assertFalse(range.containsValue("19", ipsProject));
        assertFalse(range.containsValue("20EUR", ipsProject));

        range.setContainsNull(false);
        assertFalse(range.containsValue(null, ipsProject));

        range.setContainsNull(true);
        assertTrue(range.containsValue(null, ipsProject));

        range.setStep("2");
        range.setUpperBound("26");
        assertTrue(range.containsValue("20", ipsProject));
        assertTrue(range.containsValue("24", ipsProject));
        assertTrue(range.containsValue("26", ipsProject));
        assertFalse(range.containsValue("18", ipsProject));
        assertFalse(range.containsValue("21", ipsProject));
        assertFalse(range.containsValue("28", ipsProject));
    }

    @Test
    public void testContainsValue_primitiveWithStepNull() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        range.setLowerBound("1");
        range.setUpperBound("100");
        range.setStep(null);

        assertTrue(range.containsValue("1", ipsProject));
        assertTrue(range.containsValue("22", ipsProject));
        assertTrue(range.containsValue("100", ipsProject));
        assertFalse(range.containsValue("0", ipsProject));
        assertFalse(range.containsValue("101", ipsProject));
    }

    @Test
    public void testContainsValue_nullLowerBounds() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound(null);
        range.setUpperBound("100");
        range.setStep(null);

        assertTrue(range.containsValue("5", ipsProject));
    }

    @Test
    public void testContainsValue_nullLowerBoundsWithStep() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound(null);
        range.setUpperBound("100");
        range.setStep("1");

        assertFalse(range.containsValue("5", ipsProject));
    }

    @Test
    public void testContainsValue_nullUpperBounds() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound("1");
        range.setUpperBound(null);
        range.setStep(null);

        assertTrue(range.containsValue("5", ipsProject));
    }

    @Test
    public void testContainsValue_nullUpperBoundsWithStep() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound("1");
        range.setUpperBound(null);
        range.setStep("1");

        assertTrue(range.containsValue("5", ipsProject));
    }

    @Test
    public void testContainsValue_nullLowerUpperBounds() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound(null);
        range.setUpperBound(null);
        range.setStep(null);

        assertTrue(range.containsValue("5", ipsProject));
    }

    @Test
    public void testContainsValue_nullLowerUpperBoundsWithStep() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound("1");
        range.setUpperBound(null);
        range.setStep("1");

        assertTrue(range.containsValue("5", ipsProject));
    }

    @Test
    public void testContainsValueSet_nullRange() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound(null);
        range.setUpperBound(null);
        range.setStep("1");
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound(null);
        subRange.setUpperBound(null);
        subRange.setStep("1");

        assertFalse(range.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_lowerNull() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound(null);
        range.setUpperBound("100");
        range.setStep("1");
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound(null);
        subRange.setUpperBound(null);
        subRange.setStep("1");

        assertFalse(range.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_lowerNullSubNonNull() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound(null);
        range.setUpperBound("100");
        range.setStep("1");
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound("0");
        subRange.setUpperBound("100");
        subRange.setStep("1");

        assertFalse(range.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_subLowerNull() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound(null);
        range.setUpperBound("100");
        range.setStep("1");
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound("0");
        subRange.setUpperBound("100");
        subRange.setStep("1");

        assertFalse(range.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_subLowerNullOuterNull() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound(null);
        range.setUpperBound(null);
        range.setStep("1");
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound(null);
        subRange.setUpperBound("100");
        subRange.setStep("1");

        assertFalse(range.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_upperNull() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound("1");
        range.setUpperBound(null);
        range.setStep("1");
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound(null);
        subRange.setUpperBound(null);
        subRange.setStep("1");

        assertFalse(range.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_upperNullSubNonNull() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound("0");
        range.setUpperBound(null);
        range.setStep("1");
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound("0");
        subRange.setUpperBound("100");
        subRange.setStep("1");

        assertTrue(range.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_subUpperBoundNull() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound("0");
        range.setUpperBound("100");
        range.setStep("1");
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound("0");
        subRange.setUpperBound(null);
        subRange.setStep("1");

        assertFalse(range.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_subUpperNull() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound(null);
        range.setUpperBound(null);
        range.setStep(null);
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound("1");
        subRange.setUpperBound(null);
        subRange.setStep("1");

        assertTrue(range.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_subUpperNullOuterNull() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "idXY");
        intEl.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        range.setLowerBound(null);
        range.setUpperBound(null);
        range.setStep(null);
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound("0");
        subRange.setUpperBound(null);
        subRange.setStep("1");

        assertTrue(range.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_BothSetsAreRanges() {
        RangeValueSet range = new RangeValueSet(intEl, "50");
        range.setLowerBound("10");
        range.setUpperBound("20");
        range.setStep("2");

        IRangeValueSet abstractRange = new RangeValueSet(intEl, "200");
        abstractRange.setLowerBound("10");
        abstractRange.setUpperBound("14");
        abstractRange.setStep("2");
        abstractRange.setAbstract(true);
        assertTrue(abstractRange.containsValueSet(range));
        assertFalse(range.containsValueSet(abstractRange));

        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound("10");
        subRange.setUpperBound("20");
        subRange.setStep("2");
        assertTrue(range.containsValueSet(subRange));

        subRange.setStep("3");
        assertFalse(range.containsValueSet(subRange));

        subRange.setStep("2");
        subRange.setLowerBound("8");
        assertFalse(range.containsValueSet(subRange));

        subRange.setLowerBound("14");
        assertTrue(range.containsValueSet(subRange));

        subRange.setUpperBound("18");
        assertTrue(range.containsValueSet(subRange));

        subRange.setUpperBound("24");
        assertFalse(range.containsValueSet(subRange));

        assertFalse(range.containsValueSet(subRange));

        subRange.setUpperBound("18");
        assertTrue(range.containsValueSet(subRange));

        range.setContainsNull(false);
        subRange.setContainsNull(true);
        assertFalse(range.containsValueSet(subRange));

        range.setContainsNull(true);
        assertTrue(range.containsValueSet(subRange));

        range.setUpperBound("");
        range.setLowerBound("");
        range.setStep("");

        assertTrue(range.containsValueSet(subRange));

        subRange.setUpperBound("");
        subRange.setLowerBound("");
        subRange.setStep("");

        assertTrue(range.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_EqualValueSetsWithNull() {
        RangeValueSet superRange = new RangeValueSet(intEl, "50");
        superRange.setContainsNull(true);
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setContainsNull(true);

        assertTrue(superRange.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_EqualValueSetsWithoutNull() {
        RangeValueSet superRange = new RangeValueSet(intEl, "50");
        superRange.setContainsNull(false);
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setContainsNull(false);

        assertTrue(superRange.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_ContainsSubValueSet() {
        RangeValueSet superRange = new RangeValueSet(intEl, "50");
        superRange.setContainsNull(true);
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setContainsNull(false);

        assertTrue(superRange.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_ContainsSubValueSetNot() {
        RangeValueSet superRange = new RangeValueSet(intEl, "50");
        superRange.setContainsNull(false);
        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setContainsNull(true);

        assertFalse(superRange.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSetEmptyWithDecimal() throws Exception {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attrX");
        attr.setDatatype(Datatype.DECIMAL.getQualifiedName());
        attr.setProductRelevant(true);
        attr.setValueSetType(ValueSetType.RANGE);

        IConfiguredValueSet cValueSet1 = generation.newPropertyValue(attr, IConfiguredValueSet.class);

        RangeValueSet range = new RangeValueSet(cValueSet1, "10");
        assertEquals(range.findValueDatatype(ipsProject).getQualifiedName(), Datatype.DECIMAL.getQualifiedName());
        RangeValueSet subset = new RangeValueSet(cValueSet1, "20");
        assertEquals(subset.findValueDatatype(ipsProject).getQualifiedName(), Datatype.DECIMAL.getQualifiedName());

        assertTrue(range.containsValueSet(subset));
    }

    @Test
    public void testValidate() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "50");
        range.setLowerBound("20");
        range.setUpperBound("25");
        MessageList list = range.validate(ipsProject);
        assertTrue(list.isEmpty());

        range.setLowerBound("blabla");
        list = range.validate(ipsProject);
        assertFalse(list.isEmpty());

        range.setLowerBound("22");
        range.setUpperBound("blabla");
        list.clear();
        list = range.validate(ipsProject);
        assertFalse(list.isEmpty());

        range.setUpperBound("12");
        list.clear();
        list = range.validate(ipsProject);
        assertFalse(list.isEmpty());

        range.setLowerBound(null);
        range.setUpperBound(null);
        list.clear();
        list = range.validate(ipsProject);
        assertFalse(list.containsErrorMsg());

        Datatype[] vds = ipsProject.findDatatypes(true, false);
        ArrayList<Datatype> vdlist = new ArrayList<Datatype>();
        vdlist.addAll(Arrays.asList(vds));
        vdlist.add(new PrimitiveIntegerDatatype());
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPredefinedDatatypesUsed(vdlist.toArray(new ValueDatatype[vdlist.size()]));
        ipsProject.setProperties(properties);

        IPolicyCmptTypeAttribute attr = intEl.findPcTypeAttribute(ipsProject);
        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());

        list = range.validate(ipsProject);
        assertNull(list.getMessageByCode(IValueSet.MSGCODE_NULL_NOT_SUPPORTED));
    }

    @Test
    public void testIsContainsNullPrimitive() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, "50");

        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        assertFalse(range.isContainsNull());
    }

    @Test
    public void testContainsValueSetStep() {
        RangeValueSet range = new RangeValueSet(intEl, "50");
        range.setLowerBound("10");
        range.setUpperBound("20");
        range.setStep("2");

        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound("12");
        subRange.setUpperBound("20");
        subRange.setStep("2");

        assertTrue(range.containsValueSet(subRange));

        subRange.setStep("4");
        assertTrue(range.containsValueSet(subRange));

        subRange.setUpperBound("21");
        subRange.setStep("3");
        assertFalse(range.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_stepNull() {
        RangeValueSet range = new RangeValueSet(intEl, "50");
        range.setLowerBound("10");
        range.setUpperBound("20");
        range.setStep(null);

        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound("12");
        subRange.setUpperBound("20");
        subRange.setStep(null);

        assertTrue(range.containsValueSet(subRange));

        range.setStep(null);
        subRange.setStep("2");
        assertTrue(range.containsValueSet(subRange));

        range.setStep(null);
        subRange.setStep("27");
        assertFalse(range.containsValueSet(subRange));

        range.setStep("2");
        subRange.setStep(null);
        assertFalse(range.containsValueSet(subRange));
    }

    @Test
    public void testContainsValueSet_EnumSet_BothBounds() {
        RangeValueSet rangeSet = new RangeValueSet(intEl, "50");
        rangeSet.setLowerBound("10");
        rangeSet.setUpperBound("100");
        rangeSet.setStep("10");
        EnumValueSet enumSet1 = new EnumValueSet(intEl, "1000");
        enumSet1.addValue("30");
        enumSet1.addValue("80");
        EnumValueSet enumSet2 = new EnumValueSet(intEl, "1001");
        enumSet2.addValue("-20");

        assertThat(rangeSet.containsValueSet(enumSet1), is(true));
        assertThat(rangeSet.containsValueSet(enumSet2), is(false));
    }

    @Test
    public void testContainsValueSet_EnumSet_LowerBound() {
        RangeValueSet rangeSet = new RangeValueSet(intEl, "50");
        rangeSet.setLowerBound("0");
        rangeSet.setUpperBound("");
        rangeSet.setStep("3");
        EnumValueSet enumSet1 = new EnumValueSet(intEl, "1000");
        enumSet1.addValue("33");
        enumSet1.addValue("33333");
        enumSet1.addValue("333333333");
        EnumValueSet enumSet2 = new EnumValueSet(intEl, "1001");
        enumSet2.addValue("-3");

        assertThat(rangeSet.containsValueSet(enumSet1), is(true));
        assertThat(rangeSet.containsValueSet(enumSet2), is(false));
    }

    @Test
    public void testContainsValueSet_EnumSet_UpperBound() {
        RangeValueSet rangeSet = new RangeValueSet(intEl, "50");
        rangeSet.setLowerBound("");
        rangeSet.setUpperBound("25");
        EnumValueSet enumSet1 = new EnumValueSet(intEl, "1000");
        enumSet1.addValue("0");
        enumSet1.addValue("-34737");
        EnumValueSet enumSet2 = new EnumValueSet(intEl, "1001");
        enumSet2.addValue("20");
        enumSet2.addValue("25");
        enumSet2.addValue("30");

        assertThat(rangeSet.containsValueSet(enumSet1), is(true));
        assertThat(rangeSet.containsValueSet(enumSet2), is(false));
    }

    @Test
    public void testContainsValueSet_EnumSet_Null() {
        RangeValueSet rangeSetWithNull = new RangeValueSet(intEl, "50");
        rangeSetWithNull.setContainsNull(true);
        RangeValueSet rangeSetWithoutNull = new RangeValueSet(intEl, "51");
        rangeSetWithoutNull.setContainsNull(false);
        EnumValueSet enumSetWithNull = new EnumValueSet(intEl, "1000");
        enumSetWithNull.setContainsNull(true);
        EnumValueSet enumSetWithoutNull = new EnumValueSet(intEl, "1001");
        enumSetWithoutNull.setContainsNull(false);

        assertThat(rangeSetWithNull.containsValueSet(enumSetWithNull), is(true));
        assertThat(rangeSetWithNull.containsValueSet(enumSetWithoutNull), is(true));
        assertThat(rangeSetWithoutNull.containsValueSet(enumSetWithNull), is(false));
        assertThat(rangeSetWithoutNull.containsValueSet(enumSetWithoutNull), is(true));
    }

    @Test
    public void testContainsValueSet_EnumSet_Abstract() {
        RangeValueSet abstractRangeSet = new RangeValueSet(intEl, "50");
        abstractRangeSet.setAbstract(true);
        RangeValueSet concreteRangeSet = new RangeValueSet(intEl, "51");
        concreteRangeSet.setAbstract(false);
        EnumValueSet abstractEnumSet = new EnumValueSet(intEl, "1000");
        abstractEnumSet.setAbstract(true);
        EnumValueSet concreteEnumSet = new EnumValueSet(intEl, "1001");
        concreteEnumSet.setAbstract(false);

        assertThat(abstractRangeSet.containsValueSet(abstractEnumSet), is(true));
        assertThat(abstractRangeSet.containsValueSet(concreteEnumSet), is(true));
        assertThat(concreteRangeSet.containsValueSet(abstractEnumSet), is(false));
        assertThat(concreteRangeSet.containsValueSet(concreteEnumSet), is(true));
    }

    @Test
    public void testContainsValueSet_equalStepOtherBounds() {
        RangeValueSet range = new RangeValueSet(intEl, "50");
        range.setLowerBound("10");
        range.setUpperBound("20");
        range.setStep("2");

        RangeValueSet subRange = new RangeValueSet(intEl, "100");
        subRange.setLowerBound("11");
        subRange.setUpperBound("19");
        subRange.setStep("2");

        assertFalse(range.containsValueSet(subRange));

        range.setStep(null);

        assertTrue(range.containsValueSet(subRange));

        range.setStep("2");
        subRange.setLowerBound("12");
        subRange.setUpperBound("20");
        assertTrue(range.containsValueSet(subRange));
    }

    @Test
    public void testCompareTo_Eq() throws Exception {
        IRangeValueSet range1 = createRange("0", "1", "1");
        IRangeValueSet range2 = createRange("0", "1", "1");

        assertThat(range1.compareTo(range2), is(0));
        assertThat(range2.compareTo(range1), is(0));
    }

    @Test
    public void testCompareTo_Eq_datatypeCompare() throws Exception {
        IRangeValueSet range1 = createRange("0", "01", "1");
        IRangeValueSet range2 = createRange("00", "1", "1");

        assertThat(range1.compareTo(range2), is(0));
        assertThat(range2.compareTo(range1), is(0));
    }

    @Test
    public void testCompareTo_Eq_LowNull() throws Exception {
        IRangeValueSet range1 = createRange(null, "1", null);
        IRangeValueSet range2 = createRange(null, "1", null);

        assertThat(range1.compareTo(range2), is(0));
        assertThat(range2.compareTo(range1), is(0));
    }

    @Test
    public void testCompareTo_Eq_UpNull() throws Exception {
        IRangeValueSet range1 = createRange("1", null, null);
        IRangeValueSet range2 = createRange("1", null, null);

        assertThat(range1.compareTo(range2), is(0));
        assertThat(range2.compareTo(range1), is(0));
    }

    @Test
    public void testCompareTo_Eq_AllNull() throws Exception {
        IRangeValueSet range1 = createRange(null, null, null);
        IRangeValueSet range2 = createRange(null, null, null);

        assertThat(range1.compareTo(range2), is(0));
        assertThat(range2.compareTo(range1), is(0));
    }

    @Test
    public void testCompareTo_Included() throws Exception {
        IRangeValueSet range1 = createRange("1", "100", null);
        IRangeValueSet range2 = createRange("20", "80", null);

        assertThat(range1.compareTo(range2), is(1));
        assertThat(range2.compareTo(range1), is(-1));
    }

    @Test
    public void testCompareTo_LowerEq() throws Exception {
        IRangeValueSet range1 = createRange("1", "100", null);
        IRangeValueSet range2 = createRange("1", "80", null);

        assertThat(range1.compareTo(range2), is(1));
        assertThat(range2.compareTo(range1), is(-1));
    }

    @Test
    public void testCompareTo_LowerEq_DiffStep() throws Exception {
        IRangeValueSet range1 = createRange("1", "100", "1");
        IRangeValueSet range2 = createRange("1", "80", "2");

        // less-than matcher not available in current hamcrest version
        assertTrue(range1.compareTo(range2) < 0);
        assertTrue(range2.compareTo(range1) > 0);
    }

    @Test
    public void testCompareTo_UpperEq() throws Exception {
        IRangeValueSet range1 = createRange("1", "100", null);
        IRangeValueSet range2 = createRange("2", "100", null);

        assertThat(range1.compareTo(range2), is(1));
        assertThat(range2.compareTo(range1), is(-1));
    }

    @Test
    public void testCompareTo_DifferentStep() throws Exception {
        IRangeValueSet range1 = createRange("1", "100", "1");
        IRangeValueSet range2 = createRange("1", "100", "2");

        assertThat(range1.compareTo(range2), is(-1));
        assertThat(range2.compareTo(range1), is(1));
    }

    @Test
    public void testCompareTo_Intercept() throws Exception {
        IRangeValueSet range1 = createRange("1", "100", "1");
        IRangeValueSet range2 = createRange("2", "110", "1");

        assertThat(range1.compareTo(range2), is(-1));
        assertThat(range2.compareTo(range1), is(1));
    }

    @Test
    public void testCompareTo_Intercept_DiffStep() throws Exception {
        IRangeValueSet range1 = createRange("1", "100", "1");
        IRangeValueSet range2 = createRange("2", "110", "2");

        assertThat(range1.compareTo(range2), is(-1));
        assertThat(range2.compareTo(range1), is(1));
    }

    @Test
    public void testCompareTo_Distinct() throws Exception {
        IRangeValueSet range1 = createRange("1", "100", "1");
        IRangeValueSet range2 = createRange("101", "110", "1");

        // less-than matcher not available in current hamcrest version
        assertTrue(range1.compareTo(range2) < 0);
        assertTrue(range2.compareTo(range1) > 0);
    }

    private IRangeValueSet createRange(String lower, String upper, String step) {
        RangeValueSet range = new RangeValueSet(intEl, "1234");
        range.setLowerBound(lower);
        range.setUpperBound(upper);
        range.setStep(step);
        return range;
    }

}
