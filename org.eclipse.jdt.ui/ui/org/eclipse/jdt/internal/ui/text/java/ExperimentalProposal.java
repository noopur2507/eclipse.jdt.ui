/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.text.java;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;

/**
 * An experimental proposal.
 */
public class ExperimentalProposal extends JavaCompletionProposal {

	private int[] fPositionOffsets;
	private int[] fPositionLengths;

	private IRegion fSelectedRegion; // initialized by apply()
		
	/**
	 * Creates a template proposal with a template and its context.
	 */		
	public ExperimentalProposal(String replacementString, int replacementOffset, int replacementLength, Image image,
	    String displayString, int[] positionOffsets, int[] positionLengths, ITextViewer viewer, int relevance)
	{
		super(replacementString, replacementOffset, replacementLength, image, displayString, relevance, viewer);		

		fPositionOffsets= positionOffsets;
		fPositionLengths= positionLengths;
	}

	/*
	 * @see ICompletionProposalExtension#apply(IDocument, char)
	 */
	public void apply(IDocument document, char trigger, int offset) {
		super.apply(document, trigger, offset);

		int replacementOffset= getReplacementOffset();
		String replacementString= getReplacementString();

		if (fPositionOffsets.length > 0 && fTextViewer != null) {
			try {
				LinkedModeModel model= new LinkedModeModel();
				for (int i= 0; i != fPositionOffsets.length; i++) {
					LinkedPositionGroup group= new LinkedPositionGroup();
					group.addPosition(new LinkedPosition(document, replacementOffset + fPositionOffsets[i], fPositionLengths[i], LinkedPositionGroup.NO_STOP));
					model.addGroup(group);
				}
				
				model.forceInstall();
				JavaEditor editor= getJavaEditor();
				if (editor != null) {
					model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
				}
				
				LinkedModeUI ui= new EditorLinkedModeUI(model, fTextViewer);
				ui.setExitPosition(fTextViewer, replacementOffset + replacementString.length(), 0, Integer.MAX_VALUE);
				ui.setDoContextInfo(true);
				ui.enter();
	
				fSelectedRegion= ui.getSelectedRegion();
	
			} catch (BadLocationException e) {
				JavaPlugin.log(e);	
				openErrorDialog(e);
			}		
		} else
			fSelectedRegion= new Region(replacementOffset + replacementString.length(), 0);
	}
	
	/**
	 * Returns the currently active java editor, or <code>null</code> if it 
	 * cannot be determined.
	 * 
	 * @return  the currently active java editor, or <code>null</code>
	 */
	private JavaEditor getJavaEditor() {
		IEditorPart part= JavaPlugin.getActivePage().getActiveEditor();
		if (part instanceof JavaEditor)
			return (JavaEditor) part;
		else
			return null;
	}

	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	public Point getSelection(IDocument document) {
		if (fSelectedRegion == null)
			return new Point(getReplacementOffset(), 0);

		return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
	}

	private void openErrorDialog(BadLocationException e) {
		Shell shell= fTextViewer.getTextWidget().getShell();
		MessageDialog.openError(shell, JavaTextMessages.getString("ExperimentalProposal.error.msg"), e.getMessage()); //$NON-NLS-1$
	}	

}
