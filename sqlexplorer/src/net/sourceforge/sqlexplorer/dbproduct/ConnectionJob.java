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
package net.sourceforge.sqlexplorer.dbproduct;

import java.sql.SQLException;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.connections.SessionEstablishedListener;
import net.sourceforge.sqlexplorer.dialogs.PasswordConnDlg;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * Establishes a connection in the background and notifies a listener when complete; takes
 * care of prompting the user for new username/password credentials if the login fails etc
 * @author John Spackman
 */
public class ConnectionJob extends Job {
	
	// The Alias to connect
	private Alias alias;
	
	// The User we're trying to log in
	private User user;
	
	// The callback to give the new session to
	private SessionEstablishedListener listener;
		
	// True if the password dialog was cancelled
	private boolean cancelled;

	/**
	 * Constructor
	 * @param alias Alias to connect
	 * @param user User to establish a connection for (can be null, but if provided user.getAlias() must match alias)
	 * @param listener Callback listener
	 * @param requirePassword Whether to always prompt the user for a password, even if one is already known
	 */
	public ConnectionJob(Alias alias, User user, SessionEstablishedListener listener) {
		super(Messages.getString("Progress.Connection.Title") + " " + alias.getName() + '/' + ((user != null) ? user.getUserName() : "?"));
		this.alias = alias;
		this.user = user;
		if (user != null && user.getAlias() != alias)
			throw new RuntimeException("Invalid User - users alias must match the alias provided");
		this.listener = listener;
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		boolean firstPass = true;

		while (!monitor.isCanceled() && user != null && !cancelled) {
			Session session = null;
			Exception exception = null;

			// Try to login
			try {
				if (!firstPass || user.hasAuthenticated() || alias.isAutoLogon())
					session = user.createSession();
				if (session != null) {
					if (!user.hasAuthenticated()) {
						SQLConnection connection = null;
						try {
							connection = session.grabConnection();
						}catch(SQLException e) {
							user.releaseSession(session);
							session = null;
							throw e;
						} finally {
							if (connection != null)
								session.releaseConnection(connection);
						}
					}
					if (listener != null)
						listener.sessionEstablished(session);
			        return new Status(IStatus.OK, getClass().getName(), IStatus.OK, Messages.getString("Progress.Connection.Success"), null);
				}
			} catch(SQLException e) {
				exception = e;
			}
			if (monitor.isCanceled())
				break;
			
			// Prompt for username and password
			promptForPassword(exception != null ? exception.getMessage() : null);

			// Always need to prompt for the password after the first pass
			firstPass = false;
		}
		
		// Can't do it
		if (listener != null)
			listener.cannotEstablishSession(user);
        return new Status(IStatus.CANCEL, getClass().getName(), IStatus.CANCEL, Messages.getString("Progress.Connection.Cancelled"), null);
	}
	
	/**
	 * Prompts the user for a new username/password to attempt login with; if the dialog is
	 * cancelled then this.user is set to null.
	 * @param message
	 */
	private void promptForPassword(final String message) {
		final Shell shell = SQLExplorerPlugin.getDefault().getSite().getShell();
		
    	// Switch to the UI thread to run the password dialog, but run it synchronously so we
		//	wait for it to complete
    	shell.getDisplay().syncExec(new Runnable() {
            public void run() {
            	if (message != null) {
            		String title = Messages.getString("Progress.Connection.Title") + ' ' + alias.getName();
            		if (user != null && !alias.hasNoUserName())
            			title += '/' + user.getUserName();
            		if (alias.hasNoUserName()) {
	            		MessageDialog dlg = new MessageDialog(shell, title, null, 
	            				Messages.getString("Progress.Connection.ErrorMessage_Part1") + "\n\n" +
	            				message, 
	            				MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0);
	            		dlg.open();
            			cancelled = true;
            			return;
            		} else {
	            		MessageDialog dlg = new MessageDialog(shell, title, null, 
	            				Messages.getString("Progress.Connection.ErrorMessage_Part1") + "\n\n" +
	            				message + "\n\n" + 
	            				Messages.getString("Progress.Connection.ErrorMessage_Part2"), 
	            				MessageDialog.ERROR, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
	            		boolean retry = dlg.open() == 0;
	            		if (!retry) {
	    	            	cancelled = true;
	    	            	return;
	            		}
            		}
            	}
            	
            	Shell shell = Display.getCurrent().getActiveShell();
	            PasswordConnDlg dlg = new PasswordConnDlg(shell, user.getAlias(), user);
	            if (dlg.open() != Window.OK) {
	            	cancelled = true;
	            	return;
	            }
	            
	            // Create a new user and add it to the alias
	            User userTmp = new User(dlg.getUserName(), dlg.getPassword());
	        	userTmp.setAutoCommit(dlg.getAutoCommit());
	        	userTmp.setCommitOnClose(dlg.getCommitOnClose());
	            user = alias.addUser(userTmp);
            }
    	});
	}

	
	/**
	 * Establishes a connection in the background for a brand new set of user credentials
	 * @param alias Alias to connect
	 * @param user User to establish a connection for
	 * @param listener Callback listener
	 */
	public static void createSession(Alias alias, SessionEstablishedListener listener) {
		createSession(alias, null, listener);
	}
	
	/**
	 * Establishes a connection in the background
	 * @param alias Alias to connect
	 * @param user User to establish a connection for
	 * @param listener Callback listener
	 * @param requirePassword Whether to always prompt the user for a password, even if one is already known
	 */
	public static void createSession(Alias alias, User user, SessionEstablishedListener listener) {
        final ConnectionJob bgJob = new ConnectionJob(alias, user, listener);
        final IWorkbenchSite site = SQLExplorerPlugin.getDefault().getSite();
        
        site.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
		        IWorkbenchSiteProgressService siteps = (IWorkbenchSiteProgressService) site.getAdapter(IWorkbenchSiteProgressService.class);
		        siteps.showInDialog(site.getShell(), bgJob);
		        bgJob.schedule();
			}
        });
	}
}
