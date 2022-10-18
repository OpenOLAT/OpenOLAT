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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementRow implements CurriculumElementRef, FlexiTreeTableNode {
	
	private boolean hasChildren;
	private CurriculumElementRow parent;
	private final Long parentKey;
	private final CurriculumElement element;
	private final CurriculumElementType elementType;
	private final long numOfResources;
	private final long numOfParticipants;
	private final long numOfCoaches;
	private final long numOfOwners;
	
	private final FormLink toolsLink;
	private final FormLink resourcesLink;
	private FormLink lecturesLink;
	private FormLink calendarsLink;
	private FormLink learningProgressLink;
	
	private boolean acceptedByFilter = true;
	
	public CurriculumElementRow(CurriculumElement element) {
		this.element = element;
		elementType = element.getType();
		parentKey = element.getParent() == null ? null : element.getParent().getKey();
		numOfResources = 0l;
		numOfParticipants = 0l;
		numOfCoaches = 0l;
		numOfOwners = 0l;
		toolsLink = null;
		resourcesLink = null;
	}
	
	public CurriculumElementRow(CurriculumElement element, long numOfResources,
			long numOfParticipants, long numOfCoaches, long numOfOwners,
			FormLink toolsLink, FormLink resourcesLink) {
		this.element = element;
		this.toolsLink = toolsLink;
		this.numOfResources = numOfResources;
		this.numOfParticipants = numOfParticipants;
		this.numOfCoaches = numOfCoaches;
		this.numOfOwners = numOfOwners;
		this.resourcesLink = resourcesLink;
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
	
	public String getIdentifier() {
		return element.getIdentifier();
	}
	
	public String getDisplayName() {
		return element.getDisplayName();
	}
	
	public String getExternalId() {
		return element.getExternalId();
	}
	
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
	
	public String getCurriculumElementTypeDisplayName() {
		return elementType == null ? null : elementType.getDisplayName();
	}
	
	public CurriculumElementType getCurriculumElementType() {
		return elementType;
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

	public Long getParentKey() {
		return parentKey;
	}
	
	public Integer getPos() {
		return element.getPos();
	}
	
	public Integer getPosCurriculum() {
		return element.getPosCurriculum();
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
	
	public long getNumOfMembers() {
		return numOfOwners + numOfCoaches + numOfParticipants;
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

	public FormLink getLearningProgressLink() {
		return learningProgressLink;
	}

	public void setLearningProgressLink(FormLink learningProgressLink) {
		this.learningProgressLink = learningProgressLink;
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
		if(obj instanceof CurriculumElementRow) {
			CurriculumElementRow row = (CurriculumElementRow)obj;
			return element != null && element.equals(row.element);
		}
		return false;
	}
}
