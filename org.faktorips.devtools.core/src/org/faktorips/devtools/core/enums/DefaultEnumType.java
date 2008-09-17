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

package org.faktorips.devtools.core.enums;

/**
 * Default implementation of enum type.
 */
public class DefaultEnumType implements EnumType {
    
    private String name;
    private Class valueClass; // Java class representing the values.
    private DefaultEnumValue[] values = new DefaultEnumValue[0];

    /**
     * Creates a new enum type.
     * 
     * @param name The type's name.
     * @param valueClass Java class the values are instances of.
     * 
     * @throws IllegalArgumentException if name is null or if the valueClass is
     * not a subclass of DefaultEnumValue. 
     */
    public DefaultEnumType(String name, Class valueClass) {
        if (name==null) {
            throw new NullPointerException();
        }
        if (!DefaultEnumValue.class.isAssignableFrom(valueClass)) {
            throw new IllegalArgumentException(valueClass + " is not a subclass of " + DefaultEnumValue.class);
        }
        this.name = name;
        this.valueClass = valueClass;
    }
    
    public Class getValueClass() {
        return valueClass;
    }
    
    /**
     * Adds the value to the type.
     * 
     * @throws IllegalArgumentException if the type contains already an id
     * with the given id.
     */
    void addValue(DefaultEnumValue newValue) {
        if (containsValue(newValue.getId())) {
            throw new IllegalArgumentException("The enum type " + this + " contains already a value " + newValue);
        }
        DefaultEnumValue[] newValues = new DefaultEnumValue[values.length+1];
        System.arraycopy(values, 0, newValues, 0, values.length);
        newValues[values.length] = newValue;
        values = newValues;
    }
    
    /**
     * Overridden.
     */
    public EnumValue[] getValues() {
        DefaultEnumValue[] copy = new DefaultEnumValue[values.length];
        System.arraycopy(values, 0, copy, 0, values.length);
        return copy;
    }
    
    /**
     * Overridden.
     */
    public String[] getValueIds() {
        String[] ids = new String[values.length];
        for (int i=0; i<values.length; i++) {
            ids[i] = values[i].getId();        
        }
        return ids;
    }

    /** 
     * Overridden.
     */
    public boolean containsValue(String id) {
        for (int i=0; i<values.length; i++) {
            if (values[i].getId()==null) {
                if (id==null)
                    return true;
            } else {
                if (values[i].getId().equals(id))
                    return true;
            }
        }
        return false;
    }

    /** 
     * Overridden.
     */
    public EnumValue getEnumValue(String id) throws IllegalArgumentException {
        for (int i=0; i<values.length; i++) {
            if (values[i].getId()==null) {
                if (id==null) {
                    return values[i];
                } else {}
            } else {
                if (values[i].getId().equals(id)) {
                    return values[i];
                }
            }
        }
        return null;
    }
    
    /** 
     * Overridden.
     */
    public boolean contains(Object value) {
        if (!(value instanceof EnumValue)) {
            return false;
        }
        return contains((EnumValue)value);
    }

    /** 
     * Overridden.
     */
    public boolean containsNull() {
        return contains((String)null);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.valueset.ValueSet#getNumOfValues()
     */
    public int getNumOfValues() {
       return values.length;
    }

    /** 
     * Overridden.
     */
    public EnumValue getEnumValue(int index) throws IndexOutOfBoundsException {
        return values[index];
    }
    
    public String toString() {
        return name;
    }

    /**
     * Overridden.
     */
    public Object[] getValues(String[] value) {
        EnumValue [] elements = new EnumValue[value.length];
        for (int i = 0; i < elements.length; i++) {
            elements[i]=getEnumValue(value[i]);
        }
        return elements;
    }

}
