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
package org.olat.modules.fo.model;

import java.util.Date;

import org.olat.modules.fo.Message;
import org.olat.modules.fo.MessageRef;

/**
 * 
 * Initial date: 10.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumThread implements MessageRef {
	
	private final int type;
	private final Long key;
	private final String title;
	private final Date lastModified;
	
	private final boolean guest;
	private final String pseudonym;
	private final String creatorFullname;
	
	private int markedMessages;
	private int newMessages;
	private int numOfPosts;
	
	public ForumThread(Message message, String creatorFullname, Date lastModified, int numOfPosts) {
		this.key = message.getKey();
		this.type = message.getStatusCode();
		this.title = message.getTitle();
		if(lastModified == null || lastModified.before(message.getLastModified())) {
			this.lastModified = message.getLastModified();
		} else {
			this.lastModified = lastModified;
		}
		this.guest = message.isGuest();
		this.pseudonym = message.getPseudonym();
		this.creatorFullname = creatorFullname;
		this.numOfPosts = numOfPosts;
	}
	
	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public int getStatusCode() {
		return type;
	}
	
	public boolean isGuest() {
		return guest;
	}
	
	public String getPseudonym() {
		return pseudonym;
	}
	
	public String getTitle() {
		return title;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public String getCreatorFullname() {
		return creatorFullname;
	}
	
	public int getNumOfPosts() {
		return numOfPosts;
	}
	
	public int getMarkedMessages() {
		return markedMessages;
	}
	
	public void setMarkedMessages(int markedMessages) {
		this.markedMessages = markedMessages;
	}
	
	public int getNewMessages() {
		return newMessages;
	}
	
	public void setNewMessages(int newMessages) {
		this.newMessages = newMessages;
	}
}
