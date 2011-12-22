/**
 * 
 */
package net.sourceforge.sqlexplorer.sqleditor.results.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.sqleditor.results.GenericAction;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.custom.CTabItem;

/**
 * @author Heiko
 *
 */
public class ReRunAction extends GenericAction 
{

	private CTabItem item;

	/**
	 * 
	 */
	public ReRunAction(CTabItem pItem) 
	{
		super(Messages.getString("DataSetTable.Actions.ReRun"), ImageUtil.getDescriptor("Images.ReRunIcon"));
		this.item = pItem;
	}

	@Override
	public void run() 
	{
		((Job)this.item.getData()).schedule();
	}
	
	
}
