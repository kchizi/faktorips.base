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

package org.faktorips.devtools.formulalibrary.builder;

import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.formulalibrary.builder.xpand.FormulaLibraryClassBuilder;
import org.faktorips.devtools.stdbuilder.IIpsArtefactBuilderFactory;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * Factoryclass to create the {@link FormulaLibraryClassBuilder}
 * 
 * @author frank
 */
public class FormulaLibraryBuilderFactory implements IIpsArtefactBuilderFactory {

    @Override
    public IIpsArtefactBuilder createBuilder(StandardBuilderSet builderSet) {
        return new FormulaLibraryClassBuilder(builderSet, builderSet.getGeneratorModelContext(),
                builderSet.getModelService());
    }

}