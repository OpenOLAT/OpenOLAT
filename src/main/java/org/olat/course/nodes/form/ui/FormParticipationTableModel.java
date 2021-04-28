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
package org.olat.course.nodes.form.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.modules.forms.EvaluationFormParticipationStatus;

/**
 * 
 * Initial date: 21.04.2021<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormParticipationTableModel extends DefaultFlexiTableDataModel<FormParticipationRow>
	implements SortableFlexiTableDataModel<FormParticipationRow>, FilterableFlexiTableModel {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String USAGE_IDENTIFIER = FormParticipationTableModel.class.getCanonicalName();
	public static final String FILTER_IN_PROGRESS = "inProgress";
	public static final String FILTER_DONE = "done";
	public static final String FILTER_NOT_START = "notStart";
	private static final ParticipationCols[] COLS = ParticipationCols.values();
	
	private final Locale locale;
	private List<FormParticipationRow> backups;
	
	public FormParticipationTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<FormParticipationRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		String key = filters == null || filters.isEmpty() || filters.get(0) == null ? null : filters.get(0).getFilter();
		if (StringHelper.containsNonWhitespace(key)) {
			List<FormParticipationRow> filteredRows;
			if (FILTER_NOT_START.equals(key)) {
				filteredRows = backups.stream()
						.filter(row -> row.getStatus() == null)
						.collect(Collectors.toList());
			} else if (FILTER_IN_PROGRESS.equals(key)) {
				filteredRows = backups.stream()
						.filter(row -> row.getStatus() == EvaluationFormParticipationStatus.prepared)
						.collect(Collectors.toList());
			} else if (FILTER_DONE.equals(key)) {
				filteredRows = backups.stream()
						.filter(row -> row.getStatus() == EvaluationFormParticipationStatus.done)
						.collect(Collectors.toList());
			} else {
				filteredRows = new ArrayList<>(backups);
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	@Override
	public void setObjects(List<FormParticipationRow> objects) {
		backups = objects;
		super.setObjects(objects);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		FormParticipationRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(FormParticipationRow row, int col) {
		if(col >= 0 && col < ParticipationCols.values().length) {
			switch(COLS[col]) {
				case status: return row.getStatus();
				default: return "ERROR";
			}
		}
		int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}

	@Override
	public DefaultFlexiTableDataModel<FormParticipationRow> createCopyWithEmptyList() {
		return new FormParticipationTableModel(getTableColumnModel(), locale);
	}
	
	public enum ParticipationCols implements FlexiSortableColumnDef {
		status("table.header.status");
		
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