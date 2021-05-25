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
import org.olat.core.id.User;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 21 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesAbsenceRollCallsTableModel extends DefaultFlexiTableDataModel<LectureAbsenceRollCallRow> 
implements SortableFlexiTableDataModel<LectureAbsenceRollCallRow> {
	
	private static final AbsenceCallCols[] COLS = AbsenceCallCols.values();
	
	private final Locale locale;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public LecturesAbsenceRollCallsTableModel(FlexiTableColumnModel columnModel, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<LectureAbsenceRollCallRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureAbsenceRollCallRow r = getObject(row);
		return getValueAt(r, col);
	}

	@Override
	public Object getValueAt(LectureAbsenceRollCallRow row, int col) {
		if(col < LecturesAbsenceRollCallsController.USER_PROPS_OFFSET) {
			switch(COLS[col]) {
				case id: return row.getRollCall().getKey();
				case lectureBlockDate: return row.getLectureBlock();
				case lectureBlockName: return row.getLectureBlock().getTitle();
				case externalRef: return row.getLectureBlock().getEntry().getExternalRef();
				case entry: return row.getLectureBlock().getEntry().getDisplayname();
				case lectureBlockLocation: return row.getLectureBlock().getLocation();
				case authorizedAbsence: return row.getAuthorized();
				case absentLectures: return row.getRollCall().getLecturesAbsentNumber();
				case absenceNotice: return row.getNoticeLink();
				default: return "ERROR";
			}
		}
		
		if(col >= LecturesAbsenceRollCallsController.USER_PROPS_OFFSET) {
			User user = row.getIdentity().getUser();
			int propPos = col - LecturesAbsenceRollCallsController.USER_PROPS_OFFSET;
			return userPropertyHandlers.get(propPos).getUserProperty(user, locale);
		}
		return "ERROR";
	}

	@Override
	public DefaultFlexiTableDataModel<LectureAbsenceRollCallRow> createCopyWithEmptyList() {
		return new LecturesAbsenceRollCallsTableModel(getTableColumnModel(), userPropertyHandlers, locale);
	}
	

	public enum AbsenceCallCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		externalRef("table.header.entry.external.ref"),
		entry("table.header.entry"),
		lectureBlockDate("table.header.date"),
		lectureBlockName("table.header.lecture.block"),
		lectureBlockLocation("lecture.location"),
		absentLectures("table.header.absent.lectures"),
		authorizedAbsence("table.header.authorized.absence"),
		absenceNotice("table.header.infos");

		private final String i18nKey;
		
		private AbsenceCallCols(String i18nKey) {
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
