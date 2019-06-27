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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.util.ArgumentCheck;

/**
 * Provides a classloader for the classpath defined in a given Java project.
 */
public class ClassLoaderProvider {

    private IJavaProject javaProject;
    private URLClassLoader classLoader;

    /**
     * a list of IPaths that contain the class files, either a path to a file if it's a Jar-File or
     * to a directory if it's a directory containing class files.
     */
    private final List<IPath> classfileContainers = new ArrayList<IPath>();

    /** listeners that are informed if the contents of the classpath changes */
    private final List<IClasspathContentsChangeListener> classpathContentsChangeListeners = new CopyOnWriteArrayList<IClasspathContentsChangeListener>();

    /**
     * resource change listener that is used to test for changes of the classpath elements (jars and
     * class directories)
     */
    private IResourceChangeListener resourceChangeListener;

    /**
     * Class loader used as parent of the class loader created by this provider.
     */
    private ClassLoader parentClassLoader = null;

    public ClassLoaderProvider(IJavaProject project) {
        this(project, ClassLoader.getSystemClassLoader());
    }

    public ClassLoaderProvider(IJavaProject project, ClassLoader parentClassLoader) {
        ArgumentCheck.notNull(project);
        ArgumentCheck.notNull(parentClassLoader);
        javaProject = project;
        this.parentClassLoader = parentClassLoader;
    }

    /**
     * Returns the classloader for the Java project this is a provider for.
     */
    public ClassLoader getClassLoader() {
        if (classLoader == null) {
            try {
                classLoader = getProjectClassloader(javaProject);
                IWorkspace workspace = javaProject.getProject().getWorkspace();
                if (resourceChangeListener != null) {
                    workspace.removeResourceChangeListener(resourceChangeListener);
                }
                resourceChangeListener = new ChangeListener();
                javaProject.getProject().getWorkspace().addResourceChangeListener(resourceChangeListener,
                        IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_BUILD
                                | IResourceChangeEvent.PRE_DELETE);

            } catch (IOException e) {
                throw new CoreRuntimeException(new IpsStatus(e));
            } catch (CoreException ce) {
                throw new CoreRuntimeException(ce);
            }
        }
        return classLoader;
    }

    /**
     * Adds the listener as one to be informed about changes to the classpath contents. In this case
     * the listener should get a new classloader if he wants to use classes that are up-to-date .
     */
    public void addClasspathChangeListener(IClasspathContentsChangeListener listener) {
        classpathContentsChangeListeners.add(listener);
    }

    /**
     * Removes the listener from the list.
     */
    public void removeClasspathChangeListener(IClasspathContentsChangeListener listener) {
        classpathContentsChangeListeners.remove(listener);
    }

    /**
     * Notifies the listeners and forces that a new classloader is constructed upon the next
     * request.
     */
    private void classpathContentsChanged() {
        classLoader = null;
        for (IClasspathContentsChangeListener listener : classpathContentsChangeListeners) {
            listener.classpathContentsChanges(javaProject);
        }
    }

    /**
     * Returns a classloader containing the project's output location and all it's libraries (jars).
     */
    private URLClassLoader getProjectClassloader(IJavaProject project) throws IOException, CoreException {
        String[] classPathEntries = JavaRuntime.computeDefaultRuntimeClassPath(project);
        URL[] urls = new URL[classPathEntries.length];
        for (int i = 0; i < classPathEntries.length; i++) {
            IPath path = new Path(classPathEntries[i]);
            addClassfileContainer(path);
            urls[i] = path.toFile().toURI().toURL();
        }
        return new URLClassLoader(urls, parentClassLoader);
    }

    /**
     * @param containerLocation is the full path in the file system.
     */
    private void addClassfileContainer(IPath containerLocation) {
        IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        IPath containerPath = containerLocation.removeFirstSegments(workspaceLocation.segmentCount());
        classfileContainers.add(containerPath);
    }

    private class ChangeListener implements IResourceChangeListener {

        @Override
        public void resourceChanged(IResourceChangeEvent event) {
            if (event.getType() == IResourceChangeEvent.PRE_BUILD
                    && event.getBuildKind() == IncrementalProjectBuilder.CLEAN_BUILD) {
                return;
            }
            if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
                try {
                    // TODO JAVA8: remove this hack and just use URLClassLoader#close()
                    ClassLoaderCloser.close(classLoader);
                } catch (IOException e) {
                    IpsPlugin.log(e);
                }
                classfileContainers.clear();
                classpathContentsChanged();
            }
            for (IPath container : classfileContainers) {
                IResourceDelta delta = event.getDelta().findMember(container);
                if (delta != null) {
                    classpathContentsChanged();
                    break;
                }
            }
        }
    }

    private static class ClassLoaderCloser {

        /* adapted from http://planet.jboss.org/post/classloaders_keeping_jar_files_open */
        static void close(URLClassLoader classLoader) throws IOException {
            // on Java 7+, use the close()-Method
            boolean closedViaMethodCall = invokeMethod(URLClassLoader.class, "close", classLoader); //$NON-NLS-1$
            if (!closedViaMethodCall) {
                Object urlClassPath = getFieldValue(URLClassLoader.class, classLoader, "ucp"); //$NON-NLS-1$
                Collection<?> loaders = getFieldValue(urlClassPath, "loaders"); //$NON-NLS-1$
                if (loaders != null) {
                    List<IOException> errors = new LinkedList<IOException>();
                    for (Object jarLoader : loaders) {
                        JarFile jarFile = getFieldValue(jarLoader, "jar"); //$NON-NLS-1$
                        if (jarFile != null) {
                            try {
                                jarFile.close();
                            } catch (IOException e) {
                                errors.add(e);
                            }
                        }
                    }
                    if (!errors.isEmpty()) {
                        // Can't add other exceptions as suppressed in Java 6...
                        throw errors.get(0);
                    }
                }
            }
        }

        static boolean invokeMethod(Class<URLClassLoader> clazz, String methodName, URLClassLoader object) {
            Method method = null;
            try {
                method = clazz.getMethod(methodName);
            } catch (SecurityException e) {
                return false;
            } catch (NoSuchMethodException e) {
                return false;
            }
            if (method != null) {
                try {
                    method.invoke(object);
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                } catch (IllegalAccessException e) {
                    return false;
                } catch (InvocationTargetException e) {
                    return false;
                }
            }
            return false;
        }

        private static <V> V getFieldValue(Object o, String fieldName) {
            return getFieldValue(o.getClass(), o, fieldName);
        }

        @SuppressWarnings("unchecked")
        private static <V> V getFieldValue(Class<?> c, Object o, String fieldName) {
            if (o == null) {
                return null;
            }
            try {
                Field field = c.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (V)field.get(o);
            } catch (SecurityException e) {
                return null;
            } catch (NoSuchFieldException e) {
                return null;
            } catch (IllegalArgumentException e) {
                return null;
            } catch (IllegalAccessException e) {
                return null;
            }
        }
    }

}
