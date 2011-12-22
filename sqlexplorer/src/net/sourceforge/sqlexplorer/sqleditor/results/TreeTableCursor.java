package net.sourceforge.sqlexplorer.sqleditor.results;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeTableCursor {
	
	private Tree tree;

	public TreeTableCursor(Tree tree) {
		super();
		this.tree = tree;
	}

	public TreeItem getRow() {
        TreeItem[] items = tree.getSelection();
        if (items == null || items.length == 0)
        	return null;
        return items[0];
	}
	
	public int getColumn() {
        TreeItem[] items = tree.getSelection();
        if (items == null || items.length == 0)
        	return 0;
		return 0;
	}
}
