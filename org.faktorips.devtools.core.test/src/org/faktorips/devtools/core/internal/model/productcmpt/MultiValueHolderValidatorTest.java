/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.faktorips.abstracttest.matcher.Matchers.hasMessageCode;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.junit.Test;

public class MultiValueHolderValidatorTest {

    @Test
    public void testValidate_NullValueList() throws CoreException {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        MultiValueHolder valueHolder = new MultiValueHolder(attributeValue, null);
        MultiValueHolderValidator validator = new MultiValueHolderValidator(valueHolder, attributeValue, project);
        assertThat(validator.validate().size(), is(0));
    }

    @Test
    public void testValidate_EmptyValueList() throws CoreException {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        MultiValueHolder valueHolder = new MultiValueHolder(attributeValue);
        MultiValueHolderValidator validator = new MultiValueHolderValidator(valueHolder, attributeValue, project);
        assertThat(validator.validate().size(), is(0));
    }

    @Test
    public void testValidate_OneValue() throws CoreException {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        SingleValueHolder singleValueHolder = mock(SingleValueHolder.class);
        SingleValueHolderValidator singleValueValidator = mock(SingleValueHolderValidator.class);

        MessageList valueMessages = new MessageList();
        List<SingleValueHolder> values = Lists.newArrayList(singleValueHolder);
        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue, values);

        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);
        when(singleValueHolder.newValidator(attributeValue, project)).thenReturn(singleValueValidator);
        when(singleValueValidator.validate()).thenReturn(valueMessages);

        MultiValueHolderValidator validator = new MultiValueHolderValidator(multiValueHolder, attributeValue, project);

        // No errors from single value
        assertTrue(validator.validate().isEmpty());

        // single value reports an error
        valueMessages.newError("code", "text", new ObjectProperty[0]);
        MessageList messageList = validator.validate();

        assertThat(messageList.size(), is(2));
        assertThat(messageList, hasMessageCode("code"));
        assertThat(messageList, hasMessageCode(MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE));

        Message invalidValueMsg = messageList.getMessageByCode(MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE);
        verifyInvalidValueMessage(invalidValueMsg, multiValueHolder, attributeValue);

    }

    @Test
    public void testValidate_multiValues() throws CoreException {

        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        SingleValueHolder singleValue1 = mock(SingleValueHolder.class);
        SingleValueHolder singleValue2 = mock(SingleValueHolder.class);
        SingleValueHolderValidator singleValue1Validator = mock(SingleValueHolderValidator.class);
        SingleValueHolderValidator singleValue2Validator = mock(SingleValueHolderValidator.class);

        MessageList value1Messages = new MessageList();
        MessageList value2Messages = new MessageList();

        List<SingleValueHolder> values = Lists.newArrayList(singleValue1, singleValue2);
        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue, values);

        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        when(singleValue1.newValidator(attributeValue, project)).thenReturn(singleValue1Validator);
        when(singleValue2.newValidator(attributeValue, project)).thenReturn(singleValue2Validator);

        when(singleValue1Validator.validate()).thenReturn(value1Messages);
        when(singleValue2Validator.validate()).thenReturn(value2Messages);

        MultiValueHolderValidator validator = new MultiValueHolderValidator(multiValueHolder, attributeValue, project);

        // No errors from single values
        assertThat(validator.validate().size(), is(0));

        // One single value reports an error
        value1Messages.newError("code1", "text", new ObjectProperty[0]);
        MessageList messageList = validator.validate();

        assertThat(messageList.size(), is(2));
        assertThat(messageList, hasMessageCode("code1"));
        assertThat(messageList, hasMessageCode(MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE));

        // Both single values report errors
        value2Messages.newError("code2", "text", new ObjectProperty[0]);
        messageList = validator.validate();

        assertThat(messageList.size(), is(3));
        assertThat(messageList, hasMessageCode("code1"));
        assertThat(messageList, hasMessageCode("code2"));
        assertThat(messageList, hasMessageCode(MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE));

        Message invalidValueMsg = messageList.getMessageByCode(MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE);
        verifyInvalidValueMessage(invalidValueMsg, multiValueHolder, attributeValue);
    }

    @Test
    public void testValidate_DuplicateValues() throws CoreException {
        IIpsProject project = mock(IIpsProject.class);
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        MultiValueHolder multiValueHolder = spy(new MultiValueHolder(attributeValue));
        doNothing().when(multiValueHolder).objectHasChanged(anyObject(), anyObject());

        List<SingleValueHolder> singleValues = new ArrayList<SingleValueHolder>();
        singleValues.add(new SingleValueHolder(attributeValue, "A"));
        singleValues.add(new SingleValueHolder(attributeValue, "B"));
        singleValues.add(new SingleValueHolder(attributeValue, "A"));
        singleValues.add(new SingleValueHolder(attributeValue, "C"));
        multiValueHolder.setValue(singleValues);

        MultiValueHolderValidator validator = new MultiValueHolderValidator(multiValueHolder, attributeValue, project);
        MessageList messageList = validator.validate().getMessages(Message.ERROR);
        assertThat(messageList.size(), is(2));

        assertThat(messageList, hasMessageCode(MultiValueHolder.MSGCODE_CONTAINS_DUPLICATE_VALUE));
        assertThat(messageList, hasMessageCode(MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE));

        Message invalidValueMsg = messageList.getMessageByCode(MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE);
        verifyInvalidValueMessage(invalidValueMsg, multiValueHolder, attributeValue);

        Message duplicateValueMsg = messageList.getMessageByCode(MultiValueHolder.MSGCODE_CONTAINS_DUPLICATE_VALUE);
        assertThat(duplicateValueMsg.getInvalidObjectProperties()[0].getObject(), is((Object)singleValues.get(0)));
        assertThat(duplicateValueMsg.getInvalidObjectProperties()[0].getProperty(), is(IValueHolder.PROPERTY_VALUE));

    }

    /**
     * Verifies that the invalid object properties of the given message are correct for a
     * {@link MultiValueHolder#MSGCODE_CONTAINS_INVALID_VALUE} message.
     */
    private void verifyInvalidValueMessage(Message message, MultiValueHolder valueHolder, IAttributeValue parent) {
        ObjectProperty firstObjectProperty = message.getInvalidObjectProperties()[0];
        ObjectProperty secondObjectProperty = message.getInvalidObjectProperties()[1];
        assertThat(firstObjectProperty.getObject(), is((Object)parent));
        assertThat(firstObjectProperty.getProperty(), is(IAttributeValue.PROPERTY_VALUE_HOLDER));
        assertThat(secondObjectProperty.getObject(), is((Object)valueHolder));
        assertThat(secondObjectProperty.getProperty(), is(IValueHolder.PROPERTY_VALUE));
    }

}
