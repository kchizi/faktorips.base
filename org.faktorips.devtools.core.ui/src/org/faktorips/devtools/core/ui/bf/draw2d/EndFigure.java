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

/**
 * 
 */
package org.faktorips.devtools.core.ui.bf.draw2d;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * The figure for the end business function element.
 * 
 * @author Peter Erzberger
 */
public class EndFigure extends Shape {

    private Image errorImage;
    private boolean showError;

    public EndFigure() {
        errorImage = IpsPlugin.getDefault().getImage("size8/ErrorMessage.gif"); //$NON-NLS-1$
    }

    protected void fillShape(Graphics graphics) {
        Rectangle bounds = getBounds();
        graphics.pushState();
        graphics.translate(bounds.x, bounds.y);
        graphics.fillOval(1, 1, bounds.width - 3, bounds.height - 3);
        graphics.setBackgroundColor(ColorConstants.black);
        graphics.fillOval(7, 7, bounds.width - 15, bounds.height - 15);
        if (showError) {
            graphics.drawImage(errorImage, new Point(1, 1));
        }
        graphics.popState();
    }

    public void showError(boolean show) {
        this.showError = show;
    }

    protected void outlineShape(Graphics graphics) {
        Rectangle bounds = getBounds();
        graphics.pushState();
        graphics.translate(bounds.x, bounds.y);
        graphics.drawOval(1, 1, bounds.width - 3, bounds.height - 3);
        graphics.setBackgroundColor(ColorConstants.black);
        graphics.drawOval(7, 7, bounds.width - 15, bounds.height - 15);
        if (showError) {
            graphics.drawImage(errorImage, new Point(1, 1));
        }
        graphics.popState();
    }
}
