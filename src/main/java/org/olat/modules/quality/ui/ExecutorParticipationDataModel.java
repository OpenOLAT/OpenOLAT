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
import org.olat.modules.quality.QualitySecurityCallback;

/**
 * 
 * Initial date: 20.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExecutorParticipationDataModel extends DefaultFlexiTableDataSourceModel<ExcecutorParticipationRow> {

	private final QualitySecurityCallback secCallback;
	private final Locale locale;

	public ExecutorParticipationDataModel(FlexiTableDataSourceDelegate<ExcecutorParticipationRow> dataSource,
			FlexiTableColumnModel columnsModel, QualitySecurityCallback secCallback, Locale locale) {
		super(dataSource, columnsModel);
		this.secCallback = secCallback;
		this.locale = locale;
	}
	

	@Override
	public Object getValueAt(int row, int col) {
		ExcecutorParticipationRow participationRow = getObject(row);
		switch (ExecutorParticipationCols.values()[col]) {
			case participationStatus: return participationRow.getParticipationStatus();
			case start: return participationRow.getStart();
			case deadline: return participationRow.getDeadine();
			case title: return participationRow.getTitle();
			case execute: return secCallback.canExecute(participationRow.getParticipation());
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataSourceModel<ExcecutorParticipationRow> createCopyWithEmptyList() {
		return new ExecutorParticipationDataModel(getSourceDelegate(), getTableColumnModel(), secCallback, locale);
	}

	public enum ExecutorParticipationCols implements FlexiSortableColumnDef {
		participationStatus("executor.participation.status"),
		start("executor.participation.start"),
		deadline("executor.participation.deadline"),
		title("executor.participation.title"),
		execute("executor.participation.execute");
		
		private final String i18nKey;
		
		private ExecutorParticipationCols(String i18nKey) {
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
