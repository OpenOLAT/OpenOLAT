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

/**
 *  Description:
 * 
 * 
 * @author Sabina Jeger
 */
public interface TemporaryKey {

	public Long getKey();
	
	public String getEmailAddress();

	public void setEmailAddress(String string);
	
	/**
	 * @return The ip address the registration request came from
	 */
	public String getIpAddress();

	public void setIpAddress(String string);

	public Date getCreationDate();

	/**
	 * @return The key itself
	 */
	public String getRegistrationKey();

	public void setRegistrationKey(String string);
	
	public Date getValidUntil();
	
	public void setValidUntil(Date date);
	
	/**
	 * @return Wether email has been sent.
	 */
	public boolean isMailSent();

	public void setMailSent(boolean b);

	public String getRegAction();
	
	public void setRegAction(String string);

	public Long getIdentityKey();
	
	public void setIdentityKey(Long identityKey);
}