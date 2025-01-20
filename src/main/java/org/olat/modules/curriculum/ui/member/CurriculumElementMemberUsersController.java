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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown.CaretPosition;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.event.EditMemberEvent;
import org.olat.modules.curriculum.ui.member.MemberManagementTableModel.MemberCols;
import org.olat.modules.curriculum.ui.wizard.AddMember1SearchStep;
import org.olat.modules.curriculum.ui.wizard.AddMemberFinishCallback;
import org.olat.modules.curriculum.ui.wizard.EditMember1MembershipStep;
import org.olat.modules.curriculum.ui.wizard.EditMemberFinishCallback;
import org.olat.modules.curriculum.ui.wizard.EditMembersContext;
import org.olat.modules.curriculum.ui.wizard.MembersContext;
import org.olat.resource.accesscontrol.Offer;
import org.olat.user.UserAvatarMapper;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementMemberUsersController extends AbstractMembersController implements FlexiTableComponentDelegate, Activateable2 {

	private static final String CMD_ADD_MEMBER = "add-member";
	private static final CurriculumRoles[] ADMIN_ROLES = {
			CurriculumRoles.coach, CurriculumRoles.mastercoach,
			CurriculumRoles.owner, CurriculumRoles.curriculumelementowner
		};
	protected static final String FILTER_ROLE = "Role";

	private FormLink editBatchButton;
	private FormLink removeBatchButton;
	private FormLink contactBatchButton;
	private FormLink addParticipantButton;
	
	private CloseableModalController cmc;
	
	private ToolsController toolsCtrl;
	private StepsMainRunController addMemberCtrl;
	private StepsMainRunController editMemberCtrl;
	private RemoveMembershipsController removeCtrl;
	private CloseableCalloutWindowController calloutCtrl;

	private final boolean membersManaged;
	private final Map<CurriculumRoles,FlexiFiltersTab> rolesToTab = new EnumMap<>(CurriculumRoles.class);
	
	public CurriculumElementMemberUsersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback, UserAvatarMapper avatarMapper, String avatarMapperBaseURL) {
		super(ureq, wControl, toolbarPanel, "curriculum_element_members",
				curriculumElement, secCallback, avatarMapper, avatarMapperBaseURL);
		
		membersManaged = CurriculumElementManagedFlag.isManaged(curriculumElement, CurriculumElementManagedFlag.members);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initButtonsForm(FormItemContainer formLayout) {
		super.initButtonsForm(formLayout);
		
		contactBatchButton = uifactory.addFormLink("contact", formLayout, Link.BUTTON);
		
		// Add/remove buttons
		if(!membersManaged && secCallback.canManagerCurriculumElementUsers(curriculumElement)) {
			addParticipantButton = uifactory.addFormLink("add.participants", "add.participants", null, formLayout, Link.BUTTON);
			addParticipantButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
			
			DropdownItem addDropdown = uifactory.addDropdownMenu("add.members", null, null, flc, getTranslator());
			addDropdown.setOrientation(DropdownOrientation.right);
			addDropdown.setCaretPosition(CaretPosition.right);
			addDropdown.setAriaLabel(translate("add.member"));
			for(CurriculumRoles role:ADMIN_ROLES) {
				String id = "add.".concat(role.name());
				FormLink addMemberButton = uifactory.addFormLink(id, CMD_ADD_MEMBER, id, null, formLayout, Link.LINK);
				addMemberButton.setUserObject(role);
				addDropdown.addElement(addMemberButton);
			}
		
			removeBatchButton = uifactory.addFormLink("remove.memberships", formLayout, Link.BUTTON);
			editBatchButton = uifactory.addFormLink("edit.member", formLayout, Link.BUTTON);
		}
	}
	
	@Override
	protected void initTableForm(FormItemContainer formLayout) {
		super.initTableForm(formLayout);
		tableEl.addBatchButton(contactBatchButton);
		if(editBatchButton != null) {
			tableEl.addBatchButton(editBatchButton);
		}
		if(removeBatchButton != null) {
			tableEl.addBatchButton(removeBatchButton);
		}
	}
	
	@Override
	protected void initColumns(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.registration,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.role,
				new RolesFlexiCellRenderer(getTranslator())));
		NumOfCellRenderer numOfRenderer = new NumOfCellRenderer(descendants.size() + 1);	
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.asParticipant,
				numOfRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberCols.asCoach,
				numOfRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberCols.asMasterCoach,
				numOfRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberCols.asOwner,
				numOfRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberCols.asElementOwner,
				numOfRenderer));
	}

	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		SelectionValues rolesValues = new SelectionValues();
		rolesValues.add(SelectionValues.entry(CurriculumRoles.participant.name(), translate("search.role.participant")));
		rolesValues.add(SelectionValues.entry(CurriculumRoles.coach.name(), translate("search.role.coach")));
		rolesValues.add(SelectionValues.entry(CurriculumRoles.mastercoach.name(), translate("search.role.mastercoach")));
		rolesValues.add(SelectionValues.entry(CurriculumRoles.owner.name(), translate("search.role.course.owner")));
		rolesValues.add(SelectionValues.entry(CurriculumRoles.curriculumelementowner.name(), translate("search.role.owner")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.roles"),
				FILTER_ROLE, rolesValues, true);
		filters.add(statusFilter);
	}

	@Override
	protected void initFiltersPresets(List<FlexiFiltersTab> tabs) {
		FlexiFiltersTab participantsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.participant.name(), translate("search.role.participant"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE,
						List.of(CurriculumRoles.participant.name()))));
		tabs.add(participantsTab);
		rolesToTab.put(CurriculumRoles.participant, participantsTab);
		
		FlexiFiltersTab coachesTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.coach.name(), translate("search.role.coach"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE,
						List.of(CurriculumRoles.coach.name()))));
		tabs.add(coachesTab);
		rolesToTab.put(CurriculumRoles.coach, coachesTab);
		
		FlexiFiltersTab masterCoachesTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.mastercoach.name(), translate("search.role.mastercoach"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE,
						List.of(CurriculumRoles.mastercoach.name()))));
		tabs.add(masterCoachesTab);
		rolesToTab.put(CurriculumRoles.mastercoach, masterCoachesTab);
		
		FlexiFiltersTab ownersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.owner.name(), translate("search.role.course.owner"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE,
						List.of(CurriculumRoles.owner.name()))));
		tabs.add(ownersTab);
		rolesToTab.put(CurriculumRoles.owner, ownersTab);
		
		FlexiFiltersTab curriculumElementOwnersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.curriculumelementowner.name(), translate("search.role.owner"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE,
						List.of(CurriculumRoles.curriculumelementowner.name()))));
		tabs.add(curriculumElementOwnersTab);
		rolesToTab.put(CurriculumRoles.curriculumelementowner, curriculumElementOwnersTab);
	}
	
	@Override
	protected void loadModel(boolean reset) {
		SearchMemberParameters params = getSearchParameters();
		List<CurriculumMember> members = curriculumService.getCurriculumElementsMembers(params);
		
		Map<Long,MemberRow> keyToMemberMap = new HashMap<>();
		List<Long> loadStatus = new ArrayList<>();
		for(CurriculumMember member:members) {
			MemberRow row = keyToMemberMap.computeIfAbsent(member.getIdentity().getKey(),
					key -> new MemberRow(member, userPropertyHandlers, getLocale()));
			
			if(CurriculumRoles.isValueOf(member.getRole())) {
				CurriculumRoles role = CurriculumRoles.valueOf(member.getRole());
				row.addRole(role);
			}
		
			forgeLinks(row);
			forgeOnlineStatus(row, loadStatus);
			keyToMemberMap.put(row.getIdentityKey(), row);
		}
		
		loadImStatus(loadStatus, keyToMemberMap);
		
		List<MemberRow> rows = new ArrayList<>(keyToMemberMap.values());
		tableModel.setObjects(rows);
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(reset, reset, true);
	}
	
	private SearchMemberParameters getSearchParameters() {
		List<CurriculumElement> elements = getSearchCurriculumElements();
		SearchMemberParameters params = new SearchMemberParameters(elements);
		params.setSearchString(tableEl.getQuickSearchString());
		params.setUserProperties(userPropertyHandlers);
		return params;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(CurriculumRoles.isValueOf(type)) {
			FlexiFiltersTab tab = rolesToTab.get(CurriculumRoles.valueOf(type));
			if(tab == null) {
				tab = allTab;
			}
			tableEl.setSelectedFilterTab(ureq, tab);
			loadModel(true);
		} else if(tableEl.getSelectedFilterTab() == null) {
			tableEl.setSelectedFilterTab(ureq, allTab);
			loadModel(true);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(removeCtrl == source) {
			if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadModel(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == addMemberCtrl || source == editMemberCtrl) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
				cleanUp();
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				loadModel(true);
				cleanUp();
			}
		} else if(editSingleMemberCtrl == source) {
			if(event == Event.BACK_EVENT || event == Event.CLOSE_EVENT) {
				toolbarPanel.popController(editSingleMemberCtrl);
				reloadMember(ureq, editSingleMemberCtrl.getMember());
				cleanUp();
			}
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(event instanceof EditMemberEvent ede) {
			doEditMember(ureq, ede.getMember());
			return;
		} else if(cmc == source || calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(addMemberCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(removeCtrl);
		removeAsListenerAndDispose(cmc);
		addMemberCtrl = null;
		calloutCtrl = null;
		removeCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addParticipantButton == source) {
			doAddMemberWizard(ureq, CurriculumRoles.participant);
		} else if(removeBatchButton == source) {
			doRemoveMemberships(ureq);
		} else if(editBatchButton == source) {
			doEditMemberWizard(ureq);
		} else if(contactBatchButton == source) {
			doOpenContact(ureq);
		} else if(source instanceof FormLink link && CMD_ADD_MEMBER.equals(link.getCmd())
				&& link.getUserObject() instanceof CurriculumRoles role) {
			doAddMemberWizard(ureq, role);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doRemoveMemberships(UserRequest ureq) {
		Set<Integer> selectedRows = tableEl.getMultiSelectedIndex();
		List<MemberRow> rows = new ArrayList<>(selectedRows.size());
		for(Integer selectedRow:selectedRows) {
			MemberRow row = tableModel.getObject(selectedRow.intValue());
			if(row.getInheritanceMode() == GroupMembershipInheritance.root || row.getInheritanceMode() == GroupMembershipInheritance.none) {
				rows.add(row);
			}
		}
		doRemoveMemberships(ureq, rows);
	}

	private void doRemoveMemberships(UserRequest ureq, List<MemberRow> rows) {
		List<CurriculumElement> curriculumElements = getAllCurriculumElements();
		List<Identity> identities = rows.stream()
				.map(MemberRow::getIdentity)
				.toList();
		removeCtrl = new RemoveMembershipsController(ureq, getWindowControl(),
				curriculum, curriculumElement, curriculumElements,
				identities, GroupMembershipStatus.removed);
		listenTo(removeCtrl);
		
		String title = translate("remove.memberships.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), removeCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddMemberWizard(UserRequest ureq, CurriculumRoles role) {
		List<Offer> offers = (curriculumElement.getParent() == null)
			? acService.findOfferByResource(curriculumElement.getResource(), true, null, null)
			: List.of();
		MembersContext membersContex = new MembersContext(role, curriculum, curriculumElement, descendants, offers);
		
		AddMember1SearchStep step = new AddMember1SearchStep(ureq, membersContex);
		AddMemberFinishCallback finish = new AddMemberFinishCallback(membersContex);
		
		removeAsListenerAndDispose(addMemberCtrl);
		String title = translate("wizard.add." + role.name());
		addMemberCtrl = new StepsMainRunController(ureq, getWindowControl(), step, finish, null, title, "");
		listenTo(addMemberCtrl);
		getWindowControl().pushAsModalDialog(addMemberCtrl.getInitialComponent());
	}

	private void doEditMemberWizard(UserRequest ureq) {
		List<Identity> identities = getSelectedIdentities();
		List<CurriculumRoles> baseRoles = List.of(CurriculumRoles.participant);
		EditMembersContext membersContex = new EditMembersContext(identities, baseRoles, curriculum, curriculumElement, descendants);
		
		EditMember1MembershipStep step = new EditMember1MembershipStep(ureq, membersContex);
		EditMemberFinishCallback finish = new EditMemberFinishCallback(membersContex);
		
		removeAsListenerAndDispose(editMemberCtrl);
		String title = translate("wizard.edit.members");
		editMemberCtrl = new StepsMainRunController(ureq, getWindowControl(), step, finish, null, title, "");
		listenTo(editMemberCtrl);
		getWindowControl().pushAsModalDialog(editMemberCtrl.getInitialComponent());
	}
	
	@Override
	protected void doOpenMemberDetails(UserRequest ureq, MemberRow row) {
		super.doOpenMemberDetails(ureq, row, true, true);
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
		private final Link editMemberLink;
		private final Link removeMembershipsLink;
		private final VelocityContainer mainVC;
		
		private MemberRow member;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, MemberRow member) {
			super(ureq, wControl, Util
					.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
			this.member = member;

			mainVC = createVelocityContainer("tools");

			contactLink = addLink("contact", "contact", "o_icon o_icon-fw o_icon_mail");
			editMemberLink = addLink("edit.member", "edit.member", "o_icon o_icon-fw o_icon_edit");
			removeMembershipsLink = addLink("remove.memberships", "remove.memberships", "o_icon o_icon-fw o_icon_remove");
			
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
			} else if(editMemberLink == source) {
				doEditMember(ureq, member);
			} else if(removeMembershipsLink == source) {
				doRemoveMemberships(ureq, List.of(member));
			}
		}
	}
}
