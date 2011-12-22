package net.sourceforge.sqlexplorer.preferences;
/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class ColorEditor {
	
	private Point fExtent;
	Image fImage;
	RGB fColorValue;
	Color fColor;
	Button fButton;
	
	public ColorEditor(Composite parent) {
		
		fButton= new Button(parent, SWT.PUSH);
		fExtent= computeImageSize(parent);
		fImage= new Image(parent.getDisplay(), fExtent.x, fExtent.y);
		
		GC gc= new GC(fImage);
		gc.setBackground(fButton.getBackground());
		gc.fillRectangle(0, 0, fExtent.x, fExtent.y);
		gc.dispose();
		
		fButton.setImage(fImage);
		fButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				ColorDialog colorDialog= new ColorDialog(fButton.getShell());
				colorDialog.setRGB(fColorValue);
				RGB newColor = colorDialog.open();
				if (newColor != null) {
					fColorValue= newColor;
					updateColorImage();
				}
			}
		});
		
		fButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				if (fImage != null)  {
					fImage.dispose();
					fImage= null;
				}
				if (fColor != null) {
					fColor.dispose();
					fColor= null;
				}
			}
		});
	}
	
	public RGB getColorValue() {
		return fColorValue;
	}
	
	public void setColorValue(RGB rgb) {
		fColorValue= rgb;
		updateColorImage();
	}
	
	public Button getButton() {
		return fButton;
	}
	
	protected void updateColorImage() {
		
		Display display= fButton.getDisplay();
		
		GC gc= new GC(fImage);
		gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
		gc.drawRectangle(0, 2, fExtent.x - 1, fExtent.y - 4);
		
		if (fColor != null)
			fColor.dispose();
			
		fColor= new Color(display, fColorValue);
		gc.setBackground(fColor);
		gc.fillRectangle(1, 3, fExtent.x - 2, fExtent.y - 5);
		gc.dispose();
		
		fButton.setImage(fImage);
	}
	
	protected Point computeImageSize(Control window) {
		GC gc= new GC(window);
		Font f= JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);
		gc.setFont(f);
		int height= gc.getFontMetrics().getHeight();
		gc.dispose();
		Point p= new Point(height * 3 - 6, height);
		return p;
	}
}
