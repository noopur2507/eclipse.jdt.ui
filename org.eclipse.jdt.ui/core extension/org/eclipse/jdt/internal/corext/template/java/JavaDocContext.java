/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.corext.template.java;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.internal.corext.template.ContextType;
import org.eclipse.jdt.internal.corext.template.Template;
import org.eclipse.jdt.internal.corext.template.TemplateBuffer;
import org.eclipse.jdt.internal.corext.template.TemplateTranslator;

/**
 * A context for javadoc.
 */
public class JavaDocContext extends CompilationUnitContext {

	// tags
	private static final char HTML_TAG_BEGIN= '<';
	private static final char HTML_TAG_END= '>';
	private static final char JAVADOC_TAG_BEGIN= '@';	

	/**
	 * Creates a javadoc template context.
	 * 
	 * @param type   the context type.
	 * @param document the document.
	 * @param completionPosition the completion position within the document.
	 * @param unit the compilation unit (may be <code>null</code>).
	 */
	public JavaDocContext(ContextType type, IDocument document, int completionPosition,
		ICompilationUnit compilationUnit)
	{
		super(type, document, completionPosition, compilationUnit);
	}

	/*
	 * @see DocumentTemplateContext#getStart()
	 */ 
	public int getStart() {
		try {
			IDocument document= getDocument();
			int start= getCompletionPosition();
	
			if ((start != 0) && (document.getChar(start - 1) == HTML_TAG_END))
				start--;
	
			while ((start != 0) && Character.isUnicodeIdentifierPart(document.getChar(start - 1)))
				start--;
			
			if ((start != 0) && Character.isUnicodeIdentifierStart(document.getChar(start - 1)))
				start--;
	
			// include html and javadoc tags
			if ((start != 0) && (
				(document.getChar(start - 1) == HTML_TAG_BEGIN) ||
				(document.getChar(start - 1) == JAVADOC_TAG_BEGIN)))
			{
				start--;
			}	
	
			return start;

		} catch (BadLocationException e) {
			return getCompletionPosition();	
		}
	}

	/*
	 * @see TemplateContext#canEvaluate(Template templates)
	 */
	public boolean canEvaluate(Template template) {
		return template.matches(getKey(), getContextType().getName());
	}

	/*
	 * @see TemplateContext#evaluate(Template)
	 */
	public TemplateBuffer evaluate(Template template) throws CoreException {
		TemplateTranslator translator= new TemplateTranslator();
		TemplateBuffer buffer= translator.translate(template.getPattern());

		getContextType().edit(buffer, this);
			
		return buffer;
	}

}

