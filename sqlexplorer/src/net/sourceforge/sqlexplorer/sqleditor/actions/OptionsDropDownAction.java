package net.sourceforge.sqlexplorer.sqleditor.actions;

import java.sql.SQLException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.util.ImageUtil;

public class OptionsDropDownAction extends AbstractEditorAction implements IMenuCreator {
	
	// The drop down menu
	private Menu menu;
	
	public OptionsDropDownAction(SQLEditor editor, Composite parent) {
		super(editor);
        setText("Options");
        setMenuCreator(this);
	}

	public void dispose() {
		if (menu != null) {
			menu.dispose();
			menu = null;
		}
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

	public Menu getMenu(Control parent) {
		if (menu != null)
			menu.dispose();
        menu = new Menu(parent);
		Action action = new Action(Messages.getString("SQLEditor.Options.AutoCommit"), IAction.AS_CHECK_BOX) {
			public void run(){
				try {
					_editor.getSession().setAutoCommit(isChecked());
					_editor.getEditorToolBar().refresh();
				}catch(SQLException e) {
					SQLExplorerPlugin.error(e);
				}
			}
		};
		action.setChecked(_editor.getSession().isAutoCommit());
		addActionToMenu(menu, action);
		action = new Action(Messages.getString("SQLEditor.Options.CommitOnClose"), IAction.AS_CHECK_BOX) {
			public void run(){
				_editor.getSession().setCommitOnClose(isChecked());
				_editor.getEditorToolBar().refresh();
			}

			@Override
			public boolean isEnabled() {
				return super.isEnabled() && !_editor.getSession().isAutoCommit();
			}
		};
		action.setChecked(_editor.getSession().isCommitOnClose());
		addActionToMenu(menu, action);

		return menu;
	}

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageUtil.getDescriptor("Images.OptionsDropDown");
	}

	@Override
	public String getText() {
		return Messages.getString("SQLEditor.Actions.OptionsDropDown");
	}

	@Override
	public String getToolTipText() {
		return Messages.getString("SQLEditor.Actions.OptionsDropDown.Tooltip");
	}

	@Override
	public void run() {
//		menu.setLocation(100, 100);
//		menu.setVisible(true);
	}

}
