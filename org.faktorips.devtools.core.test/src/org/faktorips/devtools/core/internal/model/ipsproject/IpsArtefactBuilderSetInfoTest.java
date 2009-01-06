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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.TestConfigurationElement;
import org.faktorips.devtools.core.TestExtension;
import org.faktorips.devtools.core.TestExtensionPoint;
import org.faktorips.devtools.core.TestExtensionRegistry;
import org.faktorips.devtools.core.TestLogger;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;

public class IpsArtefactBuilderSetInfoTest extends TestCase {

    private TestExtensionRegistry registry;
    private TestLogger logger;
    private IpsArtefactBuilderSetInfo builderSetInfo;
    
    public void setUp(){
        
        HashMap attributes = new HashMap();
        attributes.put("name", "loggingConntectors");
        attributes.put("type", "boolean");
        attributes.put("label", "Logging Connectors");
        attributes.put("defaultValue", "true");
        attributes.put("disableValue", "false");
        attributes.put("description", "Hello");
        TestConfigurationElement propertyDef1 = new TestConfigurationElement("builderSetPropertyDef", attributes, null,
                new IConfigurationElement[] {});

        attributes = new HashMap();
        attributes.put("name", "useChangeListener");
        attributes.put("type", "boolean");
        attributes.put("label", "Use Change Listener");
        attributes.put("defaultValue", "true");
        attributes.put("disableValue", "false");
        attributes.put("description", "Hello");
        TestConfigurationElement propertyDef2 = new TestConfigurationElement("builderSetPropertyDef", attributes, null,
                new IConfigurationElement[] {});
        
        attributes = new HashMap();
        attributes.put("name", "coypSupport");
        attributes.put("type", "boolean");
        attributes.put("label", "Copy Support");
        attributes.put("defaultValue", "true");
        attributes.put("disableValue", "false");
        attributes.put("description", "Hello");
        TestConfigurationElement propertyDef3 = new TestConfigurationElement("builderSetPropertyDef", attributes, null,
                new IConfigurationElement[] {});

        attributes = new HashMap();
        attributes.put("class", DefaultBuilderSet.class.getName());
        TestConfigurationElement element = new TestConfigurationElement("builderSet", attributes, null,
                new IConfigurationElement[] {propertyDef1, propertyDef2, propertyDef3});
        
        TestExtension extension = new TestExtension(new IConfigurationElement[] { element }, "mybuilderset");
        TestExtensionPoint extensionPoint = new TestExtensionPoint(new IExtension[] { extension }, IpsPlugin.PLUGIN_ID, "artefactbuilderset");
        registry = new TestExtensionRegistry(new IExtensionPoint[] { extensionPoint});
        
        logger = new TestLogger();
        ArrayList builderSetInfoList = new ArrayList();
        IIpsModel ipsModel = createTestIpsModel();
        
        IpsArtefactBuilderSetInfo.loadExtensions(registry, logger, builderSetInfoList, ipsModel);
        builderSetInfo = (IpsArtefactBuilderSetInfo)builderSetInfoList.get(0);

    }
    
    public void testLoadExtensions(){
        assertEquals(3, builderSetInfo.getPropertyDefinitions().length);
        assertEquals("mybuilderset", builderSetInfo.getBuilderSetId());
        
    }

    public void testValidateIpsBuilderSetPropertyValue(){
        IIpsProject ipsProject = createTestIpsProject("1.4");
        assertNull(builderSetInfo.validateIpsBuilderSetPropertyValue(ipsProject, "loggingConntectors", "true"));
        assertNotNull(builderSetInfo.validateIpsBuilderSetPropertyValue(ipsProject, "loggingConntectors", "hallo"));
        assertNotNull(builderSetInfo.validateIpsBuilderSetPropertyValue(ipsProject, "hanswurst", "hallo"));
        
    }
    
    public void testValidateIpsArtefactBuilderSetConfig(){
        IIpsProject ipsProject = createTestIpsProject("1.4");
        HashMap properties = new HashMap();
        properties.put("loggingConntectors", "true");
        properties.put("useChangeListener", "false");
        IpsArtefactBuilderSetConfigModel builderSetConfig = new IpsArtefactBuilderSetConfigModel(properties);
        
        MessageList msgList = builderSetInfo.validateIpsArtefactBuilderSetConfig(ipsProject, builderSetConfig);
        assertTrue(msgList.isEmpty());

        properties = new HashMap();
        properties.put("loggingConntectors", "true");
        properties.put("useChangeListener", "false");
        properties.put("hanswurst", "false");
        builderSetConfig = new IpsArtefactBuilderSetConfigModel(properties);
        
        msgList = builderSetInfo.validateIpsArtefactBuilderSetConfig(ipsProject, builderSetConfig);
        assertFalse(msgList.isEmpty());
        
    }
    
    
    private IIpsModel createTestIpsModel(){
        
        InvocationHandler ipsModelHandler = new InvocationHandler(){
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(method.getName().equals("getIpsBuilderSetPropertyDef")){
                    if(args.length < 1){
                        return null;
                    }
                    HashMap properties = new HashMap();
                    properties.put("type", "boolean");
                    properties.put("defaultValue", "false");
                    properties.put("disableValue", "false");

                    if(((String)args[0]).equals("loggingConntectors")){
                        properties.put("name", "loggingConntectors");
                    }
                    if(((String)args[0]).equals("useChangeListener")){
                        properties.put("name", "useChangeListener");
                    }
                    if(((String)args[0]).equals("coypSupport")){
                        properties.put("name", "coypSupport");
                    }
                    IpsBuilderSetPropertyDef propertyDef = new IpsBuilderSetPropertyDef();
                    propertyDef.initialize(null, properties);
                    return propertyDef;
                }
                return null;
            }
        };
        return (IIpsModel) Proxy.newProxyInstance(IIpsModel.class.getClassLoader(), new Class[] { IIpsModel.class }, ipsModelHandler);
    }
    
    private IIpsProject createTestIpsProject(final String complianceLevel){

        InvocationHandler javaProjectHandler = new InvocationHandler(){
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(method.getName().equals("getOption") && args.length > 0 && ((String)args[0]).equals(JavaCore.COMPILER_COMPLIANCE)){
                    return complianceLevel;
                }
                return null;
            }
        };
        final IJavaProject javaProject = (IJavaProject) Proxy.newProxyInstance(IJavaProject.class.getClassLoader(), new Class[] { IJavaProject.class }, javaProjectHandler);
        
        InvocationHandler ipsProjectHandler = new InvocationHandler(){
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(method.getName().equals("getJavaProject")){
                    return javaProject;
                }
                return null;
            }
        };
        return (IIpsProject) Proxy.newProxyInstance(IIpsProject.class.getClassLoader(), new Class[] { IIpsProject.class }, ipsProjectHandler);
    }

}
