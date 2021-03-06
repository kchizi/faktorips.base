/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Implements a custom Ant task, which imports a given directory to a running Eclipse workspace as
 * project.
 */
public class ProjectImportTask extends AbstractIpsTask {

    /**
     * path to project dir file
     */
    private String projectDir = "";

    /**
     * whether to copy the project content to workspace or not
     */
    private boolean copy = true;

    public ProjectImportTask() {
        super("ProjectImportTask");
    }

    /**
     * Sets the Ant attribute which describes the location of the Eclipse project to import.
     * 
     * @param dir Path to the Project as String
     */
    public void setDir(String dir) {
        this.projectDir = dir;
    }

    /**
     * Returns the path of the Eclipse project to import as String
     * 
     * @return Path as String
     */
    public String getDir() {
        return this.projectDir;
    }

    public boolean isCopy() {
        return copy;
    }

    public void setCopy(boolean copy) {
        this.copy = copy;
    }

    /**
     * Assembles the path to the .project file
     * 
     * @return File
     */
    private File getProjectFile() {
        return new File(this.getDir(), IProjectDescription.DESCRIPTION_FILE_NAME);

    }

    /**
     * Executes the Ant task.
     */
    @Override
    public void executeInternal() throws Exception {

        // Check Dir-Attribute
        checkDir();

        // Fetch Workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // Create
        IProgressMonitor monitor = new NullProgressMonitor();

        // get description provieded in .project File
        InputStream inputStream = new FileInputStream(this.getProjectFile());
        IProjectDescription description = null;

        try {
            description = workspace.loadProjectDescription(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        if (!copy) {
            description.setLocation(Path.fromPortableString(getDir()));
        }

        // create new project with name provided in description
        IProject project = workspace.getRoot().getProject(description.getName());

        // check if project already exists in current workspace
        if (project.exists()) {
            throw new BuildException("Project " + project.getName() + " does already exist.");
        }
        System.out.println("importing: " + project.getName());
        project.create(description, monitor);

        if (copy) {
            RecursiveCopy copyUtil = new RecursiveCopy();
            copyUtil.copyDir(this.getDir(), project.getLocation().toString());
        }

        project.open(monitor);
        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        // build is done via FullBuild-Target separately
    }

    /**
     * Does some security checks on the provided directory attribute
     * 
     * @throws BuildException
     */
    private void checkDir() {

        if (this.getDir() == null || "".equals(this.getDir())) {
            throw new BuildException("Please provide the 'dir' attribute.");
        }

        if (!new File(this.getDir()).exists()) {
            throw new BuildException("Directory " + this.getDir() + " doesn't exist.");
        }

        if (!new File(this.getDir()).isDirectory()) {
            throw new BuildException("Provided 'dir' " + this.getDir() + " is not a Directory.");
        }

        if (!new File(this.getDir()).canRead()) {
            throw new BuildException("Provided 'dir' " + this.getDir() + " is not readable.");
        }
    }

}
