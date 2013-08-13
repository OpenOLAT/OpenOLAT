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

import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailTemplate;

/**
 * Initial Date: Nov 23 2006
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com Comment: The
 *         MultiIdentityChosenEvent is fired when multiple identites are
 *         selected for whatever purpose. it contains a List of identites that
 *         can be recalled
 */
public class MultiIdentityChosenEvent extends Event {

	private static final long serialVersionUID = 4441806725281221375L;
	private List<Identity> identities;
	private MailTemplate mailTemplate;

	/**
	 * @param identities the List of choosen identities
	 */
	public MultiIdentityChosenEvent(List<Identity> identities) {
		super("MultiIdentitiesFound");
		this.identities = identities;
	}

	/**
   * Can be used from classes which extends the MultiIdentityChosenEvent class.
	 * @param identities  The List of choosen identities
	 * @param command     Command name from super class.
   */
	protected MultiIdentityChosenEvent(List<Identity> identities, String command) {
		super(command);
		this.identities = identities;
	}

	/**
	 * @return Returns the list of choosen identities.
	 */
	public List<Identity> getChosenIdentities() {
		return identities;
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
