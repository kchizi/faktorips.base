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

package org.faktorips.devtools.core.internal.model.enums;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEnumAttribute</code>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumAttribute
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttribute extends AtomicIpsObjectPart implements IEnumAttribute {

    /** The icon representing an enum attribute. */
    private final String ICON = "EnumAttribute.gif";

    /** The icon representing an overridden enum attribute. */
    private final String OVERRIDDEN_ICON = "EnumAttributeOverridden.gif";

    /** The icon representing an identifier enum attribute. */
    private final String IDENTIFIER_ICON = "EnumAttributeIdentifier.gif";

    /** The icon representing an overridden identifier enum attribute. */
    private final String OVERRIDDEN_IDENTIFIER_ICON = "EnumAttributeOverriddenIdentifier.gif";

    /** The datatype of this enum attribute. */
    private String datatype;

    /** Flag indicating whether this enum attribute is an identifier. */
    private boolean identifier;

    /** Flag indicating whether this enum attribute is inherited from the supertype hierarchy. */
    private boolean inherited;

    /** Flag indicating whether this enum attribute is a unique identifier. */
    private boolean uniqueIdentifier;

    /**
     * Creates a new <code>EnumAttribute</code>.
     * 
     * @param parent The enum type this enum attribute belongs to.
     * @param id A unique id for this enum attribute.
     */
    public EnumAttribute(IEnumType parent, int id) {
        super(parent, id);

        this.datatype = "";
        this.identifier = false;
        this.inherited = false;
        this.uniqueIdentifier = false;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name) {
        ArgumentCheck.notNull(name);

        String oldName = this.name;
        this.name = name;
        valueChanged(oldName, name);
    }

    /**
     * {@inheritDoc}
     */
    public void setDatatype(String datatype) {
        ArgumentCheck.notNull(datatype);

        String oldDatatype = this.datatype;
        this.datatype = datatype;
        valueChanged(oldDatatype, datatype);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLiteralNameAttribute() {
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    public void setLiteralNameAttribute(boolean isIdentifier) {
        boolean oldIsIdentifier = this.identifier;
        this.identifier = isIdentifier;
        valueChanged(oldIsIdentifier, isIdentifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initFromXml(Element element, Integer id) {
        name = element.getAttribute(PROPERTY_NAME);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        identifier = Boolean.parseBoolean(element.getAttribute(PROPERTY_LITERAL_NAME_ATTRIBUTE));
        inherited = Boolean.parseBoolean(element.getAttribute(PROPERTY_INHERITED));

        super.initFromXml(element, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_DATATYPE, datatype);
        element.setAttribute(PROPERTY_LITERAL_NAME_ATTRIBUTE, String.valueOf(identifier));
        element.setAttribute(PROPERTY_INHERITED, String.valueOf(inherited));
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        if (identifier && inherited) {
            return IpsPlugin.getDefault().getImage(OVERRIDDEN_IDENTIFIER_ICON);
        } else if (identifier) {
            return IpsPlugin.getDefault().getImage(IDENTIFIER_ICON);
        } else if (inherited) {
            return IpsPlugin.getDefault().getImage(OVERRIDDEN_ICON);
        } else {
            return IpsPlugin.getDefault().getImage(ICON);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        validateName(list, ipsProject);
        validateDatatype(list, ipsProject);
        validateIdentifier(list, ipsProject);
        validateInherited(list, ipsProject);
    }

    /** Validates the <code>name</code> property. */
    private void validateName(MessageList list, IIpsProject ipsProject) {
        String text;
        Message validationMessage;
        List<IEnumAttribute> enumAttributesThisType = getEnumType().getEnumAttributes();

        // Check for name missing
        if (name.equals("")) {
            text = Messages.EnumAttribute_NameMissing;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_NAME_MISSING, text, Message.ERROR, this,
                    PROPERTY_NAME);
            list.add(validationMessage);
        }

        // Check for other attributes with the same name
        int numberEnumAttributesThisName = 0;
        for (IEnumAttribute currentEnumAttribute : enumAttributesThisType) {
            if (currentEnumAttribute.getName().equals(name)) {
                numberEnumAttributesThisName++;
            }
            if (numberEnumAttributesThisName > 1) {
                text = NLS.bind(Messages.EnumAttribute_DuplicateName, name);
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME, text, Message.ERROR, this,
                        PROPERTY_NAME);
                list.add(validationMessage);
                break;
            }
        }
    }

    /** Validates the <code>datatype</code> property. */
    private void validateDatatype(MessageList list, IIpsProject ipsProject) throws CoreException {
        String text;
        Message validationMessage;

        // Check for datatype missing
        if (datatype.equals("")) {
            text = Messages.EnumAttribute_DatatypeMissing;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_MISSING, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
            return;
        }

        // Check for datatype not existing
        Datatype ipsDatatype = getIpsProject().findDatatype(datatype);
        if (ipsDatatype == null) {
            text = NLS.bind(Messages.EnumAttribute_DatatypeDoesNotExist, datatype);
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
            return;
        }

        // Check for identifier datatype = String
        if (identifier) {
            if (!(ipsDatatype.getName().equals("String"))) {
                text = Messages.EnumAttribute_IdentifierNotOfDatatypeString;
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_IDENTIFIER_NOT_OF_DATATYPE_STRING, text,
                        Message.ERROR, this, PROPERTY_DATATYPE);
                list.add(validationMessage);
            }
        }
    }

    /** Validates the <code>identifier</code> property. */
    private void validateIdentifier(MessageList list, IIpsProject ipsProject) {
        String text;
        Message validationMessage;
        List<IEnumAttribute> enumAttributesThisType = getEnumType().getEnumAttributes();

        if (identifier) {
            // Check for other attributes being marked as identifier
            int numberEnumAttributesIdentifier = 0;
            for (IEnumAttribute currentEnumAttribute : enumAttributesThisType) {
                if (currentEnumAttribute.isLiteralNameAttribute()) {
                    numberEnumAttributesIdentifier++;
                }
                if (numberEnumAttributesIdentifier > 1) {
                    text = Messages.EnumAttribute_DuplicateIdentifier;
                    validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_IDENTIFIER, text, Message.ERROR,
                            this, PROPERTY_LITERAL_NAME_ATTRIBUTE);
                    list.add(validationMessage);
                    break;
                }
            }

            // A literal name must also be a unique identifier
            if (!uniqueIdentifier) {
                text = Messages.EnumAttribute_LiteralNameButNotUniqueIdentifier;
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_BUT_NOT_UNIQUE_IDENTIFIER, text,
                        Message.ERROR, this, PROPERTY_LITERAL_NAME_ATTRIBUTE);
                list.add(validationMessage);
            }
        }
    }

    /** Validates the <code>inherited</code> property. */
    private void validateInherited(MessageList list, IIpsProject ipsProject) throws CoreException {
        String text;
        Message validationMessage;

        // Check existence in supertype hierarchy if this enum attribute is inherited
        if (inherited) {
            boolean attributeFound = false;
            List<IEnumType> superEnumTypes = getEnumType().findAllSuperEnumTypes();
            for (IEnumType currentSuperEnumType : superEnumTypes) {

                // Name, datatype and identifier must correspond
                IEnumAttribute possibleAttribute = currentSuperEnumType.getEnumAttribute(name);
                if (possibleAttribute != null) {
                    if (possibleAttribute.getDatatype().equals(datatype)
                            && possibleAttribute.isLiteralNameAttribute() == identifier) {
                        attributeFound = true;
                        break;
                    }
                }
            }

            if (!(attributeFound)) {
                String identifierLabel = (identifier) ? ", " + Messages.EnumAttribute_Identifier : "";
                String attribute = name + " (" + datatype + identifierLabel + ')';
                text = NLS.bind(Messages.EnumAttribute_NoSuchAttributeInSupertypeHierarchy, attribute);
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_NO_SUCH_ATTRIBUTE_IN_SUPERTYPE_HIERARCHY, text,
                        Message.ERROR, this, PROPERTY_INHERITED);
                list.add(validationMessage);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInherited() {
        return inherited;
    }

    /**
     * {@inheritDoc}
     */
    public void setInherited(boolean isInherited) {
        boolean oldIsInherited = this.inherited;
        this.inherited = isInherited;
        valueChanged(oldIsInherited, isInherited);
    }

    /**
     * {@inheritDoc}
     */
    public IEnumType getEnumType() {
        return (IEnumType)getParent();
    }

    /**
     * {@inheritDoc}
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype findDatatype(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        if (inherited) {
            return getEnumType().findEnumAttribute(name).findDatatype(ipsProject);
        }

        return ipsProject.findValueDatatype(datatype);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUniqueIdentifier() {
        return uniqueIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    public void setUniqueIdentifier(boolean isUniqueIdentifier) {
        boolean oldIsUniqueIdentifier = this.uniqueIdentifier;
        this.uniqueIdentifier = isUniqueIdentifier;
        valueChanged(oldIsUniqueIdentifier, isUniqueIdentifier);
    }

}
