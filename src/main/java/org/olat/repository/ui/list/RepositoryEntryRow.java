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
package org.olat.repository.ui.list;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatus;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.PriceMethod;

/**
 * 
 * Initial date: 29.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryRow implements RepositoryEntryRef {
	private boolean marked;
	private boolean selected;
	
	private Long key;
	private String externalId;
	private String externalRef;
	private Date creationDate;
	private String name;
	private String authors;
	private String location;
	private String expenditureOfWork;
	private String thumbnailRelPath;
	private String shortenedDescription;
	private int access;
	private int statusCode;
	
	private String score;
	private Boolean passed;
	private boolean isMembersOnly = false;
	
	private boolean member;
	
	private Integer myRating;
	private Double averageRating;
	private long numOfRatings;
	private long numOfComments;
	private long launchCounter;

	private String lifecycleLabel;
	private String lifecycleSoftKey;
	private Date lifecycleStart;
	private Date lifecycleEnd;
	
	private List<PriceMethod> accessTypes;
	
	private FormLink markLink;
	private FormLink selectLink;
	private FormLink startLink;
	private FormLink detailsLink;
	private FormLink commentsLink;
	
	private OLATResourceable olatResource;
	private FormItem ratingFormItem;
	
	public RepositoryEntryRow() {
		//
	}
	
	public RepositoryEntryRow(RepositoryEntryMyView entry) {
		setKey(entry.getKey());
		setCreationDate(entry.getCreationDate());
		setExternalId(entry.getExternalId());
		setExternalRef(entry.getExternalRef());
		setDisplayName(entry.getDisplayname());
		setShortenedDescription(entry.getDescription());
		setOLATResourceable(OresHelper.clone(entry.getOlatResource()));
		setAuthors(entry.getAuthors());
		setLocation(entry.getLocation());
		setExpenditureOfWork(entry.getExpenditureOfWork());
		setLaunchCounter(entry.getLaunchCounter());
		setIsMembersOnly(entry.isMembersOnly());
		setAccess(entry.getAccess());
		setStatusCode(entry.getStatusCode());
		
		//bookmark
		setMarked(entry.isMarked());
		
		//efficiency statement
		setPassed(entry.getPassed());
		setScore(AssessmentHelper.getRoundedScore(entry.getScore()));
		
		//rating
		setMyRating(entry.getMyRating());
		setAverageRating(entry.getAverageRating());
		setNumOfRatings(entry.getNumOfRatings());
		setNumOfComments(entry.getNumOfComments());
		
		//lifecycle
		RepositoryEntryLifecycle reLifecycle = entry.getLifecycle();
		if(reLifecycle != null) {
			setLifecycleStart(reLifecycle.getValidFrom());
			setLifecycleEnd(reLifecycle.getValidTo());
			if(!reLifecycle.isPrivateCycle()) {
				setLifecycleLabel(reLifecycle.getLabel());
				setLifecycleSoftKey(reLifecycle.getSoftKey());
			}
		}
	}
	
	private void setIsMembersOnly(boolean membersOnly) {
		this.isMembersOnly = membersOnly;
	}

	public boolean isMembersOnly() {
		return isMembersOnly;
	}
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public boolean isClosed() {
		return new RepositoryEntryStatus(statusCode).isClosed();
	}

	public int getAccess() {
		return access;
	}

	public void setAccess(int access) {
		this.access = access;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getExternalRef() {
		return externalRef;
	}
	
	public void setExternalRef(String externalRef) {
		this.externalRef = externalRef;
	}

	public String getDisplayName() {
		return name;
	}
	
	public void setDisplayName(String name) {
		this.name = name;
	}

	public String getShortenedDescription() {
		if(shortenedDescription != null) {
			String shortDesc = FilterFactory.getHtmlTagsFilter().filter(shortenedDescription);
			if(shortDesc.length() > 255) {
				shortDesc = shortDesc.substring(0, 255);
			}
			return shortDesc;
		}
		return shortenedDescription;
	}

	public void setShortenedDescription(String description) {
		if(description != null) {
			String shortDesc = FilterFactory.getHtmlTagsFilter().filter(description);
			if(shortDesc.length() > 255) {
				shortenedDescription = shortDesc.substring(0, 255);
			} else {
				shortenedDescription = shortDesc;
			}
		} else {
			shortenedDescription = "";
		}
	}

	/**
	 * Is member if the row as some type of access control
	 * @return
	 */
	public boolean isMember() {
		return member;
	}

	public void setMember(boolean member) {
		this.member = member;
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

	public String getLifecycleSoftKey() {
		return lifecycleSoftKey;
	}

	public void setLifecycleSoftKey(String lifecycleSoftKey) {
		this.lifecycleSoftKey = lifecycleSoftKey;
	}

	public String getLifecycleLabel() {
		return lifecycleLabel;
	}

	public void setLifecycleLabel(String lifecycleLabel) {
		this.lifecycleLabel = lifecycleLabel;
	}

	public Date getLifecycleStart() {
		return lifecycleStart;
	}

	public void setLifecycleStart(Date lifecycleStart) {
		this.lifecycleStart = lifecycleStart;
	}

	public Date getLifecycleEnd() {
		return lifecycleEnd;
	}

	public void setLifecycleEnd(Date lifecycleEnd) {
		this.lifecycleEnd = lifecycleEnd;
	}
	
	public boolean isActive() {
		boolean isCurrent = true; 
		if (lifecycleEnd != null || lifecycleStart != null) {
			Date now = new Date();
			if (lifecycleStart != null && lifecycleStart.after(now)) {
				isCurrent = false;
			} else if (lifecycleEnd != null && lifecycleEnd.before(now)) {
				isCurrent = false;
			}
		}
		return isCurrent;
	}
	
	public List<PriceMethod> getAccessTypes() {
		return accessTypes;
	}

	public void setAccessTypes(List<PriceMethod> accessTypes) {
		this.accessTypes = accessTypes;
	}

	public String getSelectLinkName() {
		return selectLink.getComponent().getComponentName();
	}
	
	public FormLink getSelectLink() {
		return selectLink;
	}
	
	public void setSelectLink(FormLink selectLink) {
		this.selectLink = selectLink;
	}
	
	public String getStartLinkName() {
		return startLink == null ? null :startLink.getComponent().getComponentName();
	}
	
	public FormLink getStartLink() {
		return startLink;
	}

	public void setStartLink(FormLink startLink) {
		this.startLink = startLink;
	}
	
	public String getDetailsLinkName() {
		return detailsLink.getComponent().getComponentName();
	}

	public FormLink getDetailsLink() {
		return detailsLink;
	}

	public void setDetailsLink(FormLink detailsLink) {
		this.detailsLink = detailsLink;
	}

	public FormLink getMarkLink() {
		return markLink;
	}
	
	public String getMarkLinkName() {
		if(markLink != null) {
			return markLink.getComponent().getComponentName();
		}
		return null;
	}
	
	public void setMarkLink(FormLink markLink) {
		this.markLink = markLink;
	}
	
	public String getRatingFormItemName() {
		return ratingFormItem == null ? null : ratingFormItem.getComponent().getComponentName();
	}

	public FormItem getRatingFormItem() {
		return ratingFormItem;
	}

	public void setRatingFormItem(FormItem ratingFormItem) {
		this.ratingFormItem = ratingFormItem;
	}
	
	public FormLink getCommentsLink() {
		return commentsLink;
	}
	
	public String getCommentsLinkName() {
		return commentsLink == null ? null : commentsLink.getComponent().getComponentName();
	}

	public void setCommentsLink(FormLink commentsLink) {
		this.commentsLink = commentsLink;
	}

	public OLATResourceable getRepositoryEntryResourceable() {
		return OresHelper.createOLATResourceableInstance("RepositoryEntry", getKey());
	}
	
	/**
	 * This is a clone of the repositoryEntry.getOLATResource();
	 * @return
	 */
	public OLATResourceable getOLATResourceable() {
		return olatResource;
	}
	
	public void setOLATResourceable(OLATResourceable olatResource) {
		this.olatResource = olatResource;
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

	public String getExpenditureOfWork() {
		return expenditureOfWork;
	}

	public void setExpenditureOfWork(String expenditureOfWork) {
		this.expenditureOfWork = expenditureOfWork;
	}

	public long getLaunchCounter() {
		return launchCounter;
	}

	public void setLaunchCounter(long launchCounter) {
		this.launchCounter = launchCounter;
	}

	public String getThumbnailRelPath() {
		return thumbnailRelPath;
	}
	
	public void setThumbnailRelPath(String path) {
		this.thumbnailRelPath = path;
	}
	
	public boolean isThumbnailAvailable() {
		return StringHelper.containsNonWhitespace(thumbnailRelPath);
	}
	
	public boolean isMarked() {
		return marked;
	}
	
	public void setMarked(boolean marked) {
		this.marked = marked;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public String getScore() {
		return score;
	}
	
	public void setScore(String score) {
		this.score = score;
	}
	
	public boolean isPassed() {
		return passed != null && passed.booleanValue();
	}
	
	public void setPassed(Boolean passed) {
		this.passed = passed;
	}
	
	public boolean isFailed() {
		return passed != null && !passed.booleanValue();
	}
}