/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.search;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.ISearchResultViewEntry;
import org.eclipse.search.ui.SearchUI;

/**
 * Sorts the search result viewer by the path name.
 */
public class PathNameSorter extends ViewerSorter {

	private ILabelProvider fLabelProvider;
	
	/*
	 * Overrides method from ViewerSorter
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		String name1= null;
		String name2= null;
		ISearchResultViewEntry entry1= null;
		ISearchResultViewEntry entry2= null;

		if (e1 instanceof ISearchResultViewEntry) {
			entry1= (ISearchResultViewEntry)e1;
			name1= fLabelProvider.getText(e1);
		}
		if (e2 instanceof ISearchResultViewEntry) {
			entry2= (ISearchResultViewEntry)e2;
			name2= fLabelProvider.getText(e2);
		}
		if (name1 == null)
			name1= ""; //$NON-NLS-1$
		if (name2 == null)
			name2= ""; //$NON-NLS-1$
			
		IResource resource= null;
		if (entry1 != null)
			resource= entry1.getResource();
		if (resource != null && entry2 != null && resource == entry2.getResource()) {

			if (resource instanceof IProject || resource.getFileExtension().equalsIgnoreCase("jar") || resource.getFileExtension().equalsIgnoreCase("zip")) //$NON-NLS-2$ //$NON-NLS-1$
				// binary archives
				return getCollator().compare(name1, name2);

			// Sort by marker start position if resource is equal.			
			int startPos1= -1;
			int startPos2= -1;
			IMarker marker1= entry1.getSelectedMarker();
			IMarker marker2= entry2.getSelectedMarker();

			if (marker1 != null)
				startPos1= marker1.getAttribute(IMarker.CHAR_START, -1);
			if (marker2 != null)
			 	startPos2= marker2.getAttribute(IMarker.CHAR_START, -1);
			return startPos1 - startPos2;
		}
		
		return getCollator().compare(name1, name2);
	}

	/*
	 * Overrides method from ViewerSorter
	 */
	public boolean isSorterProperty(Object element, String property) {
		return true;
	}

	/*
	 * Overrides method from ViewerSorter
	 */
	public void sort(Viewer viewer, Object[] elements) {
		// Set label provider to show "path - resource"
		ISearchResultView view= SearchUI.getSearchResultView();
		if (view == null)
			return;
		
		fLabelProvider= view.getLabelProvider();
		if (fLabelProvider instanceof JavaSearchResultLabelProvider)
			((JavaSearchResultLabelProvider)fLabelProvider).setOrder(JavaSearchResultLabelProvider.SHOW_PATH);
		super.sort(viewer, elements);
	}
}

