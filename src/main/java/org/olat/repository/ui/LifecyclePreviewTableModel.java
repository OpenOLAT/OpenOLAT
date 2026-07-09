/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 9 juil. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LifecyclePreviewTableModel extends DefaultFlexiTableDataModel<LifecyclePreviewRow> {

	private static final ForecastCols[] COLS = ForecastCols.values();

	public LifecyclePreviewTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		LifecyclePreviewRow forecastRow = getObject(row);
		return switch(COLS[col]) {
			case period -> forecastRow.period();
			case toClose -> Integer.valueOf(forecastRow.toClose());
			case toDelete -> Integer.valueOf(forecastRow.toDelete());
			case toDefinitivelyDelete -> Integer.valueOf(forecastRow.toDefinitivelyDelete());
			case total -> Integer.valueOf(forecastRow.total());
		};
	}

	public enum ForecastCols implements FlexiColumnDef {
		period("table.header.forecast.period"),
		toClose("table.header.forecast.close"),
		toDelete("table.header.forecast.delete"),
		toDefinitivelyDelete("table.header.forecast.definitively.delete"),
		total("table.header.forecast.total");

		private final String i18nKey;

		private ForecastCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
