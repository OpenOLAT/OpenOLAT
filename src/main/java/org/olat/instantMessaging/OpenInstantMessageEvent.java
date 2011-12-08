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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.instantMessaging;

import org.olat.core.gui.UserRequest;
import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Description:<br>
 * Message to open a new message window. Only use this event with
 * the Single VM message bus with the SingleUserEventCenter!!!
 * 
 * <P>
 * Initial Date:  2 mar. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenInstantMessageEvent extends MultiUserEvent {

	private final String jabberId;
	private final UserRequest ureq;

	public OpenInstantMessageEvent(String jabberId, UserRequest ureq) {
		super("openim");
		this.jabberId = jabberId;
		this.ureq = ureq;
	}

	public String getJabberId() {
		return jabberId;
	}

	public UserRequest getUreq() {
		return ureq;
	}
}
