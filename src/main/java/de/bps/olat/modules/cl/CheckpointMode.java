/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
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
