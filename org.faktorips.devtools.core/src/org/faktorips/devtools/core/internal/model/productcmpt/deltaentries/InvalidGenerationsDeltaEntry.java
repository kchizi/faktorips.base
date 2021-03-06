/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;

/**
 * This delta entry is created when the type does not support generations, that means
 * changing-over-time is set to false, but the corresponding product component has more than one
 * generation.
 * <p>
 * When the {@link #fix()} method is called, all generations except the oldest one will be removed.
 * 
 */
public class InvalidGenerationsDeltaEntry implements IDeltaEntry {

    private IProductCmpt productCmpt;

    public InvalidGenerationsDeltaEntry(IProductCmpt productCmpt) {
        this.productCmpt = productCmpt;
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.INVALID_GENERATIONS;
    }

    @Override
    public String getDescription() {
        return NLS.bind(Messages.InvalidGenerationsDeltaEntry_description, IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention().getGenerationConceptNamePlural(true));
    }

    @Override
    public void fix() {
        IIpsObjectGeneration[] generationsOrderedByValidDate = productCmpt.getGenerationsOrderedByValidDate();
        for (int i = 1; i < generationsOrderedByValidDate.length; i++) {
            generationsOrderedByValidDate[i].delete();
        }
    }

    @Override
    public Class<ProductCmptGeneration> getPartType() {
        return ProductCmptGeneration.class;
    }

}
