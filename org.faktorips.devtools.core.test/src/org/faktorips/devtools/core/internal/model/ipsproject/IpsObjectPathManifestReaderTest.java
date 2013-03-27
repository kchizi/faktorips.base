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

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.osgi.util.ManifestElement;
import org.faktorips.devtools.core.internal.model.ipsproject.jdtcontainer.IpsContainer4JdtClasspathContainerType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsObjectPathManifestReaderTest {

    private static final String MY_BAS_PACK = "myBasPack";

    private static final String MY_OBJECT_DIR = "myObjectDir";

    private static final String MY_TOC_FILE = "myTocFile";

    private static final String MY_VALIDATION_MESSAGE = "myValidationMessage";

    private static final String MY_SRC_OUT = "mySrcOut";

    private static final String MY_RESOURCE_OUT = "myResourceOut";

    @Mock
    private IpsBundleManifest bundleManifest;

    @Mock
    private IpsProject ipsProject;

    @Mock
    private IFolder myObjectDir;

    @Mock
    private IFolder mySrcOut;

    @Mock
    private IFolder myResourceOut;

    private IpsObjectPathManifestReader objectPathReader;

    @Before
    public void mockIpsProjectAndFolders() {
        IProject project = mock(IProject.class);
        when(ipsProject.getProject()).thenReturn(project);
        when(myObjectDir.getName()).thenReturn(MY_OBJECT_DIR);
        when(project.getFolder(MY_OBJECT_DIR)).thenReturn(myObjectDir);
        when(project.getFolder(MY_SRC_OUT)).thenReturn(mySrcOut);
        when(project.getFolder(MY_RESOURCE_OUT)).thenReturn(myResourceOut);
    }

    @Before
    public void createIpsObjectPathManifestReader() throws Exception {
        objectPathReader = new IpsObjectPathManifestReader(bundleManifest, ipsProject);
    }

    @Test
    public void testReadIpsObjectPath_emptyOnlyContainer() throws Exception {
        mockObjectDirElements();

        IIpsObjectPath ipsObjectPath = objectPathReader.readIpsObjectPath();

        assertEquals(1, ipsObjectPath.getEntries().length);
    }

    @Test
    public void testReadIpsObjectPath_minimalSettings() throws Exception {
        mockObjectDirElements(MY_OBJECT_DIR);

        IIpsObjectPath ipsObjectPath = objectPathReader.readIpsObjectPath();

        assertEquals(2, ipsObjectPath.getEntries().length);
    }

    @Test
    public void testReadIpsObjectPath_twoEntries() throws Exception {
        mockObjectDirElements(MY_OBJECT_DIR, MY_OBJECT_DIR);

        IIpsObjectPath ipsObjectPath = objectPathReader.readIpsObjectPath();

        assertEquals(3, ipsObjectPath.getEntries().length);
    }

    @Test
    public void testReadIpsObjectPath_checkEntry() throws Exception {
        ManifestElement[] objectDirElements = mockObjectDirElements(MY_OBJECT_DIR);
        when(bundleManifest.getBasePackage(MY_OBJECT_DIR)).thenReturn(MY_BAS_PACK);
        when(bundleManifest.getSourcecodeOutput(MY_OBJECT_DIR)).thenReturn(MY_SRC_OUT);
        when(bundleManifest.getResourceOutput(MY_OBJECT_DIR)).thenReturn(MY_RESOURCE_OUT);
        when(bundleManifest.getTocPath(objectDirElements[0])).thenReturn(MY_TOC_FILE);
        when(bundleManifest.getValidationMessagesBundle(objectDirElements[0])).thenReturn(MY_VALIDATION_MESSAGE);

        IIpsObjectPath ipsObjectPath = objectPathReader.readIpsObjectPath();
        IpsSrcFolderEntry ipsSrcFolderEntry = (IpsSrcFolderEntry)ipsObjectPath.getEntries()[0];

        assertEquals(ipsProject, ipsSrcFolderEntry.getIpsProject());
        assertEquals(MY_OBJECT_DIR, ipsSrcFolderEntry.getIpsPackageFragmentRootName());
        assertEquals(ipsObjectPath, ipsSrcFolderEntry.getIpsObjectPath());
        assertEquals(MY_BAS_PACK, ipsSrcFolderEntry.getBasePackageNameForMergableJavaClasses());
        assertEquals(MY_BAS_PACK, ipsSrcFolderEntry.getBasePackageNameForDerivedJavaClasses());
        assertEquals(mySrcOut, ipsSrcFolderEntry.getSpecificOutputFolderForMergableJavaFiles());
        assertEquals(myResourceOut, ipsSrcFolderEntry.getSpecificOutputFolderForDerivedJavaFiles());
        assertEquals(MY_BAS_PACK + "/" + MY_TOC_FILE, ipsSrcFolderEntry.getFullTocPath());
        assertEquals(MY_VALIDATION_MESSAGE, ipsSrcFolderEntry.getValidationMessagesBundle());
    }

    private ManifestElement[] mockObjectDirElements(String... objectDirs) {
        ManifestElement[] manifestElements = new ManifestElement[objectDirs.length];
        int i = 0;
        for (String objectDir : objectDirs) {
            ManifestElement objectDirElement = mock(ManifestElement.class);
            when(objectDirElement.getValue()).thenReturn(objectDir);
            manifestElements[i++] = objectDirElement;
        }
        when(bundleManifest.getObjectDirElements()).thenReturn(manifestElements);
        return manifestElements;
    }

    @Test
    public void testReadIpsObjectPath_checkContainerEntry() throws Exception {
        mockObjectDirElements();

        IIpsObjectPath ipsObjectPath = objectPathReader.readIpsObjectPath();

        assertEquals(1, ipsObjectPath.getEntries().length);
        IpsContainerEntry ipsContainerEntry = (IpsContainerEntry)ipsObjectPath.getEntries()[0];
        assertEquals(IpsContainer4JdtClasspathContainerType.ID, ipsContainerEntry.getContainerTypeId());
        assertEquals(IpsObjectPathManifestReader.REQUIRED_PLUGIN_CONTAINER, ipsContainerEntry.getOptionalPath());
    }

}
