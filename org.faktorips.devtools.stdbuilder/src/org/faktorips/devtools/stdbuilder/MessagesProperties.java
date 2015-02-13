/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.util.IoUtil;

public class MessagesProperties {

    private final Properties properties;

    private boolean modified = false;

    /**
     * Default constructor creating a new {@link Properties} object.
     */
    public MessagesProperties() {
        properties = new SortedProperties();
    }

    /**
     * Manually set the modification state
     * 
     * @param modified the new modification state
     */
    private void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isModified() {
        return modified;
    }

    /**
     * Putting a message text for a message key in map of messages and setting the modification
     * state if map has changed.
     * 
     * @param messageKey the key of the message
     * @param messageText the text of the message
     */
    public void put(String messageKey, String messageText) {
        if (!messageText.equals(properties.setProperty(messageKey, messageText))) {
            setModified(true);
        }
    }

    /**
     * Getting the message stored for the given key or null if there is no message for this key
     * 
     * @param key The key of the message you want to get
     * @return the message stored for the key or null if there is none
     */
    public String getMessage(String key) {
        return properties.getProperty(key);
    }

    /**
     * Removing the message with the given key and setting the modification state if the map
     * changed.
     * 
     * @param key the key of the message to be removed.
     */
    public void remove(String key) {
        if (properties.remove(key) != null) {
            setModified(true);
        }
    }

    /**
     * Clear all existing elements and load new properties form stream.
     * 
     * @param stream The {@link InputStream} to load, @see {@link Properties#load(InputStream)}
     */
    public void load(InputStream stream) {
        properties.clear();
        try {
            properties.load(stream);
            setModified(false);
        } catch (IOException e) {
            StdBuilderPlugin.log(new Status(IStatus.ERROR, StdBuilderPlugin.PLUGIN_ID,
                    "Error occured while reading validation messages file", e));
        } finally {
            IoUtil.close(stream);
        }
    }

    public void store(OutputStream outputStream, String comments) {
        try {
            properties.store(outputStream, comments);
            setModified(false);
        } catch (IOException e) {
            StdBuilderPlugin.log(new Status(IStatus.ERROR, StdBuilderPlugin.PLUGIN_ID,
                    "Error occured while saving validation messages file", e));
        } finally {
            IoUtil.close(outputStream);
        }
    }

    public int size() {
        return properties.size();
    }

    public void clear() {
        properties.clear();
    }

    public Set<String> keySet() {
        HashSet<String> result = new HashSet<String>();
        Set<Object> keySet = properties.keySet();
        for (Object object : keySet) {
            result.add(object.toString());
        }
        return result;
    }

    private static class SortedProperties extends Properties {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 7627392983212145038L;

        @Override
        public synchronized Enumeration<Object> keys() {
            Enumeration<Object> keysEnum = super.keys();
            Vector<Object> keyList = new Vector<Object>();
            while (keysEnum.hasMoreElements()) {
                keyList.add(keysEnum.nextElement());
            }
            Collections.sort(keyList, new Comparator<Object>() {

                @Override
                public int compare(Object o1, Object o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            return keyList.elements();
        }

    }
}