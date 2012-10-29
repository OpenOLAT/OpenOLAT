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
package org.olat.group.ui.main;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MemberListTableModel extends DefaultTableDataModel<MemberView> {
	
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public MemberListTableModel(List<UserPropertyHandler> userPropertyHandlers) {
		super(Collections.<MemberView>emptyList());
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public int getColumnCount() {
		return 4 + userPropertyHandlers.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		MemberView member = getObject(row);
		switch(col) {
			case 0: return member.getFirstTime();
			case 1: return member.getLastTime();
			case 2: return member.getMembership();
			case 3: return member;
			default: {
				int propPos = col-4;
				if(propPos < userPropertyHandlers.size()) {
					UserPropertyHandler handler = userPropertyHandlers.get(propPos);
					String value = handler.getUserProperty(member.getIdentity().getUser(), getLocale());
					return value;
				}
				return null;
			}
		}
	}
	
	public enum Cols {
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
