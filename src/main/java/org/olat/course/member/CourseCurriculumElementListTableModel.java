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
package org.olat.course.member;

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
 * Initial date: 14 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CourseCurriculumElementListTableModel extends DefaultFlexiTableDataModel<CourseCurriculumElementRow>
implements SortableFlexiTableDataModel<CourseCurriculumElementRow> {
	
	private static final ElementsCols[] COLS = ElementsCols.values();
	
	private final Locale locale;
	
	public CourseCurriculumElementListTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CourseCurriculumElementRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CourseCurriculumElementRow infosRow = getObject(row);
		return getValueAt(infosRow, col);
	}

	@Override
	public Object getValueAt(CourseCurriculumElementRow row, int col) {
		return switch(COLS[col]) {
			case id -> row.getKey();
			case displayName -> row.curriculumElement().getDisplayName();
			case externalRef -> row.curriculumElement().getIdentifier();
			case externalId -> row.curriculumElement().getExternalId();
			case curriculum -> row.curriculum().getDisplayName();
			case defaultElement -> Boolean.valueOf(row.defaultElement());
			case beginDate -> row.curriculumElement().getBeginDate();
			case endDate -> row.curriculumElement().getEndDate();
			case numOfOwners -> row.numOfOwners();
			case numOfCoaches -> row.numOfCoaches();
			case numOfParticipants -> row.numOfParticipants();
			case status -> row.curriculumElement().getElementStatus();
			default -> "ERROR";
		};
	}

	public enum ElementsCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		displayName("table.header.element"),
		externalRef("table.header.external.ref"),
		externalId("table.header.external.id"),
		curriculum("table.header.curriculum"),
		defaultElement("table.header.default.element"),
		beginDate("table.header.begin.date"),
		endDate("table.header.end.date"),
		numOfParticipants("table.header.num.of.participants"),
		numOfCoaches("table.header.num.of.coaches"),
		numOfOwners("table.header.num.of.owners"),
		status("table.header.status");
		
		private final String i18n;
		
		private ElementsCols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18n() {
			return i18n;
		}

		@Override
		public String i18nHeaderKey() {
			return i18n;
		}

		@Override
		public boolean sortable() {
			return true;
		}
		@Override
		public String sortKey() {
			return i18n;
		}
	}
}
