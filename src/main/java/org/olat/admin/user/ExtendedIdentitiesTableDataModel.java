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
import java.util.BitSet;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial Date: Feb 6, 2006
 * 
 * @author gnaegi
 * 
 * Description: The extended identities table model. Currently it displays a
 * user and its creation date. The idea is to extend the functionality further
 * to display more information using a wrapper object.
 */
public class ExtendedIdentitiesTableDataModel extends DefaultTableDataModel<Identity> {
	
	public static final String COMMAND_VCARD = "show.vcard";
	public static final String COMMAND_SELECTUSER = "select.user";

	private final boolean actionEnabled;
	private final boolean isAdministrativeUser;
	private int colCount = 0;
	private List<UserPropertyHandler> userPropertyHandlers;
	private static final String usageIdentifyer = ExtendedIdentitiesTableDataModel.class.getCanonicalName();

	/**
	 * Constructor
	 * 
	 * @param identities The list of identities to use in the table
	 * @param actionEnabled true: the action button is enabled; false: list
	 *          without action button
	 */
	ExtendedIdentitiesTableDataModel(UserRequest ureq, List<Identity> identities, boolean actionEnabled) {
		super(identities);
		this.actionEnabled = actionEnabled;

		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = CoreSpringFactory.getImpl(BaseSecurityModule.class).isUserAllowedAdminProps(roles);
		userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
	}

	/**
	 * Add all column descriptors to this table that are available in the table
	 * model
	 * 
	 * @param tableCtr
	 */
	public void addColumnDescriptors(TableController tableCtr, Translator trans) {
		setLocale(trans.getLocale());
		// first column is users login name
		// default rows
		
		if (isAdministrativeUser) {
			String action = actionEnabled ? COMMAND_SELECTUSER : null;
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.identity.name", colCount++, action, getLocale()));
		}

		UserManager um = UserManager.getInstance();
		// followed by the users fields
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			boolean visible = um.isMandatoryUserProperty(usageIdentifyer, userPropertyHandler);
			String action = null;
			if(actionEnabled && i < 2) {
				action = COMMAND_SELECTUSER;
			}
			tableCtr.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(colCount++, action, getLocale()));
		}
		// in the end the last login and creation date
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.identity.lastlogin", colCount++, null, getLocale()));
		// creation date at the end, enabled by default
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.identity.creationdate", colCount++, null, getLocale()));
		
		if(actionEnabled) {
			StaticColumnDescriptor vcd = new StaticColumnDescriptor(COMMAND_VCARD, "table.header.vcard", trans.translate("table.identity.vcard"));
			vcd.setIsPopUpWindowAction(true, "height=700, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
			tableCtr.addColumnDescriptor(vcd);
		}
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	@Override
	public final Object getValueAt(int row, int col) {
		Identity identity = getObject(row);
		User user = identity.getUser();
		int offSet = isAdministrativeUser ? 1 : 0;
		if (col == 0 && isAdministrativeUser) {
			return identity.getName();
		}
		if (col >= offSet && col < userPropertyHandlers.size() + offSet) {
			// get user property for this column
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col - offSet);
			String value = userPropertyHandler.getUserProperty(user, getLocale());
			return (value == null ? "n/a" : value);
		}
		if (col == userPropertyHandlers.size() + offSet) {
			return identity.getLastLogin();
		}
		if (col == userPropertyHandlers.size() + offSet + 1) {
			return user.getCreationDate();
		} 
		return "error";
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return colCount + 1;
	}
	
	public boolean contains(Identity identity) {
		return objects.contains(identity);
	}

	/**
	 * @param selection
	 * @return All Identities which were selected in a multiselect - table
	 */
	public List<Identity> getIdentities(BitSet selection) {
		List<Identity> identities = new ArrayList<>();
		for (int i = selection.nextSetBit(0); i >= 0; i = selection.nextSetBit(i + 1)) {
			Identity identityAt = getObject(i);
			identities.add(identityAt);
		}
		return identities;
	}
}