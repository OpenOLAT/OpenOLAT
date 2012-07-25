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
package org.olat.group.ui.wizard;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGUserManagementGroupTableDataModel extends DefaultTableDataModel<Identity> {
	private List<UserPropertyHandler> userPropertyHandlers;

	public BGUserManagementGroupTableDataModel(List<Identity> combo, Locale locale, List<UserPropertyHandler> userPropertyHandlers) {
		super(combo);
		setLocale(locale);		
		this.userPropertyHandlers = userPropertyHandlers;
	}


	public final Object getValueAt(int row, int col) {
		Identity identity = getObject(row);
		User user = identity.getUser();
		
		if (col == 0) {
			return identity.getName();			

		} else if (col > 0 && col < userPropertyHandlers.size()+1 ) {
			// get user property for this column
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col-1);
			String value = userPropertyHandler.getUserProperty(user, getLocale());
			return (value == null ? "n/a" : value);
			
		} else {
			return "error";			
		}
	}

	public int getColumnCount() {
		return userPropertyHandlers.size() + 2;
	}
}
