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

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.autocompletion.EntriesChosenEvent;
import org.olat.core.gui.control.generic.ajax.autocompletion.FlexiAutoCompleterController;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.user.propertyhandlers.UserPropertyHandler;

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
public class UserSearchFlexiController extends FormBasicController {

	private static final String ACTION_SINGLESELECT_CHOOSE = "ssc";
	private static final String ACTION_MULTISELECT_CHOOSE = "msc";

	private Panel searchPanel;
	private UserSearchForm searchform;
	private TableController tableCtr;
	private TableGuiConfiguration tableConfig;
	private UserTableDataModel tdm;
	private List<Identity> foundIdentities = new ArrayList<Identity>();
	private boolean useMultiSelect = false;
	private Object userObject;
	
	private FlexiAutoCompleterController autocompleterC;
	private String actionKeyChoose;
	private boolean isAdministrativeUser;
	private FormLink backLink;

	public static final String ACTION_KEY_CHOOSE = "action.choose";
	public static final String ACTION_KEY_CHOOSE_FINISH = "action.choose.finish";
	
	private final BaseSecurityModule securityModule;

	/**
	 * @param ureq
	 * @param wControl
	 * @param cancelbutton
	 * @param userMultiSelect
	 * @param statusEnabled
	 */
	public UserSearchFlexiController(UserRequest ureq, WindowControl wControl, boolean userMultiSelect, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "usersearch", rootForm);
		this.useMultiSelect = userMultiSelect;
		this.actionKeyChoose = ACTION_KEY_CHOOSE;
		securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			backLink = uifactory.addFormLink("btn.back", formLayout);

			searchPanel = new Panel("usersearchPanel");
			layoutCont.put("usersearchPanel", searchPanel);
			

			Roles roles = ureq.getUserSession().getRoles();
			isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
			searchform = new UserSearchForm(ureq, getWindowControl(), isAdministrativeUser, false, formLayout.getRootForm());
			listenTo(searchform);
			
			searchPanel.setContent(searchform.getInitialComponent());
			layoutCont.add(searchform.getInitialFormItem());
			layoutCont.contextPut("noList","false");			
			layoutCont.contextPut("showButton","false");

			// insert a autocompleter search
			boolean autoCompleteAllowed = securityModule.isUserAllowedAutoComplete(roles);
			boolean ajax = Windows.getWindows(ureq).getWindowManager().isAjaxEnabled();
			if (ajax && autoCompleteAllowed) {
				ListProvider provider = new UserSearchListProvider();
				autocompleterC = new FlexiAutoCompleterController(ureq, getWindowControl(), provider, null, isAdministrativeUser, 60, 3, null);
				listenTo(autocompleterC);
				layoutCont.put("autocompletionsearch", autocompleterC.getInitialComponent());
			}

			//add the table
			tableConfig = new TableGuiConfiguration();
			tableConfig.setTableEmptyMessage(translate("error.no.user.found"));
			tableConfig.setDownloadOffered(false);// no download because user should not download user-list
			tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
			listenTo(tableCtr);
		}
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
			flc.contextPut("noList","false");			
			flc.contextPut("showButton","false");
			searchPanel.popContent();
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == backLink) {
			flc.contextPut("noList","false");			
			flc.contextPut("showButton","false");
			searchPanel.popContent();
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				if (te.getActionId().equals(ACTION_SINGLESELECT_CHOOSE)) {
					int rowid = te.getRowId();
					Identity foundIdentity = (Identity)tdm.getObject(rowid);
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
					Identity chosenIdent = BaseSecurityManager.getInstance().loadIdentityByKey(key);
					// No need to check for null, exception is thrown when identity does not exist which really 
					// should not happen at all. 
					// Tell that an identity has been chosen
					fireEvent(ureq, new SingleIdentityChosenEvent(chosenIdent));
				}
			} catch (NumberFormatException e) {
				getWindowControl().setWarning(translate("error.no.user.found"));
				return;									
			}
		} else if (source == searchform) {
			if (event == Event.DONE_EVENT) {
				// form validation was ok

				String login = searchform.login.getValue();
				// build user fields search map
				Map<String, String> userPropertiesSearch = new HashMap<String, String>();				
				for (UserPropertyHandler userPropertyHandler : searchform.userPropertyHandlers) {
					if (userPropertyHandler == null) continue;
					FormItem ui = searchform.propFormItems.get(userPropertyHandler.getName());
					String uiValue = userPropertyHandler.getStringValue(ui);
					if (StringHelper.containsNonWhitespace(uiValue)) {
						userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
						getLogger().info("Search property:" + userPropertyHandler.getName() + "=" + uiValue);
					}
				}
				if (userPropertiesSearch.isEmpty()) userPropertiesSearch = null;
				
				tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
				listenTo(tableCtr);
				
				List<Identity> users = searchUsers(login,	userPropertiesSearch, true);
				if (!users.isEmpty()) {
					tdm = new UserTableDataModel(users, ureq.getLocale(), isAdministrativeUser);
					// add the data column descriptors
					tdm.addColumnDescriptors(tableCtr, null);
					// add the action columns
					if (useMultiSelect) {
						// add multiselect action
						tableCtr.addMultiSelectAction(actionKeyChoose, ACTION_MULTISELECT_CHOOSE);
					} else {
						// add single column selec action
						tableCtr.addColumnDescriptor(new StaticColumnDescriptor(ACTION_SINGLESELECT_CHOOSE, "table.header.action", translate("action.choose")));
					}
					tableCtr.setTableDataModel(tdm);
					tableCtr.setMultiSelect(useMultiSelect);
					searchPanel.pushContent(tableCtr.getInitialComponent());
					flc.contextPut("showButton","true");
				} else {
					getWindowControl().setInfo(translate("error.no.user.found"));
				}
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} else {
				fireEvent(ureq, event);
			}
		}
		
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// Child controllers auto-disposed by basic controller
	}

	/**
	 * Can be overwritten by subclassen to search other users or filter users.
	 * @param login
	 * @param userPropertiesSearch
	 * @return
	 */
	protected List<Identity> searchUsers(String login, Map<String, String> userPropertiesSearch, boolean userPropertiesAsIntersectionSearch) {
	  return BaseSecurityManager.getInstance().getVisibleIdentitiesByPowerSearch(
			(login.equals("") ? null : login),
			userPropertiesSearch, userPropertiesAsIntersectionSearch,	// in normal search fields are intersected
			null, null, null, null, null);
	}
}
