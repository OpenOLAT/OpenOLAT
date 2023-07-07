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

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
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
	
	public MediaRelationsTableModel(FlexiTableColumnModel columnsModel, Translator translator, Locale locale) {
		super(columnsModel);
		this.locale = locale;
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<MediaShareRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
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
