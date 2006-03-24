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

package org.faktorips.devtools.core.internal.model.product;

import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * An abstract implementation that uses a special character to separate the
 * constant part and the version id. 
 * <p>
 * When transforming a name to a Java identifier, special characters like 
 * blank and hypen (-) can be replaced with a String that is allowed
 * in a Java identifiers. The special characters can be registered along with
 * their replacement via the addSpecialCharReplacement() method.
 * <p>
 * Note that two special characters can't have the same replacement, as otherwise
 * two product component names (so unlikely) could result in the same Java class name.
 * <p>
 * The dot (.) is prohibited in names as it is use to separate the name from the
 * package information in qualified names.
 * <p>
 * By default the following replacement is used:<p>
 *  hypen (-) => two underscores
 *  blank => three underscores
 *
 * @author Jan Ortmann
 */
public abstract class AbstractProductCmptNamingStrategy implements
		IProductCmptNamingStrategy {

    /**
     * Validation message code to indicate that the name contains illegal characters.
     */
    public final static String MSGCODE_ILLEGAL_CHARACTERS = MSGCODE_PREFIX + "IllegalCharacters"; //$NON-NLS-1$

    private String id;
	private String separator;
	private HashMap specialCharReplacements = new HashMap();
	
	public AbstractProductCmptNamingStrategy(String id, String separator) {
		ArgumentCheck.notNull(id);
		this.id = id;
		this.separator = separator;
		putSpecialCharReplacement('-', "__");
		putSpecialCharReplacement(' ', "___");
	}
	
	protected void putSpecialCharReplacement(char specialChar, String replacement) {
		if (specialChar=='.') {
			throw new IllegalArgumentException("The dot (.) is is prohibited in names, as it is used to separate name and package information in qualified names."); //$NON-NLS-1$
		}
		if (replacement==null) {
			specialCharReplacements.remove(new Character(specialChar));
		} else {
			specialCharReplacements.put(new Character(specialChar), replacement);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Default implementation returns the id.
	 */
	public String getName(Locale locale) {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProductCmptName(String constantPart, String versionId) {
		return constantPart + separator + versionId;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConstantPart(String productCmptName) {
		int index = productCmptName.indexOf(separator);
		if (index==-1) {
			throw new IllegalArgumentException("Can't get constant part from " + productCmptName + ", separator not found!"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return productCmptName.substring(0, index);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersionId(String productCmptName) {
		int index = productCmptName.indexOf(separator);
		if (index==-1) {
			throw new IllegalArgumentException("Can't get constant part from " + productCmptName + ", separator not found!"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return productCmptName.substring(index+separator.length());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNextName(IProductCmpt productCmpt) {
		String part = getConstantPart(productCmpt.getName());
		return part + separator + getNextVersionId(productCmpt);
	}

	/**
	 * {@inheritDoc}
	 */
	public MessageList validate(String name) {
		MessageList list = new MessageList();
		if (separator.length() > 0) {
			int separatorCount = StringUtils.countMatches(name, separator); 
			if ( separatorCount == 0) {
				Message msg = Message.newError(MSGCODE_MISSING_VERSION_SEPARATOR, "The name " + name + " does not contain the version separator.");
				list.add(msg);
				return list;
			}
			if ( separatorCount > 1) {
				Message msg = Message.newError(MSGCODE_ONYL_1_OCCURENCE_OF_SEPARATOR_ALLOWED, "Only 1 occurence of " + separator + " is allowed to separate the version id, found multiple.");
				list.add(msg);
				return list;
			}
		}
		list.add(validateConstantPart(getConstantPart(name)));
		list.add(validateVersionId(getVersionId(name)));
		return list;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MessageList validateConstantPart(String constantPartName) {
		MessageList list = new MessageList();
		try {
			getJavaClassIdentifier(constantPartName);
		} catch (IllegalArgumentException e) {
			Message msg = Message.newError(MSGCODE_ILLEGAL_CHARACTERS, "The name contains at least 1 character that is not allowed.");
			list.add(msg);
		}
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getJavaClassIdentifier(String name) {
		StringBuffer buffer = new StringBuffer();
		for (int i=0; i<name.length(); i++) {
			char c = name.charAt(i);
			if (isSpecialChar(c)) {
				buffer.append(getReplacement(c));
			} else {
				buffer.append(c);
			}
		}
		String identifier = buffer.toString();
		IStatus status = JavaConventions.validateJavaTypeName(identifier); 
		if (status.isOK() || status.getSeverity()==IStatus.WARNING) {
			return identifier;
		}
		throw new IllegalArgumentException("Name " + name + " can't be transformed to a valid Java class name");
	}

	private boolean isSpecialChar(char c) {
		return specialCharReplacements.containsKey(new Character(c));
	}
	
	private String getReplacement(char c) {
		return (String)specialCharReplacements.get(new Character(c));
	}
}
