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
package org.olat.modules.project.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 17 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjTimelineDataModel extends DefaultFlexiTableDataModel<ProjTimelineRow> implements SortableFlexiTableDataModel<ProjTimelineRow> {
	
	private static final TimelineCols[] COLS = TimelineCols.values();

	private final Locale locale;
	
	public ProjTimelineDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		List<ProjTimelineRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ProjTimelineRow note = getObject(row);
		return getValueAt(note, col);
	}

	@Override
	public Object getValueAt(ProjTimelineRow row, int col) {
		switch(COLS[col]) {
		case message: return row.getMessage();
		case date: return row.getDate();
		case doerDisplayName: return row.getDoerDisplyName();
		default: return null;
		}
	}
	
	public enum TimelineCols implements FlexiSortableColumnDef {
		message("timeline.message"),
		date("timeline.date"),
		doerDisplayName("timeline.doer.display.name");
		
		private final String i18nKey;
		
		private TimelineCols(String i18nKey) {
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
