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
import java.util.Set;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResource;

/**
 * This view is based on a CROSS JOIN, the identityKey must be set
 * to search it!!!
 * 
 * 
 * Initial date: 10.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryMyCourseImpl implements RepositoryEntryMyView, CreateInfo, ModifiedInfo {

	private final Long key;
	private final Date creationDate;
	private Date lastModified;
	private final String externalId;
	private final String externalRef;
	private final String displayname;
	private final String description;
	private final String teaser;
	private final String authors;
	private final String location;
	private final RepositoryEntryEducationalType educationalType;
	private final String expenditureOfWork;
	private final RepositoryEntryStatusEnum status;
	private final boolean allUsers;
	private final boolean guests;
	private final boolean bookable;
	
	private final OLATResource olatResource;
	private final RepositoryEntryLifecycle lifecycle;
	
	private Float score;
	private Boolean passed;
	private Double completion;

	private final boolean marked;
	
	private final Integer myRating;
	
	private final Double averageRating;
	private final long numOfRatings;
	private final long numOfComments;
	private final long launchCounter;

	private final long offersAvailable;
	
	private Set<TaxonomyLevel> taxonomyLevels;
	
	public RepositoryEntryMyCourseImpl(RepositoryEntry re, RepositoryEntryStatistics stats,
			boolean marked, long offersAvailable, Integer myRating) {
		key = re.getKey();
		externalId = re.getExternalId();
		externalRef = re.getExternalRef();
		creationDate = re.getCreationDate();
		lastModified = re.getLastModified();
		displayname = re.getDisplayname();
		description = re.getDescription();
		teaser = re.getTeaser();
		authors = re.getAuthors();
		location = re.getLocation();
		educationalType = re.getEducationalType();
		expenditureOfWork = re.getExpenditureOfWork();
		status = re.getEntryStatus();
		allUsers = re.isAllUsers();
		guests = re.isGuests();
		bookable = re.isBookable();
		
		olatResource = re.getOlatResource();
		lifecycle = re.getLifecycle();

		this.marked = marked;
		this.myRating = myRating;

		if(stats != null) {
			averageRating = stats.getRating();
			numOfRatings = stats.getNumOfRatings();
			numOfComments = stats.getNumOfComments();
			launchCounter = stats.getLaunchCounter();
		} else {
			averageRating = null;
			numOfRatings = 0;
			numOfComments = 0;
			launchCounter = 0;
		}
		
		this.offersAvailable = offersAvailable;
	}

	@Override
	public Long getKey() {
		return key;
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
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
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
	public String getResourceableTypeName() {
		return "RepositoryEntry";
	}

	@Override
	public Long getResourceableId() {
		return getKey();
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
	public String getTeaser() {
		return teaser;
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
	public String getExpenditureOfWork() {
		return expenditureOfWork;
	}

	@Override
	public long getLaunchCounter() {
		return launchCounter;
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
	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	@Override
	public Boolean getPassed() {
		return passed;
	}

	public void setPassed(Boolean passed) {
		this.passed = passed;
	}
	
	@Override
	public Double getCompletion() {
		return completion;
	}

	public void setCompletion(Double completion) {
		this.completion = completion;
	}

	@Override
	public boolean isMarked() {
		return marked;
	}

	@Override
	public Integer getMyRating() {
		return myRating;
	}

	@Override
	public Double getAverageRating() {
		return averageRating;
	}

	@Override
	public long getNumOfRatings() {
		return numOfRatings;
	}

	@Override
	public long getNumOfComments() {
		return numOfComments;
	}

	@Override
	public boolean isValidOfferAvailable() {
		return offersAvailable > 0;
	}
	
	@Override
	public Set<TaxonomyLevel> getTaxonomyLevels() {
		return taxonomyLevels;
	}
	
	public void setTaxonomyLevels(Set<TaxonomyLevel> levels) {
		this.taxonomyLevels = levels;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof RepositoryEntryMyCourseImpl) {
			RepositoryEntryMyCourseImpl relc = (RepositoryEntryMyCourseImpl)obj;
			return getKey() != null && getKey().equals(relc.getKey());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 48790 : getKey().hashCode();
	}

	@Override
	public String toString() {
		return super.toString();
	}
}