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
package org.olat.modules.coach.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.EmailProperty;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSearchForm extends FormBasicController {
	
	private static final String PROPS_IDENTIFIER = UserSearchForm.class.getName();
	private static final String[] activeKeys = new String[] { "on" };
	private static final String[] activeValues = new String[] { "" };
	
	private final boolean adminProps;
	private FormLink searchButton;
	
	private TextElement login;
	private MultipleSelectionElement statusEl;
	private List<UserPropertyHandler> userPropertyHandlers;
	private final Map<String,FormItem> propFormItems = new HashMap<>();
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	/**
	 * @param name
	 * @param cancelbutton
	 * @param isAdmin if true, no field must be filled in at all, otherwise
	 *          validation takes place
	 */
	public UserSearchForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		adminProps = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
	
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		login = uifactory.addTextElement("login", "search.form.login", 128, "", formLayout);
		login.setVisible(adminProps);
		
		List<UserPropertyHandler> allPropertyHandlers = userManager.getUserPropertyHandlersFor(PROPS_IDENTIFIER, adminProps);
		userPropertyHandlers = allPropertyHandlers.stream()
			.filter(prop -> !UserConstants.NICKNAME.equals(prop.getName()))
			.collect(Collectors.toList());

		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler != null) {
				FormItem fi = userPropertyHandler.addFormItem(getLocale(), null, getClass().getCanonicalName(), false, formLayout);
				fi.setMandatory(false);
				// DO NOT validate email field => see OLAT-3324, OO-155, OO-222
				if (userPropertyHandler instanceof EmailProperty && fi instanceof TextElement) {
					TextElement textElement = (TextElement)fi;
					textElement.setItemValidatorProvider(null);
				}
	
				propFormItems.put(userPropertyHandler.getName(), fi);
			}
		}
		
		statusEl = uifactory.addCheckboxesHorizontal("only.active", formLayout, activeKeys, activeValues);
		statusEl.select(activeKeys[0], true);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("search", buttonCont);
	}
	
	public String getLogin() {
		return login.isVisible() && StringHelper.containsNonWhitespace(login.getValue())
				? login.getValue() : null;
	}
	
	public Map<String,String> getSearchProperties() {
		Map<String, String> userPropertiesSearch = new HashMap<>();				
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler != null) {
				FormItem ui = propFormItems.get(userPropertyHandler.getName());
				String uiValue = userPropertyHandler.getStringValue(ui);
				if(userPropertyHandler.getName().startsWith("genericCheckboxProperty")) {
					if(!"false".equals(uiValue)) {
						userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
					}
				} else if (StringHelper.containsNonWhitespace(uiValue) && !uiValue.equals("-")) {
					userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
				}
			}
		}
		return userPropertiesSearch;
	}
	
	public boolean isOnlyActive() {
		return statusEl.isAtLeastSelected(1);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validate();
		return allOk;
	}

	private boolean validate() {
		boolean atLeastOne = false;
		if(login.isVisible()) {
			atLeastOne = StringHelper.containsNonWhitespace(login.getValue());
		}
				
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler != null) {
				FormItem ui = propFormItems.get(userPropertyHandler.getName());
				String uiValue = userPropertyHandler.getStringValue(ui);
				if (StringHelper.containsNonWhitespace(uiValue) && !uiValue.equals("-")) {
					atLeastOne = true;
				}
			}
		}
		
		if(!atLeastOne) {
			showWarning("error.search.form.notempty");
		}
		return atLeastOne;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchButton) {
			if(validate()) {
				fireEvent (ureq, Event.DONE_EVENT);
			}		
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
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