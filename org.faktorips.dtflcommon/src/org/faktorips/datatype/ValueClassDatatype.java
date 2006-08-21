/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.datatype;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.util.StringUtil;
import org.faktorips.values.NullObjectSupport;

/**
 * A datatype that represents a Java class representing a value, for example java.lang.String.
 * 
 * @author Jan Ortmann
 */
public abstract class ValueClassDatatype extends AbstractDatatype implements ValueDatatype {
	
	private Class clazz;
	private String name;
    private boolean isNullObject = false;

	public ValueClassDatatype(Class clazz) {
		this(clazz, StringUtil.unqualifiedName(clazz.getName()));	
	}
	
	public ValueClassDatatype(Class clazz, String name) {
		this.clazz = clazz;
		this.name = name;
        Class[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].equals(NullObjectSupport.class)) {
                isNullObject = true;
                break;
            }
        }
	}

	/**
	 * Overridden.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Overridden.
	 */
    public String getQualifiedName() {
        return name;
    }
    
    /** 
     * Overridden.
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * Overridden.
     */
    public boolean isValueDatatype() {
        return true;
    }
    
    /**
     * Overridden.
     */
    public Datatype getWrapperType() {
        return null;
    }

	public Class getJavaClass() {
		return clazz;
	}

	/**
	 * Overridden.
	 */
	public String getJavaClassName() {
		return clazz.getName();
	}

	/**
	 * Overridden.
	 */
	public String valueToString(Object value) {
		return value.toString();
	}
	
    /**
     * Overridden.
     */
    public boolean isNull(String valueString) {
        Object value = getValue(valueString);
        
        if (value==null) {
            if (isNullObject) {
//                TODO: What's this? throw new RuntimeException("Class " + clazz + " implements NullObject, so the value must not be null.");
            }
            return true;
        }
        
        if (!(value instanceof NullObjectSupport)) {
            return false;
        }
        return ((NullObjectSupport)value).isNull();
    }
    
    /**
     * Overridden.
     */
    public boolean isParsable(String value) {
        try {
            if (isNull(value)) {
                return true;
            }
            
            getValue(value);
            return true;
            
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNullObject() {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean areValuesEqual(String valueA, String valueB) {
        return ObjectUtils.equals(getValue(valueA), getValue(valueB));
    }

    /**
     * {@inheritDoc}
     */
    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        if (!supportsCompare()) {
            throw new UnsupportedOperationException("Datatype " + getQualifiedName() + " does not support comparison of values");
        }
        return ((Comparable)getValue(valueA)).compareTo(getValue(valueB));
    }


    public abstract Object getValue(String value);
}
