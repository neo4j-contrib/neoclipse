/**
 * 
 */
package net.sourceforge.sqlexplorer.sqlpanel;

import java.util.Collection;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dataset.DataSetRow;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.Message;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sqleditor.results.CellRange.Column;
import net.sourceforge.sqlexplorer.sqleditor.results.actions.ReRunAction;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

/**
 * @author Heiko
 *
 */
public class SQLBatchExecution extends SQLExecution {

	private CTabItem tabItem;
	private Text text;
	private int maxDisplayWidth = 255;
	private boolean appendQuery;
	private boolean appendRowsAffected;
	private boolean appendExecTime;
	private boolean toolTipSet = false;

	public SQLBatchExecution(SQLEditor _editor, QueryParser queryParser) {
		super(_editor, queryParser, 0);
	}

	@Override
	protected void doExecution(IProgressMonitor monitor) throws Exception {
		getEditor().getSite().getShell().getDisplay().syncExec(new Runnable() {

            public void run() {
            	initTab();
            }
		});
		super.doExecution(monitor);
	}

	private void initTab()
	{
		if(tabItem != null)
		{
			return;
		}
		IPreferenceStore store = SQLExplorerPlugin.getDefault().getPreferenceStore();
		this.maxDisplayWidth = store.getInt(IConstants.BATCH_RESULT_MAX_DISPLAY_WIDTH);
		this.appendQuery = store.getBoolean(IConstants.BATCH_RESULT_APPEND_QUERY);
		this.appendRowsAffected = store.getBoolean(IConstants.BATCH_RESULT_APPEND_ROWS_AFFECTED);
		this.appendExecTime = store.getBoolean(IConstants.BATCH_RESULT_APPEND_EXEC_TIME);

		this.tabItem = getEditor().createResultsTab(this);
		this.text = new Text(tabItem.getParent(), SWT.V_SCROLL | SWT.H_SCROLL);
        FontData[] fData = PreferenceConverter.getFontDataArray(store, IConstants.BATCH_RESULT_FONT);
        if (fData.length > 0) {
            JFaceResources.getFontRegistry().put(fData[0].toString(), fData);
            text.setFont(JFaceResources.getFontRegistry().get(fData[0].toString()));
        }

		if(tabItem.getControl() != null)
		{
			tabItem.getControl().dispose();
		}
		tabItem.setControl(text);
        MenuManager menuManager = new MenuManager("BatchResultsContextMenu");
        menuManager.setRemoveAllWhenShown(true);
        
        menuManager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
            	boolean hasSelection = text.getSelectionCount() > 0;
            	
                Action act = null;
        		act = new Action("Cut") {
            		
        			@Override
        			public void run() {
        				text.cut();
        			}
        		
        		};
        		act.setEnabled(hasSelection);
        		manager.add(act);
                
        		act = new Action("Copy") {
        	    		
        				@Override
        				public void run() {
        					text.copy();
        				}
        			
        			};
           		act.setEnabled(hasSelection);
        		manager.add(act);
        	    
        		act = new Action("Paste") {
            		
        			@Override
        			public void run() {
        				text.paste();
        			}
        		
        		};
        		manager.add(act);

        		manager.add(new Separator());
        		
                act = new Action("Select all") {
            		
        			@Override
        			public void run() {
        				text.selectAll();
        			}
        		
        		};
        		manager.add(act);
        		
        		manager.add(new Separator());
        		
        		manager.add(new ReRunAction(tabItem));
            }
        });
        
        Menu contextMenu = menuManager.createContextMenu(text);
        text.setMenu(contextMenu);
        text.addKeyListener(new KeyListener() {
		
			public void keyReleased(KeyEvent e) {
				if((e.stateMask & SWT.CTRL) == SWT.CTRL)
				{
					switch(e.keyCode)
					{
						case 'a':
							text.selectAll();
							break;
						case 'x':
							text.cut();
							break;
						case 'c':
							text.copy();
							break;
						case 'v':
							text.paste();
							break;
					}
				}
			}
		
			public void keyPressed(KeyEvent e) {
			}
		});
		
	}
	
	@Override
    protected void displayResults(final DataSet dataSet) {

    	// Switch to the UI thread to execute this
    	getEditor().getSite().getShell().getDisplay().syncExec(new Runnable() {

            public void run() {
            	_displayResults(dataSet);
            }            	
        });
    }

	private void _displayResults(DataSet dataSet)
	{
        try {
            Query sql = dataSet.getQuery();
    		int lineNo = sql.getLineNo();
            lineNo = getQueryParser().adjustLineNo(lineNo);

    		if(!this.toolTipSet)
    		{
    			boolean longCaptions = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.USE_LONG_CAPTIONS_ON_RESULTS);
    			this.toolTipSet = true;
    			String caption = tabItem.getText();
    			if(longCaptions)
    			{
        			tabItem.setText(caption + " ["+TextUtil.compressWhitespace(sql.getQuerySql(), MAX_CAPTION_LENGTH)+"]");
    			}
    			tabItem.setToolTipText(caption + " ["+TextUtil.compressWhitespace(sql.getQuerySql(), MAX_CAPTION_LENGTH)+"]");
    			
    		}

            StringBuilder out = new StringBuilder();
            if(dataSet.hasData())
	    	{
	    		for(Column column : dataSet.getColumns())
	    		{
	    			out.append(adjustText(column.getCaption(),column.getDisplaySize(), column.isRightJustify()));
	    			out.append(" ");
	    		}
	    		out.append("\n");
	    		for(Column column : dataSet.getColumns())
	    		{
	    			for(int i=0, maxi = Math.min(this.maxDisplayWidth, column.getDisplaySize()); i < maxi; i++)
	    			{
	    				out.append("-");
	    			}
	    			out.append(" ");
	    		}
	    		out.append("\n");
	    		for(DataSetRow row : dataSet.getRows())
	    		{
	        		for(Column column : dataSet.getColumns())
	        		{
	        			out.append(adjustText(column.getDisplayValue(row.getCellValue(column.getColumnIndex())),column.getDisplaySize(),column.isRightJustify()));
	        			out.append(" ");
	        		}
	        		out.append("\n");
	    		}
	    	}
            if(this.appendRowsAffected)
            {
                int resultCount = dataSet.hasData() ? dataSet.getRows().length : dataSet.getUpdateCount();
                out.append(dataSet.hasData() ? 
                		Messages.getString("SQLResultsView.Count.Prefix") : 
                			Messages.getString("SQLResultsView.Update.Prefix"))
                	.append(" ")
                	.append( resultCount)
                	.append("\n");
            	
            }
            if(this.appendExecTime)
            {
            	out.append(Messages.getString("SQLResultsView.Time.Prefix"))
            	   .append(" ")
            	   .append(dataSet.getExecutionTime())
            	   .append(" ")
            	   .append(Messages.getString("SQLResultsView.Time.Postfix"))
               	   .append("\n");
            }
            
            this.text.append(out.toString());
            this.text.append("\n");
            
        } catch (Exception e) {
        	MessageDialog.openError(getEditor().getSite().getShell(), "Error creating batch results", e.getMessage());
            SQLExplorerPlugin.error("Error creating batch results", e);
        }
    };

    private String adjustText(String pValue, int pWidth, boolean rightJustify)
    {
    	int width = Math.min(this.maxDisplayWidth, pWidth);
    	String result = pValue == null ? "" : pValue;
    	if(result.length() > width)
    	{
    		return result.substring(0, width);
    	}
    	while(result.length() < width)
    	{
    		if(rightJustify)
    		{
    			result = " " + result;
    		}
    		else
    		{
    			result = result + " ";
    		}
    	}
    	return result;
    }

    private void showMessages(Collection<Message> messages)
    {
		for(Message message : messages) {
			if((!this.appendQuery) && message.getStatus() == Message.Status.FAILURE)
			{
				text.append(message.getSql());
				text.append("\n");
			}
			text.append(message.getMessage());
			text.append("\n");
		}
    	
    }
    
    @Override
    protected void addMessages(final Collection<Message> messages) {
		getEditor().getSite().getShell().getDisplay().syncExec(new Runnable() {

            public void run() {
        		showMessages(messages);
            }
        });
	}
    
    @Override
    protected void addMessage(final Message message) {
		getEditor().getSite().getShell().getDisplay().syncExec(new Runnable() {

            public void run() {
    			text.append(message.getMessage());
    			text.append("\n");
            }
        });
	}
    protected void startQuery(final String querySQL) {
    	if(!this.appendQuery)
    	{
    		return;
    	}
		getEditor().getSite().getShell().getDisplay().syncExec(new Runnable() {

            public void run() {
    			text.append(querySQL);
    			text.append("\n");
            }
        });
	}

    @Override
    protected void logOverallExecution(String message) {
    	if(!this.appendExecTime)
    	{
    		return;
    	}
    	super.logOverallExecution(message);
		
	}
    @Override
    protected void logHistory(String querySQL) {
    	// no history
    }    
}
