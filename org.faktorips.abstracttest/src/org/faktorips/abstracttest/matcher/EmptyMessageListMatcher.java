/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.abstracttest.matcher;

import org.faktorips.util.message.MessageList;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Checks whether a {@link MessageList} is empty.
 */
public class EmptyMessageListMatcher extends TypeSafeMatcher<MessageList> {

    @Override
    public void describeTo(Description description) {
        description.appendText("an empty message list");
    }

    @Override
    protected boolean matchesSafely(MessageList list) {
        return list.isEmpty();
    }

}
