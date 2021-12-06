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

package org.olat.core.configuration.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.coach.ui.StudentsTableDataModel;


/**
 * Initial Date:  27.08.2020 <br>
 * @author aboeckle, mjenny, alexander.boeckle@frentix.com, http://www.frentix.com
 */

public class OlatPropertiesTableModel extends DefaultFlexiTableDataModel<OlatPropertiesTableContentRow> implements SortableFlexiTableDataModel<OlatPropertiesTableContentRow>, FilterableFlexiTableModel {

	private static final Logger log = Tracing.createLoggerFor(StudentsTableDataModel.class);
	private List<OlatPropertiesTableContentRow> backup; 
	
	public OlatPropertiesTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		OlatPropertiesTableContentRow olatPropertiesRow = getObject(row);
		return getValueAt(olatPropertiesRow, col);
	}
	
	@Override
	public Object getValueAt(OlatPropertiesTableContentRow row, int col) {
		switch (OlatPropertiesTableColumn.values()[col]) {
		case key:
			return row.getPropertyKey();
		case defaultValue:
			return row.getDefaultPropertyValue();
		case overwriteValue:
			return row.getOverwritePropertyValue();
		case systemProperty:
			return row.getSystemPropertyValue();	
		case icon:
			return row.hasRedundantEntry();
		default:
			return "ERROR";
		}
	}
	
	public enum OlatPropertiesTableColumn implements FlexiSortableColumnDef {
		key("olat.property.key"),
		defaultValue("olat.property.value"),
		overwriteValue("olat.property.overwrite.value"),
		systemProperty("olat.system.property.value"),
		icon("olat.property.redundancy");
		

		private final String i18nHeaderKey;
		
		private OlatPropertiesTableColumn(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}
		
		@Override 
		public String i18nHeaderKey() {
			return i18nHeaderKey;
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
	
	public enum OlatPropertiesFilter {
		showAll, 
		showOverwrittenOnly,
		showRedundantOnly;
	}
	
	@Override
	public void setObjects(List<OlatPropertiesTableContentRow> objects) {
		super.setObjects(objects);
		backup = objects;
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(filters != null && !filters.isEmpty() && filters.get(0) != null && !filters.get(0).isShowAll()) {
			List<OlatPropertiesTableContentRow> filteredRows;
			if(filters.get(0).getFilter().equals(OlatPropertiesFilter.showOverwrittenOnly.name())) {
				filteredRows = backup.stream().filter(row -> row.getOverwriteProperty() != null).collect(Collectors.toList());
				super.setObjects(filteredRows);
			} else if(filters.get(0).getFilter().equals(OlatPropertiesFilter.showRedundantOnly.name())) {
				filteredRows = backup.stream().filter(row -> row.hasRedundantEntry()).collect(Collectors.toList());
				super.setObjects(filteredRows);
			} if (StringHelper.containsNonWhitespace(searchString)) {
				search(searchString, true);
			}
		} else if (StringHelper.containsNonWhitespace(searchString)) {
			search(searchString, false);
		} else {
			super.setObjects(backup);
		}
	}

	public void search(final String searchString, boolean isFilterApplied) {
		if(StringHelper.containsNonWhitespace(searchString)) {
			try {
				List<OlatPropertiesTableContentRow> filteredList;
				String loweredSearchString = searchString.toLowerCase();
				filteredList = isFilterApplied ? getObjects() : backup;
				filteredList = filteredList.stream().filter(entry -> {
						if (entry.getPropertyKey().toLowerCase().contains(loweredSearchString)) {
							return true;
						} else if(entry.getDefaultProperty() != null && entry.getDefaultProperty().getKey().toLowerCase().contains(loweredSearchString)){
							return true;
						} else if(entry.getOverwriteProperty() != null && entry.getOverwriteProperty().getValue().toLowerCase().contains(loweredSearchString)){
							return true;
						} else if(entry.getDefaultProperty() != null && entry.getDefaultProperty().getValue().toLowerCase().contains(loweredSearchString)){
							return true;
						} else {
							return false;
						}
					})
					.collect(Collectors.toList());
				super.setObjects(filteredList);
			} catch (Exception e) {
				log.error("", e);
				super.setObjects(backup);
			}
		} else {
			super.setObjects(backup);
		}
	}

	@Override
	public void sort(SortKey sortKey) {
		if (sortKey != null) {
			List<OlatPropertiesTableContentRow> properties = new OlatPropertiesSortableDelegate(sortKey, this, null).sort();
			super.setObjects(properties);
		}
	}
	
}