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
package org.olat.login.ui;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.util.StringHelper;
import org.olat.login.validation.ValidationDescription;

/**
 * 
 * Initial date: 14 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LoginUIFactory {
	
	public static String formatDescriptionAsList(List<ValidationDescription> descriptions, Locale locale) {
		return descriptions.stream()
				.map(d -> "<br>  - " + d.getText(locale))
				.collect(Collectors.joining());
	}
	
	static boolean validateInteger(TextElement el, int min) {
		boolean allOk = true;
		el.clearError();
		String val = el.getValue();
		if(StringHelper.containsNonWhitespace(val)) {
			try {
				double value = Integer.parseInt(val);
				if(min > value) {
					el.setErrorKey("error.wrong.int", null);
					allOk = false;
				}
			} catch (NumberFormatException e) {
				el.setErrorKey("error.wrong.int", null);
				allOk = false;
			}
		} else {
			el.setErrorKey("error.wrong.int", null);
			allOk = false;
		}
		return allOk;
	}
}
