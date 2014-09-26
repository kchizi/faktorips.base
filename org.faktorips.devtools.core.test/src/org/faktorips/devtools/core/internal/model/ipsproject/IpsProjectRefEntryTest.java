/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsProjectRefEntryTest extends AbstractIpsPluginTest {

    private static final String MY_RESOURCE_PATH = "myResourcePath";

    private IIpsProject ipsProject;

    private IpsObjectPath path;

    private IpsObjectPathSearchContext searchContext;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = new IpsObjectPath(ipsProject);
        searchContext = new IpsObjectPathSearchContext(ipsProject);
    }

    @Test
    public void testFindIpsSrcFiles() throws Exception {
        IpsProject refProject = (IpsProject)newIpsProject("RefProject");
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.B");

        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "b.C");

        path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        IpsProjectRefEntry entry = (IpsProjectRefEntry)path.newIpsProjectRefEntry(refProject);
        ipsProject.setIpsObjectPath(path);

        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        entry.findIpsSrcFilesInternal(IpsObjectType.POLICY_CMPT_TYPE, null, result, new HashSet<IIpsObjectPathEntry>());

        assertTrue(result.contains(a.getIpsSrcFile()));
        assertTrue(result.contains(b.getIpsSrcFile()));
        assertFalse(result.contains(c.getIpsSrcFile()));
    }

    @Test
    public void testFindIpsSrcFiles_byQualifiedNameType() throws Exception {
        IpsProject refProject = (IpsProject)newIpsProject("RefProject");
        newPolicyCmptTypeWithoutProductCmptType(refProject, "a.A");

        path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        IpsProjectRefEntry entry = (IpsProjectRefEntry)path.newIpsProjectRefEntry(refProject);
        ipsProject.setIpsObjectPath(path);
        entry.setReexported(false);

        IIpsSrcFile srcFile = entry.findIpsSrcFile(new QualifiedNameType("a.A", IpsObjectType.POLICY_CMPT_TYPE));
        assertNotNull(srcFile);
        assertEquals("a.A", srcFile.getQualifiedNameType().getName());
    }

    @Test
    public void testFindIpsSrcFiles_NoReexport() throws Exception {
        IpsProject refProject = (IpsProject)newIpsProject("RefProject");
        IpsProject refProject2 = (IpsProject)newIpsProject("RefProject2");
        newPolicyCmptTypeWithoutProductCmptType(refProject2, "x.X");

        path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        IpsObjectPath pathRef = (IpsObjectPath)refProject.getIpsObjectPath();
        IpsObjectPath pathRef2 = (IpsObjectPath)refProject2.getIpsObjectPath();
        IpsProjectRefEntry entry = (IpsProjectRefEntry)path.newIpsProjectRefEntry(refProject);
        IpsProjectRefEntry entryRef = (IpsProjectRefEntry)pathRef.newIpsProjectRefEntry(refProject2);
        entry.setReexported(false);
        entryRef.setReexported(false);

        ipsProject.setIpsObjectPath(path);
        refProject.setIpsObjectPath(pathRef);
        refProject2.setIpsObjectPath(pathRef2);

        IIpsSrcFile srcFile = entry.findIpsSrcFile(new QualifiedNameType("x.X", IpsObjectType.POLICY_CMPT_TYPE));
        assertNull(srcFile);

        srcFile = entryRef.findIpsSrcFile(new QualifiedNameType("x.X", IpsObjectType.POLICY_CMPT_TYPE));
        assertNotNull(srcFile);
        assertEquals("x.X", srcFile.getQualifiedNameType().getName());
    }

    @Test
    public void testFindIpsSrcFiles_MultipleReferencesNoReexport() throws Exception {
        IpsProject refProject = (IpsProject)newIpsProject("RefProject");
        IpsProject refProject2 = (IpsProject)newIpsProject("RefProject2");
        newPolicyCmptTypeWithoutProductCmptType(refProject2, "x.X");

        path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        IpsProjectRefEntry refEntry = (IpsProjectRefEntry)path.newIpsProjectRefEntry(refProject);
        IpsProjectRefEntry refEntry2 = (IpsProjectRefEntry)path.newIpsProjectRefEntry(refProject2);
        refEntry.setReexported(false);
        refEntry2.setReexported(false);

        ipsProject.setIpsObjectPath(path);

        IIpsSrcFile srcFile = path.findIpsSrcFile(new QualifiedNameType("x.X", IpsObjectType.POLICY_CMPT_TYPE));
        assertNotNull(srcFile);
        assertEquals("x.X", srcFile.getQualifiedNameType().getName());
    }

    @Test
    public void testFindIpsSrcFiles_WithReexport() throws Exception {
        IpsProject refProject = (IpsProject)newIpsProject("RefProject");
        IpsProject refProject2 = (IpsProject)newIpsProject("RefProject2");
        newPolicyCmptTypeWithoutProductCmptType(refProject2, "x.X");

        path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        IpsObjectPath pathRef = (IpsObjectPath)refProject.getIpsObjectPath();
        IpsObjectPath pathRef2 = (IpsObjectPath)refProject2.getIpsObjectPath();
        IpsProjectRefEntry entry = (IpsProjectRefEntry)path.newIpsProjectRefEntry(refProject);
        IpsProjectRefEntry entryRef = (IpsProjectRefEntry)pathRef.newIpsProjectRefEntry(refProject2);

        ipsProject.setIpsObjectPath(path);
        refProject.setIpsObjectPath(pathRef);
        refProject2.setIpsObjectPath(pathRef2);

        IIpsSrcFile srcFile = entry.findIpsSrcFile(new QualifiedNameType("x.X", IpsObjectType.POLICY_CMPT_TYPE));
        assertNotNull(srcFile);

        srcFile = entryRef.findIpsSrcFile(new QualifiedNameType("x.X", IpsObjectType.POLICY_CMPT_TYPE));
        assertNotNull(srcFile);
        assertEquals("x.X", srcFile.getQualifiedNameType().getName());
    }

    @Test
    public void testFindIpsSrcFilesWithPackageFragment() throws Exception {
        IpsProject refProject = (IpsProject)newIpsProject("RefProject");

        // policy cmpt types in ref project
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.b.c.A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.b.c.B");

        // policy cmpt types in original project
        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.c.C");

        // policy cmpt types in ref project
        IPolicyCmptType a2 = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.b.d.A");
        IPolicyCmptType b2 = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.b.d.B");

        // policy cmpt types in original project
        IPolicyCmptType c2 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.d.C");

        path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        IpsProjectRefEntry entry = (IpsProjectRefEntry)path.newIpsProjectRefEntry(refProject);

        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        entry.findIpsSrcFilesInternal(IpsObjectType.POLICY_CMPT_TYPE, "a.b.c", result,
                new HashSet<IIpsObjectPathEntry>());

        assertEquals(2, result.size());
        assertTrue(result.contains(a.getIpsSrcFile()));
        assertTrue(result.contains(b.getIpsSrcFile()));
        assertFalse(result.contains(c.getIpsSrcFile()));

        result = new ArrayList<IIpsSrcFile>();
        entry.findIpsSrcFilesInternal(IpsObjectType.POLICY_CMPT_TYPE, "a.b.d", result,
                new HashSet<IIpsObjectPathEntry>());

        assertEquals(2, result.size());
        assertTrue(result.contains(a2.getIpsSrcFile()));
        assertTrue(result.contains(b2.getIpsSrcFile()));
        assertFalse(result.contains(c2.getIpsSrcFile()));

    }

    @Test
    public void testInitFromXml() {
        Document doc = getTestDocument();
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path);
        entry.initFromXml(doc.getDocumentElement(), ipsProject.getProject());
        assertEquals(IpsPlugin.getDefault().getIpsModel().getIpsProject("RefProject"), entry.getReferencedIpsProject());
        assertFalse(entry.isUseNWDITrackPrefix());
        assertFalse(entry.isReexported());
    }

    @Test
    public void testToXml() {
        IIpsProject refProject = IpsPlugin.getDefault().getIpsModel().getIpsProject("RefProject");
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path, refProject);
        assertTrue(entry.isReexported());
        entry.setReexported(false);
        Element element = entry.toXml(newDocument());

        entry = new IpsProjectRefEntry(path);
        entry.initFromXml(element, ipsProject.getProject());
        assertEquals(refProject, entry.getReferencedIpsProject());
        assertFalse(entry.isUseNWDITrackPrefix());
        assertFalse(entry.isReexported());
    }

    @Test
    public void testValidate() throws CoreException {
        IIpsProjectProperties props = ipsProject.getProperties();
        path = (IpsObjectPath)props.getIpsObjectPath();
        IIpsProject refProject = this.newIpsProject("TestProject2");
        path.newIpsProjectRefEntry(refProject);
        ipsProject.setProperties(props);

        MessageList ml = ipsProject.validate();
        assertEquals(0, ml.size());

        // validate missing project reference
        refProject = IpsPlugin.getDefault().getIpsModel().getIpsProject("none");
        path.newIpsProjectRefEntry(refProject);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(1, ml.size());
        assertNotNull(ml.getMessageByCode(IIpsObjectPathEntry.MSGCODE_MISSING_PROJECT));

        // validate empty project name
        path.removeProjectRefEntry(refProject);
        path.newIpsProjectRefEntry(null);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(1, ml.size());
        assertNotNull(ml.getMessageByCode(IIpsObjectPathEntry.MSGCODE_PROJECT_NOT_SPECIFIED));
    }

    @Test
    public void testContainsResource_true() throws Exception {
        IpsProject referencedProject = mock(IpsProject.class);
        IpsObjectPath ipsObjectPath = mock(IpsObjectPath.class);
        when(ipsObjectPath.containsResource(MY_RESOURCE_PATH, searchContext)).thenReturn(true);
        when(ipsObjectPath.getIpsProject()).thenReturn(referencedProject);
        when(referencedProject.getIpsObjectPathInternal()).thenReturn(ipsObjectPath);
        when(referencedProject.getName()).thenReturn("refProject");
        IpsProjectRefEntry projectRefEntry = new IpsProjectRefEntry(ipsObjectPath, referencedProject);

        assertTrue(projectRefEntry.containsResource(MY_RESOURCE_PATH, searchContext));
    }

    @Test
    public void testContainsResource_false() throws Exception {
        IpsProjectRefEntry projectRefEntry = new IpsProjectRefEntry(path, ipsProject);

        assertFalse(projectRefEntry.containsResource(MY_RESOURCE_PATH, searchContext));
    }

}
