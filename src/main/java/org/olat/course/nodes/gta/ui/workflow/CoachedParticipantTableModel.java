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
package org.olat.course.nodes.gta.ui.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.model.SearchAssessedIdentityParams.Passed;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.DueDate;

/**
 * 
 * Initial date: 25 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachedParticipantTableModel  extends DefaultFlexiTableDataModel<CoachedParticipantRow>
implements SortableFlexiTableDataModel<CoachedParticipantRow>, FilterableFlexiTableModel {
	
	private static final CoachCols[] COLS = CoachCols.values();
	
	private final Locale locale;
	private final Identity identity;
	private List<CoachedParticipantRow> backupRows;
	
	public CoachedParticipantTableModel(FlexiTableColumnModel columnModel, Identity identity, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.identity = identity;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CoachedParticipantRow> views = new CoachedParticipantTableModelSort(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public void filter(String quickSearch, List<FlexiTableFilter> filters) {
		if(filters != null && (StringHelper.containsNonWhitespace(quickSearch) || (!filters.isEmpty() && filters.get(0) != null))) {
			String searchString = StringHelper.containsNonWhitespace(quickSearch) ? quickSearch.toLowerCase() : null;
			boolean assignedToMe = isFilterAssignedToMe(filters);
			boolean needRevisions = isFilterNeedRevisions(filters);
			boolean reviewed = isFilterReviewed(filters);
			boolean revisionsReviewed = isFilterRevisionsReviewed(filters);
			boolean toRelease = isFilterToRelease(filters);
			List<Passed> passed = getFilterPassed(filters);
			List<CoachedParticipantStatus> assignmentStatus = getFilterByStatus(filters);
			List<CoachedParticipantRow> filteredRows = backupRows.stream()
						.filter(row -> acceptSearch(row, searchString)
								&& acceptBySyntheticStatis(row, assignmentStatus)
								&& isAssignedToMe(row, assignedToMe)
								&& isInRevisions(row, needRevisions, reviewed)
								&& isInRevisionsReviewed(row, revisionsReviewed)
								&& isToRelease(row, toRelease)
								&& isPassed(row, passed))
						.collect(Collectors.toList());
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupRows);
		}
	}
	
	private boolean acceptSearch(CoachedParticipantRow row, String searchString) {
		if(searchString == null) return true;
		
		String[] userProps = row.getIdentityProps();
		for(String userProp:userProps) {
			if(userProp != null && userProp.toLowerCase().contains(searchString)) {
				return true;
			}
		}
		return false;
	}
	
	private List<CoachedParticipantStatus> getFilterByStatus(List<FlexiTableFilter> filters) {
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, AbstractCoachWorkflowListController.FILTER_STATUS);
		if(statusFilter instanceof FlexiTableExtendedFilter extendedStatusFilter) {
			List<String> filterValues = extendedStatusFilter.getValues();
			return CoachedParticipantStatus.toList(filterValues);
		}
		return List.of();
	}
	
	private boolean acceptBySyntheticStatis(CoachedParticipantRow row, List<CoachedParticipantStatus> status) {
		if(status == null || status.isEmpty()) return true;

		CoachedParticipantStatus rowStatus = row.getStatus();
		return rowStatus != null && status.contains(rowStatus);
	}
	
	private boolean isFilterAssignedToMe(List<FlexiTableFilter> filters) {
		FlexiTableFilter assignmentFilter = FlexiTableFilter.getFilter(filters, AbstractCoachWorkflowListController.FILTER_ASSIGNED_TO_ME);
		if(assignmentFilter != null) {
			String filterValue = assignmentFilter.getValue();
			return AbstractCoachWorkflowListController.FILTER_ASSIGNED_TO_ME.equals(filterValue);
		}
		return false;
	}
	
	private boolean isAssignedToMe(CoachedParticipantRow row, boolean filterAssignedToMe) {
		if(!filterAssignedToMe) return true;

		Identity assignedCoach = row.getCoach();
		return assignedCoach != null && assignedCoach.equals(identity);
	}

	private boolean isInRevisions(CoachedParticipantRow row, boolean needRevisions, boolean reviewed) {
		if(!needRevisions && !reviewed) return true;
		if(needRevisions && reviewed) return false;
		
		if(needRevisions) {
			return row.getTask() != null && row.getTask().getTaskStatus() == TaskProcess.revision;
		}
		if(reviewed) {
			return row.getTask() != null && row.getTask().getAcceptationDate() != null;
		}
		return true;
	}
	
	private boolean isFilterNeedRevisions(List<FlexiTableFilter> filters) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, AbstractCoachWorkflowListController.FILTER_NEED_REVISIONS);
		if(filter != null) {
			String filterValue = filter.getValue();
			return AbstractCoachWorkflowListController.FILTER_NEED_REVISIONS.equals(filterValue);
		}
		return false;
	}
	
	private boolean isFilterReviewed(List<FlexiTableFilter> filters) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, AbstractCoachWorkflowListController.FILTER_REVIEWED);
		if(filter != null) {
			String filterValue = filter.getValue();
			return AbstractCoachWorkflowListController.FILTER_REVIEWED.equals(filterValue);
		}
		return false;
	}
	
	private boolean isInRevisionsReviewed(CoachedParticipantRow row, boolean revisionsReviewed) {
		if(!revisionsReviewed) return true;

		if(revisionsReviewed) {
			return row.getTask() != null && row.getTask().getCollectionRevisionsDate() != null;
		}
		return true;
	}
	
	private boolean isFilterRevisionsReviewed(List<FlexiTableFilter> filters) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, AbstractCoachWorkflowListController.FILTER_REVISIONS_REVIEWED);
		if(filter != null) {
			String filterValue = filter.getValue();
			return AbstractCoachWorkflowListController.FILTER_REVISIONS_REVIEWED.equals(filterValue);
		}
		return false;
	}
	
	private boolean isFilterToRelease(List<FlexiTableFilter> filters) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, AbstractCoachWorkflowListController.FILTER_TO_RELEASE);
		if(filter != null) {
			String filterValue = filter.getValue();
			return AbstractCoachWorkflowListController.FILTER_TO_RELEASE.equals(filterValue);
		}
		return false;
	}
	
	private boolean isToRelease(CoachedParticipantRow row, boolean toRelease) {
		return !toRelease || row.getUserVisibility() == null || !row.getUserVisibility().booleanValue();
	}
	
	private List<Passed> getFilterPassed(List<FlexiTableFilter> filters) {
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, AbstractCoachWorkflowListController.FILTER_PASSED);
		if(statusFilter instanceof FlexiTableExtendedFilter extendedStatusFilter) {
			List<String> filterValues = extendedStatusFilter.getValues();
			if(filterValues != null) {
				return filterValues.stream()
						.map(Passed::valueOf).toList();
			}
		}
		return List.of();
	}
	
	private boolean isPassed(CoachedParticipantRow row, List<Passed> passed) {
		if(passed == null || passed.isEmpty()) return true;
		
		return (passed.contains(Passed.passed) && row.getPassed() != null && row.getPassed().booleanValue())
				|| (passed.contains(Passed.failed) && row.getPassed() != null && !row.getPassed().booleanValue())
				|| (passed.contains(Passed.notGraded) && row.getPassed() == null);
	}

	public int indexOf(IdentityRef identity) {
		if(identity == null) return -1;
		
		for(int i=getRowCount(); i-->0; ) {
			CoachedParticipantRow row = getObject(i);
			if(row.getIdentityKey().equals(identity.getKey())) {
				return i;
			}
		}
		
		return -1;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CoachedParticipantRow participant = getObject(row);
		return getValueAt(participant, col);
	}
	
	@Override
	public Object getValueAt(CoachedParticipantRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case mark -> row.getMarkLink();
				case taskStatus -> row.getTaskStatus();
				case taskStepStatus -> row.getStatus();
				case taskName -> getTask(row);
				case taskTitle -> getTaskTitle(row);
				case assignmentOverrideDueDate -> overrideDueDate(row.getAssignmentDueDate());
				case assignment -> row.getAsssignmentDate();
				case additionalInfosStatus -> row;
				case submissionOverrideDueDate -> overrideDueDate(row.getSubmissionDueDate());
				case submissionDate -> submissionDate(row);
				case numOfSubmissionDocs -> getNumOfSubmittedDocs(row);
				case solutionOverrideDueDate -> overrideDueDate(row.getSolutionDueDate());
				case revisionDocs -> getNumOfSubmittedRevisedDocs(row);
				case reviewAndCorrectionFeedback -> row;
				case reviewAndCorrectionAcceptationDate, revisionAcceptationDate -> row.getAcceptationDate();
				case revisionLoop -> row.getNumOfRevisionLoop();
				case revisionFeedback -> row;
				case userVisibility -> row.getUserVisibility();
				case score -> row;
				case passed -> row.getPassed();
				case assessmentDone -> row.getAssessmentDone();
				case assessmentStatus -> row.getAssessmentStatus();
				case coachAssignment -> row.getCoachFullName();
				case tools -> row.getToolsLink();
				default -> "ERROR";	
			};
		}
		if(col >= AbstractWorkflowListController.USER_PROPS_OFFSET) {
			int propIndex = col - AbstractWorkflowListController.USER_PROPS_OFFSET;
			return row.getIdentityProp(propIndex);
		}
		return "ERROR";
	}
	
	private Integer getNumOfSubmittedDocs(CoachedParticipantRow row) {
		if(row.getCollectionDate() != null) {
			return row.getCollectionNumOfDocs();
		}
		return row.getSubmissionNumOfDocs();
	}
	
	private Integer getNumOfSubmittedRevisedDocs(CoachedParticipantRow row) {
		if(row.getCollectionRevisionsDate() != null) {
			return row.getCollectionRevisionsNumOfDocs();
		}
		return row.getSubmissionRevisionsNumOfDocs();
	}
	
	private String getTaskTitle(CoachedParticipantRow row) {
		String title = row.getTaskTitle();
		if(!StringHelper.containsNonWhitespace(title)) {
			title = row.getTaskName();
		}
		return title;
	}
	
	private Object getTask(CoachedParticipantRow row) {
		if (row.getOpenTaskFileLink() == null && row.getDownloadTaskFileLink() == null) {
			return row.getTaskName();
		}
		if (row.getOpenTaskFileLink() == null) {
			return row.getDownloadTaskFileLink();
		} 
		return row.getOpenTaskFileLink();
	}
	
	private Date overrideDueDate(DueDate dueDate) {
		return dueDate == null ? null : dueDate.getOverridenDueDate();
	}
	
	private Date submissionDate(CoachedParticipantRow row) {
		Date date = row.getSubmissionDate();
		if(date == null || (row.getCollectionDate() != null && row.getCollectionDate().after(date))) {
			date = row.getCollectionDate();
		}
		return date;
	}
	
	@Override
	public void setObjects(List<CoachedParticipantRow> objects) {
		backupRows = new ArrayList<>(objects);
		super.setObjects(objects);
	}
	
	public enum CoachCols implements FlexiSortableColumnDef {
		mark("table.header.mark"),
		taskTitle("table.header.group.taskTitle"),
		taskName("table.header.group.taskName"),
		taskStatus("table.header.group.step"),
		taskStepStatus("table.header.step.status"),
		assignmentOverrideDueDate("table.header.assignment.overriden.duedate"),
		assignment("table.header.assignment"),
		submissionOverrideDueDate("table.header.submission.overriden.duedate"),
		submissionDate("table.header.submissionDate"),
		solutionOverrideDueDate("table.header.solution.overriden.duedate"),
		additionalInfosStatus("table.header.additional.infos"),
		reviewAndCorrectionFeedback("table.header.review.correction.feedback"),
		reviewAndCorrectionAcceptationDate("table.header.review.correction.acceptation.date"),
		revisionDocs("table.header.num.revision.documents"),
		revisionFeedback("table.header.revision.feedback"),
		revisionLoop("table.header.revision.loop"),
		revisionAcceptationDate("table.header.revision.acceptation.date"),
		userVisibility("table.header.userVisibility"),
		score("table.header.score"),
		passed("table.header.passed"),
		numOfSubmissionDocs("table.header.num.submissionDocs"),
		assessmentStatus("table.header.assessmentStatus"),
		assessmentDone("table.header.assessment.done"),
		coachAssignment("table.header.coach.assignment"),
		tools("table.header.tools")
		;
		
		private final String i18nKey;
		
		private CoachCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
