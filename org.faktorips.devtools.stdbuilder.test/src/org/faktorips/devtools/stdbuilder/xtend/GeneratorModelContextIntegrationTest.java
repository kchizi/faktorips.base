/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.ipsproject.bundle.IpsBundleEntry;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.junit.Test;

public class GeneratorModelContextIntegrationTest extends AbstractStdBuilderTest {

    @Test
    public void testGetGeneratorConfig_IpsObject_FromLibrary() throws CoreException, IOException {
        IIpsProject libIpsProject = newIpsProject("lib");
        newPolicyCmptTypeWithoutProductCmptType(libIpsProject, "lib.Policy");
        createManifest(libIpsProject);
        File libFile = createBundle(libIpsProject);
        libIpsProject.getProject().close(null);
        IpsObjectPath ipsObjectPath = (IpsObjectPath)ipsProject.getIpsObjectPath();
        IpsBundleEntry ipsBundleEntry = createBundleEntry(libFile, ipsObjectPath);
        addEntry(ipsObjectPath, ipsBundleEntry);

        IIpsArtefactBuilderSet builderSet = ipsProject.getIpsArtefactBuilderSet();
        assertThat(builderSet, is(instanceOf(StandardBuilderSet.class)));

        GeneratorModelContext generatorModelContext = ((StandardBuilderSet)builderSet).getGeneratorModelContext();
        PolicyCmptType subPolicy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "my.SubPolicy");
        subPolicy.setSupertype("lib.Policy");
        IPolicyCmptType policy = ipsProject.findPolicyCmptType("lib.Policy");
        assertThat(policy, is(notNullValue()));

        GeneratorConfig libGeneratorConfig = generatorModelContext.getGeneratorConfig(policy);
        GeneratorConfig generatorConfig = generatorModelContext.getGeneratorConfig(subPolicy);

        assertThat(libGeneratorConfig, is(not(generatorConfig)));
        assertThat(generatorConfig.getChangesOverTimeNamingConvention().getId(),
                is(IChangesOverTimeNamingConvention.VAA));
        assertThat(libGeneratorConfig.getChangesOverTimeNamingConvention().getId(),
                is(IChangesOverTimeNamingConvention.FAKTOR_IPS));
    }

    private void createManifest(IIpsProject libIpsProject) throws IOException, CoreException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue(Name.MANIFEST_VERSION.toString(), "1.0");
        manifest.getMainAttributes().putValue(IpsBundleManifest.HEADER_OBJECT_DIR, "ipsobjects");
        manifest.getMainAttributes().putValue(IpsBundleManifest.HEADER_GENERATOR_CONFIG,
                StandardBuilderSet.ID + ";" + StandardBuilderSet.CONFIG_PROPERTY_CHANGES_OVER_TIME_NAMING_CONVENTION
                        + "=\"" + IChangesOverTimeNamingConvention.FAKTOR_IPS + "\";"
                        + StandardBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES + "=" + Boolean.TRUE.toString());
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        manifest.write(out);
        out.close();
        IFolder metaInf = libIpsProject.getProject().getFolder("src/META-INF");
        metaInf.create(true, true, null);
        IFile manifestWorkspaceFile = libIpsProject.getProject().getFile("src/META-INF/MANIFEST.MF");
        manifestWorkspaceFile.create(in, true, null);
    }

    private File createBundle(IIpsProject libIpsProject) throws IOException, CoreException {
        File libFile = File.createTempFile("externalArchiveFile", ".jar");
        libFile.deleteOnExit();
        CreateIpsArchiveOperation createIpsArchiveOperation = new CreateIpsArchiveOperation(libIpsProject, libFile);
        createIpsArchiveOperation.setInclJavaSources(true);
        createIpsArchiveOperation.setInclJavaBinaries(true);
        createIpsArchiveOperation.run(null);
        return libFile;
    }

    private IpsBundleEntry createBundleEntry(File libFile, IpsObjectPath ipsObjectPath) throws IOException {
        IpsBundleEntry ipsBundleEntry = new IpsBundleEntry(ipsObjectPath);
        IPath libPath = Path.fromOSString(libFile.getAbsolutePath());
        ipsBundleEntry.initStorage(libPath);
        return ipsBundleEntry;
    }

    private void addEntry(IpsObjectPath ipsObjectPath, IpsBundleEntry ipsBundleEntry) throws CoreException {
        List<IIpsObjectPathEntry> entries = new LinkedList<IIpsObjectPathEntry>(
                Arrays.asList(ipsObjectPath.getEntries()));
        entries.add(ipsBundleEntry);
        ipsObjectPath.setEntries(entries.toArray(new IIpsObjectPathEntry[entries.size()]));
        ipsProject.setIpsObjectPath(ipsObjectPath);
    }

}
