/*
 * Copyright (C) 2006 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
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
package net.sourceforge.sqlexplorer.plugin.editors;

import java.util.List;

import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.DatabaseStructureView;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.Dictionary;
import net.sourceforge.sqlexplorer.sqleditor.SQLTextViewer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

class MouseClickListener implements KeyListener, MouseListener,
		MouseMoveListener, FocusListener, PaintListener,
		IPropertyChangeListener, IDocumentListener, ITextInputListener {

	private SQLEditor editor;
	
    @SuppressWarnings("unused")
	private INode activeTableNode;

	private boolean fActive;

	/** The currently active style range. */
	private IRegion fActiveRegion;

	/** The link color. */
	private Color fColor;

	/** The hand cursor. */
	private Cursor fCursor;

	/** The key modifier mask. */
	private int fKeyModifierMask = SWT.CTRL;

	/** The currently active style range as position. */
	private Position fRememberedPosition;

	private ISourceViewer sourceViewer;

	public MouseClickListener(SQLEditor editor) {
		super();
		this.editor = editor;
	}

	private void activateCursor(ISourceViewer viewer) {

		StyledText text = viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
		Display display = text.getDisplay();
		if (fCursor == null)
			fCursor = new Cursor(display, SWT.CURSOR_HAND);
		text.setCursor(fCursor);
	}
	
	public void deactivate() {

		deactivate(false);
	}

	public void deactivate(boolean redrawAll) {

		if (!fActive)
			return;

		repairRepresentation(redrawAll);
		fActive = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {

		if (fActive && fActiveRegion != null) {
			fRememberedPosition = new Position(fActiveRegion.getOffset(),
					fActiveRegion.getLength());
			try {
				event.getDocument().addPosition(fRememberedPosition);
			} catch (BadLocationException x) {
				fRememberedPosition = null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {

		if (fRememberedPosition != null && !fRememberedPosition.isDeleted()) {
			event.getDocument().removePosition(fRememberedPosition);
			fActiveRegion = new Region(fRememberedPosition.getOffset(),
					fRememberedPosition.getLength());
		}
		fRememberedPosition = null;

		if (sourceViewer != null) {
			StyledText widget = sourceViewer.getTextWidget();
			if (widget != null && !widget.isDisposed()) {
				widget.getDisplay().asyncExec(new Runnable() {

					public void run() {

						deactivate();
					}
				});
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
	 */
	public void focusGained(FocusEvent e) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
	 */
	public void focusLost(FocusEvent e) {

		deactivate();

	}

	private int getCurrentTextOffset(ISourceViewer viewer) {

		try {
			StyledText text = viewer.getTextWidget();
			if (text == null || text.isDisposed())
				return -1;

			Display display = text.getDisplay();
			Point absolutePosition = display.getCursorLocation();
			Point relativePosition = text.toControl(absolutePosition);

			int widgetOffset = text.getOffsetAtLocation(relativePosition);
			if (viewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
				return extension.widgetOffset2ModelOffset(widgetOffset);
			} else {
				return widgetOffset + viewer.getVisibleRegion().getOffset();
			}

		} catch (IllegalArgumentException e) {
			return -1;
		}
	}

	@SuppressWarnings("unchecked")
	private IRegion getCurrentTextRegion(ISourceViewer viewer) {

		if (viewer == null)
			return null;
		Dictionary dictionary = ((SQLTextViewer) viewer).dictionary;
		if (dictionary == null)
			return null;
		int offset = getCurrentTextOffset(viewer);
		if (offset == -1)
			return null;

		try {

			IRegion reg = selectWord(viewer.getDocument(), offset);
			if (reg == null)
				return null;
			String selection = viewer.getDocument().get(reg.getOffset(),
					reg.getLength());
			if (selection == null)
				return null;
			Object obj = dictionary.getByTableName(selection.toLowerCase());

			if (obj == null)
				return null;
			else {
				if (!(obj instanceof List))
					return null;
				List<Object> ls = (List<Object>) obj;
				if (ls.isEmpty())
					return null;
				Object node = ls.get(0);
				if (node instanceof TableNode)
					activeTableNode = (INode) node;
				else
					return null;
			}
			return reg;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Point getMaximumLocation(StyledText text, int offset, int length) {

		Point maxLocation = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

		for (int i = 0; i <= length; i++) {
			Point location = text.getLocationAtOffset(offset + i);

			if (location.x > maxLocation.x)
				maxLocation.x = location.x;
			if (location.y > maxLocation.y)
				maxLocation.y = location.y;
		}

		return maxLocation;
	}

	private Point getMinimumLocation(StyledText text, int offset, int length) {

		Point minLocation = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

		for (int i = 0; i <= length; i++) {
			Point location = text.getLocationAtOffset(offset + i);

			if (location.x < minLocation.x)
				minLocation.x = location.x;
			if (location.y < minLocation.y)
				minLocation.y = location.y;
		}

		return minLocation;
	}

	private void highlightRegion(ISourceViewer viewer, IRegion region) {

		if (region.equals(fActiveRegion))
			return;

		repairRepresentation();

		StyledText text = viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;

		// highlight region
		int offset = 0;
		int length = 0;

		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
			IRegion widgetRange = extension.modelRange2WidgetRange(region);
			if (widgetRange == null)
				return;

			offset = widgetRange.getOffset();
			length = widgetRange.getLength();

		} else {
			offset = region.getOffset()
					- viewer.getVisibleRegion().getOffset();
			length = region.getLength();
		}

		StyleRange oldStyleRange = text.getStyleRangeAtOffset(offset);
		Color foregroundColor = fColor;
		Color backgroundColor = oldStyleRange == null ? text
				.getBackground() : oldStyleRange.background;
		StyleRange styleRange = new StyleRange(offset, length,
				foregroundColor, backgroundColor);
		text.setStyleRange(styleRange);

		// underline
		text.redrawRange(offset, length, true);

		fActiveRegion = region;
	}

	private boolean includes(IRegion region, IRegion position) {

		return position.getOffset() >= region.getOffset()
				&& position.getOffset() + position.getLength() <= region
						.getOffset()
						+ region.getLength();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument,
	 *      org.eclipse.jface.text.IDocument)
	 */
	public void inputDocumentAboutToBeChanged(IDocument oldInput,
			IDocument newInput) {

		if (oldInput == null)
			return;
		deactivate();
		oldInput.removeDocumentListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument,
	 *      org.eclipse.jface.text.IDocument)
	 */
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {

		if (newInput == null)
			return;
		newInput.addDocumentListener(this);
	}

	public void install(ISourceViewer sourceViewer) {

		this.sourceViewer = sourceViewer;
		if (sourceViewer == null)
			return;

		StyledText text = sourceViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;

		updateColor(sourceViewer);

		sourceViewer.addTextInputListener(this);

		IDocument document = sourceViewer.getDocument();
		if (document != null)
			document.addDocumentListener(this);

		text.addKeyListener(this);
		text.addMouseListener(this);
		text.addMouseMoveListener(this);
		text.addFocusListener(this);
		text.addPaintListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent event) {
		editor.updateCursorPosition();

		if (fActive) {
			deactivate();
			return;
		}

		if (event.keyCode != fKeyModifierMask) {
			deactivate();
			return;
		}

		fActive = true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		editor.updateCursorPosition();

		if (!fActive)
			return;

		deactivate();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) {
		editor.updateCursorPosition();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDown(MouseEvent event) {
		editor.updateCursorPosition();

		if (!fActive)
			return;

		if (event.stateMask != fKeyModifierMask) {
			deactivate();
			return;
		}

		if (event.button != 1) {
			deactivate();
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseMove(MouseEvent event) {

		if (event.widget instanceof Control
				&& !((Control) event.widget).isFocusControl()) {
			deactivate();
			return;
		}

		if (!fActive) {
			if (event.stateMask != fKeyModifierMask)
				return;
			// modifier was already pressed
			fActive = true;
		}

		if (sourceViewer == null) {
			deactivate();
			return;
		}

		StyledText text = sourceViewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			deactivate();
			return;
		}

		if ((event.stateMask & SWT.BUTTON1) != 0
				&& text.getSelectionCount() != 0) {
			deactivate();
			return;
		}

		IRegion region = getCurrentTextRegion(sourceViewer);
		if (region == null || region.getLength() == 0) {
			repairRepresentation();
			return;
		}

		highlightRegion(sourceViewer, region);
		activateCursor(sourceViewer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
		editor.updateCursorPosition();

		if (!fActive)
			return;

		if (e.button != 1) {
			deactivate();
			return;
		}

		boolean wasActive = fCursor != null;

		deactivate();

		if (wasActive) {

			BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

				public void run() {

					try {
						DatabaseStructureView structureView = SQLExplorerPlugin.getDefault().getDatabaseStructureView();
						if (structureView != null) {
							editor.getEditorSite()
									.getWorkbenchWindow().getActivePage()
									.bringToTop(structureView);
						}

					} catch (Exception e1) {
						SQLExplorerPlugin
								.error("Error selecting table", e1);
					}
				}
			});

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent event) {

		if (fActiveRegion == null)
			return;

		if (sourceViewer == null)
			return;

		StyledText text = sourceViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;

		int offset = 0;
		int length = 0;

		if (sourceViewer instanceof ITextViewerExtension5) {

			ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			IRegion widgetRange = extension
					.modelRange2WidgetRange(new Region(offset, length));
			if (widgetRange == null)
				return;

			offset = widgetRange.getOffset();
			length = widgetRange.getLength();

		} else {

			IRegion region = sourceViewer.getVisibleRegion();
			if (!includes(region, fActiveRegion))
				return;

			offset = fActiveRegion.getOffset() - region.getOffset();
			length = fActiveRegion.getLength();
		}

		// support for bidi
		Point minLocation = getMinimumLocation(text, offset, length);
		Point maxLocation = getMaximumLocation(text, offset, length);

		int x1 = minLocation.x;
		int x2 = minLocation.x + maxLocation.x - minLocation.x - 1;
		int y = minLocation.y + text.getLineHeight() - 1;

		GC gc = event.gc;
		if (fColor != null && !fColor.isDisposed())
			gc.setForeground(fColor);
		gc.drawLine(x1, y, x2, y);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {

		// noop
	}

	private void repairRepresentation() {

		repairRepresentation(false);
	}

	private void repairRepresentation(boolean redrawAll) {

		if (fActiveRegion == null)
			return;

		if (sourceViewer != null) {
			resetCursor(sourceViewer);

			int offset = fActiveRegion.getOffset();
			int length = fActiveRegion.getLength();

			// remove style
			if (!redrawAll && sourceViewer instanceof ITextViewerExtension2)
				((ITextViewerExtension2) sourceViewer)
						.invalidateTextPresentation(offset, length);
			else
				sourceViewer.invalidateTextPresentation();

			// remove underline
			if (sourceViewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
				offset = extension.modelOffset2WidgetOffset(offset);
			} else {
				offset -= sourceViewer.getVisibleRegion().getOffset();
			}

			StyledText text = sourceViewer.getTextWidget();
			try {
				text.redrawRange(offset, length, true);
			} catch (IllegalArgumentException x) {
				x.printStackTrace();
				// JavaPlugin.log(x);
			}
		}

		fActiveRegion = null;
	}

	private void resetCursor(ISourceViewer viewer) {

		StyledText text = viewer.getTextWidget();
		if (text != null && !text.isDisposed())
			text.setCursor(null);

		if (fCursor != null) {
			fCursor.dispose();
			fCursor = null;
		}
	}

	private IRegion selectWord(IDocument document, int anchor) {

		try {
			int offset = anchor;
			char c;

			while (offset >= 0) {
				c = document.getChar(offset);
				if (!Character.isJavaIdentifierPart(c))
					break;
				--offset;
			}

			int start = offset;

			offset = anchor;
			int length = document.getLength();

			while (offset < length) {
				c = document.getChar(offset);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++offset;
			}

			int end = offset;

			if (start == end)
				return new Region(start, 0);
			else
				return new Region(start + 1, end - start - 1);

		} catch (BadLocationException x) {
			return null;
		}
	}

	public void uninstall() {

		if (fColor != null) {
			fColor.dispose();
			fColor = null;
		}

		if (fCursor != null) {
			fCursor.dispose();
			fCursor = null;
		}

		if (sourceViewer == null)
			return;

		sourceViewer.removeTextInputListener(this);

		IDocument document = sourceViewer.getDocument();
		if (document != null)
			document.removeDocumentListener(this);

		StyledText text = sourceViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;

		text.removeKeyListener(this);
		text.removeMouseListener(this);
		text.removeMouseMoveListener(this);
		text.removeFocusListener(this);
		text.removePaintListener(this);
	}

	private void updateColor(ISourceViewer viewer) {

		if (fColor != null)
			fColor.dispose();

		StyledText text = viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;

		Display display = text.getDisplay();
		fColor = new Color(display, new RGB(0, 0, 255));
	}

}