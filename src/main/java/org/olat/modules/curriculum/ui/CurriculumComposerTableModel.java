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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElementStatus;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumComposerTableModel extends DefaultFlexiTreeTableDataModel<CurriculumElementRow> {

	public CurriculumComposerTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || (filters != null && !filters.isEmpty() && filters.get(0) != null)) {
			FlexiTableFilter filter = null;
			if(filters != null && !filters.isEmpty()) {
				filter = filters.get(0);
			}
			
			Date now = new Date();
			Date nowBegin = CalendarUtils.removeTime(now);
			Date nowEnd = CalendarUtils.endOfDay(now);
			
			Long searchLong = null;
			if(StringHelper.isLong(searchString)) {
				searchLong = Long.valueOf(searchString);
			}
			filter(searchString, searchLong, nowBegin, nowEnd, filter);
		} else {
			for(CurriculumElementRow row:backupRows) {
				row.setAcceptedByFilter(true);
			}
			setUnfilteredObjects();
		}
	}
	
	private void filter(String searchString, Long searchLong, Date nowBegin, Date nowEnd, FlexiTableFilter filter) {
		boolean searched = searchString != null || searchLong != null;
		boolean filtered = filter != null && !filter.isShowAll();
		
		if(searchString != null) {
			searchString = searchString.toLowerCase();
		}
		
		List<CurriculumElementRow> rowsToFilter;
		if(focusedNode == null) {
			rowsToFilter = new ArrayList<>(backupRows);
		} else {
			rowsToFilter = focusedNodes(backupRows, focusedNode, backupRows.indexOf(focusedNode));
		}

		List<CurriculumElementRow> filteredRows = new ArrayList<>(backupRows.size());
		for(CurriculumElementRow row:rowsToFilter) {
			boolean accept = false;
			if(searched && filtered) {
				accept = accept(row, nowBegin, nowEnd, filter) && accept(row, searchString, searchLong);
			} else if(searched) {
				accept = accept(row, searchString, searchLong);
			} else if(filtered) {
				accept = accept(row, nowBegin, nowEnd, filter);
			}

			row.setAcceptedByFilter(accept);
			if(accept) {
				filteredRows.add(row);
			}
		}
		if(filteredRows.size() < backupRows.size()) {
			reconstructParentLine(filteredRows);
		}
		setFilteredObjects(filteredRows);
	}
	
	private void reconstructParentLine(List<CurriculumElementRow> rows) {
		Set<CurriculumElementRow> rowSet = new HashSet<>(rows);
		for(int i=0; i<rows.size(); i++) {
			CurriculumElementRow row = rows.get(i);
			for(CurriculumElementRow parent=row.getParent(); parent != null && !rowSet.contains(parent); parent=parent.getParent()) {
				rows.add(i, parent);
				rowSet.add(parent);	
			}
		}
	}

	private boolean accept(CurriculumElementRow row, String searchString, Long searchLong) {
		boolean accept = false;
		if(searchLong != null && searchLong.equals(row.getKey())) {
			accept = true;
		}
		
		if(!accept && (
				(row.getDisplayName() != null && row.getDisplayName().toLowerCase().contains(searchString))
				|| (row.getIdentifier() != null && row.getIdentifier().toLowerCase().contains(searchString))
				|| (row.getExternalId() != null && row.getExternalId().toLowerCase().contains(searchString)))) {
			accept = true;
		}
		return accept;
	}
	
	private boolean accept(CurriculumElementRow row, Date nowBegin, Date nowEnd, FlexiTableFilter filter) {
		boolean accept = false;
		if("active".equals(filter.getFilter())) {
			// empty dates and dates at same day as now count as "active" dates
			if(row.getStatus() == CurriculumElementStatus.active
					&& (row.getBeginDate() == null || row.getBeginDate().compareTo(nowBegin) <= 0)
					&& (row.getEndDate() == null || row.getEndDate().compareTo(nowBegin) >= 0)) {
				accept = true;
			}
		} else if("inactive".equals(filter.getFilter())) {
			if(row.getStatus() == CurriculumElementStatus.inactive
					|| (row.getBeginDate() != null && row.getBeginDate().compareTo(nowEnd) > 0)
					|| (row.getEndDate() != null && row.getEndDate().compareTo(nowEnd) < 0)) {
				accept = true;
			}
		}
		return accept;
	}
	
	public CurriculumElementRow getFocusedCurriculumElementRow() {
		return focusedNode;
	}
	
	public CurriculumElementRow getCurriculumElementRowByKey(Long elementKey) {
		List<CurriculumElementRow> rows = new ArrayList<>(backupRows);
		for(CurriculumElementRow row:rows) {
			if(elementKey.equals(row.getKey())) {
				return row;
			}
		}
		return null;
	}
	
	@Override
	public boolean hasChildren(int row) {
		CurriculumElementRow element = getObject(row);
		return element.hasChildren();
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementRow element = getObject(row);
		switch(ElementCols.values()[col]) {
			case key: return element.getKey();
			case displayName: return element.getDisplayName();
			case identifier: return element.getIdentifier();
			case externalId: return element.getExternalId();
			case beginDate: return element.getBeginDate();
			case endDate: return element.getEndDate();
			case type: return element.getCurriculumElementTypeDisplayName();
			case resources: return element.getResources();
			case status: return element.getStatus();
			case tools: return element.getTools();
			case numOfMembers: return element.getNumOfMembers();
			case numOfParticipants: return element.getNumOfParticipants();
			case numOfCoaches: return element.getNumOfCoaches();
			case numOfOwners: return element.getNumOfOwners();
			case calendars: return element.getCalendarsLink();
			case lectures: return element.getLecturesLink();
			case learningProgress: return element.getLearningProgressLink();
			default: return "ERROR";
		}
	}
	
	public enum ElementCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.displayName"),
		identifier("table.header.identifier"),
		externalId("table.header.external.id"),
		beginDate("table.header.begin.date"),
		endDate("table.header.end.date"),
		type("table.header.type"),
		resources("table.header.resources"),
		numOfMembers("table.header.num.of.members"),
		numOfParticipants("table.header.num.of.participants"),
		numOfCoaches("table.header.num.of.coaches"),
		numOfOwners("table.header.num.of.owners"),
		calendars("table.header.calendars"),
		lectures("table.header.lectures"),
		learningProgress("table.header.learning.progress"),
		status("table.header.status"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private ElementCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
