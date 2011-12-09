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
* <p>
*/ 

package org.olat.instantMessaging;

import org.jivesoftware.smack.ConnectionListener;

/**
 * Initial Date: 21.02.2005
 * Helper Class for connection monitoring of instant messaging connection
 * @author guido
 */
public class XMPPConnListener implements ConnectionListener {
	private InstantMessagingClient imc = null;

	public XMPPConnListener(InstantMessagingClient imc) {
		this.imc = imc;
	}

	/**
	 * called on normal disconnect
	 * 
	 * @see org.jivesoftware.smack.ConnectionListener#connectionClosed()
	 */
	public void connectionClosed() {
		// if connection.close() is called by OLAT the imc is already disconnected when getting this event
		if (imc.isConnected()) {
			imc.setIsConnected(false);
			InstantMessagingModule.getAdapter().getClientManager().destroyInstantMessagingClient(imc.getUsername());
		}
	}

	/**
	 * called automatically when server crashes
	 * 
	 * @see org.jivesoftware.smack.ConnectionListener#connectionClosedOnError(java.lang.Exception)
	 */
	public void connectionClosedOnError(Exception e) {
		// if connection.close() is called by OLAT the imc is already disconnected when getting this event
		if (imc.isConnected()) {
			imc.setIsConnected(false);
			InstantMessagingModule.getAdapter().getClientManager().destroyInstantMessagingClient(imc.getUsername());
		}
	}

	public void reconnectingIn(int arg0) {
		// TODO Auto-generated method stub
		
	}

	public void reconnectionFailed(Exception arg0) {
		// TODO Auto-generated method stub
		
	}

	public void reconnectionSuccessful() {
		// TODO Auto-generated method stub
		
	}

}