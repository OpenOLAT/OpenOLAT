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

import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;

/**
 * Initial Date:  01.09.2004
 *
 * @author Mike Stock
 */

public class UserSessionTableModel extends DefaultTableDataModel<UserSession> {
	
	private final Long myIdentityKey;

	/**
	 * @param userSessions
	 */
	public UserSessionTableModel(List<UserSession> userSessions, Long myIdentityKey) {
		super(userSessions);
		this.myIdentityKey = myIdentityKey;
	}
	
	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 7;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		UserSession usess = (UserSession)getObject(row); 
		SessionInfo sessInfo = usess.getSessionInfo();
		if (usess.isAuthenticated()) {
			switch (col) {
				case 0: return sessInfo.getLastname();
				case 1: return sessInfo.getFirstname();
				case 2: return sessInfo.getLogin();
				case 3: return sessInfo.getAuthProvider();
				case 4: return sessInfo.getFromFQN();
				case 5: try {
					//from nano to milli!!
					return new Date(sessInfo.getLastClickTime());
					//return new Date(sessInfo.getSession().getLastAccessedTime());
				} catch (Exception ise) {
					return null; // "Invalidated"; but need to return a date or null
				}
				case 6: try {
					return sessInfo.getSessionDuration()/1000;
				}catch (Exception ise){
					return -1;
				}
				case 7: 
					if (sessInfo.isWebDAV()) {
						return "WebDAV";
					} else if (sessInfo.isREST()) {
						return "REST";
					} else {
						return sessInfo.getWebMode();						
					}
				case 8: {
					//can chat?
					return myIdentityKey.equals(usess.getIdentity().getKey())
							? Boolean.FALSE : Boolean.TRUE;
				}
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
