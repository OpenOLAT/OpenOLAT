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

public class IdentitiesOfGroupTableDataModel extends DefaultTableDataModel<GroupMemberView> {
	private List<UserPropertyHandler> userPropertyHandlers;
   
	/**
	 * @param combo a List of Object[] with the array[0] = Identity, array[1] = addedToGroupTimestamp
	 */
	public IdentitiesOfGroupTableDataModel(List<GroupMemberView> combo, Locale locale, List<UserPropertyHandler> userPropertyHandlers) {
		super(combo);
		setLocale(locale);		
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public final Object getValueAt(int row, int col) {
		GroupMemberView co = getObject(row);
		if(col == 1) {
			return co.getOnlineStatus();
		}
		if(col == 2) {
			return co.getAddedAt();
		}
		int index = col - 3;
		if(index >= 0 && index < userPropertyHandlers.size()) {
			User user = co.getIdentity().getUser();
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col - 3);
			String value = userPropertyHandler.getUserProperty(user, getLocale());
			return (value == null ? "n/a" : value);
		}
		return "ERROR";
	}

	@Override
	public int getColumnCount() {
		// online status + add date
		return userPropertyHandlers.size() +  2;
	}
	
	@Override
	public IdentitiesOfGroupTableDataModel createCopyWithEmptyList() {
		return new IdentitiesOfGroupTableDataModel(new ArrayList<GroupMemberView>(), getLocale(), userPropertyHandlers);
	}
	
	/**
	 * Return a list of identites for this bitset
	 * @param objectMarkers
	 * @return
	 */
	public List<Identity> getIdentities(BitSet objectMarkers) {
		List<Identity> results = new ArrayList<>();
		for(int i=objectMarkers.nextSetBit(0); i >= 0; i=objectMarkers.nextSetBit(i+1)) {
			GroupMemberView elem = getObject(i);
			results.add(elem.getIdentity());
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
		for (Iterator<GroupMemberView> it_obj = getObjects().iterator(); it_obj.hasNext();) {
			GroupMemberView obj = it_obj.next();
			Identity aIdent = obj.getIdentity();
			if (aIdent.equals(ident)) {
				it_obj.remove();
				return;
			}
		}
	}

	/**
	 * Add identities to table-model.
	 * @param addedIdentities  Add this list of identities.
	 */
	public void add(List<GroupMemberView> addedIdentities) {
		for (GroupMemberView identity : addedIdentities) {
			add(identity);
		}
	}

	/**
	 * Add an identity to table-model.
	 * @param ident
	 */
	private void add(GroupMemberView identity) {
		getObjects().add(identity);
	}
}
