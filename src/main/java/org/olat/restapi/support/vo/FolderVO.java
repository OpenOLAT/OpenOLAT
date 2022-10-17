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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.restapi.support.vo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  16 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "folder")
public class FolderVO {
	
	@XmlAttribute(name="name", required=false)
	private String name;
	@XmlAttribute(name="detailsName", required=false)
	private String detailsName;
	@XmlAttribute(name="groupKey", required=false)
	private Long groupKey;
	@XmlAttribute(name="courseKey", required=false)
	private Long courseKey;
	@XmlAttribute(name="courseNodeId", required=false)
	private String courseNodeId;
	@XmlAttribute(name="subscribed", required=false)
	private boolean subscribed;
	@XmlAttribute(name="write", required=false)
	private boolean write;
	@XmlAttribute(name="read", required=false)
	private boolean read;
	@XmlAttribute(name="list", required=false)
	private boolean list;
	@XmlAttribute(name="delete", required=false)
	private boolean delete;
	
	public FolderVO() {
		//make JAXP happy
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

	public boolean isWrite() {
		return write;
	}

	public void setWrite(boolean write) {
		this.write = write;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isList() {
		return list;
	}

	public void setList(boolean list) {
		this.list = list;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}
}
