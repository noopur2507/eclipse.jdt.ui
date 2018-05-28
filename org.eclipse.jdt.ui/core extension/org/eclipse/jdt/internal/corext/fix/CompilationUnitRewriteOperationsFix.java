/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Inc. - modified to extend CompilationUnitRewriteOperationsFixCore
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.fix;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.dom.CompilationUnit;

import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;

public class CompilationUnitRewriteOperationsFix extends CompilationUnitRewriteOperationsFixCore implements ILinkedFix {
	
	public abstract static class CompilationUnitRewriteOperation extends CompilationUnitRewriteOperationsFixCore.CompilationUnitRewriteOperation {

		public abstract void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModel linkedModel) throws CoreException;

		@Override
		public void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModelCore linkedModel) throws CoreException {
			rewriteAST(cuRewrite, (LinkedProposalModel)linkedModel);
		}
	}

	public CompilationUnitRewriteOperationsFix(String name, CompilationUnit compilationUnit, CompilationUnitRewriteOperation operation) {
		this(name, compilationUnit, new CompilationUnitRewriteOperation[] { operation });
		Assert.isNotNull(operation);
	}

	public CompilationUnitRewriteOperationsFix(String name, CompilationUnit compilationUnit, CompilationUnitRewriteOperation[] operations) {
		super(name, compilationUnit, operations);
		fLinkedProposalModel= new LinkedProposalModel();
	}

	@Override
	public LinkedProposalModel getLinkedPositions() {
		if (!fLinkedProposalModel.hasLinkedPositions())
			return null;

		return (LinkedProposalModel)fLinkedProposalModel;
	}

}
