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
package org.olat.modules.curriculum.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.site.ComparableCurriculumElementRow;
import org.olat.modules.curriculum.ui.component.MinMaxParticipants;
import org.olat.repository.ui.PriceMethod;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementRow implements ComparableCurriculumElementRow {
	
	private boolean hasChildren;
	private CurriculumElementRow parent;
	private final Long parentKey;
	private final Curriculum curriculum;
	private CurriculumElement element;
	private CurriculumElementType elementType;
	private final long numOfResources;
	private final long numOfParticipants;
	private final long numOfCoaches;
	private final long numOfOwners;
	private final long numOfCurriculumElementOwners;
	private final long numOfMasterCoaches;
	private final long numOfPending;
	private MinMaxParticipants minMaxParticipants;
	private List<PriceMethod> accessPriceMethods;
	
	private final FormLink toolsLink;
	private final FormLink resourcesLink;
	private final FormLink structureLink;
	private FormLink lecturesLink;
	private FormLink calendarsLink;
	private FormLink qualityPreviewLink;
	private FormLink learningProgressLink;
	
	private String baseUrl;
	private String curriculumUrl;
	
	private boolean active = false;
	private boolean acceptedByFilter = true;
	
	public CurriculumElementRow(CurriculumElement element) {
		this.element = element;
		curriculum = element.getCurriculum();
		elementType = element.getType();
		parentKey = element.getParent() == null ? null : element.getParent().getKey();
		numOfResources = 0l;
		numOfParticipants = 0l;
		numOfCoaches = 0l;
		numOfOwners = 0l;
		numOfCurriculumElementOwners = 0l;
		numOfMasterCoaches = 0l;
		numOfPending = 0l;
		minMaxParticipants = new MinMaxParticipants(element.getMinParticipants(), element.getMaxParticipants());
		toolsLink = null;
		resourcesLink = null;
		structureLink = null;
	}
	
	public CurriculumElementRow(CurriculumElement element, long numOfResources,
			long numOfParticipants, long numOfCoaches, long numOfOwners,
			long numOfCurriculumElementOwners, long numOfMasterCoaches, long numOfPending,
			FormLink toolsLink, FormLink resourcesLink, FormLink structureLink) {
		this.element = element;
		curriculum = element.getCurriculum();
		this.toolsLink = toolsLink;
		this.numOfResources = numOfResources;
		this.numOfParticipants = numOfParticipants;
		this.numOfCoaches = numOfCoaches;
		this.numOfOwners = numOfOwners;
		this.numOfCurriculumElementOwners = numOfCurriculumElementOwners;
		this.numOfMasterCoaches = numOfMasterCoaches;
		this.numOfPending = numOfPending;
		minMaxParticipants = new MinMaxParticipants(element.getMinParticipants(), element.getMaxParticipants());
		this.resourcesLink = resourcesLink;
		this.structureLink = structureLink;
		elementType = element.getType();
		parentKey = element.getParent() == null ? null : element.getParent().getKey();
	}
	
	@Override
	public Long getKey() {
		return element.getKey();
	}
	
	public CurriculumElement getCurriculumElement() {
		return element;
	}
	
	public void setCurriculumElement(CurriculumElement element) {
		this.element = element;
		minMaxParticipants = new MinMaxParticipants(element.getMinParticipants(), element.getMaxParticipants());
	}
	
	public Long getCurriculumKey() {
		return curriculum == null ? null : curriculum.getKey();
	}
	
	public String getCurriculumDisplayName() {
		return curriculum == null ? null : curriculum.getDisplayName();
	}
	
	public String getCurriculumExternalRef() {
		return curriculum == null ? null : curriculum.getIdentifier();
	}

	@Override
	public String getIdentifier() {
		return element.getIdentifier();
	}

	@Override
	public String getDisplayName() {
		return element.getDisplayName();
	}
	
	public String getExternalId() {
		return element.getExternalId();
	}

	@Override
	public Date getBeginDate() {
		return element.getBeginDate();
	}
	
	public Date getEndDate() {
		return element.getEndDate();
	}
	
	public CurriculumElementStatus getStatus() {
		CurriculumElementStatus status = element.getElementStatus();
		return status == null ? CurriculumElementStatus.active : status;
	}
	
	public Long getCurriculumElementTypeKey() {
		return elementType == null ? null : elementType.getKey();
	}
	
	public String getCurriculumElementTypeDisplayName() {
		return elementType == null ? null : elementType.getDisplayName();
	}
	
	public CurriculumElementType getCurriculumElementType() {
		return elementType;
	}
	
	public void setCurriculumElementType(CurriculumElementType elementType) {
		this.elementType = elementType;
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
	
	public boolean isLecturesEnabled() {
		return CurriculumLectures.isEnabled(element, elementType);
	}
	
	public boolean isLearningProgressEnabled() {
		return CurriculumLearningProgress.isEnabled(element, elementType);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isAcceptedByFilter() {
		return acceptedByFilter;
	}

	public void setAcceptedByFilter(boolean acceptedByFilter) {
		this.acceptedByFilter = acceptedByFilter;
	}

	@Override
	public CurriculumElementRow getParent() {
		return parent;
	}
	
	public void setParent(CurriculumElementRow parent) {
		this.parent = parent;
		if(parent != null) {
			parent.hasChildren = true;
		}
	}
	
	public boolean hasChildren() {
		return hasChildren;
	}

	@Override
	public Long getParentKey() {
		return parentKey;
	}

	@Override
	public Integer getPos() {
		return element.getPos();
	}

	@Override
	public Integer getPosCurriculum() {
		return element.getPosCurriculum();
	}
	
	public String getNumber() {
		return element.getNumberImpl();
	}

	public long getNumOfResources() {
		return numOfResources;
	}

	public long getNumOfParticipants() {
		return numOfParticipants;
	}

	public long getNumOfCoaches() {
		return numOfCoaches;
	}

	public long getNumOfOwners() {
		return numOfOwners;
	}
	
	public long getNumOfCurriculumElementOwners() {
		return numOfCurriculumElementOwners;
	}

	public long getNumOfMasterCoaches() {
		return numOfMasterCoaches;
	}
	
	public long getNumOfMembers() {
		return numOfOwners + numOfCoaches + numOfParticipants
				+ numOfMasterCoaches + numOfCurriculumElementOwners;
	}
	
	public long getNumOfPending() {
		return numOfPending;
	}
	
	public MinMaxParticipants getMinMaxParticipants() {
		return minMaxParticipants;
	}

	public List<PriceMethod> getAccessPriceMethods() {
		return accessPriceMethods;
	}

	public void setAccessPriceMethods(List<PriceMethod> accessPriceMethods) {
		this.accessPriceMethods = accessPriceMethods;
	}

	@Override
	public String getCrump() {
		return element.getDisplayName();
	}

	public FormLink getTools() {
		return toolsLink;
	}
	
	public FormLink getResources() {
		return resourcesLink;
	}
	
	public FormLink getStructureLink() {
		return structureLink;
	}

	public FormLink getCalendarsLink() {
		return calendarsLink;
	}

	public void setCalendarsLink(FormLink calendarsLink) {
		this.calendarsLink = calendarsLink;
	}

	public FormLink getLecturesLink() {
		return lecturesLink;
	}

	public void setLecturesLink(FormLink lecturesLink) {
		this.lecturesLink = lecturesLink;
	}

	public FormLink getQualityPreviewLink() {
		return qualityPreviewLink;
	}

	public void setQualityPreviewLink(FormLink qualityPreviewLink) {
		this.qualityPreviewLink = qualityPreviewLink;
	}

	public FormLink getLearningProgressLink() {
		return learningProgressLink;
	}

	public void setLearningProgressLink(FormLink learningProgressLink) {
		this.learningProgressLink = learningProgressLink;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getCurriculumUrl() {
		return curriculumUrl;
	}

	public void setCurriculumUrl(String curriculumUrl) {
		this.curriculumUrl = curriculumUrl;
	}

	@Override
	public int hashCode() {
		return element.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumElementRow row) {
			return element != null && element.equals(row.element);
		}
		return false;
	}
}
