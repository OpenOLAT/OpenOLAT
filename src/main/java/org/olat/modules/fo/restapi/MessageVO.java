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

package org.olat.modules.fo.restapi;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.Status;
import org.olat.restapi.support.vo.FileVO;

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
	private Long threadKey;
	private Long forumKey;
	private Long parentKey;
	private Long authorKey;
	private String author;
	private String authorName;
	private Long modifierKey;
	private String modifier;
	private String modifierName;
	private Boolean sticky;
	
	private String title;
	private String body;
	
	private FileVO[] attachments;
	
	public MessageVO() {
		//make JAXB happy
	}
	
	public MessageVO(Message message) {
		key = message.getKey();
		if(message.getThreadtop() != null) {
			threadKey = message.getThreadtop().getKey();
		}
		Identity auth = message.getCreator();
		authorKey = auth.getKey();
		authorName = auth.getName();
		author = auth.getUser().getProperty(UserConstants.FIRSTNAME, null) + " " + auth.getUser().getProperty(UserConstants.LASTNAME, null);
		
		Identity mod = message.getModifier();
		if(mod != null) {
			modifierKey = mod.getKey();
			modifierName = mod.getName();
			modifier = mod.getUser().getProperty(UserConstants.FIRSTNAME, null) + " " + mod.getUser().getProperty(UserConstants.LASTNAME, null);
		}
		
		if(message.getParent() != null) {
			parentKey = message.getParent().getKey();
		}
		
		forumKey = message.getForum().getKey();
		title = message.getTitle();
		body = message.getBody();
		
		Status messageStatus = Status.getStatus(message.getStatusCode());
		sticky = Boolean.valueOf(messageStatus.isSticky());
	}

	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	public Long getThreadKey() {
		return threadKey;
	}

	public void setThreadKey(Long threadKey) {
		this.threadKey = threadKey;
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

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public Long getModifierKey() {
		return modifierKey;
	}

	public void setModifierKey(Long modifierKey) {
		this.modifierKey = modifierKey;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getModifierName() {
		return modifierName;
	}

	public void setModifierName(String modifierName) {
		this.modifierName = modifierName;
	}

	public Boolean getSticky() {
		return sticky;
	}

	public void setSticky(Boolean sticky) {
		this.sticky = sticky;
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
	
	public FileVO[] getAttachments() {
		return attachments;
	}

	public void setAttachments(FileVO[] attachments) {
		this.attachments = attachments;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -54236 : getKey().hashCode();
	}

	@Override
	public String toString() {
		return "messageVO[key=" + key + ":title=" + title +"]";
	}
	
	@Override
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