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

package org.olat.registration;

import java.util.Date;

import org.olat.core.id.CreateInfo;

/**
 *  Description:
 * 
 * 
 * @author Sabina Jeger
 */
public class TemporaryKeyImpl implements CreateInfo, TemporaryKey {
	
	private Long key = null;
	private String emailAddress = null;
	private String ipAddress = null;
	private Date creationDate = new Date();
	private Date lastModified = null;
	private String registrationKey = null;
	private String regAction = null;
	private boolean mailSent = false;
	private int version;

	/**
	 * 
	 */
	protected TemporaryKeyImpl() {
		super();
	}

	/**
	 * Temporary key database object.
	 * @param emailaddress
	 * @param ipaddress
	 * @param registrationKey
	 * @param action
	 */
	public TemporaryKeyImpl(String emailaddress, String ipaddress, String registrationKey, String action) {
		this.emailAddress = emailaddress;
		this.ipAddress = ipaddress;
		this.registrationKey = registrationKey;
		this.regAction = action;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#getEmailAddress()
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#setEmailAddress(java.lang.String)
	 */
	public void setEmailAddress(String string) {
		emailAddress = string;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#getIpAddress()
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#setIpAddress(java.lang.String)
	 */
	public void setIpAddress(String string) {
		ipAddress = string;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#getCreationDate()
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#getLastModified()
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#getRegistrationKey()
	 */
	public String getRegistrationKey() {
		return registrationKey;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#setRegistrationKey(java.lang.String)
	 */
	public void setRegistrationKey(String string) {
		registrationKey = string;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#isMailSent()
	 */
	public boolean isMailSent() {
		return mailSent;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#setMailSent(boolean)
	 */
	public void setMailSent(boolean b) {
		mailSent = b;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#getKey()
	 */
	public Long getKey() {
		return key;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#setKey(java.lang.Long)
	 */
	public void setKey(Long long1) {
		key = long1;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#setCreationDate(java.util.Date)
	 */
	public void setCreationDate(Date date) {
		creationDate = date;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#setLastModified(java.util.Date)
	 */
	public void setLastModified(Date date) {
		lastModified = date;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#getRegAction()
	 */
	public String getRegAction() {
		return regAction;
	}

	/**
	 * @see org.olat.registration.TemporaryKey#setRegAction(java.lang.String)
	 */
	public void setRegAction(String string) {
		regAction = string;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
