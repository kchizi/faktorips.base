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

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.EditFieldChangesBroadcaster;
import org.faktorips.util.message.MessageList;


/**
 * Abstract base class to easy implementation of new edit fields..
 * 
 * @author Jan Ortmann
 */
public abstract class DefaultEditField implements EditField {
    
    private boolean notifyChangeListeners = true;
    private List changeListeners;
    private boolean supportNull = true;
    
    /**
     * Returns the value shown in the edit field's underlying control. If the control's content
     * can't be parsed to an instance of the appropriate datatype, <code>null</code> is returned. 
     * 
     * {@inheritDoc}
     */
    public final Object getValue() {
        try {
            return parseContent();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Parses the content shown in the edit field's underlying control.
     * 
     * @throws Exception if the content can't be parsed.
     */
    protected abstract Object parseContent() throws Exception;
    
    /**
     * {@inheritDoc}
     */
    public boolean isTextContentParsable() {
        try {
            parseContent();
            return true;
        } catch (Exception e ) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setMessages(MessageList list) {
        MessageCueController.setMessageCue(getControl(), list);
    }

    /**
     * {@inheritDoc}
     */
    public boolean addChangeListener(ValueChangeListener listener) {
        if (changeListeners==null) {
            changeListeners = new ArrayList(1);
        }
        boolean added = changeListeners.add(listener);
        if (added && changeListeners.size()==1) {
            addListenerToControl();
        }
        return added;
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeChangeListener(ValueChangeListener listener) {
        if (changeListeners==null) {
            return false;
        }
        return changeListeners.remove(listener);
    }
    
    /**
     * If the first change listener is added to the edit field, the edit field
     * itself has to listen to changes in it's control.
     */
    protected abstract void addListenerToControl();
    
    /**
     * {@inheritDoc}
     */
    public void setValue(Object newValue, boolean triggerValueChanged) {
        notifyChangeListeners = triggerValueChanged;
        try {
            setValue(newValue);
        } catch (Exception e){
            IpsPlugin.log(e);
        } finally {
            notifyChangeListeners = true;
        }
    }
    
    /**
     * Sends the given event to all registered listeners (in delayed manner).
     * 
     * @see EditFieldChangesBroadcaster
     */
    protected void notifyChangeListeners(FieldValueChangedEvent event) {
        notifyChangeListeners(event, false);
    }
 
    /**
     * Sends the given event to all registered listeners.
     * 
     * @param event The event which we'll send to the listeners
     * @param broadcastImmediately <code>true</code> the event will be directly broadcast to all registered
     *            listener <code>false</code> the event will be delayed send to the listeners.
     * @see EditFieldChangesBroadcaster
     */
    protected void notifyChangeListeners(FieldValueChangedEvent event, boolean broadcastImmediately) {
        if (!notifyChangeListeners || changeListeners==null) {
            return;
        }
        
        if (broadcastImmediately) {
            // notify change listeners immediately
            IpsUIPlugin.getDefault().getEditFieldChangeBroadcaster().broadcastImmediately(event,
                    (ValueChangeListener[])changeListeners.toArray(new ValueChangeListener[changeListeners.size()]));
        } else {
            // notify change listeners in delayed manner
            IpsUIPlugin.getDefault().getEditFieldChangeBroadcaster().broadcastDelayed(event,
                    (ValueChangeListener[])changeListeners.toArray(new ValueChangeListener[changeListeners.size()]));
        }
    }
 
    /**
     * Returns the null-representation-string defined by the user (see IpsPreferences)
     * if the given object is <code>null</code>, the unmodified object otherwise.
     */
    public Object prepareObjectForSet(Object object) {
    	if (object == null && supportNull) {
    		return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
    	}
    	return object;
    }

    /**
     * Returns <code>null</code> if the given value is the null-representation-string, 
     * the unmodified value otherwise.
     */
    public Object prepareObjectForGet(Object value) {
    	if (supportNull && IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(value)) {
    		return null;
    	}
    	return value;
    }
    
    /**
     * <code>true</code> to activate null-handling (which means that a null-object
     * is transformed to the user defined null-representations-string and vice versa) or
     * <code>false</code> to deactivate null-handling.
     */
    public void setSupportsNull(boolean supportsNull) {
    	this.supportNull = supportsNull;
    }
    
    /**
     * Returns whether null is replaced by the null representation string or not.
     */
    public boolean supportsNull() {
        return supportNull;
    }
}
