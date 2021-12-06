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
import org.olat.modules.opencast.OpencastSeries;

/**
 * 
 * Initial date: 6 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SeriesDataModel extends DefaultFlexiTableDataModel<OpencastSeries>
implements SortableFlexiTableDataModel<OpencastSeries> {
	
	private final Locale locale;
	
	public SeriesDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<OpencastSeries> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		OpencastSeries reason = getObject(row);
		return getValueAt(reason, col);
	}

	@Override
	public Object getValueAt(OpencastSeries series, int col) {
		switch(SeriesCols.values()[col]) {
			case identifier: return series.getIdentifier();
			case title: return series.getTitle();
			case description: return Formatter.truncate(series.getDescription(), 200);
			case contributors: return series.getContributors().stream().collect(Collectors.joining(", "));
			case subjects: return series.getSubjects().stream().collect(Collectors.joining(", "));
			default: return null;
		}
	}
	
	public enum SeriesCols implements FlexiSortableColumnDef {
		identifier("series.identifier"),
		title("series.title"),
		description("series.description"),
		contributors("series.contributors"),
		subjects("series.subjects"),
		select("select");
		
		private final String i18nKey;
		
		private SeriesCols(String i18nKey) {
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
