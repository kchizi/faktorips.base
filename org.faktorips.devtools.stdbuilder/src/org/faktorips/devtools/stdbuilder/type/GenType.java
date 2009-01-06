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

package org.faktorips.devtools.stdbuilder.type;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.JavaGeneratorForIpsPart;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * Base class for the genertors of policy and product component type.
 * 
 * @author Peter Erzberger
 */
public abstract class GenType extends JavaGeneratorForIpsPart {

    private StandardBuilderSet builderSet;
    private IType type;

    protected static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * @param policyCmptType
     * @param builder
     * @throws CoreException
     */
    public GenType(IType type, StandardBuilderSet builderSet, LocalizedStringsSet localizedStringsSet) {
        super(type, localizedStringsSet);
        ArgumentCheck.notNull(type, this);
        ArgumentCheck.notNull(builderSet, this);
        this.type = type;
        this.builderSet = builderSet;
    }

    public IType getType() {
        return type;
    }

    public StandardBuilderSet getBuilderSet() {
        return builderSet;
    }

    public String getPackageName(boolean forInterface) throws CoreException {
        return getPackageName(type, builderSet, forInterface);
    }

    private static String getPackageName(IType type, StandardBuilderSet builderSet, boolean forInterface)
            throws CoreException {
        if (type != null) {
            if (forInterface) {
                return builderSet.getPackageName(type.getIpsSrcFile());
            }
            return builderSet.getInternalPackageName(type.getIpsSrcFile());
        }
        return null;
    }

    public String getQualifiedName(boolean forInterface) throws CoreException {
        return getQualifiedName(type, builderSet, getPackageName(forInterface), forInterface);
    }

    public static String getQualifiedName(IType type, StandardBuilderSet builderSet, boolean forInterface)
            throws CoreException {
        if (type != null) {
            return getQualifiedName(type, builderSet, getPackageName(type, builderSet, forInterface), forInterface);
        }
        return null;
    }

    private static String getQualifiedName(IType type,
            StandardBuilderSet builderSet,
            String packageName,
            boolean forInterface) {
        StringBuffer buf = new StringBuffer();
        buf.append(packageName);
        buf.append('.');
        buf.append(getUnqualifiedClassName(type, builderSet, forInterface));
        return buf.toString();
    }

    public static String getUnqualifiedClassName(IType type, StandardBuilderSet builderSet, boolean forInterface) {
        if (forInterface) {
            return builderSet.getJavaNamingConvention().getPublishedInterfaceName(type.getName());

        }
        return StringUtil.getFilenameWithoutExtension(type.getName());
    }

    /**
     * Returns the unqualified name for Java class generated by this builder for the given ips
     * source file.
     * 
     * @param ipsSrcFile the ips source file
     * @return the qualified class name
     * @throws CoreException is delegated from calls to other methods
     */
    public String getUnqualifiedClassName(boolean forInterface) throws CoreException {
        return getUnqualifiedClassName(type, builderSet, forInterface);
    }

    /**
     * Returns the abbreviation for the generation (changes over time) concept.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention
     * @see org.faktorips.devtools.core.builder.AbstractTypeBuilder#getAbbreviationForGenerationConcept
     */
    public String getAbbreviationForGenerationConcept() {
        return getChangesInTimeNamingConvention().getGenerationConceptNameAbbreviation(
                getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Returns the naming convention for product changes over time.
     * 
     * @see org.faktorips.devtools.core.builder.JavaSourceFileBuilder#getChangesInTimeNamingConvention
     */
    public IChangesOverTimeNamingConvention getChangesInTimeNamingConvention() {
        return builderSet.getIpsProject().getChangesInTimeNamingConventionForGeneratedCode();
    }

    /**
     * Returns the language in that variables, methods are named and and Java docs are written in.
     * 
     * @see IIpsArtefactBuilderSet#getLanguageUsedInGeneratedSourceCode()
     */
    public Locale getLanguageUsedInGeneratedSourceCode() {
        return builderSet.getLanguageUsedInGeneratedSourceCode();
    }

    /**
     * Returns the name (singular form) for the generation (changes over time) concept.
     * 
     * @param element An ips element needed to access the ipsproject where the neccessary
     *            configuration information is stored.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention
     * @see org.faktorips.devtools.core.builder.AbstractPcTypeBuilder#getNameForGenerationConcept
     */
    public String getNameForGenerationConcept() {
        return getChangesInTimeNamingConvention().getGenerationConceptNameSingular(
                getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Returns the getter method to access a property/attribute value.
     * 
     * @since 2.0
     */
    public String getMethodNameGetPropertyValue(String propName, Datatype datatype) {
        return getJavaNamingConvention().getGetterMethodName(propName, datatype);
    }

}
