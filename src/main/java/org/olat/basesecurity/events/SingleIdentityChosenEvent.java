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

package org.olat.basesecurity.events;

import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailTemplate;

/**
 * Initial Date:  Feb 19, 2004
 *
 * @author jeger
 * 
 * Comment:  
 * The SingleIdentityChosenEvent has an additional field that tells which identity has been found
 */
public class SingleIdentityChosenEvent extends Event {
	
	private static final long serialVersionUID = -4006475285941616460L;
	private Identity identity;
	private MailTemplate mailTemplate;
	
	/**
	 * Event of type 'SingleIdentityChosenEvent' with extra parameter, the identity itself
	 * @param identity Must not be NULL
	 */
	public SingleIdentityChosenEvent(Identity identity) {
		super("IdentityFound");
		this.identity = identity;
	}
		
	/**
	 * @return Returns the identity.
	 */
	public Identity getChosenIdentity() {
		return identity;
	}
	
	/**
	 * @return a mailTemplate that is used in this workflow or NULL if not used
	 */
	public MailTemplate getMailTemplate() {
		return mailTemplate;
	}

	/**
	 * @param mailTemplate a mailTemplate that is used in this workflow or NULL if
	 *          not used
	 */
	public void setMailTemplate(MailTemplate mailTemplate) {
		this.mailTemplate = mailTemplate;
	}

}
