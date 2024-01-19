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
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.portfolio.ui.model.MediaRow;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaDataModel extends DefaultFlexiTableDataModel<MediaRow>
	implements SortableFlexiTableDataModel<MediaRow>, FilterableFlexiTableModel {
	
	private static final MediaCols[] COLS = MediaCols.values();
	
	private final Locale locale;
	private List<MediaRow> backups;
	private final Translator translator;
	
	public MediaDataModel(FlexiTableColumnModel columnsModel, Translator translator, Locale locale) {
		super(columnsModel);
		this.locale = locale;
		this.translator = translator;
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		super.setObjects(backups);
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<MediaRow> views = new MediaDataModelSorterDelegate(orderBy, this, translator, locale).sort();
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
		switch(COLS[col]) {
			case key: return media.getKey();
			case title: return media.getTitle();
			case collectionDate: return media.getCollectionDate();
			case tags: return media.getTags();
			case taxonomyLevels, taxonomyLevelsPaths: return media.getTaxonomyLevels();
			case type: return media;
			case source: return media.getSource();
			case platform: return media.getPlatform(translator);
			default: return "ERROR";
		}
	}
	
	@Override
	public void setObjects(List<MediaRow> objects) {
		backups = objects;
		super.setObjects(objects);
	}
	
	public MediaRow getObjectByMediaKey(Long mediaKey) {
		List<MediaRow> rows = getObjects();
		for(MediaRow prow:rows) {
			if(mediaKey.equals(prow.getKey())) {
				return prow;
			}
		}
		return null;
	}
		
	public enum MediaCols implements FlexiSortableColumnDef {
		key("table.header.key", true),
		title("table.header.title", true),
		collectionDate("table.header.collection.date", true),
		tags("table.header.tags", true),
		type("table.header.type", true),
		taxonomyLevels("table.header.taxonomy.levels", true),
		taxonomyLevelsPaths("table.header.taxonomy.levels.paths", true),
		source("table.header.source", true),
		platform("table.header.platform", true);

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
}
