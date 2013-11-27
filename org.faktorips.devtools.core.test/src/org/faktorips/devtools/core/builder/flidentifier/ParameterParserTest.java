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

package org.faktorips.devtools.core.builder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.AnyDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.fl.ExprCompiler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParameterParserTest extends AbstractParserTest {

    private static final String ANY_PARAMETER = "noParameter";

    static final String MY_PARAMETER = "anyParameter";

    @Mock
    private IFormulaMethod formulaSignature;

    @Mock
    private IParameter parameter;

    private ParameterParser parameterParser;

    @Before
    public void createParameterParser() throws Exception {
        parameterParser = new ParameterParser(getExpression(), getIpsProject());
    }

    @Before
    public void mockFormulaSignature() throws Exception {
        when(getExpression().findFormulaSignature(getIpsProject())).thenReturn(formulaSignature);
        when(formulaSignature.getParameters()).thenReturn(new IParameter[] { parameter });
        when(parameter.getName()).thenReturn(MY_PARAMETER);
        when(parameter.findDatatype(getIpsProject())).thenReturn(AnyDatatype.INSTANCE);
    }

    @Test
    public void testParse_wrongType() throws Exception {
        IdentifierNode parameterNode = parameterParser.parse(MY_PARAMETER, new TestNode(mock(IProductCmptType.class)));

        assertNull(parameterNode);
    }

    @Test
    public void testParse_noParameter() throws Exception {
        IdentifierNode parameterNode = parameterParser.parse(ANY_PARAMETER, null);

        assertNull(parameterNode);
    }

    @Test
    public void testParse_findParameter() throws Exception {
        ParameterNode parameterNode = (ParameterNode)parameterParser.parse(MY_PARAMETER, null);

        assertNotNull(parameterNode);
        assertEquals(parameter, parameterNode.getParameter());
        assertEquals(AnyDatatype.INSTANCE, parameterNode.getDatatype());
    }

    @Test
    public void testGetParameters() throws Exception {
        IParameter[] parameters = parameterParser.getParameters();

        assertEquals(1, parameters.length);
        assertEquals(parameter, parameters[0]);
    }

    @Test
    public void testParse_noDatatype() throws Exception {
        when(parameter.findDatatype(getIpsProject())).thenReturn(null);

        InvalidIdentifierNode node = (InvalidIdentifierNode)parameterParser.parse(MY_PARAMETER, null);

        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, node.getMessage().getCode());
    }

}