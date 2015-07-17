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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.coach.model.SearchCoachedIdentityParams;

/**
 * 
 * Initial date: 20.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSearchController extends BasicController implements Activateable2 {
	
	private final UserSearchForm searchForm;
	private final UserListController userListCtrl;
	private final StackedPanel mainPanel;
	
	public UserSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		//search form
		searchForm = new UserSearchForm(ureq, getWindowControl());
		listenTo(searchForm);
		userListCtrl = new UserListController(ureq, getWindowControl());
		listenTo(userListCtrl);

		mainPanel = putInitialPanel(searchForm.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		//
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
				doSearch();
			}	
		} else if(userListCtrl == source) {
			if(event == Event.BACK_EVENT) {
				mainPanel.popContent();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doSearchByIdentityKey(UserRequest ureq, Long identityKey) {
		SearchCoachedIdentityParams params = new SearchCoachedIdentityParams();
		params.setIdentityKey(identityKey);
		userListCtrl.search(params);
		mainPanel.pushContent(userListCtrl.getInitialComponent());
		if(userListCtrl.size() == 1) {
			userListCtrl.selectUniqueStudent(ureq);
		}
	}
	
	private void doSearch() {
		String login = searchForm.getLogin();
		boolean onlyActive = searchForm.isOnlyActive();
		Map<String,String> searchProps = searchForm.getSearchProperties();
		
		SearchCoachedIdentityParams params = new SearchCoachedIdentityParams();
		params.setLogin(login);
		params.setUserProperties(searchProps);
		if(onlyActive) {
			params.setStatus(Identity.STATUS_VISIBLE_LIMIT);
		}
		userListCtrl.search(params);
		mainPanel.pushContent(userListCtrl.getInitialComponent());
	}
}