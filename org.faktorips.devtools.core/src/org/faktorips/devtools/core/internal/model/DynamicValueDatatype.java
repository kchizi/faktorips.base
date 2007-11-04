/*******************************************************************************
  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
  *
  * Alle Rechte vorbehalten.
  *
  * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
  * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
  * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
  * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
  *   http://www.faktorips.org/legal/cl-v01.html
  * eingesehen werden kann.
  *
  * Mitwirkende:
  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
  *
  *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.datatype.GenericValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.ClassLoaderProvider;
import org.faktorips.devtools.core.internal.model.ipsproject.IClasspathContentsChangeListener;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Element;

/**
 * A dynamic value datatype is a generic value datatype where the Java class
 * represented by the datatype is defined by it's qualified class name. The
 * class is resolved, when one of the datatype's method like is isParsable() is
 * called, which needs a static method of the underlying Java class. If the Java
 * class' source belongs to the IpsProject/JavaProject, than this class (the
 * bytecode) might exist for some time and than be unavailable or incomplete,
 * e.g. if the class has compile errors.
 * 
 * @author Jan Ortmann
 */
public class DynamicValueDatatype extends GenericValueDatatype {

	public final static DynamicValueDatatype createFromXml(
			IIpsProject ipsProject, Element element) {

		DynamicValueDatatype datatype = null;
		String isEnumTypeString = element.getAttribute("isEnumType"); //$NON-NLS-1$
		if (StringUtils.isEmpty(isEnumTypeString)
				|| !Boolean.valueOf(isEnumTypeString).booleanValue()) {
			datatype = new DynamicValueDatatype(ipsProject);
		} else {
			DynamicEnumDatatype enumDatatype = new DynamicEnumDatatype(
					ipsProject);
			enumDatatype.setAllValuesMethodName(element
					.getAttribute("getAllValuesMethod")); //$NON-NLS-1$
			enumDatatype.setGetNameMethodName(element
					.getAttribute("getNameMethod")); //$NON-NLS-1$
			String isSupporting = element.getAttribute("isSupportingNames"); //$NON-NLS-1$
			enumDatatype
					.setIsSupportingNames(StringUtils.isEmpty(isSupporting) ? false
							: Boolean.valueOf(isSupporting).booleanValue());
			datatype = enumDatatype;
		}

		datatype.setAdaptedClassName(element.getAttribute("valueClass")); //$NON-NLS-1$
		datatype.setQualifiedName(element.getAttribute("id")); //$NON-NLS-1$
		if (element.hasAttribute("valueOfMethod")) { //$NON-NLS-1$
			datatype
					.setValueOfMethodName(element.getAttribute("valueOfMethod")); //$NON-NLS-1$
		} else {
			datatype.setValueOfMethodName(null);
		}
		if (element.hasAttribute("isParsableMethod")) {//$NON-NLS-1$
			datatype.setIsParsableMethodName(element
					.getAttribute("isParsableMethod")); //$NON-NLS-1$
		} else {
			datatype.setIsParsableMethodName(null);
		}
		if (element.hasAttribute("valueToStringMethod")) {//$NON-NLS-1$
			datatype.setToStringMethodName(element
					.getAttribute("valueToStringMethod")); //$NON-NLS-1$
		} else {
			datatype.setToStringMethodName(null);
		}
		Element nullObjectEl = XmlUtil.getFirstElement(element, "NullObjectId"); //$NON-NLS-1$
		if (nullObjectEl==null) {
			datatype.setNullObjectDefined(false);
			datatype.setNullObjectId(null);
		} else {
			datatype.setNullObjectDefined(true);
			datatype.setNullObjectId(ValueToXmlHelper.getValueFromElement(element, "NullObjectId")); //$NON-NLS-1$
		}
		// datatype.getAdaptedClass();
		return datatype;
	}

	public void writeToXml(Element element) {
		if (getAdaptedClassName() != null) {
			element.setAttribute("valueClass", getAdaptedClassName()); //$NON-NLS-1$
		}
		if (getQualifiedName() != null) {
			element.setAttribute("id", getQualifiedName()); //$NON-NLS-1$
		}
		if (getValueOfMethodName() != null) {
			element.setAttribute("valueOfMethod", getValueOfMethodName()); //$NON-NLS-1$
		}
		if (getIsParsableMethodName() != null) {
			element.setAttribute("isParsableMethod", getIsParsableMethodName()); //$NON-NLS-1$
		}
		if (getToStringMethodName() != null) {
			element.setAttribute("valueToStringMethod", getToStringMethodName()); //$NON-NLS-1$
		}
		if (hasNullObject()) {
			ValueToXmlHelper.addValueToElement(getNullObjectId(), element, "NullObjectId"); //$NON-NLS-1$
		}			
	}

	private IIpsProject ipsProject;
	private ClassLoaderProvider classLoaderProvider = null;
	private IClasspathContentsChangeListener listener = null;
	
	private String className;
	private Class adaptedClass;

	public DynamicValueDatatype(IIpsProject ipsProject) {
		super();
		this.ipsProject = ipsProject;
	}

	public void setAdaptedClassName(String className) {
		this.className = className;
		clearCache();
	}

	public void setAdaptedClass(Class clazz) {
		this.adaptedClass = clazz;
	}

	public String getAdaptedClassName() {
		return className;
	}

	protected void clearCache() {
		super.clearCache();
		adaptedClass = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Class getAdaptedClass() {
		if (adaptedClass == null) {
			try {
				classLoaderProvider = ((IpsProject)ipsProject).getClassLoaderProviderForJavaProject();
				adaptedClass = classLoaderProvider.getClassLoader().loadClass(this.className);
				listener = new Listener();
				classLoaderProvider.addClasspathChangeListener(listener);
			} catch (Exception e) {
				IpsPlugin.log(e);
				// datatype remains invalid as long as the class can't be loaded.
			}
		}
		return adaptedClass;
	}

	
	class Listener implements IClasspathContentsChangeListener {

		/**
		 * {@inheritDoc}
		 */
		public void classpathContentsChanges(IJavaProject project) {
			clearCache();
            IpsProject ipsProject = (IpsProject)IpsPlugin.getDefault().getIpsModel().getIpsProject(project.getProject());
			ipsProject.getClassLoaderProviderForJavaProject().removeClasspathChangeListener(listener);
			listener = null;
		}
		
	}
}
