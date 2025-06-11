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
package org.olat.modules.coach.ui.curriculum.course;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.curriculum.CurriculumElementStatus;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementWithViewsDataModel extends DefaultFlexiTreeTableDataModel<CourseCurriculumTreeWithViewsRow>
implements FlexiBusinessPathModel, SortableFlexiTableDataModel<CourseCurriculumTreeWithViewsRow> {
	
	private static final ElementViewCols[] COLS = ElementViewCols.values();
	
	private final Locale locale;

	public CurriculumElementWithViewsDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(filters != null && !filters.isEmpty() && filters.get(0) != null) {
			List<CurriculumElementStatus> status = getFilteredStatus(filters);
			if(status == null || status.isEmpty()) {
				setUnfilteredObjects();
			} else {
				List<CourseCurriculumTreeWithViewsRow> filteredRows = new ArrayList<>(backupRows.size());
				for(CourseCurriculumTreeWithViewsRow row:backupRows) {
					if(acceptStatus(status, row)) {
						filteredRows.add(row);
					}
				}
				setFilteredObjects(filteredRows);
			}
		} else {
			setUnfilteredObjects();
		}
	}
	
	private boolean acceptStatus(List<CurriculumElementStatus> status, CourseCurriculumTreeWithViewsRow row) {
		if(status == null || status.isEmpty()) return true;
		
		CurriculumElementStatus elementStatus = row.getCurriculumElementStatus();
		return status.contains(elementStatus);
	}
	
	private List<CurriculumElementStatus> getFilteredStatus(List<FlexiTableFilter> filters) {
		List<CurriculumElementStatus> status = null;
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, CurriculumElementListController.FILTER_STATUS);
		if (statusFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				status = filterValues.stream()
						.map(CurriculumElementStatus::valueOf)
						.toList();
			}
		}
		return status;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<CourseCurriculumTreeWithViewsRow> views= new CurriculumElementWithViewsSortDelegate(orderBy, this, locale)
					.sort();
			super.setObjects(views);
		}
	}
	
	public CourseCurriculumTreeWithViewsRow getObjectByKey(Long key) {
		List<CourseCurriculumTreeWithViewsRow> rows = this.getObjects();
		return rows.stream().filter(row -> key.equals(row.getKey()))
				.findFirst()
				.orElse(null);
	}
	
	@Override
	public boolean hasChildren(int row) {
		CourseCurriculumTreeWithViewsRow element = getObject(row);
		return element.hasChildren();
	}

	@Override
	public Object getValueAt(int row, int col) {
		CourseCurriculumTreeWithViewsRow curriculum = getObject(row);
		return getValueAt(curriculum, col);
	}

	@Override
	public Object getValueAt(CourseCurriculumTreeWithViewsRow curriculum, int col) {
		return switch(COLS[col]) {
			case key -> curriculum.getKey();
			case displayName -> curriculum.isRepositoryEntryOnly()
				?  curriculum.getRepositoryEntryDisplayName()
				: curriculum.getCurriculumElementDisplayName();
			case identifier -> curriculum.isRepositoryEntryOnly()
				? curriculum.getRepositoryEntryExternalRef()
				: curriculum.getCurriculumElementIdentifier();
			case calendars -> curriculum.getCalendarsLink();
			case completion -> curriculum.getCompletionItem();
			default -> "ERROR";
		};
	}
	
	@Override
	public String getUrl(Component source, Object object, String action) {
		if("select".equals(action) && object instanceof CourseCurriculumTreeWithViewsRow) {
			CourseCurriculumTreeWithViewsRow row = (CourseCurriculumTreeWithViewsRow)object;
			if(row.getStartUrl() != null) {
				return row.getStartUrl();
			}
			if(row.getDetailsUrl() != null) {
				return row.getDetailsUrl();
			}
		}
		return null;
	}

	public enum ElementViewCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.curriculum.element.display.name"),
		identifier("table.header.curriculum.element.identifier"),
		completion("table.header.completion"),
		calendars("table.header.calendars");
		
		private final String i18nHeaderKey;
		
		private ElementViewCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
