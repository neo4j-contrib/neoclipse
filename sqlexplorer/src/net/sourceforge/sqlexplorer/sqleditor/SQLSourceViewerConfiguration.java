package net.sourceforge.sqlexplorer.sqleditor;

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

//import org.eclipse.jdt.internal.ui.text.HTMLTextPresenter;
import net.sourceforge.sqlexplorer.IConstants;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;


public class SQLSourceViewerConfiguration extends SourceViewerConfiguration {
	
	InformationPresenter iPresenter;
	private SQLTextTools fSQLTextTools;
	private IDocumentPartitioner docPartitioner;
	
	private DefaultTextDoubleClickStrategy defaultTextDoubleClickStrategy=new DefaultTextDoubleClickStrategy();
	
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer,
													   String contentType){
		//System.out.println("double click");
		return defaultTextDoubleClickStrategy;
		                                                       	
	}
	
	/**
	 * Creates a new SQL source viewer configuration for viewers in the given editor 
	 * using the given SQLTextTools.
	 *
	 * @param tools the SQL tools to be used
	 */
	public SQLSourceViewerConfiguration(SQLTextTools tools) {
		fSQLTextTools= tools;
		docPartitioner=tools.createDocumentPartitioner();
	}
	public IDocumentPartitioner getDocumentPartitioner(){return docPartitioner;}
	

	public RuleBasedScanner getCodeScanner() {
		return fSQLTextTools.getCodeScanner();
	}
	

	protected RuleBasedScanner getMultilineCommentScanner() {
		return fSQLTextTools.getMultilineCommentScanner();
	}
	

	protected RuleBasedScanner getSinglelineCommentScanner() {
		return fSQLTextTools.getSinglelineCommentScanner();
	}
	

	protected RuleBasedScanner getStringScanner() {
		return fSQLTextTools.getStringScanner();
	}
	
	

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {

		
		PresentationReconciler reconciler= new PresentationReconciler();

		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr= new DefaultDamagerRepairer(getMultilineCommentScanner());		
		reconciler.setDamager(dr, IConstants.SQL_COLOR_MULTILINE_COMMENT);
		reconciler.setRepairer(dr, IConstants.SQL_COLOR_MULTILINE_COMMENT);

		dr= new DefaultDamagerRepairer(getSinglelineCommentScanner());		
		reconciler.setDamager(dr, IConstants.SQL_COLOR_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, IConstants.SQL_COLOR_SINGLE_LINE_COMMENT);
		
		dr= new DefaultDamagerRepairer(getStringScanner());
		reconciler.setDamager(dr, IConstants.SQL_COLOR_STRING);
		reconciler.setRepairer(dr, IConstants.SQL_COLOR_STRING);
		
		return reconciler;
	}

	public int getTabWidth(ISourceViewer sourceViewer) {
		return 4;
	}
	
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		
			ContentAssistant assistant= new ContentAssistant(){
				public void uninstall(){
					//System.out.println("Uninstalling content assistant");
					SQLCompletionProcessor p1=(SQLCompletionProcessor)getContentAssistProcessor(IConstants.SQL_COLOR_STRING);
					SQLCompletionProcessor p2=(SQLCompletionProcessor)getContentAssistProcessor(IConstants.SQL_COLOR_SINGLE_LINE_COMMENT);
					p1.dispose();
					p2.dispose();
					super.uninstall();
				}
			};	
			
			SQLCompletionProcessor processor= new SQLCompletionProcessor(fSQLTextTools.getDictionary());
			assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
				// Register the same processor for strings and single line comments to get code completion at the start of those partitions.
			assistant.setContentAssistProcessor(processor, IConstants.SQL_COLOR_STRING);
			assistant.setContentAssistProcessor(processor, IConstants.SQL_COLOR_SINGLE_LINE_COMMENT);
			assistant.enableAutoActivation(true);

			assistant.setAutoActivationDelay(500);
			assistant.enableAutoInsert(true);
			assistant.enableAutoActivation(true);
			processor.setCompletionProposalAutoActivationCharacters(".".toCharArray()); //$NON-NLS-1$
			
			/*assistant.setProposalSelectorForeground(c);
		c= getColor(store, PROPOSALS_BACKGROUND, manager);
		assistant.setProposalSelectorBackground(c);
		
		c= getColor(store, PARAMETERS_FOREGROUND, manager);
		assistant.setContextInformationPopupForeground(c);
		assistant.setContextSelectorForeground(c);
		
		c= getColor(store, PARAMETERS_BACKGROUND, manager);
		assistant.setContextInformationPopupBackground(c);
		assistant.setContextSelectorBackground(c);*/

			//ContentAssistPreference.configure(assistant, getPreferenceStore());
			
			assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
			assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
					
			return assistant;
		
	}


	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getInformationPresenter(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
		if(iPresenter==null){
			IInformationControlCreator informationControlCreator= new IInformationControlCreator() {
						public IInformationControl createInformationControl(Shell parent) {
							//boolean cutDown= false;
							//int style= cutDown ? SWT.NONE : (SWT.V_SCROLL | SWT.H_SCROLL);
							return new DefaultInformationControl(parent);//, SWT.RESIZE,, style, new HTMLTextPresenter(cutDown));
						}
					};

			iPresenter=new InformationPresenter(informationControlCreator);
			iPresenter.setSizeConstraints(60, 10, true, true);		

		}
		return iPresenter;

	}

}
