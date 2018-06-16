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
package org.olat.modules.quality.ui;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;

/**
 * 
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ParticipationDataModel  extends DefaultFlexiTableDataSourceModel<ParticipationRow> {

	private final Locale locale;

	public ParticipationDataModel(FlexiTableDataSourceDelegate<ParticipationRow> dataSource,
			FlexiTableColumnModel columnsModel, Locale locale) {
		super(dataSource, columnsModel);
		this.locale = locale;
	}
	

	@Override
	public Object getValueAt(int row, int col) {
		ParticipationRow participationRow = getObject(row);
		switch (ParticipationCols.values()[col]) {
			case firstname: return participationRow.getFirstname();
			case lastname: return participationRow.getLastname();
			case email: return participationRow.getEmail();
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataSourceModel<ParticipationRow> createCopyWithEmptyList() {
		return new ParticipationDataModel(getSourceDelegate(), getTableColumnModel(), locale);
	}

	public enum ParticipationCols implements FlexiSortableColumnDef {
		firstname("participation.firstname"),
		lastname("participation.lastname"),
		email("participation.email");
		
		private final String i18nKey;
		
		private ParticipationCols(String i18nKey) {
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
