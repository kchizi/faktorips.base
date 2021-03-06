/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.LocalizedStringsSet;

/**
 * This subclass of <code>JavaGeneratorForIpsPart</code> works together with
 * <code>DefaultJavaSourceFileBuilder</code>.
 * 
 * @see DefaultJavaSourceFileBuilder
 * @author Jan Ortmann
 */
public abstract class DefaultJavaGeneratorForIpsPart extends JavaGeneratorForIpsPart {

    protected static final String[] EMPTY_STRING_ARRAY = new String[0];

    public DefaultJavaGeneratorForIpsPart(IIpsObjectPartContainer part, LocalizedStringsSet localizedStringsSet) {
        super(part, localizedStringsSet);
    }

    /**
     * Generates the source code for the IPS object part this is a generator for.
     */
    public void generate(boolean generatesInterface, IIpsProject ipsProject, TypeSection mainSection)
            throws CoreException {
        generateConstants(mainSection.getConstantBuilder(), ipsProject, generatesInterface);
        if (!generatesInterface) {
            generateMemberVariables(mainSection.getMemberVarBuilder(), ipsProject, generatesInterface);
        }
        generateMethods(mainSection.getMethodBuilder(), ipsProject, generatesInterface);
    }

    /**
     * Subclasses have to implement generation of the methods here.
     * 
     * @param builder The builder for the type's method section.
     * 
     * @throws CoreException if an error occurs while generating the member variables.
     */
    protected abstract void generateMethods(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException;

    /**
     * Subclasses have to implement generation of the member variables here. This method is only
     * called if the generator is generating an implementation class.
     * 
     * @param builder The builder for the type's member variables section.
     * 
     * @throws CoreException if an error occurs while generating the member variables
     */
    protected abstract void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException;

    /**
     * Subclasses have to implement generation of the constants (final statics) here.
     * 
     * @param builder The builder for the type's constants section.
     * 
     * @throws CoreException if an error occurs while generating the member variables
     */
    protected abstract void generateConstants(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException;

}
