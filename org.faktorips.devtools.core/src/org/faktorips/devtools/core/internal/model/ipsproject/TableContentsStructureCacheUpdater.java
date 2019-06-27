/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Set;

import org.eclipse.core.resources.IResourceDelta;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.core.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * This class is responsible for updating the table structure map. It is an implementation of
 * {@link IIpsSrcFilesChangeListener} and is registered in the {@link IIpsModel} to get notified for
 * every changed {@link IIpsSrcFile}. It could also be used for the initialization of the table
 * structure map.
 * 
 */
class TableContentsStructureCacheUpdater implements IIpsSrcFilesChangeListener {

    private final TableContentsStructureCache cache;
    private final IIpsProject ipsProject;

    public TableContentsStructureCacheUpdater(TableContentsStructureCache cache, IIpsProject ipsProject) {
        this.cache = cache;
        this.ipsProject = ipsProject;
    }

    @Override
    public void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event) {
        Set<IIpsSrcFile> changedIpsSrcFiles = event.getChangedIpsSrcFiles();
        for (IIpsSrcFile ipsSrcFile : changedIpsSrcFiles) {
            OperationKind operationKind = OperationKind.getKind(event.getResourceDelta(ipsSrcFile));
            if (isRelevant(ipsSrcFile)) {
                handleTableStructureChange(ipsSrcFile, operationKind, cache);
                handleTableContentChange(ipsSrcFile, operationKind, cache);
            }
        }
    }

    private boolean isRelevant(IIpsSrcFile ipsSrcFile) {
        IIpsProject ipsSrcFileProject = ipsSrcFile.getIpsProject();
        return ipsProject.equals(ipsSrcFileProject) || ipsProject.isReferencing(ipsSrcFileProject);
    }

    private void handleTableStructureChange(IIpsSrcFile ipsSrcFile,
            TableContentsStructureCacheUpdater.OperationKind operation,
            TableContentsStructureCache tableContentsStructureCache) {
        if (IpsObjectType.TABLE_STRUCTURE.equals(ipsSrcFile.getIpsObjectType())) {
            operation.performTableStructureUpdate(tableContentsStructureCache, ipsSrcFile);
        }
    }

    private void handleTableContentChange(IIpsSrcFile ipsSrcFile,
            TableContentsStructureCacheUpdater.OperationKind operationKind,
            TableContentsStructureCache tableContentsStructureCache) {
        if (IpsObjectType.TABLE_CONTENTS.equals(ipsSrcFile.getIpsObjectType())) {
            operationKind.performTableContentUpdate(tableContentsStructureCache, ipsSrcFile);
        }
    }

    /**
     * The kind of update operation. We only distinguish added and removed. While kind
     * {@link #REMOVED} removes an existing mapping, all other operations are treated as
     * {@link #CHANGED}.
     */
    public enum OperationKind {
        ADDED {
            @Override
            public void performTableStructureUpdate(TableContentsStructureCache tableContentsStructureCache,
                    IIpsSrcFile tableStructure) {
                tableContentsStructureCache.newTableStructure(tableStructure);
            }

            @Override
            public void performTableContentUpdate(TableContentsStructureCache tableContentsStructureCache,
                    IIpsSrcFile tableContent) {
                tableContentsStructureCache.putTableContent(tableContent);
            }
        },
        CHANGED {
            @Override
            public void performTableStructureUpdate(TableContentsStructureCache tableContentsStructureCache,
                    IIpsSrcFile tableStructure) {
                // do nothing: table structure changes are not relevant. Rename is notified as
                // REMOVED and ADDED events
            }

            @Override
            public void performTableContentUpdate(TableContentsStructureCache tableContentsStructureCache,
                    IIpsSrcFile tableContent) {
                tableContentsStructureCache.tableContentChanged(tableContent);
            }
        },
        REMOVED {
            @Override
            public void performTableStructureUpdate(TableContentsStructureCache tableContentsStructureCache,
                    IIpsSrcFile tableStructure) {
                tableContentsStructureCache.removeTableStructure(tableStructure);
            }

            @Override
            public void performTableContentUpdate(TableContentsStructureCache tableContentsStructureCache,
                    IIpsSrcFile tableContent) {
                tableContentsStructureCache.removeTableContent(tableContent);
            }
        };

        public static OperationKind getKind(IResourceDelta resourceDelta) {
            if ((resourceDelta.getKind() & IResourceDelta.ADDED) != 0) {
                return ADDED;
            } else if ((resourceDelta.getKind() & IResourceDelta.REMOVED) != 0) {
                return REMOVED;
            } else {
                return CHANGED;
            }
        }

        public abstract void performTableStructureUpdate(TableContentsStructureCache tableContentsStructureCache,
                IIpsSrcFile tableStructure);

        public abstract void performTableContentUpdate(TableContentsStructureCache tableContentsStructureCache,
                IIpsSrcFile tableContent);

    }

}