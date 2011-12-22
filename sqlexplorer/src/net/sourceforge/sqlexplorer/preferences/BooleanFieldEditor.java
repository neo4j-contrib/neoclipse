package net.sourceforge.sqlexplorer.preferences;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class BooleanFieldEditor extends
		org.eclipse.jface.preference.BooleanFieldEditor {
	
	private Button checkbox;

	public BooleanFieldEditor() {
		super();
	}

	public BooleanFieldEditor(String name, String label, Composite parent) {
		super(name, label, parent);
	}

	public BooleanFieldEditor(String name, String labelText, int style, Composite parent) {
		super(name, labelText, style, parent);
	}

	@Override
	protected Button getChangeControl(Composite parent) {
		checkbox = super.getChangeControl(parent);
		return checkbox;
	}

	public Button getCheckbox() {
		return checkbox;
	}

}
