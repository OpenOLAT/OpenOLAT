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
package org.olat.course.nodes.livestream.ui;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 24 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamEventDataModel extends DefaultFlexiTableDataModel<LiveStreamEventRow>
		implements SortableFlexiTableDataModel<LiveStreamEventRow> {
	
	public LiveStreamEventDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		SortableFlexiTableModelDelegate<LiveStreamEventRow> sorter = new SortableFlexiTableModelDelegate<>(orderBy, this, null);
		List<LiveStreamEventRow> rows = sorter.sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		LiveStreamEventRow reason = getObject(row);
		return getValueAt(reason, col);
	}

	@Override
	public Object getValueAt(LiveStreamEventRow row, int col) {
		switch(EventCols.values()[col]) {
			case begin: return row.getEvent().getBegin();
			case end: return row.getEvent().getEnd();
			case subject: return row.getEvent().getSubject();
			case description: return row.getEvent().getDescription();
			case location: return row.getEvent().getLocation();
			case viewers: return row.getViewers();
			default: return null;
		}
	}
	
	public enum EventCols implements FlexiSortableColumnDef {
		subject("table.header.subject"),
		begin("table.header.begin"),
		end("table.header.end"),
		location("table.header.location"),
		description("table.header.description"),
		viewers("table.header.viewers");
		
		private final String i18nKey;
		
		private EventCols(String i18nKey) {
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
