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
package org.olat.modules.project.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;

/**
 * 
 * Initial date: 3 Feb 2023<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjActivityLogTableModel extends DefaultFlexiTableDataModel<ProjActivityLogRow>
	implements SortableFlexiTableDataModel<ProjActivityLogRow>, ExportableFlexiTableDataModel {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String USAGE_IDENTIFIER = ProjActivityLogTableModel.class.getCanonicalName();
	private static final ActivityLogCols[] COLS = ActivityLogCols.values();
	
	private final Translator translator;
	private final Locale locale;
	
	public ProjActivityLogTableModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
		this.locale = translator.getLocale();
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<ProjActivityLogRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public MediaResource export(FlexiTableComponent ftC) {
		FlexiTableColumnModel columnModel = getTableColumnModel();
		int numOfColumns = columnModel.getColumnCount();
		List<FlexiColumnModel> columns = new ArrayList<>();
		for(int i=0; i<numOfColumns; i++) {
			FlexiColumnModel column = columnModel.getColumnModel(i);
			if(column.isExportable()) {
				columns.add(column);
			}
		}
		return new ProjActivityLogExport().export(ftC, columns, translator);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ProjActivityLogRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(ProjActivityLogRow row, int col) {
		if(col >= 0 && col < ActivityLogCols.values().length) {
			switch(COLS[col]) {
				case date: return row.getDate();
				case message: return row.getMessage();
				case originalValue: return row.getOriginalValue();
				case newValue: return row.getNewValue();
				default: return "ERROR";
			}
		}
		int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	public enum ActivityLogCols implements FlexiSortableColumnDef {
		date("activity.log.date"),
		message("activity.log.message"),
		originalValue("activity.log.original.value"),
		newValue("activity.log.new.value");
		
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