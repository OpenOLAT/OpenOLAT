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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
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
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.user.UserAvatarMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementPendingUsersController extends AbstractMembersController implements FlexiTableComponentDelegate, Activateable2 {

	protected static final String FILTER_CONFIRMATION_BY = "confirmationBy";
	protected static final String FILTER_CONFIRMATION_DATE = "confirmationDate";
	
	private FormLink acceptAllButton;
	private FormLink addReservationButton;

	private CloseableModalController cmc;
	private UserSearchController userSearchCtrl;
	
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController calloutCtrl;

	private boolean memberChanged;
	private final boolean membersManaged;

	@Autowired
	private ACService acService;
	
	public CurriculumElementPendingUsersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback,
			UserAvatarMapper avatarMapper, String avatarMapperBaseURL) {
		super(ureq, wControl, toolbarPanel, "curriculum_element_pending",
				curriculumElement, secCallback, avatarMapper, avatarMapperBaseURL);
	
		membersManaged = CurriculumElementManagedFlag.isManaged(curriculumElement, CurriculumElementManagedFlag.members);

		initForm(ureq);
	}

	@Override
	protected void initButtonsForm(FormItemContainer formLayout) {
		super.initButtonsForm(formLayout);
		
		// Add/remove buttons
		if(!membersManaged && secCallback.canManagerCurriculumElementUsers(curriculumElement)) {
			acceptAllButton = uifactory.addFormLink("accept.all", formLayout, Link.BUTTON);
			acceptAllButton.setIconLeftCSS("o_icon o_icon-fw o_icon_accept_all");
			
			addReservationButton = uifactory.addFormLink("add.member", formLayout, Link.BUTTON);
			addReservationButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.pending));
	}

	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		SelectionValues rolesValues = new SelectionValues();
		rolesValues.add(SelectionValues.entry(ConfirmationByEnum.ADMINISTRATIVE_ROLE.name(), translate("confirmation.membership.by.admin")));
		rolesValues.add(SelectionValues.entry(ConfirmationByEnum.PARTICIPANT.name(), translate("confirmation.membership.by.participant")));
		FlexiTableSingleSelectionFilter confirmationByFilter = new FlexiTableSingleSelectionFilter(translate("filter.confirmation.by"),
				FILTER_CONFIRMATION_BY, rolesValues, true);
		filters.add(confirmationByFilter);

		SelectionValues datesValues = new SelectionValues();
		datesValues.add(SelectionValues.entry("true", translate("filter.confirmation.date")));
		FlexiTableOneClickSelectionFilter confirmationDateFilter = new FlexiTableOneClickSelectionFilter(translate("filter.confirmation.date"),
				FILTER_CONFIRMATION_DATE, datesValues, true);
		filters.add(confirmationDateFilter);
	}

	@Override
	protected void initFiltersPresets(List<FlexiFiltersTab> tabs) {
		FlexiFiltersTab confirmByAdminTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ConfirmationByEnum.ADMINISTRATIVE_ROLE.name(), translate("search.confirm.by.admin"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_CONFIRMATION_BY,
						ConfirmationByEnum.ADMINISTRATIVE_ROLE.name())));
		tabs.add(confirmByAdminTab);
		
		FlexiFiltersTab confirmByParticipantsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CurriculumRoles.coach.name(), translate("search.confirm.by.participant"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_CONFIRMATION_BY,
						ConfirmationByEnum.PARTICIPANT.name())));
		tabs.add(confirmByParticipantsTab);
		
		FlexiFiltersTab withConfirmationDateTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FILTER_CONFIRMATION_DATE, translate("search.confirm.date"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_CONFIRMATION_DATE, Boolean.TRUE)));
		tabs.add(withConfirmationDateTab);
	}
	
	private void reloadMember(UserRequest ureq, Identity member) {
		boolean openDetails = false;
		MemberRow row = tableModel.getObject(member);
		if(row != null && row.getDetailsController() != null) {
			doCloseMemberDetails(row);
			openDetails = true;
		}
		loadModel(false);
		if(openDetails) {
			MemberRow reloadedRow = tableModel.getObject(member);
			doOpenMemberDetails(ureq, reloadedRow);
		}
	}
	
	@Override
	protected void loadModel(boolean reset) {
		// Reservations
		List<OLATResource> resources = getCurriculumElementsResources();
		List<ResourceReservation> reservations = acService.getReservations(resources);

		// Memberships
		SearchMemberParameters params = getSearchCurriculumElementParameters();
		List<CurriculumMember> members = curriculumService.getCurriculumElementsMembers(params);
		
		Map<Long,MemberRow> keyToMemberMap = new HashMap<>();
		List<Long> loadStatus = new ArrayList<>();
		for(ResourceReservation reservation:reservations) {
			final Identity member = reservation.getIdentity();
			CurriculumRoles role = ResourceToRoleKey.reservationToRole(reservation.getType());
			if(role != null) {
				MemberRow row = keyToMemberMap.computeIfAbsent(reservation.getIdentity().getKey(),
						key -> new MemberRow(member, userPropertyHandlers, getLocale()));
				row.addReservation(role, reservation);
				
				forgeLinks(row);
				forgeOnlineStatus(row, loadStatus);
			}
		}
		
		for(CurriculumMember member:members) {
			MemberRow row = keyToMemberMap.get(member.getIdentity().getKey());
			if(row != null && CurriculumRoles.isValueOf(member.getRole())) {
				CurriculumRoles role = CurriculumRoles.valueOf(member.getRole());
				row.addRole(role);
			}
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
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(userSearchCtrl == source) {
			if (event instanceof SingleIdentityChosenEvent singleEvent) {
				Identity choosenIdentity = singleEvent.getChosenIdentity();
				if (choosenIdentity != null) {
					List<Identity> toAdd = Collections.singletonList(choosenIdentity);
					doAddMemberReservation(toAdd, (CurriculumRoles)userSearchCtrl.getUserObject());
				}
			} else if (event instanceof MultiIdentityChosenEvent multiEvent) {
				if(!multiEvent.getChosenIdentities().isEmpty()) {
					doAddMemberReservation(multiEvent.getChosenIdentities(), (CurriculumRoles)userSearchCtrl.getUserObject());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(editSingleMemberCtrl == source) {
			if(event == Event.BACK_EVENT) {
				toolbarPanel.popController(editSingleMemberCtrl);
				if(memberChanged) {
					reloadMember(ureq, editSingleMemberCtrl.getMember());
					memberChanged = false;
				}
				cleanUp();
			} else if(event == Event.CHANGED_EVENT) {
				memberChanged = true;
			}
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(event instanceof EditMemberEvent ede) {
			doEditMember(ureq, ede.getMember());
		} else if(cmc == source || calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Autowired
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(cmc);
		userSearchCtrl = null;
		calloutCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addReservationButton == source) {
			doAddReservation(ureq);
		} else if(acceptAllButton == source) {
			doAcceptAll(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doAcceptAll(UserRequest ureq) {
		// TODO curriculum
		getWindowControl().setWarning("Not implemented");
	}
	
	private void doAccept(UserRequest ureq, MemberRow member) {
		// TODO curriculum
		getWindowControl().setWarning("Not implemented");
	}
	
	private void doDecline(UserRequest ureq, MemberRow member) {
		// TODO curriculum
		getWindowControl().setWarning("Not implemented");
	}
	
	private void doAddReservation(UserRequest ureq) {
		doAddReservation(ureq, CurriculumRoles.participant);
	}
	
	private void doAddReservation(UserRequest ureq, CurriculumRoles role) {
		if(guardModalController(userSearchCtrl)) return;

		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);
		userSearchCtrl.setUserObject(role);
		listenTo(userSearchCtrl);
		
		String title = translate("add.member.role",  translate("role.".concat(role.name())));
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddMemberReservation(List<Identity> identitiesToAdd, CurriculumRoles role) {
		for(Identity identityToAdd:identitiesToAdd) {
			curriculumService.addMemberReservation(curriculumElement, identityToAdd, role, null, Boolean.TRUE, getIdentity(), null);
		}
		loadModel(true);
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
		private final Link acceptLink;
		private final Link declineLink;
		private final Link editMemberLink;
		private final VelocityContainer mainVC;
		
		private MemberRow member;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, MemberRow member) {
			super(ureq, wControl, Util
					.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
			this.member = member;

			mainVC = createVelocityContainer("tools");

			contactLink = addLink("contact", "contact", "o_icon o_icon-fw o_icon_mail");
			acceptLink = addLink("accept", "accept", "o_icon o_icon-fw o_icon_check");
			declineLink = addLink("decline", "decline", "o_icon o_icon-fw o_icon_decline");
			editMemberLink = addLink("edit.member", "edit.member", "o_icon o_icon-fw o_icon_edit");
			
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
			} else if(acceptLink == source) {
				doAccept(ureq, member);
			} else if(declineLink == source) {
				doDecline(ureq, member);
			}
		}
	}
}
