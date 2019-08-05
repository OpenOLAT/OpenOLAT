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
package org.olat.modules.fo.ui;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.modules.fo.MessageLight;
import org.olat.modules.fo.MessageRef;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 11.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MessageLightView extends UserPropertiesRow implements MessageRef {
	
	private final int type;
	private final Long key;
	private final String title;

	private final Long parentKey;
	private final Date lastModified;

	private final boolean guest;
	private final String pseudonym;

	private final Long threadtopKey;

	private int depth = -1;
	private int numOfChildren = 0;

	private boolean newMessage;

	public MessageLightView(MessageLight message, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(message.getCreator(), userPropertyHandlers, locale);
		key = message.getKey();
		type = message.getStatusCode();
		title = message.getTitle();
		guest = message.isGuest();
		pseudonym = message.getPseudonym();
		lastModified = message.getLastModified();
		parentKey = message.getParentKey();
		threadtopKey = message.getThreadtop() == null ? null : message.getThreadtop().getKey();
	}
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public Long getParentKey() {
		return parentKey;
	}
	
	public Long getThreadtopKey() {
		return threadtopKey;
	}

	@Override
	public int getStatusCode() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public boolean isGuest() {
		return guest;
	}

	public String getPseudonym() {
		return pseudonym;
	}
	
	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public boolean isHasChildren() {
		return numOfChildren > 0;
	}

	public int getNumOfChildren() {
		return numOfChildren;
	}

	public void setNumOfChildren(int numOfChildren) {
		this.numOfChildren = numOfChildren;
	}

	public boolean isThreadTop() {
		return threadtopKey != null;
	}

	public boolean isNewMessage() {
		return newMessage;
	}

	public void setNewMessage(boolean newMessage) {
		this.newMessage = newMessage;
	}
}
