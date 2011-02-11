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
* <p>
*/ 

package org.olat.registration;

import java.util.Date;

/**
 *  Description:
 * 
 * 
 * @author Sabina Jeger
 */
public interface TemporaryKey {
	
	
	public int getVersion();
	public void setVersion(int version);
	/**
	 * @return email address
	 */
	public abstract String getEmailAddress();
	/**
	 * @param string
	 */
	public abstract void setEmailAddress(String string);
	/**
	 * @return The ip address the registration request came from
	 */
	public abstract String getIpAddress();
	/**
	 * @param string
	 */
	public abstract void setIpAddress(String string);

	/**
	 * @return Creation date
	 */
	public abstract Date getCreationDate();

	/**
	 * @return The key itself
	 */
	public abstract String getRegistrationKey();
	/**
	 * @param string
	 */
	public abstract void setRegistrationKey(String string);
	/**
	 * @return Wether email has been sent.
	 */
	public abstract boolean isMailSent();
	/**
	 * @param b
	 */
	public abstract void setMailSent(boolean b);
	/**
	 * @return Object key.
	 */
	public abstract Long getKey();
	/**
	 * @param long1
	 */
	public abstract void setKey(Long long1);
	/**
	 * @param date
	 */
	public abstract void setCreationDate(Date date);
	/**
	 * @return Registration action.
	 */
	public abstract String getRegAction();
	/**
	 * @param string
	 */
	public abstract void setRegAction(String string);
}