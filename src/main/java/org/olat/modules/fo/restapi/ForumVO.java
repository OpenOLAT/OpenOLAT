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
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.fo.Forum;

/**
 * 
 * <h3>Description:</h3>
 * Wrapper class for Forum
 * <p>
 * Initial Date:  11 janv. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "forum")
public class ForumVO {

	@XmlAttribute(name="name", required=false)
	private String name;
	@XmlAttribute(name="detailsName", required=false)
	private String detailsName;
	@XmlAttribute(name="forumKey", required=false)
	private Long forumKey;
	@XmlAttribute(name="groupKey", required=false)
	private Long groupKey;
	@XmlAttribute(name="courseKey", required=false)
	private Long courseKey;
	@XmlAttribute(name="courseNodeId", required=false)
	private String courseNodeId;
	@XmlAttribute(name="subscribed", required=false)
	private boolean subscribed;
	
	public ForumVO() {
		//make JAXB happy
	}

	public ForumVO(Forum forum) {
		forumKey = forum.getKey();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDetailsName() {
		return detailsName;
	}

	public void setDetailsName(String detailsName) {
		this.detailsName = detailsName;
	}

	public Long getForumKey() {
		return forumKey;
	}

	public void setForumKey(Long forumKey) {
		this.forumKey = forumKey;
	}

	public Long getGroupKey() {
		return groupKey;
	}

	public void setGroupKey(Long groupKey) {
		this.groupKey = groupKey;
	}

	public Long getCourseKey() {
		return courseKey;
	}

	public void setCourseKey(Long courseKey) {
		this.courseKey = courseKey;
	}

	public String getCourseNodeId() {
		return courseNodeId;
	}

	public void setCourseNodeId(String courseNodeId) {
		this.courseNodeId = courseNodeId;
	}

	public boolean isSubscribed() {
		return subscribed;
	}

	public void setSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
	}
}
