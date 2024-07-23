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
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.ui.GTACoachedGroupGradingController;

/**
 * 
 * Initial date: 12 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAPeerReviewAssignmentTableModel extends DefaultFlexiTableDataModel<PeerReviewAssignmentRow>
implements SortableFlexiTableDataModel<PeerReviewAssignmentRow> {
	
	private static final AssignmentsCols[] COLS = AssignmentsCols.values();
	
	private final Locale locale;
	private List<PeerReviewAssignmentRow> backupRows;
	
	public GTAPeerReviewAssignmentTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<PeerReviewAssignmentRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	public void filter(String quickSearch, List<FlexiTableFilter> filters) {
		if(filters != null && (StringHelper.containsNonWhitespace(quickSearch) || (!filters.isEmpty() && filters.get(0) != null))) {
			String searchString = StringHelper.containsNonWhitespace(quickSearch) ? quickSearch.toLowerCase() : null;
			String assignmentStatus = getFilterStatus(filters);
			List<PeerReviewAssignmentRow> filteredRows = backupRows.stream()
						.filter(row -> acceptSearch(row, searchString) && acceptStatus(row, assignmentStatus))
						.collect(Collectors.toList());
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupRows);
		}
	}
	
	private String getFilterStatus(List<FlexiTableFilter> filters) {
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, GTAPeerReviewAssignmentController.FILTER_ASSIGNMENT_STATUS);
		if(statusFilter != null) {
			return statusFilter.getValue();
		}
		return null;
	}
	
	private boolean acceptSearch(PeerReviewAssignmentRow row, String searchString) {
		if(searchString != null) {
			String[] userProperties = row.getIdentityProps();
			for(int i=userProperties.length; i-->0; ) {
				String userProperty = userProperties[i];
				if(userProperty != null && userProperty.toLowerCase().contains(searchString)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	private boolean acceptStatus(PeerReviewAssignmentRow row, String assignmentStatus) {
		return assignmentStatus == null
				|| GTAPeerReviewAssignmentController.ALL_ASSIGNED.equals(assignmentStatus)
				|| (row.isAssigned() && GTAPeerReviewAssignmentController.ASSIGNED.equals(assignmentStatus))
				|| (!row.isAssigned() && GTAPeerReviewAssignmentController.NOT_ASSIGNED.equals(assignmentStatus));
	}

	@Override
	public Object getValueAt(int row, int col) {
		PeerReviewAssignmentRow assignmentRow = getObject(row);
		return getValueAt(assignmentRow, col);
	}

	@Override
	public Object getValueAt(PeerReviewAssignmentRow assignmentRow, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case taskTitle -> assignmentRow.getTaskTitle();
				case numberReviews -> getNumberOfReviews(assignmentRow);
				case assignment -> assignmentRow.getAssignmentEl();
				default -> "ERROR";
			};
		}
		if(col >= GTACoachedGroupGradingController.USER_PROPS_OFFSET) {
			int propIndex = col - GTACoachedGroupGradingController.USER_PROPS_OFFSET;
			return assignmentRow.getIdentityProp(propIndex);
		}
		return "ERROR";
	}
	
	private String getNumberOfReviews(PeerReviewAssignmentRow assignmentRow) {
		return new StringBuilder(6).append(assignmentRow.getNumOfTasksToReviews()).append("/")
				.append(assignmentRow.getNumOfReviewers()).toString();
	}
	
	@Override
	public void setObjects(List<PeerReviewAssignmentRow> objects) {
		backupRows = new ArrayList<>(objects);
		super.setObjects(objects);
	}

	public enum AssignmentsCols implements FlexiSortableColumnDef {
		taskTitle("table.header.group.taskTitle"),
		numberReviews("table.header.num.of.review"),
		assignment("table.header.assignment")
		;
		
		private final String i18nKey;
		
		private AssignmentsCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
