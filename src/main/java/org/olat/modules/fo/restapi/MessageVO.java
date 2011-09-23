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

package org.olat.modules.fo.restapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.olat.modules.fo.Message;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "messageVO")
public class MessageVO {
	
	private Long key;
	private Long forumKey;
	private Long parentKey;
	private Long authorKey;
	
	private String title;
	private String body;
	
	public MessageVO() {
		//make JAXB happy
	}
	
	public MessageVO(Message message) {
		key = message.getKey();
		authorKey = message.getCreator().getKey();
		if(message.getParent() != null) {
			parentKey = message.getParent().getKey();
		}
		forumKey = message.getForum().getKey();
		title = message.getTitle();
		body = message.getBody();
	}

	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	public Long getForumKey() {
		return forumKey;
	}

	public void setForumKey(Long forumKey) {
		this.forumKey = forumKey;
	}

	public Long getParentKey() {
		return parentKey;
	}

	public void setParentKey(Long parentKey) {
		this.parentKey = parentKey;
	}

	public Long getAuthorKey() {
		return authorKey;
	}

	public void setAuthorKey(Long authorKey) {
		this.authorKey = authorKey;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	public String toString() {
		return "messageVO[key=" + key + ":title=" + title +"]";
	}
	
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof MessageVO) {
			MessageVO vo = (MessageVO)obj;
			return key != null && key.equals(vo.key);
		}
		return false;
	}
}