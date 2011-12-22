package net.sourceforge.sqlexplorer.sqleditor.results.export;

import java.nio.charset.Charset;
import java.util.SortedMap;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.ResourceManager;

/**
 * Dialog for Exporting.  Modified from the original to a) handle the task of exporting
 * instead of just getting values, and b) use Eclipse 3.2 string externalisation to allow
 * SWT designer etc to present descriptions at design time.
 * 
 * The Export to XLS option has been removed because it was simply outputting an HTML table
 * which Excel could read, and therefore provided little benefit over CSV.  The previous
 * version used flags to indicate which fields were required, but the only difference in
 * usage was that CSV asked for delimiters but HTML and XLS did not.  The dialog now takes
 * responsibility for asking for the desired export format and modifies questions to suit.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * @auther John Spackman <a href="mailto:john.spackman@zenesis.com">john.spackman@zenesis.com</a>
 * 
 */
public class ExportDlg extends TitleAreaDialog {

	private Combo uiCharset;

	private Combo uiDelim;

	private Text uiNullValue;

	private Button uiIncHeaders;

	private Button uiQuoteText;

	private Button uiRtrim;

	private Text uiFile;

	private String file;

	private Exporter exporter;

	private ExportOptions options;

	private static final String[] DELIMS = { ";", "|", "\\t [TAB]", "," }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/**
	 * Create new base dialog.
	 * 
	 * @param parentShell
	 *            Parent's shell.
	 * @param exportOptions 
	 */
	public ExportDlg(Shell parentShell, Exporter exporter, ExportOptions exportOptions ) {
		super(parentShell);
		this.exporter = exporter;
		this.options = exportOptions;
	}

	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		String title = Messages.getString("ExportDlg.Title",exporter.getFormatName());
		setTitle(title);
		getShell().setText(title);
		setMessage(Messages.getString("ExportDlg.TitleMessage", exporter.getFormatName()));
		setTitleImage(ResourceManager.getPluginImage(SQLExplorerPlugin.getDefault(), "icons/ExportDataLarge.png"));
		return contents;
	}

	protected Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new FillLayout(SWT.VERTICAL));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		Label l = null;
		int flags = this.exporter.getFlags();

		if ((flags & Exporter.FMT_CHARSET) != 0 || (flags & Exporter.FMT_DELIM) != 0 || (flags & Exporter.FMT_NULL) != 0) {
	
			Group fmtGroup = new Group(comp, SWT.SHADOW_ETCHED_IN);
			fmtGroup.setText(Messages.getString("ExportDlg.Format"));
			fmtGroup.setLayout(new GridLayout(2, false));
			int i = 0, def = 0;
	
			if ((flags & Exporter.FMT_CHARSET) != 0) {
				if(this.options.characterSet == null)
				{
					this.options.characterSet = "utf-8"; //$NON-NLS-1$
				}
				l = new Label(fmtGroup, SWT.NONE);
				l.setText(Messages.getString("ExportDlg.CharacterSet"));
				uiCharset = new Combo(fmtGroup, SWT.READ_ONLY);
				SortedMap<String,Charset> m = Charset.availableCharsets();
				for (Charset cs : m.values()) 
				{
					uiCharset.add(cs.displayName());
					if (cs.displayName().toLowerCase().equals(this.options.characterSet.toLowerCase())) 
						def = i;
					i++;
				}
				uiCharset.select(def);
			}	
			
			if ((flags & Exporter.FMT_DELIM) != 0) {
				l = new Label(fmtGroup, SWT.NONE);
				l.setText(Messages.getString("ExportDlg.Delimiter"));
				uiDelim = new Combo(fmtGroup, SWT.NONE);
				for (i = 0, def = 0; i < DELIMS.length; i++) {
					uiDelim.add(DELIMS[i]);
					if (DELIMS[i].toLowerCase().equals(this.options.columnSeparator))
						def = i;
				}
				uiDelim.select(def);
			}			
			
			if ((flags & Exporter.FMT_NULL) != 0) {
				l = new Label(fmtGroup, SWT.NONE);
				l.setText(Messages.getString("ExportDlg.NullValue"));
				uiNullValue = new Text(fmtGroup, SWT.SINGLE | SWT.BORDER | SWT.FILL);
				uiNullValue.setText(this.options.nullValue);
				uiNullValue.setLayoutData(new GridData(50, SWT.DEFAULT));
			}
		}
		
		if ((flags & Exporter.OPT_HDR) != 0 || (flags & Exporter.OPT_QUOTE) != 0
				|| (flags & Exporter.OPT_RTRIM) != 0) {
			Group optionsGroup = new Group(comp, SWT.SHADOW_ETCHED_IN);
			optionsGroup.setText(Messages.getString("ExportDlg.Options"));
			optionsGroup.setLayout(new GridLayout(1, true));
	
			if ((flags & Exporter.OPT_HDR) != 0) {
				uiIncHeaders = new Button(optionsGroup, SWT.CHECK);
				uiIncHeaders.setText(Messages.getString("ExportDlg.Headers"));
				uiIncHeaders.setSelection(this.options.includeColumnNames);
			}	

			if ((flags & Exporter.OPT_QUOTE) != 0) {
				uiQuoteText = new Button(optionsGroup, SWT.CHECK);
				uiQuoteText.setText(Messages.getString("ExportDlg.QuoteTextValues"));
				uiQuoteText.setSelection(this.options.quote);
			}	

			if ((flags & Exporter.OPT_RTRIM) != 0) {
				uiRtrim = new Button(optionsGroup, SWT.CHECK);
				uiRtrim.setText(Messages.getString("ExportDlg.RightTrimValues"));
				uiRtrim.setSelection(this.options.rtrim);
			}
		}
		Group fileGroup = new Group(comp, SWT.SHADOW_ETCHED_IN);
		fileGroup.setText(Messages.getString("ExportDlg.Destination"));
		fileGroup.setLayout(new GridLayout(2, false));

		uiFile = new Text(fileGroup, SWT.BORDER | SWT.FILL | SWT.SINGLE);
		uiFile.setLayoutData(new GridData(300, SWT.DEFAULT));
		uiFile.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				sync();
			}

			public void keyReleased(KeyEvent e) {
				sync();
			}
		});
		Button choose = new Button(fileGroup, SWT.NONE);
		choose.setText(Messages.getString("ExportDlg.Choose"));
		choose.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				chooseFilename();
			}
		});

		comp.pack();
		sync();
		return comp;
	}

	protected Control createButtonBar(Composite parent) {
		Control c = super.createButtonBar(parent);
		/*
		 * interupt dialog setup to force Ok button to disabled since filename
		 * field is empty.
		 */
		sync();
		return c;
	}

	/**
	 * Toggle accessibility of Ok button depending on whether all input is
	 * given. This currently only depends on the filename being present.
	 */
	private void sync() {
		String filename = uiFile.getText();
		if (filename == null || filename.trim().length() == 0)
			setErrorMessage(Messages.getString("ExportDlg.DestinationRequired"));
		else
			setErrorMessage(null);
		Button ok = getButton(IDialogConstants.OK_ID);
		if (ok != null)
			ok.setEnabled(filename != null && filename.trim().length() != 0);
	}

	private void chooseFilename() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
		fileDialog.setFilterExtensions(exporter.getFileFilter());

		final String fileName = fileDialog.open();
		if (fileName != null && fileName.trim().length() != 0) {
			uiFile.setText(fileName);
		}
		sync();
	}

	protected void okPressed() {
		this.options.characterSet = uiCharset != null ? uiCharset.getText() : null;
		this.options.columnSeparator = uiDelim != null ? uiDelim.getText() : null;
		this.options.includeColumnNames = uiIncHeaders != null && uiIncHeaders.getSelection();
		this.options.quote = uiQuoteText != null && uiQuoteText.getSelection();
		this.options.rtrim = uiRtrim != null && uiRtrim.getSelection();
		file = uiFile != null ? uiFile.getText() : null;
		this.options.nullValue = uiNullValue != null ? uiNullValue.getText() : null;
		super.okPressed();
	}

	/**
	 * Return chosen filename.
	 * 
	 * @return Filename
	 */
	public String getFilename() {
		return file;
	}
}