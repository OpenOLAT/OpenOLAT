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
package org.olat.modules.invitation.ui;

import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.group.ui.main.BusinessGroupListController;
import org.olat.modules.portfolio.ui.shared.InviteeBindersController;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.ui.list.RepositoryEntryListController;

/**
 * 
 * Initial date: 12 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationsMainController extends MainLayoutBasicController implements Activateable2 {
		
	public InvitationsMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		TooledStackedPanel content = new TooledStackedPanel("invitations-stack", getTranslator(), this);
		content.setNeverDisposeRootController(true);
		content.setToolbarAutoEnabled(true);
		content.setInvisibleCrumb(1);
		putInitialPanel(content);
		
		InvitationsOverviewController overviewCtrl = new InvitationsOverviewController(ureq, getWindowControl(), content);
		listenTo(overviewCtrl);
		
		LayoutMain3ColsController columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, overviewCtrl.getInitialComponent(), "invitations");
		columnLayoutCtr.addCssClassToMain("o_invitations");
		listenTo(columnLayoutCtr); // auto dispose later
		
		content.pushController(translate("overview"), columnLayoutCtr);
		addToHistory(ureq, overviewCtrl);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public static class InvitationsOverviewController extends BasicController {

		private final InviteeBindersController bindersCtrl;
		private final BusinessGroupListController businessGroupCtrl;
		private final RepositoryEntryListController repositoryEntryListCtrl;
		
		public InvitationsOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel) {
			super(ureq, wControl);
			
			VelocityContainer mainVC = createVelocityContainer("overview");
			
			Roles roles = ureq.getUserSession().getRoles();
			SearchMyRepositoryEntryViewParams searchParams = new SearchMyRepositoryEntryViewParams(getIdentity(), roles);
			searchParams.setMembershipOnly(true);
			searchParams.setMembershipMandatory(true);
			searchParams.setEntryStatus(RepositoryEntryStatusEnum.preparationToPublished());
			
			// Courses
			repositoryEntryListCtrl = new RepositoryEntryListController(ureq, getWindowControl(),
					searchParams, false, true, true, true, "entries-invitations", stackedPanel);
			listenTo(repositoryEntryListCtrl);
			mainVC.put("entries", repositoryEntryListCtrl.getInitialComponent());
			repositoryEntryListCtrl.selectFilterTab(ureq, repositoryEntryListCtrl.getMyEntriesPreset());
			
			// Business groups
			businessGroupCtrl = new BusinessGroupListController(ureq, getWindowControl(), "businessgroups-invitations");
			listenTo(businessGroupCtrl);
			mainVC.put("groups", businessGroupCtrl.getInitialComponent());
			businessGroupCtrl.selectFilterTab(ureq, businessGroupCtrl.getMyGroupsTab());
			
			// Portfolio
			bindersCtrl = new InviteeBindersController(ureq, getWindowControl(), stackedPanel);
			listenTo(bindersCtrl);
			mainVC.put("portfolio", bindersCtrl.getInitialComponent());
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
	}
}
