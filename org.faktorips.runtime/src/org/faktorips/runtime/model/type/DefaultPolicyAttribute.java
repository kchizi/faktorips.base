/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.type;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsDefaultValue;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.type.Type.AnnotatedElementMatcher;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;

public class DefaultPolicyAttribute extends PolicyAttribute {

    private final Method getter;

    private final Method setter;

    private Method defaultValueMethod;
    private Map<Type, Method> valueSetMethods = new HashMap<Type, Method>(2);

    public DefaultPolicyAttribute(PolicyCmptType policyCmptType, Method getter, Method setter,
            boolean changingOverTime) {
        super(policyCmptType, getter.getAnnotation(IpsAttribute.class),
                getter.getAnnotation(IpsExtensionProperties.class), getter.getReturnType(), changingOverTime);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public boolean isProductRelevant() {
        return getter.isAnnotationPresent(IpsConfiguredAttribute.class);
    }

    @Override
    public Object getValue(IModelObject modelObject) {
        return invokeMethod(getter, modelObject);
    }

    @Override
    public void setValue(IModelObject modelObject, Object value) {
        if (setter == null) {
            if (isOverriding()) {
                getSuperAttribute().setValue(modelObject, value);
            } else {
                throw new IllegalArgumentException(String.format("There is no setter for attribute %s in type %s.",
                        getName(), getType().getName()));
            }
        } else {
            invokeMethod(setter, modelObject, value);
        }
    }

    @Override
    public Object getDefaultValue(IConfigurableModelObject modelObject) {
        return getDefaultValue(modelObject.getProductComponent(), modelObject.getEffectiveFromAsCalendar());
    }

    @Override
    public Object getDefaultValue(IProductComponent source, Calendar effectiveDate) {
        if (!isProductRelevant()) {
            throw new IllegalStateException(
                    "Trying to find default value method in product class, but policy attribute " + getType().getName()
                            + '.' + getName() + " is not configurable.");
        }
        return invokeMethod(getDefaultValueMethod(getType().getProductCmptType()),
                getRelevantProductObject(source, effectiveDate));
    }

    private Method getDefaultValueMethod(Type type) {
        if (defaultValueMethod == null) {
            defaultValueMethod = findDefaultValueMethod(type);
        }
        return defaultValueMethod;
    }

    private Method findDefaultValueMethod(Type type) {
        AnnotatedElementMatcher<IpsDefaultValue> filter = new AnnotatedElementMatcher<IpsDefaultValue>() {
            @Override
            public boolean matches(IpsDefaultValue ann) {
                return ann.value().equals(getName());
            }
        };

        Method method = type.searchDeclaredMethod(IpsDefaultValue.class, filter);
        if (method == null) {
            throw new IllegalStateException(
                    "No method found for retrieving the default value of attribute: " + getName());
        }
        return method;
    }

    @Override
    public ValueSet<?> getValueSet(IModelObject modelObject, IValidationContext context) {
        Method valueSetMethod = getValueSetMethod(getType());
        return getValueSet(valueSetMethod, modelObject, context);
    }

    @Override
    public ValueSet<?> getValueSet(IProductComponent source, Calendar effectiveDate, IValidationContext context) {
        Method valueSetMethod = getValueSetMethod(getType().getProductCmptType());
        Object productObject = getRelevantProductObject(source, effectiveDate);
        return getValueSet(valueSetMethod, productObject, context);
    }

    private ValueSet<?> getValueSet(Method valueSetMethod, Object object, IValidationContext context) {
        if (valueSetMethod == null) {
            return new UnrestrictedValueSet<Object>(!getDatatype().isPrimitive());
        } else if (valueSetMethod.getParameterTypes().length == 0) {
            return (ValueSet<?>)invokeMethod(valueSetMethod, object);
        } else if (valueSetMethod.getParameterTypes().length == 1) {
            return (ValueSet<?>)invokeMethod(valueSetMethod, object, context);
        } else {
            throw new IllegalStateException("The method for retrieving the allowed values of attribute: " + getName()
                    + " has too many aruments: " + valueSetMethod);
        }
    }

    private Method getValueSetMethod(Type model) {
        if (valueSetMethods.containsKey(model)) {
            return valueSetMethods.get(model);
        } else {
            Method valueSetMethod = findValueSetMethod(model);
            valueSetMethods.put(model, valueSetMethod);
            return valueSetMethod;
        }
    }

    @Override
    public DefaultPolicyAttribute createOverwritingAttributeFor(Type subType) {
        return new DefaultPolicyAttribute((PolicyCmptType)subType, getter, setter, isChangingOverTime());
    }

    private Method findValueSetMethod(Type type) {
        AnnotatedElementMatcher<IpsAllowedValues> filter = new AnnotatedElementMatcher<IpsAllowedValues>() {
            @Override
            public boolean matches(IpsAllowedValues ann) {
                return ann.value().equals(getName());
            }
        };

        return type.searchDeclaredMethod(IpsAllowedValues.class, filter);
    }
}
