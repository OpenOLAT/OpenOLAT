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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.ui.model.MediaRow;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaDataModel extends DefaultFlexiTableDataModel<MediaRow>
	implements SortableFlexiTableDataModel<MediaRow>, FilterableFlexiTableModel {
	
	private final Locale locale;
	private List<MediaRow> backups;
	
	public MediaDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		String key = filters == null || filters.isEmpty() || filters.get(0) == null ? null : filters.get(0).getFilter();
		if(StringHelper.containsNonWhitespace(key) && !"showall".equals(key)) {
			List<MediaRow> filteredRows = new ArrayList<>();
			for(MediaRow row:backups) {
				if(key.equals(row.getType())) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<MediaRow> views = new MediaDataModelSorterDelegate(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		MediaRow media = getObject(row);
		return getValueAt(media, col);
	}

	@Override
	public Object getValueAt(MediaRow media, int col) {
		switch(MediaCols.values()[col]) {
			case key: return media.getKey();
			case title: return media.getTitle();
			case collectionDate: return media.getCollectionDate();
			case categories: return media.getCategories();
			case type: return media;
		}
		return null;
	}
	
	@Override
	public void setObjects(List<MediaRow> objects) {
		backups = objects;
		super.setObjects(objects);
	}
		
	public enum MediaCols implements FlexiSortableColumnDef {
		key("table.header.key", true),
		title("table.header.title", true),
		collectionDate("table.header.collection.date", true),
		categories("table.header.categories", false),
		type("table.header.type", true);

		private final String i18nKey;
		private final boolean sortable;
		
		private MediaCols(String i18nKey, boolean sortable) {
			this.i18nKey = i18nKey;
			this.sortable = sortable;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return sortable;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
	
	public static class MediaDataModelSorterDelegate extends SortableFlexiTableModelDelegate<MediaRow> {
		
		public MediaDataModelSorterDelegate(SortKey orderBy, MediaDataModel model, Locale locale) {
			super(orderBy, model, locale);
		}

		@Override
		protected void sort(List<MediaRow> rows) {
			int columnIndex = getColumnIndex();
			MediaCols column = MediaCols.values()[columnIndex];
			switch(column) {
				case type: Collections.sort(rows, new TypeComparator()); break;
				default: {
					super.sort(rows);
				}
			}
		}
		
		private class TypeComparator implements Comparator<MediaRow> {
			@Override
			public int compare(MediaRow t1, MediaRow t2) {
				String r1 = t1.getType();
				String r2 = t2.getType();
				
				int compare = compareString(r1, r2);
				if(compare == 0) {
					compare = compareString(t1.getTitle(), t2.getTitle());
				}
				return compare;
			}
		}
	}
}
