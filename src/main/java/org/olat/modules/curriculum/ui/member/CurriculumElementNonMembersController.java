/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistory;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ResourceReservation;

/**
 * 
 * Initial date: 4 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementNonMembersController extends AbstractMembersController {

	private ToolsController	toolsCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	
	public CurriculumElementNonMembersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, toolbarPanel, "curriculum_element_non_members", curriculumElement, secCallback);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initColumns(FlexiTableColumnModel columnsModel) {
		//
	}
	
	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		//
	}

	@Override
	protected void initFiltersPresets(List<FlexiFiltersTab> tabs) {
		//
	}

	@Override
	protected void initTableForm(FormItemContainer formLayout, UserRequest ureq) {
		super.initTableForm(formLayout, ureq);
		
		tableEl.setAndLoadPersistedPreferences(ureq, "cpl-element-non-members-v1");
	}

	@Override
	protected void loadModel(boolean reset) {
		// Reservations
		List<OLATResource> resources = getCurriculumElementsResources();
		List<ResourceReservation> reservations = acService.getReservations(resources);
		Set<Long> reservationsSet = reservations.stream()
				.map(r -> r.getIdentity().getKey())
				.collect(Collectors.toSet());

		// Memberships
		List<CurriculumElement> elements = getSearchCurriculumElements();
		List<CurriculumMember> members = curriculumService
				.getCurriculumElementsMembers(new SearchMemberParameters(elements));
		Set<Long> membersSet = members.stream()
				.map(c -> c.getIdentity().getKey())
				.collect(Collectors.toSet());
		
		// History
		CurriculumElementMembershipHistorySearchParameters searchParams = new CurriculumElementMembershipHistorySearchParameters();
		searchParams.setElements(elements);
		List<CurriculumElementMembershipHistory> membershipsHistory = curriculumService
				.getCurriculumElementMembershipsHistory(searchParams);

		Map<Long,MemberRow> keyToMemberMap = new HashMap<>();
		List<Long> loadStatus = new ArrayList<>();
		for(CurriculumElementMembershipHistory history:membershipsHistory) {
			Identity identity = history.getIdentity();
			if(reservationsSet.contains(identity.getKey()) || membersSet.contains(identity.getKey())) {
				continue;
			}
			
			MemberRow row = keyToMemberMap.computeIfAbsent(identity.getKey(),
					key -> new MemberRow(identity, userPropertyHandlers, getLocale()));
			

			forgeLinks(row);
			forgeOnlineStatus(row, loadStatus);
		}
		
		loadImStatus(loadStatus, keyToMemberMap);

		List<MemberRow> rows = new ArrayList<>(keyToMemberMap.values());
		tableModel.setObjects(rows);
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(reset, reset, true);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		if(tableEl.getSelectedFilterTab() == null) {
			tableEl.setSelectedFilterTab(ureq, allTab);
			loadModel(true);
		}
	}
	
	@Override
	protected void doOpenMemberDetails(UserRequest ureq, MemberRow row) {
		super.doOpenMemberDetails(ureq, row, false, false);
	}
	
	@Override
	protected void doOpenTools(UserRequest ureq, MemberRow member, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), member);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final Link contactLink;
		private final VelocityContainer mainVC;
		
		private MemberRow member;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, MemberRow member) {
			super(ureq, wControl, Util
					.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
			this.member = member;

			mainVC = createVelocityContainer("tools");

			contactLink = addLink("contact", "contact", "o_icon o_icon-fw o_icon_mail");
			
			putInitialPanel(mainVC);
		}
		private Link addLink(String name, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			return link;
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(contactLink == source) {
				doOpenContact(ureq, member);
			}
		}
	}
}
