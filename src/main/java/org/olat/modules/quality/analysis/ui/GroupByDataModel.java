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

/**
 * 
 * Initial date: 11.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class GroupByDataModel extends DefaultFlexiTableDataModel<GroupByRow>
		implements SortableFlexiTableDataModel<GroupByRow> {
	
	protected final Locale locale;
	
	GroupByDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		List<GroupByRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		GroupByRow generator = getObject(row);
		return getValueAt(generator, col);
	}

	@Override
	public Object getValueAt(GroupByRow row, int col) {
		if (col >= GroupByController.DATA_OFFSET) {
			int pos = col - GroupByController.DATA_OFFSET;
			if (pos < row.getStatisticsSize()) {
				return row.getStatistic(pos);
			}
		}
		if (col == GroupByController.TOTAL_OFFSET) {
			return row.getTotal();
		}
		if (col < row.getGroupNamesSize()) {
			return row.getGroupName(col);
		}
		return null;
	}

	@Override
	public DefaultFlexiTableDataModel<GroupByRow> createCopyWithEmptyList() {
		return new GroupByDataModel(getTableColumnModel(), locale);
	}
	
}
