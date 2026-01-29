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
package org.olat.modules.certificationprogram.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 6 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramLogTableModel extends DefaultFlexiTableDataModel<CertificationProgramLogRow> 
implements SortableFlexiTableDataModel<CertificationProgramLogRow> {
	
	private static final ActivityLogCols[] COLS = ActivityLogCols.values();

	private final Locale locale;
	
	public CertificationProgramLogTableModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		locale = translator.getLocale();
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<CertificationProgramLogRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CertificationProgramLogRow logRow = getObject(row);
		return getValueAt(logRow, col);
	}

	@Override
	public Object getValueAt(CertificationProgramLogRow row, int col) {
		return switch(COLS[col]) {
			case date -> row.getCreationDate();
			case context -> row.getContext();
			case object -> row.getObject();
			case message -> row.getMessage();// activity
			case originalValue -> row.getOriginalValue();
			case newValue -> row.getNewValue();
			case user -> row.getActor();
			default -> "ERROR";
		};
	}

	public enum ActivityLogCols implements FlexiSortableColumnDef {
		date("activity.log.date"),
		message("activity.log.message"),
		context("activity.log.context"),
		object("activity.log.object"),
		originalValue("activity.log.original.value"),
		newValue("activity.log.new.value"),
		user("activity.log.user");
		
		private final String i18nKey;

		private ActivityLogCols(String i18nKey) {
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
