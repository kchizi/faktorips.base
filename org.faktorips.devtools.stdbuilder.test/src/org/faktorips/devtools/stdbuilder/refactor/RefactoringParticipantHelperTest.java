/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.refactor.IpsRefactoringProcessor;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RefactoringParticipantHelperTest extends RefactoringParticipantTest {

    @Mock
    private IIpsObjectPartContainer mockIpsObjectPartContainer;

    @Mock
    private IIpsProject mockIpsProject;

    @Mock
    private StandardBuilderSet mockStandardBuilderSet;

    @Mock
    private IpsRefactoringProcessor ipsRefactoringProcessor;

    private TestParticipantHelper spyRefactoringHelper;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        when(ipsRefactoringProcessor.refactorIpsModel(any(IProgressMonitor.class))).thenReturn(
                new IpsRefactoringModificationSet(null));
        TestParticipantHelper refactoringHelper = new TestParticipantHelper();
        spyRefactoringHelper = spy(refactoringHelper);
    }

    @Test
    public void testInitializeNonIpsElement() {
        assertFalse(spyRefactoringHelper.initialize(ipsRefactoringProcessor, new Object()));
    }

    @Test
    public void testInitializeIpsObjectPartContainer() throws CoreException {
        when(mockIpsObjectPartContainer.getIpsProject()).thenReturn(mockIpsProject);
        when(mockIpsProject.getIpsArtefactBuilderSet()).thenReturn(mockStandardBuilderSet);
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(mockIpsObjectPartContainer);
        IIpsObjectPartContainer targetIpsObjectPartContainer = mock(IIpsObjectPartContainer.class);
        modificationSet.setTargetElement(targetIpsObjectPartContainer);
        when(ipsRefactoringProcessor.refactorIpsModel(any(IProgressMonitor.class))).thenReturn(modificationSet);

        assertTrue(spyRefactoringHelper.initialize(ipsRefactoringProcessor, mockIpsObjectPartContainer));

        verify(spyRefactoringHelper).initializeJavaElements(targetIpsObjectPartContainer, mockStandardBuilderSet);
    }

    @Test
    public void testInitializeNoStandardBuilderSetGiven() {
        when(mockIpsObjectPartContainer.getIpsProject()).thenReturn(mockIpsProject);
        IIpsArtefactBuilderSet mockBuilderSet = mock(IIpsArtefactBuilderSet.class);
        when(mockIpsProject.getIpsArtefactBuilderSet()).thenReturn(mockBuilderSet);

        assertFalse(spyRefactoringHelper.initialize(ipsRefactoringProcessor, mockIpsObjectPartContainer));
    }

    public static class TestParticipantHelper extends RefactoringParticipantHelper {

        public TestParticipantHelper() {
            super();
        }

        @Override
        protected JavaRefactoring createJavaRefactoring(IJavaElement generatedJavaElement,
                IJavaElement targetJavaElement,
                RefactoringStatus status,
                IProgressMonitor progressMonitor) {

            return null;
        }

        @Override
        protected IJavaElement getTargetJavaElementForOriginalJavaElement(IJavaElement originalJavaElement) {
            return null;
        }

    }

}
