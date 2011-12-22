/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
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
package net.sourceforge.sqlexplorer.dialogs;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.Collator;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.sqlexplorer.util.URLUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;

@SuppressWarnings("restriction")
public class AboutDlg extends Dialog {

    public AboutDlg(Shell parentShell) {
        super(parentShell);
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.getString("AboutDialog.Title")); //$NON-NLS-1$
    }

    protected Control createDialogArea(Composite parent) {
        // top level composite
        Composite parentComposite = (Composite) super.createDialogArea(parent);

        parentComposite.setLayout(new FillLayout());

        TabFolder tabFolder = new TabFolder(parentComposite, SWT.NULL);

        TabItem tabItem1 = new TabItem(tabFolder, SWT.NULL);
        tabItem1.setText(Messages.getString("AboutDialog.Tab.About"));
        tabItem1.setToolTipText(Messages.getString("AboutDialog.Tab.AboutToolTip"));

        TabItem tabItem2 = new TabItem(tabFolder, SWT.NULL);
        tabItem2.setText(Messages.getString("AboutDialog.Tab.Credits"));
        tabItem2.setToolTipText(Messages.getString("AboutDialog.Tab.CreditsToolTip"));

        TabItem tabItem3 = new TabItem(tabFolder, SWT.NULL);
        tabItem3.setText(Messages.getString("AboutDialog.Tab.License"));
        tabItem3.setToolTipText(Messages.getString("AboutDialog.Tab.LicenseToolTip"));
        
        TabItem tabItem4 = new TabItem(tabFolder, SWT.NULL);
        tabItem4.setText(Messages.getString("AboutDialog.Tab.System"));
        tabItem4.setToolTipText(Messages.getString("AboutDialog.Tab.SystemToolTip"));

        new AboutItem(tabItem1, tabFolder);
        new CreditsItem(tabItem2, tabFolder);
        new LicenseItem(tabItem3, tabFolder);        
        new SystemProperties(tabItem4, tabFolder);
        return parentComposite;
    }

    protected Point getInitialSize() {
        return new Point(455, 380);
    }

    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(newShellStyle | SWT.RESIZE);// Make the about dialog
                                                        // resizable
    }
}

class AboutItem {

    Image logoImage;

    AboutItem(TabItem item, Composite parent) {

        logoImage = ImageUtil.getImage("Images.Logo");
        

        parent.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent event) {
                ImageUtil.disposeImage("Images.Logo");
            }
        });
        
        Composite cmp = new Composite(parent, SWT.NULL);
        item.setControl(cmp);
        GridLayout lay = new GridLayout();
        lay.numColumns = 1;
        lay.marginWidth = 15;
        lay.marginHeight = 15;
        cmp.setLayout(lay);

        Label lb = new Label(cmp, SWT.NULL);
        lb.setText(Messages.getString("AboutDialog.About.copyright"));

        GridData data = new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.CENTER);
        lb.setLayoutData(data);
        lb.setSize(SWT.DEFAULT, 50);

        ImageData imgData = logoImage.getImageData();
        int width = imgData.width;
        int height = imgData.height;

        final Composite imgComposite = new Composite(cmp, SWT.BORDER);
        data = new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.CENTER);
        imgComposite.setLayoutData(data);
        data.heightHint = height;
        data.widthHint = width;

   
        final Color imageBackgroundColor = new Color(parent.getDisplay(), 255, 255, 255);
        final Color fontColor = new Color(parent.getDisplay(), 102, 118, 145);
        
        final String version = Messages.getString("AboutDialog.About.versionPrefix") + SQLExplorerPlugin.getDefault().getVersion();        
        imgComposite.addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent event) {
                GC gc = event.gc;
                gc.drawImage(logoImage, 0, 0);
                gc.setBackground(imageBackgroundColor);
                gc.setForeground(fontColor);
                gc.drawText(version, 140, 80);
            }
        });

        Link link = new Link(cmp, SWT.CENTER);
        link.setText(Messages.getString("AboutDialog.About.url"));
        data = new GridData(SWT.FILL);
        
        
        link.setLayoutData(data);
        link.setForeground(fontColor);
        link.addListener (SWT.Selection, new Listener () {
            public void handleEvent(Event event) {

                try {
                	@SuppressWarnings("restriction")
                    IWebBrowser browser = WorkbenchBrowserSupport.getInstance().getExternalBrowser();
                    browser.openURL(new URL(event.text));
                } catch (Exception e) {
                    SQLExplorerPlugin.error("Error launching browser", e); //$NON-NLS-1$
                }
            }
        });
    }
}

class LicenseItem {

    LicenseItem(TabItem item, Composite parent) {
        Composite cmp = new Composite(parent, SWT.NULL);
        item.setControl(cmp);
        GridLayout lay = new GridLayout();
        lay.numColumns = 1;
        cmp.setLayout(lay);

        StyledText st = new StyledText(cmp, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        st.setEditable(false);
        String separator = System.getProperty("line.separator"); //$NON-NLS-1$

        BufferedReader bbr = null;
        try {

            InputStream is = URLUtil.getResourceURL("license.txt").openStream(); //$NON-NLS-1$
            bbr = new BufferedReader(new InputStreamReader(is));

            String str;
            StringBuffer all = new StringBuffer();
            while ((str = bbr.readLine()) != null) {
                all.append(str);
                all.append(separator);
            }
            st.setText(all.toString());
            is.close();

        } catch (Exception e) {
            st.setText(Messages.getString("AboutDialog.License")); //$NON-NLS-1$
        } finally {
            try {
                if (bbr != null)
                    bbr.close();
            } catch (java.io.IOException e) {
            }

        }

        GridData data = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_CENTER | GridData.CENTER);
        st.setLayoutData(data);
    }
}

class CreditsItem {

    CreditsItem(TabItem item, Composite parent) {
        Composite cmp = new Composite(parent, SWT.NULL);
        item.setControl(cmp);
        GridLayout lay = new GridLayout();
        lay.numColumns = 1;
        cmp.setLayout(lay);

        StyledText st = new StyledText(cmp, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        st.setEditable(false);
        String separator = System.getProperty("line.separator"); //$NON-NLS-1$

        final String credits =    
            "Developers (version 3.6.1):" + separator +
            " - Heiko Hilbert" + separator + 
            " - Vladimir Rüntü" + separator + 
            " - John Spackman" + separator + 
            " - Davy Vanherbergen" + separator + 
            separator +              
            "Developers (version 3.5.1):" + separator +
            " - Heiko Hilbert" + separator + 
            " - joco01" + separator + 
            " - pat19" + separator +
            " - André Schulze" + separator +
            separator +              
            "Developers (version 3.5.0.RC8):" + separator +
            " - Heiko Hilbert" + separator + 
            " - a_lazar" + separator + 
            " - joco01" + separator + 
            " - seekforth" + separator + 
            " - sishi" + separator + 
            separator +              
            "Developers (version 3.5.0.RC6):" + separator +
            " - John Spackman (Zenesis Limited - www.zenesis.com)" + separator + 
            " - Heiko Hilbert" + separator + 
            separator +              
            "Developers (version 3.5.0):" + separator +
            " - John Spackman (Zenesis Limited - www.zenesis.com)" + separator + 
            separator +              
            "Developers (version 3.0.0):" + separator +
            " - Davy Vanherbergen" + separator + 
            separator +              
            "Developers (version 2.2.5 (never released)):" + separator +
            " - Alexandre Luti Telles" + separator +
            " - Davy Vanherbergen" + separator + 
            separator +        
            "Previous Developers (versions 2.2.3 and 2.2.4):" + separator +
            " - Alexandre Luti Telles" + separator +            
            " - Gert Wohlgemuth" + separator +            
            separator +        
            "Other Contributors (versions 2.2.2 and before):" + separator +
            " - Andrea Mazzolini (original version of JFacedb)" + separator +
            " - Johan Compagner" + separator +
            " - Jouneau Luc" + separator +
            " - Stephen Schaub" + separator +
            " - Chris Potter (Sybase plugin, Sql Server plugin)" + separator +
            " - Joao Reis Belo (Sql Server plugin)" + separator + 
            separator +        
            "The SQL stuff is based on SquirreL SQL (http://squirrel-sql.sourceforge.net)." + separator +
            separator+
            "SQLExplorer uses the following libraries too:" + separator +
            " - NanoXML (http://NanoXML.sourceforge.net/) Java XML API" + separator +
            " - log4j (http://jakarta.apache.org/log4j) Logging API" + separator;

        st.setText(credits);
        GridData data = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_CENTER | GridData.CENTER);
        st.setLayoutData(data);
    }
}

class SystemProperties {

    private class LProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object arg0, int arg1) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            java.util.Map.Entry<?,?> cp = (java.util.Map.Entry<?,?>) element;
            if (columnIndex == 0)
                return cp.getKey().toString();
            else
                return cp.getValue().toString();
        }
    }

    java.util.Properties props;

    SystemProperties(TabItem itemTab, Composite parent) {
        props = System.getProperties();

        TableViewer tv = new TableViewer(parent, SWT.NULL);
        tv.setSorter(new MyViewerSorter());
        Table table = tv.getTable();
        TableColumn c1 = new TableColumn(table, SWT.NULL);
        c1.setText(Messages.getString("Property_9")); //$NON-NLS-1$
        TableColumn c2 = new TableColumn(table, SWT.NULL);
        c2.setText(Messages.getString("Value_10")); //$NON-NLS-1$
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        TableLayout tableLayout = new TableLayout();
        for (int i = 0; i < 2; i++)
            tableLayout.addColumnData(new ColumnWeightData(1, 50, true));
        table.setLayout(tableLayout);

        itemTab.setControl(tv.getControl());
        tv.setContentProvider(new IStructuredContentProvider() {

            public Object[] getElements(Object input) {
                return props.entrySet().toArray();
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object arg1, Object arg2) {
            }
        });
        tv.setLabelProvider(new LProvider());
        tv.setInput(this);
    }

}

class MyViewerSorter extends ViewerSorter {

    public MyViewerSorter() {
        super();
    }

    public MyViewerSorter(Collator collator) {
        super(collator);
    }

    public boolean isSorterProperty(Object element, String propertyId) {
        return true;
    }
}





