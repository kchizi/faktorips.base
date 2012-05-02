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

package org.faktorips.devtools.core.ui.controls.chooser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.util.message.MessageList;

public class MultiValueSubsetChooserModel extends AbstractSubsetChooserModel {

    private final MultiValueHolder multiValueHolder;
    private final List<ListChooserValue> resultingValues = new ArrayList<ListChooserValue>();
    private final List<ListChooserValue> sourceValues = new ArrayList<ListChooserValue>();

    public MultiValueSubsetChooserModel(List<String> allValues, MultiValueHolder multiValueHolder,
            ValueDatatype datatype) {
        super(datatype);
        this.multiValueHolder = multiValueHolder;
        for (SingleValueHolder holder : multiValueHolder.getValue()) {
            resultingValues.add(new ListChooserValue(holder.getStringValue()));
        }
        for (String string : allValues) {
            sourceValues.add(new ListChooserValue(string));
        }
    }

    @Override
    public List<ListChooserValue> getAllValues() {
        return sourceValues;
    }

    @Override
    public List<ListChooserValue> getResultingValues() {
        return resultingValues;
    }

    @Override
    public MessageList validateValue(ListChooserValue value) {
        try {
            SingleValueHolder holder = findSingleValueHolderFor(value);
            MessageList messageList = multiValueHolder.validate(multiValueHolder.getIpsProject());
            messageList = messageList.getMessagesFor(holder);
            return messageList;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    protected SingleValueHolder findSingleValueHolderFor(ListChooserValue value) {
        List<SingleValueHolder> holders = multiValueHolder.getValue();
        for (SingleValueHolder singleValueHolder : holders) {
            if (value.getValue() == null && singleValueHolder.getStringValue() == null) {
                return singleValueHolder;
            } else if (value.getValue() != null && value.getValue().equals(singleValueHolder.getStringValue())) {
                return singleValueHolder;
            }
        }
        return null;
    }

    @Override
    protected void moveInternal(List<ListChooserValue> selectedValues, boolean up) {
        ListElementMover<ListChooserValue> mover = new ListElementMover<ListChooserValue>(resultingValues);
        mover.move(getValueIndices(selectedValues), up);
        updateMultiValueHolder();
    }

    protected int[] getValueIndices(List<ListChooserValue> selectedValues) {
        int[] indices = new int[selectedValues.size()];
        for (int i = 0; i < selectedValues.size(); i++) {
            indices[i] = resultingValues.indexOf(selectedValues.get(i));
        }
        return indices;
    }

    @Override
    protected void removeFromResultingValues(ListChooserValue value) {
        resultingValues.remove(value);
        updateMultiValueHolder();
    }

    protected void updateMultiValueHolder() {
        List<SingleValueHolder> holderList = new ArrayList<SingleValueHolder>();
        for (ListChooserValue value : getResultingValues()) {
            SingleValueHolder holder = new SingleValueHolder(multiValueHolder.getParent(), value.getValue());
            holderList.add(holder);
        }
        multiValueHolder.setValue(holderList);
    }

    @Override
    protected void addToResultingValues(ListChooserValue value) {
        resultingValues.add(value);
        updateMultiValueHolder();
    }

}