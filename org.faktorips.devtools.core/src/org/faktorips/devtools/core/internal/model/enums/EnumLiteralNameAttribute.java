/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <tt>IEnumLiteralNameAttribute</tt>, see the corresponding interface for more
 * details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class EnumLiteralNameAttribute extends EnumAttribute implements IEnumLiteralNameAttribute {

    /**
     * The name of the <tt>IEnumAttribute</tt> that is used as default value provider for
     * enumeration literals.
     */
    private String defaultValueProviderAttribute;

    /**
     * Creates a new <tt>IEnumLiteralNameAttribute</tt>.
     * 
     * @param parent The <tt>IEnumType</tt> this <tt>IEnumLiteralNameAttribute</tt> belongs to.
     * @param id A unique ID for this <tt>IEnumLiteralNameAttribute</tt>.
     */
    public EnumLiteralNameAttribute(IEnumType parent, String id) {
        super(parent, id);
        defaultValueProviderAttribute = ""; //$NON-NLS-1$
    }

    @Override
    public void setDefaultValueProviderAttribute(String defaultValueProviderAttributeName) {
        ArgumentCheck.notNull(defaultValueProviderAttributeName);

        String oldValue = defaultValueProviderAttribute;
        defaultValueProviderAttribute = defaultValueProviderAttributeName;
        valueChanged(oldValue, defaultValueProviderAttributeName);
    }

    @Override
    public String getDefaultValueProviderAttribute() {
        return defaultValueProviderAttribute;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        defaultValueProviderAttribute = element.getAttribute(PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE);
        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE, defaultValueProviderAttribute);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        validateIsNeeded(list);
        if (list.containsErrorMsg()) {
            return;
        }
        validateDefaultValueProviderAttribute(list);
    }

    /**
     * Validates whether this <tt>IEnumLiteralNameAttribute</tt> is needed by the <tt>IEnumType</tt>
     * it belongs to.
     */
    private void validateIsNeeded(MessageList list) {
        IEnumType enumType = getEnumType();
        if (enumType.isAbstract()) {
            String text = Messages.EnumLiteralNameAttribute_NotNeeded;
            Message msg = new Message(MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_NOT_NEEDED, text, Message.ERROR, this);
            list.add(msg);
        }
    }

    /** Validates the <tt>defaultValueProviderAttribute</tt> property. */
    private void validateDefaultValueProviderAttribute(MessageList list) throws CoreException {
        // Pass validation if no provider is specified.
        if (StringUtils.isEmpty(defaultValueProviderAttribute)) {
            return;
        }
        validateValueProviderAttributeExists(list);
        validateValueProviderAttributeHasStringDatatype(list);
    }

    private void validateValueProviderAttributeExists(MessageList list) {
        IEnumType enumType = getEnumType();
        if (isValueProviderAttributeMissing(enumType)) {
            String text = NLS.bind(Messages.EnumLiteralNameAttribute_DefaultValueProviderAttributeDoesNotExist,
                    defaultValueProviderAttribute);
            Message msg = new Message(
                    MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_DOES_NOT_EXIST, text,
                    Message.ERROR, this, PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE);
            list.add(msg);
        }
    }

    private boolean isValueProviderAttributeMissing(IEnumType enumType) {
        return !(enumType.containsEnumAttributeIncludeSupertypeCopies(defaultValueProviderAttribute));
    }

    private void validateValueProviderAttributeHasStringDatatype(MessageList list) throws CoreException {
        IEnumType enumType = getEnumType();
        if (isValueProviderAttributeMissing(enumType)) {
            return;
        }
        IEnumAttribute providerAttribute = enumType
                .getEnumAttributeIncludeSupertypeCopies(defaultValueProviderAttribute);
        Datatype datatype = providerAttribute.findDatatype(getIpsProject());
        if (datatype != null) {
            if (!(datatype.equals(Datatype.STRING))) {
                String text = NLS.bind(
                        Messages.EnumLiteralNameAttribute_DefaultValueProviderAttributeNotOfDatatypeString,
                        defaultValueProviderAttribute);
                Message msg = new Message(
                        MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_NOT_OF_DATATYPE_STRING,
                        text, Message.ERROR, this, PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE);
                list.add(msg);
            }
        }
    }

    /**
     * Not supported by <tt>IEnumLiteralNameAttribute</tt>s.
     * 
     * @throws UnsupportedOperationException If the operation is called.
     */
    @Override
    public void setIdentifier(boolean usedAsIdInFaktorIpsUi) {
        throw new UnsupportedOperationException("The identifier property is not used by EnumLiteralNameAttributes."); //$NON-NLS-1$
    }

    /**
     * Not supported by <tt>IEnumLiteralNameAttribute</tt>s.
     * 
     * @throws UnsupportedOperationException If the operation is called.
     */
    @Override
    public void setInherited(boolean isInherited) {
        throw new UnsupportedOperationException("The inherited property is not used by EnumLiteralNameAttributes."); //$NON-NLS-1$
    }

    /**
     * Not supported by <tt>IEnumLiteralNameAttribute</tt>s.
     * 
     * @throws UnsupportedOperationException If the operation is called.
     */
    @Override
    public void setUsedAsNameInFaktorIpsUi(boolean usedAsNameInFaktorIpsUi) {
        throw new UnsupportedOperationException("The usedAsName property is not used by EnumLiteralNameAttributes."); //$NON-NLS-1$
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IEnumLiteralNameAttribute.XML_TAG);
    }

}
