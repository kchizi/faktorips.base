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
package org.faktorips.devtools.core.ui.views.instanceexplorer;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * @author dirmeier
 * 
 */
public class InstanceContentProvideTest extends AbstractIpsPluginTest {

    // private InstanceContentProvider contentProvider = new InstanceContentProvider();

    private IIpsProject ipsProject;

    private IIpsProject referencingProject;

    // private IIpsProject independentProject;

    private IIpsProject leaveProject1;

    private IIpsProject leaveProject2;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();

        referencingProject = newIpsProject("ReferencingProject");
        IIpsObjectPath path = referencingProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        referencingProject.setIpsObjectPath(path);

        /*
         * leaveProject1 and leaveProject2 are not directly integrated in any test. But the tested
         * instance search methods have to search in all project that holds a reference to the
         * project of the object. So the search for a Object in e.g. ipsProject have to search for
         * instances in leaveProject1 and leaveProject2. The tests implicit that no duplicates are
         * found.
         */

        leaveProject1 = newIpsProject("LeaveProject1");
        path = leaveProject1.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencingProject);
        leaveProject1.setIpsObjectPath(path);

        leaveProject2 = newIpsProject("LeaveProject2");
        path = leaveProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencingProject);
        leaveProject2.setIpsObjectPath(path);

        // independentProject = newIpsProject("ReferencedProject");
    }

    public void testEmtptyTest() {
        assertTrue(true);
    }

    // /**
    // * Test method for
    // * {@link
    // org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceContentProvider#getElements(java.lang.Object)}
    // * .
    // *
    // * @throws CoreException
    // */
    // public void testGetElements() throws CoreException {
    // contentProvider.asyncSetInputData(null, new JobChangeAdapter() {
    //
    // @Override
    // public void done(IJobChangeEvent event) {
    // Object[] result = contentProvider.getElements(null);
    // assertTrue(result.length == 0);
    //
    // Object object = new Object();
    // try {
    // result = contentProvider.getElements(object);
    // fail();
    // } catch (Exception e) {
    // }
    // }
    // });
    // }
    //
    // private IIpsArchive createArchive(IIpsProject project, IIpsProject projectToArchive) throws
    // CoreException {
    // IFile archiveFile = projectToArchive.getProject().getFile("test.ipsar");
    // archiveFile.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    //
    // File file = archiveFile.getLocation().toFile();
    // CreateIpsArchiveOperation operation = new CreateIpsArchiveOperation(projectToArchive
    // .getIpsPackageFragmentRoots(), file);
    // operation.setInclJavaBinaries(true);
    // operation.setInclJavaSources(true);
    // operation.run(null);
    // createLinkIfNecessary(archiveFile, file);
    //
    // assertTrue(archiveFile.exists());
    //
    // IIpsArchive archive = new IpsArchive(project, archiveFile.getLocation());
    // return archive;
    // }
    //
    // /**
    // * Test method for
    // * {@link
    // org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceContentProvider#getElements(java.lang.Object)}
    // * .
    // *
    // * @throws CoreException
    // */
    // public void testGetElementsForProductCmptType() throws CoreException {
    // String prodCmptTypeQName = "pack.MyProductCmptType";
    // String prodCmptTypeIndepQName = "otherpack.MyProductCmptTypeProj2";
    // String prodCmpt1QName = "pack.MyProductCmpt1";
    // String prodCmpt2QName = "pack.MyProductCmpt2";
    // String prodCmpt3QName = "pack.MyProductCmpt3";
    // String prodCmptRef1QName = "otherpack.MyProductCmptRef1";
    //
    // final ProductCmptType prodCmptType = newProductCmptType(ipsProject, prodCmptTypeQName);
    // final ProductCmpt[] prodCmpt = new ProductCmpt[3];
    // prodCmpt[0] = newProductCmpt(prodCmptType, prodCmpt1QName);
    // prodCmpt[1] = newProductCmpt(prodCmptType, prodCmpt2QName);
    // prodCmpt[2] = newProductCmpt(ipsProject, prodCmpt3QName);
    //
    // contentProvider.asyncSetInputData(prodCmptType, new JobChangeAdapter() {
    // @Override
    // public void done(IJobChangeEvent event) {
    // Object[] result = contentProvider.getElements(prodCmptType);
    // assertEquals(2, result.length);
    // boolean[] included = new boolean[3];
    // for (Object obj : result) {
    // if (obj instanceof InstanceViewerItem) {
    // InstanceViewerItem item = (InstanceViewerItem)obj;
    // for (int i = 0; i < 3; i++) {
    // if (item.getIpsSrcFile().equals(prodCmpt[i].getIpsSrcFile())) {
    // included[i] = true;
    // continue;
    // }
    // }
    // } else {
    // fail("Not a InstanceViewerItem: " + obj.toString());
    // }
    // }
    // assertTrue(included[0]);
    // assertTrue(included[1]);
    // assertFalse(included[2]);
    // }
    //
    // });
    //
    // final ProductCmpt prodCmptRef1 = newProductCmpt(referencingProject, prodCmptRef1QName);
    // prodCmptRef1.setProductCmptType(prodCmptTypeQName);
    //
    // contentProvider.asyncSetInputData(prodCmptType, new JobChangeAdapter() {
    //
    // @Override
    // public void done(IJobChangeEvent event) {
    // Object[] result = contentProvider.getElements(prodCmptType);
    // assertEquals(result.length, 3);
    //
    // boolean[] included = new boolean[4];
    // for (Object obj : result) {
    // if (obj instanceof InstanceViewerItem) {
    // InstanceViewerItem item = (InstanceViewerItem)obj;
    // for (int i = 0; i < 3; i++) {
    // if (item.getIpsSrcFile().equals(prodCmpt[i].getIpsSrcFile())) {
    // included[i] = true;
    // continue;
    // }
    // }
    // if (item.getIpsSrcFile().equals(prodCmptRef1.getIpsSrcFile())) {
    // included[3] = true;
    // }
    // } else {
    // fail("Not a InstanceViewerItem: " + obj.toString());
    // }
    // }
    // assertTrue(included[0]);
    // assertTrue(included[1]);
    // assertFalse(included[2]);
    // assertTrue(included[3]);
    // }
    //
    // });
    //
    // final ProductCmptType prodCmptTypeIndep = newProductCmptType(independentProject,
    // prodCmptTypeIndepQName);
    //
    // contentProvider.asyncSetInputData(prodCmptTypeIndep, new JobChangeAdapter() {
    //
    // @Override
    // public void done(IJobChangeEvent event) {
    // try {
    // Object[] result = contentProvider.getElements(prodCmptTypeIndep);
    // assertEquals(result.length, 0);
    //
    // String archTypeQName = "archpack.ArchType";
    // String archCmptQName = "archpack.ArchCmpt";
    // IIpsProject archProj = newIpsProject("archProj");
    // ProductCmptType archType = newProductCmptType(archProj, archTypeQName);
    // @SuppressWarnings("unused")
    // ProductCmpt archCmpt = newProductCmpt(archType, archCmptQName);
    // IIpsArchive archive = createArchive(ipsProject, archProj);
    //
    // IIpsObjectPath path = referencingProject.getIpsObjectPath();
    // path.newIpsProjectRefEntry(archProj);
    // referencingProject.setIpsObjectPath(path);
    //
    // path = leaveProject1.getIpsObjectPath();
    // IIpsArchiveEntry aentry = path.newArchiveEntry(archive.getArchivePath());
    // path.moveEntries(new int[] { aentry.getIndex() }, true);
    // leaveProject1.setIpsObjectPath(path);
    //
    // contentProvider.asyncSetInputData(archType, null);
    // result = contentProvider.getElements(archType);
    // assertEquals(2, result.length);
    // } catch (Exception e) {
    // fail();
    // }
    // }
    //
    // });
    // }
    //
    // /**
    // * Test method for
    // * {@link
    // org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceContentProvider#getElements(java.lang.Object)}
    // * .
    // *
    // * @throws CoreException
    // */
    // public void testGetElementsForTableStructure() throws CoreException {
    // String tableStructureQName = "pack.MyTableStructure";
    // String tableStructureProj2QName = "otherpack.MyTableStructureProj2";
    // String tabContent1QName = "pack.MyTableContent1";
    // String tabContent2QName = "pack.MyTableContent2";
    // String tabContent3QName = "pack.MyTableContent3";
    // String tabContentProj2QName = "otherpack.MyTableContentProj2";
    //
    // final TableStructure tableStructure = newTableStructure(ipsProject, tableStructureQName);
    // final TableContents[] tabContent = new TableContents[3];
    // tabContent[0] = newTableContents(tableStructure, tabContent1QName);
    // tabContent[1] = newTableContents(tableStructure, tabContent2QName);
    // tabContent[2] = newTableContents(ipsProject, tabContent3QName);
    //
    // contentProvider.asyncSetInputData(tableStructure, new JobChangeAdapter() {
    //
    // @Override
    // public void done(IJobChangeEvent event) {
    //
    // Object[] result = contentProvider.getElements(tableStructure);
    // assertEquals(2, result.length);
    //
    // boolean[] included = new boolean[3];
    // for (Object obj : result) {
    // if (obj instanceof InstanceViewerItem) {
    // InstanceViewerItem item = (InstanceViewerItem)obj;
    // for (int i = 0; i < 3; i++) {
    // if (item.getIpsSrcFile().equals(tabContent[i].getIpsSrcFile())) {
    // included[i] = true;
    // continue;
    // }
    // }
    // } else {
    // fail("Not a InstanceViewerItem: " + obj.toString());
    // }
    // }
    // assertTrue(included[0]);
    // assertTrue(included[1]);
    // assertFalse(included[2]);
    // }
    //
    // });
    //
    // final TableContents tabContentProj2 = newTableContents(referencingProject,
    // tabContentProj2QName);
    // tabContentProj2.setTableStructure(tableStructureQName);
    //
    // contentProvider.asyncSetInputData(tableStructure, new JobChangeAdapter() {
    //
    // @Override
    // public void done(IJobChangeEvent event) {
    // Object[] result = contentProvider.getElements(tableStructure);
    // assertEquals(3, result.length);
    //
    // boolean[] included = new boolean[4];
    // for (Object obj : result) {
    // if (obj instanceof InstanceViewerItem) {
    // InstanceViewerItem item = (InstanceViewerItem)obj;
    // for (int i = 0; i < 3; i++) {
    // if (item.getIpsSrcFile().equals(tabContent[i].getIpsSrcFile())) {
    // included[i] = true;
    // continue;
    // }
    // }
    // if (item.getIpsSrcFile().equals(tabContentProj2.getIpsSrcFile())) {
    // included[3] = true;
    // }
    // } else {
    // fail("Not a InstanceViewerItem: " + obj.toString());
    // }
    // }
    // assertTrue(included[0]);
    // assertTrue(included[1]);
    // assertFalse(included[2]);
    // assertTrue(included[3]);
    // }
    //
    // });
    //
    // final TableStructure tableStructureProj2 = newTableStructure(independentProject,
    // tableStructureProj2QName);
    //
    // contentProvider.asyncSetInputData(tableStructureProj2, new JobChangeAdapter() {
    //
    // @Override
    // public void done(IJobChangeEvent event) {
    // Object[] result = contentProvider.getElements(tableStructureProj2);
    // assertEquals(0, result.length);
    // }
    //
    // });
    // }
    //
    // /**
    // * Test method for
    // * {@link
    // org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceContentProvider#getElements(java.lang.Object)}
    // * .
    // *
    // * @throws CoreException
    // */
    // public void testGetElementsForTestCaseTypes() throws CoreException {
    // String testCaseTypeQName = "pack.MyTestCaseType";
    // String testCaseTypeProj2QName = "otherpack.MyTestCaseTypeProj2";
    // String testCase1QName = "pack.MyTestCase1";
    // String testCase2QName = "pack.MyTestCase2";
    // String testCase3QName = "pack.MyTestCase3";
    // String testCaseProj2QName = "otherpack.MyTestCaseProj2";
    //
    // final TestCaseType testCaseType = newTestCaseType(ipsProject, testCaseTypeQName);
    // final TestCase[] testCase = new TestCase[3];
    // testCase[0] = newTestCase(testCaseType, testCase1QName);
    // testCase[1] = newTestCase(testCaseType, testCase2QName);
    // testCase[2] = newTestCase(ipsProject, testCase3QName);
    //
    // contentProvider.asyncSetInputData(testCaseType, new JobChangeAdapter() {
    //
    // @Override
    // public void done(IJobChangeEvent event) {
    // Object[] result = contentProvider.getElements(testCaseType);
    // assertEquals(2, result.length);
    //
    // boolean[] included = new boolean[3];
    // for (Object obj : result) {
    // if (obj instanceof InstanceViewerItem) {
    // InstanceViewerItem item = (InstanceViewerItem)obj;
    // for (int i = 0; i < 3; i++) {
    // if (item.getIpsSrcFile().equals(testCase[i].getIpsSrcFile())) {
    // included[i] = true;
    // continue;
    // }
    // }
    // } else {
    // fail("Not a InstanceViewerItem: " + obj.toString());
    // }
    // }
    // assertTrue(included[0]);
    // assertTrue(included[1]);
    // assertFalse(included[2]);
    // }
    //
    // });
    //
    // final TestCase testCaseProj2 = newTestCase(referencingProject, testCaseProj2QName);
    // testCaseProj2.setTestCaseType(testCaseTypeQName);
    //
    // contentProvider.asyncSetInputData(testCaseType, new JobChangeAdapter() {
    //
    // @Override
    // public void done(IJobChangeEvent event) {
    // Object[] result = contentProvider.getElements(testCaseType);
    // assertEquals(3, result.length);
    //
    // boolean[] included = new boolean[4];
    // for (Object obj : result) {
    // if (obj instanceof InstanceViewerItem) {
    // InstanceViewerItem item = (InstanceViewerItem)obj;
    // for (int i = 0; i < 3; i++) {
    // if (item.getIpsSrcFile().equals(testCase[i].getIpsSrcFile())) {
    // included[i] = true;
    // continue;
    // }
    // }
    // if (item.getIpsSrcFile().equals(testCaseProj2.getIpsSrcFile())) {
    // included[3] = true;
    // }
    // } else {
    // fail("Not a InstanceViewerItem: " + obj.toString());
    // }
    // }
    // assertTrue(included[0]);
    // assertTrue(included[1]);
    // assertFalse(included[2]);
    // assertTrue(included[3]);
    // }
    //
    // });
    //
    // final TestCaseType testCaseTypeProj2 = newTestCaseType(independentProject,
    // testCaseTypeProj2QName);
    //
    // contentProvider.asyncSetInputData(testCaseTypeProj2, new JobChangeAdapter() {
    //
    // @Override
    // public void done(IJobChangeEvent event) {
    // Object[] result = contentProvider.getElements(testCaseTypeProj2);
    // assertEquals(0, result.length);
    // }
    //
    // });
    // }
    //
    // /**
    // * Test method for
    // * {@link
    // org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceContentProvider#getElements(java.lang.Object)}
    // * .
    // *
    // * @throws CoreException
    // */
    // public void testGetElementsForEnumTypes() throws CoreException {
    // String enumTypeQName = "pack.MyEnumType";
    // String enumTypeProj2QName = "otherpack.MyEnumTypeProj2";
    // String enum1QName = "pack.MyEnum1";
    // String enum2QName = "pack.MyEnum2";
    // String enum3QName = "pack.MyEnum3";
    // String enumProj2QName = "otherpack.MyEnumProj2";
    //
    // EnumType enumType = newEnumType(ipsProject, enumTypeQName);
    // EnumContent[] enumc = new EnumContent[3];
    // enumc[0] = newEnumContent(enumType, enum1QName);
    // enumc[1] = newEnumContent(enumType, enum2QName);
    // enumc[2] = newEnumContent(ipsProject, enum3QName);
    //
    // contentProvider.asyncSetInputData(enumType, new JobChangeAdapter() {
    //
    // });
    //
    // Object[] result = contentProvider.getElements(enumType);
    // assertEquals(2, result.length);
    // boolean[] included = new boolean[3];
    // for (Object obj : result) {
    // if (obj instanceof InstanceViewerItem) {
    // InstanceViewerItem item = (InstanceViewerItem)obj;
    // for (int i = 0; i < 3; i++) {
    // if (item.getIpsSrcFile().equals(enumc[i].getIpsSrcFile())) {
    // included[i] = true;
    // continue;
    // }
    // }
    // } else {
    // fail("Not a InstanceViewerItem: " + obj.toString());
    // }
    // }
    // assertTrue(included[0]);
    // assertTrue(included[1]);
    // assertFalse(included[2]);
    //
    // result = contentProvider.getElements(enumc[0]);
    // assertEquals(0, result.length);
    //
    // EnumContent enumProj2 = newEnumContent(referencingProject, enumProj2QName);
    // enumProj2.setEnumType(enumTypeQName);
    //
    // result = contentProvider.getElements(enumType);
    // assertEquals(3, result.length);
    //
    // included = new boolean[4];
    // for (Object obj : result) {
    // if (obj instanceof InstanceViewerItem) {
    // InstanceViewerItem item = (InstanceViewerItem)obj;
    // for (int i = 0; i < 3; i++) {
    // if (item.getIpsSrcFile().equals(enumc[i].getIpsSrcFile())) {
    // included[i] = true;
    // continue;
    // }
    // }
    // if (item.getIpsSrcFile().equals(enumProj2.getIpsSrcFile())) {
    // included[3] = true;
    // }
    // } else {
    // fail("Not a InstanceViewerItem: " + obj.toString());
    // }
    // }
    // assertTrue(included[0]);
    // assertTrue(included[1]);
    // assertFalse(included[2]);
    // assertTrue(included[3]);
    //
    // EnumType enumTypeProj2 = newEnumType(independentProject, enumTypeProj2QName);
    //
    // result = contentProvider.getElements(enumTypeProj2);
    // assertEquals(0, result.length);
    // }

}
