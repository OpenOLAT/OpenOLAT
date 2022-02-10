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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityPowerSearchQueries;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.ajax.autocompletion.AutoCompleterController;
import org.olat.core.gui.control.generic.ajax.autocompletion.EntriesChosenEvent;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Jul 29, 2003
 *
 * @author Felix Jost, Florian Gnaegi
 * 
 * <pre>
 * Comment:  
 * Subworkflow that allows the user to search for a user and choose the user from 
 * the list of users that match the search criteria. Users can be searched by
 * <ul>
 * <li />
 * Username
 * <li />
 * First name
 * <li />
 * Last name
 * <li />
 * Email address
 * </ul>
 * 
 * </pre>
 * 
 * Events:<br>
 *         Fires a SingleIdentityChoosenEvent when an identity has been chosen
 *         which contains the choosen identity<br>
 *         Fires a MultiIdentityChoosenEvent when multiples identities have been
 *         chosen which contains the choosen identities<br>
 *         <p>
 *         Optionally set the useMultiSelect boolean to true which allows to
 *         select multiple identities from within the search results.
 */
public class UserSearchController extends BasicController {
	// Needs PACKAGE and VELOCITY_ROOT because DeletableUserSearchController extends AddUserSearchController and re-use translations
	private static final String PACKAGE = UserSearchController.class.getPackage().getName();
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(PACKAGE);
	
	private static final String ACTION_SINGLESELECT_CHOOSE = "ssc";
	private static final String ACTION_MULTISELECT_CHOOSE = "msc";
	
	public static final String ACTION_KEY_CHOOSE = "action.choose";
	public static final String ACTION_KEY_CHOOSE_FINISH = "action.choose.finish";
	
	private VelocityContainer myContent;
	private StackedPanel searchPanel;
	private UserSearchForm searchform;
	private TableController tableCtr;
	private TableGuiConfiguration tableConfig;
	private UserTableDataModel tdm;
	private List<Identity> foundIdentities = new ArrayList<>();
	private boolean useMultiSelect = false;
	private Object userObject;
	public final List<Organisation> searchableOrganisations;
	
	private AutoCompleterController autocompleterC;
	private String actionKeyChoose;
	private final boolean isAdministrativeUser;
	private Link backLink;
	
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	protected BaseSecurityModule securityModule;
	@Autowired
	protected OrganisationService organisationService;
	@Autowired
	private IdentityPowerSearchQueries identitySearchQueries;

	public UserSearchController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, false, false, false);
	}

	public UserSearchController(UserRequest ureq, WindowControl wControl, boolean cancelbutton) {
		this(ureq, wControl, cancelbutton, false, false);
	}


	public UserSearchController(UserRequest ureq, WindowControl windowControl, boolean cancelbutton, boolean userMultiSelect, String actionKeyChooseFinish) {
		this(ureq, windowControl, cancelbutton, userMultiSelect, false);
		this.actionKeyChoose = actionKeyChooseFinish;
	}

	public UserSearchController(UserRequest ureq, WindowControl wControl, boolean cancelbutton, boolean userMultiSelect, boolean allowReturnKey) {
		this(ureq, wControl, cancelbutton, userMultiSelect, allowReturnKey, false);
	}

	/**
	 * 
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param cancelbutton 
	 * @param userMultiSelect
	 * @param allowReturnKey
	 * @param wildCardOrgsForSysAdmin
	 */
	public UserSearchController(UserRequest ureq, WindowControl wControl, boolean cancelbutton, boolean userMultiSelect,
			boolean allowReturnKey, boolean wildCardOrgsForSysAdmin) {
		super(ureq, wControl);
		this.useMultiSelect = userMultiSelect;
		this.actionKeyChoose = ACTION_KEY_CHOOSE;
	  // Needs PACKAGE and VELOCITY_ROOT because DeletableUserSearchController extends AddUserSearchController and re-use translations
		Translator pT = UserManager.getInstance().getPropertyHandlerTranslator(Util.createPackageTranslator(UserSearchController.class, ureq.getLocale()) );	
		myContent = new VelocityContainer("olatusersearch", VELOCITY_ROOT + "/usersearch.html", pT, this);
		backLink = LinkFactory.createButton("btn.back", myContent, this);
		
		searchPanel = new SimpleStackedPanel("usersearchPanel");
		searchPanel.addListener(this);
		myContent.put("usersearchPanel", searchPanel);

		UserSession usess = ureq.getUserSession();
		Roles roles = usess.getRoles();
		if(wildCardOrgsForSysAdmin && roles.isSystemAdmin()) {
			searchableOrganisations = organisationService.getOrganisations();
			isAdministrativeUser = true;
		} else {
			searchableOrganisations = organisationService.getOrganisations(getIdentity(), roles,
					OrganisationRoles.valuesWithoutGuestAndInvitee());
			isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		}

		searchform = new UserSearchForm(ureq, wControl, isAdministrativeUser, cancelbutton, allowReturnKey);
		listenTo(searchform);
		searchPanel.setContent(searchform.getInitialComponent());
	
		myContent.contextPut("noList", "false");			
		myContent.contextPut("showButton", "false");
		
		boolean autoCompleteAllowed = securityModule.isUserAllowedAutoComplete(roles);
		if (autoCompleteAllowed) {
			// insert a autocompleter search
			ListProvider provider = new UserSearchListProvider(searchableOrganisations);
			autocompleterC = new AutoCompleterController(ureq, getWindowControl(), provider, null, isAdministrativeUser, 60, 3, null);
			listenTo(autocompleterC);
			myContent.put("autocompletionsearch", autocompleterC.getInitialComponent());
		}
		
		tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(false);// no download because user should not download user-list
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), myContent.getTranslator());
		listenTo(tableCtr);
		putInitialPanel(myContent);
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == backLink) {		
			myContent.contextPut("noList","false");			
			myContent.contextPut("showButton","false");
			searchPanel.popContent();
		} 
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				if (te.getActionId().equals(ACTION_SINGLESELECT_CHOOSE)) {
					int rowid = te.getRowId();
					Identity foundIdentity = tdm.getObject(rowid);
					foundIdentities.add(foundIdentity);
					// Tell parentController that a subject has been found
					fireEvent(ureq, new SingleIdentityChosenEvent(foundIdentity));
				}
			} else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals(ACTION_MULTISELECT_CHOOSE)) {
					foundIdentities = tdm.getObjects(tmse.getSelection());
					fireEvent(ureq, new MultiIdentityChosenEvent(foundIdentities));
				}
			}
		} else if (source == autocompleterC) {
			if(event instanceof EntriesChosenEvent) {
				EntriesChosenEvent ece = (EntriesChosenEvent)event;
				List<String> res = ece.getEntries();
				// if we get the event, we have a result or an incorrect selection see OLAT-5114 -> check for empty
				String mySel = res.isEmpty() ? null : res.get(0);
				if (( mySel == null) || mySel.trim().equals("")) {
					getWindowControl().setWarning(translate("error.search.form.notempty"));
					return;
				}
				Long key = -1l; // default not found
				try {
					key = Long.valueOf(mySel);				
					if (key > 0) {
						Identity chosenIdent = securityManager.loadIdentityByKey(key);
						// No need to check for null, exception is thrown when identity does not exist which really 
						// should not happen at all. 
						// Tell that an identity has been chosen
						fireEvent(ureq, new SingleIdentityChosenEvent(chosenIdent));
					}
				} catch (NumberFormatException e) {
					getWindowControl().setWarning(translate("error.no.user.found"));
					return;									
				}
			}
		} else if (source == searchform) {
			if (event == Event.DONE_EVENT) {
				// form validation was ok
				doSearch(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}
	
	private void doSearch(UserRequest ureq) {
		String login = searchform.login.getValue();
		// build user fields search map
		Map<String, String> userPropertiesSearch = new HashMap<>();				
		for (UserPropertyHandler userPropertyHandler : searchform.userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			FormItem ui = searchform.propFormItems.get(userPropertyHandler.getName());
			if(ui != null) {
				String uiValue = userPropertyHandler.getStringValue(ui);
				if(userPropertyHandler.getName().startsWith("genericCheckboxProperty")) {
					if(!"false".equals(uiValue)) {
						userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
					}
				} else if (StringHelper.containsNonWhitespace(uiValue)) {
					userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
				}
			}
		}
		if (userPropertiesSearch.isEmpty()) {
			userPropertiesSearch = null;
		}
		
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), myContent.getTranslator());
		listenTo(tableCtr);
		
		List<Identity> users = searchUsers(login, userPropertiesSearch, true);
		int maxResults = securityModule.getUserSearchMaxResultsValue();
		if(maxResults > 0 && users.size() > maxResults) {
			users = users.subList(0, maxResults);
			showWarning("error.search.maxResults", Integer.toString(maxResults));
		}
		if (!users.isEmpty()) {
			tdm = new UserTableDataModel(users, getLocale(), isAdministrativeUser);
			// add the data column descriptors
			tdm.addColumnDescriptors(tableCtr, null);
			// add the action columns
			if (useMultiSelect) {
				// add multiselect action
				tableCtr.addMultiSelectAction(this.actionKeyChoose, ACTION_MULTISELECT_CHOOSE);
			} else {
				// add single column selec action
				tableCtr.addColumnDescriptor(new StaticColumnDescriptor(ACTION_SINGLESELECT_CHOOSE, "table.header.action", myContent
						.getTranslator().translate("action.choose")));
			}
			tableCtr.setTableDataModel(tdm);
			tableCtr.setMultiSelect(useMultiSelect);
			searchPanel.pushContent(tableCtr.getInitialComponent());
			myContent.contextPut("showButton","true");
		} else {
			getWindowControl().setInfo(translate("error.no.user.found"));
		}
	}

	/**
	 * Can be overwritten by subclassen to search other users or filter users.
	 * @param login
	 * @param userPropertiesSearch
	 * @return
	 */
	protected List<Identity> searchUsers(String login, Map<String, String> userPropertiesSearch, boolean userPropertiesAsIntersectionSearch) {
		int maxResults = securityModule.getUserSearchMaxResultsValue() > 0 ? securityModule.getUserSearchMaxResultsValue() + 1 : -1;
		login = (login.equals("") ? null : login);
		SearchIdentityParams params = new SearchIdentityParams(login, userPropertiesSearch, userPropertiesAsIntersectionSearch, null, 
				null, null, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);
		params.setOrganisations(searchableOrganisations);
		return identitySearchQueries.getIdentitiesByPowerSearch(params, 0, maxResults);
	}
}