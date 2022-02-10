/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.admin.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.context.StateMapped;
import org.olat.core.util.StringHelper;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationProvider;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthSPI;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.EmailProperty;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/** 
 * Initial Date: Jan 31, 2006
 * 
 * @author gnaegi
 * 
 * Description: Search form for the usermanager power search. Should only be
 * used by the UserManagerSearchController
 */
public class UsermanagerUserSearchForm extends FormBasicController {
	
	private static final String formIdentifyer = UsermanagerUserSearchForm.class.getCanonicalName();
	
	private MultipleSelectionElement roles;
	private MultipleSelectionElement organisations;
	private MultipleSelectionElement status;
	private MultipleSelectionElement extraSearch;
	private SelectionElement auth;
	private DateChooser beforeDate;
	private DateChooser afterDate;
	private DateChooser userLoginBefore;
	private DateChooser userLoginAfter;
	private FormLink searchButton;

	
	private String[] statusKeys;
	private String[] statusValues;
	private List<String> roleKeys;
	private List<String> roleValues;
	private String[] organisationKeys;
	private String[] organisationValues;
	private String[] authKeys;
	private String[] authValues;
	private String[] extraSearchKeys;
	private String[] extraSearchValues;

	private final boolean isAdministrativeUser;
	private List<UserPropertyHandler> userPropertyHandlers;
	private final Map <String,FormItem> items = new HashMap<>();
	private final List<Organisation> manageableOrganisations;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private WebDAVModule webDAVModule;
	@Autowired
	private OAuthLoginModule oauthLoginModule;
	
	/**
	 * @param binderName
	 * @param cancelbutton
	 */
	public UsermanagerUserSearchForm(UserRequest ureq, WindowControl wControl,
			boolean isAdministrativeUser, List<Organisation> manageableOrganisations) {
		super(ureq, wControl);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.isAdministrativeUser = isAdministrativeUser;
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(formIdentifyer, true);
		this.manageableOrganisations = new ArrayList<>(manageableOrganisations);
		
		roleKeys = OrganisationRoles.toList(OrganisationRoles.values());
		roleValues = new ArrayList<>(roleKeys.size());
		for(int i=0; i<roleKeys.size(); i++) {
			roleValues.add(translate("search.form.constraint.".concat(roleKeys.get(i))));
		}

		statusKeys = new String[] { 
				Integer.toString(Identity.STATUS_ACTIV),
				Integer.toString(Identity.STATUS_PERMANENT),
				Integer.toString(Identity.STATUS_PENDING),
				Integer.toString(Identity.STATUS_INACTIVE),
				Integer.toString(Identity.STATUS_LOGIN_DENIED)
		};
		statusValues = new String[] {
				translate("rightsForm.status.activ"),
				translate("rightsForm.status.permanent"),
				translate("rightsForm.status.pending"),
				translate("rightsForm.status.inactive"),
				translate("rightsForm.status.login_denied")
		};
		
		extraSearchKeys = new String[] { "no-resources", "no-eff-statements" };
		extraSearchValues = new String[] { translate("no.resource"), translate("no.eff.statement") };
		
		List<String> organisationKeyList = new ArrayList<>();
		List<String> organisationValueList = new ArrayList<>();
		for(Organisation organisation:manageableOrganisations) {
			organisationKeyList.add(organisation.getKey().toString());
			organisationValueList.add(organisation.getDisplayName());
		}
		organisationKeys = organisationKeyList.toArray(new String[organisationKeyList.size()]);
		organisationValues = organisationValueList.toArray(new String[organisationValueList.size()]);
		
		// take all providers from the config file
		// convention is that a translation key "search.form.constraint.auth." +
		// providerName
		// must exist. the element is stored using the name "auth." + providerName
		List<String>authKeyList = new ArrayList<>();
		List<String>authValueList = new ArrayList<>();
		Collection<AuthenticationProvider> providers = loginModule.getAuthenticationProviders();
		for (AuthenticationProvider provider:providers) {
			if (provider.isEnabled()) {
				authKeyList.add(provider.getName());
				authValueList.add(translate("search.form.constraint.auth." +provider.getName()));
			}
		}
		if(webDAVModule.isEnabled()) {
			authKeyList.add(WebDAVAuthManager.PROVIDER_WEBDAV);
			authValueList.add(translate("search.form.constraint.auth.WEBDAV"));
		}
		
		// add additional no authentication element
		authKeyList.add("noAuth");
		authValueList.add(translate("search.form.constraint.auth.none"));
		authKeys = authKeyList.toArray(new String[authKeyList.size()]);
		authValues = authValueList.toArray(new String[authValueList.size()]);
		
		initForm(ureq);
	}
	
	/**
	 * @return List of identities that match the criterias from the search form
	 */
	public SearchIdentityParams getSearchIdentityParams() {
		// get user attributes from form
		String idVal = getStringValue("id");
		idVal = idVal.equals("") ? null : idVal;

		String loginVal = getStringValue("login");
		// when searching for deleted users, add wildcard to match with backup prefix
		List<Integer> statusList = getStatus();
		if (statusList.contains(Identity.STATUS_DELETED)) {
			loginVal = "*" + loginVal;
		}
		loginVal = loginVal.equals("") ? null : loginVal;

		// get user fields from form
		// build user fields search map
		Map<String, String> userPropertiesSearch = new HashMap<>();
		for (UserPropertyHandler userPropertyHandler : getPropertyHandlers()) {
			if (userPropertyHandler == null) continue;
			
			FormItem ui = getItem(userPropertyHandler.getName());
			String uiValue = userPropertyHandler.getStringValue(ui);
			if(userPropertyHandler.getName().startsWith("genericCheckboxProperty") && ui instanceof MultipleSelectionElement) {
				if(!"false".equals(uiValue)) {//ignore false for the search
					userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
				}	
			} else if (StringHelper.containsNonWhitespace(uiValue)) {
				// when searching for deleted users, add wildcard to match with backup prefix
				if (userPropertyHandler instanceof EmailProperty && statusList.contains(Identity.STATUS_DELETED)) {
					uiValue = "*" + uiValue;
				}
				userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
			}
		}
		if (userPropertiesSearch.isEmpty()) {
			userPropertiesSearch = null;
		}
		
		OrganisationRoles[] selectedRoles = getRoles().toArray(new OrganisationRoles[0]);
		SearchIdentityParams params = new SearchIdentityParams(loginVal, userPropertiesSearch, true,
				selectedRoles, null, getAuthProviders(), getAfterDate(), getBeforeDate(),
				getUserLoginAfter(), getUserLoginBefore(), null);
		params.setOrganisations(getOrganisations());
		params.setExactStatusList(statusList);
		params.setIdAndExternalIds(idVal);
		params.setWithoutResources(isNoResources());
		params.setWithoutEfficiencyStatements(isNoEfficiencyStatements());
		return params;
	}

	public List<UserPropertyHandler> getPropertyHandlers() {
		return userPropertyHandlers;
	}

	protected Date getBeforeDate() {
		return beforeDate.getDate();
	}
	protected Date getAfterDate() {
		return afterDate.getDate();
	}
	
	protected Date getUserLoginBefore() {
		return userLoginBefore.getDate();
	}

	protected Date getUserLoginAfter() {
		return userLoginAfter.getDate();
	}

	protected FormItem getItem(String name) {
		return items.get(name);
	}

	protected String getStringValue(String key) {
		FormItem f = items.get(key);
		if (f == null) return null;
		if (f instanceof TextElement) {
			return ((TextElement) f).getValue();
		}
		return null;
	}

	protected void setStringValue(String key, String value) {
		FormItem f = items.get(key);
		if (f == null) return;
		if (f instanceof TextElement) {
			((TextElement) f).setValue(value);
		}
	}
	
	protected List<OrganisationRoles> getRoles() {
		List<OrganisationRoles> selectedRoles = new ArrayList<>();
		for(String selectedKey:roles.getSelectedKeys()) {
			if(StringHelper.containsNonWhitespace(selectedKey)) {
				selectedRoles.add(OrganisationRoles.valueOf(selectedKey));
			}
		}
		return selectedRoles;
	}
	
	protected List<Organisation> getOrganisations() {
		List<Organisation> selectedOrganisations = new ArrayList<>();
		Collection<String> selectedKeys = organisations.getSelectedKeys();
		for(Organisation organisation:manageableOrganisations) {
			if(selectedKeys.contains(organisation.getKey().toString())) {
				selectedOrganisations.add(organisation);
			}
		}
		return selectedOrganisations;
	}
	
	protected List<Integer> getStatus() {
		List<Integer> statusList = new ArrayList<>();
		for(String selectedKey:status.getSelectedKeys()) {
			statusList.add(Integer.valueOf(selectedKey));
		}
		return statusList;
	}
	
	protected boolean isNoResources() {
		return extraSearch.getSelectedKeys().contains("no-resources");
	}
	
	protected boolean isNoEfficiencyStatements() {
		return extraSearch.getSelectedKeys().contains("no-eff-statements");
	}

	protected String[] getAuthProviders () {
		List<String> apl = new ArrayList<>();
		for (int i=0; i<authKeys.length; i++) {
			if (auth.isSelected(i)) {
				String authKey = authKeys[i];
				if("noAuth".equals(authKey)) {
					apl.add(null);//special case
				} else if("OAuth".equals(authKey)) {
					List<OAuthSPI> spis = oauthLoginModule.getAllSPIs();
					for(OAuthSPI spi:spis) {
						apl.add(spi.getProviderName());
					}
				} else if("ShibGeneric".equals(authKey)) {
					apl.add(ShibbolethDispatcher.PROVIDER_SHIB);
					apl.add(authKey);
				} else {
					apl.add(authKey);
				}
			}
		}
		return apl.toArray(new String[apl.size()]);
	}

	protected StateMapped getStateEntry() {
		StateMapped state = new StateMapped();
		for(Map.Entry<String, FormItem> itemEntry : items.entrySet()) {
			String key = itemEntry.getKey();
			FormItem f = itemEntry.getValue();
			if (f instanceof TextElement) {
				state.getDelegate().put(key, ((TextElement) f).getValue());
			}	
		}
		return state;
	}

	protected void setStateEntry(StateMapped state) {
		for(Map.Entry<String, String> entry:state.getDelegate().entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			setStringValue(key, value);
		}	
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_user_search_form");
		
		Roles uroles = ureq.getUserSession().getRoles();
		TextElement id = uifactory.addTextElement("id", "search.form.id", 128, "", formLayout);
		id.setVisible(uroles.isAdministrator() || uroles.isSystemAdmin());
		id.setElementCssClass("o_sel_user_search_id");
		items.put("id", id);
	
		TextElement login = uifactory.addTextElement("login", "search.form.login", 128, "", formLayout);
		login.setVisible(isAdministrativeUser);
		login.setElementCssClass("o_sel_user_search_username");
		items.put("login", login);

		String currentGroup = null;
		// Add all available user fields to this form
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			
			String group = userPropertyHandler.getGroup();
			if (!group.equals(currentGroup)) {
				if (currentGroup != null) {
					uifactory.addSpacerElement("spacer_" + group, formLayout, false);
				}
				currentGroup = group;
			}

			FormItem fi = userPropertyHandler.addFormItem(
					getLocale(), null, getClass().getCanonicalName(), false, formLayout
			);
			// Do not validate items, this is a search form!
			if (fi instanceof TextElement) {
				TextElement textElement = (TextElement) fi;
				textElement.setItemValidatorProvider(null);
			}

			fi.setElementCssClass("o_sel_user_search_".concat(userPropertyHandler.getName().toLowerCase()));
			fi.setTranslator(getTranslator());
			items.put(fi.getName(), fi);
		}

		uifactory.addSpacerElement("space1", formLayout, false);
		
		organisations = uifactory.addCheckboxesDropdown("organisations", "search.form.title.organisations",
				formLayout, organisationKeys, organisationValues);
		
		roles = uifactory.addCheckboxesDropdown("roles", "search.form.title.roles", formLayout,
				roleKeys.toArray(new String[roleKeys.size()]), roleValues.toArray(new String[roleValues.size()]));

		uifactory.addSpacerElement("space2", formLayout, false);
		auth = uifactory.addCheckboxesVertical("auth", "search.form.title.authentications", formLayout, authKeys, authValues, 2);
		
		uifactory.addSpacerElement("space3", formLayout, false);
		status = uifactory.addCheckboxesVertical("status", "search.form.title.status", formLayout, statusKeys, statusValues, 2);
		status.select(statusKeys[0], true);	
		status.select(statusKeys[1], true);	
		status.select(statusKeys[2], true);	
		status.select(statusKeys[3], true);	
		status.select(statusKeys[4], true);	
		
		extraSearch = uifactory.addCheckboxesVertical("extra.search", null, formLayout, extraSearchKeys, extraSearchValues, 2);

		uifactory.addSpacerElement("space4", formLayout, false);
		afterDate  = uifactory.addDateChooser("search.form.afterDate", null, formLayout);
		afterDate.setValidDateCheck("error.search.form.no.valid.datechooser");
		beforeDate = uifactory.addDateChooser("search.form.beforeDate", null, formLayout);
		beforeDate.setValidDateCheck("error.search.form.no.valid.datechooser");
		
		uifactory.addSpacerElement("space5", formLayout, false);
		userLoginAfter = uifactory.addDateChooser("search.form.userLoginAfterDate", null, formLayout);
		userLoginAfter.setValidDateCheck("error.search.form.no.valid.datechooser");
		userLoginBefore = uifactory.addDateChooser("search.form.userLoginBeforeDate", null, formLayout);
		userLoginBefore.setValidDateCheck("error.search.form.no.valid.datechooser");
		
		uifactory.addSpacerElement("spaceBottom", formLayout, false);
		
		// Don't use submit button, form should not be marked as dirty since this is
		// not a configuration form but only a search form (OLAT-5626)
		searchButton = uifactory.addFormLink("search", formLayout, Link.BUTTON);
		searchButton.addActionListener(FormEvent.ONCLICK);
		
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchButton) {
			source.getRootForm().submit(ureq);			
		}
	}
}
