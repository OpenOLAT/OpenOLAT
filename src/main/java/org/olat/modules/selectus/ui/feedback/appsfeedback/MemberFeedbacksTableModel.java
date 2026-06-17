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
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 29 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberFeedbacksTableModel extends DefaultFlexiTableDataModel<MemberFeedbackRow>
implements SortableFlexiTableDataModel<MemberFeedbackRow>, FilterableFlexiTableModel {

	private static final MemberFeedCols[] COLS = MemberFeedCols.values();
	
	private final Locale locale;
	private final Translator translator;
	
	private List<MemberFeedbackRow> backupRows;
	
	public MemberFeedbacksTableModel(FlexiTableColumnModel columnsModel, Translator translator, Locale locale) {
		super(columnsModel);
		this.locale = locale;
		this.translator = translator;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<MemberFeedbackRow> views = new MemberFeedbacksSortTableModelDelegate(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString)) {
			
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();
		
			List<MemberFeedbackRow> filteredRows = new ArrayList<>(backupRows.size());
			for(MemberFeedbackRow row:backupRows) {
				boolean accept = accept(loweredSearchString, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else if(backupRows == null) {
			super.setObjects(new ArrayList<>());
		} else {
			super.setObjects(backupRows);
		}
	}
	
	private boolean accept(String searchValue, MemberFeedbackRow row) {
		if(searchValue == null) return true;
		return accept(searchValue, row.getPosition().getMLTitle(locale))
				|| accept(searchValue, row.getApplication().getPerson().getFirstName())
				|| accept(searchValue, row.getApplication().getPerson().getLastName())
				|| accept(searchValue, getOrganisation(row.getOrganisation()))
				|| accept(searchValue, row.getPosition().getMLDepartement(locale))
				|| accept(searchValue, row.getPosition().getPlaningsNumber());
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		MemberFeedbackRow ref = getObject(row);
		return getValueAt(ref, col);
	}
	
	@Override
	public Object getValueAt(MemberFeedbackRow row, int col) {
		switch(COLS[col]) {
			case positionTitle: return getPositionTitle(row.getPosition());
			case application: return RecruitingHelper.formatFullName(row.getApplication(), translator);
			case submissionDeadline: return getSubmissionDeadline(row);
			case myFeedback: return row.getEditLink();
			case organisation: return getOrganisation(row.getOrganisation());
			case department: return row.getPosition().getMLDepartement(locale);
			case planingsNumber: return row.getPosition().getPlaningsNumber();
			default: return "ERROR";
		}
	}
	
	private String getOrganisation(Organisation organisation) {
		return organisation == null ? null : organisation.getDisplayName();
	}
	
	private String getPositionTitle(Position position) {
		String title = position.getMLTitle(locale);
		if(!StringHelper.containsNonWhitespace(title)) {
			title = "Untitled";
		}
		return title;
	}
	
	private Date getSubmissionDeadline(MemberFeedbackRow row) {
		ApplicationFeedback feedback = row.getFeedback();
		Date deadline = feedback.getDeadline();
		if(deadline == null) {
			deadline = feedback.getConfiguration().getDeadline();
		}
		return deadline;
	}
	
	@Override
	public void setObjects(List<MemberFeedbackRow> objects) {
		backupRows = new ArrayList<>(objects);
		super.setObjects(objects);
	}
	
	public enum MemberFeedCols implements FlexiSortableColumnDef {
		positionTitle("edit.position_title"),
		application("table.header.feedback.application"),
		submissionDeadline("table.header.reference.submission.deadline"),
		myFeedback("table.header.feedback.my.feedback"),
		organisation("table.header.organisation.unit"),
		department("edit.department"),
		planingsNumber("edit.position_id");
		
		private final String i18nKey;
		
		private MemberFeedCols(String i18nKey) {
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
