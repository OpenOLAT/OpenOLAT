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
package org.olat.modules.coach.ui.curriculum.course;

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
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementWithView;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseCurriculumTreeWithViewsRow implements CurriculumElementWithView, FlexiTreeTableNode {
	
	private boolean hasChildren;
	private CourseCurriculumTreeWithViewsRow parent;

	private Long parentKey;

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


	private boolean member;

	private String startUrl;
	private String detailsUrl;

	private FormLink calendarsLink;
	private ProgressBarItem completionItem;

	private EfficiencyStatementEntry efficiencyStatementEntry;
	
	public CourseCurriculumTreeWithViewsRow(CurriculumElement element, CurriculumElementMembership curriculumMembership, int myEntryCount) {
		this.element = element;
		elementType = element.getType();
		parentKey = element.getParent() == null ? null : element.getParent().getKey();
		this.curriculumMembership = curriculumMembership;
		curriculumMember = curriculumMembership != null && curriculumMembership.hasMembership();
		singleEntry = false;
		elementEntryCount = myEntryCount;
		setShortenedDescription(element.getDescription());
	
		// calculate level of current curr element based on parent chain
		for(CurriculumElement parentEl=element.getParent(); parentEl != null; parentEl=parentEl.getParent()) {
			level++;
		}
	}

	/**
	 * Used to show the head parent of elements without any curriculum
	 * @param representationalName
	 */
	public CourseCurriculumTreeWithViewsRow(String representationalName) {
		this.representationalName = representationalName;
		parentKey = null;
		element = null;
		elementType = null;
		curriculumMembership = null;
		curriculumMember = false;
		level = -1;
	}

	/**
	 * Used to show elements without any curriculum
	 * @param repositoryEntryView
	 */
	public CourseCurriculumTreeWithViewsRow(RepositoryEntryMyView repositoryEntryView) {
		status = repositoryEntryView.getEntryStatus();
		repositoryEntry = repositoryEntryView;
		olatResource = repositoryEntryView.getOlatResource();
		setShortenedDescription(repositoryEntryView.getDescription());

		parentKey = null;
		element = null;
		elementType = null;
		curriculumMembership = null;
		curriculumMember = false;
		level = 0;
	}

	public CourseCurriculumTreeWithViewsRow(CurriculumElement element, CurriculumElementMembership curriculumMembership,
				RepositoryEntryMyView repositoryEntryView, boolean alone) {
		this.element = element;
		elementType = element == null ? null : element.getType();
		this.curriculumMembership = curriculumMembership;
		curriculumMember = curriculumMembership != null && curriculumMembership.hasMembership();
		singleEntry = alone;
		elementEntryCount = 0;
		
		if(alone) {
			parentKey = element.getParent() == null ? null : element.getParent().getKey();
		} else {
			parentKey = element.getKey();
			// add ourself as level
			level++;
		}
		
		repositoryEntry = repositoryEntryView;
		olatResource = repositoryEntryView.getOlatResource();
		setShortenedDescription(repositoryEntryView.getDescription());
		
		// calculate level of current curr element based on parent chain
		for(CurriculumElement parentEl=element.getParent(); parentEl != null; parentEl=parentEl.getParent()) {
			level++;
		}
	}

	@Override
	public Long getKey() {
		return element == null ? null : element.getKey();
	}
	
	public boolean isActive() {
		if(element != null) {
			return element.getElementStatus() == null
					|| element.getElementStatus() == CurriculumElementStatus.active;
		}
		return true;
	}
	
	public String getDisplayName() {
		if(repositoryEntry != null) {
			return repositoryEntry.getDisplayname();
		} else if(element != null) {
			return element.getDisplayName();
		}
		return null;
	}
	
	public boolean isRepresentationOnly() {
		return StringHelper.containsNonWhitespace(representationalName)
				&& element == null && repositoryEntry == null;
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
		return repositoryEntry == null || singleEntry ? element != null ? element.getIdentifier() : null : null;
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
		} else if (repositoryEntry != null) {
			return repositoryEntry.getDisplayname();
		} else if (representationalName != null) {
			return representationalName;
		} else {
			return null;
		}
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
	
	public Integer getCurriculumElementPos() {
		return element == null ? null : element.getPos();
	}
	
	public String getCurriculumElementTypeCssClass() {
		return elementType == null ? null : element.getType().getCssClass();
	}

	public String getCurriculumElementTypeName() {
		return elementType == null ? null : element.getType().getDisplayName();
	}
	
	public Long getCurriculumElementParentKey() {
		return element == null || element.getParent() == null ? null : element.getParent().getKey();
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
	
	public boolean isClosed() {
		return status != null && status.decommissioned();
	}

	public boolean isSingleEntry() {
		return singleEntry;
	}
	
	public RepositoryEntryStatusEnum getEntryStatus() {
		return status;
	}
	
	public Long getRepositoryEntryKey() {
		return repositoryEntry == null ? null : repositoryEntry.getKey();
	}
	
	public String getRepositoryEntryDisplayName() {
		return repositoryEntry == null ? null : repositoryEntry.getDisplayname();
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
	
	public boolean isClosedOrInactive() {
		if(isCurriculumElementOnly()) {
			return element.getElementStatus() != null && element.getElementStatus().isClosed();
		}
		if(isRepositoryEntryOnly()) {
			return status != null && status.decommissioned();
		}
		return (status != null && status.decommissioned())
				|| (element != null &&  element.getElementStatus() != null
				&& (element.getElementStatus() != null && element.getElementStatus().isClosed()));
	}

	@Override
	public CourseCurriculumTreeWithViewsRow getParent() {
		return parent;
	}
	
	public void setParent(CourseCurriculumTreeWithViewsRow parent) {
		this.parent = parent;
		if(parent != null) {
			parent.hasChildren = true;
		}
	}
	
	public boolean hasChildren() {
		return hasChildren;
	}

	public Long getParentKey() {
		return parentKey;
	}

	public String getStartUrl() {
		return startUrl;
	}

	public String getDetailsUrl() {
		return detailsUrl;
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
		if(obj instanceof CourseCurriculumTreeWithViewsRow) {
			CourseCurriculumTreeWithViewsRow row = (CourseCurriculumTreeWithViewsRow)obj;
			return ((element == null && row.element == null)
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
		}
		
		return sb.toString();
	}

}
