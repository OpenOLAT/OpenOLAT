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
package org.olat.course.assessment;

import org.olat.core.id.OrganisationRef;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 15 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ScoreAccountingTriggerSearchParams {
	
	private BusinessGroupRef businessGroupRef;
	private OrganisationRef organisationRef;
	private CurriculumElementRef curriculumElementRef;
	private String userPropertyName;
	private String userPropertyValue;
	
	public BusinessGroupRef getBusinessGroupRef() {
		return businessGroupRef;
	}

	public void setBusinessGroupRef(BusinessGroupRef businessGroupRef) {
		this.businessGroupRef = businessGroupRef;
	}

	public OrganisationRef getOrganisationRef() {
		return organisationRef;
	}

	public void setOrganisationRef(OrganisationRef organisationRef) {
		this.organisationRef = organisationRef;
	}

	public CurriculumElementRef getCurriculumElementRef() {
		return curriculumElementRef;
	}

	public void setCurriculumElementRef(CurriculumElementRef curriculumElementRef) {
		this.curriculumElementRef = curriculumElementRef;
	}

	public String getUserPropertyName() {
		return userPropertyName;
	}

	public void setUserPropertyName(String userPropertyName) {
		this.userPropertyName = userPropertyName;
	}

	public String getUserPropertyValue() {
		return userPropertyValue;
	}

	public void setUserPropertyValue(String userPropertyValue) {
		this.userPropertyValue = userPropertyValue;
	}

}
