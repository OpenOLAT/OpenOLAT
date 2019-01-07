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
package org.olat.modules.edusharing.ui;

import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 20 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingUIFactory {
	
	public static int toIntOrZero(String value) {
		if (StringHelper.containsNonWhitespace(value)) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				// return default value
			}
		}
		return 0;
	}
	
	public static boolean validateIsMandatory(TextElement el) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String value = el.getValue();
			if (!StringHelper.containsNonWhitespace(value)) {
				el.setErrorKey("form.mandatory.hover", null);
				allOk = false;
			}
		}
		return allOk;
	}
	
	public static boolean validateInteger(TextElement el, int min, int max) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				
				try {
					int value = Integer.parseInt(val);
					if(min > value) {
						el.setErrorKey("error.wrong.number", null);
						allOk = false;
					} else if(max < value) {
						el.setErrorKey("error.wrong.number", null);
						allOk = false;
					}
				} catch (NumberFormatException e) {
					el.setErrorKey("error.wrong.number", null);
					allOk = false;
				}
			}
		}
		return allOk;
	}

}
