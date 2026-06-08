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
package org.olat.modules.selectus.ui.position;

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.selectus.RecruitingModule;

/**
 * 
 * Initial date: 18 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditProfileVisibilityHelper {
	
	public List<Visibility> getPersonalDataFields(List<String> excludedAttributesList) {
		List<Visibility> available = new ArrayList<>();
		available.add(new Visibility(RecruitingModule.APP_ID, "edit.application.id.long"));
		available.add(new Visibility(RecruitingModule.APP_PERSON_TITLE, "edit.application.title", null, true));
		available.add(new Visibility(RecruitingModule.APP_PERSON_GENDER, "edit.application.gender"));
		available.add(new Visibility(RecruitingModule.APP_PERSON_FIRSTNAME, "edit.application.firstName", null, true));
		available.add(new Visibility(RecruitingModule.APP_PERSON_LASTNAME, "edit.application.lastName", null, true));
		available.add(new Visibility(RecruitingModule.APP_PERSON_MARITAL_STATUS, "edit.application.marital.status"));
		available.add(new Visibility(RecruitingModule.APP_PERSON_DISABILITY, "edit.application.disability"));
		
		available.add(new Visibility(RecruitingModule.APP_PERSON_ACADEMIC_TITLE, "edit.application.academicTitle"));
		available.add(new Visibility(RecruitingModule.APP_PERSON_BIRTHDAY, "edit.application.birthday"));
		available.add(new Visibility(RecruitingModule.APP_PERSON_MOBILE_PHONE, "edit.application.mobile.phone"));
		available.add(new Visibility(RecruitingModule.APP_PERSON_PHONE, "edit.application.phone"));
		available.add(new Visibility(RecruitingModule.APP_PERSON_EMAIL, "edit.application.mail"));
		available.add(new Visibility(RecruitingModule.APP_PERSON_NATIONALITY, "edit.application.nationality"));
		available.add(new Visibility(RecruitingModule.APP_PERSON_ADD_NATIONALITIES, "edit.application.additional.nationalities"));
		
		if(!excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION)) {
			available.add(new Visibility(RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION, "edit.application.organization"));
		}
		if(!excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_UNIT)) {
			available.add(new Visibility(RecruitingModule.APP_BUSINESS_INFOS_UNIT, "edit.application.unit"));
		}
		if(!excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_POSITION)) {
			available.add(new Visibility(RecruitingModule.APP_BUSINESS_INFOS_POSITION, "edit.application.currentPosition"));
		}

		available.add(new Visibility(RecruitingModule.APP_ADDRESS_BUSINESS_LINE1, "edit.application.addressLine1", "edit.application.prefix.business", false));
		available.add(new Visibility(RecruitingModule.APP_ADDRESS_BUSINESS_LINE2, "edit.application.addressLine2", "edit.application.prefix.business", false));
		available.add(new Visibility(RecruitingModule.APP_ADDRESS_BUSINESS_LINE3, "edit.application.addressLine3", "edit.application.prefix.business", false));
		available.add(new Visibility(RecruitingModule.APP_ADDRESS_BUSINESS_CODE, "edit.application.zipcode", "edit.application.prefix.business", false));
		available.add(new Visibility(RecruitingModule.APP_ADDRESS_BUSINESS_CITY, "edit.application.city", "edit.application.prefix.business", false));
		available.add(new Visibility(RecruitingModule.APP_ADDRESS_BUSINESS_COUNTRY, "edit.application.country", "edit.application.prefix.business", false));
		
		available.add(new Visibility(RecruitingModule.APP_ADDRESS_BUSINESS_PHONE, "edit.application.business.phone", "edit.application.prefix.business", false));
		available.add(new Visibility(RecruitingModule.APP_ADDRESS_BUSINESS_MAIL, "edit.application.business.mail", "edit.application.prefix.business", false));
		
		available.add(new Visibility(RecruitingModule.APP_ADDRESS_PRIVATE_LINE1, "edit.application.addressLine1"));
		available.add(new Visibility(RecruitingModule.APP_ADDRESS_PRIVATE_LINE2, "edit.application.addressLine2"));
		available.add(new Visibility(RecruitingModule.APP_ADDRESS_PRIVATE_LINE3, "edit.application.addressLine3"));
		available.add(new Visibility(RecruitingModule.APP_ADDRESS_PRIVATE_CODE, "edit.application.zipcode"));
		available.add(new Visibility(RecruitingModule.APP_ADDRESS_PRIVATE_CITY, "edit.application.city"));
		available.add(new Visibility(RecruitingModule.APP_ADDRESS_PRIVATE_COUNTRY, "edit.application.country"));

		return available;
	}
	
	public List<Visibility> getAcademicalBackgroundFields(List<String> excludedAttributesList) {
		List<Visibility> available = new ArrayList<>();
	
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_NUM_OF_ORIGINAL_PUBLICATIONS, "edit.application.numberOfOriginalPublications"));
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_NUM_OF_FIRST_AUTHORSHIPS, "edit.application.numberOfFirstAuthorships"));
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_NUM_OF_LAST_AUTHORSHIPS, "edit.application.numberOfLastAuthorships"));
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_CITATIONS, "edit.application.citations"));
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_IMPACT_FACTOR, "edit.application.impactFactor"));
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_HFACTORY, "edit.application.hFactor"));

		if(!excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)) {
			if(!excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_TYPE)
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR)
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_INSTITUTION)) {
				available.add(new Visibility(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE, "edit.application.highestdegree"));
			} else {
				available.add(new Visibility(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE, "standard.attributes.highestdegreetypeyear.part"));
			}
		}
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_WORKED_IN_ACADEMIA_SINCE, "edit.application.workedInAcademiaSince.label"));
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_WORKED_OUT_ACADEMIA_SINCE, "edit.application.workedOutAcademiaSince.label"));
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_WORKED_OUT_ACADEMIA_CARE_SINCE, "edit.application.workedOutAcademiaCareSince.label"));
		
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_CAREER_DESCRIPTION, "edit.application.careerDescription.label"));
		if(!excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION)) {
			available.add(new Visibility(RecruitingModule.APP_ACADEMIC_DISSERTATION, "edit.application.dissertation"));
		}
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD1, "edit.application.dissertationkeyword1"));
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD2, "edit.application.dissertationkeyword2"));
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD3, "edit.application.dissertationkeyword3"));
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_HABILITATION, "edit.application.habilitation"));
		available.add(new Visibility(RecruitingModule.APP_ACADEMIC_ORCID, "edit.application.orcid"));
		
		return available;
	}
	
	public List<Visibility> getProjectFields() {
		List<Visibility> available = new ArrayList<>();
		available.add(new Visibility(RecruitingModule.APP_PROJECT_TITLE, "edit.application.project.title"));
		available.add(new Visibility(RecruitingModule.APP_PROJECT_DESCRIPTION, "edit.application.project.description"));
		available.add(new Visibility(RecruitingModule.APP_PROJECT_DURATION, "edit.application.project.duration"));
		available.add(new Visibility(RecruitingModule.APP_PROJECT_START_DATE, "edit.application.project.start.date"));

		available.add(new Visibility(RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_1, "edit.application.project.financialimpact.unit.1"));
		available.add(new Visibility(RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_2, "edit.application.project.financialimpact.unit.2"));
		available.add(new Visibility(RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_3, "edit.application.project.financialimpact.unit.3"));
		available.add(new Visibility(RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_4, "edit.application.project.financialimpact.unit.4"));
		available.add(new Visibility(RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_5, "edit.application.project.financialimpact.unit.5"));
		available.add(new Visibility(RecruitingModule.APP_PROJECT_ACRONYM, "edit.application.project.acronym"));
		available.add(new Visibility(RecruitingModule.APP_PROJECT_KEYWORDS, "edit.application.project.keywords"));
		available.add(new Visibility(RecruitingModule.APP_PROJECT_DISCIPLINES, "edit.application.project.disciplines"));

		return available;
	}
	
	public static class Visibility {
		
		private final String field;
		private final String i18nKey;
		private final String wrapperI18nKey;
		private final boolean always;
		
		public Visibility(String field, String i18nKey) {
			this(field, i18nKey, null, false);
		}
		
		public Visibility(String field, String i18nKey, String wrapperI18nKey, boolean always) {
			this.field = field;
			this.i18nKey = i18nKey;
			this.wrapperI18nKey = wrapperI18nKey;
			this.always = always;
		}
		
		public String field() {
			return field;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
		
		public String wrapperI18nKey() {
			return wrapperI18nKey;
		}
		
		public boolean always() {
			return always;
		}
	}
}
