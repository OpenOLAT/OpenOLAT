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

package org.olat.core.id;

import java.io.Serializable;

/**
 * Description:
 * Interface for a user preferences object. This is a component object
 * of an OLAT user.
 *
 * @author Florian Gnägi
 */
public interface Preferences extends Serializable {

	/**
	 * Get users language settings
	 * @return Users language
	 */
	public String getLanguage();
	
	/** 
	 * Set users language settings
	 * @param l new language
	 */
	public void setLanguage(String l);
	
	/**
	 * Get users fontsize settings
	 * @return Users fontsize
	 */
	public String getFontsize();
	
	/** 
	 * Set users fontsize settings
	 * @param l new fontsize
	 */
	public void setFontsize(String l);

	/**
	 * @param notificationInterval The notificationInterval to set.
	 */
	public void setNotificationInterval(String notificationInterval);

	/**
	 * @return Returns the notificationInterval.
	 */
	public String getNotificationInterval();
	
	/**
	 * @return True if user wants to be informed about the session timeout (popup)
	 */
	public boolean getInformSessionTimeout();

	/**
	 * @param b Set information about wether session timeout should be displayed or not
	 */
	public void setInformSessionTimeout(boolean b);
	
	/**
	 * @return True if the user wants to receive a real e-mail too and not only a message in
	 * the OpenOLAT Inbox
	 */
	public String getReceiveRealMail();

	/**
	 * @param receiveRealMail Set if the user wants to receive a real e-mail
	 * and not only a message in the OpenOLAT intern Inbox.
	 */
	public void setReceiveRealMail(String receiveRealMail);
	
	/**
	 * Instant Messaging preferences
	 * When enabled the presence messages are shown public
	 * on the user list. Only visible in settings if module is loaded
	 * @return true if enabled, default.
	 */
	public boolean getPresenceMessagesPublic();
	
	/**
	 * disable the presence messages 
	 * @param b true to enable, false to disable
	 */
	public void setPresenceMessagesPublic(boolean b);
}