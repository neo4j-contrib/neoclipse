package net.sourceforge.sqlexplorer.sqleditor.results;

import net.sourceforge.sqlexplorer.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class ColumnLocatorPopup extends Shell {

    private static final int ENTER = 13;

    private TableResultsTable resultsTab;
    
    private String lastNameSearched = null;
    private int lastColumnIndex = 0;

	public ColumnLocatorPopup(TableResultsTable resultsTab, Point popupLocation) {
		super(resultsTab.getTableViewer().getTable().getDisplay(), SWT.BORDER | SWT.ON_TOP);
		this.resultsTab = resultsTab;
		
        setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        setForeground(getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        setSize(250, 50);
        setLocation(popupLocation);
        setLayout(new GridLayout());

        lastNameSearched = null;
        
        // create new shell
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;

        // add 'find:' label
        Label label = new Label(this, SWT.NULL);
        label.setText(Messages.getString("DataSetTable.PopUp.Find"));
        label.setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        
        // add input field for search text
        final Text input = new Text(this, SWT.SINGLE | SWT.FILL);
        input.setLayoutData(gridData);
        input.setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        // scroll columns whenever something is typed in input field.
        input.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                Text t = (Text) e.widget;
                String text = t.getText();

                // locate column and show if found
                if (jumpToColumn(text))
                    input.setForeground(getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
                else
                    // give some subtle feedback to user that column doesn't exist..
                    input.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));                    
            }
        });

        // add listener so that we can jump to next column match when
        // user hits enter..
        input.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {       
                if (e.character == ENTER) {
                    // scroll to next match
                    if (jumpToColumn(null))
                        input.setForeground(getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
                    else
                        // give some subtle feedback to user that column doesn't exist..
                        input.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));                    
                }                
            }            
        });
        
        // close popup when user is no longer in inputfield
        input.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
            	close();
            }
        });
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Shell#close()
	 */
	@Override
	public void close() {
		super.close();
        if (!isDisposed())
        	dispose();
	}
    
    /**
     * Jump to next availabel column with header name.
     * If the same name is processed again, we jump to
     * the next column with the same name.  If no further columns
     * are available, we jump to first available column again.
     *  
     * @param name of column to jump to.
     * @return true if a matching column was found
     */
    private boolean jumpToColumn(String name) {
        String text = null;
        if (name != null) {            
            // use input to find column
            text = name.toLowerCase().trim();
            lastNameSearched = text;
            lastColumnIndex = 0;
            
        } else {
            // use previous name to search
            text = lastNameSearched;
            lastColumnIndex += 1;
        }
        
        if (text == null)
            text = "";
        
        Table table = resultsTab.getTableViewer().getTable();
        TableColumn[] columns = table.getColumns();        
        if (columns == null || lastColumnIndex >= columns.length) {
            // no columns or we searched them all..
            lastColumnIndex = 0;
            return false;
        }
        
        boolean columnFound = false;
        TableCursor cursor = resultsTab.getCursor();
        
        // find column
        for (int i = lastColumnIndex; i < columns.length; i++) {
            TableColumn column = columns[i];
            
            if (column.getText().toLowerCase().startsWith(text)) {
                columnFound = true;

                // first scroll all the way to right
                table.showColumn(columns[columns.length - 1]);

                // now back to the column we want, this way it should be
                // the first column visible in most cases
                table.showColumn(column);
                
                // move cursor to found column
                if (table.getItemCount() > 0) {
                    cursor.setSelection(0, i);
                    cursor.setVisible(true);
                }

                // store column index so we can pickup where we left of
                // in case of repeated search
                lastColumnIndex = i;
                
                break;
            }
        }
        
        // reset search to start from start again
        if (!columnFound)
            lastColumnIndex = 0;
        
        return columnFound;
    }
}
