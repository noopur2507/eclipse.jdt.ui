/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.jarpackager;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class JarPackagerMessages {

	private static final String RESOURCE_BUNDLE= JarPackagerMessages.class.getName();

	private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

	/*
	 * Class has no instances 
	 */
	private JarPackagerMessages() {
	}
	/**
	 * Gets a string from the resource bundle
	 * 
	 * @param key	the string used to get the bundle value, must not be null
	 */	
	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}
	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 * 
	 * @param key	the string used to get the bundle value, must not be null
	 */
	public static String getFormattedString(String key, Object arg) {
		String format= null;
		try {
			format= fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
		if (arg == null)
			arg= ""; //$NON-NLS-1$
		return MessageFormat.format(format, new Object[] { arg });
	}
	/**
	 * Gets a string from the resource bundle and formats it with the arguments
	 * 
	 * @param key	the string used to get the bundle value, must not be null
	 */
	public static String getFormattedString(String key, Object arg1, Object arg2) {
		String format= null;
		try {
			format= fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
		if (arg1 == null)
			arg1= ""; //$NON-NLS-1$
		if (arg2 == null)
			arg2= ""; //$NON-NLS-1$
		return MessageFormat.format(format, new Object[] { arg1, arg2 });
	}
	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 * 
	 * @param key	the string used to get the bundle value, must not be null
	 */
	public static String getFormattedString(String key, boolean arg) {
		String format= null;
		try {
			format= fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
		return MessageFormat.format(format, new Object[] { new Boolean(arg) });
	}
}
