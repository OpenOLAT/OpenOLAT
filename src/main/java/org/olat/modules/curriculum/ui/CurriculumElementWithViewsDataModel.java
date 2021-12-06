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
import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.modules.curriculum.CurriculumElementStatus;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementWithViewsDataModel extends DefaultFlexiTreeTableDataModel<CurriculumElementWithViewsRow> implements FlexiBusinessPathModel {
	
	public CurriculumElementWithViewsDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(filters != null && !filters.isEmpty() && filters.get(0) != null) {
			FlexiTableFilter filter = filters.get(0);
			if(filter == null || filter.isShowAll()) {
				setUnfilteredObjects();
			} else {
				List<CurriculumElementWithViewsRow> filteredRows = new ArrayList<>(backupRows.size());
				// curriculum element inactive -> all repo are inactives
				// parent inactive, child is active -> parent is forced active
				for(CurriculumElementWithViewsRow row:backupRows) {
					boolean accept = active(row);
					if(accept) {
						filteredRows.add(row);
					}
				}
				setFilteredObjects(filteredRows);
			}
		} else {
			setUnfilteredObjects();
		}
	}
	
	private boolean active(CurriculumElementWithViewsRow row) {
		boolean active = true;
		if(row.isCurriculumElementOnly() || row.isCurriculumElementWithEntry()) {
			active = row.getCurriculumElementStatus() == CurriculumElementStatus.active;
		}
		if(active) {
			for(CurriculumElementWithViewsRow parent=row.getParent(); parent != null; parent=parent.getParent()) {
				if(parent.isCurriculumElementOnly() || parent.isCurriculumElementWithEntry()) {
					active &= row.getCurriculumElementStatus() == CurriculumElementStatus.active;
				}
			}
		}
		return active;
	}
	
	@Override
	public String getUrl(Component source, Object object, String action) {
		if("select".equals(action) && object instanceof CurriculumElementWithViewsRow) {
			CurriculumElementWithViewsRow row = (CurriculumElementWithViewsRow)object;
			if(row.getStartUrl() != null) {
				return row.getStartUrl();
			}
			if(row.getDetailsUrl() != null) {
				return row.getDetailsUrl();
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(int row) {
		CurriculumElementWithViewsRow element = getObject(row);
		return element.hasChildren();
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementWithViewsRow curriculum = getObject(row);
		switch(ElementViewCols.values()[col]) {
			case key: return curriculum.getKey();
			case displayName: {
				String displayName;
				if(curriculum.isRepositoryEntryOnly()) {
					displayName = curriculum.getRepositoryEntryDisplayName();
				} else {
					displayName = curriculum.getCurriculumElementDisplayName();
				}
				return displayName;
			}
			case identifier: {
				String identifier;
				if(curriculum.isRepositoryEntryOnly()) {
					identifier = curriculum.getRepositoryEntryExternalRef();
				} else {
					identifier = curriculum.getCurriculumElementIdentifier();
				}
				return identifier;
			}
			case mark: return curriculum.getMarkLink();
			case select: return curriculum.getSelectLink();
			case details: return curriculum.getDetailsLink();
			case start: return curriculum.getStartLink();
			case calendars: return curriculum.getCalendarsLink();
			case completion: return curriculum.getCompletionItem();
			default: return "ERROR";
		}
	}
	
	public enum ElementViewCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.curriculum.element.displayName"),
		identifier("table.header.curriculum.element.identifier"),
		mark("table.header.mark"),
		select("table.header.displayName"),
		completion("table.header.completion"),
		details("table.header.details"),
		start("table.header.start"),
		calendars("table.header.calendars");
		
		private final String i18nHeaderKey;
		
		private ElementViewCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return false;
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
