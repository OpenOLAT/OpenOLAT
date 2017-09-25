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
package org.olat.resource.accesscontrol.provider.auto.ui;

import java.util.Arrays;
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
 * Initial date: 08.09.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AdvanceOrderDataModel extends DefaultFlexiTableDataModel<AdvanceOrderRow>
		implements SortableFlexiTableDataModel<AdvanceOrderRow> {

	private final Locale locale;

	public AdvanceOrderDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<AdvanceOrderRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		AdvanceOrderRow advanceOrder = getObject(row);
		return getValueAt(advanceOrder, col);
	}

	@Override
	public Object getValueAt(AdvanceOrderRow row, int col) {
		switch(AdvanceOrderCol.values()[col]) {
			case creationDate:
				return row.getAdvanceOrder().getCreationDate();
			case identifierKey:
				return row.getAdvanceOrder().getIdentifierKey().toString();
			case identifierValue:
				return row.getAdvanceOrder().getIdentifierValue();
			case method:
				return Arrays.asList(row.getAdvanceOrder().getMethod());
			default:
				return row.getAdvanceOrder();
		}
	}

	@Override
	public DefaultFlexiTableDataModel<AdvanceOrderRow> createCopyWithEmptyList() {
		return new AdvanceOrderDataModel(getTableColumnModel(), locale);
	}

	public enum AdvanceOrderCol implements FlexiSortableColumnDef {
		creationDate("advanceOrder.creationDate", "creationdate"),
		identifierKey("advanceOrder.identitfier.key", "a_identitfier_key"),
		identifierValue("advanceOrder.identitfier.value", "a_identitfier_value"),
		method("advanceOrder.method", "trxMethodIds");

		private final String i18nKey;
		private final String sortKey;

		private AdvanceOrderCol(String i18nKey, String sortKey) {
			this.i18nKey = i18nKey;
			this.sortKey = sortKey;
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
			return sortKey;
		}
	}

}
