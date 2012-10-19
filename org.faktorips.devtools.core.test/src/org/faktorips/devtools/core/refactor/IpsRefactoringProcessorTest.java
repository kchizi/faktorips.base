/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IpsRefactoringProcessorTest {

    @Mock
    private IIpsElement ipsElement;

    @Mock
    private IProgressMonitor progressMonitor;

    @Mock
    private CheckConditionsContext checkConditionsContext;

    private TestProcessor testProcessorSpy;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(ipsElement.exists()).thenReturn(true);

        TestProcessor testProcessor = new TestProcessor(ipsElement);
        testProcessorSpy = spy(testProcessor);
    }

    @Test
    public void shouldReturnOkStatusOnCheckInitialConditionsIfAllConditionsAreFulfilled()
            throws OperationCanceledException, CoreException {

        RefactoringStatus status = testProcessorSpy.checkInitialConditions(progressMonitor);

        assertFalse(status.hasError());
    }

    @Test
    public void shouldReturnFatalErrorStatusOnCheckInitialConditionsIfTheIpsElementToBeRefactoredDoesNotExist()
            throws OperationCanceledException, CoreException {

        when(ipsElement.exists()).thenReturn(false);

        RefactoringStatus status = testProcessorSpy.checkInitialConditions(progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void shouldCallSubclassImplementationOfCheckInitialConditionsIfChecksWereSuccessfulThusFar()
            throws OperationCanceledException, CoreException {

        testProcessorSpy.checkInitialConditions(progressMonitor);

        verify(testProcessorSpy).checkInitialConditionsThis(any(RefactoringStatus.class), eq(progressMonitor));
    }

    @Test
    public void shouldNotCallSubclassImplementationOfCheckInitialConditionsIfChecksWereNotSuccessfulThusFar()
            throws OperationCanceledException, CoreException {

        when(ipsElement.exists()).thenReturn(false);

        testProcessorSpy.checkInitialConditions(progressMonitor);

        verify(testProcessorSpy, never()).checkInitialConditionsThis(any(RefactoringStatus.class), eq(progressMonitor));
    }

    @Test
    public void shouldReturnOkStatusOnCheckFinalConditionsIfAllConditionsAreFulfilled()
            throws OperationCanceledException, CoreException {

        RefactoringStatus status = testProcessorSpy.checkFinalConditions(progressMonitor, checkConditionsContext);

        assertFalse(status.hasError());
    }

    @Test
    public void shouldReturnFatalErrorStatusOnCheckFinalConditionsIfAnAffectedIpsSrcFileIsOutOfSync()
            throws OperationCanceledException, CoreException {

        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class, RETURNS_DEEP_STUBS);
        when(ipsSrcFile.getCorrespondingResource().isSynchronized(anyInt())).thenReturn(false);
        Set<IIpsSrcFile> affectedFiles = new HashSet<IIpsSrcFile>();
        affectedFiles.add(ipsSrcFile);
        when(testProcessorSpy.getAffectedIpsSrcFiles()).thenReturn(affectedFiles);

        RefactoringStatus status = testProcessorSpy.checkFinalConditions(progressMonitor, checkConditionsContext);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void shouldValidateUserInputOnCheckFinalConditions() throws OperationCanceledException, CoreException {
        testProcessorSpy.checkFinalConditions(progressMonitor, checkConditionsContext);
        assertTrue(testProcessorSpy.validateUserInputThisCalled);
    }

    @Test
    public void shouldCallSubclassImplementationOfCheckFinalConditionsIfChecksWereSuccessfulThusFar()
            throws CoreException {

        testProcessorSpy.checkFinalConditions(progressMonitor, checkConditionsContext);
        verify(testProcessorSpy).checkFinalConditionsThis(any(RefactoringStatus.class), eq(progressMonitor),
                eq(checkConditionsContext));
    }

    @Test
    public void shouldNotCallSubclassImplementationOfCheckFinalConditionsIfChecksWereNotSuccessfulThusFar()
            throws CoreException {

        testProcessorSpy.invalid = true;

        testProcessorSpy.checkFinalConditions(progressMonitor, checkConditionsContext);

        verify(testProcessorSpy, never()).checkFinalConditionsThis(any(RefactoringStatus.class), eq(progressMonitor),
                eq(checkConditionsContext));
    }

    @Test
    public void shouldAlwaysBeApplicable() throws CoreException {
        assertTrue(testProcessorSpy.isApplicable());
    }

    // Public so it can be accessed by Mockito
    public static class TestProcessor extends IpsRefactoringProcessor {

        private boolean invalid;

        private boolean validateUserInputThisCalled;

        protected TestProcessor(IIpsElement ipsElement) {
            super(ipsElement);
        }

        @Override
        protected void validateIpsModel(MessageList validationMessageList) throws CoreException {

        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
            if (invalid) {
                status.addFatalError("foo");
            }
            validateUserInputThisCalled = true;
        }

        @Override
        protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
            return new HashSet<IIpsSrcFile>();

        }

        @Override
        public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) throws CoreException {
            IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(null);
            addAffectedSrcFiles(modificationSet);
            return modificationSet;
        }

        @Override
        public String getIdentifier() {
            return null;
        }

        @Override
        public String getProcessorName() {
            return null;
        }

        @Override
        public RefactoringParticipant[] loadParticipants(RefactoringStatus status,
                SharableParticipants sharedParticipants) throws CoreException {

            return null;
        }

        @Override
        public boolean isSourceFilesSavedRequired() {
            return false;
        }

    }

    @Test
    public void testGetIpsElement_ipsObjectPart() throws Exception {
        IIpsObjectPart ipsObjectPart = mock(IIpsObjectPart.class);
        IIpsObject ipsObject = mock(IIpsObject.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);

        IpsRefactoringProcessor refactoringProcessor = new TestProcessor(ipsObjectPart);

        when(ipsObjectPart.isDeleted()).thenReturn(false);
        assertSame(ipsObjectPart, refactoringProcessor.getIpsElement());

        IIpsObjectPart ipsObjectPart2 = mock(IIpsObjectPart.class);

        when(ipsObjectPart.getName()).thenReturn("ipsObjectPart");
        when(ipsObjectPart2.getName()).thenReturn("ipsObjectPart");
        when(ipsObject.getName()).thenReturn("ipsObject");
        when(ipsSrcFile.getName()).thenReturn("ipsSrcFile");

        when(ipsObjectPart.isDeleted()).thenReturn(true);
        when(ipsObjectPart.getParent()).thenReturn(ipsObject);
        when(ipsObject.getParent()).thenReturn(ipsSrcFile);
        when(ipsSrcFile.getChildren()).thenReturn(new IIpsElement[] { ipsObject });
        when(ipsObject.getChildren()).thenReturn(new IIpsElement[] { ipsObjectPart2 });

        assertSame(ipsObjectPart2, refactoringProcessor.getIpsElement());
    }

    @Test
    public void testGetIpsElement_ipsObject() throws Exception {
        IIpsObject ipsObject = mock(IIpsObject.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);

        IpsRefactoringProcessor refactoringProcessor = new TestProcessor(ipsObject);

        when(ipsObject.exists()).thenReturn(true);
        assertSame(ipsObject, refactoringProcessor.getIpsElement());

        IIpsObject ipsObject2 = mock(IIpsObject.class);

        when(ipsObject.getName()).thenReturn("ipsObject");
        when(ipsObject2.getName()).thenReturn("ipsObject");
        when(ipsSrcFile.getName()).thenReturn("ipsSrcFile");

        when(ipsObject.exists()).thenReturn(false);
        when(ipsObject.getParent()).thenReturn(ipsSrcFile);
        when(ipsSrcFile.getChildren()).thenReturn(new IIpsElement[] { ipsObject2 });

        assertSame(ipsObject2, refactoringProcessor.getIpsElement());
    }

    @Test
    public void testGetIpsElement_anyOther() throws Exception {
        IIpsElement ipsElement = mock(IIpsElement.class);

        IpsRefactoringProcessor refactoringProcessor = new TestProcessor(ipsElement);

        when(ipsElement.exists()).thenReturn(true);
        assertSame(ipsElement, refactoringProcessor.getIpsElement());

        when(ipsElement.exists()).thenReturn(false);

        assertSame(ipsElement, refactoringProcessor.getIpsElement());
    }

}
