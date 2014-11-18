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

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
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

	private Long key;
	private Date creationDate;
	private Date lastModified;
	private String externalId;
	private String externalRef;
	private String displayname;
	private String description;
	private String authors;
	private boolean membersOnly;
	private int access;
	
	private OLATResource olatResource;
	private RepositoryEntryLifecycle lifecycle;
	
	private Float score;
	private Boolean passed;

	private boolean marked;
	
	private Integer myRating;
	
	private Double averageRating;
	private long numOfRatings;
	private long numOfComments;
	private Integer visit;

	private long offersAvailable;
	
	public RepositoryEntryMyCourseImpl(RepositoryEntry re, RepositoryEntryStatistics stats,
			boolean marked, long offersAvailable, Integer myRating) {
		key = re.getKey();
		externalId = re.getExternalId();
		externalRef = re.getExternalRef();
		creationDate = re.getCreationDate();
		lastModified = re.getLastModified();
		displayname = re.getDisplayname();
		description = re.getDescription();
		authors = re.getAuthors();
		membersOnly = re.isMembersOnly();
		access = re.getAccess();
		
		olatResource = re.getOlatResource();
		lifecycle = re.getLifecycle();
	

		this.marked = marked;
		this.myRating = myRating;

		if(stats != null) {
			averageRating = stats.getRating();
			numOfRatings = stats.getNumOfRatings();
			numOfComments = stats.getNumOfComments();
		}
		
		this.offersAvailable = offersAvailable;
	}
	
	public void setEfficiencyStatement(UserEfficiencyStatementLight efficiencyStatment) {
		if(efficiencyStatment != null) {
			score = efficiencyStatment.getScore();
			passed = efficiencyStatment.getPassed();
		}
	}
	
	public void setCourseInfos(UserCourseInformations courseInfos) {
		if(courseInfos != null) {
			visit = courseInfos.getVisit();
		}
	}

	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String getExternalRef() {
		return externalRef;
	}
	
	public void setExternalRef(String externalRef) {
		this.externalRef = externalRef;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
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
	public boolean isMembersOnly() {
		return membersOnly;
	}

	public void setMembersOnly(boolean membersOnly) {
		this.membersOnly = membersOnly;
	}

	@Override
	public int getAccess() {
		return access;
	}

	public void setAccess(int access) {
		this.access = access;
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

	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	@Override
	public OLATResource getOlatResource() {
		return olatResource;
	}

	public void setOlatResource(OLATResource olatResource) {
		this.olatResource = olatResource;
	}

	@Override
	public RepositoryEntryLifecycle getLifecycle() {
		return lifecycle;
	}

	public void setLifecycle(RepositoryEntryLifecycle lifecycle) {
		this.lifecycle = lifecycle;
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
	public boolean isMarked() {
		return marked;
	}

	@Override
	public Integer getVisit() {
		return visit;
	}

	public void setVisit(Integer visit) {
		this.visit = visit;
	}

	public Integer getMyRating() {
		return myRating;
	}

	public void setMyRating(Integer myRating) {
		this.myRating = myRating;
	}

	public Double getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(Double averageRating) {
		this.averageRating = averageRating;
	}

	public long getNumOfRatings() {
		return numOfRatings;
	}

	public void setNumOfRatings(long numOfRatings) {
		this.numOfRatings = numOfRatings;
	}

	public long getNumOfComments() {
		return numOfComments;
	}

	public void setNumOfComments(long numOfComments) {
		this.numOfComments = numOfComments;
	}

	@Override
	public boolean isValidOfferAvailable() {
		return offersAvailable > 0;
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