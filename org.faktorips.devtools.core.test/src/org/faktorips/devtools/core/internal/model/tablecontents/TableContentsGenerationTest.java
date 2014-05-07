/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablecontents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class TableContentsGenerationTest extends AbstractIpsPluginTest {

    private ITableContents table;
    private TableContentsGeneration generation;
    private IIpsProject project;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE,
                "StructureTable");
        table = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "TestTable");
        table.setTableStructure(structure.getQualifiedName());
        generation = (TableContentsGeneration)table.newGeneration();
        table.newColumn(null);
        table.newColumn(null);
        table.newColumn(null);

        generation.getIpsSrcFile().save(true, null);
    }

    @Test
    public void testGetChildren() {
        int childrenSizeBefore = generation.getChildren().length;
        table.newColumn(null);
        table.newColumn(null);
        generation.newRow();
        generation.newRow();
        IIpsElement[] children = generation.getChildren();
        assertEquals(childrenSizeBefore + 2, children.length);
    }

    @Test
    public void testNewRow() {
        table.newColumn(null);
        table.newColumn(null);
        IRow row0 = generation.newRow();
        String id0 = row0.getId();
        assertNotNull(id0);
        assertEquals(0, row0.getRowNumber());
        assertNull(row0.getValue(0));
        assertNull(row0.getValue(1));

        IRow row1 = generation.newRow();
        String id1 = row1.getId();
        assertNotNull(id1);
        assertFalse(id0.equals(id1));
        assertEquals(1, row1.getRowNumber());
    }

    @Test
    public void testNewColumn() {
        IRow row1 = generation.newRow();
        IRow row2 = generation.newRow();
        generation.newColumn(3, "a");
        assertEquals("a", row1.getValue(3));
        assertEquals("a", row2.getValue(3));
    }

    @Test
    public void testRemoveColumn() {
        IRow row1 = generation.newRow();
        IRow row2 = generation.newRow();
        row1.setValue(0, "row1,col1");
        row1.setValue(1, "row1,col2");
        row1.setValue(2, "row1,col3");
        row2.setValue(0, "row2,col1");
        row2.setValue(1, "row2,col2");
        row2.setValue(2, "row2,col3");
        generation.removeColumn(1);
        assertEquals("row1,col1", row1.getValue(0));
        assertEquals("row1,col3", row1.getValue(1));
        try {
            row1.getValue(2);
            fail();
        } catch (Exception e) {
        }
        assertEquals("row2,col1", row2.getValue(0));
        assertEquals("row2,col3", row2.getValue(1));
        try {
            row2.getValue(2);
            fail();
        } catch (Exception e) {
        }

    }

    @Test
    public void testToXml() {
        IRow row1 = generation.newRow();
        IRow row2 = generation.newRow();
        Element element = generation.toXml(newDocument());
        row1.delete();
        row2.delete();
        generation.initFromXml(element);
        assertEquals(2, generation.getNumOfRows());
    }

    @Test
    public void testInitFromXml() {
        generation.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(2, generation.getNumOfRows());
    }

    @Test
    public void testNewPart() {
        // test rownumber init within newPart()
        IRow row0 = generation.newPart(IRow.class);
        String id0 = row0.getId();
        assertNotNull(id0);
        assertEquals(0, row0.getRowNumber());

        IRow row1 = generation.newPart(IRow.class);
        String id1 = row1.getId();
        assertNotNull(id1);
        assertFalse(id0.equals(id1));
        assertEquals(1, row1.getRowNumber());

        assertTrue(generation.newPart(IRow.class) != null);
    }

    @Test
    public void testClear() {
        generation.newRow();
        generation.newRow();
        generation.clear();
        assertEquals(0, generation.getNumOfRows());
    }

    @Test
    public void testGetRow() {
        IRow row1 = generation.newRow();
        generation.newRow();
        IRow row2 = generation.newRow();

        assertEquals(row1, generation.getRow(0));
        assertEquals(row2, generation.getRow(2));

        assertNull(generation.getRow(-1));
        assertNull(generation.getRow(42));
    }

    @Test
    public void testGetRowIndex() {
        IRow row = generation.newRow();
        assertEquals(0, row.getRowNumber());
        row = generation.newRow();
        assertEquals(1, row.getRowNumber());
        row = generation.newRow();
        assertEquals(2, row.getRowNumber());
    }

    @Test
    public void testInsertRowAfter() {
        IRow row0 = generation.insertRowAfter(999);
        assertEquals(0, row0.getRowNumber());
        assertEquals(true, generation.getIpsSrcFile().isDirty());

        IRow row1 = generation.newRow();
        assertEquals(1, row1.getRowNumber());
        IRow row2 = generation.insertRowAfter(0);
        assertEquals(1, row2.getRowNumber());
        IRow row3 = generation.insertRowAfter(0);
        assertEquals(1, row3.getRowNumber());
        assertEquals(2, row2.getRowNumber());
        IRow row4 = generation.insertRowAfter(999);
        assertEquals(4, row4.getRowNumber());
    }

}
