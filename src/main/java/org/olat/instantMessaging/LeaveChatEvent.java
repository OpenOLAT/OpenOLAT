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
package org.olat.instantMessaging;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 5 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LeaveChatEvent extends MultiUserEvent {

	private static final long serialVersionUID = 4395873098351195377L;
	public static final String LEAVE_CHAT = "leave-chat";
	
	private Long identityKey;
	private OLATResourceable ores;
	
	public LeaveChatEvent(Long identityKey, OLATResourceable ores) {
		super(LEAVE_CHAT);
		this.identityKey = identityKey;
		this.ores = ores;
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}
	
	public OLATResourceable getOres() {
		return ores;
	}
	
	public boolean sameOres(OLATResourceable resource) {
		if(resource == null || ores == null) return false;
		return ores.getResourceableId() != null && ores.getResourceableId().equals(resource.getResourceableId())
				&& ores.getResourceableTypeName() != null && ores.getResourceableTypeName().equals(resource.getResourceableTypeName());
	}
}
