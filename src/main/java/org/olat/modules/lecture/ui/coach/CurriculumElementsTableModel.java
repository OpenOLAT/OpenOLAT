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
package org.olat.modules.lecture.ui.coach;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.lecture.model.LectureCurriculumElementInfos;

/**
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementsTableModel extends DefaultFlexiTableDataModel<LectureCurriculumElementInfos>
implements SortableFlexiTableDataModel<LectureCurriculumElementInfos> {
	
	private Locale locale;
	
	public CurriculumElementsTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<LectureCurriculumElementInfos> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureCurriculumElementInfos infos = getObject(row);
		return getValueAt(infos, col);
	}

	@Override
	public Object getValueAt(LectureCurriculumElementInfos row, int col) {
		CurriculumElement element = row.getElement();
		switch(LectureCurriculumCols.values()[col]) {
			case key: return element.getKey();
			case curriculum: return row.getCurriculum().getDisplayName();
			case identifier: return element.getIdentifier();
			case displayName: return element.getDisplayName();
			case externalId: return element.getExternalId();
			case beginDate: return element.getBeginDate();
			case endDate: return element.getEndDate();
			case numOfParticipants: return row.getNumOfParticipants();
			case absences: return isLecturesEnabled(row);
			default: return "ERROR";
		}
	}
	
	private boolean isLecturesEnabled(LectureCurriculumElementInfos row) {
		CurriculumElement element = row.getElement();
		CurriculumElementType elementType = row.getElementType();
		return CurriculumLectures.isEnabled(element, elementType);
	}
	
	public enum LectureCurriculumCols implements FlexiSortableColumnDef {
		
		key("table.header.key"),
		curriculum("table.header.curriculum"),
		displayName("table.header.curriculum.element.displayName"),
		identifier("table.header.curriculum.element.identifier"),
		externalId("table.header.external.id"),
		beginDate("table.header.begin.date"),
		endDate("table.header.end.date"),
		numOfParticipants("table.header.num.participants"),
		absences("table.header.absences");
		
		private final String i18nHeaderKey;
		
		private LectureCurriculumCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
