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
package org.olat.instantMessaging.ui;

/**
 * 
 * Initial date: 03.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChatMessage {
	
	private final boolean first;
	private final boolean anonym;
	private final String creationDate;
	private final String from;
	private final Long fromKey;
	private final String body;
	private Long avatarKey;
	
	public ChatMessage(String creationDate, String from, Long fromKey, String body,
			boolean first, boolean anonym) {
		this.creationDate = creationDate;
		this.from = from;
		this.fromKey = fromKey;
		this.body = body;
		this.first = first;
		this.anonym = anonym;
	}

	/**
	 * The creation date of the message
	 * @return
	 */
	public String getCreationDate() {
		return creationDate;
	}

	/**
	 * The display name of the author of the message
	 * @return
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * The identity key of the author of the message
	 * @return
	 */
	public Long getFromKey() {
		return fromKey;
	}

	/**
	 * The message text
	 * @return
	 */
	public String getBody() {
		return body;
	}

	/**
	 * The key to the avatar icon or -1 if no avatar icon is available
	 * @return
	 */
	public Long getAvatarKey() {
		return avatarKey;
	}

	public void setAvatarKey(Long avatarKey) {
		this.avatarKey = avatarKey;
	}

	/**
	 * true if this is the message before this one was written by another user.
	 * false if the message before this one was written by the same user.
	 * 
	 * @return
	 */
	public boolean isFirst() {
		return first;
	}

	/**
	 * true if the user posted this message as an anonymous user
	 * @return
	 */
	public boolean isAnonym() {
		return anonym;
	}
	
	
}
