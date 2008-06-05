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

package org.faktorips.devtools.core.model.ipsproject;


/**
 * A configuration object for ips artefact builder sets. Provides string values for string keys.
 * An ips artefact builder set instance can be configured by means of the ips project properties. Therefor 
 * the IpsArtefactBuilderSet tag of an .ipsproject file can contain one IpsArtefactBuilderSetConfig tag.
 * Here is an example for a configuration declaration:
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilderSetConfig{

    /**
     * Returns the names of all properties provided by this configuration.
     */
    public String[] getPropertyNames();

    /**
     * Returns the value of the property of the provided property name.
     */
    public Object getPropertyValue(String propertyName);

    /**
     * Tries to get a value for the provided property name and expects it to be a Boolean if not a
     * RuntimeException is thrown.
     */
    public Boolean getPropertyValueAsBoolean(String propertyName);

    /**
     * Tries to get a value for the provided property name and expects it to be a String if not a
     * RuntimeException is thrown.
     */
    public String getPropertyValueAsString(String propertyName);

    /**
     * Tries to get a value for the provided property name and expects it to be an Integer if not a
     * RuntimeException is thrown.
     */
    public Integer getPropertyValueAsInteger(String propertyName);

}
