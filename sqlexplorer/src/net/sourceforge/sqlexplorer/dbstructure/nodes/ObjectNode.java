package net.sourceforge.sqlexplorer.dbstructure.nodes;

import org.eclipse.swt.graphics.Image;


public class ObjectNode extends AbstractNode {

	private char quoteChar = '"';
	
    public ObjectNode(String name, String type, INode parent, Image image) {
    	super(parent, name, parent.getSession(), type);
        _image = image;
    }
    

    /**
     * This node cannot have childnodes.
     */
    public boolean isEndNode() {
        return true;
    }

    /**
     * This node cannot have childnodes.
     */
    public void loadChildren() {
        return;
    }


    public String getQualifiedName() {
        return quoteChar + getSchemaOrCatalogName() + quoteChar + "." +quoteChar + getName() + quoteChar;
    }


	public char getQuoteChar() {
		return quoteChar;
	}


	public void setQuoteChar(char quoteChar) {
		this.quoteChar = quoteChar;
	}

}
