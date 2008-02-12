/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere. Alle Rechte vorbehalten. Dieses Programm und
 * alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, etc.) dürfen nur unter
 * den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung
 * Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann. Mitwirkende: Faktor Zehn GmbH -
 * initial API and implementation
 **************************************************************************************************/

package org.faktorips.devtools.core.model;

import java.io.Serializable;

import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of the {@link IDependency} interface that describes a dependency between two IpsObjects.
 *  
 * @author Peter Erzberger
 */
public class IpsObjectDependency implements IDependency, Serializable{

    private static final long serialVersionUID = -4763466997240470890L;

    private QualifiedNameType source;
    private QualifiedNameType target;
    private int hashCode;
    private DependencyType dependencyType;

    private IpsObjectDependency(QualifiedNameType source, QualifiedNameType target, DependencyType dependencyType) {
        super();
        ArgumentCheck.notNull(source, this);
        ArgumentCheck.notNull(target, this);
        ArgumentCheck.notNull(dependencyType, this);
        this.source = source;
        this.target = target;
        this.dependencyType = dependencyType;
        calculateHashCode();
    }

    /**
     * Creates a new Dependency between the specified source and target objects and defines if it is
     * a transitive dependency.
     */
    public final static IpsObjectDependency create(QualifiedNameType source,
            QualifiedNameType target,
            DependencyType dependencyType) {
        return new IpsObjectDependency(source, target, dependencyType);
    }

    /**
     * Creates a new Dependency instance indicating an instance of dependency between the specified
     * source and target objects. A Dependency instance indicates that the source is subtype of the
     * target and hence the source depends on the target.
     */
    public final static IpsObjectDependency createSubtypeDependency(QualifiedNameType source, QualifiedNameType target) {
        return new IpsObjectDependency(source, target, DependencyType.SUBTYPE);
    }

    /**
     * Creates a new Dependency instance indicating referencing dependency between the specified
     * source and target objects. A Dependency instance indicates that the source references the
     * target and hence the source depends on the target.
     */
    public final static IpsObjectDependency createReferenceDependency(QualifiedNameType source, QualifiedNameType target) {
        return new IpsObjectDependency(source, target, DependencyType.REFERENCE);
    }

    /**
     * Creates a new Dependency instance indicating special referencing dependency of the kind
     * compostion master to detail between the specified source and target objects. A Dependency
     * instance indicates that the source references the target and hence the source depends on the
     * target.
     */
    public final static IpsObjectDependency createCompostionMasterDetailDependency(QualifiedNameType source,
            QualifiedNameType target) {
        return new IpsObjectDependency(source, target, DependencyType.REFERENCE_COMPOSITION_MASTER_DETAIL);
    }

    /**
     * Creates a new Dependency instance indicating an instance of dependency between the specified
     * source and target objects. A Dependency instance indicates that the source is an instance of
     * the target and hence the source depends on the target.
     */
    public final static IpsObjectDependency createInstanceOfDependency(QualifiedNameType source, QualifiedNameType target) {
        return new IpsObjectDependency(source, target, DependencyType.INSTANCEOF);
    }

    /**
     * The source object
     */
    public QualifiedNameType getSource() {
        return source;
    }

    /**
     * The target object
     */
    public QualifiedNameType getTargetAsQNameType() {
        return target;
    }

    /**
     * The target object
     */
    public Object getTarget() {
        return target;
    }

    /**
     * {@inheritDoc}
     */
    public DependencyType getType() {
        return dependencyType;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (o instanceof IDependency) {
            IDependency other = (IDependency)o;
            return this.dependencyType.equals(other.getType()) && this.target.equals(other.getTarget())
                    && this.source.equals(other.getSource());
        }
        return false;
    }

    private void calculateHashCode() {
        int result = 17;
        result = result * 37 + target.hashCode();
        result = result * 37 + source.hashCode();
        result = result * 37 + dependencyType.hashCode();
        hashCode = result;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return hashCode;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "(" + source.toString() + " -> " + target.toString() + ", type: " + dependencyType + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
