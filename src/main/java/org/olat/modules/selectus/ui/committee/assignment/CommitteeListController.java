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
package org.olat.modules.selectus.ui.committee.assignment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AssignmentService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.model.ApplicationAssignmentLight;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.committee.assignment.CommitteeListDataModel.CommitteeCols;
import org.olat.modules.selectus.ui.committee.list.PositionCommitteeController;
import org.olat.modules.selectus.ui.components.AssignmentsCellRenderer;

/**
 * 
 * Initial date: 24 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeListController extends StepFormBasicController {
	
	public static final String formIdentifyer = PositionCommitteeController.formIdentifyer;
	public static final int USER_PROP_OFFSET = PositionCommitteeController.USER_PROP_OFFSET;
	
	private FlexiTableElement tableEl;
	private CommitteeListDataModel tableModel;
	
	private final Position position;
	private final AssignmentsData data;
	private final boolean onlyAssignedCommittee;
	private final Set<PositionRole> ratingRoles;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private AssignmentService assignmentService;
	
	public CommitteeListController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form,
			AssignmentsData data, boolean onlyAssignedCommittee) {
		super(ureq, wControl, form, runContext, LAYOUT_CUSTOM, "committee_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(PositionController.class, ureq.getLocale(), getTranslator()));
		this.data = data;
		position = data.getPosition();
		this.onlyAssignedCommittee = onlyAssignedCommittee;
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(formIdentifyer, true);
		ratingRoles = recruitingModule.getRolesAllowedToRateSet();
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("committee.list.title");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CommitteeCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CommitteeCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CommitteeCols.role, new CommitteeRoleRenderer(getTranslator(), ratingRoles)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CommitteeCols.institution));
		
		RecruitingTableOption userPropertiesOption = recruitingModule.getTableCommitteeUserPropertiesOption();
		if(userPropertiesOption != RecruitingTableOption.disabled) {
			boolean visible = userPropertiesOption == RecruitingTableOption.enabled;
			
			int colIndex = USER_PROP_OFFSET;
			for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
				if (userPropertyHandler == null) continue;
				FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colIndex++, true, userPropertyHandler.getName());
				columnsModel.addFlexiColumnModel(col);
			}
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CommitteeCols.assignments, new AssignmentsCellRenderer()));
		
		tableModel = new CommitteeListDataModel(columnsModel, userPropertyHandlers, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setExportEnabled(false);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
	}
	
	private void loadModel() {
		List<AssigneeRow> rows = new ArrayList<>();

		List<ApplicationAssignmentLight> assignments = assignmentService.getAssignments(position);
		Map<Long, Long> assignmentsMap = assignments.stream()
		           .collect(Collectors.groupingBy(ApplicationAssignmentLight::getAssigneeKey, Collectors.counting()));

		List<Identity> committeeMembers = recruitingService.getCommitteeMembers(position);
		wrapRoles(committeeMembers, PositionRole.member, rows);
		List<Identity> secretaries = recruitingService.getSecretaries(position);
		wrapRoles(secretaries, PositionRole.secretary, rows);
		List<Identity> heads = recruitingService.getHeads(position);
		wrapRoles(heads, PositionRole.head, rows);
		if(recruitingModule.isRoleExOfficioEnabled()) {
			List<Identity> exOfficios = recruitingService.getExOfficios(position);
			wrapRoles(exOfficios, PositionRole.exofficio, rows);
		}
		
		if(onlyAssignedCommittee) {
			rows = getAssignedCommittee(rows, assignments);
		}

		List<UserRating> ratings = recruitingService.getRatings(position, committeeMembers);
		Map<Long, Long> ratingsMaps = ratings.stream()
		           .collect(Collectors.groupingBy(rating -> rating.getCreator().getKey(), Collectors.counting()));
		
		for(AssigneeRow row:rows) {
			Long identityKey = row.getIdentity().getKey();
			Long numOfAssignmets = assignmentsMap.get(identityKey);
			row.setNumOfAssignments(numOfAssignmets == null ? 0 : numOfAssignmets.intValue());
			
			if(ratingRoles.contains(row.getPositionRole())) {
				Long numOfRatings = ratingsMaps.get(identityKey);
				row.setNumOfRatings(numOfRatings == null ? 0 : numOfRatings.intValue());
			} else {
				row.setNumOfRatings(0);
			}
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private List<AssigneeRow> getAssignedCommittee(List<AssigneeRow> rows, List<ApplicationAssignmentLight> assignments) {

		Map<Long,AssigneeRow> committeeSet = rows.stream()
				.collect(Collectors.toMap(AssigneeRow::getIdentityKey, ident -> ident, (u, v) -> u));
		Set<Long> applicationKeys = data.getApplications().stream()
				.map(ApplicationLight::getKey).collect(Collectors.toSet());
		
		List<AssigneeRow> assignedCommittee = new ArrayList<>();
		Set<Long> assignedCommitteeSet = new HashSet<>();
		
		for(ApplicationAssignmentLight assignment:assignments) {
			if(applicationKeys.contains(assignment.getApplicationKey())
					&& !assignedCommitteeSet.contains(assignment.getAssigneeKey())) {
				AssigneeRow identity = committeeSet.get(assignment.getAssigneeKey());
				assignedCommitteeSet.add(assignment.getAssigneeKey());
				if(identity != null) {
					assignedCommittee.add(identity);
				}
			}
		}

		return assignedCommittee;
	}
	
	private void wrapRoles(List<Identity> identities, PositionRole role, List<AssigneeRow> rows) {
		for(Identity identity:identities) {
			rows.add(new AssigneeRow(identity, role));
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(tableEl.getMultiSelectedIndex().isEmpty()) {
			tableEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<Identity> assigneeList = getSelectedIdentities();
		data.setAssigneeList(assigneeList);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private List<Identity> getSelectedIdentities() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<Identity> identities = new ArrayList<>(selectedIndexes.size());
		for(Integer index:selectedIndexes) {
			AssigneeRow row = tableModel.getObject(index.intValue());
			identities.add(row.getIdentity());
		}
		return identities;
	}
	
	

}
