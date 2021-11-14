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
package org.olat.course.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExtendedFlexiTableSearchController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.group.ui.main.SearchMembersParams.Origin;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.EmailProperty;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MemberSearchForm extends FormBasicController implements ExtendedFlexiTableSearchController {
	
	public static final String PROPS_IDENTIFIER = MemberSearchForm.class.getCanonicalName();
	
	private String[] roleKeys = { GroupRoles.owner.name(), GroupRoles.coach.name(), GroupRoles.participant.name(), GroupRoles.waiting.name() };
	private String[] originKeys = new String[]{ Origin.all.name(), Origin.repositoryEntry.name(), Origin.businessGroup.name(), Origin.curriculum.name() };
	
	private TextElement login;
	private SingleSelection originEl;
	private MultipleSelectionElement rolesEl;
	private FormLink searchButton;
	
	private Map<String,FormItem> propFormItems;
	private List<UserPropertyHandler> userPropertyHandlers;

	private boolean enabled = true;
	
	@Autowired
	private UserManager userManager;

	public MemberSearchForm(UserRequest ureq, WindowControl wControl, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "search_form", rootForm);
		setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, ureq.getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer leftContainer = FormLayoutContainer.createDefaultFormLayout("left_1", getTranslator());
		leftContainer.setRootForm(mainForm);
		formLayout.add(leftContainer);
		//user property
		login = uifactory.addTextElement("login", "search.login", 128, "", leftContainer);
		login.setDisplaySize(28);

		userPropertyHandlers = userManager.getUserPropertyHandlersFor(PROPS_IDENTIFIER, false);
		propFormItems = new HashMap<>();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			FormItem fi = userPropertyHandler.addFormItem(getLocale(), null, PROPS_IDENTIFIER, false, leftContainer);
			fi.setTranslator(this.getTranslator());
			// DO NOT validate email field => see OLAT-3324, OO-155, OO-222
			if (userPropertyHandler instanceof EmailProperty && fi instanceof TextElement) {
				TextElement textElement = (TextElement)fi;
				textElement.setItemValidatorProvider(null);
				
			}
			if(fi instanceof TextElement) {
				((TextElement)fi).setDisplaySize(28);
			}
			
			propFormItems.put(userPropertyHandler.getName(), fi);
		}

		//others
		FormLayoutContainer rightContainer = FormLayoutContainer.createDefaultFormLayout("right_1", getTranslator());
		rightContainer.setRootForm(mainForm);
		formLayout.add(rightContainer);
		
		//roles
		String[] roleValues = new String[roleKeys.length];
		for(int i=roleKeys.length; i-->0; ) {
			roleValues[i] = translate("search." + roleKeys[i]);
		}
		rolesEl = uifactory.addCheckboxesHorizontal("roles", "search.roles", rightContainer, roleKeys, roleValues);
		for(String roleKey: roleKeys) {
			rolesEl.select(roleKey, true);
		}

		String[] openValues = new String[originKeys.length];
		for(int i=originKeys.length; i-->0; ) {
			openValues[i] = translate("search." + originKeys[i]);
		}
		originEl = uifactory.addRadiosVertical("openBg", "search.origin", rightContainer, originKeys, openValues);
		originEl.select("all", true);

		FormLayoutContainer buttonLayout = FormLayoutContainer.createDefaultFormLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		searchButton = uifactory.addFormLink("search", buttonLayout, Link.BUTTON);
		searchButton.setCustomEnabledLinkCSS("btn btn-primary");
	}

	@Override
	public void setEnabled(boolean enable) {
		this.enabled = enable;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(enabled) {
			fireSearchEvent(ureq);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enabled) {
			if (source == searchButton) {
				fireSearchEvent(ureq);
			}
		}
	}

	private void fireSearchEvent(UserRequest ureq) {
		SearchMembersParams params = new SearchMembersParams();
		//roles
		List<String> selectedKeys = new ArrayList<>(rolesEl.getSelectedKeys());
		GroupRoles[] roles = new GroupRoles[selectedKeys.size()];
		for(int i=0; i<selectedKeys.size(); i++) {
			roles[i] = GroupRoles.valueOf(selectedKeys.get(i));
		}
		
		params.setRoles(roles);

		//origin
		if(!originEl.isOneSelected()) {
			params.setOrigin(Origin.all);
		} else {
			params.setOrigin(Origin.valueOf(originEl.getSelectedKey()));
		}
		
		String loginVal = login.getValue();
		if(StringHelper.containsNonWhitespace(loginVal)) {
			params.setLogin(loginVal);
		}
		
		//user properties
		Map<String, String> userPropertiesSearch = new HashMap<>();				
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			FormItem ui = propFormItems.get(userPropertyHandler.getName());
			String uiValue = userPropertyHandler.getStringValue(ui);
			if (StringHelper.containsNonWhitespace(uiValue)) {
				userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
			}
		}
		if(!userPropertiesSearch.isEmpty()) {
			params.setUserPropertiesSearch(userPropertiesSearch);
		}

		fireEvent(ureq, params);
	}
}