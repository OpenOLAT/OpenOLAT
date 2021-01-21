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
package org.olat.repository.model;

import java.util.Date;

import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 04.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryAuthorImpl implements RepositoryEntryAuthorView {

	private final Long key;
	
	private final Date creationDate;
	
	private final String technicalType;
	private final String displayname;
	private final String description;
	private final String author;
	private final String authors;
	private final String location;
	private final RepositoryEntryEducationalType educationalType;
	
	private final String softkey;
	private final String externalId;
	private final String externalRef;
	private final RepositoryEntryManagedFlag[] managedFlags;
	
	private final RepositoryEntryStatusEnum status;
	private final boolean allUsers;
	private final boolean guests;
	private final boolean bookable;
	
	private final Date lastUsage;
	
	public final int numOfReferences;
	
	private final boolean lectureEnabled;
	private final boolean rollCallEnabled;
	
	private final Date deletionDate;
	private final String deletedByFullName;
	
	private final OLATResource olatResource;
	private final RepositoryEntryLifecycle lifecycle;
	
	private final boolean marked;
	
	private final long offers;
	
	public RepositoryEntryAuthorImpl(RepositoryEntry re, boolean marked, long offers, int numOfReferences, String deletedByFullName,
			boolean lectureEnabled, boolean rollCallEnabled) {
		key = re.getKey();
		creationDate = re.getCreationDate();
		
		technicalType = re.getTechnicalType();
		displayname = re.getDisplayname();
		description = re.getDescription();
		author = re.getInitialAuthor();
		authors = re.getAuthors();
		location = re.getLocation();
		educationalType = re.getEducationalType();
		
		softkey = re.getSoftkey();
		externalId = re.getExternalId();
		externalRef = re.getExternalRef();
		managedFlags = re.getManagedFlags();
		
		status = re.getEntryStatus();
		allUsers = re.isAllUsers();
		guests = re.isGuests();
		bookable = re.isBookable();
		
		lastUsage = re.getStatistics().getLastUsage();
		
		this.numOfReferences = numOfReferences;
		
		this.lectureEnabled = lectureEnabled;
		this.rollCallEnabled = rollCallEnabled;
		
		deletionDate = re.getDeletionDate();
		this.deletedByFullName = deletedByFullName;
		
		olatResource = re.getOlatResource();
		lifecycle = re.getLifecycle();
		this.marked = marked;
		this.offers = offers;
	}

	@Override
	public Long getKey() {
		return key;
	}
	
	@Override
	public String getResourceableTypeName() {
		return OresHelper.calculateTypeName(RepositoryEntry.class);
	}

	@Override
	public String getResourceType() {
		return olatResource.getResourceableTypeName();
	}

	@Override
	public Long getResourceableId() {
		return getKey();
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public String getTechnicalType() {
		return technicalType;
	}

	@Override
	public String getDisplayname() {
		return displayname;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getSoftkey() {
		return softkey;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public String getExternalRef() {
		return externalRef;
	}

	@Override
	public RepositoryEntryManagedFlag[] getManagedFlags() {
		return managedFlags;
	}

	@Override
	public String getAuthor() {
		return author;
	}

	@Override
	public String getAuthors() {
		return authors;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public RepositoryEntryEducationalType getEducationalType() {
		return educationalType;
	}

	@Override
	public RepositoryEntryStatusEnum getEntryStatus() {
		return status;
	}

	@Override
	public boolean isAllUsers() {
		return allUsers;
	}

	@Override
	public boolean isGuests() {
		return guests;
	}

	@Override
	public boolean isBookable() {
		return bookable;
	}

	@Override
	public OLATResource getOlatResource() {
		return olatResource;
	}

	@Override
	public RepositoryEntryLifecycle getLifecycle() {
		return lifecycle;
	}

	@Override
	public int getNumOfReferences() {
		return numOfReferences;
	}

	@Override
	public boolean isLectureEnabled() {
		return lectureEnabled;
	}

	@Override
	public boolean isRollCallEnabled() {
		return rollCallEnabled;
	}

	@Override
	public boolean isMarked() {
		return marked;
	}

	@Override
	public Date getLastUsage() {
		return lastUsage;
	}

	@Override
	public boolean isOfferAvailable() {
		return offers > 0;
	}

	@Override
	public String getDeletedByFullName() {
		return deletedByFullName;
	}

	@Override
	public Date getDeletionDate() {
		return deletionDate;
	}
}