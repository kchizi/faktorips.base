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

package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.core.IFunctionResolverFactory;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.fl.AbstractProjectRelatedFunctionResolverFactory;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.fl.FunctionResolver;

/**
 * Cache for function resolvers that are added to expression compilers (formula language) for a
 * specific IPS project by default. Loads Function resolvers via the extension point
 * <code>org.faktorips.fl.FunctionResolver</code>. Reuses them as long as the IPS model does not
 * change. This behavior is sufficient for now but may be refined in the future.
 * 
 * This class is not thread safe.
 * 
 */
public class ExtensionFunctionResolversCache {

    private final IIpsProject ipsProject;

    private List<FunctionResolver> cachedFunctionResolvers;

    private List<IFunctionResolverFactory> resolverFactories;

    /**
     * @param ipsProject the project this class caches function resolvers for.
     */
    public ExtensionFunctionResolversCache(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        resolverFactories = Arrays.asList(IpsPlugin.getDefault().getFlFunctionResolverFactories());
        registerListenerWithIpsModel();
    }

    /**
     * As of now only used for tests.
     * 
     * @param ipsProject the project this class caches function resolvers for.
     * @param resolverFactories factories creating resolvers. Used instead of the factories provided
     *            by the extension point <code>org.faktorips.fl.FunctionResolver</code>.
     */
    public ExtensionFunctionResolversCache(IIpsProject ipsProject, List<IFunctionResolverFactory> resolverFactories) {
        this.ipsProject = ipsProject;
        this.resolverFactories = resolverFactories;
        registerListenerWithIpsModel();
    }

    private void registerListenerWithIpsModel() {
        getIpsProject().getIpsModel().addChangeListener(new ContentsChangeListener() {
            @Override
            public void contentsChanged(ContentChangeEvent event) {
                clearCache();
            }
        });
    }

    /**
     * Creates a new expression compiler or returns the cached one if it was created before.
     */
    public void addExtensionFunctionResolversToCompiler(ExtendedExprCompiler compiler) {
        List<FunctionResolver> resolvers = createFunctionResolversIfNeccessary();
        addFunctionResolversTo(resolvers, compiler);
    }

    protected List<FunctionResolver> createFunctionResolversIfNeccessary() {
        if (cachedFunctionResolvers == null) {
            cachedFunctionResolvers = createExtendingFunctionResolvers();
        }
        return cachedFunctionResolvers;
    }

    protected List<FunctionResolver> createExtendingFunctionResolvers() {
        ArrayList<FunctionResolver> resolvers = new ArrayList<FunctionResolver>();
        for (IFunctionResolverFactory factory : resolverFactories) {
            if (isActive(factory)) {
                FunctionResolver resolver = createFuntionResolver(factory);
                addIfNotNull(resolvers, resolver);
            }
        }
        return resolvers;
    }

    private void addIfNotNull(ArrayList<FunctionResolver> resolverList, FunctionResolver resolver) {
        if (resolver != null) {
            resolverList.add(resolver);
        }
    }

    private FunctionResolver createFuntionResolver(IFunctionResolverFactory factory) {
        try {
            FunctionResolver resolver = createFunctionResolver(factory);
            return resolver;
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            IpsPlugin.log(new IpsStatus("Unable to create the function resolver for the following factory: " //$NON-NLS-1$
                    + factory.getClass(), e));
        }
        return null;
    }

    private boolean isActive(IFunctionResolverFactory factory) {
        return getIpsProject().getReadOnlyProperties().isActive(factory);
    }

    private FunctionResolver createFunctionResolver(IFunctionResolverFactory factory) {
        Locale formulaLanguageLocale = getIpsProject().getFormulaLanguageLocale();
        if (factory instanceof AbstractProjectRelatedFunctionResolverFactory) {
            return ((AbstractProjectRelatedFunctionResolverFactory)factory).newFunctionResolver(getIpsProject(),
                    formulaLanguageLocale);
        } else {
            return factory.newFunctionResolver(formulaLanguageLocale);
        }
    }

    private void addFunctionResolversTo(List<FunctionResolver> resolvers, ExtendedExprCompiler compiler) {
        for (FunctionResolver resolver : resolvers) {
            compiler.add(resolver);
        }
    }

    private IIpsProject getIpsProject() {
        return ipsProject;
    }

    protected void clearCache() {
        cachedFunctionResolvers = null;
    }
}