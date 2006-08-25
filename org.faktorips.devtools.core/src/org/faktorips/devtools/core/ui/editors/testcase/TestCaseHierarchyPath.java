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

package org.faktorips.devtools.core.ui.editors.testcase;

import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;

/**
 * Class to evalulate and navigate a hierarchy path for test case or test case types.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseHierarchyPath{
	// Seperator between each hierarchy element
	private static final String separator = "//"; //$NON-NLS-1$
	
	// Full path indicator
	private static final String WILDCARD = "***"; //$NON-NLS-1$
	
    // Offset separator
    private static final String OFFSET = "###"; //$NON-NLS-1$
    
	// Contains the complete hierarchy path
	private String hierarchyPath = ""; //$NON-NLS-1$
	
	private boolean isFullPath = true;
	
    private int offset = 0;
    
    /**
     * Removes the folder information from the beginning.
     */
	public static String unqualifiedName(String hierarchyPath){
        int index = hierarchyPath.lastIndexOf(separator); //$NON-NLS-1$
        if (index == -1) {
            return hierarchyPath;
        }
        return hierarchyPath.substring(index + separator.length());
	}
	
	public TestCaseHierarchyPath(String hierarchyPath){
		if (hierarchyPath.startsWith(WILDCARD)){
			setFullPath(false);
			hierarchyPath = hierarchyPath.substring(hierarchyPath.indexOf(WILDCARD) + WILDCARD.length());
		}
        if (hierarchyPath.indexOf(OFFSET) >= 0){
            offset = Integer.parseInt(hierarchyPath.substring(hierarchyPath.indexOf(OFFSET) + OFFSET.length()));
            hierarchyPath = hierarchyPath.substring(0, hierarchyPath.indexOf(OFFSET));
        }
        this.hierarchyPath = hierarchyPath;
	}
	
	/**
	 * Creates a test case hierarchy path for a given test policy component.
	 * 
	 * @param currTestPolicyCmpt The test policy compcomponentonengt for which the path will be created.
	 * @param evalForTestCase <code>true</code> if the hierarchy path will be evaluated for a test case
	 *                        <code>false</code> if the hierarchy path will be evaluated for a test case type.
	 */
	public TestCaseHierarchyPath(ITestPolicyCmpt currTestPolicyCmpt, boolean evalForTestCase){
		if (evalForTestCase){
			this.hierarchyPath = evalHierarchyPathForTestCase(currTestPolicyCmpt, ""); //$NON-NLS-1$
		}else{
			this.hierarchyPath = evalHierarchyPathForTestCaseType(currTestPolicyCmpt, ""); //$NON-NLS-1$
		}
	}
	
	/**
	 * Creates a test case hierarchy path for a given test policy component relation.
	 * 
	 * @param currTestPolicyCmpt The test policy component relation for which the path will be created.
	 * @param evalForTestCase <code>true</code> if the hierarchy path will be evaluated for a test case
	 *                        <code>false</code> if the hierarchy path will be evaluated for a test case type.
	 */	
	public TestCaseHierarchyPath(ITestPolicyCmptRelation relation, boolean evalforTestCase){
		String relationPath = relation.getTestPolicyCmptType();
		if (evalforTestCase){	
			this.hierarchyPath = evalHierarchyPathForTestCase((ITestPolicyCmpt) relation.getParent(), relationPath);
		}else{
			this.hierarchyPath = evalHierarchyPathForTestCaseType((ITestPolicyCmpt) relation.getParent(), relationPath);
		}
	}
	
	/**
	 * Returns the hierarchy path.
	 */
	public String getHierarchyPath() {
		return hierarchyPath;
	}

	/**
	 * Returns <code>true</code> if there is a next path element.
	 */
	public boolean hasNext(){
		return hierarchyPath.length() > 0;
	}
			
	/**
	 * Returns the current path element and sets the navigation pointer one element forward.
	 */
	public String next(){
		String next = ""; //$NON-NLS-1$
		if (!isFullPath){
			return hierarchyPath;
		}
		
		if (hierarchyPath.indexOf(separator)>=0){
			next = hierarchyPath.substring(0, hierarchyPath.indexOf(separator));
			hierarchyPath = hierarchyPath.substring(hierarchyPath.indexOf(separator) + separator.length());
			return next;
		}else{
			next = hierarchyPath;
			hierarchyPath = ""; //$NON-NLS-1$
		}
		return next;
	}
	
	/**
	 * Returns the string representation of this object.
	 */
	public String toString(){
		return (!isFullPath?WILDCARD:"") + hierarchyPath + (offset>0?OFFSET + offset:""); //$NON-NLS-1$
	}

	/**
	 * Returns the count of path elements.
	 */
	public int count() {
		int count = 0;
		TestCaseHierarchyPath tempHierarchyPath = new TestCaseHierarchyPath(hierarchyPath);
		if (!(hierarchyPath.length() > 0)){
			return 0;
		}
		while(tempHierarchyPath.hasNext()){
			tempHierarchyPath.next();
			count ++;
		}
		return count;
	}
	
	/**
	 * Sets if the full path is given.
	 */
	public void setFullPath(boolean fullPath) {
		isFullPath = fullPath;
	}
	
    /**
     * Sets the offset in case of equal elements.
     */
	public void setOffset(int offset) {
        this.offset = offset;
    }
    
	/**
	 * Returns the offset which could be used to identify the element in case of ambiguous elements.
	 */
    public int getOffset() {
        return offset;
    }

    /**
	 * Returns <code>true</code> if the full path is given otherwise return <code>false</code>.
	 */	
	public boolean isFullPath(){
		return isFullPath;
	}
	
    /**
     * Returns the folder name for a given hierarchy path.
     */	
	public static String getFolderName(String hierarchyPath){
        int index = hierarchyPath.lastIndexOf(separator); //$NON-NLS-1$
        if (index == -1){
            return ""; //$NON-NLS-1$
        }
        return hierarchyPath.substring(0, index);
	}
	
	private String evalHierarchyPathForTestCaseType(ITestPolicyCmpt currTestPolicyCmpt, String hierarchyPath){
		while (!currTestPolicyCmpt.isRoot()){
			if (hierarchyPath.length()>0)
				hierarchyPath = separator + hierarchyPath ;
			ITestPolicyCmptRelation testPcTypeRelation = (ITestPolicyCmptRelation) currTestPolicyCmpt.getParent();
			hierarchyPath = testPcTypeRelation.getTestPolicyCmptType() + hierarchyPath;
			currTestPolicyCmpt = (ITestPolicyCmpt) testPcTypeRelation.getParent();
		}
		hierarchyPath = currTestPolicyCmpt.getTestPolicyCmptTypeParameter() + (hierarchyPath.length() > 0 ? separator + hierarchyPath : ""); //$NON-NLS-1$
		return hierarchyPath;
	}
	
	private String evalHierarchyPathForTestCase(ITestPolicyCmpt currTestPolicyCmpt, String hierarchyPath){
		while (!currTestPolicyCmpt.isRoot()){
			if (hierarchyPath.length()>0)
				hierarchyPath = separator + hierarchyPath ;
			hierarchyPath = separator + currTestPolicyCmpt.getName() + hierarchyPath;
			ITestPolicyCmptRelation testPcTypeRelation = (ITestPolicyCmptRelation) currTestPolicyCmpt.getParent();
			hierarchyPath = testPcTypeRelation.getTestPolicyCmptType() + hierarchyPath;
			currTestPolicyCmpt = (ITestPolicyCmpt) testPcTypeRelation.getParent();
		}
		hierarchyPath = currTestPolicyCmpt.getName() + (hierarchyPath.length() > 0 ? separator + hierarchyPath : ""); //$NON-NLS-1$
		return hierarchyPath;
	}
}
