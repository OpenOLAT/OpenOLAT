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
package org.olat.modules.selectus.ui.committee.imp;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import org.olat.modules.selectus.ui.committee.list.PositionCommitteeController;

/**
 * 
 * Initial date: 29 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectCommitteeTableModel extends DefaultFlexiTableDataModel<CommitteeRow>
implements SortableFlexiTableDataModel<CommitteeRow> {
	
	private static final SelectCols[] COLS = SelectCols.values();

	private final Locale locale;
	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public SelectCommitteeTableModel(FlexiTableColumnModel columnsModel, List<UserPropertyHandler> userPropertyHandlers,
			Translator translator) {
		super(columnsModel);
		this.locale = translator.getLocale();
		this.translator = translator;
		this.userPropertyHandlers = userPropertyHandlers;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		//
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CommitteeRow identity = getObject(row);
		return getValueAt(identity, col);
	}

	@Override
	public Object getValueAt(CommitteeRow row, int col) {
		Identity identity = row.getIdentity();
		if(col < PositionCommitteeController.USER_PROP_OFFSET) {
			switch(COLS[col]) {
				case title: return getTitle(row);
				case name: return identity.getUser().getProperty(UserConstants.LASTNAME, locale) + ", " 
						+ identity.getUser().getProperty(UserConstants.FIRSTNAME, locale);
				case institution: return identity.getUser().getProperty(UserConstants.INSTITUTIONALNAME, locale);
				case role: return row.getSelectRole();
				default: return "ERROR";
			}
		} else if(col >= PositionCommitteeController.USER_PROP_OFFSET) {
			int propIndex = col - PositionCommitteeController.USER_PROP_OFFSET;
			UserPropertyHandler prop = userPropertyHandlers.get(propIndex);
			return prop.getUserProperty(identity.getUser(), translator.getLocale());
		}
		return "ERROR";
	}
	
	private String getTitle(CommitteeRow row) {
		String title = row.getIdentity().getUser().getProperty("title", locale);
		return "-".equals(title) ? "" : title;
	}

	public enum SelectCols implements FlexiSortableColumnDef {
		title("edit.committee.title"),
		name("edit.committee.name"),
		institution("edit.committee.institution"),
		role("table.header.roles");

		private final String key;
		
		private SelectCols(String key) {
			this.key = key;
		}
		
		@Override
		public String i18nHeaderKey() {
			return key;
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
