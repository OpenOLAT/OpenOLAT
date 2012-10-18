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

package org.olat.admin.securitygroup.gui;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial Date:  Jul 29, 2003
 *
 * @author Felix Jost, Florian Gnaegi
 */

public class IdentitiesOfGroupTableDataModel extends DefaultTableDataModel {
	private List<UserPropertyHandler> userPropertyHandlers;
	private boolean isAdministrativeUser;

   
	/**
	 * @param combo a List of Object[] with the array[0] = Identity, array[1] = addedToGroupTimestamp
	 */
	public IdentitiesOfGroupTableDataModel(List<Object[]> combo, Locale locale, List<UserPropertyHandler> userPropertyHandlers, boolean isAdministrativeUser) {
		super(combo);
		setLocale(locale);		
		this.userPropertyHandlers = userPropertyHandlers;
		this.isAdministrativeUser = isAdministrativeUser;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public final Object getValueAt(int row, int col) {
		Object[] co = (Object[])getObject(row);
		Identity identity = (Identity) co[0];

		// special case: user name only for administrative users
		if (col == 0 && isAdministrativeUser) {
			return identity.getName();						
		}

		User user = identity.getUser();
		if (col - (isAdministrativeUser ? 1 : 0) < userPropertyHandlers.size()) {
			// get user property for this column
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col - (isAdministrativeUser ? 1 : 0));
			String value = userPropertyHandler.getUserProperty(user, getLocale());
			return (value == null ? "n/a" : value);
			
		} else if (col == userPropertyHandlers.size() + (isAdministrativeUser ? 1 : 0)) {
			// one item more than available handlers is the added date
			Date addedTo = (Date) co[1];
			return addedTo;

		} else {
			return "error";			
		}
	}
	
	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		// + loginname + adddate or just + loginname
		return userPropertyHandlers.size() + (isAdministrativeUser ? 2 : 1);
	}
	
	/**
	 * @param rowid
	 * @return The identity at the given position in the dable
	 */
	public Identity getIdentityAt(int rowid) {
		Object[] co = (Object[])getObject(rowid);
		Identity ident = (Identity) co[0];
		return ident;
	}

	/**
	 * Return a list of identites for this bitset
	 * @param objectMarkers
	 * @return
	 */
	public List<Identity> getIdentities(BitSet objectMarkers) {
		List<Identity> results = new ArrayList<Identity>();
		for(int i=objectMarkers.nextSetBit(0); i >= 0; i=objectMarkers.nextSetBit(i+1)) {
			Object[] elem = (Object[]) getObject(i);
			results.add((Identity)elem[0]);
		}
		return results;
	}
	
	/**
	 * Remove identities from table-model.
	 * @param toBeRemoved  Remove this identities from table-model.
	 */
	public void remove(List<Identity> toBeRemoved) {
		for (Identity identity : toBeRemoved) {
			remove(identity);
		}
	}
	
	/**
	 * Remove an identity from table-model.
	 * @param ident
	 */
	private void remove(Identity ident) {
		for (Iterator<Object[]> it_obj = getObjects().iterator(); it_obj.hasNext();) {
			Object[] obj = it_obj.next();
			Identity aIdent = (Identity) obj[0];
			if (aIdent == ident) {
				it_obj.remove();
				return;
			}
		}
	}

	/**
	 * Add identities to table-model.
	 * @param addedIdentities  Add this list of identities.
	 */
	public void add(List<Identity> addedIdentities) {
		for (Identity identity : addedIdentities) {
			add(identity);
		}
	}

	/**
	 * Add an identity to table-model.
	 * @param ident
	 */
	private void add(Identity identity) {
		getObjects().add(new Object[] { identity, new Date() });
	}

}
