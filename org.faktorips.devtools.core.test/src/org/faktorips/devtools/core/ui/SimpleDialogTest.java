/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ITestAnswerProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.editors.productcmpt.GenerationSelectionDialog;

/**
 * Testcase to test some simple GUI-functions like opening an editor.
 * 
 * @author Thorsten Guenther
 */
public class SimpleDialogTest extends AbstractIpsPluginTest implements ILogListener, ITestAnswerProvider {

	private IpsPlugin plugin;
	private int answer = GenerationSelectionDialog.CHOICE_BROWSE;
	
	public void setUp() throws Exception {
        super.setUp();
		plugin = IpsPlugin.getDefault();
		plugin.getLog().addLogListener(this);
		plugin.setTestMode(true);
		plugin.setTestAnswerProvider(this);
	}
    
    /**
     * {@inheritDoc}
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        plugin.getLog().removeLogListener(this);
    }

	public void testOpenProductCmptEditor() throws Exception {
        IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(new GregorianCalendar(2003, 7, 1));

        IIpsProject ipsProject = newIpsProject();
        IProductCmptType type = newProductCmptType(ipsProject, "Type");
        IProductCmpt product1 = newProductCmpt(type, "Product1");
        openEditor(product1);
        
        IProductCmpt product2 = newProductCmpt(type, "Product2");
        openEditor(product1);
		openEditor(product2);
	}

	private void openEditor(IIpsObject file) throws Exception {
    	IpsPlugin.getDefault().openEditor((IFile) file.getCorrespondingResource());
		plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void logging(IStatus status, String plugin) {
		// never ever should a logentry appear...
		fail();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBooleanAnswer() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getStringAnswer() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAnswer() {
		return null;
	}

    public int getIntAnswer() {
        return answer;
    }
}
