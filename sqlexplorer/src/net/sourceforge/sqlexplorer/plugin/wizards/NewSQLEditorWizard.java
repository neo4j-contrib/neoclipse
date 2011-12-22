/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
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
package net.sourceforge.sqlexplorer.plugin.wizards;

import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


public class NewSQLEditorWizard extends Wizard implements INewWizard {

	private NewSQLEditorPage page;
	private IStructuredSelection selection;
	private IWorkbench workbench;

	public NewSQLEditorWizard()
	{
	}

	public void addPages()
	{
		page = new NewSQLEditorPage(workbench, selection);
		addPage(page);
	}

	public void init(IWorkbench iworkbench, IStructuredSelection istructuredselection)
	{
		workbench = iworkbench;
		selection = istructuredselection;
	}

	public boolean performFinish()
	{
		return page.performFinish();
	}

    public Image getDefaultPageImage() {
        return ImageUtil.getImage("Images.WizardLogo");
    }

    public void dispose() {
        super.dispose();
        ImageUtil.disposeImage("Images.WizardLogo");
    }


}
