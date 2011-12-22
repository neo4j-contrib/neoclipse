/*
 * Copyright (C) 2006 Davy Vanherbergen
 * dvanherbergen@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.sqleditor.results.export;

import java.io.File;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sqleditor.results.AbstractResultsTable;
import net.sourceforge.sqlexplorer.sqleditor.results.ResultProvider;
import net.sourceforge.sqlexplorer.sqleditor.results.ResultsTableAction;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

/**
 * Export table contents to a CSV file.
 * @author Davy Vanherbergen
 */
public class ExportAction extends ResultsTableAction {

    private static final ImageDescriptor _image = ImageUtil.getDescriptor("Images.ExportIcon");
    
	private Exporter exporter;

	private ExportOptions exportOptions;

    public ExportAction(Exporter exporter, AbstractResultsTable resultTable)
    {
    	this.exporter = exporter;
    	this.exportOptions = ExportOptions.Current;
    	setResultsTable(resultTable);
    }

	/**
     * Return the text that will be displayed in the context popup menu for this action. 
     */
    public String getText() {
        return Messages.getString("DataSetTable.Actions.Export",exporter.getFormatName());
    }

    /**
     * Provide image for action
     */
    public ImageDescriptor getImageDescriptor() {
        return _image;
    }

    /**
     * Main method. Prompt for file name and save table contents to csv file.
     */
    public void run() 
    {
    	final ExportDlg	dlg = new ExportDlg(Display.getCurrent().getActiveShell(), exporter, exportOptions);
    	if (dlg.open() != Window.OK)
    		return;

        // let's show the fancy wait cursor..
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() 
        {

            public void run() 
            {

                try 
                {
                    // create new file
                    File file = new File(dlg.getFilename());

                    if (file.exists()) 
                    {
                        // overwrite existing files
                        file.delete();
                    }
                    
                    file.createNewFile();

                    // check if there is somethign in our table                    
                    ResultProvider data = getResultsTable().getResultProvider();
                    
                    exporter.export(data, exportOptions, file);

                } 
                catch (final Exception e) 
                {
                	SQLExplorerPlugin.error(e);
                	Display.getCurrent().asyncExec(new Runnable() 
                    {

                        public void run() {
                            MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("SQLResultsView.Error.Export.Title"), e.getMessage());
                        }
                    });
                }
            }
        });
    }

}
