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
package org.olat.course.nodes.livestream.ui;

import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 5 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class LiveStreamUIFactory {

	static boolean validateMandatory(TextElement el) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if (!StringHelper.containsNonWhitespace(val)) {
				el.setErrorKey("form.mandatory.hover", null);
				allOk = false;
			}
		}
		return allOk;
	}

	
	static boolean validateInteger(TextElement el, boolean mandatory) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if (StringHelper.containsNonWhitespace(val)) {
				try {
					Integer.parseInt(val);
				} catch (NumberFormatException e) {
					el.setErrorKey("form.error.wrong.int", null);
					allOk = false;
				}
			} else if (mandatory) {
				el.setErrorKey("form.mandatory.hover", null);
				allOk = false;
			}
		}
		return allOk;
	}

}
