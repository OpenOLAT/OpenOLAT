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
package org.olat.modules.selectus.ui.reference;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.ui.RecruitingHelper;


/**
 * 
 * Initial date: 20 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicantRefereeTableModel extends DefaultFlexiTableDataModel<ApplicantRefereeRow>
implements SortableFlexiTableDataModel<ApplicantRefereeRow> {
	
	private static final RefCols[] COLS = RefCols.values();
	
	private final Locale locale;
	private final Position position;

	public ApplicantRefereeTableModel(FlexiTableColumnModel columnsModel, Position position, Locale locale) {
		super(columnsModel);
		this.locale = locale;
		this.position = position;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ApplicantRefereeRow> views = new ApplicantRefereeTableSortDelegate(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		ApplicantRefereeRow arRow = getObject(row);
		return getValueAt(arRow, col);
	}

	@Override
	public Object getValueAt(ApplicantRefereeRow row, int col) {
		Reference reference = row.getReference();
		
		switch(COLS[col]) {
			case fullName: return RecruitingHelper.formatPersonLastnameFirstname(reference);
			case mail: return reference.getEmail();
			case referenceStatus: return reference;
			case submissionDeadline: return getSubmissionDeadline(row);
			case dateInvitation: return reference.getDateInvitation();
			case dateLastReminder: return reference.getDateLastReminder();
			case numOfReminders: return getRemindersSent(reference);
			case sendReminder: return Boolean.valueOf(reference.getRequestStatus() != ReferenceRequestStatus.declined
					&& reference.getReferenceStatus() != ReferenceStatus.deactivated && reference.getReferenceStatus() != ReferenceStatus.submitted);
			case tools: return row.getToolsLink();
			default: return "ERROR";
		}
	}
	
	private Integer getRemindersSent(Reference reference) {
		int numOfReminders = reference.getRemindersByApplicant();
		return numOfReminders > 0 ? Integer.valueOf(numOfReminders) : null;
	}
	
	private Date getSubmissionDeadline(ApplicantRefereeRow row) {
		Date deadline = row.getReference().getSubmissionDeadline();
		if(deadline == null) {
			deadline = position.getRefereeRecommandationDeadline();
		}
		return deadline;
	}

	public enum RefCols implements FlexiSortableColumnDef {
		fullName("table.header.reference.name", true),
		mail("table.header.reference.email", true),
		referenceStatus("table.header.reference.status", true),
		submissionDeadline("table.header.reference.submission.deadline", true),
		dateInvitation("table.header.reference.date.invitation", true),
		dateLastReminder("table.header.reference.date.last.reminder", true),
		numOfReminders("table.header.reference.num.of.reminders", true),
		sendReminder("table.header.reference.send.reminder", false),
		tools("table.header.action", false);
		
		private final String i18nKey;
		private final boolean sortable;
		
		private RefCols(String i18nKey, boolean sortable) {
			this.i18nKey = i18nKey;
			this.sortable = sortable;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return sortable;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
	
	private static class ApplicantRefereeTableSortDelegate extends SortableFlexiTableModelDelegate<ApplicantRefereeRow> {
		
		public ApplicantRefereeTableSortDelegate(SortKey orderBy, ApplicantRefereeTableModel tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}
		
		@Override
		protected void sort(List<ApplicantRefereeRow> rows) {
			int columnIndex = getColumnIndex();
			if(columnIndex == RefCols.referenceStatus.ordinal()) {
				Collections.sort(rows, new ReferenceStatusComparator());
			} else {
				super.sort(rows);
			}
		}
		
		private class ReferenceStatusComparator implements Comparator<ApplicantRefereeRow> {

			@Override
			public int compare(ApplicantRefereeRow o1, ApplicantRefereeRow o2) {
				int c = 0;
				if(o1 == null || o2 == null) {
					c = compareNullObjects(o1, o2);
				} else if(o1.getReference() == null || o2.getReference() == null) {
					c = compareNullObjects(o1.getReference(), o2.getReference());
				} else {
					c = ReferenceHelper.compareStatus(o1.getReference().getReferenceStatus(), o1.getReference().getRequestStatus(),
							o2.getReference().getReferenceStatus(), o2.getReference().getRequestStatus());
				}
				return c;
			}	
		}
	}
}
