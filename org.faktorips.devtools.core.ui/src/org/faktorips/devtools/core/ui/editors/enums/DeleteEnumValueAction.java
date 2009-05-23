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

package org.faktorips.devtools.core.ui.editors.enums;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * This action is used by the <code>EnumValuesSection</code> for deleting enum values.
 * 
 * @see EnumValuesSection
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class DeleteEnumValueAction extends Action {

    /** The name of the image for the action. */
    private final String IMAGE_NAME = "Delete.gif";

    /** The enum values table viewer linking the enum values ui table widget with the model data. */
    private TableViewer enumValuesTableViewer;

    /**
     * Creates a new <code>DeleteEnumValueAction</code>.
     * 
     * @param enumValuesTableViewer The enum values table viewer linking the enum values ui table
     *            widget with the model data.
     * 
     * @throws NullPointerException If <code>enumValuesTableViewer</code> is <code>null</code>.
     */
    public DeleteEnumValueAction(TableViewer enumValuesTableViewer) {
        super();

        ArgumentCheck.notNull(enumValuesTableViewer);

        this.enumValuesTableViewer = enumValuesTableViewer;

        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(IMAGE_NAME));
        setText(Messages.EnumValuesSection_labelDeleteValue);
        setToolTipText(Messages.EnumValuesSection_tooltipDeleteValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        IStructuredSelection selection = (IStructuredSelection)enumValuesTableViewer.getSelection();
        if (selection == null) {
            return;
        }

        IEnumValue enumValue = (IEnumValue)selection.getFirstElement();
        if (enumValue != null) {
            // Determine index to delete for selecting the next enum value after deletion
            IEnumValueContainer enumValueContainer = enumValue.getEnumValueContainer();
            List<IEnumValue> enumValuesList = enumValueContainer.getEnumValues();
            for (int i = 0; i < enumValuesList.size(); i++) {
                IEnumValue currentEnumValue = enumValuesList.get(i);
                if (currentEnumValue.equals(enumValue)) {
                    if (enumValuesList.size() > i + 1) {
                        IStructuredSelection newSelection = new StructuredSelection(enumValuesList.get(i + 1));
                        enumValuesTableViewer.setSelection(newSelection, true);
                        break;
                    }
                }
            }

            enumValue.delete();
            enumValuesTableViewer.refresh(true);
        }

    }

}
