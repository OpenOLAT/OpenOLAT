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

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.modules.coach.CoachingService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSearchController extends BasicController {
	
	private final UserSearchForm searchForm;
	private final UserListController userListCtrl;
	private final StackedPanel mainPanel;

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CoachingService coachingService;
	
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
	
	private void doSearch() {
		String login = searchForm.getLogin();
		Map<String,String> searchProps = searchForm.getSearchProperties();
		
		SearchIdentityParams params = new SearchIdentityParams();
		params.setLogin(login);
		params.setUserProperties(searchProps);
		params.setUserPropertiesAsIntersectionSearch(true);
		params.setStatus(Identity.STATUS_VISIBLE_LIMIT);
		
		long count = securityManager.countIdentitiesByPowerSearch(params);
		if(count > 501) {
			showWarning("error.search.form.too.many");
		} else {
			List<Identity> identities = securityManager.getIdentitiesByPowerSearch(params, 0, 501);
			userListCtrl.loadModel(identities);
			mainPanel.pushContent(userListCtrl.getInitialComponent());
		}
	}
}