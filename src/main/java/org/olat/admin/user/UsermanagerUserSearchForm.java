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

import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.basesecurity.SearchIdentityParams.AuthProviders;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTablePeriodFilter.PeriodWithUnit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.OrganisationUIFactory;
import org.olat.core.gui.components.util.SelectionValues;
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
import org.olat.login.webauthn.OLATWebAuthnManager;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.olat.user.UserManager;
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
	private MultiSelectionFilterElement organisations;
	private MultipleSelectionElement status;
	private MultipleSelectionElement extraSearch;
	private SelectionElement auth;
	private DateChooser beforeDate;
	private DateChooser afterDate;
	private DateChooser userLoginBefore;
	private DateChooser userLoginAfter;
	private TextElement userAccountExpirationEl;
	private SingleSelection userAccountExpirationTypeEl;
	private SingleSelection userAccountExpirationUnitEl;
	private FormLink searchButton;

	private List<String> roleKeys;
	private List<String> roleValues;
	private SelectionValues organisationSV;
	private SelectionValues authKeysValues;
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
		
		extraSearchKeys = new String[] { "no-resources", "no-eff-statements" };
		extraSearchValues = new String[] { translate("no.resource"), translate("no.eff.statement") };
		
		organisationSV = OrganisationUIFactory.createSelectionValues(manageableOrganisations, getLocale());
		
		// take all providers from the config file
		// convention is that a translation key "search.form.constraint.auth." +
		// providerName
		// must exist. the element is stored using the name "auth." + providerName
		authKeysValues = new SelectionValues();
		Collection<AuthenticationProvider> providers = loginModule.getAuthenticationProviders();
		for (AuthenticationProvider provider:providers) {
			if (provider.isEnabled()) {
				authKeysValues.add(SelectionValues.entry(provider.getName(), translate("search.form.constraint.auth." + provider.getName())));
			}
		}
		if(loginModule.isOlatProviderWithPasskey()) {
			authKeysValues.add(SelectionValues.entry(OLATWebAuthnManager.PASSKEY, translate("search.form.constraint.auth.PASSKEY")));
		}
		if(webDAVModule.isEnabled()) {
			authKeysValues.add(SelectionValues.entry(WebDAVAuthManager.PROVIDER_WEBDAV, translate("search.form.constraint.auth.WEBDAV")));
		}
		
		// add additional no authentication element
		authKeysValues.add(SelectionValues.entry("noAuth", translate("search.form.constraint.auth.none")));
		authKeysValues.add(SelectionValues.entry("noOpenOlatAuth", translate("search.form.constraint.auth.OLAT.none")));

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
		List<Integer> statusList = status.getSelectedKeys().stream()
				.map(Integer::valueOf).toList();

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
		params.setExpireIn(getExpireIn());
		params.setExpiredSince(getExpiredSince());
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

	protected PeriodWithUnit getExpireIn() {
		if(userAccountExpirationTypeEl.isOneSelected() && "future".equals(userAccountExpirationTypeEl.getSelectedKey())) {
			return getExpiration(userAccountExpirationEl, userAccountExpirationUnitEl, false);
		}
		return null;
	}
	
	protected PeriodWithUnit getExpiredSince() {
		if(userAccountExpirationTypeEl.isOneSelected() && "past".equals(userAccountExpirationTypeEl.getSelectedKey())) {
			return getExpiration(userAccountExpirationEl, userAccountExpirationUnitEl, true);
		}
		return null;
	}
	
	protected PeriodWithUnit getExpiration(TextElement valueEl, SingleSelection unitEl, boolean past) {
		String val = valueEl.getValue();
		if(StringHelper.isLong(val)) {
			int value = Integer.parseInt(val);
			ChronoUnit unit = ChronoUnit.valueOf(unitEl.getSelectedKey());
			return switch(unit) {
				case DAYS -> new PeriodWithUnit(Period.ofDays(value), past, value, ChronoUnit.DAYS);
				case WEEKS -> new PeriodWithUnit(Period.ofWeeks(value), past, value, ChronoUnit.WEEKS);
				case MONTHS -> new PeriodWithUnit(Period.ofMonths(value), past, value, ChronoUnit.MONTHS);
				case YEARS -> new PeriodWithUnit(Period.ofYears(value), past, value, ChronoUnit.YEARS);
				default -> new PeriodWithUnit(Period.ofDays(value), past, value, ChronoUnit.DAYS);
			};
		}
		return null;
	}

	protected FormItem getItem(String name) {
		return items.get(name);
	}

	protected String getStringValue(String key) {
		FormItem f = items.get(key);
		if (f == null) return null;
		if (f instanceof TextElement t) {
			return t.getValue();
		}
		return null;
	}

	protected void setStringValue(String key, String value) {
		FormItem f = items.get(key);
		if (f == null) return;
		if (f instanceof TextElement t) {
			t.setValue(value);
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
	
	protected boolean isNoResources() {
		return extraSearch.getSelectedKeys().contains("no-resources");
	}
	
	protected boolean isNoEfficiencyStatements() {
		return extraSearch.getSelectedKeys().contains("no-eff-statements");
	}

	private AuthProviders getAuthProviders () {
		boolean noAuthentication = false;
		boolean noOpenOlatAuthentication = false;
		List<String> apl = new ArrayList<>();
		
		for (int i=0; i<authKeysValues.size(); i++) {
			if (auth.isSelected(i)) {
				String authKey = authKeysValues.keys()[i];
				if("noAuth".equals(authKey)) {
					noAuthentication = true;
				} else if("noOpenOlatAuth".equals(authKey)) {
					noOpenOlatAuthentication = true;
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
					apl.add(authKey.toUpperCase());
				}
			}
		}
		
		String[] providers = apl.toArray(new String[apl.size()]);
		return new AuthProviders(providers, noAuthentication, noOpenOlatAuthentication);
	}

	protected StateMapped getStateEntry() {
		StateMapped state = new StateMapped();
		for(Map.Entry<String, FormItem> itemEntry : items.entrySet()) {
			String key = itemEntry.getKey();
			FormItem f = itemEntry.getValue();
			if (f instanceof TextElement t) {
				state.getDelegate().put(key, t.getValue());
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
			if (fi instanceof TextElement textElement) {
				textElement.setItemValidatorProvider(null);
			}

			fi.setElementCssClass("o_sel_user_search_".concat(userPropertyHandler.getName().toLowerCase()));
			fi.setTranslator(getTranslator());
			items.put(fi.getName(), fi);
		}

		uifactory.addSpacerElement("space1", formLayout, false);
		
		organisations = uifactory.addCheckboxesFilterDropdown("organisations", "search.form.title.organisations",
				formLayout, getWindowControl(), organisationSV);
		
		roles = uifactory.addCheckboxesDropdown("roles", "search.form.title.roles", formLayout,
				roleKeys.toArray(new String[roleKeys.size()]), roleValues.toArray(new String[roleValues.size()]));

		uifactory.addSpacerElement("space2", formLayout, false);
		auth = uifactory.addCheckboxesVertical("auth", "search.form.title.authentications", formLayout,
				authKeysValues.keys(), authKeysValues.values(), 2);
		
		uifactory.addSpacerElement("space3", formLayout, false);
		
		SelectionValues statusPK = new SelectionValues();
		statusPK.add(SelectionValues.entry(Integer.toString(Identity.STATUS_ACTIV), translate("rightsForm.status.activ")));
		statusPK.add(SelectionValues.entry(Integer.toString(Identity.STATUS_PERMANENT), translate("rightsForm.status.permanent")));
		statusPK.add(SelectionValues.entry(Integer.toString(Identity.STATUS_PENDING), translate("rightsForm.status.pending")));
		statusPK.add(SelectionValues.entry(Integer.toString(Identity.STATUS_INACTIVE), translate("rightsForm.status.inactive")));
		statusPK.add(SelectionValues.entry(Integer.toString(Identity.STATUS_LOGIN_DENIED), translate("rightsForm.status.login_denied")));
		status = uifactory.addCheckboxesVertical("status", "search.form.title.status", formLayout, statusPK.keys(), statusPK.values(), 2);
		
		extraSearch = uifactory.addCheckboxesVertical("extra.search", null, formLayout, extraSearchKeys, extraSearchValues, 2);

		uifactory.addSpacerElement("space4", formLayout, false);
		afterDate  = uifactory.addDateChooser("search.form.afterDate", null, formLayout);
		afterDate.setValidDateCheck("error.search.form.no.valid.datechooser");
		beforeDate = uifactory.addDateChooser("search.form.beforeDate", null, formLayout);
		beforeDate.setValidDateCheck("error.search.form.no.valid.datechooser");
		
		uifactory.addSpacerElement("space5", formLayout, false);
		
		SelectionValues unitPK = new SelectionValues();
		unitPK.add(SelectionValues.entry(ChronoUnit.DAYS.name(), translate("filter.day")));
		unitPK.add(SelectionValues.entry(ChronoUnit.WEEKS.name(), translate("filter.week")));
		unitPK.add(SelectionValues.entry(ChronoUnit.MONTHS.name(), translate("filter.month")));
		unitPK.add(SelectionValues.entry(ChronoUnit.YEARS.name(), translate("filter.year")));
		
		SelectionValues pastPK = new SelectionValues();
		pastPK.add(SelectionValues.entry("future", translate("filter.expiration.future")));
		pastPK.add(SelectionValues.entry("past", translate("filter.expiration.past")));

		FormLayoutContainer userAccountExpirationCont = uifactory.addInlineFormLayout("search.form.userAccountExpiration", "search.form.userAccountExpiration", formLayout);
		userAccountExpirationTypeEl = uifactory.addDropdownSingleselect("search.form.userAccountExpiration.type", null, userAccountExpirationCont, pastPK.keys(), pastPK.values());
		userAccountExpirationTypeEl.setDomReplacementWrapperRequired(false);
		
		userAccountExpirationEl = uifactory.addTextElement("search.form.user.account.expiration.value", null, 5, "", userAccountExpirationCont);
		userAccountExpirationEl.setDomReplacementWrapperRequired(false);

		userAccountExpirationUnitEl = uifactory.addDropdownSingleselect("search.form.user.account.expiration.unit", null, userAccountExpirationCont, unitPK.keys(), unitPK.values());
		userAccountExpirationUnitEl.setDomReplacementWrapperRequired(false);
		userAccountExpirationUnitEl.select(ChronoUnit.DAYS.name(), true);

		uifactory.addSpacerElement("space6", formLayout, false);
		userLoginAfter = uifactory.addDateChooser("search.form.userLoginAfterDate", null, formLayout);
		userLoginAfter.setValidDateCheck("error.search.form.no.valid.datechooser");
		userLoginBefore = uifactory.addDateChooser("search.form.userLoginBeforeDate", null, formLayout);
		userLoginBefore.setValidDateCheck("error.search.form.no.valid.datechooser");
		
		uifactory.addSpacerElement("spaceBottom", formLayout, false);
		
		// Don't use submit button, form should not be marked as dirty since this is
		// not a configuration form but only a search form (OLAT-5626)
		searchButton = uifactory.addFormLink("search", formLayout, Link.BUTTON);
		searchButton.setElementCssClass("o_sel_user_search_button");
		searchButton.addActionListener(FormEvent.ONCLICK);
		
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchButton) {
			source.getRootForm().submit(ureq);			
		}
	}
}
