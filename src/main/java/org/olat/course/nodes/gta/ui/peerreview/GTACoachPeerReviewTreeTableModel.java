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
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;

/**
 * 
 * Initial date: 10 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachPeerReviewTreeTableModel extends DefaultFlexiTreeTableDataModel<CoachPeerReviewRow>
implements SortableFlexiTableDataModel<CoachPeerReviewRow> {
	
	private static final CoachReviewCols[] COLS = CoachReviewCols.values();
	
	private final Locale locale;
	
	public GTACoachPeerReviewTreeTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CoachPeerReviewRow> views = new GTACoachPeerReviewTreeTableModelSortDelegate(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public void filter(String quickSearch, List<FlexiTableFilter> filters) {
		if(filters != null && (StringHelper.containsNonWhitespace(quickSearch) || (!filters.isEmpty() && filters.get(0) != null))) {
			String searchString = StringHelper.containsNonWhitespace(quickSearch) ? quickSearch.toLowerCase() : null;
			TaskReviewAssignmentStatus assignmentStatus = getFilterStatus(filters);
			List<CoachPeerReviewRow> filteredRows = backupRows.stream()
						.filter(row -> acceptSearch(row, searchString) && acceptStatus(row, assignmentStatus))
						.toList();
			List<CoachPeerReviewRow> filteredRowsWithParents = preserveParents(filteredRows);
			super.setFilteredObjects(filteredRowsWithParents);
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
	
	private TaskReviewAssignmentStatus getFilterStatus(List<FlexiTableFilter> filters) {
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, AbstractCoachPeerReviewListController.FILTER_ASSIGNMENT_STATUS);
		if(statusFilter != null) {
			return TaskReviewAssignmentStatus.secureValueOf(statusFilter.getValue(), null);
		}
		return null;
	}
	
	private boolean acceptSearch(CoachPeerReviewRow row, String searchString) {
		if(searchString != null) {
			String fullName = row.getFullName();
			return fullName != null && fullName.toLowerCase().contains(searchString);
		}
		return true;
	}
	
	private boolean acceptStatus(CoachPeerReviewRow row, TaskReviewAssignmentStatus assignmentStatus) {
		return assignmentStatus == null || row.getAssignmentStatus() == assignmentStatus;
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
			case progress -> row.getProgressBar();
			case plot -> row.getAssessmentPlot();
			case median -> row.getMedian();
			case average -> row.getAverage();
			case sum -> row.getSum();
			case sessionStatus -> row;
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
		progress("table.header.review.progress"),
		plot("table.header.review.plot"),
		median("table.header.median"),
		average("table.header.average"),
		sum("table.header.sum"),
		sessionStatus("table.header.review.status"),
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
