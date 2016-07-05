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

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.user.AbstractUserPropertyHandler;
import org.olat.user.UserImpl;

/**
 * <h3>Description:</h3> The UserNameReadOnlyPropertyHandler allows displaying
 * of the user name. The property is read only and will throw errors when trying
 * to saving values.
 * <p>
 * Initial Date: 04.04.2014 <br>
 * 
 * @author gnaegi, http://www.frentix.com
 */
public class UserNameReadOnlyPropertyHandler extends AbstractUserPropertyHandler {
	
	@Override
	public FormItem addFormItem(Locale locale, User user,
			String usageIdentifyer, boolean isAdministrativeUser,
			FormItemContainer formItemContainer) {
		TextElement tElem = null;
		
		tElem = FormUIFactory.getInstance().addTextElement(getName(), i18nFormElementLabelKey(), 127, getInternalValue(user), formItemContainer);
		tElem.setLabel(i18nFormElementLabelKey(), null);
		// always read-only
		tElem.setEnabled(false);
		return tElem;
	}
	
	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		String val = getInternalValue(user);
		if (val != null) {
			Identity identity = ((UserImpl)user).getIdentity();
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(identity);
			String homepage = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);
			return "<a href='" + homepage + "'>" + StringHelper.escapeHtml(val) + "</a>";			
		}
		return "";
	}

	@Override
	protected String getInternalValue(User user) {
		if (user instanceof UserImpl) {
			Identity identity = ((UserImpl)user).getIdentity();
			return identity.getName();					
		}
		return "";
	}

	@Override
	protected void setInternalValue(User user, String value) {
		throw new UnsupportedOperationException("Can not set user names in the read only property.");
	}


	@Override
	public void updateUserFromFormItem(User user, FormItem formItem) {
		throw new UnsupportedOperationException("Can not update user names by using the read only property.");
	}

	@Override
	public boolean isValid(User user, FormItem formItem,
			Map<String, String> formContext) {
		// read only, always true
		return true;
	}

	@Override
	public boolean isValidValue(User user, String value,
			ValidationError validationError, Locale locale) {
		// read only, always true
		return true;
	}

	@Override
	public String getStringValue(FormItem formItem) {
		return ((TextElement) formItem).getValue();
	}

	@Override
	public String getStringValue(String displayValue, Locale locale) {
		return displayValue;
	}

	@Override
	protected void setInternalGetterSetter(String name) {
		//
	}

	@Override
	public String i18nFormElementLabelKey() {
		return "username";
	}

	@Override
	public String i18nFormElementGroupKey() {
		return "username";
	}

	@Override
	public String i18nColumnDescriptorLabelKey() {
		return "username";
	}

}
