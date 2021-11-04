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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.editor.importnodes;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 3 nov. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportHelper {
	
	private ImportHelper() {
		//
	}
	
	protected static final void errorMessage(AbstractConfigurationRow row, String i18nKey, String[] args,  Translator translator) {
		String htmlMsg = "<div class='o_error'><i class='o_icon o_icon_error'> </i> " + translator.translate(i18nKey, args) + "</div>";
		row.setMessage(concatMessage(row.getMessage(), htmlMsg));
	}
	
	protected static final void warningMessage(AbstractConfigurationRow row, String i18nKey, String[] args, Translator translator) {
		String htmlMsg = "<div class='o_warning'><i class='o_icon o_icon_warning'> </i> " + translator.translate(i18nKey, args) + "</div>";
		row.setMessage(concatMessage(row.getMessage(), htmlMsg));
	}
	
	protected static final void infoMessage(AbstractConfigurationRow row, String i18nKey, String[] args, Translator translator) {
		String htmlMsg = "<div class='o_info'><i class='o_icon o_icon_info'> </i> " + translator.translate(i18nKey, args) + "</div>";
		row.setMessage(concatMessage(row.getMessage(), htmlMsg));
	}
	
	protected static final String concatMessage(String message, String newMessage) {
		if(StringHelper.containsNonWhitespace(message)) {
			message = message.concat(newMessage);
		} else {
			message = newMessage;
		}
		return message;
	}
}
