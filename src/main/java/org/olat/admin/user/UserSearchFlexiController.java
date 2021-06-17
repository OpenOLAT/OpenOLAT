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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityPowerSearchQueries;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.autocompletion.FlexiAutoCompleterController;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.winmgr.ScrollTopCommand;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.EmailProperty;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Initial Date:  Jul 29, 2003
 *
 * 
 * Comment:  
 * Subworkflow that allows the user to search for a user and choose the user from 
 * the list of users that match the search criteria. Users can be searched by
 * <ul>
 *  <li>Username
 *  <li>First name
 *  <li>Last name
 *  <li>Email address
 * </ul>
 * 
 * 
 * Events:<br>
 *         Fires a SingleIdentityChoosenEvent when an identity has been chosen
 *         which contains the choosen identity<br>
 *         Fires a MultiIdentityChoosenEvent when multiples identities have been
 *         chosen which contains the choosen identities<br>
 *         <p>
 *         Optionally set the useMultiSelect boolean to true which allows to
 *         select multiple identities from within the search results.
 *         
 *         
 * @author Felix Jost, Florian Gnaegi
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class UserSearchFlexiController extends FlexiAutoCompleterController {

	private static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private FormLink backLink;
	private FormLink searchButton;
	private FormLink selectUsersButton;
	private TextElement loginEl;
	private Map <String,FormItem>propFormItems;
	private FlexiTableElement tableEl;
	private UserSearchFlexiTableModel userTableModel;
	private FormLayoutContainer autoCompleterContainer;
	
	private boolean showSelectUsersButton;
	private boolean multiSelection;
	private boolean isAdministrativeUser;
	private List<UserPropertyHandler> userSearchFormPropertyHandlers;
	private List<Organisation> searchableOrganisations;
	private GroupRoles repositoryEntryRole;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private IdentityPowerSearchQueries identitySearchQueries;

	public UserSearchFlexiController(UserRequest ureq, WindowControl wControl, Form rootForm) {
		this(ureq, wControl, rootForm, null, true);
	}
	
	public UserSearchFlexiController(UserRequest ureq, WindowControl wControl, Form rootForm, GroupRoles repositoryEntryRole, boolean multiSelection) {
		super(ureq, wControl, LAYOUT_CUSTOM, "usersearchext", rootForm);
		
		init(ureq, wControl, repositoryEntryRole, multiSelection, false);

		initForm(ureq);
	}
	
	public UserSearchFlexiController(UserRequest ureq, WindowControl wControl, GroupRoles repositoryEntryRole, boolean multiSelection) {
		super(ureq, wControl, "usersearchext");
		
		init(ureq, wControl, repositoryEntryRole, multiSelection, true);
		
		initForm(ureq);
	}
	
	private void init(UserRequest ureq, WindowControl wControl, GroupRoles repositoryEntryRole, boolean multiSelection, boolean showSelectButton) {
		setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(UserSearchFlexiController.class, getLocale(), getTranslator()));

		this.multiSelection = multiSelection;
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		List<UserPropertyHandler> allSearchFormPropertyHandlers = userManager.getUserPropertyHandlersFor(UserSearchForm.class.getCanonicalName(), isAdministrativeUser);
		userSearchFormPropertyHandlers = allSearchFormPropertyHandlers.stream()
				.filter(prop -> !UserConstants.NICKNAME.equals(prop.getName()))// admin. has the login search field
				.collect(Collectors.toList());
		
		searchableOrganisations = organisationService.getOrganisations(getIdentity(), roles,
				OrganisationRoles.valuesWithoutGuestAndInvitee());
		this.repositoryEntryRole = repositoryEntryRole;

		ListProvider provider = new UserSearchListProvider(searchableOrganisations, repositoryEntryRole);
		setListProvider(provider);
		setAllowNewValues(false);
		
		this.showSelectUsersButton = showSelectButton;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;

			// insert a autocompleter search
			Roles roles = ureq.getUserSession().getRoles();
			boolean autoCompleteAllowed = securityModule.isUserAllowedAutoComplete(roles);
			if (autoCompleteAllowed) {
				//auto complete
				String velocityAutoCRoot = Util.getPackageVelocityRoot(FlexiAutoCompleterController.class);
				String autoCPage = velocityAutoCRoot + "/autocomplete.html";
				autoCompleterContainer = FormLayoutContainer.createCustomFormLayout("autocompletionsearch", getTranslator(), autoCPage);
				autoCompleterContainer.setRootForm(mainForm);
				layoutCont.add(autoCompleterContainer);
				layoutCont.add("autocompletionsearch", autoCompleterContainer);
				setupAutoCompleter(ureq, autoCompleterContainer, null, isAdministrativeUser, 60, 3, null);
			}

			// user search form
			backLink = uifactory.addFormLink("btn.back", formLayout);
			backLink.setIconLeftCSS("o_icon o_icon_back");

			FormLayoutContainer searchFormContainer = FormLayoutContainer.createDefaultFormLayout("usersearchPanel", getTranslator());
			searchFormContainer.setRootForm(mainForm);
			searchFormContainer.setElementCssClass("o_sel_usersearch_searchform");
			searchFormContainer.setFormTitle(translate("header.normal"));
			layoutCont.add(searchFormContainer);
			layoutCont.add("usersearchPanel", searchFormContainer);
			
			loginEl = uifactory.addTextElement("login", "search.form.login", 128, "", searchFormContainer);
			loginEl.setVisible(isAdministrativeUser);

			propFormItems = new HashMap<>();
			for (UserPropertyHandler userPropertyHandler : userSearchFormPropertyHandlers) {
				if (userPropertyHandler == null) continue;
				
				FormItem fi = userPropertyHandler.addFormItem(getLocale(), null, UserSearchForm.class.getCanonicalName(), false, searchFormContainer);
				
				// DO NOT validate email field => see OLAT-3324, OO-155, OO-222
				if (userPropertyHandler instanceof EmailProperty && fi instanceof TextElement) {
					TextElement textElement = (TextElement)fi;
					textElement.setItemValidatorProvider(null);
				}

				propFormItems.put(userPropertyHandler.getName(), fi);
			}
			
			FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
			buttonGroupLayout.setRootForm(mainForm);
			searchFormContainer.add(buttonGroupLayout);
			// Don't use submit button, form should not be marked as dirty since this is
			// not a configuration form but only a search form (OLAT-5626)
			searchButton = uifactory.addFormLink("submit.search", buttonGroupLayout, Link.BUTTON);

			layoutCont.contextPut("noList","false");			
			layoutCont.contextPut("showButton","false");

			//add the table
			FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			int colPos = 0;
			List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
			List<UserPropertyHandler> resultingPropertyHandlers = new ArrayList<>();
			// followed by the users fields
			for (int i = 0; i < userPropertyHandlers.size(); i++) {
				UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
				boolean visible = UserManager.getInstance().isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
				if(visible) {
					resultingPropertyHandlers.add(userPropertyHandler);
					tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos++, true, userPropertyHandler.getName()));
				}
			}
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
			
			Translator myTrans = userManager.getPropertyHandlerTranslator(getTranslator());
			userTableModel = new UserSearchFlexiTableModel(Collections.<Identity>emptyList(), resultingPropertyHandlers, getLocale(), tableColumnModel);
			tableEl = uifactory.addTableElement(getWindowControl(), "users", userTableModel, 250, false, myTrans, formLayout);
			tableEl.setCustomizeColumns(false);
			tableEl.setMultiSelect(multiSelection);
			tableEl.setSelectAllEnable(multiSelection);
			
			if (showSelectUsersButton) {
				selectUsersButton = uifactory.addFormLink("select", formLayout);
				tableEl.addBatchButton(selectUsersButton);
			}

			layoutCont.put("userTable", tableEl.getComponent());
		}
	}
	
	@Override
	protected String getSearchValue(UserRequest ureq) {
		if(autoCompleterContainer != null) {
			return ureq.getParameter(autoCompleterContainer.getId(JSNAME_INPUTFIELD));
		}
		return null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == backLink) {
			flc.contextPut("showButton","false");
		} else if(autoCompleterContainer != null && source == autoCompleterContainer.getComponent()) {
			if (event.getCommand().equals(COMMAND_SELECT)) {
				doSelect(ureq);
			}
		} else {
			super.event(ureq, source, event);
		}
	}
	
	@Override
	protected void doFireSelection(UserRequest ureq, List<String> res) {
		String mySel = res.isEmpty() ? null : res.get(0);
		if(StringHelper.containsNonWhitespace(mySel) && StringHelper.isLong(mySel)) {
			try {
				Long key = Long.valueOf(mySel);				
				if (key > 0) {
					Identity chosenIdent = securityManager.loadIdentityByKey(key);
					if(chosenIdent != null) {
						fireEvent(ureq, new SingleIdentityChosenEvent(chosenIdent));
						List<Identity> selectedIdentities = Collections.singletonList(chosenIdent);
						userTableModel.setObjects(selectedIdentities);
						Set<Integer> selectedIndex = new HashSet<>();
						selectedIndex.add(Integer.valueOf(0));
						tableEl.setMultiSelectedIndex(selectedIndex);
					}
				}
			} catch (NumberFormatException e) {
				getWindowControl().setWarning(translate("error.no.user.found"));
			}
		} else {
			getWindowControl().setWarning(translate("error.search.form.notempty"));
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return true;
	}
	
	private boolean validateForm(UserRequest ureq) {
		// override for sys admins
		UserSession usess = ureq.getUserSession();
		if (usess != null && usess.getRoles() != null
				&& (usess.getRoles().isAdministrator() || usess.getRoles().isRolesManager())) {
			return true;
		}
		
		boolean filled = !loginEl.isEmpty();
		StringBuilder  full = new StringBuilder(loginEl.getValue().trim());
		FormItem lastFormElement = loginEl;
		
		// DO NOT validate each user field => see OLAT-3324
		// this are custom fields in a Search Form
		// the same validation logic can not be applied
		// i.e. email must be searchable and not about getting an error like
		// "this e-mail exists already"
		for (UserPropertyHandler userPropertyHandler : userSearchFormPropertyHandlers) {
			FormItem ui = propFormItems.get(userPropertyHandler.getName());
			String uiValue = userPropertyHandler.getStringValue(ui);
			// add value for later non-empty search check
			if (StringHelper.containsNonWhitespace(uiValue)) {
				full.append(uiValue.trim());
				filled = true;
			}

			lastFormElement = ui;
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
		if (fullString.contains("**") ) {
			lastFormElement.setErrorKey("error.search.form.no.wildcard.dublicates", null);
			return false;
		}		
		if (fullString.length() < 4) {
			lastFormElement.setErrorKey("error.search.form.to.short", null);
			return false;
		}
		return true;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == backLink) {
			flc.contextPut("noList","false");			
			flc.contextPut("showButton","false");
			if(userTableModel != null) {
				userTableModel.setObjects(new ArrayList<>());
				tableEl.reset();
			}
		} else if(searchButton == source) {
			if(validateForm(ureq)) {
				getWindowControl().getWindowBackOffice().sendCommandTo(new ScrollTopCommand());
				doSearch();
			}
		} else if (source == selectUsersButton) {
			fireEvent(ureq, new MultiIdentityChosenEvent(userTableModel.getObjects(tableEl.getMultiSelectedIndex())));
		} else if (tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				Identity chosenIdent = userTableModel.getObject(se.getIndex());
				fireEvent(ureq, new SingleIdentityChosenEvent(chosenIdent));
			}
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected void formNext(UserRequest ureq) {
		//
	}

	@Override
	protected void formFinish(UserRequest ureq) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String searchValue = getSearchValue(ureq);
		if(StringHelper.containsNonWhitespace(searchValue)) {
			if(StringHelper.isLong(searchValue)) {
				doFireSelection(ureq, Collections.singletonList(searchValue));
			} else if(searchValue.length() >= 3){
				Map<String, String> userProperties = new HashMap<>();
				userProperties.put(UserConstants.FIRSTNAME, searchValue);
				userProperties.put(UserConstants.LASTNAME, searchValue);
				userProperties.put(UserConstants.EMAIL, searchValue);
				List<Identity> res = searchUsers(searchValue,	userProperties, false);
				if(res.size() == 1) {
					//do select
					Identity chosenIdent = res.get(0);
					fireEvent(ureq, new SingleIdentityChosenEvent(chosenIdent));
				} else if (res.size() > 1){
					tableEl.reset();
					userTableModel.setObjects(res);
				}
			}
		} else {
			if(validateForm(ureq)) {
				doSearch();
			}
		}
	}
	
	public List<Identity> getSelectedIdentities() {
		Set<Integer> index = tableEl.getMultiSelectedIndex();		
		List<Identity> selectedIdentities =	new ArrayList<>(index.size());
		for(Integer i : index) {
			Identity selectedIdentity = userTableModel.getObject(i.intValue());
			selectedIdentities.add(selectedIdentity);
		}
		return selectedIdentities;
	}
	
	public void doSearch() {
		String login = loginEl.getValue();
		// build user fields search map
		Map<String, String> userPropertiesSearch = new HashMap<>();				
		for (UserPropertyHandler userPropertyHandler : userSearchFormPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			FormItem ui = propFormItems.get(userPropertyHandler.getName());
			String uiValue = userPropertyHandler.getStringValue(ui);
			if(userPropertyHandler.getName().startsWith("genericCheckboxProperty")) {
				if(!"false".equals(uiValue)) {
					userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
				}
			} else if (StringHelper.containsNonWhitespace(uiValue)) {
				userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
			}
		}
		if (userPropertiesSearch.isEmpty()) {
			userPropertiesSearch = null;
		}

		tableEl.reset();
		
		List<Identity> users = searchUsers(login, userPropertiesSearch, true);
		if (!users.isEmpty()) {
			userTableModel.setObjects(users);
			flc.contextPut("showButton","true");
		} else {
			getWindowControl().setInfo(translate("error.no.user.found"));
		}
	}

	@Override
	protected void doDispose() {
		// Child controllers auto-disposed by basic controller
	}

	/**
	 * Can be overwritten by subclassen to search other users or filter users.
	 * @param login
	 * @param userPropertiesSearch
	 * @return
	 */
	private List<Identity> searchUsers(String login, Map<String, String> userPropertiesSearch, boolean userPropertiesAsIntersectionSearch) {
		SearchIdentityParams params = new SearchIdentityParams(login,
				userPropertiesSearch, userPropertiesAsIntersectionSearch, null, 
				null, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);
		params.setOrganisations(searchableOrganisations);
		params.setRepositoryEntryRole(repositoryEntryRole, false);
		return identitySearchQueries.getIdentitiesByPowerSearch(params, 0, -1);
	}
}