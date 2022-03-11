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
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.model.InstantMessageNotificationTypeEnum;

/**
 * 
 * Initial date: 07.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InstantMessagingEvent extends MultiUserEvent {

	private static final long serialVersionUID = 4656092473467740418L;
	
	public static final String MESSAGE = InstantMessageNotificationTypeEnum.message.name();
	public static final String REQUEST = InstantMessageNotificationTypeEnum.request.name();
	public static final String DELETE_NOTIFICATION = "delete.notification";
	public static final String PARTICIPANT = "participant";
	public static final String END_CHANNEL = "end.channel";

	private Long messageId;
	private InstantMessageTypeEnum messageType;
	private Long fromId;
	private String name;
	private boolean vip;
	private boolean anonym;
	private OLATResourceable chatResource;
	private String resSubPath;
	private String channel;
	
	public InstantMessagingEvent(String command, OLATResourceable chatResource, String resSubPath, String channel) {
		super(command);
		this.chatResource = chatResource;
		this.resSubPath = resSubPath;
		this.channel = channel;
	}

	public Long getMessageId() {
		return messageId;
	}
	
	public InstantMessageTypeEnum getMessageType() {
		return messageType;
	}

	public void setMessage(Long messageId, InstantMessageTypeEnum messageType) {
		this.messageId = messageId;
		this.messageType = messageType;
	}

	public Long getFromId() {
		return fromId;
	}

	public void setFromId(Long fromId) {
		this.fromId = fromId;
	}

	public OLATResourceable getChatResource() {
		return chatResource;
	}

	public void setChatResource(OLATResourceable chatResource) {
		this.chatResource = OresHelper.clone(chatResource);
	}

	public String getResSubPath() {
		return resSubPath;
	}

	public void setResSubPath(String resSubPath) {
		this.resSubPath = resSubPath;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAnonym() {
		return anonym;
	}

	public void setAnonym(boolean anonym) {
		this.anonym = anonym;
	}

	public boolean isVip() {
		return vip;
	}

	public void setVip(boolean vip) {
		this.vip = vip;
	}
}
