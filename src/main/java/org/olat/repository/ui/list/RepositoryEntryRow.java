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
import java.util.Set;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoyUIFactory;

/**
 * 
 * Initial date: 29.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryRow implements RepositoryEntryRef {
	private boolean marked;
	private boolean selected;
	
	private final Long key;
	private final String externalId;
	private final String externalRef;
	private final Date creationDate;
	private final String name;
	private final String authors;
	private final String location;
	private final RepositoryEntryEducationalType educationalType;
	private final String expenditureOfWork;
	private String thumbnailRelPath;
	private final String shortenedDescription;
	private final RepositoryEntryStatusEnum status;
	private final boolean allUsers;
	private final boolean guests;
	private final boolean bookable;
	
	private final String score;
	private final Boolean passed;
	private final Double completion;
	private ProgressBarItem completionItem;
	
	private boolean member;
	
	private final Integer myRating;
	private final Double averageRating;
	private final long numOfRatings;
	private final long numOfComments;
	private final long launchCounter;

	private String lifecycleLabel;
	private String lifecycleSoftKey;
	private Date lifecycleStart;
	private Date lifecycleEnd;
	
	private List<PriceMethod> accessTypes;
	private Set<TaxonomyLevel> taxonomyLevels;
	
	private FormLink markLink;
	private FormLink selectLink;
	private FormLink startLink;
	private FormLink detailsLink;
	private FormLink commentsLink;
	
	private OLATResourceable olatResource;
	private FormItem ratingFormItem;
	
	public RepositoryEntryRow(RepositoryEntryMyView entry) {
		key = entry.getKey();
		creationDate = entry.getCreationDate();
		externalId = entry.getExternalId();
		externalRef = entry.getExternalRef();
		name = entry.getDisplayname();
		shortenedDescription = StringHelper.truncateText(entry.getDescription());
		setOLATResourceable(OresHelper.clone(entry.getOlatResource()));
		authors = entry.getAuthors();
		location = entry.getLocation();
		educationalType = entry.getEducationalType();
		expenditureOfWork = entry.getExpenditureOfWork();
		launchCounter = entry.getLaunchCounter();
		status = entry.getEntryStatus();
		allUsers = entry.isAllUsers();
		guests = entry.isGuests();
		bookable = entry.isBookable();
		taxonomyLevels = entry.getTaxonomyLevels();
		
		//bookmark
		setMarked(entry.isMarked());
		
		//efficiency statement
		passed = entry.getPassed();
		score = AssessmentHelper.getRoundedScore(entry.getScore());
		
		// Assessment entry
		completion = entry.getCompletion();
		
		//rating
		myRating = entry.getMyRating();
		averageRating = entry.getAverageRating();
		numOfRatings = entry.getNumOfRatings();
		numOfComments = entry.getNumOfComments();
		
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
	
	@Override
	public Long getKey() {
		return key;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public boolean isClosed() {
		return status.decommissioned();
	}

	public RepositoryEntryStatusEnum getStatus() {
		return status;
	}
	
	public boolean isAllUsers() {
		return allUsers;
	}
	
	public boolean isGuests() {
		return guests;
	}
	
	public boolean isBookable() {
		return bookable;
	}

	public String getExternalId() {
		return externalId;
	}

	public String getExternalRef() {
		return externalRef;
	}

	public String getDisplayName() {
		return name;
	}

	public String getShortenedDescription() {
		return shortenedDescription;
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

	public Double getAverageRating() {
		return averageRating;
	}

	public long getNumOfRatings() {
		return numOfRatings;
	}

	public long getNumOfComments() {
		return numOfComments;
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

	public String getCompletionItemName() {
		return completionItem != null? completionItem.getComponent().getComponentName(): null;
	}

	public ProgressBarItem getCompletionItem() {
		return completionItem;
	}

	public void setCompletionItem(ProgressBarItem completionItem) {
		this.completionItem = completionItem;
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
	
	public String getLocation() {
		return location;
	}

	public RepositoryEntryEducationalType getEducationalType() {
		return educationalType;
	}

	public String getEducationalTypei18nKey() {
		return RepositoyUIFactory.getI18nKey(educationalType);
	}

	public String getExpenditureOfWork() {
		return expenditureOfWork;
	}

	public long getLaunchCounter() {
		return launchCounter;
	}

	public String getThumbnailRelPath() {
		return thumbnailRelPath;
	}
	
	public Set<TaxonomyLevel> getTaxonomyLevels() {
		return taxonomyLevels;
	}
	
	public boolean isThumbnailAvailable() {
		return StringHelper.containsNonWhitespace(thumbnailRelPath);
	}
	
	public void setThumbnailRelPath(String thumbnailRelPath) {
		this.thumbnailRelPath = thumbnailRelPath;
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
	
	public boolean isPassed() {
		return passed != null && passed.booleanValue();
	}
	
	public boolean isFailed() {
		return passed != null && !passed.booleanValue();
	}

	public Double getCompletion() {
		return completion;
	}

	@Override
	public int hashCode() {
		return key == null ? 16174545 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof RepositoryEntryRow) {
			RepositoryEntryRow row = (RepositoryEntryRow)obj;
			return key != null && key.equals(row.getKey());
		}
		return false;
	}
}