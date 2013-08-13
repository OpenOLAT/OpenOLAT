/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.basesecurity.events;

import org.olat.core.id.Identity;
import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Description:<br>
 * An event which is fired after the creation of a new identity.
 * 
 * <P>
 * Initial Date:  19 august 2009 <br>
 *
 * @author srosse
 */
public class NewIdentityCreatedEvent extends MultiUserEvent {

	private static final long serialVersionUID = 2970338524539022136L;
	private Long identityId;
	
	public NewIdentityCreatedEvent(Identity newIdentity) {
		super("NewIdentityCreated");
		identityId = newIdentity.getKey();
	}

	public Long getIdentityId() {
		return identityId;
	}
}
