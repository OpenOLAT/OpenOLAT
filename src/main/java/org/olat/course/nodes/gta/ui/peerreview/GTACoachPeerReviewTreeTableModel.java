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
package org.olat.course.nodes.gta.ui.peerreview;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.ui.peerreview.CoachPeerReviewRow.NumOf;
import org.olat.course.nodes.gta.ui.workflow.CoachedParticipantStatus;

/**
 * 
 * Initial date: 10 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachPeerReviewTreeTableModel extends DefaultFlexiTreeTableDataModel<CoachPeerReviewRow>
implements SortableFlexiTableDataModel<CoachPeerReviewRow> {
	
	private static final CoachReviewCols[] COLS = CoachReviewCols.values();
	
	public GTACoachPeerReviewTreeTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			//
		}
	}

	@Override
	public void filter(String quickSearch, List<FlexiTableFilter> filters) {
		if(filters != null && (StringHelper.containsNonWhitespace(quickSearch) || (!filters.isEmpty() && filters.get(0) != null))) {
			List<CoachPeerReviewRow> filteredRows = backupRows;

			String searchString = StringHelper.containsNonWhitespace(quickSearch) ? quickSearch.toLowerCase() : null;
			TaskReviewAssignmentStatus assignmentStatus = getFilterAssignmentStatus(filters);
			List<CoachedParticipantStatus> stepStatus = getFilterStepStatus(filters);
			if(StringHelper.containsNonWhitespace(quickSearch) || assignmentStatus != null || stepStatus != null) {
				filteredRows = filteredRows.stream()
							.filter(row -> acceptSearch(row, searchString)
									&& acceptAssignmentStatus(row, assignmentStatus)
									&& acceptStepStatus(row, stepStatus))
							.toList();
				filteredRows = preserveParents(filteredRows);
			}
			
			boolean unsufficientReviews = isUnsufficientNumber(AbstractCoachPeerReviewListController.FILTER_UNSUFFICIENT_REVIEWS, filters);
			boolean unsufficientReviewers = isUnsufficientNumber(AbstractCoachPeerReviewListController.FILTER_UNSUFFICIENT_REVIEWERS, filters);
			if(unsufficientReviews || unsufficientReviewers) {
				filteredRows = filteredRows.stream()
					.filter(row -> acceptUnsufficientNumberOf(row.getNumOfReviewers(), unsufficientReviewers)
							&& acceptUnsufficientNumberOf(row.getNumOfReviews(), unsufficientReviews))
					.toList();
				filteredRows = preserveChildren(filteredRows);
			}
			
			super.setFilteredObjects(new ArrayList<>(filteredRows));
		} else {
			setObjects(backupRows);
		}
	}
	
	private List<CoachPeerReviewRow> preserveParents(List<CoachPeerReviewRow> filteredRows) {
		List<CoachPeerReviewRow> finalRows = new ArrayList<>(backupRows.size());
		
		CoachPeerReviewRow lastParent = null;
		for(CoachPeerReviewRow row:filteredRows) {
			if(row.getParent() == null) {
				finalRows.add(row);
				lastParent = row;
			} else {
				if(lastParent == null || lastParent != row.getParent()) {
					lastParent = row.getParent();
					finalRows.add(lastParent);
				}
				finalRows.add(row);
			}
		}
		
		return finalRows;
	}
	
	private List<CoachPeerReviewRow> preserveChildren(List<CoachPeerReviewRow> filteredRows) {
		List<CoachPeerReviewRow> finalRows = new ArrayList<>(backupRows.size());
		
		for(CoachPeerReviewRow row:filteredRows) {
			finalRows.add(row);
			if(row.getParent() == null && row.getChildrenRows() != null && !row.getChildrenRows().isEmpty()) {
				List<CoachPeerReviewRow> children = row.getChildrenRows();
				for(CoachPeerReviewRow child:children) {
					if(!filteredRows.contains(child)) {
						finalRows.add(child);
					}
				}
			}
		}
		
		return finalRows;
	}
	
	private TaskReviewAssignmentStatus getFilterAssignmentStatus(List<FlexiTableFilter> filters) {
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, AbstractCoachPeerReviewListController.FILTER_ASSIGNMENT_SESSION_STATUS);
		if(statusFilter != null) {
			return TaskReviewAssignmentStatus.secureValueOf(statusFilter.getValue(), null);
		}
		return null;
	}
	
	private List<CoachedParticipantStatus> getFilterStepStatus(List<FlexiTableFilter> filters) {
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, AbstractCoachPeerReviewListController.FILTER_ASSIGNMENT_STEP_STATUS);
		if(statusFilter instanceof FlexiTableMultiSelectionFilter stepStatusFilter) {
			return CoachedParticipantStatus.toList(stepStatusFilter.getValues());
		}
		return null;
	}
	
	private boolean isUnsufficientNumber(String filterId, List<FlexiTableFilter> filters) {
		FlexiTableFilter unsufficientFilter = FlexiTableFilter.getFilter(filters, filterId);
		return unsufficientFilter != null && filterId.equals(unsufficientFilter.getValue());
	}
	
	private boolean acceptSearch(CoachPeerReviewRow row, String searchString) {
		if(searchString != null) {
			String fullName = row.getFullName();
			return fullName != null && fullName.toLowerCase().contains(searchString);
		}
		return true;
	}
	
	private boolean acceptAssignmentStatus(CoachPeerReviewRow row, TaskReviewAssignmentStatus assignmentStatus) {
		return assignmentStatus == null || row.getAssignmentStatus() == assignmentStatus;
	}
	
	private boolean acceptStepStatus(CoachPeerReviewRow row, List<CoachedParticipantStatus> statusList) {
		CoachedParticipantStatus status = row.getStepStatus();
		return statusList == null || statusList.isEmpty() || (status != null && statusList.contains(status));
	}
	
	private boolean acceptUnsufficientNumberOf(NumOf row, boolean unsufficientNumber) {
		if(!unsufficientNumber) return true;
		return row != null && row.number() < row.reference();
	}

	@Override
	public Object getValueAt(int row, int col) {
		CoachPeerReviewRow peerReviewRow = getObject(row);
		return getValueAt(peerReviewRow, col);
	}

	@Override
	public Object getValueAt(CoachPeerReviewRow row, int col) {
		return switch(COLS[col]) {
			case identityFullName -> row;
			case numOfReviewers -> row.getNumOfReviewers();
			case numOfReviews -> row.getNumOfReviews();
			case plot -> row.getAssessmentPlot();
			case median -> AssessmentHelper.getRoundedScore(row.getMedian());
			case average -> AssessmentHelper.getRoundedScore(row.getAverage());
			case sum -> AssessmentHelper.getRoundedScore(row.getSum());
			case sessionStatus -> row;
			case taskStepStatus -> row.getStepStatus();
			case submissionStatus -> row.getSubmissionStatus();
			case editReview -> Boolean.valueOf(row.canEdit());
			case tools -> row.getToolsLink();
			default -> "ERROR";
		};
	}

	@Override
	public boolean hasChildren(int row) {
		CoachPeerReviewRow peerReviewRow = getObject(row);
		return peerReviewRow.getParent() == null;
	}

	public enum CoachReviewCols implements FlexiSortableColumnDef {
		identityFullName("table.header.reviewed.identity"),
		numOfReviewers("table.header.num.of.reviewers"),
		numOfReviews("table.header.num.of.reviews"),
		plot("table.header.review.plot"),
		median("table.header.median"),
		average("table.header.average"),
		sum("table.header.sum"),
		sessionStatus("table.header.review.status"),
		taskStepStatus("table.header.step.status"),
		submissionStatus("table.header.submission.status"),
		editReview("table.header.review.view"),
		tools("table.header.tools"),
		;
		
		private final String i18nKey;
		
		private CoachReviewCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != plot && this != tools && this != editReview;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
