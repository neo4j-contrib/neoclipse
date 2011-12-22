package net.sourceforge.sqlexplorer.dbstructure.nodes;

import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.eclipse.swt.graphics.Image;

public abstract class AbstractFolderNode extends AbstractNode {

    private String[] _filterExpressions;

    public AbstractFolderNode(String name) {
		super(name);
	}

	public AbstractFolderNode(String name, MetaDataSession session) {
		super(name, session);
	}

	public AbstractFolderNode(INode parent, String name, MetaDataSession session, String type) {
		super(parent, name, session, type);
        setImageKey("Images.closedFolder");
        setExpandedImageKey("Images.OpenFolder");
    }

    /**
     * Override this method to change the image that is displayed for this node
     * in the database structure outline.
     */
    public Image getImage() {
        if (get_imageKey() == null)
        	return super.getImage();
        return ImageUtil.getImage(get_imageKey());
    }

    public final String getUniqueIdentifier() {
        return getParent().getName() + '.' + getType();
    }


    /**
     * Checks if a node name should be filtered.
     * 
     * @param name to check for filtering
     * @return true if the name should be filtered
     */
    protected boolean isExcludedByFilter(String name) {

        if (_filterExpressions == null) {
            String filter = getSession().getUser().getAlias().getNameFilterExpression();
            if (filter != null) {
                _filterExpressions = filter.split(",");
            }
        }
        if (_filterExpressions == null || _filterExpressions.length == 0) {
            // no active filter
            return false;
        }

        for (int i = 0; i < _filterExpressions.length; i++) {

            String regex = _filterExpressions[i].trim();
            regex = TextUtil.replaceChar(regex, '?', ".");
            regex = TextUtil.replaceChar(regex, '*', ".*");

            if (regex.length() != 0 && name.matches(regex)) {
                // we have a match, exclude node..
                return true;
            }
        }

        // no match found
        return false;

    }


    public abstract void loadChildren();
}
