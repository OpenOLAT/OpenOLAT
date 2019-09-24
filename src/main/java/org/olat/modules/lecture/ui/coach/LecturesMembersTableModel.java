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
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesMembersTableModel extends DefaultFlexiTableDataModel<LecturesMemberRow> 
implements SortableFlexiTableDataModel<LecturesMemberRow> {
	
	private final Locale locale;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public LecturesMembersTableModel(FlexiTableColumnModel columnModel, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.userPropertyHandlers = userPropertyHandlers;
	}
	
	@Override
	public void sort(SortKey sortKey) {
		//
	}

	@Override
	public Object getValueAt(int row, int col) {
		LecturesMemberRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}
	
	@Override
	public Object getValueAt(LecturesMemberRow row, int col) {	
		if(col < LecturesMembersSearchController.USER_PROPS_OFFSET) {
			switch(PTCols.values()[col]) {
				case username: return row.getIdentityName();
				default: return "ERROR";
			}
		}
		
		if(col >= LecturesMembersSearchController.USER_PROPS_OFFSET) {
			int propPos = col - LecturesMembersSearchController.USER_PROPS_OFFSET;
			return row.getIdentityProp(propPos);
		}
		return "ERROR";
	}

	@Override
	public DefaultFlexiTableDataModel<LecturesMemberRow> createCopyWithEmptyList() {
		return new LecturesMembersTableModel(getTableColumnModel(), userPropertyHandlers, locale);
	}
	
	public enum PTCols implements FlexiSortableColumnDef {
		username("table.header.username");
		
		private final String i18nKey;
		
		private PTCols(String i18nKey) {
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
