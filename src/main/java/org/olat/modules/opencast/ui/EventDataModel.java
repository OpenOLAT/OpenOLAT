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
package org.olat.modules.opencast.ui;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.Formatter;
import org.olat.modules.opencast.OpencastEvent;

/**
 * 
 * Initial date: 6 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EventDataModel extends DefaultFlexiTableDataModel<OpencastEvent>
implements SortableFlexiTableDataModel<OpencastEvent> {
	
	private final Locale locale;
	
	public EventDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<OpencastEvent> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		OpencastEvent reason = getObject(row);
		return getValueAt(reason, col);
	}

	@Override
	public Object getValueAt(OpencastEvent event, int col) {
		switch(EventCols.values()[col]) {
			case identifier: return event.getIdentifier();
			case title: return event.getTitle();
			case description: return Formatter.truncate(event.getDescription(), 200);
			case presenters: return event.getPresenters().stream().collect(Collectors.joining(", "));
			case start: return event.getStart();
			case series: return event.getSeries();
			default: return null;
		}
	}
	
	public enum EventCols implements FlexiSortableColumnDef {
		identifier("event.identifier"),
		title("event.title"),
		description("event.description"),
		presenters("event.presenters"),
		start("event.start"),
		series("event.series"),
		select("select");
		
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
