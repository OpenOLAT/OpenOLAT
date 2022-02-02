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
package org.olat.modules.coach.ui.curriculum.certificate;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementRow;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumTreeWithViewsRow implements CurriculumTreeWithView, FlexiTreeTableNode {
	
	private boolean hasChildren;
	private CurriculumTreeWithViewsRow parent;

	private CurriculumKey parentKey;
	private CurriculumKey key;

	private final Curriculum curriculum;
	private final CurriculumElement element;
	private final CurriculumElementType elementType;
	private final CurriculumElementMembership curriculumMembership;
	private boolean curriculumMember;
	private int level;

	private boolean singleEntry;
	private int elementEntryCount;
	private OLATResource olatResource;
	private RepositoryEntryMyView repositoryEntry;
	
	private String shortenedDescription;
	private String representationalName;
	
	private RepositoryEntryStatusEnum status;
	private boolean guests;
	private boolean allUsers;
	private boolean bookable;
	private List<PriceMethod> accessTypes;

	private boolean member;
	private String thumbnailRelPath;
	private boolean marked;

	private String startUrl;
	private String detailsUrl;

	private FormLink startLink;
	private FormLink detailsLink;
	private FormLink markLink;
	private FormLink selectLink;
	private FormLink calendarsLink;
	private ProgressBarItem completionItem;

	private EfficiencyStatementEntry efficiencyStatementEntry;
	private CertificateAndEfficiencyStatementRow certificateAndEfficiencyStatement;
	private boolean hasStatement;
	
	public CurriculumTreeWithViewsRow(Curriculum curriculum, CurriculumElement element, CurriculumElementMembership curriculumMembership, int myEntryCount) {
		this.element = element;
		elementType = element.getType();
		this.curriculumMembership = curriculumMembership;
		curriculumMember = curriculumMembership != null && curriculumMembership.hasMembership();
		singleEntry = false;
		elementEntryCount = myEntryCount;
		this.curriculum = curriculum;
		setKey();
		setParentKey();
		setShortenedDescription(element.getDescription());
	
		// calculate level of current curr element based on parent chain
		for(CurriculumElement parentEl=element.getParent(); parentEl != null; parentEl=parentEl.getParent()) {
			level++;
		}
	}

	public CurriculumTreeWithViewsRow(Curriculum curriculum) {
		this.curriculum = curriculum;
		parentKey = null;
		element = null;
		elementType = null;
		curriculumMembership = null;
		curriculumMember = true;
		level = -1;
		setKey();
	}

	/**
	 * Used to show the head parent of elements without any curriculum
	 * @param representationalName
	 */
	public CurriculumTreeWithViewsRow(String representationalName) {
		this.representationalName = representationalName;
		curriculum = null;
		parentKey = null;
		element = null;
		elementType = null;
		curriculumMembership = null;
		curriculumMember = false;
		level = -1;
		setKey();
	}

	/**
	 * Used to show elements without any curriculum
	 * @param repositoryEntryView
	 */
	public CurriculumTreeWithViewsRow(RepositoryEntryMyView repositoryEntryView) {
		guests = repositoryEntryView.isGuests();
		allUsers = repositoryEntryView.isAllUsers();
		bookable = repositoryEntryView.isBookable();
		status = repositoryEntryView.getEntryStatus();
		repositoryEntry = repositoryEntryView;
		olatResource = repositoryEntryView.getOlatResource();
		marked = repositoryEntryView.isMarked();
		setShortenedDescription(repositoryEntryView.getDescription());

		curriculum = null;
		parentKey = null;
		element = null;
		elementType = null;
		curriculumMembership = null;
		curriculumMember = false;
		level = 0;
		setKey(true);
	}

	public CurriculumTreeWithViewsRow(CertificateAndEfficiencyStatementRow statement) {
		this.certificateAndEfficiencyStatement = statement;

		setShortenedDescription(statement.getDisplayName());
		setKey(true);

		curriculum = null;
		parentKey = null;
		element = null;
		elementType = null;
		curriculumMembership = null;
		curriculumMember = false;
		level = 0;
	}

	public CurriculumTreeWithViewsRow(Curriculum curriculum, CurriculumElement element, CurriculumElementMembership curriculumMembership,
			RepositoryEntryMyView repositoryEntryView, boolean alone) {
		this.element = element;
		elementType = element == null ? null : element.getType();
		this.curriculumMembership = curriculumMembership;
		curriculumMember = curriculumMembership != null && curriculumMembership.hasMembership();
		singleEntry = alone;
		elementEntryCount = 0;

		Long parentElementKey;
		Long parentCurriculumKey = curriculum != null ? curriculum.getKey() : null;

		if(alone) {
			parentElementKey = element != null && element.getParent() != null ? element.getParent().getKey() : null;
		} else {
			parentElementKey = element == null ? null : element.getKey();
			// add ourself as level
			level++;
		}
		parentKey = new CurriculumKey(parentCurriculumKey, parentElementKey);
		setKey();
		
		guests = repositoryEntryView.isGuests();
		allUsers = repositoryEntryView.isAllUsers();
		bookable = repositoryEntryView.isBookable();
		status = repositoryEntryView.getEntryStatus();
		repositoryEntry = repositoryEntryView;
		olatResource = repositoryEntryView.getOlatResource();
		marked = repositoryEntryView.isMarked();
		setShortenedDescription(repositoryEntryView.getDescription());
		
		// calculate level of current curr element based on parent chain
		for(CurriculumElement parentEl=element.getParent(); parentEl != null; parentEl=parentEl.getParent()) {
			level++;
		}

		this.curriculum = curriculum;
	}

	@Override
	public CurriculumKey getKey() {
		return this.key;
	}
	
	public boolean isActive() {
		if(element != null) {
			return element.getElementStatus() == null
					|| element.getElementStatus() == CurriculumElementStatus.active;
		}
		return true;
	}

	public Curriculum getCurriculum() {
		return curriculum;
	}
	
	public String getDisplayName() {
		if(repositoryEntry != null) {
			return repositoryEntry.getDisplayname();
		} else if(element != null) {
			return element.getDisplayName();
		} else if (curriculum != null) {
			return curriculum.getDisplayName();
		} else if (certificateAndEfficiencyStatement != null) {
			return certificateAndEfficiencyStatement.getDisplayName();
		} else if (representationalName != null) {
			return representationalName;
		}
		return null;
	}

	public boolean isCurriculum() {
		return curriculum != null && element == null && repositoryEntry == null && parentKey == null;
	}
	
	public boolean isCurriculumElementOnly() {
		return element != null && repositoryEntry == null;
	}
	
	public boolean isRepositoryEntryOnly() {
		return element != null && repositoryEntry != null && !singleEntry;
	}
	
	public boolean isCurriculumElementWithEntry() {
		return element != null && repositoryEntry != null && singleEntry;
	}
	
	public String getCurriculumElementIdentifier() {
		if ((repositoryEntry == null || singleEntry) && element != null) {
			return element.getIdentifier();
		} else if (isCurriculum()) {
			return curriculum.getIdentifier();
		} else {
			return null;
		}
	}
	
	public Long getId() {
		if ((repositoryEntry == null || singleEntry) && element != null) {
			return element.getKey();
		}
		if (isCurriculum()) {
			return curriculum.getKey();
		}
		if (repositoryEntry != null) {
			return repositoryEntry.getKey();
		}
		if (element != null) {
			return element.getKey();
		}
		return null;
	}
	
	public String getCurriculumElementExternalId() {
		return element == null ? null : element.getExternalId();
	}
	
	public CurriculumElementStatus getCurriculumElementStatus() {
		return element == null ? null : element.getElementStatus();
	}
	
	public String getCurriculumElementDisplayName() {
		if ((representationalName == null || singleEntry) && element != null) {
			return element.getDisplayName();
		} else if (isCurriculum()) {
			return curriculum.getDisplayName();
		} else if (repositoryEntry != null) {
			return repositoryEntry.getDisplayname();
		} else if (representationalName != null) {
			return representationalName;
		} else {
			return null;
		}
	}

	public Long getCurriculumKey() {
		return curriculum == null ? null : curriculum.getKey();
	}

	public Long getCurriculumElementKey() {
		return element == null ? null : element.getKey();
	}
	
	public Date getCurriculumElementBeginDate() {
		return element == null ? null : element.getBeginDate();
	}
	
	public Date getCurriculumElementEndDate() {
		return element == null ? null : element.getEndDate();
	}
	
	public Long getCurriculumElementPos() {
		return element == null ? null : element.getPos();
	}
	
	public String getCurriculumElementTypeCssClass() {
		return elementType == null ? null : element.getType().getCssClass();
	}

	public String getCurriculumElementTypeName() {
		return elementType == null ? null : element.getType().getDisplayName();
	}
	
	public boolean isCalendarsEnabled() {
		boolean enabled = false;
		if(element != null) {
			if(element.getCalendars() == CurriculumCalendars.enabled) {
				enabled = true;
			} else if(element.getCalendars() == CurriculumCalendars.inherited && elementType != null) {
				enabled = elementType.getCalendars() == CurriculumCalendars.enabled;
			}
		}
		return enabled;
	}
	
	public boolean isLearningProgressEnabled() {
		boolean enabled = false;
		if(element != null) {
			if(element.getLearningProgress() == CurriculumLearningProgress.enabled) {
				enabled = true;
			} else if(element.getLearningProgress() == CurriculumLearningProgress.inherited && elementType != null) {
				enabled = elementType.getLearningProgress() == CurriculumLearningProgress.enabled;
			}
		}
		return enabled;
	}
	
	public int getCurriculumElementRepositoryEntryCount() {
		return elementEntryCount;
	}
	
	public int getLevel() {
		return level;
	}

	
	public String getExternalId() {
		String extId = null;
		if (element != null && element.getExternalId() != null) {
			extId = element.getExternalId();
		}
		if (repositoryEntry != null && repositoryEntry.getExternalId() != null) {
			if (extId != null) {
				extId += " ";
				extId += repositoryEntry.getExternalId();
			} else {
				extId += repositoryEntry.getExternalId();				
			}
		}
		return extId;
	}
	
	public String getMaterializedPathKeys() {
		return element.getMaterializedPathKeys();
	}
	
	public String getShortenedDescription() {
		return shortenedDescription;
	}

	private void setShortenedDescription(String description) {
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

	private void setParentKey() {
		Long curriculumKey = element != null && curriculum != null ? curriculum.getKey() : null;
		Long curriculumElementParentKey = element != null && element.getParent() != null ? element.getParent().getKey() : null;

		parentKey = new CurriculumKey(curriculumKey, curriculumElementParentKey);
	}

	private void setKey() {
		Long curriculumKey = curriculum != null ? curriculum.getKey() : null;
		Long curriculumElementKey = element != null ? element.getKey() : null;

		key = new CurriculumKey(curriculumKey, curriculumElementKey);
	}

	private void setKey(boolean isWithoutCurriculum) {
		key = new CurriculumKey(null, null, isWithoutCurriculum);
	}
	
	public boolean isClosed() {
		return status.decommissioned();
	}

	public boolean isSingleEntry() {
		return singleEntry;
	}
	
	public boolean isMarked() {
		return marked;
	}
	
	public void setMarked(boolean marked) {
		this.marked = marked;
	}
	
	public RepositoryEntryStatusEnum getEntryStatus() {
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
	
	public boolean isThumbnailAvailable() {
		return StringHelper.containsNonWhitespace(thumbnailRelPath);
	}
	
	public String getThumbnailRelPath() {
		return thumbnailRelPath;
	}

	public void setThumbnailRelPath(String thumbnailRelPath) {
		this.thumbnailRelPath = thumbnailRelPath;
	}
	
	public Long getRepositoryEntryKey() {
		return repositoryEntry == null ? null : repositoryEntry.getKey();
	}
	
	public String getRepositoryEntryDisplayName() {
		if (repositoryEntry != null) {
			return repositoryEntry.getDisplayname();
		} else if (certificateAndEfficiencyStatement != null) {
			return certificateAndEfficiencyStatement.getDisplayName();
		} else {
			return null;
		}
	}
	
	public String getRepositoryEntryExternalRef() {
		return repositoryEntry == null ? null : repositoryEntry.getExternalRef();
	}
	
	public String getRepositoryEntryCssClass() {
		return olatResource == null ? "" : RepositoyUIFactory.getIconCssClass(olatResource.getResourceableTypeName());
	}
	
	public String getRepositoryEntryAuthors() {
		return repositoryEntry == null ? null : repositoryEntry.getAuthors();
	}
	
	public String getRepositoryEntryLocation() {
		return repositoryEntry == null ? null : repositoryEntry.getLocation();
	}
	
	public String getRepositoryEntryShortenedDescription() {
		return repositoryEntry == null ? null : repositoryEntry.getDescription();
	}
	
	public Date getLifecycleStart() {
		return repositoryEntry == null || repositoryEntry.getLifecycle() == null
				? null : repositoryEntry.getLifecycle().getValidFrom();
	}
	
	public Date getLifecycleEnd() {
		return repositoryEntry == null || repositoryEntry.getLifecycle() == null
				? null : repositoryEntry.getLifecycle().getValidTo();
	}
	
	public String getLifecycleSoftKey() {
		return repositoryEntry == null || repositoryEntry.getLifecycle() == null || repositoryEntry.getLifecycle().isPrivateCycle()
				? null : repositoryEntry.getLifecycle().getSoftKey();
	}
	
	public String getLifecycleLabel() {
		return repositoryEntry == null || repositoryEntry.getLifecycle() == null || repositoryEntry.getLifecycle().isPrivateCycle()
				? null : repositoryEntry.getLifecycle().getLabel();
	}
	
	public String getScore() {
		return repositoryEntry == null ? null : AssessmentHelper.getRoundedScore(repositoryEntry.getScore());
	}
	
	public boolean isPassed() {
		return repositoryEntry == null || repositoryEntry.getPassed() == null
				? false : repositoryEntry.getPassed().booleanValue();
	}
	
	public boolean isFailed() {
		return repositoryEntry == null || repositoryEntry.getPassed() == null
				? false : !repositoryEntry.getPassed().booleanValue();
	}
	
	public Double getRepositoryEntryCompletion() {
		return repositoryEntry != null? repositoryEntry.getCompletion(): null;
	}
	
	public OLATResourceable getRepositoryEntryResourceable() {
		return repositoryEntry;
	}
	
	public OLATResource getOlatResource() {
		return olatResource;
	}
	
	@Override
	public List<RepositoryEntryMyView> getEntries() {
		if(repositoryEntry == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(repositoryEntry);
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

	public CurriculumElementMembership getCurriculumMembership() {
		return curriculumMembership;
	}

	@Override
	public boolean isCurriculumMember() {
		return curriculumMember;
	}

	public void setCurriculumMember(boolean curriculumMember) {
		this.curriculumMember = curriculumMember;
	}

	public List<PriceMethod> getAccessTypes() {
		return accessTypes;
	}

	public void setAccessTypes(List<PriceMethod> accessTypes) {
		this.accessTypes = accessTypes;
	}
	
	public boolean isClosedOrInactive() {
		if(isCurriculumElementOnly()) {
			return element.getElementStatus() == CurriculumElementStatus.inactive || element.getElementStatus() == CurriculumElementStatus.deleted;
		}
		if(isRepositoryEntryOnly()) {
			return status != null && status.decommissioned();
		}
		return (status != null && status.decommissioned())
				|| (element != null &&  element.getElementStatus() != null
				&& (element.getElementStatus() == CurriculumElementStatus.inactive || element.getElementStatus() == CurriculumElementStatus.deleted));
	}

	@Override
	public CurriculumTreeWithViewsRow getParent() {
		return parent;
	}
	
	public void setParent(CurriculumTreeWithViewsRow parent) {
		this.parent = parent;
		if(parent != null) {
			parent.hasChildren = true;
		}
	}
	
	public boolean hasChildren() {
		return hasChildren;
	}

	public CurriculumKey getParentKey() {
		return parentKey;
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
	
	public String getStartLinkName() {
		return startLink == null ? null : startLink.getComponent().getComponentName();
	}
	
	public FormLink getStartLink() {
		return startLink;
	}
	
	public void setStartLink(FormLink startLink, String startURL) {
		this.startLink = startLink;
		this.startUrl = startURL;
	}

	public String getStartUrl() {
		return startUrl;
	}
	
	public String getDetailsLinkName() {
		return detailsLink == null ? null : detailsLink.getComponent().getComponentName();
	}

	public FormLink getDetailsLink() {
		return detailsLink;
	}

	public void setDetailsLink(FormLink detailsLink, String detailsURL) {
		this.detailsLink = detailsLink;
		this.detailsUrl = detailsURL;
	}

	public String getDetailsUrl() {
		return detailsUrl;
	}
	
	public String getSelectLinkName() {
		return selectLink == null ? null : selectLink.getComponent().getComponentName();
	}
	
	public FormLink getSelectLink() {
		return selectLink;
	}
	
	public void setSelectLink(FormLink selectLink) {
		this.selectLink = selectLink;
	}
	
	public FormLink getCalendarsLink() {
		return calendarsLink;
	}

	public void setCalendarsLink(FormLink calendarsLink) {
		this.calendarsLink = calendarsLink;
	}
	
	public String getCalendarsLinkName() {
		return calendarsLink == null ? null : calendarsLink.getComponent().getComponentName();
	}
	
	public ProgressBarItem getCompletionItem() {
		return completionItem;
	}
	
	public void setCompletionItem(ProgressBarItem completionItem) {
		this.completionItem = completionItem;
	}
	
	public String getCompletionItemName() {
		return completionItem == null ? null : completionItem.getComponent().getComponentName();
	}

	public EfficiencyStatementEntry getEfficiencyStatementEntry() {
		return this.efficiencyStatementEntry;
	}

	public void setEfficiencyStatementEntry(EfficiencyStatementEntry efficiencyStatementEntry) {
		this.efficiencyStatementEntry = efficiencyStatementEntry;
	}

	public boolean hasStatement() {
		return hasStatement;
	}

	public void setHasStatement(boolean hasStatement) {
		if (parent != null) {
			parent.setHasStatement(true);
		}
		this.hasStatement = hasStatement;
	}

	@Override
	public String getCrump() {
		return element.getDisplayName();
	}

	@Override
	public int hashCode() {
		return (element == null ? 73465971 : element.getKey().hashCode())
				+ (repositoryEntry == null ?-3726247 : repositoryEntry.getKey().hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumTreeWithViewsRow) {
			CurriculumTreeWithViewsRow row = (CurriculumTreeWithViewsRow)obj;
			return ((element == null && row.element == null && curriculum!= null && row.curriculum != null && curriculum.getKey().equals(row.curriculum.getKey()))
							|| (element != null && row.element != null && element.getKey().equals(row.element.getKey())))
					&& ((repositoryEntry == null && row.repositoryEntry == null)
							|| (repositoryEntry != null && row.repositoryEntry != null && repositoryEntry.getKey().equals(row.repositoryEntry.getKey())));
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(isCurriculumElementOnly()) {
			sb.append("element[key").append(element.getKey()).append(":identifier=").append(element.getIdentifier()).append("]");
		} else if(isRepositoryEntryOnly()) {
			sb.append("repositoryEntry[key").append(repositoryEntry.getKey()).append(":identifier=").append(repositoryEntry.getExternalRef()).append("]");
		} else if(isCurriculumElementWithEntry()) {
			sb.append("composite[key").append(element.getKey()).append(":identifier=").append(element.getIdentifier()).append("]")
			  .append("entry[key").append(repositoryEntry.getKey()).append(":identifier=").append(repositoryEntry.getExternalRef()).append("]");
		} else if(isCurriculum()) {
			sb.append("curriculum[key").append(curriculum.getKey()).append(":identifier=").append(curriculum.getIdentifier()).append("]");
		}
		
		return sb.toString();
	}

}
