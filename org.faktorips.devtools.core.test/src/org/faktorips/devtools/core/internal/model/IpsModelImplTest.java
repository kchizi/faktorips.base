/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model;

import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.DecimalHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsModelImplTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsModel model;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        model = (IpsModel)ipsProject.getIpsModel();
    }

    public void testGetIpsObjectPath() throws CoreException{
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.getSourceFolderEntries()[0].setSpecificBasePackageNameForMergableJavaClasses("newpackage");
        ipsProject.setIpsObjectPath(path);
        
        //path is created in the first call
        path = ipsProject.getIpsObjectPath();
        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForMergableJavaClasses());

        //path is read from the cache in the second call
        path = ipsProject.getIpsObjectPath();
        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForMergableJavaClasses());

        IIpsProject secondProject = newIpsProject("TestProject2");
        IIpsObjectPath secondPath = secondProject.getIpsObjectPath();
        secondPath.getSourceFolderEntries()[0].setSpecificBasePackageNameForMergableJavaClasses("secondpackage");
        secondProject.setIpsObjectPath(secondPath);
        
        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForMergableJavaClasses());
        assertEquals("secondpackage", secondPath.getSourceFolderEntries()[0].getSpecificBasePackageNameForMergableJavaClasses());
    }
    
    
    public void testGetValueDatatypes() throws CoreException, IOException {
    	IIpsProjectProperties props = ipsProject.getProperties();
    	props.setPredefinedDatatypesUsed(new String[]{Datatype.DECIMAL.getQualifiedName()});
        ipsProject.setProperties(props);
        SortedSet datatypes = new TreeSet();
        model.getValueDatatypes(ipsProject, datatypes);
        assertEquals(1, datatypes.size());
        Iterator it = datatypes.iterator();
        assertEquals(Datatype.DECIMAL, it.next());
    }

    public void testGetDatatypeHelpers() throws IOException, CoreException {
    	IIpsProjectProperties props = ipsProject.getProperties();
    	props.setPredefinedDatatypesUsed(new String[]{Datatype.DECIMAL.getQualifiedName()});
        ipsProject.setProperties(props);
        DatatypeHelper helper = model.getDatatypeHelper(ipsProject, Datatype.DECIMAL);
        assertEquals(DecimalHelper.class, helper.getClass());
        assertNull(model.getDatatypeHelper(ipsProject, Datatype.MONEY));
    }
    
}
