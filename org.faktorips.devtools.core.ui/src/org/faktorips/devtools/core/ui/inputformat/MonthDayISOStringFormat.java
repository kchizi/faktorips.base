/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Format for time input. Maps a {@link Locale} specific time string to the ISO time format (also
 * string) and vice versa.
 */
public class MonthDayISOStringFormat extends AbstractInputFormat<String> {

    private SimpleDateFormat localFormat = null;
    private final SimpleDateFormat isoMonthDayFormat = new SimpleDateFormat("--MM-dd"); //$NON-NLS-1$

    protected MonthDayISOStringFormat(String defaultNullString, Locale locale) {
        super(defaultNullString, locale);
        initFormat(locale);
    }

    public static MonthDayISOStringFormat newInstance() {
        MonthDayISOStringFormat instance = new MonthDayISOStringFormat(StringUtils.EMPTY, IpsPlugin.getDefault()
                .getIpsPreferences().getDatatypeFormattingLocale());
        return instance;
    }

    @Override
    protected String parseInternal(String stringToBeParsed) {
        try {
            Date date = localFormat.parse(stringToBeParsed);
            return isoMonthDayFormat.format(date);
        } catch (IllegalArgumentException e) {
            return stringToBeParsed;
        } catch (ParseException e) {
            return stringToBeParsed;
        }
    }

    @Override
    protected String formatInternal(String stringToBeFormatted) {
        try {
            Date date = isoMonthDayFormat.parse(stringToBeFormatted);
            return localFormat.format(date);
        } catch (ParseException e) {
            return stringToBeFormatted;
        }
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // nothing to do
    }

    @Override
    protected void initFormat(Locale locale) {
        String pattern = ((SimpleDateFormat)SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, locale))
                .toPattern();
        String monthDayPattern = pattern.replaceAll("[yY]*", StringUtils.EMPTY); //$NON-NLS-1$
        localFormat = new SimpleDateFormat(monthDayPattern, locale);
    }
}
