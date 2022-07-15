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
package org.olat.modules.portfolio.ui.shared;

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
 * Initial date: 15 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InviteeBindersAdminDataModel extends DefaultFlexiTableDataModel<InviteeBinderAdminRow>
implements SortableFlexiTableDataModel<InviteeBinderAdminRow>  {
	
	private static final BinderAdminCols[] COLS = BinderAdminCols.values();
	
	private final Locale locale;
	
	public InviteeBindersAdminDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<InviteeBinderAdminRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		InviteeBinderAdminRow binderRow = getObject(row);
		return getValueAt(binderRow, col);
	}

	@Override
	public Object getValueAt(InviteeBinderAdminRow row, int col) {
		switch(COLS[col]) {
			case binderKey: return row.getBinderKey();
			case binderOwner: return row.getOwnerFullname();
			case binderName: return row.getBinderName();
			case courseName: return row.getCourseName();
			case invitationDate: return row.getInvitationDate();
			case invitationLink: return row.getInvitationLink();
			default: return "ERROR";
		}
	}

	public enum BinderAdminCols implements FlexiSortableColumnDef {
		binderKey("table.header.key"),
		binderName("table.header.title"),
		courseName("table.header.course"),
		binderOwner("table.header.owner"),
		invitationDate("table.header.invitation.date"),
		invitationLink("table.header.invitation");
		
		private final String i18nKey;
		
		private BinderAdminCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != invitationLink;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
