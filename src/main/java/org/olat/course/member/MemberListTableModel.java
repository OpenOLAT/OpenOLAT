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
package org.olat.course.member;

import java.util.Collections;

import org.olat.core.gui.components.table.DefaultTableDataModel;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MemberListTableModel extends DefaultTableDataModel<MemberView> {
	
	private final int numOfColumns;
	
	public MemberListTableModel(int numOfColumns) {
		super(Collections.<MemberView>emptyList());
		this.numOfColumns = numOfColumns;
	}

	@Override
	public int getColumnCount() {
		return numOfColumns;
	}

	@Override
	public Object getValueAt(int row, int col) {
		MemberView member = getObject(row);
		switch(Cols.values()[col]) {
			case firstName: return member.getFirstName();
			case lastName: return member.getLastName();
			case firstTime: return member.getFirstTime();
			case lastTime: return member.getLastTime();
			case role: return member.getMembership();
			case groups: return member;
			default: return "ERROR";
		}
	}
	
	public enum Cols {
		firstName("table.header.firstName"),
		lastName("table.header.lastName"),
		firstTime("table.header.firstTime"),
		lastTime("table.header.lastTime"),
		role("table.header.role"),
		groups("table.header.groups");
		
		private final String i18n;
		
		private Cols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18n() {
			return i18n;
		}
	}
}
