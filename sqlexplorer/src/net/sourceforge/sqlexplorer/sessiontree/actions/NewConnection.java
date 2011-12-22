package net.sourceforge.sqlexplorer.sessiontree.actions;

import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.actions.OpenPasswordConnectDialogAction;
import net.sourceforge.sqlexplorer.util.TextUtil;
import org.eclipse.jface.action.Action;

/**
 * @author Mazzolini
 * 
 */
public class NewConnection extends Action {

    private User user;

    /**
     * @param alias
     */
    public NewConnection(User user) {
        this.user = user;
    }

    public void run() {
        OpenPasswordConnectDialogAction openDlgAction = new OpenPasswordConnectDialogAction(user.getAlias(), user);
        openDlgAction.run();
    }

    public String getText() {
        String name = user.getAlias().getName() + '/' + user.getUserName();
        name = TextUtil.replaceChar(name, '@', "_");
        return name;
    }

    
}
