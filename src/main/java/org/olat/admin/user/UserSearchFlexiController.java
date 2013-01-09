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
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElment;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.autocompletion.EntriesChosenEvent;
import org.olat.core.gui.control.generic.ajax.autocompletion.FlexiAutoCompleterController;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;
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

	private static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();

	private FormLink backLink, selectAll, deselectAll;
	private Panel searchPanel;
	private FlexiTableElment tableEl;
	private VelocityContainer tableVC;
	private UserSearchForm searchform;
	private UserSearchFlexiTableModel userTableModel;
	private FlexiAutoCompleterController autocompleterC;

	private boolean isAdministrativeUser;
	private final BaseSecurityModule securityModule;
	private final UserManager userManager;

	/**
	 * @param ureq
	 * @param wControl
	 * @param cancelbutton
	 * @param userMultiSelect
	 * @param statusEnabled
	 */
	public UserSearchFlexiController(UserRequest ureq, WindowControl wControl, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "usersearch", rootForm);
		
		securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		userManager = UserManager.getInstance();

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
			
			tableVC = createVelocityContainer("userflexisearch");

			//add the table
			FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.user.select"));
			if(isAdministrativeUser) {
				tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.user.login"));
			}
			List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
			List<UserPropertyHandler> resultingPropertyHandlers = new ArrayList<UserPropertyHandler>();
			// followed by the users fields
			for (int i = 0; i < userPropertyHandlers.size(); i++) {
				UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
				boolean visible = UserManager.getInstance().isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
				if(visible) {
					resultingPropertyHandlers.add(userPropertyHandler);
					tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey()));
				}
			}
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select"));
			
			Translator myTrans = userManager.getPropertyHandlerTranslator(getTranslator());
			userTableModel = new UserSearchFlexiTableModel(Collections.<UserResultWrapper>emptyList(), resultingPropertyHandlers, getLocale(), tableColumnModel);
			tableEl = uifactory.addTableElement("users", userTableModel, myTrans, formLayout);

			selectAll = uifactory.addFormLink("selectall", formLayout);
			deselectAll = uifactory.addFormLink("deselectall", formLayout);
			
			tableVC.put("table", tableEl.getComponent());
			tableVC.put("selectAll", selectAll.getComponent());
			tableVC.put("deselectAll", deselectAll.getComponent());
		}
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
		} else if(selectAll == source) {
			checkAll(true);
		} else if(deselectAll == source) {
			checkAll(false);
		} else if (source instanceof FormLink && source.getName().startsWith("sel_lin")) {
			Identity chosenIdent = (Identity)source.getUserObject();
			fireEvent(ureq, new SingleIdentityChosenEvent(chosenIdent));
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	private void checkAll(boolean select) {
		for(UserResultWrapper wrapper : userTableModel.getObjects()) {
			wrapper.getSelectEl().select("on", select);
		}
		tableVC.setDirty(true);
	}
	
	public List<Identity> getSelectedIdentities() {
		List<Identity> identities = new ArrayList<Identity>();
		for(UserResultWrapper wrapper : userTableModel.getObjects()) {
			if(wrapper.getSelectEl().isSelected(0)) {
				identities.add(wrapper.getIdentity());
			}
		}
		return identities;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == autocompleterC) {
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
					// should not happen at all. Tell that an identity has been chosen
					fireEvent(ureq, new SingleIdentityChosenEvent(chosenIdent));
				}
			} catch (NumberFormatException e) {
				getWindowControl().setWarning(translate("error.no.user.found"));
				return;									
			}
		} else if (source == searchform) {
			if (event == Event.DONE_EVENT) {
				doSearch();
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} else {
				fireEvent(ureq, event);
			}
		}
	}
	
	private void doSearch() {
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
		if (userPropertiesSearch.isEmpty()) {
			userPropertiesSearch = null;
		}

		List<Identity> users = searchUsers(login,	userPropertiesSearch, true);
		if (!users.isEmpty()) {
			userTableModel.setObjects(wrapIdentities(users));
			searchPanel.pushContent(tableVC);
			flc.contextPut("showButton","true");
		} else {
			getWindowControl().setInfo(translate("error.no.user.found"));
		}
	}
	
	private List<UserResultWrapper> wrapIdentities(List<Identity> identities) {
		List<UserResultWrapper> wrappers = new ArrayList<UserResultWrapper>(identities.size());
		for(Identity identity:identities) {
			MultipleSelectionElement selectEl = uifactory.addCheckboxesHorizontal("sel_" + identity.getKey(), flc, new String[]{"on"}, new String[]{""}, null);
			FormLink selectLink = uifactory.addFormLink("sel_lin_" + identity.getKey(), "select", null, flc, Link.LINK);
			selectLink.setUserObject(identity);
			wrappers.add(new UserResultWrapper(identity, selectLink, selectEl));
		}
		return wrappers;
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
	private List<Identity> searchUsers(String login, Map<String, String> userPropertiesSearch, boolean userPropertiesAsIntersectionSearch) {
	  return BaseSecurityManager.getInstance().getVisibleIdentitiesByPowerSearch(
			(login.equals("") ? null : login),
			userPropertiesSearch, userPropertiesAsIntersectionSearch,	// in normal search fields are intersected
			null, null, null, null, null);
	}
}
