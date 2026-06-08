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
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.AddressOption;
import org.olat.modules.selectus.ApplicationFieldType;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.ui.model.StandardAttributeRow;

/**
 * 
 * Initial date: 23 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StandardAttributesDelegate {
	
	public static List<StandardAttributeRow> getRows(PositionApplicationAttributeTabEnum tab,
			List<String> excludedAttributesList, Translator translator) {
		if(tab == PositionApplicationAttributeTabEnum.personalData) {
			return getPersonalRows(excludedAttributesList, translator);
		}
		if(tab == PositionApplicationAttributeTabEnum.academicalBackground) {
			return getAcademicalBackgroundRows(excludedAttributesList, translator);
		}
		if(tab == PositionApplicationAttributeTabEnum.project) {
			return getProjectRows(translator);
		}
		return Collections.emptyList();
	}
	
	public static List<StandardAttributeRow> getPersonalRows(List<String> excludedAttributesList, Translator translator) {
		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		List<StandardAttributeRow> rows = new ArrayList<>();
		
		{
			String type = translator.translate("standard.attributes.personal.data");
			List<String> fieldsLabels = new ArrayList<>();
			List<String> fieldsKeys = new ArrayList<>();
			
			fieldsLabels.add(translator.translate("edit.application.title"));
			fieldsKeys.add(RecruitingModule.APP_ID);
			fieldsKeys.add(RecruitingModule.APP_PERSON_TITLE);
			
			if(recruitingModule.isApplicationPersonGenderEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.gender"));
				fieldsKeys.add(RecruitingModule.APP_PERSON_GENDER);
			}

			fieldsLabels.add(translator.translate("edit.application.firstName"));
			fieldsKeys.add(RecruitingModule.APP_PERSON_FIRSTNAME);
			fieldsLabels.add(translator.translate("edit.application.lastName"));
			fieldsKeys.add(RecruitingModule.APP_PERSON_LASTNAME);

			if(recruitingModule.isApplicationPersonMaritalStatusEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.marital.status"));
				fieldsKeys.add(RecruitingModule.APP_PERSON_MARITAL_STATUS);
			}
			
			if(recruitingModule.isApplicationPersonBirthdayEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.birthday"));
				fieldsKeys.add(RecruitingModule.APP_PERSON_BIRTHDAY);
			}

			if(recruitingModule.isApplicationPersonNationalityEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.nationality"));
				fieldsKeys.add(RecruitingModule.APP_PERSON_NATIONALITY);
			}
			
			if(recruitingModule.isApplicationPersonAdditionalNationalitiesEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.additional.nationalities"));
				fieldsKeys.add(RecruitingModule.APP_PERSON_ADD_NATIONALITIES);
			}

			if(recruitingModule.isApplicationPersonAcademicTitleEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.academicTitle"));
				fieldsKeys.add(RecruitingModule.APP_PERSON_ACADEMIC_TITLE);
			}

			if(recruitingModule.isApplicationPersonPhoneEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.phone"));
				fieldsKeys.add(RecruitingModule.APP_PERSON_PHONE);
			}
			
			if(recruitingModule.isApplicationPersonMobilePhoneEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.mobile.phone"));
				fieldsKeys.add(RecruitingModule.APP_PERSON_MOBILE_PHONE);
			}
			
			fieldsLabels.add(translator.translate("edit.application.mail"));
			fieldsKeys.add(RecruitingModule.APP_PERSON_EMAIL);
			
			if(recruitingModule.isApplicationPersonDisabilityEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.disability"));
				fieldsKeys.add(RecruitingModule.APP_PERSON_DISABILITY);
			}

			if(!fieldsLabels.isEmpty()) {
				rows.add(new StandardAttributeRow(true, type, type, String.join("; ", fieldsLabels), fieldsKeys));
			}
		}
		
		{
			boolean mandatory = false;
			String type = translator.translate("business.infos");
			List<String> fieldsLabels = new ArrayList<>();
			List<String> fieldsKeys = new ArrayList<>();
			
			if(recruitingModule.isApplicationBusinessInformationsOrganizationEnabled()
					&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION)) {
				fieldsLabels.add(translator.translate("edit.application.organization"));
				fieldsKeys.add(RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION);
				mandatory |= !recruitingModule.isApplicationBusinessInformationsOrganizationOptional();
			}
			
			if(recruitingModule.isApplicationBusinessInformationsUnitEnable()
					&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_UNIT)) {
				fieldsLabels.add(translator.translate("edit.application.unit"));
				fieldsKeys.add(RecruitingModule.APP_BUSINESS_INFOS_UNIT);
				mandatory |= !recruitingModule.isApplicationBusinessInformationsUnitOptional();
			}
			
			if(recruitingModule.isApplicationBusinessInformationsCurrentPositionEnabled()
					&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_POSITION)) {
				fieldsLabels.add(translator.translate("edit.application.currentPosition"));
				fieldsKeys.add(RecruitingModule.APP_BUSINESS_INFOS_POSITION);
				mandatory |= !recruitingModule.isApplicationBusinessInformationsCurrentPositionOptional();
			}
			
			if(!fieldsLabels.isEmpty()) {
				rows.add(new StandardAttributeRow(mandatory, type, type, String.join("; ", fieldsLabels), fieldsKeys));
			}
		}
		
		AddressOption privateOption = recruitingModule.getApplicationAddressPrivateOption();
		AddressOption businessOption = recruitingModule.getApplicationAddressBusinessOption();
		if(AddressOption.xor.equals(privateOption) && AddressOption.xor.equals(businessOption)) {
			appendAddressRows(rows, translator);
		} else {
			if(AddressOption.enabled.equals(businessOption) || AddressOption.optional.equals(businessOption)) {
				appendBusinessAddressRows(rows, translator);
			}
			if(AddressOption.enabled.equals(privateOption) || AddressOption.optional.equals(privateOption)) {
				appendPrivatAddressRows(rows, translator);
			}
		}
		
		return rows;
	}

	public static void appendAddressRows(List<StandardAttributeRow> rows, Translator translator) {
		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		
		String type = translator.translate("correspondence.address");
		List<String> fieldsLabels = new ArrayList<>();
		List<String> fieldsKeys = new ArrayList<>();
	
		fieldsLabels.add(translator.translate("edit.application.addressLine1"));
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_BUSINESS_LINE1);
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_PRIVATE_LINE1);
		fieldsLabels.add(translator.translate("edit.application.addressLine2"));
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_BUSINESS_LINE2);
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_PRIVATE_LINE2);
		
		if(recruitingModule.isApplicationAddressLine3Enabled()) {
			fieldsLabels.add(translator.translate("edit.application.addressLine3"));
			fieldsKeys.add(RecruitingModule.APP_ADDRESS_BUSINESS_LINE3);
			fieldsKeys.add(RecruitingModule.APP_ADDRESS_PRIVATE_LINE3);
		}
		
		fieldsLabels.add(translator.translate("edit.application.zipcode"));
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_BUSINESS_CODE);
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_PRIVATE_CODE);
		
		fieldsLabels.add(translator.translate("edit.application.city"));
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_BUSINESS_CITY);
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_PRIVATE_CITY);
		if (recruitingModule.isApplicationAddressCountryEnabled()) {
			fieldsLabels.add(translator.translate("edit.application.country"));
			fieldsKeys.add(RecruitingModule.APP_ADDRESS_BUSINESS_COUNTRY);
			fieldsKeys.add(RecruitingModule.APP_ADDRESS_PRIVATE_COUNTRY);
		}
		
		if(!fieldsLabels.isEmpty()) {
			rows.add(new StandardAttributeRow(true, type, type, String.join("; ", fieldsLabels), fieldsKeys));
		}
	}
	
	public static void appendBusinessAddressRows(List<StandardAttributeRow> rows, Translator translator) {
		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		AddressOption businessOption = recruitingModule.getApplicationAddressBusinessOption();
		boolean optional = AddressOption.optional.equals(businessOption);

		String type = translator.translate("address.business");
		List<String> fieldsLabels = new ArrayList<>();
		List<String> fieldsKeys = new ArrayList<>();
	
		fieldsLabels.add(translator.translate("edit.application.addressLine1"));
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_BUSINESS_LINE1);
		fieldsLabels.add(translator.translate("edit.application.addressLine2"));
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_BUSINESS_LINE2);
		
		if(recruitingModule.isApplicationAddressLine3Enabled()) {
			fieldsLabels.add(translator.translate("edit.application.addressLine3"));
			fieldsKeys.add(RecruitingModule.APP_ADDRESS_BUSINESS_LINE3);
		}
		
		fieldsLabels.add(translator.translate("edit.application.zipcode"));
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_BUSINESS_CODE);
		fieldsLabels.add(translator.translate("edit.application.city"));
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_BUSINESS_CITY);
		if (recruitingModule.isApplicationAddressCountryEnabled()) {
			fieldsLabels.add(translator.translate("edit.application.country"));
			fieldsKeys.add(RecruitingModule.APP_ADDRESS_BUSINESS_COUNTRY);
		}
		
		rows.add(new StandardAttributeRow(!optional, type, type, String.join("; ", fieldsLabels), fieldsKeys));
	}
	
	public static void appendPrivatAddressRows(List<StandardAttributeRow> rows, Translator translator) {
		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		AddressOption privateOption = recruitingModule.getApplicationAddressPrivateOption();
		boolean optional = AddressOption.optional.equals(privateOption);

		String type = translator.translate("address.private");
		List<String> fieldsLabels = new ArrayList<>();
		List<String> fieldsKeys = new ArrayList<>();
	
		fieldsLabels.add(translator.translate("edit.application.addressLine1"));
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_PRIVATE_LINE1);
		fieldsLabels.add(translator.translate("edit.application.addressLine2"));
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_PRIVATE_LINE2);
		
		if(recruitingModule.isApplicationAddressLine3Enabled()) {
			fieldsLabels.add(translator.translate("edit.application.addressLine3"));
			fieldsKeys.add(RecruitingModule.APP_ADDRESS_PRIVATE_LINE3);
		}
		
		fieldsLabels.add(translator.translate("edit.application.zipcode"));
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_PRIVATE_CODE);
		fieldsLabels.add(translator.translate("edit.application.city"));
		fieldsKeys.add(RecruitingModule.APP_ADDRESS_PRIVATE_CITY);
		if (recruitingModule.isApplicationAddressCountryEnabled()) {
			fieldsLabels.add(translator.translate("edit.application.country"));
			fieldsKeys.add(RecruitingModule.APP_ADDRESS_PRIVATE_COUNTRY);
		}
		
		rows.add(new StandardAttributeRow(!optional, type, type, String.join("; ", fieldsLabels), fieldsKeys));
	}
	
	public static List<StandardAttributeRow> getAcademicalBackgroundRows(List<String> excludedAttributesList, Translator translator) {
		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		List<StandardAttributeRow> rows = new ArrayList<>();
		
		boolean highestDegreeEnabled = recruitingModule.isApplicationAcademicalBackgroundHighestDegreeEnabled();
		if(highestDegreeEnabled && !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)) {
			boolean mandatory = !recruitingModule.isApplicationAcademicalBackgroundHighestDegreeOptional();
			String type = translator.translate("edit.application.highestdegree");
			
			String fields;
			if(excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_TYPE)
					|| excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR)
					|| excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_INSTITUTION)) {
				fields = translator.translate("standard.attributes.highestdegreetypeyear.part");
			} else {
				fields = translator.translate("standard.attributes.highestdegreetypeyear");
			}
			rows.add(new StandardAttributeRow(mandatory, type, type, fields, RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE));
		}

		{// academic age
			boolean mandatory = false;
			String type = translator.translate("edit.application.highestdegreeWorkedSince");
			List<String> fieldsLabels = new ArrayList<>();
			List<String> fieldsKeys = new ArrayList<>();
			
			if(recruitingModule.isApplicationAcademicalBackgroundWorkedInAcademiaSinceEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.workedInAcademiaSince.label"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_WORKED_IN_ACADEMIA_SINCE);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundWorkedInAcademiaSinceOptional();
			}
			
			if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaSinceEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.workedOutAcademiaSince.label"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_WORKED_OUT_ACADEMIA_SINCE);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaSinceOptional();
			}
			
			if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.workedOutAcademiaCareSince.label"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_WORKED_OUT_ACADEMIA_CARE_SINCE);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceOptional();
			}
			
			if(recruitingModule.isApplicationAcademicalBackgroundCareerDescriptionEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.careerDescription.label"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_CAREER_DESCRIPTION);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundCareerDescriptionOptional();
			}
			
			if(!fieldsLabels.isEmpty()) {
				rows.add(new StandardAttributeRow(mandatory, type, type, String.join("; ", fieldsLabels), fieldsKeys));
			}
		}
		
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationEnabled()) {
			boolean mandatory = false;
			String type = translator.translate("edit.application.dissertation");
			List<String> fieldsLabels = new ArrayList<>();
			List<String> fieldsKeys = new ArrayList<>();
			
			if(recruitingModule.isApplicationAcademicalBackgroundDissertationTitleEnabled()
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION)) {
				fieldsLabels.add(translator.translate("edit.application.dissertation"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_DISSERTATION);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundDissertationTitleOptional();
			}
		
			if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword1Enabled()) {
				fieldsLabels.add(translator.translate("edit.application.dissertationkeyword1"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD1);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword1Optional();
			}
			
			if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword2Enabled()) {
				fieldsLabels.add(translator.translate("edit.application.dissertationkeyword2"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD2);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword2Optional();
			}
			
			if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword3Enabled()) {
				fieldsLabels.add(translator.translate("edit.application.dissertationkeyword3"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD3);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword3Optional();
			}
			
			if(!fieldsLabels.isEmpty()) {
				rows.add(new StandardAttributeRow(mandatory, type, type, String.join("; ", fieldsLabels), fieldsKeys));
			}
		}
		
		{
			boolean mandatory = false;
			String type = translator.translate("edit.application.habilitation");
			List<String> fieldsLabels = new ArrayList<>();
			List<String> fieldsKeys = new ArrayList<>();
			
			if(recruitingModule.isApplicationAcademicalBackgroundHabilitationTitleEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.habilitation"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_HABILITATION);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundHabilitationTitleOptional();
			}

			if(!fieldsLabels.isEmpty()) {
				rows.add(new StandardAttributeRow(mandatory, type, type, String.join("; ", fieldsLabels), fieldsKeys));
			}
		}
		
		if(recruitingModule.isApplicationAcademicalBackgroundOrcidEnabled()) {
			String fieldLabel = translator.translate("edit.application.orcid");
			boolean mandatory = !recruitingModule.isApplicationAcademicalBackgroundOrcidOptional();
			rows.add(new StandardAttributeRow(mandatory, fieldLabel, "", fieldLabel, RecruitingModule.APP_ACADEMIC_ORCID));
		}
		
		{
			boolean mandatory = false;
			String type = translator.translate("publications.infos");
			List<String> fieldsLabel = new ArrayList<>();
			List<String> fieldsKeys = new ArrayList<>();
			
			if(recruitingModule.isApplicationAcademicalBackgroundNumberOfOriginalPublicationsEnabled()) {
				fieldsLabel.add(translator.translate("edit.application.numberOfOriginalPublications"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_NUM_OF_ORIGINAL_PUBLICATIONS);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundNumberOfOriginalPublicationsOptional();
			}
			
			if(recruitingModule.isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsEnabled()) {
				fieldsLabel.add(translator.translate("edit.application.numberOfFirstAuthorships"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_NUM_OF_FIRST_AUTHORSHIPS);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsOptional();
			}
			
			if(recruitingModule.isApplicationAcademicalBackgroundNumberOfLastAuthorshipsEnabled()) {
				fieldsLabel.add(translator.translate("edit.application.numberOfLastAuthorships"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_NUM_OF_LAST_AUTHORSHIPS);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundNumberOfLastAuthorshipsOptional();
			}
			
			if(recruitingModule.isApplicationAcademicalBackgroundCitationsEnabled()) {
				fieldsLabel.add(translator.translate("edit.application.citations"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_CITATIONS);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundCitationsOptional();
			}
			
			if(recruitingModule.isApplicationAcademicalBackgroundImpactFactorEnabled()) {
				fieldsLabel.add(translator.translate("edit.application.impactFactor"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_IMPACT_FACTOR);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundImpactFactorOptional();
			}
			
			if(recruitingModule.isApplicationAcademicalBackgroundHFactorEnabled()) {
				fieldsLabel.add(translator.translate("edit.application.hFactor"));
				fieldsKeys.add(RecruitingModule.APP_ACADEMIC_HFACTORY);
				mandatory |= !recruitingModule.isApplicationAcademicalBackgroundHFactorOptional();
			}
			
			if(!fieldsLabel.isEmpty()) {
				rows.add(new StandardAttributeRow(mandatory, type, type, String.join("; ", fieldsLabel), fieldsKeys));
			}
		}

		return rows;
	}
	
	public static List<StandardAttributeRow> getProjectRows(Translator translator) {
		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		List<StandardAttributeRow> rows = new ArrayList<>();
		
		{
			boolean mandatory = false;
			String type = translator.translate("standard.attributes.project");
			List<String> fieldsLabels = new ArrayList<>();
			List<String> fieldsKeys = new ArrayList<>();
			
			if(recruitingModule.isApplicationProjectTitleEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.project.title"));
				fieldsKeys.add(RecruitingModule.APP_PROJECT_TITLE);
				mandatory |= !recruitingModule.isApplicationProjectTitleOptional();
			}
			
			if(recruitingModule.isApplicationProjectDescriptionEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.project.description"));
				fieldsKeys.add(RecruitingModule.APP_PROJECT_DESCRIPTION);
				mandatory |= !recruitingModule.isApplicationProjectDescriptionOptional();
			}
			
			if(recruitingModule.isApplicationProjectAcronymEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.project.acronym"));
				fieldsKeys.add(RecruitingModule.APP_PROJECT_ACRONYM);
				mandatory |= !recruitingModule.isApplicationProjectAcronymOptional();
			}

			if(recruitingModule.isApplicationProjectKeywordsEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.project.keywords"));
				fieldsKeys.add(RecruitingModule.APP_PROJECT_KEYWORDS);
				mandatory |= !recruitingModule.isApplicationProjectKeywordsOptional();
			}
			
			if(recruitingModule.isApplicationProjectDisciplinesEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.project.disciplines"));
				fieldsKeys.add(RecruitingModule.APP_PROJECT_DISCIPLINES);
				mandatory |= !recruitingModule.isApplicationProjectDisciplinesOptional();
			}
			
			if(recruitingModule.isApplicationProjectStartDateEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.project.start.date"));
				fieldsKeys.add(RecruitingModule.APP_PROJECT_START_DATE);
				mandatory |= !recruitingModule.isApplicationProjectStartDateOptional();
			}

			if(recruitingModule.isApplicationProjectDurationEnabled()) {
				fieldsLabels.add(translator.translate("edit.application.project.duration"));
				fieldsKeys.add(RecruitingModule.APP_PROJECT_DURATION);
				mandatory |= !recruitingModule.isApplicationProjectDurationOptional();
			}
			
			if(!fieldsLabels.isEmpty()) {
				rows.add(new StandardAttributeRow(mandatory, type, type, String.join("; ", fieldsLabels), fieldsKeys));
			}
		}
		
		{
			boolean mandatory = false;
			String type = translator.translate("edit.application.project.financialimpact");
			List<String> fieldsLabels = new ArrayList<>();
			List<String> fieldsKeys = new ArrayList<>();

			mandatory |= tableRow(recruitingModule.getApplicationProjectFinancialImpact1Type(), "edit.application.project.financialimpact.1", fieldsLabels,
					RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_1, fieldsKeys, translator);
			mandatory |= tableRow(recruitingModule.getApplicationProjectFinancialImpact2Type(), "edit.application.project.financialimpact.2", fieldsLabels,
					RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_2, fieldsKeys, translator);
			mandatory |= tableRow(recruitingModule.getApplicationProjectFinancialImpact3Type(), "edit.application.project.financialimpact.3", fieldsLabels,
					RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_3, fieldsKeys, translator);
			mandatory |= tableRow(recruitingModule.getApplicationProjectFinancialImpact4Type(), "edit.application.project.financialimpact.4", fieldsLabels,
					RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_4, fieldsKeys, translator);
			mandatory |= tableRow(recruitingModule.getApplicationProjectFinancialImpact5Type(), "edit.application.project.financialimpact.5", fieldsLabels,
					RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_5, fieldsKeys, translator);
			
			if(!fieldsLabels.isEmpty()) {
				rows.add(new StandardAttributeRow(mandatory, type, type, String.join("; ", fieldsLabels), fieldsKeys));
			}
		}

		return rows;
	}
	
	private static boolean tableRow(ApplicationFieldType fieldType, String i18nKey, List<String> fieldsLabels, String fieldKey, List<String> fieldsKeys, Translator translator) {
		if(fieldType.isEnabled()) {
			fieldsLabels.add(translator.translate(i18nKey));
			fieldsKeys.add(fieldKey);
		}
		return !fieldType.isOptional();
	}
}
