/*******************************************************************************
 * Copyright (c) 2000, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.internal.ui.text.correction;

import java.util.List;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.ImportEdit;
import org.eclipse.jdt.internal.corext.codemanipulation.MemberEdit;
import org.eclipse.jdt.internal.corext.codemanipulation.NameProposer;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.refactoring.changes.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.textmanipulation.TextRange;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;

public class NewMethodCompletionProposal extends CUCorrectionProposal {

	private ICompilationUnit fCurrCU;
	private IType fDestType;
	private MethodInvocation fNode;

	private MemberEdit fMemberEdit;

	public NewMethodCompletionProposal(String label, MethodInvocation node, ICompilationUnit currCU, IType destType, int relevance) throws CoreException {
		super(label, destType.getCompilationUnit(), !destType.getCompilationUnit().isWorkingCopy(), relevance);
		
		fDestType= destType;
		fCurrCU= currCU;
		fNode= node;
		
		fMemberEdit= null;
	}
	
	private boolean isLocalChange() {
		return fDestType.getCompilationUnit().equals(fCurrCU);
	}
	
	
	/*
	 * @see JavaCorrectionProposal#addEdits(CompilationUnitChange)
	 */
	protected void addEdits(CompilationUnitChange changeElement) throws CoreException {
	
		ICompilationUnit changedCU= changeElement.getCompilationUnit();
		
		CodeGenerationSettings settings= JavaPreferencesSettings.getCodeGenerationSettings();
		ImportEdit importEdit= new ImportEdit(changedCU, settings);

		IMethod currMethod= null;
		IJavaElement elem= fCurrCU.getElementAt(fNode.getStartPosition());
		if (elem != null && elem.getElementType() == IJavaElement.METHOD) {
			currMethod= (IMethod) elem;
		}
		String content= generateStub(importEdit, settings);
		
		int insertPos= MemberEdit.ADD_AT_END;
		IJavaElement anchor= fDestType;
		if (isLocalChange() && currMethod != null) {
			anchor= elem;
			insertPos= MemberEdit.INSERT_AFTER;			
		}
		
		fMemberEdit= new MemberEdit(anchor, insertPos, new String[] { content }, settings.tabWidth);
		fMemberEdit.setUseFormatter(true);
		
		if (!importEdit.isEmpty()) {
			changeElement.addTextEdit("Add imports", importEdit); //$NON-NLS-1$
		}
		changeElement.addTextEdit("Add method", fMemberEdit); //$NON-NLS-1$
		
		if (!isLocalChange()) {
			setElementToOpen(changedCU);
		}
	}
	
	
	private String generateStub(ImportEdit importEdit, CodeGenerationSettings settings) throws CoreException {
		boolean isStatic= false;
		String methodName= fNode.getName().getIdentifier();
		List arguments= fNode.arguments();
		
		StringBuffer buf= new StringBuffer();
		
		boolean isInterface= fDestType.isInterface();
		boolean isSameType= isLocalChange();
		
		ITypeBinding returnType= evaluateMethodType(importEdit);
		String returnTypeName= returnType.getName();
		
		String[] paramTypes= new String[arguments.size()];
		String[] paramNames= new String[arguments.size()];
		
		NameProposer nameProposer= new NameProposer();
		for (int i= 0; i < paramTypes.length; i++) {
			Expression expr= (Expression) arguments.get(i);
			ITypeBinding binding= evaluateParameterType(expr, importEdit);
			paramTypes[i]= (binding != null) ? binding.getName() : "Object";
			paramNames[i]= nameProposer.proposeParameterName(paramTypes[i]);
		}
		
		if (settings.createComments) {
			
			StubUtility.genJavaDocStub("Method " + methodName, paramNames, Signature.createTypeSignature(returnTypeName, true), null, buf); //$NON-NLS-1$
		}
		
		if (isSameType) {
			buf.append("private "); //$NON-NLS-1$
		} else if (!isInterface) {
			buf.append("public "); //$NON-NLS-1$
		}
		
		if (isStatic) {
			buf.append("static "); //$NON-NLS-1$
		}
		
		buf.append(returnTypeName);
		buf.append(' ');
		buf.append(methodName);
		buf.append('(');
		
		if (!arguments.isEmpty()) {
			for (int i= 0; i < arguments.size(); i++) {
				if (i > 0) {
					buf.append(", "); //$NON-NLS-1$
				}
				buf.append(paramTypes[i]);
				buf.append(' ');
				buf.append(paramNames[i]);
			}
		}
		buf.append(')');
		if (isInterface) {
			buf.append(";\n");  //$NON-NLS-1$
		} else {
			buf.append("{\n"); //$NON-NLS-1$
	
			if (!returnType.isPrimitive()) {
				buf.append("return null;\n"); //$NON-NLS-1$
			} else if (returnTypeName.equals("boolean")) { //$NON-NLS-1$
				buf.append("return false;\n"); //$NON-NLS-1$
			} else if (!returnTypeName.equals("void")) { //$NON-NLS-1$
				buf.append("return 0;\n"); //$NON-NLS-1$
			}
			buf.append("}\n"); //$NON-NLS-1$
		}
		return buf.toString();
	}
	
	private ITypeBinding evaluateMethodType(ImportEdit importEdit) {
		ITypeBinding binding= ASTResolving.getTypeBinding(fNode);
		if (binding != null) {
			ITypeBinding baseType= binding.isArray() ? binding.getElementType() : binding;
			if (!baseType.isPrimitive()) {
				importEdit.addImport(Bindings.getFullyQualifiedName(baseType));
			}
			return binding;
		}
		return fNode.getAST().resolveWellKnownType("void"); //$NON-NLS-1$
	}
	
	private ITypeBinding evaluateParameterType(Expression expr, ImportEdit importEdit) {
		ITypeBinding binding= expr.resolveTypeBinding();
		if (binding != null) {
			ITypeBinding baseType= binding.isArray() ? binding.getElementType() : binding;
			if (!baseType.isPrimitive()) {
				importEdit.addImport(Bindings.getFullyQualifiedName(baseType));
			}
		}
		return binding;
	}
	
		
	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
	}

	/* (non-Javadoc)
	 * @see ChangeCorrectionProposal#getRangeToReveal()
	 */
	protected TextRange getRangeToReveal() throws CoreException {
		CompilationUnitChange change= getCompilationUnitChange();
		return change.getNewTextRange(fMemberEdit);
	}

}
