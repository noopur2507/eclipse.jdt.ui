/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.text.java.hover;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

class JavaHoverMessages {

	private static final String RESOURCE_BUNDLE= "org.eclipse.jdt.internal.ui.text.java.hover.JavaHoverMessages";//$NON-NLS-1$

	private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private JavaHoverMessages() {
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}
}
