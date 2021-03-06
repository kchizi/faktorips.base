/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;

/**
 * Generates JAXB annotations for policy component type implementation classes
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS
 * 
 */
public class PolicyCmptImplClassJaxbAnnGen extends AbstractAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragmentBuilder codeBuilder = new JavaCodeFragmentBuilder();
        if (generatorModelNode instanceof XPolicyCmptClass) {
            XPolicyCmptClass xPolicyCmptClass = (XPolicyCmptClass)generatorModelNode;

            String unqualifiedName = xPolicyCmptClass.getImplClassName();
            codeBuilder.annotationLn("javax.xml.bind.annotation.XmlRootElement", "name", unqualifiedName);
        }
        return codeBuilder.getFragment();
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return true;
    }

}
