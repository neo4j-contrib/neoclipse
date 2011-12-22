package net.sourceforge.sqlexplorer.sqleditor.results;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores query cost breakdown in a recursive heirarchy; implements CellRangeRow to
 * be compatable with the standardised Editor and Result Tabs 
 * 
 * @author John Spackman
 */
public abstract class AbstractExplainNode implements CellRangeRow {

    // Parent node
    private AbstractExplainNode parent;

	// Children
    private ArrayList<AbstractExplainNode> children = new ArrayList<AbstractExplainNode>();

    /**
     * Constructor
     * @param parent
     */
	public AbstractExplainNode(AbstractExplainNode parent) {
		super();
		this.parent = parent;
	}
	
	/**
	 * Adds a child node
	 * @param nd
	 */
    public void add(AbstractExplainNode child) {
        children.add(child);
    }

    /**
     * Returns the list of children
     * @return
     */
    public List<AbstractExplainNode> getChildren() {
        return children;
    }

    /**
     * Returns the parent, nor null if the root node
     * @return
     */
    public AbstractExplainNode getParent() {
        return parent;
    }

	public CellRangeRow[] getChildRows() {
		return children.toArray(new CellRangeRow[0]);
	}

	public CellRangeRow getParentRow() {
		return parent;
	}

	public boolean hasChildRows() {
		return !children.isEmpty();
	}

}
