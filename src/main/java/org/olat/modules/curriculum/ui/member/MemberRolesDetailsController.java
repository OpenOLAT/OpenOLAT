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
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistory;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.component.ConfirmationByCellRenderer;
import org.olat.modules.curriculum.ui.component.GroupMembershipHistoryComparator;
import org.olat.modules.curriculum.ui.component.GroupMembershipStatusRenderer;
import org.olat.modules.curriculum.ui.member.MemberRolesDetailsTableModel.MemberDetailsCols;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberRolesDetailsController extends FormBasicController {
	
	protected static final int ROLES_OFFSET = 500;
	protected static final int CONFIRMATION_BY_OFFSET = 1000;
	protected static final int CONFIRMATION_UNTIL_OFFSET = 1500;
	
	private FlexiTableElement tableEl;
	private FlexiTableColumnModel columnsModel;
	private MemberRolesDetailsTableModel tableModel;
	
	private final Identity member;
	private final Curriculum curriculum;
	private final MemberDetailsConfig config;
	private final List<CurriculumElement> elements;
	private final CurriculumElement selectedCurriculumElement;

	@Autowired
	private ACService acService;
	@Autowired
	private CurriculumService curriculumService;
	
	public MemberRolesDetailsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			Curriculum curriculum, CurriculumElement selectedCurriculumElement, List<CurriculumElement> elements,
			Identity member, MemberDetailsConfig config) {
		super(ureq, wControl, LAYOUT_CUSTOM, "member_details_roles", rootForm);
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale()));
		this.selectedCurriculumElement = selectedCurriculumElement;
		this.elements = new ArrayList<>(elements);
		this.curriculum = curriculum;
		this.member = member;
		this.config = config;
		
		initForm(ureq);
		loadModel();
	}
	
	public List<MemberRolesDetailsRow> getRolesDetailsRows() {
		return tableModel.getObjects();
	}
	
	public void setModifications(List<MembershipModification> modifications) {
		List<MemberRolesDetailsRow> rows = tableModel.getObjects();
		EnumMap<CurriculumRoles,Boolean> usedRoles = updateRolesColumnsVisibility(getRolesDetailsRows());
		
		Map<Long,MemberRolesDetailsRow> rowsMap = rows.stream()
				.collect(Collectors.toMap(MemberRolesDetailsRow::getKey, r -> r, (u, v) -> u));
		for(MembershipModification modification:modifications) {
			CurriculumElement element = modification.curriculumElement();
			MemberRolesDetailsRow row = rowsMap.get(element.getKey());
			if(row != null) {
				CurriculumRoles role = modification.role();
				row.addModification(role, modification);
				usedRoles.put(role, Boolean.TRUE);
			}
		}
		
		for(MemberRolesDetailsRow row:rows) {
			ModificationStatusSummary modificationSummary = evaluateModificationSummary(row);
			row.setModificationSummary(modificationSummary);
		}

		updateRolesColumnsVisibility(usedRoles);
		tableEl.reset(false, false, true);
	}
	
	public boolean hasReservations() {
		List<MemberRolesDetailsRow> rows = tableModel.getObjects();
		for(MemberRolesDetailsRow row:rows) {
			if(row.hasReservations()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("membership");
		
		columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberDetailsCols.key));
		if(config.withActivityColumns()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberDetailsCols.modifications,
				new ModificationCellRenderer(getTranslator())));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberDetailsCols.displayName,
				new TreeNodeFlexiCellRenderer(false)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberDetailsCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberDetailsCols.externalId));
		
		GroupMembershipStatusRenderer statusRenderer = new GroupMembershipStatusRenderer(getLocale());
		for(CurriculumRoles role:CurriculumRoles.curriculumElementsRoles()) {
			String i18nLabel = "role.".concat(role.name());
			DefaultFlexiColumnModel col = new DefaultFlexiColumnModel(i18nLabel, role.ordinal() + ROLES_OFFSET, null, false, null, statusRenderer);
			col.setDefaultVisible(true);
			col.setAlwaysVisible(false);
			columnsModel.addFlexiColumnModel(col);
			
			if(role == CurriculumRoles.participant && config.withConfirmationColumns()) {
				DefaultFlexiColumnModel byCol = new DefaultFlexiColumnModel("table.header.confirmation.by", role.ordinal() + CONFIRMATION_BY_OFFSET, false, null,
						new ConfirmationByCellRenderer(getTranslator()));
				byCol.setDefaultVisible(true);
				byCol.setAlwaysVisible(false);
				columnsModel.addFlexiColumnModel(byCol);
				
				DefaultFlexiColumnModel untilCol = new DefaultFlexiColumnModel("table.header.confirmation.until", role.ordinal() + CONFIRMATION_UNTIL_OFFSET, false, null);
				untilCol.setDefaultVisible(true);
				untilCol.setAlwaysVisible(false);
				columnsModel.addFlexiColumnModel(untilCol);
			}
		}

		String footerHeader = translate("table.footer.roles");
		tableModel = new MemberRolesDetailsTableModel(columnsModel, config.withActivityColumns(), footerHeader);
		tableEl = uifactory.addTableElement(getWindowControl(), "rolesTable", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setFooter(true);
		if(selectedCurriculumElement != null) {
			tableEl.setCssDelegate(new SelectedCurriculumElementCssDelegate());
		}
	}
	
	protected void loadModel() {
		// Memberships
		List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(curriculum, member);
		Map<Long, CurriculumElementMembership> membershipsMap = memberships.stream()
				.collect(Collectors.toMap(CurriculumElementMembership::getCurriculumElementKey, u -> u, (u, v) -> u));
		
		// Reservations
		List<ResourceReservation> reservations = acService.getReservations(member);
		Map<ResourceToRoleKey,ResourceReservation> reservationsMap = reservations.stream()
				.filter(reservation -> StringHelper.containsNonWhitespace(reservation.getType()))
				.filter(reservation -> reservation.getType().startsWith(CurriculumService.RESERVATION_PREFIX))
				.collect(Collectors.toMap(reservation -> new ResourceToRoleKey(ResourceToRoleKey.reservationToRole(reservation.getType()), reservation.getResource()) , r -> r, (u, v) -> u));
		
		// History
		CurriculumElementMembershipHistorySearchParameters searchParams = new CurriculumElementMembershipHistorySearchParameters();
		searchParams.setCurriculum(curriculum);
		searchParams.setIdentities(List.of(member));
		List<CurriculumElementMembershipHistory> curriculumHistory = curriculumService.getCurriculumElementMembershipsHistory(searchParams);
		Map<Long, CurriculumElementMembershipHistory> historyMap = curriculumHistory.stream()
				.collect(Collectors.toMap(CurriculumElementMembershipHistory::getCurriculumElementKey, u -> u, (u, v) -> u));

		List<MemberRolesDetailsRow> rows = new ArrayList<>();
		Map<Long, MemberRolesDetailsRow> rowsMap = new HashMap<>();
		for(CurriculumElement element:elements) {
			List<CurriculumRoles> elementRoles = new ArrayList<>();
			MemberRolesDetailsRow row = new MemberRolesDetailsRow(element, elementRoles);
			rows.add(row);
			rowsMap.put(row.getKey(), row);
			
			CurriculumElementMembership membership = membershipsMap.get(element.getKey());
			CurriculumElementMembershipHistory history = historyMap.get(element.getKey());
			forgeStatus(row, membership, history, reservationsMap);
		}
		
		for(MemberRolesDetailsRow row:rows) {
			if(row.getParentKey() != null) {
				row.setParent(rowsMap.get(row.getParentKey()));
			}
			
			ModificationStatusSummary modificationSummary = evaluateModificationSummary(row);
			row.setModificationSummary(modificationSummary);
		}
		
		// Update columns visibility
		EnumMap<CurriculumRoles,Boolean> usedRoles = updateRolesColumnsVisibility(rows);
		updateRolesColumnsVisibility(usedRoles);
		
		Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private EnumMap<CurriculumRoles,Boolean> updateRolesColumnsVisibility(List<MemberRolesDetailsRow> rows) {
		EnumMap<CurriculumRoles,Boolean> usedRoles = new EnumMap<>(CurriculumRoles.class);
		for(MemberRolesDetailsRow row:rows) {
			for(CurriculumRoles role:CurriculumRoles.values()) {
				if(usedRoles.containsKey(role)) {
					continue;
				}
				
				if(row.getStatus(role) != null) {
					usedRoles.put(role, Boolean.TRUE);
				}
			}
		}
		return usedRoles;
	}
	
	private void updateRolesColumnsVisibility(EnumMap<CurriculumRoles,Boolean> usedRoles) {
		if(config.alwaysVisibleRoles() != null && config.alwaysVisibleRoles().isEmpty()) {
			for(CurriculumRoles role:config.alwaysVisibleRoles()) {
				usedRoles.put(role, Boolean.TRUE);
			}
		}
		
		// Update columns visibility
		for(CurriculumRoles role:CurriculumRoles.values()) {
			FlexiColumnModel col = columnsModel.getColumnModelByIndex(role.ordinal() + ROLES_OFFSET);
			if(col instanceof DefaultFlexiColumnModel) {
				tableEl.setColumnModelVisible(col, usedRoles.containsKey(role));
			}
			
			if(role == CurriculumRoles.participant) {
				FlexiColumnModel confirmationCol = columnsModel.getColumnModelByIndex(role.ordinal() + CONFIRMATION_BY_OFFSET);
				if(confirmationCol instanceof DefaultFlexiColumnModel) {
					tableEl.setColumnModelVisible(confirmationCol, usedRoles.containsKey(role));
				}
				
				FlexiColumnModel confirmationUntilCol = columnsModel.getColumnModelByIndex(role.ordinal() + CONFIRMATION_UNTIL_OFFSET);
				if(confirmationUntilCol instanceof DefaultFlexiColumnModel) {
					tableEl.setColumnModelVisible(confirmationUntilCol, usedRoles.containsKey(role));
				}
			}
		}
	}
	
	private void forgeStatus(MemberRolesDetailsRow row, CurriculumElementMembership membership,
			CurriculumElementMembershipHistory history, Map<ResourceToRoleKey,ResourceReservation> reservationsMap) {
		List<CurriculumRoles> membershipRoles = membership == null ? List.of() : membership.getRoles();
		OLATResource resource = row.getCurriculumElement().getResource();
		
		for(CurriculumRoles role:CurriculumRoles.curriculumElementsRoles()) {
			ResourceReservation reservation = resource == null
					? null : reservationsMap.get(new ResourceToRoleKey(role, resource));
			
			if(membershipRoles.contains(role)) {
				row.addStatus(role, GroupMembershipStatus.active);
			} else if(reservation != null) {
				row.addStatus(role, GroupMembershipStatus.reservation);
				row.addConfirmationBy(role, Boolean.FALSE.equals(reservation.getUserConfirmable())
						? ConfirmationByEnum.ADMINISTRATIVE_ROLE : ConfirmationByEnum.PARTICIPANT);
				row.addConfirmationUntil(role, reservation.getExpirationDate());
			} else {
				GroupMembershipHistory lastHistory = lastHistoryPoint(role, history);
				if(lastHistory != null) {
					row.addStatus(role, lastHistory.getStatus());
				}
			}
		}
	}
	
	private GroupMembershipHistory lastHistoryPoint(CurriculumRoles role, CurriculumElementMembershipHistory history) {
		if(history == null) return null;
		
		List<GroupMembershipHistory> roleHistory = history.getHistory(role);
		if(roleHistory == null || roleHistory.isEmpty()) {
			return null;
		}
		if(roleHistory.size() == 1) {
			return roleHistory.get(0);
		}
		
		Collections.sort(roleHistory, new GroupMembershipHistoryComparator());
		return roleHistory.get(0);
	}
	
	private ModificationStatusSummary evaluateModificationSummary(MemberRolesDetailsRow row) {
		int hasElementAccessBefore = 0;
		int gainAccessAfter = 0;
		int gainAccessAfterReservation = 0;
		int gainReservationAfter = 0;
		int looseAccessAfter = 0;
		int looseReservationAfter = 0;
		
		for(CurriculumRoles role:CurriculumRoles.curriculumElementsRoles()) {
			GroupMembershipStatus currentStatus = row.getStatus(role);
			if(currentStatus == GroupMembershipStatus.active) {
				++hasElementAccessBefore;
			}
		}

		for(CurriculumRoles role:CurriculumRoles.curriculumElementsRoles()) {
			GroupMembershipStatus currentStatus = row.getStatus(role);
			GroupMembershipStatus modificationStatus = row.getModificationStatus(role);
			if(currentStatus == GroupMembershipStatus.reservation && modificationStatus == GroupMembershipStatus.active) {
				gainAccessAfterReservation++;
			} else if(currentStatus == GroupMembershipStatus.active) {
				if(modificationStatus != null && (modificationStatus == GroupMembershipStatus.removed
						|| modificationStatus == GroupMembershipStatus.cancel
						||  modificationStatus == GroupMembershipStatus.cancelWithFee)) {
					looseAccessAfter++;
				}
			} else if(currentStatus == GroupMembershipStatus.reservation) {
				if(modificationStatus != null && (modificationStatus == GroupMembershipStatus.removed
						|| modificationStatus == GroupMembershipStatus.cancel
						||  modificationStatus == GroupMembershipStatus.cancelWithFee)) {
					looseReservationAfter++;
				}
			} else if(modificationStatus != null && modificationStatus == GroupMembershipStatus.active) {
				gainAccessAfter++;
			} else if(modificationStatus != null && modificationStatus == GroupMembershipStatus.reservation) {
				gainReservationAfter++;
			}
		}
		
		boolean modification = false;
		boolean removal = false;
		boolean addition = false;
		if(hasElementAccessBefore == 0) {
			if(gainAccessAfter > 0) {
				addition |= true;
			} else if(gainAccessAfterReservation > 0 || looseReservationAfter > 0 || gainReservationAfter > 0) {
				modification |= true;
			}
		} else if(hasElementAccessBefore > 0) {
			if(hasElementAccessBefore == looseAccessAfter && gainAccessAfter == 0 && gainAccessAfterReservation == 0) {
				removal |= true;
			} else if(gainAccessAfter > 0 || looseAccessAfter > 0 || gainAccessAfterReservation > 0
					|| looseReservationAfter > 0 || gainReservationAfter > 0) {
				modification |= true;
			}
		}
		return new ModificationStatusSummary(modification, addition, removal, gainAccessAfter + looseAccessAfter);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private class SelectedCurriculumElementCssDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			MemberRolesDetailsRow row = tableModel.getObject(pos);
			if(selectedCurriculumElement.equals(row.getCurriculumElement())) {
				return "o_row_selected";
			}
			return null;
		}
	}
}
