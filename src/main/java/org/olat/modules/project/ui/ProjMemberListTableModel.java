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
package org.olat.modules.project.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjMemberListTableModel extends DefaultFlexiTableDataModel<ProjMemberRow> implements SortableFlexiTableDataModel<ProjMemberRow> {
	
	static final String USER_PROPS_ID = ProjMemberListTableModel.class.getCanonicalName();
	static final int USER_PROPS_OFFSET = 500;
	private static final MemberCols[] COLS = MemberCols.values();
	
	private final Locale locale;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public ProjMemberListTableModel(FlexiTableColumnModel columnModel, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ProjMemberRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ProjMemberRow member = getObject(row);
		return getValueAt(member, col);
	}

	@Override
	public Object getValueAt(ProjMemberRow row, int col) {
		if(col < USER_PROPS_OFFSET) {
			switch(COLS[col]) {
				case id: return row.getIdentity().getKey();
				case roles: return row.getTranslatedRoles();
				case tools: return row.getToolsLink();
				default: return null;
			}
		}
		
		int propPos = col - USER_PROPS_OFFSET;
		return userPropertyHandlers.get(propPos).getUserProperty(row.getIdentity().getUser(), locale);
	}
	
	public enum MemberCols implements FlexiSortableColumnDef {
		id("id"),
		lastVisitDate("member.last.visit.date"),
		roles("roles"),
		tools("tools");
		
		private final String i18nKey;
		
		private MemberCols(String i18nKey) {
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
