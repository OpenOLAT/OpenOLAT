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
	private final String body;
	private String avatarKey;
	
	public ChatMessage(String creationDate, String from, String body,
			boolean first, boolean anonym) {
		this.creationDate = creationDate;
		this.from = from;
		this.body = body;
		this.first = first;
		this.anonym = anonym;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public String getFrom() {
		return from;
	}

	public String getBody() {
		return body;
	}

	public String getAvatarKey() {
		return avatarKey;
	}

	public void setAvatarKey(String avatarKey) {
		this.avatarKey = avatarKey;
	}

	public boolean isFirst() {
		return first;
	}

	public boolean isAnonym() {
		return anonym;
	}
	
	
}
