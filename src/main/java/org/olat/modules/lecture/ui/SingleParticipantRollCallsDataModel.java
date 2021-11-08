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
package org.olat.modules.lecture.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.id.Identity;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 30 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SingleParticipantRollCallsDataModel extends DefaultFlexiTableDataModel<SingleParticipantRollCallRow> {

	private final UserManager userManager;
	
	public SingleParticipantRollCallsDataModel(FlexiTableColumnModel columnModel, UserManager userManager) {
		super(columnModel);
		this.userManager = userManager;
	}

	@Override
	public Object getValueAt(int row, int col) {
		SingleParticipantRollCallRow call = getObject(row);
		if(col < SingleParticipantRollCallsController.CHECKBOX_OFFSET) {
			switch(RollCallsCols.values()[col]) {
				case entry: return call.getEntryDisplayname();
				case externalRef: return call.getEntryExternalRef();
				case lecturesBlock: return call.getLectureBlock().getTitle();
				case times: return call.getLectureBlock();
				case teacher: return getTeachers(call);
				case status: return call.getRollCallStatusEl(); 
				case authorizedAbsence: return call.getAuthorizedAbsenceCont();
				case comment: return call.getCommentEl();
				case all: return call.getAllLink();
				case numOfAbsences: return call.getNumOfAbsencesEl();
				default: return "ERROR";
			}
		}
		int propPos = col - TeacherRollCallController.CHECKBOX_OFFSET;
		return call.getCheck(propPos);
	}
	
	private String getTeachers(SingleParticipantRollCallRow row) {
		StringBuilder sb = new StringBuilder(1024);
		for(Identity teacher:row.getTeachers()) {
			sb.append("<i class='o_icon o_icon_user'> </i> ")
			  .append(userManager.getUserDisplayName(teacher))
			  .append(" ");	
		}
		return sb.toString();
	}

	@Override
	public DefaultFlexiTableDataModel<SingleParticipantRollCallRow> createCopyWithEmptyList() {
		return new SingleParticipantRollCallsDataModel(getTableColumnModel(), userManager);
	}

	public enum RollCallsCols implements FlexiSortableColumnDef {
		entry("table.header.entry"),
		externalRef("table.header.external.ref"),
		lecturesBlock("table.header.lecture.block"),
		times("table.header.times"),
		teacher("table.header.teachers"),
		status("table.header.status"),
		authorizedAbsence("table.header.authorized.absence"),
		comment("table.header.comment"),
		all("all"),
		numOfAbsences("table.header.absences");
		
		private final String i18nKey;
		
		private RollCallsCols(String i18nKey) {
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
