/*
 * Licensed to "Neo Technology," Network Engine for Objects in Lund AB
 * (http://neotechnology.com) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at (http://www.apache.org/licenses/LICENSE-2.0). Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.neo4j.neoclipse.action;

import org.eclipse.draw2d.PrintFigureOperation;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.zest.core.widgets.Graph;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action prints the neo graph as it is currently displayed in the graph
 * view.
 * @author Peter H&auml;nsgen
 */
public class PrintGraphAction extends WorkbenchPartAction
{
    /**
     * The constructor.
     */
    public PrintGraphAction( final NeoGraphViewPart view )
    {
        super( view );
    }

    /**
     * Returns true if there are any available printers.
     */
    protected boolean calculateEnabled()
    {
        PrinterData[] printers = Printer.getPrinterList();
        return (printers != null) && (printers.length > 0);
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        NeoGraphViewPart view = (NeoGraphViewPart) getWorkbenchPart();
        // let the user select the print mode
        PrintDialog dialog = new PrintDialog( view.getViewer().getControl()
            .getShell(), SWT.NULL );
        PrinterData data = dialog.open();
        if ( data != null )
        {
            // TODO This is only a temporary implementation, until the Zest
            // Graph supports true
            // scalable printing...
            // print the neo figure
            Graph g = view.getViewer().getGraphControl();
            PrintFigureOperation p = new PrintFigureOperation( new Printer(
                data ), g.getContents() );
            p.setPrintMode( PrintFigureOperation.FIT_PAGE );
            p.run( view.getTitle() );
        }
    }
}
