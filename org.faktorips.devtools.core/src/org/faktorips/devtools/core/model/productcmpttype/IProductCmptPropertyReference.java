/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.productcmpttype;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

/**
 * An object part used by {@link IProductCmptCategory}s to store references to
 * {@link IProductCmptProperty}s.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public interface IProductCmptPropertyReference extends IIpsObjectPart {

    public final static String XML_TAG_NAME = "ProductCmptPropertyReference"; //$NON-NLS-1$

    public final static String PROPERTY_PROPERTY_TYPE = "propertyType"; //$NON-NLS-1$

    /**
     * Sets the name of the referenced property.
     * 
     * @param name The name identifying the referenced property
     */
    public void setName(String name);

    /**
     * Sets the {@link ProductCmptPropertyType} of the referenced property.
     * 
     * @param propertyType The {@link ProductCmptPropertyType} of the referenced property
     */
    public void setProductCmptPropertyType(ProductCmptPropertyType propertyType);

    /**
     * Returns the {@link ProductCmptPropertyType} of the referenced property.
     */
    public ProductCmptPropertyType getProductCmptPropertyType();

    /**
     * Returns whether the given {@link IProductCmptProperty} is identified by this reference.
     * 
     * @param property The property to check whether this is a corresponding reference
     */
    public boolean isIdentifyingProperty(IProductCmptProperty property);

}
