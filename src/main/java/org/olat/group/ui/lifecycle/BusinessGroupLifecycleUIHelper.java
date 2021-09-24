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
package org.olat.group.ui.lifecycle;

import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;

/**
 * 
 * Initial date: 24 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupLifecycleUIHelper {
	
	private BusinessGroupLifecycleUIHelper() {
		//
	}
	
	public static boolean validateEmail(TextElement el, boolean mandatory) {
		boolean allOk = true;
		
		el.clearError();
		if (el.isVisible() && el.isEnabled()) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				String[] mails = val.replace(" ", "").split(",");
				for (String mail : mails) {
					if (!MailHelper.isValidEmailAddress(mail)) {
						allOk &= false;
						el.setErrorKey("form.error.nomail", null);
					}
				}
			} else if(mandatory) {
				allOk &= false;
				el.setErrorKey("form.legende.mandatory", null);
			}
		}
		
		return allOk;
	}
	
	/**
	 * 
	 * @param el The text element to validate
	 * @param mandatory If mandatory
	 * @return true if the value is a positive integer
	 */
	public static boolean validateInteger(TextElement el, boolean mandatory) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				try {
					int value = Integer.parseInt(val);
					if(value <= 0) {
						allOk = false;
						el.setErrorKey("form.error.nointeger", null);
					}
				} catch (NumberFormatException e) {
					allOk = false;
					el.setErrorKey("form.error.nointeger", null);
				}
			} else if (mandatory) {
				allOk = false;
				el.setErrorKey("form.legende.mandatory", null);
			}
		}
		return allOk;
	}
}
