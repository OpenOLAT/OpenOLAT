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

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.quality.QualityReminder;

/**
 * 
 * Initial date: 09.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReminderDataModel extends DefaultFlexiTableDataModel<QualityReminder>
implements SortableFlexiTableDataModel<QualityReminder> {
	
	private final Translator translator;
	
	public ReminderDataModel(FlexiTableColumnModel columnsModel, Translator translator) {
		super(columnsModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<QualityReminder> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, translator.getLocale()).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		QualityReminder reminder = getObject(row);
		return getValueAt(reminder, col);
	}

	@Override
	public Object getValueAt(QualityReminder row, int col) {
		System.out.println("col: " + col);
		ReminderCols[] values = ReminderCols.values();
		switch (values[col]) {
		case sent: return row.isSent();
		case sendDate: return row.getSendDate();
		case to: return translator.translate(row.getTo().getI18nKey());
		case subject: return row.getSubject();
		case edit: return true;
		case delete: return !row.isSent();
		default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataModel<QualityReminder> createCopyWithEmptyList() {
		return new ReminderDataModel(getTableColumnModel(), translator);
	}
	
	public enum ReminderCols implements FlexiSortableColumnDef {
		sent("reminder.sent.title"),
		sendDate("reminder.send.date.title"),
		to("reminder.to.title"),
		subject("reminder.subject.title"),
		edit("reminder.edit.title"),
		delete("reminder.delete.title");
		
		private final String i18nKey;
		
		private ReminderCols(String i18nKey) {
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
