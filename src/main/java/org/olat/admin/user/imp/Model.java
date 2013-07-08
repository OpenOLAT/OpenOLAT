/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.user.imp;

import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.id.Identity;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial Date: 2005
 * 
 * @author Felix Jost, Roman Haag
 * 
 * Description: Table model for user mass import.
 */
public class Model extends DefaultTableDataModel<Identity> {

	private List<UserPropertyHandler> userPropertyHandlers;
	private static final String usageIdentifyer = UserImportController.class.getCanonicalName();
	private int columnCount = 0;

	public Model(List<Identity> objects, int columnCount) {
		super(objects);
		userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, true);
		this.columnCount = columnCount;
	}

	@Override
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Identity ident = getObject(row);
		boolean userExists = !(ident instanceof TransientIdentity);
		
		if (col == 0) { // existing
			return (userExists ? Boolean.FALSE : Boolean.TRUE);
		}
		if (col == 1) {
			return ident.getName();
		}

		if (col == 2) {// pwd
			if (userExists) {
				return "-";
			} else {
				return (((TransientIdentity)ident).getPassword() == null ? "-" : "***");
			}
		} else if (col == 3) {// lang
			if (userExists) {
				return ident.getUser().getPreferences().getLanguage();
			} else {
				return ((TransientIdentity)ident).getLanguage();
			}
		} else if (col > 3 && col < getColumnCount()) {
			// get user property for this column for an already existing user
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col - 4);
			String value = userPropertyHandler.getUserProperty(ident.getUser(), getLocale());
			return (value == null ? "n/a" : value);
		}
		return "ERROR";
	}
}
