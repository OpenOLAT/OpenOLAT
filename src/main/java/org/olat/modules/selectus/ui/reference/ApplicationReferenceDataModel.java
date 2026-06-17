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

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

import org.olat.modules.selectus.SalutationGenerator;

/**
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationReferenceDataModel extends DefaultFlexiTableDataModel<ApplicationReference>
	implements SortableFlexiTableDataModel<ApplicationReference> {
	
	private static final ARCols[] COLS = ARCols.values();
	
	private final Locale locale;
	private final SalutationGenerator salutationGenerator;
	
	public ApplicationReferenceDataModel(FlexiTableColumnModel columnsModel, SalutationGenerator salutationGenerator, Locale locale) {
		super(columnsModel);
		this.locale = locale;
		this.salutationGenerator = salutationGenerator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ApplicationReference> views = new ApplicationReferenceSortDelegate(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ApplicationReference ref = getObject(row);
		return getValueAt(ref, col);
	}

	@Override
	public Object getValueAt(ApplicationReference row, int col) {
		switch(COLS[col]) {
			case refereeFullName:
			case comparativeExpertFullName:
			case expertFullName: return salutationGenerator.getFullname(row.getReference(), locale);
			case status: return row.getReference();
			case submissionDeadline: return row.getReference().getSubmissionDeadline();
			case sendMail: return row.getSendLink();
			default: return "ERROR";
		}
	}
	
	public enum ARCols implements FlexiSortableColumnDef {
		refereeFullName("table.header.referee.fullname"),
		expertFullName("table.header.expert.fullname"),
		comparativeExpertFullName("table.header.comparative.expert.fullname"),
		status("table.header.reference.status"),
		submissionDeadline("table.header.reference.submission.deadline"),
		sendMail("table.header.action");
		
		private final String i18nKey;
		
		private ARCols(String i18nKey) {
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
		
		public static ARCols getValueAt(int ordinal) {
			if(ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			return status;
		}
	}
	
	
	private static class ApplicationReferenceSortDelegate extends SortableFlexiTableModelDelegate<ApplicationReference> {
		
		public ApplicationReferenceSortDelegate(SortKey orderBy, ApplicationReferenceDataModel tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}
		
		@Override
		protected void sort(List<ApplicationReference> rows) {
			super.sort(rows);
		}
	}
}
