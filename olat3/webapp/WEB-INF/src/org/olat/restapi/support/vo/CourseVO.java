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

package org.olat.restapi.support.vo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  20 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "courseVO")
public class CourseVO {
	
	private Long key;
	private String softKey;
	private Long repoEntryKey;
	private String title;
	private String editorRootNodeId;
	
	
	@XmlElement(name="link")
	private List<LinkVO> link = new ArrayList<LinkVO>();
	
	public CourseVO() {
		//make JAXB happy
	}
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public String getSoftKey() {
		return softKey;
	}

	public void setSoftKey(String softKey) {
		this.softKey = softKey;
	}

	public Long getRepoEntryKey() {
		return repoEntryKey;
	}

	public void setRepoEntryKey(Long repoEntryKey) {
		this.repoEntryKey = repoEntryKey;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getEditorRootNodeId() {
		return editorRootNodeId;
	}

	public void setEditorRootNodeId(String editorRootNodeId) {
		this.editorRootNodeId = editorRootNodeId;
	}

	public List<LinkVO> getLink() {
		return link;
	}

	public void setLink(List<LinkVO> link) {
		this.link = link;
	}
	
	
}