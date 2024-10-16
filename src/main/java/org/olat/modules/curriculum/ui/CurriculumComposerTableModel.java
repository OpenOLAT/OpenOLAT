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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
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

	private static final ElementCols[] COLS = ElementCols.values();
	
	public CurriculumComposerTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || (filters != null && !filters.isEmpty() && filters.get(0) != null)) {	
			List<Long> typesKeys = List.of();
			List<Long> curriculumsKeys = List.of();
			List<CurriculumElementStatus> status = List.of();
			Long searchLong = StringHelper.isLong(searchString) ? Long.valueOf(searchString) : null;
			searchString = StringHelper.containsNonWhitespace(searchString) ? searchString.toLowerCase() : null;
			
			FlexiTableFilter curriculumFilter = FlexiTableFilter.getFilter(filters, CurriculumComposerController.FILTER_CURRICULUM);
			if (curriculumFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> filterValues = extendedFilter.getValues();
				if(filterValues != null && !filterValues.isEmpty()) {
					curriculumsKeys = filterValues.stream()
							.map(Long::valueOf).toList();
				}
			}
			
			FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, CurriculumComposerController.FILTER_STATUS);
			if (statusFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> filterValues = extendedFilter.getValues();
				if(filterValues != null && !filterValues.isEmpty()) {
					status = filterValues.stream()
							.map(CurriculumElementStatus::valueOf).toList();
				}
			}
			
			FlexiTableFilter typeFilter = FlexiTableFilter.getFilter(filters, CurriculumComposerController.FILTER_TYPE);
			if (typeFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> filterValues = extendedFilter.getValues();
				if(filterValues != null && !filterValues.isEmpty()) {
					typesKeys = filterValues.stream()
							.map(Long::valueOf).toList();
				}
			}
			
			List<CurriculumElementRow> filteredRows = new ArrayList<>(backupRows.size());
			for(CurriculumElementRow row:backupRows) {
				boolean accept = (accept(row, searchLong) || accept(row, searchString))
						&& acceptCurriculum(row, curriculumsKeys)
						&& acceptStatus(row, status)
						&& acceptTypes(row,  typesKeys);
				
				row.setAcceptedByFilter(accept);
				if(accept) {
					filteredRows.add(row);
				}
			}
			
			if(filteredRows.size() < backupRows.size()) {
				reconstructParentLine(filteredRows);
			}
			setFilteredObjects(filteredRows);
		} else {
			for(CurriculumElementRow row:backupRows) {
				row.setAcceptedByFilter(true);
			}
			setUnfilteredObjects();
		}
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
	
	private boolean accept(CurriculumElementRow row, String searchString) {
		if(!StringHelper.containsNonWhitespace(searchString)) {
			return true;
		}
		return ((row.getDisplayName() != null && row.getDisplayName().toLowerCase().contains(searchString))
				|| (row.getIdentifier() != null && row.getIdentifier().toLowerCase().contains(searchString))
				|| (row.getExternalId() != null && row.getExternalId().toLowerCase().contains(searchString)));
	}

	private boolean accept(CurriculumElementRow row, Long searchLong) {
		return searchLong != null && searchLong.equals(row.getKey());
	}
	
	private boolean acceptCurriculum(CurriculumElementRow row, List<Long> curriculumsKeys) {
		return curriculumsKeys.isEmpty()
				|| (row.getCurriculumKey() != null && curriculumsKeys.contains(row.getCurriculumKey()));
	}
	
	private boolean acceptStatus(CurriculumElementRow row, List<CurriculumElementStatus> statusList) {
		return statusList.isEmpty()
				|| (row.getStatus() != null && statusList.contains(row.getStatus()));
	}
	
	private boolean acceptTypes(CurriculumElementRow row, List<Long> elementTypesList) {
		return elementTypesList.isEmpty()
				|| (row.getCurriculumElementTypeKey() != null && elementTypesList.contains(row.getCurriculumElementTypeKey()));
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
		return switch(COLS[col]) {
			case key -> element.getKey();
			case displayName -> element.getDisplayName();
			case externalRef -> element.getIdentifier();
			case externalId -> element.getExternalId();
			case curriculum -> getCurriculum(element);
			case beginDate -> element.getBeginDate();
			case endDate -> element.getEndDate();
			case type -> element.getCurriculumElementTypeDisplayName();
			case resources -> element.getResources();
			case status -> element.getStatus();
			case tools -> element.getTools();
			case numOfMembers -> element.getNumOfMembers();
			case numOfParticipants -> element.getNumOfParticipants();
			case numOfCoaches -> element.getNumOfCoaches();
			case numOfOwners -> element.getNumOfOwners();
			case calendars -> element.getCalendarsLink();
			case lectures -> element.getLecturesLink();
			case qualityPreview -> element.getQualityPreviewLink();
			case learningProgress -> element.getLearningProgressLink();
			default -> "ERROR";
		};
	}
	
	private String getCurriculum(CurriculumElementRow element) {
		if(StringHelper.containsNonWhitespace(element.getCurriculumExternalRef())) {
			return element.getCurriculumExternalRef();
		}
		return element.getCurriculumDisplayName();
	}
	
	public enum ElementCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.displayName"),
		externalRef("table.header.external.ref"),
		externalId("table.header.external.id"),
		curriculum("table.header.curriculum"),
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
		qualityPreview("table.header.quality.preview"),
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
