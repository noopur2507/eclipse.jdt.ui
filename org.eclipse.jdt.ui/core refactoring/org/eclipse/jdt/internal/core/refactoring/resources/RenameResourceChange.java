package org.eclipse.jdt.internal.core.refactoring.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.refactoring.Assert;
import org.eclipse.jdt.internal.core.refactoring.base.ChangeContext;
import org.eclipse.jdt.internal.core.refactoring.base.IChange;
import org.eclipse.jdt.internal.core.refactoring.base.RefactoringStatus;
import org.eclipse.jdt.internal.core.refactoring.AbstractRenameChange;
import org.eclipse.jdt.internal.core.refactoring.*;

public class RenameResourceChange extends AbstractRenameChange {
	
	public RenameResourceChange(IResource resource, String newName) throws JavaModelException {
		this(resource.getFullPath(), resource.getName(), newName);
		Assert.isTrue(!resource.isReadOnly(), "should not be read-only"); 
	}
	
	private RenameResourceChange(IPath resourcePath, String oldName, String newName) {
		super(resourcePath, oldName, newName);
	}
	
	/**
	 * @see AbstractRenameChange#doRename(IProgressMonitor)
	 */
	protected void doRename(IProgressMonitor pm) throws Exception {
		IResource res= getResource();
		IPath path= res.getFullPath().removeLastSegments(1).append(getNewName());
		res.move(path, false, pm);
	}
	
	private IPath createNewPath(){
		return getResourcePath().removeLastSegments(1).append(getNewName());
	}
	
	/**
	 * @see AbstractRenameChange#createUndoChange()
	 */
	protected IChange createUndoChange() throws JavaModelException {
		return new RenameResourceChange(createNewPath(), getNewName(), getOldName());
	}

	/**
	 * @see IChange#getName()
	 */
	public String getName() {
		return "Rename resource " + getOldName() + " to :" + getNewName();
	}

	public RefactoringStatus aboutToPerform(ChangeContext context, IProgressMonitor pm) {
		RefactoringStatus result= super.aboutToPerform(context, pm);

		if (context.getUnsavedFiles().length == 0)
			return result;
		
		checkIfResourceIsUnsaved(getResource(), result, context);
		return result;
	}
}