package org.faktorips.devtools.core.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.codegen.jmerge.JControlModel;
import org.eclipse.emf.codegen.jmerge.JMerger;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.util.XmlUtil;

/**
 * An implementation of <code>IpsArtefactBuilder</code> that generates a java
 * source file for a specific IpsObject. It provides support for formatting of
 * the java source file content and merging of the content with the content of
 * an already existing java file that has the same file name. A
 * JavaSourceFileBuilder needs a IJavaPackageStructure that provides the package
 * information about the java source file. A kindId has to be specified at
 * instantiation time. The kindId is used within the IjavaPackageStructure
 * implementation to identify the builder. To generate the actual content of the
 * java source file implementations must override the generate(IProgressMonitor)
 * method.
 * 
 * @author Peter Erzberger
 */
public abstract class JavaSourceFileBuilder implements IIpsArtefactBuilder {

	/**
	 * This constant is supposed to be used as a javadoc annotation. If the
	 * merging capabilities are activated a class, method or attribute that is
	 * marked by this annotation will be regenerated with every build.
	 */
	public final static String[] ANNOTATION_GENERATED = new String[] { "generated" };

	/**
	 * This constant is supposed to be used as a javadoc annotation. It becomes
	 * relevant if the merging capabilities are activated. It indicates that a
	 * piece of code was generated in the first place and will not be overridden
	 * by the generator by further builds.
	 */
	public final static String[] ANNOTATION_MODIFIABLE = new String[] { "modifiable" };

	private final static String JAVA_EXTENSION = ".java";

	private boolean mergeEnabled;

	private IJavaPackageStructure packageStructure;

	private String kindId;

	private IIpsObject ipsObject;

	private IIpsSrcFile ipsSrcFile;

	private LocalizedStringsSet localizedStringsSet;

	private boolean generationCanceled;

	private MultiStatus buildStatus;

	private static JControlModel model;

	/**
	 * Implementations of this class must override this method to provide the
	 * content of the java source file.
	 * 
	 * @param monitor
	 *            implementations can report the progress of the generation
	 *            process to this monitor
	 * @return the source file content
	 * @throws CoreException
	 *             implementations can wrap rising checked exceptions into a
	 *             CoreException. If an exception is thrown by this method the
	 *             current build of this builder is interrupted. Alternatively
	 *             the exception can be reported to the buildStatus to avoid
	 *             interrupting the build process of this builder.
	 * 
	 */
	protected abstract String generate() throws CoreException;

	/**
	 * Creates a new JavaSourceFileBuilder.
	 * 
	 * @param packageStructure
	 *            the package information for the generated java source file and
	 *            for other generated java classes within this package
	 *            structure. Cannot be null.
	 * @param kindId
	 *            cannot be null
	 * @param localizedStringsSet
	 *            provides locale specific texts. It can be null. If the
	 *            getLocalizedText() methods are called and the
	 *            localizedStringsSet is not set an exception is thrown
	 */
	public JavaSourceFileBuilder(IJavaPackageStructure packageStructure,
			String kindId, LocalizedStringsSet localizedStringsSet) {
		ArgumentCheck.notNull(packageStructure, this);
		ArgumentCheck.notNull(kindId, this);
		this.packageStructure = packageStructure;
		this.kindId = kindId;
		this.localizedStringsSet = localizedStringsSet;
	}

	/**
	 * Returns the IpsObject provided to this builder. It returns the IpsObject
	 * only during the generating phase otherwise null is returned.
	 */
	protected IIpsObject getIpsObject() {
		return ipsObject;
	}

	/**
	 * Returns the IpsSrcFile provided to this builder. It returns the
	 * IpsSrcFile only during the generating phase otherwise null is returned.
	 */
	protected IIpsSrcFile getIpsSrcFile() {
		return ipsSrcFile;
	}

	/**
	 * Convenience method that delegates the call to the package structure and
	 * returns the package name for the java class that is build by this
	 * builder.
	 * 
	 * @param the
	 *            package string
	 * @throws CoreException
	 *             is delegated from calls to other methods
	 */
	public String getPackage(IIpsSrcFile ipsSrcFile) throws CoreException {
		return getPackageStructure().getPackage(getKindId(), ipsSrcFile);
	}

	/**
	 * Calls getPackage(IpsObject). It is only allowed to call this method
	 * during the build cycle of this builder.
	 * 
	 * @return the package string
	 * @throws CoreException
	 *             is delegated from calls to other methods
	 */
	public String getPackage() throws CoreException {
		return getPackageStructure().getPackage(getKindId(), getIpsSrcFile());
	}

	/**
	 * Returns the qualified class name for the class definition contained in
	 * the java source file that is generated by this builder.
	 * 
	 * @param object
	 *            the IpsObject this builder is registered for
	 * @return the qualified class name
	 * @throws CoreException
	 *             is delegated from calls to other methods
	 */
	public String getQualifiedClassName(IIpsSrcFile ipsSrcFile)
			throws CoreException {
		StringBuffer buf = new StringBuffer();
		String packageName = getPackageStructure().getPackage(getKindId(),
				ipsSrcFile);
		if (packageName != null) {
			buf.append(packageName);
			buf.append('.');
		}
		buf.append(getUnqualifiedClassName(ipsSrcFile));
		return buf.toString();
	}

	/**
	 * Calls getQualifiedClassName(IpsObject). It is only allowed to call this
	 * method during the build cycle of this builder.
	 * 
	 * @return the qualified class name
	 * @throws CoreException
	 *             is delegated from calls to other methods
	 */
	public String getQualifiedClassName() throws CoreException {
		return getQualifiedClassName(getIpsSrcFile());
	}

	/**
	 * Returns the unqualified class name for the class definition contained in
	 * java source file that is generated by this builder.
	 * 
	 * @param object
	 *            the IpsObject this builder is registered for
	 * @return the qualified class name
	 * @throws CoreException
	 *             is delegated from calls to other methods
	 */
	public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile)
			throws CoreException {
		return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
	}

	/**
	 * Calls getUnqualifiedClassName(IpsObject). It is only allowed to call this
	 * method during the build cycle of this builder.
	 * 
	 * @return the unqualified class name
	 * @throws CoreException
	 *             is delegated from calls to other methods
	 */
	public String getUnqualifiedClassName() throws CoreException {
		return getUnqualifiedClassName(getIpsSrcFile());
	}

	/**
	 * This method has been overriden for convinence. Subclasses might need to
	 * implement this method to clean up the state of the builder that was
	 * created during the generation.
	 * 
	 * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#afterBuild(org.faktorips.devtools.core.model.IIpsSrcFile)
	 */
	public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
		ipsSrcFile = null;
		ipsObject = null;
		buildStatus = null;
		generationCanceled = false;
	}

	/**
	 * This method has been overriden for convinence. Subclasses might need to
	 * implement this method to set up a defined state before the generation
	 * starts.
	 * 
	 * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#beforeBuild(org.faktorips.devtools.core.model.IIpsSrcFile,
	 *      org.eclipse.core.runtime.MultiStatus)
	 */
	public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status)
			throws CoreException {
		this.ipsSrcFile = ipsSrcFile;
		this.buildStatus = status;
		ipsObject = ipsSrcFile.getIpsObject();
		generationCanceled = false;
	}

	/**
	 * Empty implementation of the super class method.
	 * 
	 * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#beforeFullBuild()
	 */
	public void beforeFullBuild() throws CoreException {
	}

	/**
	 * Empty implementation of the super class method.
	 * 
	 * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#afterFullBuild()
	 */
	public void afterFullBuild() throws CoreException {
	}

	/**
	 * Returns the java package structure available for this builder.
	 */
	public IJavaPackageStructure getPackageStructure() {
		return packageStructure;
	}

	/**
	 * Can be used by subclasses within the implementation of the generate()
	 * method to prevent this builder from creating a java source file.
	 */
	public final void cancelGeneration() {
		generationCanceled = true;
	}

	/**
	 * Sets if merging is enabled or not.
	 */
	public void setMergeEnabled(boolean enabled) {
		mergeEnabled = enabled;
	}

	/**
	 * Returns if merging is enabled or not.
	 */
	public boolean isMergeEnabled() {
		return mergeEnabled;
	}

	/**
	 * Returns the id that identifies which kind of java classes this builder
	 * creates.
	 */
	public String getKindId() {
		return kindId;
	}

	/**
	 * Logs a CoreException to the build status of this builder. This method can
	 * only be called during the build cycle.
	 */
	protected void addToBuildStatus(CoreException e) {
		buildStatus.add(new IpsStatus(e));
	}

	/**
	 * Logs the provided IStatus to the build status of this builder. This
	 * method can only be called during the build cycle.
	 */
	protected void addToBuildStatus(IStatus status) {
		buildStatus.add(status);
	}

	/**
	 * Returns the localized text for the provided key. Calling this method is
	 * only allowed during the build cycle. If it is called outside the build
	 * cycle a RuntimeException is thrown. In addition if no LocalizedStringSet
	 * has been set to this builder a RuntimeException is thrown.
	 * 
	 * @param key
	 *            the key that identifies the requested text
	 * @return the requested text
	 */
	public String getLocalizedText(String key) {

		if (localizedStringsSet == null) {
			throw new RuntimeException(
					"A LocalizedStringSet has to be set to this builder to be able to call this method.");
		}
		return getLocalizedStringSet().getString(
				key,
				getIpsObject().getIpsProject()
						.getGeneratedJavaSourcecodeDocumentationLanguage());
	}

	/**
	 * Returns the localized text for the provided key. Calling this method is
	 * only allowed during the build cycle. If it is called outside the build
	 * cycle a RuntimeException is thrown. In addition if no LocalizedStringSet
	 * has been set to this builder a RuntimeException is thrown.
	 * 
	 * @param key
	 *            the key that identifies the requested text
	 * @param replacement
	 *            an indicated region within the text is replaced by the string
	 *            representation of this value
	 * @return the requested text
	 */
	public String getLocalizedText(String key, Object replacement) {
		if (localizedStringsSet == null) {
			throw new RuntimeException(
					"A LocalizedStringSet has to be set to this builder to be able to call this method.");
		}
		return getLocalizedStringSet().getString(
				key,
				getIpsObject().getIpsProject()
						.getGeneratedJavaSourcecodeDocumentationLanguage(),
				replacement);
	}

	/**
	 * Implementation of the build procedure of this builder.
	 * 
	 * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#build(org.faktorips.devtools.core.model.IIpsSrcFile)
	 */
	public void build(IIpsSrcFile ipsSrcFile) throws CoreException {

		if (!isBuilderFor(ipsSrcFile)) {
			return;
		}

		IFile javaFile = getJavaFile(ipsSrcFile);
		String content = generate();
		if (content == null || generationCanceled) {
			return;
		}
		String formattedContent = format(content);
		boolean newFileCreated = createFileIfNotThere(javaFile);

		if (!newFileCreated) {

			String charset = ipsSrcFile.getIpsProject().getProject()
					.getDefaultCharset();
			if (isMergeEnabled()) {
				InputStream javaFileContents = null;
				InputStream newContents = null;
				try {
					javaFileContents = javaFile.getContents();
					newContents = transform(ipsSrcFile, formattedContent);
					merge(javaFile, javaFileContents, newContents, charset);
					return;
				} finally {
					closeStream(javaFileContents);
					closeStream(newContents);
				}
			}

			// if merging is not activated and the content of the file is
			// identical compared to the generated and formatted
			// content then the new content is not written to the file
			try {
				if (formattedContent.equals(StringUtil.readFromInputStream(
						javaFile.getContents(), charset))) {
					return;
				}
			} catch (IOException e) {
				throw new CoreException(new IpsStatus(
						"An exception occured while trying to read the content of the file: "
								+ javaFile.getName(), e));
			}
		}

		javaFile.setContents(transform(ipsSrcFile, formattedContent), true,
				false, null);
	}

	private void closeStream(InputStream is) {

		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * Overridden IMethod.
	 * 
	 * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#delete(org.faktorips.devtools.core.model.IIpsSrcFile)
	 */
	public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
		IFile file = getJavaFile(ipsSrcFile);
		IContainer parent = file.getParent();
		IFolder destination = ipsSrcFile.getIpsPackageFragment().getRoot()
				.getArtefactDestination();
		if (file.exists()) {
			file.delete(true, null);
			if (!parent.equals(destination) && parent instanceof IFolder) {
				IFolder parentFolder = (IFolder) parent;
				if (parentFolder.members().length == 0) {
					parentFolder.delete(true, null);
				}
			}
		}
	}

	/**
	 * Returns the localized string set of this builder.
	 */
	private LocalizedStringsSet getLocalizedStringSet() {
		return localizedStringsSet;
	}

	private String format(String content) {

		if (content == null) {
			return content;
		}
		// with parameter null the CodeFormatter is configured with the
		// preferences that are
		// currently set
		CodeFormatter formatter = ToolFactory.createCodeFormatter(null);
		TextEdit edit = formatter.format(CodeFormatter.K_COMPILATION_UNIT,
				content, 0, content.length(), 0, StringUtil
						.getSystemLineSeparator());

		if (edit == null) {
			return content;
		}
		Document doc = new Document(content);
		try {
			edit.apply(doc);
		} catch (MalformedTreeException e) {
			throw new RuntimeException(e);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
		return doc.get();
	}

	// TODO check for qualified class name validity
	public IFile getJavaFile(IIpsSrcFile ipsSrcFile) throws CoreException {
		IFolder destinationFolder = ipsSrcFile.getIpsPackageFragment()
				.getRoot().getArtefactDestination();

		String name = getQualifiedClassName(ipsSrcFile);
		int index = name.lastIndexOf('.');

		if (index == name.length()) {
			throw new RuntimeException(
					"The qualified class name is not a valid java class name");
		}
		if (index == -1) {
			return destinationFolder.getFile(name + JAVA_EXTENSION);
		}
		String packageName = name.substring(0, index);
		String fileName = name.substring(index + 1, name.length());
		String[] packageFolders = packageName.split("\\.");
		IFolder folder = destinationFolder;
		for (int i = 0; i < packageFolders.length; i++) {
			folder = folder.getFolder(packageFolders[i]);
		}
		return folder.getFile(fileName + JAVA_EXTENSION);
	}

	private JControlModel getJControlModel() {

		if (model != null) {
			return model;
		}
		InputStream is = null;
		try {
			StringBuffer mergeFile = new StringBuffer();
			mergeFile.append('/').append(
					JavaSourceFileBuilder.class.getPackage().getName().replace(
							'.', '/')).append("/merge.xml");
			is = (InputStream) Platform.getBundle(IpsPlugin.PLUGIN_ID)
					.getResource(mergeFile.toString()).getContent();
			org.w3c.dom.Document doc = XmlUtil.getDocument(is);
			model = new JControlModel(doc.getDocumentElement());
			return model;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			closeStream(is);
		}
	}

	private void merge(IFile javaFile, InputStream oldContent,
			InputStream newContent, String charset) throws CoreException {

		JMerger merger = new JMerger();
		merger.setControlModel(getJControlModel());
		merger.setSourceCompilationUnit(merger
				.createCompilationUnitForInputStream(newContent));
		merger.setTargetCompilationUnit(merger
				.createCompilationUnitForInputStream(oldContent));
		String targetContentsBeforeMerge = merger
				.getTargetCompilationUnitContents();
		merger.merge();
		try {
			String targetContents = merger.getTargetCompilationUnitContents();

			if (targetContents == null
					|| targetContents.equals(targetContentsBeforeMerge)) {
				return;
			}
			javaFile.setContents(new ByteArrayInputStream(targetContents
					.getBytes(charset)), true, false, null);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private ByteArrayInputStream transform(IIpsSrcFile ipsSrcFile,
			String content) throws CoreException {
		String charset = ipsSrcFile.getIpsProject().getProject()
				.getDefaultCharset();
		try {
			return new ByteArrayInputStream(content.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					"org.faktorips.std.builder", IStatus.OK,
					"The charset is not supported: " + charset, e));
		}
	}

	private boolean createFileIfNotThere(IFile file) throws CoreException {

		if (!file.exists()) {
			IContainer parent = file.getParent();

			if (parent instanceof IFolder) {
				createFolder((IFolder) parent);
			}
			file.create(new ByteArrayInputStream("".getBytes()), true, null);
			return true;
		}

		return false;
	}

	private void createFolder(IFolder folder) throws CoreException {

		if (folder == null) {
			return;
		}
		if (!folder.exists()) {
			IContainer parent = folder.getParent();
			if (parent instanceof IFolder) {
				createFolder((IFolder) parent);
			}
			folder.create(true, true, null);
		}
	}
}
