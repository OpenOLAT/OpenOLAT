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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.den;

import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;

/**
 * Table data model for the view of participants enrolled in an event
 * @author skoeber
 */
class DENParticipantsTableDataModel extends DefaultTableDataModel<Identity> {
	
	public static final String MAIL_ACTION = "denSendMail";
	public static final String REMOVE_ACTION = "denRemoveParticipant";
	
	private static final int COLUMN_COUNT = 2;

	public DENParticipantsTableDataModel(List<Identity> objects) {
		super(objects);
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Identity identity = objects.get(row);
		
		switch (col) {
		case 0:
			return identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale());
		case 1:
			return identity.getUser().getProperty(UserConstants.LASTNAME, getLocale());
		case 2:
			return identity.getUser().getProperty(UserConstants.EMAIL, getLocale());
		case 3:
			return Boolean.TRUE;
		case 4:
			return Boolean.TRUE;

		default:
			return "error";
		}
	}
	
	public void setEntries(List<Identity> newEntries) {
		this.objects = newEntries;
	}
	
	public Identity getEntryAt(int row) {
		return objects.get(row);
	}
	
}
