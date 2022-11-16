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
package org.olat.modules.video.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * table-model for to list de available subtitle-tracks in the metadata
 *	
 * Initial date: 01.04.2015<br>
 * @author Dirk Furrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoTracksTableModel extends DefaultFlexiTableDataModel<TrackTableRow> implements SortableFlexiTableDataModel<TrackTableRow> {

	private final Locale locale;
	
	public VideoTracksTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public Object getValueAt(int row, int col) {
		return getValueAt(getObject(row), col);
	}

	@Override
	public Object getValueAt(TrackTableRow track, int col) {
		switch(TrackTableCols.values()[col]) {
			case file: return track.getTrack() == null ? "-" : track.getTrack().getName();
			case language: return new Locale(track.getLanguage()).getDisplayLanguage(locale);
			case delete: return track.getDeleteLink();
			default: return "";
		}
	}

	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<TrackTableRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	public enum TrackTableCols implements FlexiSortableColumnDef {
		file("track.table.header.file"),
		language("track.table.header.language"),
		delete("track.table.header.delete");

		private final String i18nKey;

		private TrackTableCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != delete;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}