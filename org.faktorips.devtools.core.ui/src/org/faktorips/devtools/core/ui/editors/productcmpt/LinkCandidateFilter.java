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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * 
 * This LinkCandidateFilter filters, whether an {@link IIpsSrcFile} can be linked to the
 * {@link IProductCmptGeneration} represented by an {@link IProductCmptStructureReference}.
 * <p>
 * It is recommended, that the {@link #filter(IIpsSrcFile)} is called immediately after
 * instantiation.
 * 
 * @author dicker
 */
public class LinkCandidateFilter {

    private boolean canAnyLinkBeAdded;
    private List<IProductCmptTypeAssociation> associations;

    private IProductCmptGeneration generation;

    /**
     * Constructs a new filter for the given {@link IProductCmptStructureReference}.
     * <p>
     * This filter always returns false, if the given <code>workingModeBrowse</code> is set to
     * <code>true</code>. workingModeBrowse could be read by calling {@link IpsPlugin}
     * .getDefault().getIpsPreferences() .isWorkingModeBrowse()}.
     */
    public LinkCandidateFilter(IProductCmptStructureReference structureReference, boolean workingModeBrowse) {
        Assert.isNotNull(structureReference);
        IProductCmpt productCmpt = getProductCmpt(structureReference);

        if (productCmpt == null) {
            initializeNotEditable();
        } else {
            GregorianCalendar validAt = structureReference.getStructure().getValidAt();
            generation = productCmpt.getGenerationEffectiveOn(validAt);

            initialize(workingModeBrowse, structureReference);
        }
    }

    private void initialize(boolean workingModeBrowse, IProductCmptStructureReference structureReference) {
        if (generation == null) {
            initializeNotEditable();
        } else {
            initAssociations(structureReference);
            initCanAnyLinkBeAdded(workingModeBrowse);
        }
    }

    private void initializeNotEditable() {
        associations = Collections.emptyList();
        generation = null;
        canAnyLinkBeAdded = false;
    }

    private void initCanAnyLinkBeAdded(boolean workingModeBrowse) {
        canAnyLinkBeAdded = !(associations.isEmpty() || workingModeBrowse || generation.getIpsSrcFile().isReadOnly());
    }

    private List<IProductCmptTypeAssociation> getUncheckedAssociations(IProductCmptStructureReference structureReference) {
        if (structureReference instanceof IProductCmptReference) {
            IProductCmptReference productCmptReference = (IProductCmptReference)structureReference;
            return getUncheckedAssociations(productCmptReference);
        } else if (structureReference instanceof IProductCmptTypeAssociationReference) {
            IProductCmptTypeAssociationReference associationReference = (IProductCmptTypeAssociationReference)structureReference;
            return Arrays.asList(associationReference.getAssociation());
        }
        return Collections.emptyList();
    }

    private List<IProductCmptTypeAssociation> getUncheckedAssociations(IProductCmptReference productCmptReference) {
        List<IProductCmptTypeAssociation> uncheckedAssociations;
        IProductCmptTypeAssociationReference[] childProductCmptTypeAssociationReferences = productCmptReference
                .getStructure().getChildProductCmptTypeAssociationReferences(productCmptReference);
        uncheckedAssociations = new ArrayList<IProductCmptTypeAssociation>();
        for (IProductCmptTypeAssociationReference associationReference : childProductCmptTypeAssociationReferences) {
            uncheckedAssociations.add(associationReference.getAssociation());
        }
        return uncheckedAssociations;
    }

    private IProductCmpt getProductCmpt(IProductCmptStructureReference structureReference) {
        if (structureReference instanceof IProductCmptReference) {
            IProductCmptReference productCmptReference = (IProductCmptReference)structureReference;
            return productCmptReference.getProductCmpt();
        } else if (structureReference instanceof IProductCmptTypeAssociationReference) {
            IProductCmptTypeAssociationReference associationReference = (IProductCmptTypeAssociationReference)structureReference;
            return getProductCmpt(associationReference.getParent());
        }
        return null;
    }

    private void initAssociations(IProductCmptStructureReference structureReference) {
        List<IProductCmptTypeAssociation> checkedAssociations = new ArrayList<IProductCmptTypeAssociation>();

        List<IProductCmptTypeAssociation> uncheckedAssociations = getUncheckedAssociations(structureReference);

        for (IProductCmptTypeAssociation association : uncheckedAssociations) {
            String name = association.getName();
            IProductCmptLink[] links = generation.getLinks(name);
            if (links.length < association.getMaxCardinality()) {
                checkedAssociations.add(association);
            }
        }

        associations = checkedAssociations;
    }

    public boolean filter(IIpsSrcFile srcFile) {
        if (!canAnyLinkBeAdded) {
            return false;
        }
        if (!srcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
            return false;
        }
        if (isOutsideReferencedProjects(srcFile)) {
            return false;
        }
        if (isWrongType(srcFile)) {
            return false;
        }
        if (isAlreadyLinked(srcFile)) {
            return false;
        }
        return true;
    }

    private boolean isAlreadyLinked(IIpsSrcFile srcFile) {
        String qualifiedName = srcFile.getQualifiedNameType().getName();
        List<IProductCmptLink> linksAsList = generation.getLinksAsList();
        for (IProductCmptLink link : linksAsList) {
            if (link.getTarget().equals(qualifiedName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWrongType(IIpsSrcFile srcFile) {
        try {
            String typeName = srcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);

            IProductCmptType productCmptType = getIpsProject().findProductCmptType(typeName);

            for (IProductCmptTypeAssociation association : associations) {

                IProductCmptType targetType = association.findTargetProductCmptType(getIpsProject());
                boolean subtypeOrSameType = productCmptType.isSubtypeOrSameType(targetType, getIpsProject());

                if (subtypeOrSameType) {
                    return false;
                }
            }

        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        return true;
    }

    private IIpsProject getIpsProject() {
        return generation.getIpsProject();
    }

    private boolean isOutsideReferencedProjects(IIpsSrcFile srcFile) {
        IIpsProject ipsProject = getIpsProject();
        IIpsProject ipsProject2 = srcFile.getIpsProject();

        if (ipsProject.equals(ipsProject2)) {
            return false;
        }
        try {
            return !ipsProject.isReferencing(ipsProject2);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }
}