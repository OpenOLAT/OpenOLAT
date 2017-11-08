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
package org.olat.user.propertyhandlers;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 10.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GenericUnique127CharTextPropertyHandler extends Generic127CharTextPropertyHandler {

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String, String> formContext) {
		boolean allOk = super.isValid(user, formItem, formContext);
		
		if(formItem instanceof TextElement) {
			String value = ((TextElement)formItem).getValue();
			if(!isUnique(user, value)) {
				List<Identity> found = UserManager.getInstance().findIdentitiesWithProperty(getName(), value);
				Identity propId = null;
				if(found.size() > 0) {
					// only display first one 
					propId = found.get(0);
					String username = propId.getName();
					formItem.setErrorKey("general.error.unique", new String[]{ username });
					allOk &= false;
				}
			}
		}

		return allOk;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		boolean allOk = super.isValidValue(user, value, validationError, locale);

		if(!isUnique(user, value)) {
			List<Identity> found = UserManager.getInstance().findIdentitiesWithProperty(getName(), value);
			Identity propId = null;
			if(found.size() > 0) {
				// only display first one 
				propId = found.get(0);
				String username = propId.getName();
				validationError.setErrorKey("general.error.unique");
				validationError.setArgs(new String[]{ username });
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private boolean isUnique(User user, String value) {
		boolean allOk = true;
		if(StringHelper.containsNonWhitespace(value)) {
			List<Long> usedBy = UserManager.getInstance().findUserKeyWithProperty(getName(), value);
			if(user == null || user.getKey() == null) {
				allOk &= usedBy.isEmpty();
			} else {
				allOk &= usedBy.isEmpty() || usedBy.contains(user.getKey());
			}
		}
		return allOk;
	}
}
