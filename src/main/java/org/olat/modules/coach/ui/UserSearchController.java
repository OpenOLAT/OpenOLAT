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

import java.util.List;
import java.util.Map;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.coach.model.SearchCoachedIdentityParams;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSearchController extends BasicController implements Activateable2 {
	
	private UserListController userListCtrl;
	private final UserSearchForm searchForm;
	private final TooledStackedPanel stackPanel;
	private final List<OrganisationRef> searcheableOrganisations;
	
	@Autowired
	private OrganisationModule organisationModule;
	
	public UserSearchController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl);
		
		this.stackPanel = stackPanel;
		Roles roles = ureq.getUserSession().getRoles();
		
		if(organisationModule.isEnabled()) {
			searcheableOrganisations = roles.getOrganisationsWithRoles(OrganisationRoles.administrator,
				OrganisationRoles.principal, OrganisationRoles.learnresourcemanager);
		} else {
			searcheableOrganisations = null;
		}

		//search form
		searchForm = new UserSearchForm(ureq, getWindowControl());
		listenTo(searchForm);
		putInitialPanel(searchForm.getInitialComponent());
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		if("Identity".equalsIgnoreCase(entry.getOLATResourceable().getResourceableTypeName())) {
			Long identityKey = entry.getOLATResourceable().getResourceableId();
			doSearchByIdentityKey(ureq, identityKey);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchForm == source) {
			if(event == Event.DONE_EVENT) {
				doSearch(ureq);
			}	
		} 
		super.event(ureq, source, event);
	}
	
	private void doSearchByIdentityKey(UserRequest ureq, Long identityKey) {
		SearchCoachedIdentityParams params = new SearchCoachedIdentityParams();
		params.setIdentityKey(identityKey);
		params.setOrganisations(searcheableOrganisations);
		userListCtrl = new UserListController(ureq, getWindowControl(), stackPanel);
		userListCtrl.search(params);
		if(userListCtrl.size() == 1) {
			userListCtrl.selectUniqueStudent(ureq);
			stackPanel.pushController("Result", userListCtrl);
		} else {

			stackPanel.pushController("Results", userListCtrl);
		}
	}
	
	private void doSearch(UserRequest ureq) {
		String login = searchForm.getLogin();
		boolean onlyActive = searchForm.isOnlyActive();
		Map<String,String> searchProps = searchForm.getSearchProperties();
		
		SearchCoachedIdentityParams params = new SearchCoachedIdentityParams();
		params.setLogin(login);
		params.setUserProperties(searchProps);
		params.setOrganisations(searcheableOrganisations);
		if(onlyActive) {
			params.setStatus(Identity.STATUS_VISIBLE_LIMIT);
		}
		
		userListCtrl = new UserListController(ureq, getWindowControl(), stackPanel);
		userListCtrl.search(params);
		listenTo(userListCtrl);
		stackPanel.popUpToRootController(ureq);
		stackPanel.pushController(translate("results"), userListCtrl);
	}
}