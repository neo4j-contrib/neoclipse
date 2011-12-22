/**
 * 
 */
package net.sourceforge.sqlexplorer.dbdetail.tab;

import net.sourceforge.sqlexplorer.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * @author k709335
 *
 */
public abstract class AbstractSourceTab extends AbstractTab {

    private String _source = null;
    
    private Text _viewer = null;
    
    public final void fillDetailComposite(Composite parent) {

        if (_source == null) {
            _source = getSource();
        }

        Composite composite = new Composite(parent, SWT.FILL);
        
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;                
        layout.marginLeft = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        
        composite.setLayout(layout);
        composite.setLayoutData(gridData);
        
        
        _viewer = new Text(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.WRAP);
        if (_source != null) {
            _viewer.setText(_source);
        } else {
            _viewer.setText("");
        }
        _viewer.setLayoutData(gridData);
        
        // add status bar labels
        String info = getStatusMessage();
        if (info == null) {
            info = "";
        }
        Label infoLabel = new Label(composite, SWT.NULL);
        infoLabel.setText(info);
        infoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.NULL, true, false));
    }

    public String getLabelText() {
        return Messages.getString("DatabaseDetailView.Tab.Source");
    }

    public abstract String getSource();
    
    
    public final void refresh() {
       _source = null;
    }
    
    public String getStatusMessage() {
        return Messages.getString("DatabaseDetailView.Tab.SourceFor") + " " + getNode().getQualifiedName();
    }

}
