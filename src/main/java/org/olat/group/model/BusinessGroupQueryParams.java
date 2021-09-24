/**
 * <a href=“http://www.openolat.org“>
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
 * 2011 by frentix GmbH, http://www.frentix.com
 * <p>
**/
package org.olat.group.model;

import java.util.Date;
import java.util.List;

import org.olat.group.BusinessGroupStatusEnum;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 08.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupQueryParams {
	
	private String nameOrDesc;
	private String name;
	private String description;
	private String ownerName;
	private String courseTitle;
	private String externalId;
	private String idRef;
	private List<String> technicalTypes;
	
	private boolean owner;
	private boolean attendee;
	private boolean waiting;
	private Boolean publicGroups;
	private Boolean managed;
	private boolean marked;
	private Boolean resources;
	private boolean headless = false;
	private boolean authorConnection;
	
	private List<Long> businessGroupKeys;
	private RepositoryEntryRef repositoryEntry;

	private Date lastUsageBefore;
	
	private List<BusinessGroupStatusEnum> groupStatus;
	
	private LifecycleSyntheticStatus lifecycleStatus;
	private Date lifecycleStatusReference;
	
	public BusinessGroupQueryParams() {
		//
	}
	
	public BusinessGroupQueryParams(boolean owner, boolean attendee) {
		this.owner = owner;
		this.attendee = attendee;
	}

	public String getIdRef() {
		return idRef;
	}

	public void setIdRef(String idRef) {
		this.idRef = idRef;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

	public String getNameOrDesc() {
		return nameOrDesc;
	}

	public void setNameOrDesc(String nameOrDesc) {
		this.nameOrDesc = nameOrDesc;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public List<String> getTechnicalTypes() {
		return technicalTypes;
	}

	public void setTechnicalTypes(List<String> technicalTypes) {
		this.technicalTypes = technicalTypes;
	}

	public boolean isOwner() {
		return owner;
	}

	public void setOwner(boolean owner) {
		this.owner = owner;
	}

	public boolean isAttendee() {
		return attendee;
	}

	public void setAttendee(boolean attendee) {
		this.attendee = attendee;
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}

	public Boolean getPublicGroups() {
		return publicGroups;
	}

	public void setPublicGroups(Boolean publicGroups) {
		this.publicGroups = publicGroups;
	}

	public Boolean getManaged() {
		return managed;
	}

	public void setManaged(Boolean managed) {
		this.managed = managed;
	}

	public boolean isMarked() {
		return marked;
	}

	/**
	 * Set true to see the bookmarked groups, false has no effect.
	 * @param marked
	 */
	public void setMarked(boolean marked) {
		this.marked = marked;
	}

	public boolean isAuthorConnection() {
		return authorConnection;
	}

	public void setAuthorConnection(boolean authorConnection) {
		this.authorConnection = authorConnection;
	}

	public Boolean getResources() {
		return resources;
	}

	public void setResources(Boolean resources) {
		this.resources = resources;
	}

	public List<Long> getBusinessGroupKeys() {
		return businessGroupKeys;
	}

	public void setBusinessGroupKeys(List<Long> businessGroupKeys) {
		this.businessGroupKeys = businessGroupKeys;
	}

	public RepositoryEntryRef getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(RepositoryEntryRef repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	/**
	 * Only available on views
	 * @return
	 */
	public boolean isHeadless() {
		return headless;
	}

	/**
	 * Only available on views
	 * @param headless
	 */
	public void setHeadless(boolean headless) {
		this.headless = headless;
	}

	public Date getLastUsageBefore() {
		return lastUsageBefore;
	}

	public void setLastUsageBefore(Date lastUsageBefore) {
		this.lastUsageBefore = lastUsageBefore;
	}
	
	public List<BusinessGroupStatusEnum> getGroupStatus() {
		return groupStatus;
	}

	public void setGroupStatus(List<BusinessGroupStatusEnum> groupStatus) {
		this.groupStatus = groupStatus;
	}

	public LifecycleSyntheticStatus getLifecycleStatus() {
		return lifecycleStatus;
	}

	public void setLifecycleStatus(LifecycleSyntheticStatus lifecycleStatus) {
		this.lifecycleStatus = lifecycleStatus;
	}

	public Date getLifecycleStatusReference() {
		return lifecycleStatusReference;
	}

	public void setLifecycleStatusReference(Date lifecycleStatusReference) {
		this.lifecycleStatusReference = lifecycleStatusReference;
	}

	public enum LifecycleSyntheticStatus {
		ACTIVE,
		ACTIVE_LONG,
		ACTIVE_RESPONSE_DELAY,
		TO_START_INACTIVATE,
		TO_INACTIVATE,
		
		INACTIVE,
		INACTIVE_LONG,
		INACTIVE_RESPONSE_DELAY,
		TO_START_SOFT_DELETE,
		TO_SOFT_DELETE,
		
		SOFT_DELETE,
		SOFT_DELETE_LONG,
		TO_DELETE
	}
}
