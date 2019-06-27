/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

/* Generated By:JavaCC: Do not edit this line. Token.java Version 3.0 */
package org.faktorips.fl.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Describes the input token stream.
 */

public class Token {

    /**
     * An integer that describes the kind of this token. This numbering system is determined by
     * JavaCCParser, and a table of these numbers is stored in the file ...Constants.java.
     */
    public int kind;

    /**
     * beginLine and beginColumn describe the position of the first character of this token; endLine
     * and endColumn describe the position of the last character of this token.
     */
    public int beginLine, beginColumn, endLine, endColumn;

    /**
     * The string image of the token.
     */
    public String image;

    /**
     * A reference to the next regular (non-special) token from the input stream. If this is the
     * last token from the input stream, or if the token manager has not read tokens beyond this
     * one, this field is set to null. This is true only if this token is also a regular token.
     * Otherwise, see below for a description of the contents of this field.
     */
    public Token next;

    /**
     * This field is used to access special tokens that occur prior to this token, but after the
     * immediately preceding regular (non-special) token. If there are no such special tokens, this
     * field is set to null. When there are more than one such special token, this field refers to
     * the last of these special tokens, which in turn refers to the next previous special token
     * through its specialToken field, and so on until the first special token (whose specialToken
     * field is null). The next fields of special tokens refer to other special tokens that
     * immediately follow it (without an intervening regular token). If there is no such token, this
     * field is null.
     */
    public Token specialToken;

    /**
     * Returns a new Token object, by default. However, if you want, you can create and return
     * subclass objects based on the value of ofKind. Simply add the cases to the switch for all
     * those special cases. For example, if you have a subclass of Token called IDToken that you
     * want to create if ofKind is ID, simlpy add something like :
     * 
     * case MyParserConstants.ID : return new IDToken();
     * 
     * to the following switch statement. Then you can cast matchedToken variable to the appropriate
     * type and use it in your lexical actions.
     */
    public static final Token newToken(int ofKind) {
        switch (ofKind) {
            default:
                return new Token();
        }
    }

    /**
     * Returns the starting point of the token. The Token only provides the row and column of the
     * starting point so we need to transfer these coordinates into a text position.
     * 
     * @param text The text in which the position should be determined
     * 
     * @return the position of the start of the identifier within the expression text
     */
    public int getStartPositionRelativeTo(String text) {
        return getPosition(text, beginLine, beginColumn);
    }

    /**
     * Returns the ending position of the token relative the given string by converting the token's
     * row and column indices to an absolute string position. (The string is interpreted as a single
     * line, and line-feed and carriage-return are each counted as a character)
     * 
     * @param text The text in which the position should be determined
     * 
     * @return the position of the start of the identifier within the expression text
     */
    public int getEndPositionRelativeTo(String text) {
        return getPosition(text, endLine, endColumn);
    }

    private int getPosition(String text, int line, int column) {
        Matcher matcher = Pattern.compile("(\\r\\n)|\\r|\\n").matcher(text); //$NON-NLS-1$
        boolean found = false;
        for (int i = 1; i < line; i++) {
            found = matcher.find();
        }
        return (found ? matcher.end() : 0) + column - 1;
    }

    /**
     * Returns the image.
     */
    @Override
    public String toString() {
        return image;
    }

}
