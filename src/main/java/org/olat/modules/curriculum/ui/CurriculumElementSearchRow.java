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
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;

/**
 * 
 * Initial date: 10 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementSearchRow implements CurriculumElementRef {
	
	private final long numOfResources;
	private final long numOfParticipants;
	private final long numOfCoaches;
	private final long numOfOwners;
	private final long numOfCurriculumElementOwners;
	private final long numOfMasterCoaches;
	
	private final Curriculum curriculum;
	private final CurriculumElement element;
	private final CurriculumElementType elementType;
	
	private final FormLink toolsLink;
	private final FormLink resourcesLink;
	private final FormLink structureLink;
	
	public CurriculumElementSearchRow(CurriculumElement element, long numOfResources,
			long numOfParticipants, long numOfCoaches, long numOfOwners,
			long numOfCurriculumElementOwners, long numOfMasterCoaches,
			FormLink resourcesLink, FormLink structureLink, FormLink toolsLink) {
		this.element = element;
		curriculum = element.getCurriculum();
		elementType = element.getType();
		this.numOfResources = numOfResources;
		this.numOfParticipants = numOfParticipants;
		this.numOfCoaches = numOfCoaches;
		this.numOfOwners = numOfOwners;
		this.numOfCurriculumElementOwners = numOfCurriculumElementOwners;
		this.numOfMasterCoaches = numOfMasterCoaches;
		this.toolsLink = toolsLink;
		this.structureLink = structureLink;
		this.resourcesLink = resourcesLink;
	}
	
	@Override
	public Long getKey() {
		return element.getKey();
	}
	
	public CurriculumElement getCurriculumElement() {
		return element;
	}
	
	public Curriculum getCurriculum() {
		return curriculum;
	}
	
	public Long getCurriculumKey() {
		return curriculum.getKey();
	}
	
	public String getCurriculumName() {
		if(StringHelper.containsNonWhitespace(element.getCurriculum().getIdentifier())) {
			return curriculum.getIdentifier();
		}
		return curriculum.getDisplayName();
	}
	
	public String getDisplayName() {
		return element.getDisplayName();
	}
	
	public String getIdentifier() {
		return element.getIdentifier();
	}
	
	public String getExternalID() {
		return element.getExternalId();
	}
	
	public Date getBeginDate() {
		return element.getBeginDate();
	}
	
	public Date getEndDate() {
		return element.getEndDate();
	}
	
	public String getCurriculumElementTypeDisplayName() {
		return elementType == null ? null : elementType.getDisplayName();
	}
	
	public CurriculumElementStatus getStatus() {
		CurriculumElementStatus status = element.getElementStatus();
		return status == null ? CurriculumElementStatus.active : status;
	}
	
	public long getNumOfRessources() {
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
		return numOfOwners + numOfCoaches + numOfParticipants + numOfCurriculumElementOwners + numOfMasterCoaches;
	}
	
	public FormLink getToolsLink() {
		return toolsLink;
	}

	public FormLink getResourcesLink() {
		return resourcesLink;
	}
	
	public FormLink getStructureLink() {
		return structureLink;
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
		if(obj instanceof CurriculumElementSearchRow row) {
			return element.equals(row.element);
		}
		return false;
	}

}
