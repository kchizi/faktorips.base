/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsproject;


import org.eclipse.core.runtime.IPath;

/**
 * An IPS archive is an archive for IPS objects. It is physically stored in a file. The file's
 * format is jar.
 * 
 * @author Jan Ortmann
 */
public interface IIpsArchive extends IIpsStorage {

    /**
     * Constant for the top-level folder in the archive file that contains the entries for the ips
     * objects.
     */
    public static final String IPSOBJECTS_FOLDER = "ipsobjects"; //$NON-NLS-1$

    /**
     * Constant for the jar entry name" that contains additional ipsobjects properties like the
     * mapping to Java base packages.
     */
    public static final String JAVA_MAPPING_ENTRY_NAME = IPSOBJECTS_FOLDER + IPath.SEPARATOR + "ipsobjects.properties"; //$NON-NLS-1$

    public static final String QNT_PROPERTY_POSTFIX_SEPARATOR = "#"; //$NON-NLS-1$

    public static final String PROPERTY_POSTFIX_BASE_PACKAGE_MERGABLE = "basePackageMergable"; //$NON-NLS-1$

    public static final String PROPERTY_POSTFIX_BASE_PACKAGE_DERIVED = "basePackageDerived"; //$NON-NLS-1$

    /**
     * Returns the path to the underlying file. Note that the file might exists outside the
     * workspace or might not exists at all. Do not use this method to locate the archive because
     * this path may be project relative or workspace relative. Use {@link #getLocation()} instead!
     */
    public IPath getArchivePath();

}
