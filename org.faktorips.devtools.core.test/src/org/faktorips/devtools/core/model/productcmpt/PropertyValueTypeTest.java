/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Comparator;

import com.google.common.base.Function;

import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.util.functional.BiConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PropertyValueTypeTest {

    private static final String ANY_VALUE = "anyValue";

    @Mock
    private IValueSet valueSet1;

    @Mock
    private IValueSet valueSet2;

    @Mock
    private IAttributeValue attributeValue;

    @Mock
    private IConfigElement configElement;

    @Mock
    private ITableContentUsage tableContentUsage;

    @Mock
    private IFormula formula;

    @Mock
    private IValidationRuleConfig ruleConfig;

    @Test
    public void testGetValueComparator_valueSet_eq() throws Exception {
        when(valueSet1.containsValueSet(valueSet2)).thenReturn(true);
        when(valueSet2.containsValueSet(valueSet1)).thenReturn(true);

        Comparator<Object> valueComparator = PropertyValueType.CONFIG_ELEMENT.getValueComparator();
        assertThat(valueComparator.compare(valueSet1, valueSet2), is(0));
    }

    @Test
    public void testGetValueComparator_valueSet_gt() throws Exception {
        when(valueSet1.containsValueSet(valueSet2)).thenReturn(true);

        Comparator<Object> valueComparator = PropertyValueType.CONFIG_ELEMENT.getValueComparator();
        assertThat(valueComparator.compare(valueSet1, valueSet2), is(1));
    }

    @Test
    public void testGetValueComparator_valueSet_lt() throws Exception {
        when(valueSet2.containsValueSet(valueSet1)).thenReturn(true);

        Comparator<Object> valueComparator = PropertyValueType.CONFIG_ELEMENT.getValueComparator();
        assertThat(valueComparator.compare(valueSet1, valueSet2), is(-1));
    }

    @Test
    public void testGetValueComparator_other_eq() throws Exception {
        Comparator<Object> valueComparator = PropertyValueType.ATTRIBUTE_VALUE.getValueComparator();
        assertThat(valueComparator.compare("abc", "abc"), is(0));
    }

    @Test
    public void testGetValueComparator_other_gt() throws Exception {
        Comparator<Object> valueComparator = PropertyValueType.ATTRIBUTE_VALUE.getValueComparator();
        assertTrue(valueComparator.compare("abx", "abc") > 0);
    }

    @Test
    public void testGetValueComparator_other_lt() throws Exception {
        Comparator<Object> valueComparator = PropertyValueType.ATTRIBUTE_VALUE.getValueComparator();
        assertTrue(valueComparator.compare("abc", "abx") < 0);
    }

    @Test
    public void testGetValueComparator_nullValues() throws Exception {
        Comparator<Object> valueComparator = PropertyValueType.ATTRIBUTE_VALUE.getValueComparator();
        assertTrue(valueComparator.compare(null, "abx") < 0);
        assertTrue(valueComparator.compare("abc", null) > 0);
        assertTrue(valueComparator.compare(null, null) == 0);
    }

    @Test
    public void testGetValueGetter_attributeValue() throws Exception {
        IValueHolder<?> value = mock(IValueHolder.class);
        doReturn(value).when(attributeValue).getValueHolder();

        Function<IPropertyValue, Object> valueFunction = PropertyValueType.ATTRIBUTE_VALUE.getValueGetter();
        assertThat(valueFunction.apply(attributeValue), is((Object)value));
    }

    @Test
    public void testGetValueGetter_configElement() throws Exception {
        when(configElement.getValueSet()).thenReturn(valueSet1);

        Function<IPropertyValue, Object> valueFunction = PropertyValueType.CONFIG_ELEMENT.getValueGetter();
        assertThat(valueFunction.apply(configElement), is((Object)valueSet1));
    }

    @Test
    public void testGetValueGetter_tableContent() throws Exception {
        when(tableContentUsage.getTableContentName()).thenReturn(ANY_VALUE);

        Function<IPropertyValue, Object> valueFunction = PropertyValueType.TABLE_CONTENT_USAGE.getValueGetter();
        assertThat(valueFunction.apply(tableContentUsage), is((Object)ANY_VALUE));
    }

    @Test
    public void testGetValueGetter_formula() throws Exception {
        when(formula.getExpression()).thenReturn(ANY_VALUE);

        Function<IPropertyValue, Object> valueFunction = PropertyValueType.FORMULA.getValueGetter();
        assertThat(valueFunction.apply(formula), is((Object)ANY_VALUE));
    }

    @Test
    public void testGetValueGetter_ruleConfig() throws Exception {
        when(ruleConfig.isActive()).thenReturn(true);

        Function<IPropertyValue, Object> valueFunction = PropertyValueType.VALIDATION_RULE_CONFIG.getValueGetter();
        assertThat(valueFunction.apply(ruleConfig), is((Object)true));
    }

    @Test
    public void testGetValueSetter_AttributeValue() throws Exception {
        IValueHolder<?> value = mock(IValueHolder.class);
        IValueHolder<?> copy = mock(IValueHolder.class);
        doReturn(copy).when(value).copy(attributeValue);

        BiConsumer<IPropertyValue, Object> valueConsumer = PropertyValueType.ATTRIBUTE_VALUE.getValueSetter();
        valueConsumer.accept(attributeValue, value);
        verify(attributeValue).setValueHolder(copy);
    }

    @Test
    public void testGetValueSetter_TableContent() throws Exception {
        BiConsumer<IPropertyValue, Object> valueConsumer = PropertyValueType.TABLE_CONTENT_USAGE.getValueSetter();
        valueConsumer.accept(tableContentUsage, ANY_VALUE);
        verify(tableContentUsage).setTableContentName(ANY_VALUE);
    }

    @Test
    public void testGetValueSetter_TableContent_null() throws Exception {
        BiConsumer<IPropertyValue, Object> valueConsumer = PropertyValueType.TABLE_CONTENT_USAGE.getValueSetter();
        valueConsumer.accept(tableContentUsage, null);
        verify(tableContentUsage).setTableContentName(null);
    }

    @Test
    public void testGetValueSetter_Formula() throws Exception {
        BiConsumer<IPropertyValue, Object> valueConsumer = PropertyValueType.FORMULA.getValueSetter();
        valueConsumer.accept(formula, ANY_VALUE);
        verify(formula).setExpression(ANY_VALUE);
    }

    @Test
    public void testGetValueSetter_Formula_null() throws Exception {
        BiConsumer<IPropertyValue, Object> valueConsumer = PropertyValueType.FORMULA.getValueSetter();
        valueConsumer.accept(formula, null);
        verify(formula).setExpression(null);
    }

    @Test
    public void testGetValueSetter_ConfigElement() throws Exception {
        IValueSet value = mock(IValueSet.class);
        IValueSet copy = mock(IValueSet.class);
        when(value.copy(configElement, ANY_VALUE)).thenReturn(copy);
        IIpsModel ipsModel = mock(IIpsModel.class);
        when(configElement.getIpsModel()).thenReturn(ipsModel);
        when(ipsModel.getNextPartId(configElement)).thenReturn(ANY_VALUE);

        BiConsumer<IPropertyValue, Object> valueConsumer = PropertyValueType.CONFIG_ELEMENT.getValueSetter();
        valueConsumer.accept(configElement, value);
        verify(configElement).setValueSet(copy);
    }

    @Test
    public void testGetValueSetter_RuleConfig() throws Exception {
        BiConsumer<IPropertyValue, Object> valueConsumer = PropertyValueType.VALIDATION_RULE_CONFIG.getValueSetter();
        valueConsumer.accept(ruleConfig, true);
        verify(ruleConfig).setActive(true);
    }

}
