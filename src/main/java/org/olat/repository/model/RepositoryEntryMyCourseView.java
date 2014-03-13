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

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * This view is based on a CROSS JOIN, the identityKey must be set
 * to search it!!!
 * 
 * 
 * Initial date: 10.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Cacheable(false)
@Entity(name="repositoryentrymy")
@Table(name="o_repositoryentry_my_v")
public class RepositoryEntryMyCourseView implements RepositoryEntryMyView, Persistable, CreateInfo, ModifiedInfo {

	private static final long serialVersionUID = -8484159601386853047L;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "hilo")
	@Column(name="re_id", nullable=false, unique=true, insertable=false, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="re_creationdate", nullable=false, insertable=false, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="re_lastmodified", nullable=false, insertable=false, updatable=false)
	private Date lastModified;

	@Column(name="re_displayname", nullable=false, insertable=false, updatable=false)
	private String displayname;
	@Column(name="re_description", nullable=false, insertable=false, updatable=false)
	private String description;
	@Column(name="re_authors", nullable=false, insertable=false, updatable=false)
	private String authors;
	@Column(name="re_membersonly", nullable=false, insertable=false, updatable=false)
	private boolean membersOnly;
	@Column(name="re_accesscode", nullable=false, insertable=false, updatable=false)
	private int access;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_olatresource", nullable=false, insertable=false, updatable=false)
	private OLATResource olatResource;

	@ManyToOne(targetEntity=RepositoryEntryLifecycle.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_lifecycle", nullable=true, insertable=false, updatable=false)
	private RepositoryEntryLifecycle lifecycle;
	
	@Column(name="eff_score", nullable=true, insertable=false, updatable=false)
	private Float score;
	@Column(name="eff_passed", nullable=true, insertable=false, updatable=false)
	private Boolean passed;

	@Column(name="mark_id", nullable=true, insertable=false, updatable=false)
	private Long markKey;
	
	@Column(name="rat_rating", nullable=true, insertable=false, updatable=false)
	private Integer myRating;
	@Column(name="stats_rating", nullable=true, insertable=false, updatable=false)
	private Float averageRating;
	@Column(name="stats_num_of_ratings", nullable=true, insertable=false, updatable=false)
	private long numOfRatings;
	@Column(name="stats_num_of_comments", nullable=true, insertable=false, updatable=false)
	private long numOfComments;
	
	@Column(name="ci_initiallaunchdate", nullable=true, insertable=false, updatable=false)
	private Date initialLaunch;
	@Column(name="ci_recentlaunchdate", nullable=true, insertable=false, updatable=false)
	private Date recentLaunch;
	@Column(name="ci_visit", nullable=true, insertable=false, updatable=false)
	private Integer visit;
	@Column(name="ci_timespend", nullable=true, insertable=false, updatable=false)
	private Long timeSpend;

	@Column(name="num_of_valid_offers", nullable=true, insertable=false, updatable=false)
	private long offersAvailable;
	@Column(name="num_of_offers", nullable=true, insertable=false, updatable=false)
	private long offers;
	
	@Column(name="member_id", nullable=true, insertable=false, updatable=false)
	private Long identityKey;
	

	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
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

	public String getAuthosr() {
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
		return markKey != null;
	}

	@Override
	public Date getInitialLaunch() {
		return initialLaunch;
	}

	public void setInitialLaunch(Date initialLaunch) {
		this.initialLaunch = initialLaunch;
	}

	@Override
	public Date getRecentLaunch() {
		return recentLaunch;
	}

	public void setRecentLaunch(Date recentLaunch) {
		this.recentLaunch = recentLaunch;
	}

	@Override
	public Integer getVisit() {
		return visit;
	}

	public void setVisit(Integer visit) {
		this.visit = visit;
	}

	@Override
	public Long getTimeSpend() {
		return timeSpend;
	}

	public void setTimeSpend(Long timeSpend) {
		this.timeSpend = timeSpend;
	}

	public Integer getMyRating() {
		return myRating;
	}

	public void setMyRating(Integer myRating) {
		this.myRating = myRating;
	}

	public Float getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(Float averageRating) {
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
	public boolean isOfferAvailable() {
		return offers > 0;
	}
	

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof RepositoryEntryMyCourseView) {
			RepositoryEntryMyCourseView relc = (RepositoryEntryMyCourseView)obj;
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
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}