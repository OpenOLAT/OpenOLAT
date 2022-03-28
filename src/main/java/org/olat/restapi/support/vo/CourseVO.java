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

package org.olat.restapi.support.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

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
	private String displayName;
	private String description;
	private String teaser;
	private Long repoEntryKey;
	@XmlAttribute(name="organisationKey", required=false)
	private Long organisationKey;
	@XmlAttribute(name="authors", required=false)
	private String authors;
	@XmlAttribute(name="location", required=false)
	private String location;
	private String externalId;
	private String externalRef;
	@Schema(required = true, description = "Action to be performed on managedFlags", allowableValues = { "all",
			  "editcontent(all)",
			    "details(all) //details tab",
			      "title(details,all)",
			      "description(details,all)",
			      "objectives(details,all)",
			      "requirements(details,all)",
			      "credits(details,all)",
			      "location(details,all)",
			      "organisations(details,all)",
			    "settings(all) //max num of participants...",
			     "access(settings,all)",
			      "search(settings, all)",
			      "chat(settings,all)",
			      "layout(settings,all)",
			      "resourcefolder(settings,all)",
			      "efficencystatement(settings,all)",
			      "calendar(settings,all)",
			      "glossary(settings,all)",
			    "lecture(all)",
			      "lectureconfig(lecture, all)",
			      "lecturemanagement(lecture, all)",
			    "bookings(all) // change booking rules",
			    "membersmanagement(all)",
			    "groups(all)",
			    "close(all)",
			    "copy(all)",
			    "delete(all)"})	
	private String managedFlags;
	
	private Long olatResourceKey;
	private Long olatResourceId;
	private String olatResourceTypeName;
	
	private String title;
	private String editorRootNodeId;
	private String nodeAccessType;
	
	private RepositoryEntryLifecycleVO lifecycle;
	
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

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String id) {
		this.externalId = id;
	}

	public String getExternalRef() {
		return externalRef;
	}

	public void setExternalRef(String ref) {
		this.externalRef = ref;
	}

	public String getManagedFlags() {
		return managedFlags;
	}

	public void setManagedFlags(String managedFlags) {
		this.managedFlags = managedFlags;
	}

	public Long getOrganisationKey() {
		return organisationKey;
	}

	public void setOrganisationKey(Long organisationKey) {
		this.organisationKey = organisationKey;
	}

	public Long getOlatResourceKey() {
		return olatResourceKey;
	}

	public void setOlatResourceKey(Long olatResourceKey) {
		this.olatResourceKey = olatResourceKey;
	}

	public Long getOlatResourceId() {
		return olatResourceId;
	}

	public void setOlatResourceId(Long olatResourceId) {
		this.olatResourceId = olatResourceId;
	}

	public String getOlatResourceTypeName() {
		return olatResourceTypeName;
	}

	public void setOlatResourceTypeName(String olatResourceTypeName) {
		this.olatResourceTypeName = olatResourceTypeName;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTeaser() {
		return teaser;
	}

	public void setTeaser(String teaser) {
		this.teaser = teaser;
	}

	public String getEditorRootNodeId() {
		return editorRootNodeId;
	}

	public void setEditorRootNodeId(String editorRootNodeId) {
		this.editorRootNodeId = editorRootNodeId;
	}

	public String getNodeAccessType() {
		return nodeAccessType;
	}

	public void setNodeAccessType(String nodeAccessType) {
		this.nodeAccessType = nodeAccessType;
	}

	public RepositoryEntryLifecycleVO getLifecycle() {
		return lifecycle;
	}

	public void setLifecycle(RepositoryEntryLifecycleVO lifecycle) {
		this.lifecycle = lifecycle;
	}
	
	@Override
	public String toString() {
		return "CourseVO[key=" + key + ":externalId=" + externalId + ":display=" + displayName + "]";
	}
	
	@Override
	public int hashCode() {
		return key == null ? 24348 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CourseVO) {
			CourseVO course = (CourseVO)obj;
			return key != null && key.equals(course.getKey());
		}
		return super.equals(obj);
	}
}