/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;

public class ProductCmptTypeHierarchyVisitorTest extends AbstractIpsPluginTest {

    private IProductCmptType type;
    private IProductCmptType supertype;
    private IProductCmptType superSupertype;
    private IIpsProject ipsProject;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        type = newProductCmptType(ipsProject, "Type");
        supertype = newProductCmptType(ipsProject, "Supertype");
        superSupertype = newProductCmptType(ipsProject, "SuperSupertype");
        
        type.setSupertype("Supertype");
        supertype.setSupertype("SuperSupertype");
    }
    
    public void test_NoCycle() throws CoreException {
        MyVisitor visitor = new MyVisitor(ipsProject);
        visitor.start(type);
        IProductCmptType[] types = visitor.getVisitedProductCmptTypes();
        assertEquals(3, types.length);
        assertEquals(type, types[0]);
        assertEquals(supertype, types[1]);
        assertEquals(superSupertype, types[2]);
        assertFalse(visitor.cycleDetected());

        visitor.stopVisitingAfterThisType = supertype;
        visitor.start(type);
        types = visitor.getVisitedProductCmptTypes();
        assertEquals(2, types.length);
        assertEquals(type, types[0]);
        assertEquals(supertype, types[1]);
        assertFalse(visitor.cycleDetected());
        
        visitor.stopVisitingAfterThisType = null;
        visitor.start(superSupertype);
        types = visitor.getVisitedProductCmptTypes();
        assertEquals(1, types.length);
        assertEquals(superSupertype, types[0]);
        assertFalse(visitor.cycleDetected());
        
        visitor.start(null);
        types = visitor.getVisitedProductCmptTypes();
        assertEquals(0, types.length);
        assertFalse(visitor.cycleDetected());
    }
    
    public void test_WithCycle() throws CoreException {
        superSupertype.setSupertype("Type");
        ProductCmptTypeHierarchyVisitor visitor = new MyVisitor(ipsProject);
        visitor.start(type);
        IProductCmptType[] types = visitor.getVisitedProductCmptTypes();
        assertEquals(3, types.length);
        assertEquals(type, types[0]);
        assertEquals(supertype, types[1]);
        assertEquals(superSupertype, types[2]);
        assertTrue(visitor.cycleDetected());
        
        visitor.start(null);
        types = visitor.getVisitedProductCmptTypes();
        assertEquals(0, types.length);
        assertFalse(visitor.cycleDetected());
    }
    
    
    public class MyVisitor extends ProductCmptTypeHierarchyVisitor {

        private IProductCmptType stopVisitingAfterThisType = null;
        
        public MyVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            if (stopVisitingAfterThisType==null) {
                return true;
            }
            return currentType!=stopVisitingAfterThisType;
        }
        
    }
}
