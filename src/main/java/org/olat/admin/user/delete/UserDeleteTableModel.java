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

package org.olat.admin.user.delete;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.commons.lifecycle.LifeCycleEntry;
import org.olat.commons.lifecycle.LifeCycleManager;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * The user table data model for user deletion. This uses a list of Identities 
 * and not org.olat.user.User to build the list!
 * 
 * @author Christian Guretzki
 */
public class UserDeleteTableModel extends DefaultTableDataModel<Identity> {

	private final List<UserPropertyHandler> userPropertyHandlers;
	private final boolean isAdministrativeUser;
	private static final String usageIdentifyer = UserDeleteTableModel.class.getCanonicalName();
	
	/**
	 * 
	 * @param objects
	 * @param locale
	 * @param isAdministrativeUser
	 */
	public UserDeleteTableModel(List<Identity> objects, Locale locale, boolean isAdministrativeUser) {
		super(objects);
		setLocale(locale);
		this.isAdministrativeUser = isAdministrativeUser;
		userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
	}
		
	
	/**
	 * Add all column descriptors to this table that are available in the table model.
	 * The table contains userPropertyHandlers.size() columns plus a column for the
	 * username and one for the last login date.
	 * 
	 * @param tableCtr
	 * @param actionCommand command fired when the login name is clicked or NULL when no command is used
	 */
	public void addColumnDescriptors(TableController tableCtr, String actionCommand) {
		// first column is the username
		if(isAdministrativeUser) {
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.login", 301, actionCommand, getLocale()));
		}
		// followed by the users fields
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			tableCtr.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(i, null, getLocale()));						
		}
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.lastlogin", 302, actionCommand, getLocale()));
	}
	
	/**
	 * The table contains a suplementary column for the "delete email date".
	 * @param tableCtr
	 * @param actionCommand
	 * @param deleteEmailKey
	 */
	public void addColumnDescriptors(TableController tableCtr, String actionCommand, String deleteEmailKey) {
		addColumnDescriptors(tableCtr, actionCommand);
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(deleteEmailKey, 303, actionCommand, getLocale()));
	}

	/**
	 * 
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {		
		Identity identity = getObject(row);
		User user = identity.getUser();

		if (col == 301) {
			return identity.getName();
		} else if (col < userPropertyHandlers.size()) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col);
			String value = userPropertyHandler.getUserProperty(user, getLocale());
			return (value == null ? "n/a" : value);			
		} else if(col == 302) {
			Date lastLogin= identity.getLastLogin();
			return (lastLogin == null ? "n/a" : lastLogin);			
		} else if(col == 303) {
			LifeCycleEntry lcEvent = LifeCycleManager.createInstanceFor(identity).lookupLifeCycleEntry(UserDeletionManager.SEND_DELETE_EMAIL_ACTION);
			if (lcEvent == null) {
				return "n/a";
			}
			Date deleteEmail= lcEvent.getLcTimestamp();
			return (deleteEmail == null ? "n/a" : deleteEmail);					
		} else {
			return "error";			
		}
	}
	
	/**
	 * The table model contains userPropertyHandlers.size() columns plus a column for the
	 * username, one for the last login date, and one for the delete email date.
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getColumnCount()
	 */
  public int getColumnCount() {		
		return userPropertyHandlers.size() + 3;		
	}
	
}
