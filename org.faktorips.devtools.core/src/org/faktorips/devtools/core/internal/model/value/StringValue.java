/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.value;

import java.util.Observer;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Value-Representation of a String
 * 
 * @author frank
 * @since 3.9
 */
public class StringValue extends AbstractValue<String> {

    private final String content;

    /**
     * New StringValue with String
     * 
     * @param content String
     */
    public StringValue(String content) {
        this.content = content;
    }

    /**
     * Create a new StringValue from the XML-Text-Node
     * 
     * @param text XML-Text
     * @return new StringValue
     */
    public static StringValue createFromXml(Text text) {
        return new StringValue(text.getNodeValue());
    }

    @Override
    public Node toXml(Document doc) {
        if (getContent() != null) {
            return doc.createTextNode(getContent());
        } else {
            return doc.createTextNode(StringUtils.EMPTY);
        }
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getContentAsString() {
        return getContent();
    }

    @Override
    public String toString() {
        return getContent();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof StringValue)) {
            return false;
        }
        StringValue other = (StringValue)obj;
        if (getContent() == null) {
            if (other.getContent() != null) {
                return false;
            }
        } else if (!getContent().equals(other.getContent())) {
            return false;
        }
        return true;
    }

    @Override
    public void validate(ValueDatatype datatype,
            String datatypeName,
            IIpsProject ipsproject,
            MessageList list,
            ObjectProperty... objectProperties) {
        MessageList newMsgList = new MessageList();
        ValidationUtils.checkValue(datatype, datatypeName, getContent(),
                objectProperties[0].getObject(), objectProperties[0].getProperty(), newMsgList);
        for (Message message : newMsgList) {
            list.add(new Message(message.getCode(), message.getText(), message.getSeverity(), objectProperties));
        }
    }

    @Override
    public void addObserver(Observer observer) {
        // no implementation
    }

    @Override
    public void deleteObserver(Observer observer) {
        // no implementation
    }

    @Override
    public int compare(IValue<?> other, ValueDatatype valueDatatype) {
        if (other instanceof StringValue) {
            if (valueDatatype.supportsCompare()) {
                return valueDatatype.compare(content, (String)other.getContent());
            }
        }
        if (other == null) {
            return 1;
        } else {
            return ObjectUtils.compare(getContentAsString(), other.getContentAsString());
        }
    }

}
