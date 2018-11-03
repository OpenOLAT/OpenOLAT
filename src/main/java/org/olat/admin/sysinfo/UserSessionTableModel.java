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

package org.olat.admin.sysinfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.olat.admin.sysinfo.model.UserSessionView;
import org.olat.core.gui.components.table.DefaultTableDataModel;

/**
 * Initial Date:  01.09.2004
 *
 * @author Mike Stock
 */

public class UserSessionTableModel extends DefaultTableDataModel<UserSessionView> {
	
	private final Long myIdentityKey;
	private final Map<String,String> fromFQN = new ConcurrentHashMap<>();

	/**
	 * @param userSessions
	 */
	public UserSessionTableModel(List<UserSessionView> userSessions, Long myIdentityKey) {
		super(userSessions);
		this.myIdentityKey = myIdentityKey;
	}

	@Override
	public int getColumnCount() {
		return 7;
	}

	@Override
	public Object getValueAt(int row, int col) {
		UserSessionView usess = getObject(row); 
		if (usess.isAuthenticated()) {
			switch (col) {
				case 0: return usess.getLastname();
				case 1: return usess.getFirstname();
				case 2: return usess.getLogin();
				case 3: return usess.getAuthProvider();
				case 4: return fromFQN.computeIfAbsent(usess.getFromIP(), ip -> {
					String fqn = null;
					try {
						InetAddress[] iaddr = InetAddress.getAllByName(ip);
						if (iaddr.length > 0) {
							fqn = iaddr[0].getHostName();
						}
					} catch (UnknownHostException e) {
						//       ok, already set IP as FQDN
					}
					return fqn == null ? ip : fqn;
				});
				case 5: return usess.getLastClickTime();
				case 6: return usess.getSessionDuration();
				case 7: return usess.getMode();
				case 8: return myIdentityKey.equals(usess.getIdentityKey()) ? Boolean.FALSE : Boolean.TRUE;
				case 9: return usess.getLastAccessTime();
				default: return "Error";
			}
		} else { // not signed on
			switch (col) {
				case 5: return null;
				case 6: return null;
				case 7: return null;
				default: return "-";
			}
		}																			
	}
}
