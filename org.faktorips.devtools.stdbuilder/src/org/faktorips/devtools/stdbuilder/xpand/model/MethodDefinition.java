/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.util.Arrays;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

/**
 * Class to store the method definition as it is generated. A method consists of the method's name
 * and optional an array of parameters.
 * <p>
 * This class could store a method definition and is able to give the methods definition string for
 * example <em>getFooFor(String bar)</em> as well as the list of parameter type signatures as it is
 * needed by a JDT model.
 * 
 * @see #getDefinition()
 * @see #getTypeSignatures()
 * 
 * @author dirmeier
 */
public class MethodDefinition implements IGeneratedJavaElement {

    private final String name;

    private final MethodParameter[] parameters;

    /**
     * Create the method definition with the methods name and any optional parameters
     * 
     * @param name The methods name
     * @param parameters The parameters if you need any
     */
    public MethodDefinition(String name, MethodParameter... parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IJavaElement getJavaElement(IType javaType) {
        return javaType.getMethod(getName(), getTypeSignatures());
    }

    /**
     * Returns the name of the method.
     * 
     * @return The method's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the list of parameters, may be empty but never null.
     * 
     * @return The list of the method's parameters
     */
    public MethodParameter[] getParameters() {
        return parameters;
    }

    /**
     * Returns an array of type signatures for the parameters. The type signature is created by
     * using {@link Signature}.
     * 
     * @see MethodParameter#getTypeSignature()
     * @see Signature#createTypeSignature(String, boolean)
     * 
     * @return The array of type signatures, one entry for every parameter
     */
    public String[] getTypeSignatures() {
        String[] result = new String[parameters.length];
        int i = 0;
        for (MethodParameter parameter : parameters) {
            result[i] = parameter.getTypeSignature();
            i++;
        }
        return result;
    }

    /**
     * Returns the method definition as it is used in generated java code. This method definition
     * does not include the return type as far it is not part of the signature.
     * 
     * @return The methods definition, for example <em>getFooFor(String bar)</em>
     */
    public String getDefinition() {
        StringBuilder result = new StringBuilder(name);
        result.append("(");
        for (MethodParameter parameter : parameters) {
            result.append(parameter.getDefinition());
            if (parameter != parameters[parameters.length - 1]) {
                result.append(", ");
            }
        }
        result.append(")");
        return result.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + Arrays.hashCode(parameters);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MethodDefinition other = (MethodDefinition)obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (!Arrays.equals(parameters, other.parameters)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MethodDefinition " + getDefinition();
    }

}
