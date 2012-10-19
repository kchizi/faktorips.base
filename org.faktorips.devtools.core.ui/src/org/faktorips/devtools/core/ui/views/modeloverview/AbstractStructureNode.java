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

package org.faktorips.devtools.core.ui.views.modeloverview;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.util.ArgumentCheck;

abstract class AbstractStructureNode implements IModelOverviewNode {

    private final ComponentNode parent;

    private List<ComponentNode> children = new ArrayList<ComponentNode>();

    /**
     * Creates a node with a mandatory parent {@link ComponentNode} and a {@link List} of mandatory
     * {@link #children}.
     * 
     * @param parent the parent, this parameter must not be {@code null}.
     * @param children a {@link List} of {@link ComponentNode component nodes}, this {@link List}
     *            must not be {@code null} or empty.
     * @throws NullPointerException if one of the parameters is {@code null} or the provided list of
     *             children is empty.
     */
    public AbstractStructureNode(ComponentNode parent, List<? extends ComponentNode> children) {
        ArgumentCheck.notNull(parent, "'parent' must not be null."); //$NON-NLS-1$
        ArgumentCheck.notNull(children, "'children' must not be null."); //$NON-NLS-1$
        ArgumentCheck.isTrue(!children.isEmpty(), "'children', must not be empty."); //$NON-NLS-1$

        this.parent = parent;
        addChildren(children);
    }

    /**
     * Returns all children of this node or an empty {@link List} if there are no children.
     */
    public List<ComponentNode> getChildren() {
        return children;
    }

    /**
     * Returns the parent node of this node, which should never be {@code null}.
     */
    @Override
    public ComponentNode getParent() {
        return parent;
    }

    private void addChildren(List<? extends ComponentNode> children) {

        this.children.addAll(children);

        for (ComponentNode componentNode : children) {
            componentNode.setParent(this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
        AbstractStructureNode other = (AbstractStructureNode)obj;
        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }
        return true;
    }
}
