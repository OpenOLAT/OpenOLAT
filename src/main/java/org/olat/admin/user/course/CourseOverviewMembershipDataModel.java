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
package org.olat.admin.user.course;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 16 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseOverviewMembershipDataModel extends DefaultFlexiTableDataModel<CourseMemberView>
implements SortableFlexiTableDataModel<CourseMemberView>, FlexiBusinessPathModel {
	
	private static final MSCols[] COLS = MSCols.values();

	private final Locale locale;
	
	public CourseOverviewMembershipDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CourseMemberView> views = new CourseOverviewMembershipSortDelegate(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public String getUrl(Component source, Object object, String action) {
		if(action == null) return null;
		
		CourseMemberView view = (CourseMemberView)object;
		return view.getUrl();
	}

	@Override
	public Object getValueAt(int row, int col) {
		CourseMemberView  view = getObject(row);
		return getValueAt(view, col);
	}

	@Override
	public Object getValueAt(CourseMemberView view, int col) {
		switch(COLS[col]) {
			case key: return view.getRepoKey();
			case entry: return view.getEntry();
			case title: return view.getDisplayName();
			case externalId: return view.getExternalId();
			case externalRef: return view.getExternalRef();
			case role: return view.getMembership();
			case firstTime: return view.getFirstTime();
			case lastTime: return view.getLastTime();
			case allowLeave: return view.isFullyManaged() ? Boolean.FALSE : Boolean.TRUE;
			case invitationLink: return view.getInvitationLink();
			default: return "ERROR";
		}
	}
	
	public enum MSCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		entry("table.header.typeimg"),
		title("cif.displayname"),
		externalId("table.header.externalid"),
		externalRef("table.header.externalref"),
		role("table.header.role"),
		lastTime("table.header.lastTime"),
		firstTime("table.header.firstTime"),
		allowLeave("table.header.leave"),
		invitationLink("table.header.invitation");
		
		private final String i18n;
		
		private MSCols(String i18n) {
			this.i18n = i18n;
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
			return name();
		}
	}
}
