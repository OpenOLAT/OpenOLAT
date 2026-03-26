/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.curriculum.ui.widgets;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.curriculum.ui.component.RelevanceSortDelegate;

/**
 *
 * Initial date: Jan 23, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ImplementationTableModel extends DefaultFlexiTableDataModel<ImplementationRow>
		implements SortableFlexiTableDataModel<ImplementationRow> {

	private final Locale locale;

	public ImplementationTableModel(FlexiTableColumnModel tableColumnModel, Locale locale) {
		super(tableColumnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ImplementationRow> rows = new RelevanceSortDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(ImplementationRow row, int col) {
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		return null;
	}
}
