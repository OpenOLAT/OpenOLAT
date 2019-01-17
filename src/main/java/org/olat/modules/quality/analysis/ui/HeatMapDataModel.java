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
package org.olat.modules.quality.analysis.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.quality.analysis.MultiKey;

/**
 * 
 * Initial date: 11.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class HeatMapDataModel extends DefaultFlexiTableDataModel<HeatMapRow>
		implements SortableFlexiTableDataModel<HeatMapRow> {
	
	private final Locale locale;
	
	HeatMapDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		List<HeatMapRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		HeatMapRow generator = getObject(row);
		return getValueAt(generator, col);
	}

	@Override
	public Object getValueAt(HeatMapRow row, int col) {
		int index = col;
		if (index < row.getGroupNamesSize()) {
			return row.getGroupName(index);
		}
		index = col - row.getGroupNamesSize();
		if (index < row.getStatisticsSize()) {
			return row.getStatistic(index);
		}
		// trend icon
		index -= row.getStatisticsSize();
		if (index == 0) {
			return MultiKey.none().equals(row.getMultiKey())
					? Boolean.FALSE
					: Boolean.TRUE;
			
		}
		return null;
	}

	@Override
	public DefaultFlexiTableDataModel<HeatMapRow> createCopyWithEmptyList() {
		return new HeatMapDataModel(getTableColumnModel(), locale);
	}
	
}
