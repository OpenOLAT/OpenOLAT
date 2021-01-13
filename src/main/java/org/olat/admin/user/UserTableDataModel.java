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

package org.olat.admin.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * <pre>
 * Initial Date:  Jul 29, 2003
 *
 * @author gnaegi
 * Comment:  
 * The user table data model. This uses a list of Identities 
 * and not org.olat.user.User to build the list!
 * </pre>
 */
public class UserTableDataModel extends DefaultTableDataModel<Identity> {

	private final List<UserPropertyHandler> userPropertyHandlers;
	private static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private UserTableDataModel(Locale locale, List<UserPropertyHandler> userPropertyHandlers) {
		super(new ArrayList<>());
		setLocale(locale);
		this.userPropertyHandlers = userPropertyHandlers;
	}
	
	/**
	 * @param objects
	 */
	public UserTableDataModel(List<Identity> objects, Locale locale, boolean isAdministrativeUser) {
		super(objects);
		setLocale(locale);
		userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
	}
	
	/**
	 * Add all column descriptors to this table that are available in the table
	 * model
	 * 
	 * @param tableCtr
	 * @param actionCommand command fired when the login name is clicked or NULL when no command is used
	 */
	public void addColumnDescriptors(TableController tableCtr, String actionCommand) {
		// followed by the users fields
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			tableCtr.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(i+1, actionCommand, getLocale()));						
		}
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public final Object getValueAt(int row, int col) {
		Identity identity = getObject(row);
		User user = identity.getUser();
		if (col == 0) {
			return identity.getName();

		} else if ((col-1) < userPropertyHandlers.size()) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col-1);
			String value = userPropertyHandler.getUserProperty(user, getLocale());
			return (value == null ? "n/a" : value);

		} else {
			return "error";			
		}
	}
	
	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return userPropertyHandlers.size() + 1;
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new UserTableDataModel(getLocale(), userPropertyHandlers);
	}

	/**
	 * Return the selected identity
	 * @param rowid
	 * @return
	 */
	public Identity getIdentityAt(int rowid) {
		return getObject(rowid);
	}

}
