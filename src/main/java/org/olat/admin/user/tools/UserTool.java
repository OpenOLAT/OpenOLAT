package org.olat.admin.user.tools;

import java.util.Locale;

import org.olat.core.extensions.action.GenericActionExtension;

/**
 * 
 * Initial date: 16.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserTool {
	private final String key;
	private final String label;
	
	public UserTool(GenericActionExtension gAe, Locale locale) {
		key = gAe.getUniqueExtensionID();
		label = gAe.getActionText(locale);
	}

	public String getKey() {
		return key;
	}

	public String getLabel() {
		return label;
	}
}
