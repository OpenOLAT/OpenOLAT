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
package org.olat.modules.cemedia.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.model.MediaShare;

/**
 * 
 * Initial date: 6 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaRelationsTableModel extends DefaultFlexiTableDataModel<MediaShareRow>
implements SortableFlexiTableDataModel<MediaShareRow> {
	
	private static final MediaRelationsCols[] COLS = MediaRelationsCols.values();
	
	private final Locale locale;
	private final Translator translator;
	
	private List<MediaShareRow> backups;
	
	public MediaRelationsTableModel(FlexiTableColumnModel columnsModel, Translator translator, Locale locale) {
		super(columnsModel);
		this.locale = locale;
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<MediaShareRow> views = new MediaRelationsDataModelSorterDelegate(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	public void filter(String searchString, Boolean editable, MediaToGroupRelationType type) {
		if(searchString != null) {
			searchString = searchString.toLowerCase();
		}
		
		List<MediaShareRow> filteredRows = new ArrayList<>();
		for(MediaShareRow row:backups) {
			if(accept(row, searchString, editable, type)) {
				filteredRows.add(row);
			}
		}
		super.setObjects(filteredRows);
	}
	
	private boolean accept(MediaShareRow row, String searchString, Boolean editable, MediaToGroupRelationType type) {
		if(!StringHelper.containsNonWhitespace(searchString) && type == null && editable == null) return true;
		
		return (type == null || (row.getType() == type))
				&& (editable == null || (row.isEditable() == editable.booleanValue()))
				&&(!StringHelper.containsNonWhitespace(searchString) || (row.getDisplayName() != null && row.getDisplayName().toLowerCase().contains(searchString)));
	}

	@Override
	public Object getValueAt(int row, int col) {
		MediaShareRow relationRow = getObject(row);
		return getValueAt(relationRow, col);
	}

	@Override
	public Object getValueAt(MediaShareRow row, int col) {
		switch(COLS[col]) {
			case name: return row;
			case type: return getType(row);
			case editable: return row.getEditableToggleButton();
			default: return "ERROR";
		}
	}
	
	private String getType(MediaShareRow row) {
		switch(row.getShare().getType()) {
			case USER: return translator.translate("type.user");
			case BUSINESS_GROUP: return translator.translate("type.business.group");
			case ORGANISATION: return translator.translate("type.organisation");
			case REPOSITORY_ENTRY: return translator.translate("type.repositoryentry");
			default: return null;
		}
	}
	
	public void addObject(MediaShareRow row) {
		List<MediaShareRow> objects = getObjects();
		objects.add(row);
		setObjects(objects);
	}
	
	public void removeObject(MediaShare share) {
		List<MediaShareRow> objects = getObjects();
		for(MediaShareRow object:objects) {
			if(object.getShare().equals(share)) {
				objects.remove(object);
				break;
			}
		}
		setObjects(objects);
	}
	
	@Override
	public void setObjects(List<MediaShareRow> objects) {
		super.setObjects(objects);
		backups = objects;
	}

	public enum MediaRelationsCols implements FlexiSortableColumnDef {
		
		name("table.header.name"),
		type("table.header.type"),
		editable("table.header.editable");

		private final String i18nKey;
		
		private MediaRelationsCols(String i18nKey) {
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
