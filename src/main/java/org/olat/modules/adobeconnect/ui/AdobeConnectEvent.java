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
package org.olat.modules.adobeconnect.ui;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 4 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectEvent extends MultiUserEvent {

	private static final long serialVersionUID = 3199767160246830180L;

	public static final String OPEN_MEETING = "adobe-connect-open-meeting";
	public static final String CREATE_MEETING = "adobe-connect-create-meeting";
	
	private final Long meetingKey;
	private final Long actingIdentityKey;
	
	public AdobeConnectEvent(String name, Long meetingKey, Long actingIdentityKey) {
		super(name);
		this.meetingKey = meetingKey;
		this.actingIdentityKey = actingIdentityKey;
	}

	public Long getMeetingKey() {
		return meetingKey;
	}

	public Long getActingIdentityKey() {
		return actingIdentityKey;
	}
}
