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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 29 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MemberSearchTableModel extends DefaultFlexiTableDataModel<MemberRow> implements SortableFlexiTableDataModel<MemberRow> {
	
	private static final MembersCols[] COLS = MembersCols.values();
	
	private final Locale locale;
	
	public MemberSearchTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<MemberRow> views = new MemberSearchSortDelegate(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	public List<Integer> getIndexes(Collection<Long> keys) {
		Set<Long> keysSet = Set.copyOf(keys);
		
		List<Integer> indexes = new ArrayList<>(keys.size());
		List<MemberRow> mRows = this.getObjects();
		for(int i=0; i<mRows.size(); i++) {
			MemberRow mRow = mRows.get(i);
			if(keysSet.contains(mRow.getIdentityKey())) {
				indexes.add(Integer.valueOf(i));
			}
		}
		
		return indexes;
	}

	@Override
	public Object getValueAt(int row, int col) {
		MemberRow memberRow = getObject(row);
		return getValueAt(memberRow, col);
	}
	
	@Override
	public Object getValueAt(MemberRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case id -> row.getIdentityKey();
				case role, origin -> row.getMembership();
				case creationDate -> row.getCreationDate();
				case userPortrait -> row.getPortraitComponent();
				default -> "ERROR";
			};
		}
		
		if(col >= MemberSearchController.USER_PROPS_OFFSET) {
			int propPos = col - MemberSearchController.USER_PROPS_OFFSET;
			return row.getIdentityProp(propPos);
		}
		return "ERROR";
	}
	
	public enum MembersCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		userPortrait("table.header.user.portrait"),
		role("table.header.role"),
		origin("table.header.origin"),
		creationDate("table.header.date.entry");
		
		private final String i18n;
		
		private MembersCols(String i18n) {
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
			return userPortrait != this;
		}
		@Override
		public String sortKey() {
			return i18n;
		}
	}
}
