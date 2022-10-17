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
package org.olat.user.ui.admin.lifecycle;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.WebappHelper;
import org.olat.user.UserModule;
import org.olat.user.ui.admin.UserSearchTableController;
import org.olat.user.ui.admin.UserSearchTableSettings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserLifecycleOverviewController extends BasicController implements Activateable2 {
	
	private VelocityContainer mainVC;
	private TooledStackedPanel stackPanel;
	private TabbedPane lifecycleTabbedPane;
	
	private List<OrganisationRef> manageableOrganisations;

	private UserSearchTableController inactiveUserCtrl;
	private UserSearchTableController readyToDeleteUserCtrl;
	private UserSearchTableController readyToInactivateUserCtrl;
	
	@Autowired
	private UserModule userModule;
	
	/**
	 * Overview of all the users in the organization that the user can manage
	 * as user manager (if it can delete a user), role manager or administrator.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param stackPanel For bread crumb
	 */
	public UserLifecycleOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;

		Roles roles = ureq.getUserSession().getRoles();
		if(BaseSecurityModule.USERMANAGER_CAN_DELETE_USER.booleanValue()) {
			manageableOrganisations = roles.getOrganisationsWithRoles(OrganisationRoles.administrator,
				OrganisationRoles.usermanager, OrganisationRoles.rolesmanager);
		} else {
			manageableOrganisations = roles.getOrganisationsWithRoles(OrganisationRoles.administrator,
				OrganisationRoles.rolesmanager);
		}
		initPanel(ureq);
	}
	
	/**
	 * 
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param stackPanel For bread crumb
	 * @param organisation The organization to overview
	 */
	public UserLifecycleOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, Organisation organisation) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;

		Roles roles = ureq.getUserSession().getRoles();
		if(organisation == null) {
			manageableOrganisations = Collections.emptyList();
		} else if(roles.hasSomeRoles(organisation, OrganisationRoles.administrator, OrganisationRoles.rolesmanager)
				|| (BaseSecurityModule.USERMANAGER_CAN_DELETE_USER.booleanValue() && roles.hasSomeRoles(organisation, OrganisationRoles.usermanager))) {
			manageableOrganisations = Collections.singletonList(organisation);
		} else {
			manageableOrganisations = Collections.emptyList();
		}
		initPanel(ureq);
	}
	
	private void initPanel(UserRequest ureq) {
		if (!manageableOrganisations.isEmpty()) {
			mainVC = createVelocityContainer("lifecycle_overview");
			initTabbedPane(ureq);
			putInitialPanel(mainVC);
		} else {
			String supportAddr = WebappHelper.getMailConfig("mailSupport");
			getWindowControl().setWarning(translate("error.noaccess.to.user", supportAddr));
			putInitialPanel(new Panel("empty"));
		}
	}
	
	/**
	 * Initialize the tabbed pane according to the users rights and the system
	 * configuration
	 * @param identity
	 * @param ureq
	 */
	private void initTabbedPane(UserRequest ureq) {
		lifecycleTabbedPane = new TabbedPane("tabPane", ureq.getLocale());
		lifecycleTabbedPane.addListener(this);
		mainVC.put("tabPane", lifecycleTabbedPane);
		
		// ready to inactivate
		readyToInactivateUserCtrl = new UserSearchTableController(ureq, getWindowControl(), stackPanel, UserSearchTableSettings.minimal());
		listenTo(readyToInactivateUserCtrl);
		lifecycleTabbedPane.addTab(translate("overview.ready.to.inactivate.user"), readyToInactivateUserCtrl.getInitialComponent());
			
		SearchIdentityParams readyToInactivateSearchParams = getReadyToInactivateParams();
		readyToInactivateUserCtrl.loadModel(readyToInactivateSearchParams);

		// inactive
		lifecycleTabbedPane.addTabControllerCreator(ureq, translate("overview.inactive.user"), (uureq -> {
			inactiveUserCtrl = new UserSearchTableController(uureq, getWindowControl(), stackPanel, UserSearchTableSettings.minimal());
			listenTo(inactiveUserCtrl);
			
			SearchIdentityParams inactiveSearchParams = getInactiveParams();
			inactiveUserCtrl.loadModel(inactiveSearchParams);
			return inactiveUserCtrl;
		}));

		// ready to delete
		lifecycleTabbedPane.addTabControllerCreator(ureq, translate("overview.ready.to.delete.user"), uureq -> {
			readyToDeleteUserCtrl = new UserSearchTableController(uureq, getWindowControl(), stackPanel, UserSearchTableSettings.minimal());
			listenTo(readyToDeleteUserCtrl);
			
			SearchIdentityParams readyToDeleteSearchParams = getReadyToDeleteParams();
			readyToDeleteUserCtrl.loadModel(readyToDeleteSearchParams);
			return readyToDeleteUserCtrl;
		});
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && entries.isEmpty()) return;
		
		lifecycleTabbedPane.activate(ureq, entries, state);
	}
	
	private SearchIdentityParams getReadyToInactivateParams() {
		SearchIdentityParams params = new SearchIdentityParams();
		
		Date lastLoginBefore;
		if(userModule.isUserAutomaticDeactivation()) {
			int numOfDaysBeforeDeactivation = userModule.getNumberOfInactiveDayBeforeDeactivation();
			int numOfDaysBeforeEmail = userModule.getNumberOfDayBeforeDeactivationMail();
			lastLoginBefore = toDate(numOfDaysBeforeDeactivation - numOfDaysBeforeEmail);
		} else {
			lastLoginBefore = toDate(userModule.getNumberOfInactiveDayBeforeDeactivation());
		}
		params.setUserLoginBefore(lastLoginBefore);
		
		List<Integer> statusList = List.of(Identity.STATUS_ACTIV, Identity.STATUS_PENDING, Identity.STATUS_LOGIN_DENIED);
		params.setExactStatusList(statusList);
		params.setOrganisations(manageableOrganisations);
		params.setExcludedRoles(new OrganisationRoles[] { OrganisationRoles.guest });
		return params;
	}
	
	private SearchIdentityParams getInactiveParams() {
		SearchIdentityParams params = new SearchIdentityParams();
		params.setExactStatusList(List.of(Identity.STATUS_INACTIVE));
		params.setOrganisations(manageableOrganisations);
		params.setExcludedRoles(new OrganisationRoles[] { OrganisationRoles.guest });
		return params;
	}
	
	private SearchIdentityParams getReadyToDeleteParams() {
		SearchIdentityParams params = new SearchIdentityParams();
		
		Date lastLoginBefore;
		if(userModule.isUserAutomaticDeletion()) {
			int numOfDaysBeforeDeactivation = userModule.getNumberOfInactiveDayBeforeDeactivation();
			int numOfDaysBeforeEmail = userModule.getNumberOfDayBeforeDeactivationMail();
			lastLoginBefore = toDate(numOfDaysBeforeDeactivation - numOfDaysBeforeEmail);
		} else {
			lastLoginBefore = toDate(userModule.getNumberOfInactiveDayBeforeDeactivation());
		}
		params.setUserLoginBefore(lastLoginBefore);
		params.setExactStatusList(List.of(Identity.STATUS_INACTIVE));
		params.setOrganisations(manageableOrganisations);
		params.setExcludedRoles(new OrganisationRoles[] { OrganisationRoles.guest });
		return params;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private Date toDate(int days) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -days);
		return CalendarUtils.startOfDay(cal.getTime());
	}
}
