/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.olat.modules.cl;

import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * This class provides the various modes of checkpoint.
 * 
 * <P>
 * Initial Date:  23.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class CheckpointMode {
	
	/** checkpoint is one time editable by user and always editable by author */
	public final static String MODE_EDITABLE_ONCE = "mode.editable.once";
	/** checkpoint is editable by user and author */
	public final static String MODE_EDITABLE = "mode.editable";
	/** checkpoint is hidden for user but visible and editable for author */
	public final static String MODE_HIDDEN = "mode.hidden";
	/** checkpoint is visible for user but only editable for author */
	public final static String MODE_LOCKED = "mode.visible.lock";

	/**
	 * @return keys as <code>String[]</code>
	 */
	public static String[] getModes() {
		return new String[] {CheckpointMode.MODE_EDITABLE, CheckpointMode.MODE_EDITABLE_ONCE, CheckpointMode.MODE_LOCKED, CheckpointMode.MODE_HIDDEN};
	}
	
	public static String getLocalizedMode(String mode, Translator translator) {
		String localizedMode = translator.translate(mode);
		if(localizedMode.startsWith(Translator.NO_TRANSLATION_ERROR_PREFIX))
			return "";
		else
			return localizedMode;
	}
	
}
