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
 * Initial date: 29 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentitiesLecturesRollCallTableModel extends DefaultFlexiTableDataModel<IdentityLecturesRollCallsRow> 
implements SortableFlexiTableDataModel<IdentityLecturesRollCallsRow> {
	
	private static final IdentitiesLecturesCols[] COLS = IdentitiesLecturesCols.values();
	
	private final Locale locale;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public IdentitiesLecturesRollCallTableModel(FlexiTableColumnModel columnModel, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.userPropertyHandlers = userPropertyHandlers;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<IdentityLecturesRollCallsRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		IdentityLecturesRollCallsRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(IdentityLecturesRollCallsRow row, int col) {	
		if(col < IdentitiesLecturesRollCallController.USER_PROPS_OFFSET) {
			switch(COLS[col]) {
				case tools: return row.getTools();
				case immunoStatus: return row.getImmunoStatus();
				default: return "ERROR";
			}
		}
		
		if(col >= IdentitiesLecturesRollCallController.USER_PROPS_OFFSET
				&& col < IdentitiesLecturesRollCallController.LECTURES_OFFSET) {
			User user = row.getIdentity().getUser();
			int propPos = col - IdentitiesLecturesRollCallController.USER_PROPS_OFFSET;
			return userPropertyHandlers.get(propPos).getUserProperty(user, locale);
		}
		
		if(col >= IdentitiesLecturesRollCallController.LECTURES_OFFSET) {
			int partPos = col - IdentitiesLecturesRollCallController.LECTURES_OFFSET;
			IdentityLecturesRollCallPart part = row.getPart(partPos);
			if(part.isParticipate()) {
				if(part.getRollCall() == null) {
					return "?";
				}
				return part.getStatusItem();
			}
			return "-";
		}
		return null;
	}

	@Override
	public IdentitiesLecturesRollCallTableModel createCopyWithEmptyList() {
		return new IdentitiesLecturesRollCallTableModel(getTableColumnModel(), userPropertyHandlers, locale);
	}
	
	public enum IdentitiesLecturesCols implements FlexiSortableColumnDef {
		status("table.header.status"),
		tools("table.header.tools"),
		immunoStatus("table.header.immuno.status");
		
		private final String i18nKey;
		
		private IdentitiesLecturesCols(String i18nKey) {
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
