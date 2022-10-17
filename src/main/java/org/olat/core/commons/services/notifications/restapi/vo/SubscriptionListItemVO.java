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

package org.olat.core.commons.services.notifications.restapi.vo;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import org.olat.core.commons.services.notifications.model.SubscriptionListItem;

/**
 * 
 * <h3>Description:</h3>
 * 
 * <p>
 * Initial Date:  25 aug. 2010 <br>
 * @author srosse, srosse@frentix.com, www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SubscriptionListItemVO {
	
	private String link;
	private Date date;
	private String description;
	private String iconCssClass;
	
	private String type;
	private Long groupKey;
	private Long courseKey;
	private String courseNodeId;
	
	//to the message of a forum
	private Long messageKey;
	//to the file path
	private String path;
	
	public SubscriptionListItemVO() {
		//make JAXB happy
	}
	
	public SubscriptionListItemVO(SubscriptionListItem item) {
		link = item.getLink();
		date = item.getDate();
		description = item.getDescription();
		iconCssClass = item.getIconCssClass();
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIconCssClass() {
		return iconCssClass;
	}

	public void setIconCssClass(String iconCssClass) {
		this.iconCssClass = iconCssClass;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public Long getMessageKey() {
		return messageKey;
	}

	public void setMessageKey(Long messageKey) {
		this.messageKey = messageKey;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}