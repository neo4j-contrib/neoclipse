/**
 * 
 */
package net.sourceforge.sqlexplorer.plugin.editors;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class CursorPositionContrib extends ContributionItem {

    // Label that shows current cursor position
    private CLabel _cursorPosLabel;

	public CursorPositionContrib() {
		super(CursorPositionContrib.class.getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void fill(Composite parent) {
		super.fill(parent);
        
		String text = "9999, 999";
		GC gc = new GC(parent);
		gc.setFont(parent.getFont());
		FontMetrics fm = gc.getFontMetrics();
		Point extent = gc.textExtent(text);
		
		StatusLineLayoutData statusLineLayoutData = new StatusLineLayoutData();
		statusLineLayoutData.widthHint = extent.x;
		statusLineLayoutData.heightHint = fm.getHeight();
		gc.dispose();
		
        _cursorPosLabel = new CLabel(parent, SWT.NONE);
		_cursorPosLabel.setLayoutData(statusLineLayoutData);
		_cursorPosLabel.setText("");
	}

	public void setPosition(int lineNo, int charNo) {
		if (_cursorPosLabel == null || _cursorPosLabel.isDisposed())
			return;
		_cursorPosLabel.setText("" + lineNo + ", " + charNo);
		_cursorPosLabel.pack();
		_cursorPosLabel.getParent().layout();
	}

}