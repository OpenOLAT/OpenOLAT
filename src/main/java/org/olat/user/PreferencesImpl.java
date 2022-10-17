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

package org.olat.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.id.Preferences;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;


/**
 * Desciption:
 * Implementation of the user preferences. When creating a new preferences object
 * all attributes are set to null! One needs to populate the object with the setter 
 * methods. This is a component object of a user.
 *
 * @author guido
 */
@Embeddable
public class PreferencesImpl implements Preferences {

	private static final long serialVersionUID = -8013230820111033911L;

	@Column(name="language", nullable=true, insertable=true, updatable=true)
	private String language;
	@Column(name="fontsize", nullable=true, insertable=true, updatable=true)
	private String fontsize; // not used anymore
	@Column(name="notification_interval", nullable=true, insertable=true, updatable=true)
	private String notificationInterval;
	@Column(name="informsessiontimeout", nullable=true, insertable=true, updatable=true)
	private boolean informSessionTimeout = false;
	@Column(name="receiverealmail", nullable=true, insertable=true, updatable=true)
	private String receiveRealMail;
	@Column(name="presencemessagespublic", nullable=true, insertable=true, updatable=true)
	private boolean presenceMessagesPublic;
	
	/**
	 * Default constructor.
	 */
	public PreferencesImpl() { super(); }

	/**
	 * Get users language settings
	 * @return Users language
	 */
	@Override
	public String getLanguage() {
		return this.language;
	}

	/** 
	 * Set users language settings
	 * @param l new language
	 */
	@Override
	public void setLanguage(String l) {
		// validate the language; fallback to default
		I18nManager i18n = I18nManager.getInstance();
		this.language = i18n.getLocaleOrDefault(l).toString();
	}
	
	/**
	 * @param notificationInterval The notificationInterval to set.
	 */
	@Override
	public void setNotificationInterval(String notificationInterval) {
		this.notificationInterval = notificationInterval;
	}

	/**
	 * @return Returns the notificationInterval.
	 */
	@Override
	public String getNotificationInterval() {
		// Always return a valid notification interval
		NotificationsManager notiMgr = CoreSpringFactory.getImpl(NotificationsManager.class);
		if (!StringHelper.containsNonWhitespace(notificationInterval)) {
			if(notiMgr != null) {
				notificationInterval = notiMgr.getDefaultNotificationInterval();
			}
		} else if(notiMgr != null && notiMgr.getEnabledNotificationIntervals() != null
			&& !notiMgr.getEnabledNotificationIntervals().contains(notificationInterval)) {
			notificationInterval = notiMgr.getDefaultNotificationInterval();
		}
		return notificationInterval;
	}

	/**
	 * @return True if user wants to be informed about the session timeout (popup)
	 */
	@Override
	public boolean getInformSessionTimeout() {
		return informSessionTimeout;
	}

	/**
	 * @param b Set information about wether session timeout should be displayed or not
	 */
	@Override
	public void setInformSessionTimeout(boolean b) {
		informSessionTimeout = b;
	}
	
	/**
	 * @see org.olat.core.id.Preferences#isReceiveRealMail()
	 */
	//fxdiff VCRP-16: intern mail system
	@Override
	public String getReceiveRealMail() {
		return receiveRealMail;
	}

	/**
	 * @see org.olat.core.id.Preferences#setReceiveRealMail(boolean)
	 */
	@Override
	public void setReceiveRealMail(String receiveRealMail) {
		this.receiveRealMail = receiveRealMail;
	}

	/**
	 * @see org.olat.user.Preferences#getPresenceMessagesPublic()
	 */
	@Override
	public boolean getPresenceMessagesPublic() {
		return presenceMessagesPublic;
	}

	/**
	 * @see org.olat.user.Preferences#setPresenceMessagesPublic(boolean)
	 */
	@Override
	public void setPresenceMessagesPublic(boolean b) {
		this.presenceMessagesPublic = b;
	}
}