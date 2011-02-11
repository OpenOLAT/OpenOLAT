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
* <p>
*/ 

package org.olat.modules.fo;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
/**
 * @author Felix Jost
 */

public class MessageImpl extends PersistentObject implements Message {
	
	private String title;
	private String body;
	private Message parent;
	private Message threadtop;
	private Forum forum;

	private Identity creator = null;
	private Identity modifier = null;
	private int statusCode;
	private Date lastModified;
	private Integer numOfCharacters;
	private Integer numOfWords;
		
	/**
	 * Default construcor
	 */
	MessageImpl() {
	    // nothing to do 
	}
	

	/**
	 * @return
	 */
	public String getBody() {
		return body;
	}


	/**
	 * @return
	 */
	public Identity getCreator() {
		return creator;
	}

	/**
	 * @return
	 */
	public Forum getForum() {
		return forum;
	}

	/**
	 * @return
	 */
	public Identity getModifier() {
		return modifier;
	}

	/**
	 * @return
	 */
	public Message getParent() {
		return parent;
	}

	/**
	 * @return
	 */
	public Message getThreadtop() {
		return threadtop;
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param string
	 */
	public void setBody(String string) {
		body = string;
	}

	/**
	 * @param identity
	 */
	public void setCreator(Identity identity) {
		creator = identity;
	}

	/**
	 * @param forum
	 */
	public void setForum(Forum forum) {
		this.forum = forum;
	}

	/**
	 * @param identity
	 */
	public void setModifier(Identity identity) {
		modifier = identity;
	}

	/**
	 * @param message
	 */
	public void setParent(Message message) {
		parent = message;
	}

	/**
	 * @param message
	 */
	public void setThreadtop(Message message) {
		threadtop = message;
	}

	/**
	 * @param string
	 */
	public void setTitle(String string) {
		title = string;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * 
	 * @see org.olat.core.id.ModifiedInfo#getLastModified()
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * 
	 * @see org.olat.core.id.ModifiedInfo#setLastModified(java.util.Date)
	 */
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	public Integer getNumOfCharacters() {
		return numOfCharacters;
	}

	public void setNumOfCharacters(Integer numOfCharacters) {
		this.numOfCharacters = numOfCharacters;
	}

	public Integer getNumOfWords() {
		return numOfWords;
	}

	public void setNumOfWords(Integer numOfWords) {
		this.numOfWords = numOfWords;
	}


	public int compareTo(Message arg0) {
		//threadtop always is on top!
		if (arg0.getParent()==null) return 1;
		if (getCreationDate().after(arg0.getCreationDate())) return 1;
		return 0;
	}

}
