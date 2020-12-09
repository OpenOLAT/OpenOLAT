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
package org.olat.group.ui.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.course.member.PermissionHelper;
import org.olat.course.member.PermissionHelper.BGPermission;
import org.olat.course.member.PermissionHelper.RepoPermission;
import org.olat.course.member.wizard.MembersContext;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.ui.main.EditMemberShipReviewTableModel.EditMemberShipReviewCols;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Initial date: Nov 13, 2020<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class EditMembershipStep2 extends BasicStep {

	private List<Identity> identities;
	private MembersContext membersContext;
	
	public EditMembershipStep2(UserRequest ureq, List<Identity> identities, MembersContext membersContext) {
		super(ureq);

		this.identities = identities;
		this.membersContext = membersContext;

		setI18nTitleAndDescr("edit.member.confirm", null);
		
		if (membersContext.isSendMailMandatory()) {
			setNextStep(Step.NOSTEP);
		} else {
			setNextStep(new EditMembershipStep3(ureq));
		}		
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !membersContext.isSendMailMandatory(), membersContext.isSendMailMandatory());
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form form) {
		return new EditMembershipConfirmationController(ureq, wControl, form, stepsRunContext);
	}

	private class EditMembershipConfirmationController extends StepFormBasicController {

		private MemberPermissionChangeEvent changeEvent;
		private List<Identity> members;
		private EditMemberShipReviewTableModel tableModel;
		
		@Autowired
		private RepositoryManager repositoryManager;
		@Autowired
		private CurriculumService curriculumService;
		@Autowired
		private BusinessGroupService businessGroupService;
		
		@SuppressWarnings("unchecked")
		public EditMembershipConfirmationController(UserRequest ureq, WindowControl wControl, Form rootForm,
				StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

			changeEvent = (MemberPermissionChangeEvent) runContext.get("membershipChanges");
			members = (List<Identity>) runContext.get("members");
			
			initForm(ureq);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {			
			
			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			FlexiCellRenderer cellRenderer = new EditMembershipReviewTableRenderer();
			TreeNodeFlexiCellRenderer treeRenderer = new TreeNodeFlexiCellRenderer(false);
			
			
			DefaultFlexiColumnModel changeElementOrName = new DefaultFlexiColumnModel(EditMemberShipReviewCols.name);
			changeElementOrName.setCellRenderer(treeRenderer);
			DefaultFlexiColumnModel participant = new DefaultFlexiColumnModel(EditMemberShipReviewCols.participant);
			participant.setCellRenderer(cellRenderer);
			DefaultFlexiColumnModel coach = new DefaultFlexiColumnModel(EditMemberShipReviewCols.coach);
			coach.setCellRenderer(cellRenderer);
			DefaultFlexiColumnModel owner = new DefaultFlexiColumnModel(EditMemberShipReviewCols.owner);
			owner.setCellRenderer(cellRenderer);
			DefaultFlexiColumnModel waitingList = new DefaultFlexiColumnModel(EditMemberShipReviewCols.waitingList);
			waitingList.setCellRenderer(cellRenderer);
			
			// Table objects
			List<EditMembershipReviewTableRow> objects = new ArrayList<>();
			boolean hideWaitingListColumn = true;
			
			// Repo changes
			if (changeEvent.getRepoChanges() != null && changeEvent.getRepoChanges().size() > 0) {
				EditMembershipReviewTableRow repoChanges = new EditMembershipReviewTableRow(null, 1, true);
				repoChanges.setNameOrIdentifier(translate("review.member.course.changes"));
				objects.add(repoChanges);
				
				for (RepositoryEntryPermissionChangeEvent change : changeEvent.getRepoChanges()) {
					RepoPermission repoPermission = PermissionHelper.getPermission(membersContext.getRepoEntry(), change.getMember(), repositoryManager.getRepositoryEntryMembership(membersContext.getRepoEntry(), change.getMember()));
					
					EditMembershipReviewTableRow changeRow = new EditMembershipReviewTableRow(repoChanges, 0, false);
					changeRow.setNameOrIdentifier(change.getMember().getUser().getFirstName() + " " + change.getMember().getUser().getLastName());
					
					if (change.getRepoParticipant() != null) {
						if (change.getRepoParticipant()) {
							changeRow.setParticipantPermissionState(2);
							changeRow.increaseTotalAddedParticipant();
						} else {
							changeRow.setParticipantPermissionState(4);
							changeRow.increaseTotalRemovedParticipant();
						}
						
					} else if (repoPermission.isParticipant()) {
						changeRow.setParticipantPermissionState(1);
					} else {
						changeRow.setParticipantPermissionState(3);
					}
					
					if (change.getRepoOwner() != null) {
						if (change.getRepoOwner()) {
							changeRow.setOwnerPermissionState(2);
							changeRow.increaseTotalAddedOwner();
						} else {
							changeRow.setOwnerPermissionState(2);
							changeRow.increaseTotalRemovedOwner();
						}
					} else if (repoPermission.isOwner()) {
						changeRow.setOwnerPermissionState(1);
					} else {
						changeRow.setOwnerPermissionState(3);
					}
					
					if (change.getRepoTutor() != null) {
						if (change.getRepoTutor()) {
							changeRow.setTutorPermissionState(2);
							changeRow.increaseTotalAddedPTutor();
						} else {
							changeRow.setTutorPermissionState(4);
							changeRow.increaseTotalRemovedTutor();
						}
						
					} else if (repoPermission.isTutor()) {
						changeRow.setTutorPermissionState(1);
					} else {
						changeRow.setTutorPermissionState(3);
					}
					
					objects.add(changeRow);
				}
			}
			
			// Group changes
			if (changeEvent.generateBusinessGroupMembershipChange(members).size() > 0) {
				EditMembershipReviewTableRow groupChanges = new EditMembershipReviewTableRow(null, 1, true);
				groupChanges.setNameOrIdentifier(translate("review.member.group.changes"));
				objects.add(groupChanges);
				
				List<Long> businessGroupKeys = changeEvent.getGroups().stream().map(group -> group.getKey()).collect(Collectors.toList());
				
				Map<BusinessGroupShort, List<EditMembershipReviewTableRow>> changedGroupTableRows = new HashMap<>();
				
				// Init helper lists and maps
				changeEvent.getGroups().forEach(group -> {
						List<EditMembershipReviewTableRow> rows = new ArrayList<>();
						EditMembershipReviewTableRow groupRow = new EditMembershipReviewTableRow(groupChanges, 1, true);
						groupRow.setNameOrIdentifier(group.getName());
						rows.add(groupRow);
						changedGroupTableRows.put(group, rows);
					});
				
				for (BusinessGroupMembershipChange change : changeEvent.generateBusinessGroupMembershipChange(members)) {
					List<BusinessGroupMembership> memberships = businessGroupService.getBusinessGroupMembership(businessGroupKeys, change.getMember());
					BGPermission bgPermission = PermissionHelper.getPermission(change.getGroupKey(), change.getMember(), memberships);
					
					EditMembershipReviewTableRow parentRow = changedGroupTableRows.get(change.getGroup()).get(0);
					EditMembershipReviewTableRow changeRow = new EditMembershipReviewTableRow(parentRow, 0, false);
					changeRow.setNameOrIdentifier(change.getMember().getUser().getFirstName() + " " + change.getMember().getUser().getLastName());
					
					if (change.getParticipant() != null) {
						if (change.getParticipant()) {
							changeRow.setParticipantPermissionState(2);
							parentRow.increaseTotalAddedParticipant();
						} else {
							changeRow.setParticipantPermissionState(4);
							parentRow.increaseTotalRemovedParticipant();
						}
					} else if (bgPermission.isParticipant()) {
						changeRow.setParticipantPermissionState(1);
					} else {
						changeRow.setParticipantPermissionState(3);
					}
					
					if (change.getTutor() != null) {
						if (change.getTutor()) {
							changeRow.setTutorPermissionState(2);
							parentRow.increaseTotalAddedPTutor();
						} else {
							changeRow.setTutorPermissionState(4);
							parentRow.increaseTotalRemovedTutor();
						}
					} else if (bgPermission.isTutor()) {
						changeRow.setTutorPermissionState(1);
					} else {
						changeRow.setTutorPermissionState(3);
					}
					
					if (change.getWaitingList() != null) {
						if (change.getWaitingList()) {
							changeRow.setWaitingListPermissionState(2);
							parentRow.increaseTotalAddedWaitingList();
						} else {
							changeRow.setWaitingListPermissionState(4);
							parentRow.increaseTotalRemovedWaitingList();
						}
					} else if (bgPermission.isTutor()) {
						changeRow.setWaitingListPermissionState(1);
					} else {
						changeRow.setWaitingListPermissionState(3);
					}
					
					List<EditMembershipReviewTableRow> tableRows = changedGroupTableRows.get(change.getGroup());
					tableRows.add(changeRow);
					changedGroupTableRows.put(change.getGroup(), tableRows);
				}
				
				// Check if there are any changes in the groups
				changedGroupTableRows.forEach((group, changeList) -> {
					if (changeList.size() > 1) {
						objects.addAll(changeList);
					}
				});				
			}
			
			// Curriculum changes
			if (changeEvent.generateCurriculumElementMembershipChange(members).size() > 0) {
				EditMembershipReviewTableRow curriculumChanges = new EditMembershipReviewTableRow(null, 1, true);
				curriculumChanges.setNameOrIdentifier(translate("review.member.curriculum.changes"));
				objects.add(curriculumChanges);
				
				Map<CurriculumElement, List<EditMembershipReviewTableRow>> changedCurriculumTableRows = new HashMap<>();
				
				// Init helper map				
				for (CurriculumElementMembershipChange change : changeEvent.generateCurriculumElementMembershipChange(members)) {
					EditMembershipReviewTableRow parentRow;
					
					if (!changedCurriculumTableRows.containsKey(change.getElement())) {
						List<EditMembershipReviewTableRow> curriculumRows = new ArrayList<>();
						parentRow = new EditMembershipReviewTableRow(curriculumChanges, 1, true);
						parentRow.setNameOrIdentifier(change.getElement().getDisplayName());
						curriculumRows.add(parentRow);
						changedCurriculumTableRows.put(change.getElement(), curriculumRows);
					} else {
						parentRow = changedCurriculumTableRows.get(change.getElement()).get(0);
					}
					
					EditMembershipReviewTableRow changeRow = new EditMembershipReviewTableRow(parentRow, 0, false);
					changeRow.setNameOrIdentifier(change.getMember().getUser().getFirstName() + " " + change.getMember().getUser().getLastName());
					
					RepoPermission curriculumPermission = PermissionHelper.getPermission(change.getElement(), change.getMember(), curriculumService.getCurriculumElementMemberships(Collections.singletonList(change.getElement()), change.getMember()));
					
					if (change.getParticipant() != null) {
						if (change.getParticipant()) {
							changeRow.setParticipantPermissionState(2);
							changeRow.increaseTotalAddedParticipant();
						} else {
							changeRow.setParticipantPermissionState(4);
							changeRow.increaseTotalRemovedParticipant();
						}
					} else if (curriculumPermission.isParticipant()) {
						changeRow.setParticipantPermissionState(1);
					} else {
						changeRow.setParticipantPermissionState(3);
					}
					
					if (change.getCurriculumElementOwner() != null) {
						if (change.getCurriculumElementOwner()) {
							changeRow.setOwnerPermissionState(2);
							changeRow.increaseTotalAddedOwner();
						} else {
							changeRow.setParticipantPermissionState(4);
							changeRow.increaseTotalRemovedOwner();
						}
					} else if (curriculumPermission.isOwner()) {
						changeRow.setOwnerPermissionState(1);
					} else {
						changeRow.setOwnerPermissionState(3);
					}
					
					if (change.getCoach() != null) {
						if (change.getCoach()) {
							changeRow.setTutorPermissionState(2);
							changeRow.increaseTotalAddedPTutor();
						} else {
							changeRow.setTutorPermissionState(4);
							changeRow.increaseTotalRemovedTutor();
						}
					} else if (curriculumPermission.isMasterCoach()) {
						changeRow.setTutorPermissionState(1);
					} else {
						changeRow.setTutorPermissionState(3);
					}
					
					List<EditMembershipReviewTableRow> tableRows = changedCurriculumTableRows.get(change.getElement());
					tableRows.add(changeRow);
					changedCurriculumTableRows.put(change.getElement(), tableRows);
				}
				
				// Check if there are any changes in the curricula
				changedCurriculumTableRows.forEach((curriculum, changeList) -> {
					if (changeList.size() > 1) {
						objects.addAll(changeList);
					}
				});	
			}			
			
			
			columnsModel.addFlexiColumnModel(changeElementOrName);
			columnsModel.addFlexiColumnModel(owner);
			columnsModel.addFlexiColumnModel(coach);
			columnsModel.addFlexiColumnModel(participant);
			if(!hideWaitingListColumn) {
				columnsModel.addFlexiColumnModel(waitingList);
			}
			
			tableModel = new EditMemberShipReviewTableModel(columnsModel);
			tableModel.setObjects(objects);
			if (objects.size() > 20) {
				tableModel.closeAll();
				
			}
			
			FlexiTableElement tableElement = uifactory.addTableElement(getWindowControl(), "editMembershipReviewTable", tableModel, -1, false, getTranslator(), formLayout);
			tableElement.setEmtpyTableMessageKey("review.no.changes");
			tableElement.setCustomizeColumns(false);		
			
			
		}			

		@Override
		protected void formOK(UserRequest ureq) {
			// Save send mail config
			addToRunContext("sendMail", membersContext.isSendMailMandatory());
			
			// Fire event
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose here

		}

	}

}
