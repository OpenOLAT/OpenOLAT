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
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeEvent;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeSelection;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.user.UserAvatarMapper;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementUserManagementController extends BasicController implements Activateable2 {

	private static final String ACTIVE_SCOPE = "Active";
	private static final String PENDING_SCOPE = "Pending";
	private static final String NON_MEMBERS_SCOPE = "NonMembers";
	private static final String HISTORY_SCOPE = "History";
	
	private static final String MAIN_CONTROLLER = "component";
	
	private final ScopeSelection searchScopes;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;

	private final CurriculumElement curriculumElement;
	private final String avatarMapperBaseURL;
	private final CurriculumSecurityCallback secCallback;
	private final UserAvatarMapper avatarMapper = new UserAvatarMapper(true);
	
	private CurriculumElementHistoryController historyCtrl;
	private CurriculumElementMemberUsersController membersCtrl;
	private CurriculumElementPendingUsersController pendingCtrl;
	private CurriculumElementNonMembersController nonMembersCtrl;
	
	public CurriculumElementUserManagementController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		this.curriculumElement = curriculumElement;

		avatarMapperBaseURL = registerCacheableMapper(ureq, "users-avatars", avatarMapper);
		
		mainVC = createVelocityContainer("curriculum_element_user_mgmt");
		
		// Scope active, pending...
		List<Scope> scopes = new ArrayList<>(4);
		scopes.add(ScopeFactory.createScope(ACTIVE_SCOPE, translate("search.active"), null, "o_icon o_icon_circle_check"));
		scopes.add(ScopeFactory.createScope(PENDING_SCOPE, translate("search.pending"), null, "o_icon o_icon_timelimit_half"));
		scopes.add(ScopeFactory.createScope(NON_MEMBERS_SCOPE, translate("search.non.members"), null, "o_icon o_icon_non_members"));
		scopes.add(ScopeFactory.createScope(HISTORY_SCOPE, translate("search.members.history"), null, "o_icon o_icon_history"));
		searchScopes = ScopeFactory.createScopeSelection("search.scopes", mainVC, this, scopes);
		
		putInitialPanel(mainVC);
		
		searchScopes.setSelectedKey(ACTIVE_SCOPE);
		doOpenMembers(ureq);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(PENDING_SCOPE.equalsIgnoreCase(type)) {
			Activateable2 ctrl = doOpenPending(ureq);
			if(ctrl != null) {
				searchScopes.setSelectedKey(PENDING_SCOPE);
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				ctrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else  if(ACTIVE_SCOPE.equalsIgnoreCase(type)) {
			Activateable2 ctrl = doOpen(ureq, type);
			if(ctrl != null) {
				searchScopes.setSelectedKey(ACTIVE_SCOPE);
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				ctrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(searchScopes == source) {
			if(event instanceof ScopeEvent se) {
				List<ContextEntry> all = BusinessControlFactory.getInstance().createCEListFromString("[All:0]");
				doOpen(ureq, se.getSelectedKey()).activate(ureq, all, null);
			}
		}
	}
	
	private Activateable2 doOpen(UserRequest ureq, String scope) {
		return switch(scope) {
			case ACTIVE_SCOPE -> doOpenMembers(ureq);
			case PENDING_SCOPE -> doOpenPending(ureq);
			case NON_MEMBERS_SCOPE -> doOpenNonMembers(ureq);
			case HISTORY_SCOPE -> doOpenHistory(ureq);
			default -> null;
		};
	}
	
	private Activateable2 doOpenMembers(UserRequest ureq) {
		removeAsListenerAndDispose(membersCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ACTIVE_SCOPE), null);
		membersCtrl = new CurriculumElementMemberUsersController(ureq, swControl, toolbarPanel,
				curriculumElement, secCallback, avatarMapper, avatarMapperBaseURL);
		listenTo(membersCtrl);
		
		mainVC.put(MAIN_CONTROLLER, membersCtrl.getInitialComponent());
		return membersCtrl;
	}
	
	private Activateable2 doOpenPending(UserRequest ureq) {
		removeAsListenerAndDispose(pendingCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(PENDING_SCOPE), null);
		pendingCtrl = new CurriculumElementPendingUsersController(ureq, swControl, toolbarPanel,
				curriculumElement, secCallback, avatarMapper, avatarMapperBaseURL);
		listenTo(pendingCtrl);
		
		mainVC.put(MAIN_CONTROLLER, pendingCtrl.getInitialComponent());
		return pendingCtrl;
	}
	
	private Activateable2 doOpenNonMembers(UserRequest ureq) {
		removeAsListenerAndDispose(nonMembersCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(NON_MEMBERS_SCOPE), null);
		nonMembersCtrl = new CurriculumElementNonMembersController(ureq, swControl, toolbarPanel,
				curriculumElement, secCallback, avatarMapper, avatarMapperBaseURL);
		listenTo(nonMembersCtrl);
		
		mainVC.put(MAIN_CONTROLLER, nonMembersCtrl.getInitialComponent());
		return nonMembersCtrl;
	}
	
	private Activateable2 doOpenHistory(UserRequest ureq) {
		removeAsListenerAndDispose(historyCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(HISTORY_SCOPE), null);
		historyCtrl = new CurriculumElementHistoryController(ureq, swControl, curriculumElement);
		listenTo(historyCtrl);
		
		mainVC.put(MAIN_CONTROLLER, historyCtrl.getInitialComponent());
		return historyCtrl;
	}
}
