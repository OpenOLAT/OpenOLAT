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
package org.olat.admin.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.EmailProperty;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * <pre>
 *
 * Initial Date:  Jul 29, 2003
 *
 * @author gnaegi
 * 
 * Comment:  
 * The user search form
 * </pre>
 */
public class UserSearchForm extends FormBasicController {
	
	private final boolean isAdminProps;
	private final boolean cancelButton;
	private final boolean allowReturnKey;
	private FormLink searchButton;
	
	protected TextElement login;
	protected final List<UserPropertyHandler> userPropertyHandlers;
	protected final Map<String,FormItem> propFormItems = new HashMap<>();
	
	@Autowired
	private UserManager userManager;

	
	/**
	 * @param name
	 * @param cancelbutton
	 * @param isAdmin if true, no field must be filled in at all, otherwise
	 *          validation takes place
	 */
	public UserSearchForm(UserRequest ureq, WindowControl wControl, boolean isAdminProps, boolean cancelButton, boolean allowReturnKey) {
		super(ureq, wControl);
		
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(getClass().getCanonicalName(), isAdminProps);
		
		this.isAdminProps = isAdminProps;
		this.cancelButton = cancelButton;
		this.allowReturnKey = allowReturnKey;
	
		initForm(ureq);
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		return true;
	}
	
	private boolean validateForm(UserRequest ureq) {
		// override for sys admins
		UserSession usess = ureq.getUserSession();
		if (usess != null && usess.getRoles() != null
				&& (usess.getRoles().isAdministrator() || usess.getRoles().isRolesManager())) {
			return true;
		}
		
		boolean filled = !login.isEmpty();
		StringBuilder  full = new StringBuilder(login.getValue().trim());  
		FormItem lastFormElement = login;
		
		// DO NOT validate each user field => see OLAT-3324
		// this are custom fields in a Search Form
		// the same validation logic can not be applied
		// i.e. email must be searchable and not about getting an error like
		// "this e-mail exists already"
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem ui = propFormItems.get(userPropertyHandler.getName());
			if(ui != null) {
				String uiValue = userPropertyHandler.getStringValue(ui);
				// add value for later non-empty search check
				if (StringHelper.containsNonWhitespace(uiValue)) {
					full.append(uiValue.trim());
					filled = true;
				}
	
				lastFormElement = ui;
			}
		}

		// Don't allow searches with * or %  or @ chars only (wild cards). We don't want
		// users to get a complete list of all OLAT users this easily.
		String fullString = full.toString();
		boolean onlyStar= fullString.matches("^[\\*\\s@\\%]*$");

		if (!filled || onlyStar) {
			// set the error message
			lastFormElement.setErrorKey("error.search.form.notempty", null);
			return false;
		}
		if ( fullString.contains("**") ) {
			lastFormElement.setErrorKey("error.search.form.no.wildcard.dublicates", null);
			return false;
		}		
		int minLength = 4;
		if ( fullString.length() < minLength ) {
			lastFormElement.setErrorKey("error.search.form.to.short", null);
			return false;
		}
		
		return true;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_user_search_form");
		
		login = uifactory.addTextElement("login", "search.form.login", 128, "", formLayout);
		login.setVisible(isAdminProps);
		login.setElementCssClass("o_sel_user_search_username");

		Translator tr = Util.createPackageTranslator(UserPropertyHandler.class, getLocale(),  getTranslator());

		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null || (userPropertyHandler.getName().equals(UserConstants.NICKNAME) && !isAdminProps)) {
				continue;
			}
			
			FormItem fi = userPropertyHandler.addFormItem(getLocale(), null, getClass().getCanonicalName(), false, formLayout);
			fi.setTranslator(tr);
			
			// DO NOT validate email field => see OLAT-3324, OO-155, OO-222
			if ((userPropertyHandler instanceof EmailProperty || userPropertyHandler.getName().equals(UserConstants.NICKNAME)) 
					&& fi instanceof TextElement) {
				TextElement textElement = (TextElement)fi;
				textElement.setItemValidatorProvider(null);
			}

			fi.setElementCssClass("o_sel_user_search_".concat(userPropertyHandler.getName().toLowerCase()));
			propFormItems.put(userPropertyHandler.getName(), fi);
		}
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);

		// Don't use submit button, form should not be marked as dirty since this is
		// not a configuration form but only a search form (OLAT-5626)
		searchButton = uifactory.addFormLink("submit.search", buttonGroupLayout, Link.BUTTON);
		searchButton.setElementCssClass("o_sel_user_search_button");
		if (cancelButton) {
			uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchButton) {
			if(validateForm(ureq)) {
				fireEvent (ureq, Event.DONE_EVENT);
			}		
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if(allowReturnKey) {
			fireEvent (ureq, Event.DONE_EVENT);
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void doDispose() {
		//
	}
}