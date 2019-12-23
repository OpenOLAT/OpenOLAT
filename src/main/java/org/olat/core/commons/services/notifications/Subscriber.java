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

package org.olat.core.commons.services.notifications;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 * Initial Date: 21.10.2004 <br>
 * @author Felix Jost
 */
public interface Subscriber extends Persistable, CreateInfo, ModifiedInfo {
	/**
	 * @return the identity
	 */
	public Identity getIdentity();

	/**
	 * @param identity
	 */
	public void setIdentity(Identity identity);

	/**
	 * @return the latest date the user got an email concering this subscription here
	 */
	public Date getLatestEmailed();

	/**
	 * @param latestEmailed
	 */
	public void setLatestEmailed(Date latestEmailed);
	
	/**
	 * @return the publisher
	 */
	public Publisher getPublisher();

	/**
	 * @param publisher
	 */
	public void setPublisher(Publisher publisher);
	
	/**
	 * @return true if the subscriber is enabled, false if it needs to be ignored
	 */
	public boolean isEnabled();
	
	public void setEnabled(boolean enabled);
}