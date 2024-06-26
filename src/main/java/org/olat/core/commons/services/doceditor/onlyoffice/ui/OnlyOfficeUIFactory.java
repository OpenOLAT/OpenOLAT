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
package org.olat.core.commons.services.doceditor.onlyoffice.ui;

import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 12.04.2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class OnlyOfficeUIFactory {
	
	static boolean validateIsMandatory(TextElement el) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String value = el.getValue();
			if (!StringHelper.containsNonWhitespace(value)) {
				el.setErrorKey("form.mandatory.hover");
				allOk = false;
			}
		}
		return allOk;
	}
	
	public static boolean validatePositiveInteger(TextElement el) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				try {
					int intVal = Integer.parseInt(val);
					if (intVal < 0) {
						el.setErrorKey("error.positive.integer");
					}
				} catch (NumberFormatException e) {
					el.setErrorKey("error.positive.integer");
					allOk = false;
				}
			}
		}
		return allOk;
	}

}
