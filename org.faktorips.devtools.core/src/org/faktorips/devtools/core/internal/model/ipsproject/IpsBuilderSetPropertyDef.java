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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;

/**
 * The default implementation of the
 * <code>org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef</code> interface. If
 * no special class is defined in the plugin descriptor for the IpsArtefactBuilderSetPropertyDef
 * instances of this class are created
 * 
 * @author Peter Erzberger
 */
public class IpsBuilderSetPropertyDef implements IIpsBuilderSetPropertyDef {

    private String name;
    private String label;
    private String description;
    private String type;
    private String defaultValue;
    private String disableValue;
    private List<String> supportedJdkVersions;
    private List<String> discretePropertyValues;

    public IpsBuilderSetPropertyDef() {
    }

    /**
     * {@inheritDoc}
     */
    public String getDefaultValue(IIpsProject ipsProject) {
        return defaultValue;
    }

    /**
     * Returns the descripton specified in the plugin descriptor.
     */
    public String getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisableValue(IIpsProject ipsProject) {
        return disableValue;
    }

    /**
     * Returns the name specified in the plugin descriptor.
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the type specified in the plugin descriptor.
     */
    public String getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    public IStatus initialize(IIpsModel ipsModel, Map properties) {
        type = (String)properties.get("type"); //$NON-NLS-1$
        name = (String)properties.get("name"); //$NON-NLS-1$
        defaultValue = (String)properties.get("defaultValue"); //$NON-NLS-1$
        description = (String)properties.get("description"); //$NON-NLS-1$
        disableValue = (String)properties.get("disableValue"); //$NON-NLS-1$
        label = (String)properties.get("label"); //$NON-NLS-1$
        discretePropertyValues = (List)properties.get("discreteValues"); //$NON-NLS-1$
        supportedJdkVersions = (List)properties.get("jdkComplianceLevels"); //$NON-NLS-1$
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object parseValue(String value) {

        if (value == null) {
            return null;
        }
        if (type.equals("string")) { //$NON-NLS-1$
            return value;
        } else if (type.equals("boolean")) { //$NON-NLS-1$
            return Boolean.valueOf(value);
        } else if (type.equals("integer")) { //$NON-NLS-1$
            return Integer.valueOf(value);
        } else if (type.equals("enum") || type.equals("extensionPoint")) { //$NON-NLS-1$ //$NON-NLS-2$
            for (Iterator it = discretePropertyValues.iterator(); it.hasNext();) {
                String discreteValue = (String)it.next();
                if (discreteValue.equals(value)) {
                    return value;
                }
            }
        }
        throw new IllegalArgumentException("The provided value \"" + value //$NON-NLS-1$
                + "\" cannot be converted into an instance of the type " + type + " of this IpsBuilderSetPropertyDef."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAvailable(IIpsProject ipsProject) {
        String optionValue = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
        return supportedJdkVersions.isEmpty() || supportedJdkVersions.contains(optionValue);
    }

    /**
     * {@inheritDoc}
     */
    public Message validateValue(String value) {

        if (value == null) {
            return null;
        }
        boolean parsable = false;
        if (type.equals("string")) { //$NON-NLS-1$
            parsable = Datatype.STRING.isParsable(value);
        } else if (type.equals("boolean")) { //$NON-NLS-1$
            parsable = Datatype.BOOLEAN.isParsable(value);
        } else if (type.equals("integer")) { //$NON-NLS-1$
            parsable = Datatype.INTEGER.isParsable(value);
        } else if (type.equals("enum") || type.equals("extensionPoint")) { //$NON-NLS-1$ //$NON-NLS-2$
            for (Iterator it = discretePropertyValues.iterator(); it.hasNext();) {
                String discreteValue = (String)it.next();
                if (discreteValue.equals(value)) {
                    parsable = true;
                }
            }
        }

        if (!parsable) {
            return getStandardValidationMessage(value);
        }
        return null;
    }

    // TODO internationalize message
    protected Message getStandardValidationMessage(String value) {
        return new Message(MSGCODE_NON_PARSABLE_VALUE,
                "The value \"" + value + "\" is not supported for the type \"" + type //$NON-NLS-1$ //$NON-NLS-2$
                        + "\" of this property \"" + getName() + "\"", Message.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    public String[] getDiscreteValues() {
        return discretePropertyValues.toArray(new String[discretePropertyValues.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasDiscreteValues() {
        return discretePropertyValues.size() != 0;
    }

    private final static void retrieveEnumValues(String type,
            List discreteValues,
            IConfigurationElement element,
            ILog logger) {
        if (!StringUtils.isEmpty(type) && type.equals("enum") && element.getName().equals("discreteValues")) { //$NON-NLS-1$ //$NON-NLS-2$
            IConfigurationElement[] values = element.getChildren();
            for (int j = 0; j < values.length; j++) {
                String value = values[j].getAttribute("value"); //$NON-NLS-1$
                if (!StringUtils.isEmpty(value)) {
                    discreteValues.add(value);
                }
            }
        }
    }

    private final static boolean retrieveReferencedExtensionPoint(String type,
            List discreteValues,
            IExtensionRegistry registry,
            String builderSetId,
            Map properties,
            IConfigurationElement element,
            ILog logger) {
        if ("extensionPoint".equals(type)) { //$NON-NLS-1$

            String extensionPointId = element.getAttribute("extensionPointId"); //$NON-NLS-1$
            if (StringUtils.isEmpty(extensionPointId)) {
                logger
                        .log(new IpsStatus(
                                "If the type attribute of the builder set property " + element.getName() + " of the builder set " + //$NON-NLS-1$ //$NON-NLS-2$
                                        builderSetId
                                        + " has the value \"extensionPoint\" then the \"extensionPointId\" attribute has to have a value.")); //$NON-NLS-1$
                return false;
            }
            properties.put("extensionPointId", extensionPointId); //$NON-NLS-1$
            IExtensionPoint refExtPoint = registry.getExtensionPoint(extensionPointId);
            IExtension[] refExts = refExtPoint.getExtensions();
            for (int j = 0; j < refExts.length; j++) {
                discreteValues.add(refExts[j].getUniqueIdentifier());
            }
        }
        return true;
    }

    private final static void retrieveJdkComplianceLevels(List jdkComplianceLevelList, IConfigurationElement element) {
        if (element.getName().equals("jdkComplianceLevels")) { //$NON-NLS-1$
            IConfigurationElement[] values = element.getChildren();
            for (int j = 0; j < values.length; j++) {
                String level = values[j].getAttribute("value"); //$NON-NLS-1$
                if (!StringUtils.isEmpty(level)) {
                    jdkComplianceLevelList.add(level);
                }
            }
        }
    }

    private final static boolean retrieveProperties(ILog logger,
            IExtensionRegistry registry,
            String builderSetId,
            IConfigurationElement element,
            Map properties,
            List discreteValues) {

        String classValue = element.getAttribute("class"); //$NON-NLS-1$
        boolean classValueSpecified = !StringUtils.isEmpty(classValue);

        String name = element.getAttribute("name"); //$NON-NLS-1$
        if (!classValueSpecified && StringUtils.isEmpty(name)) {
            logger
                    .log(new IpsStatus(
                            "The required attribute \"name\" of the builder set property " + element.getName() + " of the builder set " + builderSetId + " is missing.")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return false;
        }
        properties.put("name", name); //$NON-NLS-1$
        String label = element.getAttribute("label"); //$NON-NLS-1$
        if (!classValueSpecified && StringUtils.isEmpty(label)) {
            logger
                    .log(new IpsStatus(
                            "The required attribute \"label\" of the builder set property " + element.getName() + " of the builder set " + builderSetId + " is missing.")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return false;
        }
        properties.put("label", label); //$NON-NLS-1$
        String type = element.getAttribute("type"); //$NON-NLS-1$
        if (!classValueSpecified && StringUtils.isEmpty(type)) {
            logger
                    .log(new IpsStatus(
                            "The required attribute \"type\" of the builder set property " + element.getName() + " of the builder set " + builderSetId + " is missing.")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return false;
        }
        properties.put("type", type); //$NON-NLS-1$

        String defaultValue = element.getAttribute("defaultValue"); //$NON-NLS-1$
        properties.put("defaultValue", defaultValue); //$NON-NLS-1$

        String disableValue = element.getAttribute("disableValue"); //$NON-NLS-1$
        properties.put("disableValue", disableValue); //$NON-NLS-1$

        String description = element.getAttribute("description"); //$NON-NLS-1$
        properties.put("description", description); //$NON-NLS-1$

        if (!retrieveReferencedExtensionPoint(type, discreteValues, registry, builderSetId, properties, element, logger)) {
            return false;
        }

        if (classValueSpecified) {
            try {
                properties.put("class", element.createExecutableExtension("class")); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (CoreException e) {
                logger.log(new IpsStatus(e));
            }
        }
        return true;
    }

    public final static IIpsBuilderSetPropertyDef loadExtensions(IConfigurationElement element,
            IExtensionRegistry registry,
            String builderSetId,
            ILog logger,
            IIpsModel ipsModel) {

        Map properties = new HashMap();
        ArrayList discreteValues = new ArrayList();
        ArrayList jdkComplianceLevelList = new ArrayList();

        if (!retrieveProperties(logger, registry, builderSetId, element, properties, discreteValues)) {
            return null;
        }

        String type = (String)properties.get("type"); //$NON-NLS-1$
        IConfigurationElement[] childs = element.getChildren();

        for (int j = 0; j < childs.length; j++) {
            retrieveEnumValues(type, discreteValues, childs[j], logger);
            retrieveJdkComplianceLevels(jdkComplianceLevelList, childs[j]);
        }
        if ("enum".equals(type) && discreteValues.isEmpty()) { //$NON-NLS-1$
            logger.log(new IpsStatus("If the type attribute of the builder set property " + element.getName() //$NON-NLS-1$
                    + " of the builder set " + builderSetId
                    + " has the value \"enum\" then discrete values have to be specified.")); //$NON-NLS-1$
        }
        properties.put("discreteValues", discreteValues); //$NON-NLS-1$
        properties.put("jdkComplianceLevels", jdkComplianceLevelList); //$NON-NLS-1$

        IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)properties.remove("class"); //$NON-NLS-1$
        if (propertyDef == null) {
            propertyDef = new IpsBuilderSetPropertyDef();
        }
        try {
            propertyDef.initialize(ipsModel, properties);
        } catch (Exception e) {
            logger.log(new IpsStatus(e));
            return null;
        }
        return propertyDef;
    }

}
