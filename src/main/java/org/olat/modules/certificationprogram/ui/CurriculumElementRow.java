/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;

/**
 * 
 * Initial date: 29 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementRow implements CurriculumElementRef {

	private final long numOfResources;
	private final long numOfParticipants;
	private final long numOfPassedParticipants;
	private final Curriculum curriculum;
	private final CurriculumElement curriculumElement;
	private final CurriculumElementType curriculumElementType;
	
	private FormLink resourcesLink;
	private CertificationProgramCurriculumElementDetailsController detailsCtrl;
	
	public CurriculumElementRow(CurriculumElement curriculumElement, Curriculum curriculum,
			long numOfParticipants, long numOfPassedParticipants, long numOfResources) {
		this.numOfResources = numOfResources;
		this.numOfParticipants = numOfParticipants;
		this.numOfPassedParticipants = numOfPassedParticipants;
		this.curriculumElement = curriculumElement;
		curriculumElementType = curriculumElement.getType();
		this.curriculum = curriculum;
	}
	
	@Override
	public Long getKey() {
		return curriculumElement.getKey();
	}
	
	public String getDisplayName() {
		return curriculumElement.getDisplayName();
	}
	
	public String getIdentifier() {
		return curriculumElement.getIdentifier();
	}
	
	public String getExternalId() {
		return curriculumElement.getExternalId();
	}
	
	public CurriculumElementStatus getElementStatus() {
		return curriculumElement.getElementStatus();
	}
	
	public Date getBeginDate() {
		return curriculumElement.getBeginDate();
	}
	
	public Date getEndDate() {
		return curriculumElement.getEndDate();
	}
	
	public Long getCurriculumKey() {
		return curriculum.getKey();
	}
	
	public String getCurriculumDisplayName() {
		return curriculum.getDisplayName();
	}
	
	public String getCurriculumElementTypeDisplayName() {
		return curriculumElementType == null ? null : curriculumElementType.getDisplayName();
	}
	
	public long getNumOfResources() {
		return numOfResources;
	}
	
	public long getNumOfParticipants() {
		return numOfParticipants;
	}
	
	public long getNumOfPassedParticipants() {
		return numOfPassedParticipants;
	}
	
	public FormLink getResources() {
		return resourcesLink;
	}
	
	public void setResources(FormLink link) {
		this.resourcesLink = link;
	}
	
	public boolean isDetailsControllerAvailable() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().isVisible();
		}
		return false;
	}

	public CertificationProgramCurriculumElementDetailsController getDetailsController() {
		return detailsCtrl;
	}
	
	public String getDetailsControllerName() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().getComponent().getComponentName();
		}
		return null;
	}
	
	public void setDetailsController(CertificationProgramCurriculumElementDetailsController detailsCtrl) {
		this.detailsCtrl = detailsCtrl;
	}

}
