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
package org.olat.modules.selectus.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.AddressOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.manager.DecisionRubricSPI;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRefereeStats;
import org.olat.modules.selectus.model.DecisionRubric;
import org.olat.modules.selectus.model.DecisionRubricDefinition;
import org.olat.modules.selectus.model.HighestDegreeType;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionConfiguration;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.olat.modules.selectus.ui.components.DefaultExportTableDataModel;
import org.olat.modules.selectus.ui.components.SelectAdditionalAttributeCellRenderer;

/**
 * 
 * Description:<br>
 * Data model for the Excel download
 * 
 * <P>
 * Initial Date:  8 avr. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionApplicationsExcelDataModel extends DefaultExportTableDataModel<ApplicationLight> {

	private final int numOfCommitteeMembers;
	private final Translator translator;
	
	private final List<Tab> customTabs;
	private final boolean customTabsEnabled;
	private final boolean academicalBackgroundEnabled;
	
	private final Position position;
	private final List<UserRating> userRatings;
	private final List<DecisionRubric> rubrics;
	private final Map<Long,List<ApplicationCategoryInfos>> appToCategories;
	private final List<DecisionRubricDefinition> definitions;
	private final RecruitingPositionSecurityCallback secCallback;
	private Map<Long,ApplicationRefereeStats> appKeyToReviewerStats;
	private final Map<String,DecisionRubricSPI> keyToSpies = new HashMap<>();
	private final List<String> excludedAttributesList;
	
	private final RecruitingModule recruitingModule;

	public PositionApplicationsExcelDataModel(int numOfCommitteeMembers, Position position, List<ApplicationLight> applications,
			List<UserRating> userRatings, Map<Long,ApplicationRefereeStats> appKeyToReviewerStats,
			List<DecisionRubricDefinition> definitions, List<DecisionRubric> rubrics, Map<Long,List<ApplicationCategoryInfos>> appToCategories,
			RecruitingPositionSecurityCallback secCallback, Translator translator) {
		super(applications);
		this.numOfCommitteeMembers = numOfCommitteeMembers;
		this.translator = translator;
		this.position = position;
		this.rubrics = rubrics;
		this.definitions = definitions;
		this.userRatings = userRatings;
		this.secCallback = secCallback;
		this.appToCategories = appToCategories;
		customTabs = position.getCustomTabsList();
		excludedAttributesList = position.getExcludedAttributesList();
		this.appKeyToReviewerStats = appKeyToReviewerStats;
		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		academicalBackgroundEnabled = recruitingModule.isApplicationAcademicalBackgroundEnabled(position);
		customTabsEnabled = recruitingModule.isPositionCustomStepsEnabled() && !position.getCustomTabsList().isEmpty();
		
		if(definitions != null && !definitions.isEmpty()) {
			List<DecisionRubricSPI> spies = recruitingModule.getDecisionRubricSpies();
			for(DecisionRubricSPI spi:spies) {
				keyToSpies.put(spi.getKey(), spi);
			}
		}
	}
	
	public Translator getTranslator() {
		return translator;
	}
	
	public int getNumOfCommitteeMembers() {
		return numOfCommitteeMembers;
	}

	public List<ApplicationLight> getApplications() {
		return new ArrayList<>(objects);
	}
	
	public List<UserRating> getRatings() {
		return userRatings;
	}
	
	@Override
	public int getColumnCount() {
		int numOfRubricsCols = 0;
		if(definitions != null && !definitions.isEmpty()) {
			numOfRubricsCols += definitions.size();
			for(DecisionRubricDefinition def:definitions) {
				if(def.isSum()) {
					numOfRubricsCols++;
					break;
				}
			}
		}
		return ExcelFields.values().length + numOfRubricsCols;
	}
	
	@Override
	public int[] getExportColumnIndex() {
		int[] columns;

		ExcelFields[] fields = ExcelFields.values();
		int numOfFields = fields.length;
		
		List<Integer> columnList = new ArrayList<>(numOfFields);
		int j=0;
		for(; j<fields.length; j++) {
			ExcelFields field = fields[j];
			if(isEnabled(field)) {
				columnList.add(field.ordinal());
			}
			
			if(field == ExcelFields.currentPosition) {
				getExportAdditionalColumnIndex(PositionApplicationAttributeTabEnum.personalData, columnList);
			} else if(field == ExcelFields.orcid) {
				getExportAdditionalColumnIndex(PositionApplicationAttributeTabEnum.academicalBackground, columnList);
			} else if(field == ExcelFields.projectFinancialImpact5) {
				getExportAdditionalColumnIndex(PositionApplicationAttributeTabEnum.project, columnList);
			}else if(field == ExcelFields.businessMail && customTabsEnabled) {
				for(Tab tab:customTabs) {
					TabConfiguration tabConfiguration = position.getTabConfiguration(tab);
					if(!tabConfiguration.isDisabled()) {
						getExportAdditionalColumnIndex(tab.attributesTab(), columnList);
					}
				}
			}
		}

		if(definitions != null) {
			boolean hasSum = false;
			for(int k=0; k<definitions.size(); k++) {
				columnList.add(j++);
				if(definitions.get(k).isSum()) {
					hasSum |= true;
				}
			}
			if(hasSum) {
				columnList.add(j++);
			}
		}
		
		columns = new int[columnList.size()];
		for(int i=columnList.size(); i-->0; ) {
			columns[i] = columnList.get(i).intValue();
		}
		
		return columns;
	}
	
	private void getExportAdditionalColumnIndex(PositionApplicationAttributeTabEnum tab, List<Integer> columnList) {
		List<PositionAttributeDefinition> attributeDefinitions = position.getAttributesDefinitions();
		for(int i=0; i<attributeDefinitions.size(); i++) {
			PositionAttributeDefinition attributeDefinition = attributeDefinitions.get(i);
			if(attributeDefinition != null && attributeDefinition.getTabEnum() == tab
					&& (attributeDefinition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.question
						|| attributeDefinition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.select
						|| attributeDefinition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.number
						|| attributeDefinition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.percentage
						|| attributeDefinition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.date)) {
				columnList.add(ApplicationAttributesDelegate.COLS_OFFSET + i);
			}
		}
	}
	
	private boolean isEnabled(ExcelFields field) {
		switch(field) {
			case birthday: return recruitingModule.isApplicationPersonBirthdayEnabled();
			case language: return recruitingModule.getPositionLocales().length > 1;
			//academic title
			case academicTitle: return recruitingModule.isApplicationPersonAcademicTitleEnabled();
			// phone
			case phone: return recruitingModule.isApplicationPersonPhoneEnabled();
			case mobilePhone: return recruitingModule.isApplicationPersonMobilePhoneEnabled();
			case disability: return recruitingModule.isApplicationPersonDisabilityEnabled();
			//marital status
			case maritalStatus: return recruitingModule.isApplicationPersonMaritalStatusEnabled();
			case gender: return recruitingModule.isApplicationPersonGenderEnabled();
			case nationality: return recruitingModule.isApplicationPersonNationalityEnabled();
			case additionalNationalities: return recruitingModule.isApplicationPersonAdditionalNationalitiesEnabled();
			case organization: return recruitingModule.isApplicationBusinessInformationsOrganizationEnabled()
					&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION);
			case unit: return recruitingModule.isApplicationBusinessInformationsUnitEnable()
					&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_UNIT);
			case currentPosition: return recruitingModule.isApplicationBusinessInformationsCurrentPositionEnabled()
					&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_POSITION);
			//academic background
			case numberOfOriginalPublications: return recruitingModule.isApplicationAcademicalBackgroundNumberOfOriginalPublicationsEnabled() && academicalBackgroundEnabled;
			case numberOfFirstAuthorships: return recruitingModule.isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsEnabled() && academicalBackgroundEnabled;
			case numberOfLastAuthorships: return recruitingModule.isApplicationAcademicalBackgroundNumberOfLastAuthorshipsEnabled() && academicalBackgroundEnabled;
			case citations: return recruitingModule.isApplicationAcademicalBackgroundCitationsEnabled() && academicalBackgroundEnabled;
			case impactFactor: return recruitingModule.isApplicationAcademicalBackgroundImpactFactorEnabled() && academicalBackgroundEnabled;
			case hFactor: return recruitingModule.isApplicationAcademicalBackgroundHFactorEnabled() && academicalBackgroundEnabled;
			//degree
			case highestDegreeType: return academicalBackgroundEnabled && recruitingModule.isApplicationAcademicalBackgroundHighestDegreeEnabled()
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_TYPE);
			case highestDegreeYear: return recruitingModule.isApplicationAcademicalBackgroundHighestDegreeEnabled()
					&& !recruitingModule.isTableApplicationsHighestDegreeYearOnlyPhDOption() && academicalBackgroundEnabled
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR);
			case highestDegreeYearPhD: return recruitingModule.isApplicationAcademicalBackgroundHighestDegreeEnabled()
					&& recruitingModule.isTableApplicationsHighestDegreeYearOnlyPhDOption() && academicalBackgroundEnabled
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR);
			case highestDegreeInstitution: return recruitingModule.isApplicationAcademicalBackgroundHighestDegreeEnabled() && academicalBackgroundEnabled
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_INSTITUTION);
			case workedInAcademiaSince: return recruitingModule.isApplicationAcademicalBackgroundWorkedInAcademiaSinceEnabled() && academicalBackgroundEnabled;
			case workedOutAcademiaSince: return recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaSinceEnabled() && academicalBackgroundEnabled;
			case workedOutAcademiaCareSince: return recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceEnabled() && academicalBackgroundEnabled;
			case careerDescription: return recruitingModule.isApplicationAcademicalBackgroundCareerDescriptionEnabled() && academicalBackgroundEnabled;
			//dissertation
			case dissertationTitle: return recruitingModule.isApplicationAcademicalBackgroundDissertationTitleEnabled() && academicalBackgroundEnabled
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION);
			case dissertationDate: return recruitingModule.isApplicationAcademicalBackgroundDissertationDateEnabled() && academicalBackgroundEnabled
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION);
			case dissertationInstitution: return recruitingModule.isApplicationAcademicalBackgroundDissertationInstitutionEnabled() && academicalBackgroundEnabled
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION);
			case dissertationKeyword1: return recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword1Enabled() && academicalBackgroundEnabled;
			case dissertationKeyword2: return recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword2Enabled() && academicalBackgroundEnabled;
			case dissertationKeyword3: return recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword3Enabled() && academicalBackgroundEnabled;
			//habilitation
			case habilitationTitle:
			case habilitationDate:
			case habilitationInstitution: return recruitingModule.isApplicationAcademicalBackgroundHabilitationEnabled() && academicalBackgroundEnabled;
			case orcid: return recruitingModule.isApplicationAcademicalBackgroundOrcidEnabled() && academicalBackgroundEnabled;
			case creationDate: {
				RecruitingTableOption option = recruitingModule.getTableApplicationsSubmittedDateOption();
				return option == RecruitingTableOption.enabled || option == RecruitingTableOption.optional;
			}
			case submittedByStaff: {
				RecruitingTableOption option = recruitingModule.getTableApplicationsSubmittedByStaffOption();
				return option == RecruitingTableOption.enabled || option == RecruitingTableOption.optional;
			}
			case addressLine1:
			case addressLine2:
			case addressLine3:
			case zipcode:
			case city:
			case country: {
				if (recruitingModule.isApplicationAddressCountryEnabled()) {
					AddressOption option = recruitingModule.getApplicationAddressPrivateOption();
					return !AddressOption.disabled.equals(option);
				}
				return false;
			}
			case businessAddressLine1:
			case businessAddressLine2:
			case businessAddressLine3:
			case businessZipcode:
			case businessCity:
			case businessCountry: {
				if (recruitingModule.isApplicationAddressCountryEnabled()) {
					AddressOption option = recruitingModule.getApplicationAddressBusinessOption();
					return AddressOption.enabled.equals(option) || AddressOption.optional.equals(option);
				}
				return false;
			}
			case businessPhone: return recruitingModule.isApplicationBusinessPhoneEnabled();
			case businessMail: return recruitingModule.isApplicationBusinessMailEnabled();
			case abstention: return recruitingModule.isRatingAbstentionEnabled();
			case experts: return recruitingModule.isReferenceEnabled() && position.isExpertRecommendationEnabled();
			case recommendations: return recruitingModule.isReferenceEnabled() && position.isRefereeRecommendationEnabled();
			case comparativeExperts: return recruitingModule.isReferenceEnabled() && recruitingModule.isComparativeAssessmentExpertsEnabled()
					&& position.isComparativeAssessmentExpertEnabled();
			case projectTitle: return recruitingModule.isApplicationProjectEnabled() && recruitingModule.isApplicationProjectTitleEnabled() && position.isApplicationProject();
			case projectDescription: return recruitingModule.isApplicationProjectDescriptionEnabled() && position.isApplicationProject();
			case projectAcronym: return recruitingModule.isApplicationProjectAcronymEnabled() && position.isApplicationProject();
			case projectKeywords: return recruitingModule.isApplicationProjectKeywordsEnabled() && position.isApplicationProject();
			case projectDisciplines: return recruitingModule.isApplicationProjectDisciplinesEnabled() && position.isApplicationProject();
			case projectStartDate: return recruitingModule.isApplicationProjectStartDateEnabled() && position.isApplicationProject();
			case projectDuration: return recruitingModule.isApplicationProjectDurationEnabled() && position.isApplicationProject();
			case projectFinancialImpact1: return recruitingModule.isApplicationProjectFinancialImpact1Enabled() && position.isApplicationProject();
			case projectFinancialImpact2: return recruitingModule.isApplicationProjectFinancialImpact2Enabled() && position.isApplicationProject();
			case projectFinancialImpact3: return recruitingModule.isApplicationProjectFinancialImpact3Enabled() && position.isApplicationProject();
			case projectFinancialImpact4: return recruitingModule.isApplicationProjectFinancialImpact4Enabled() && position.isApplicationProject();
			case projectFinancialImpact5: return recruitingModule.isApplicationProjectFinancialImpact5Enabled() && position.isApplicationProject();
			case categories: return recruitingModule.isCategoriesEnabledFor(position);
			case expertBlackList: return recruitingModule.isReferenceExpertsBlackListEnabled() && secCallback.canSeeExpertBlackList();
			case memo: return recruitingModule.isApplicationsMemoEnabled();
			case committeeComment: return recruitingModule.isApplicationsCommitteeCommentEnabled() && position.isCommitteeCommentEnabled()
					&& (secCallback.canEditApplicationCommitteeComment() || secCallback.canViewCommitteeComment());
			default: return true;
		}
	}

	@Override
	public String getHeader(int col) {
		if(col >= 0 && col < ExcelFields.values().length) {
			ExcelFields field = ExcelFields.values()[col];
			return translator.translate(field.key());
		}
		
		if(col >= ApplicationAttributesDelegate.COLS_OFFSET) {
			int index = col - ApplicationAttributesDelegate.COLS_OFFSET;
			List<PositionAttributeDefinition> attributeDefinitions = position.getAttributesDefinitions();
			String label = "Custom attribute";
			if(index < attributeDefinitions.size()) {
				PositionAttributeDefinition attributeDefinition = attributeDefinitions.get(index);
				if(attributeDefinition != null) {
					label = attributeDefinition.getLabel(getLocale(), true);
				}
			}
			return label;
		}
		
		int definitionCol = col - ExcelFields.values().length;
		if(definitionCol >= 0 && definitionCol < definitions.size()) {
			return definitions.get(definitionCol).getRubric();
		} else if(definitionCol == definitions.size()) {
			return "Sum";
		}
		
		return "";
	}
	
	@Override
	public String getFieldNameAt(int col) {
		if(col >= 0 && col < ExcelFields.values().length) {
			ExcelFields field = ExcelFields.values()[col];
			return field.name();
		}
		
		int definitionCol = col - ExcelFields.values().length;
		if(definitionCol >= 0 && definitionCol < definitions.size()) {
			return definitions.get(definitionCol).getRubric();
		} else if(definitionCol == definitions.size()) {
			return "Sum";
		}
		
		return "";
	}

	@Override
	public Class<?> getTypeAt(int row, int col) {
		if(col >= 0 && col < ExcelFields.values().length) {
			ExcelFields field = ExcelFields.values()[col];
			switch(field) {
				case disability: return Boolean.class;
				case numberOfOriginalPublications: return Integer.class;
				case numberOfFirstAuthorships: return Integer.class;
				case numberOfLastAuthorships: return Integer.class;
				case citations: return Integer.class;
				case impactFactor: return Double.class; 
				case hFactor: return Double.class;
				case dissertationDate: return Integer.class;
				case habilitationDate: return Integer.class;
				case highestDegreeYear: return Integer.class;
				case highestDegreeYearPhD: return Integer.class;
				case committeeRatingA: return Integer.class;
				case committeeRatingB: return Integer.class;
				case committeeRatingC: return Integer.class;
				case abstention: return Integer.class;
				case birthday: return Date.class;
				case creationDate: return Date.class;
				case applicationStatusDate: return Date.class;
				case projectStartDate: return Date.class;
				case projectFinancialImpact1: return recruitingModule.getApplicationProjectFinancialImpact1Type().toClass();
				case projectFinancialImpact2: return recruitingModule.getApplicationProjectFinancialImpact2Type().toClass();
				case projectFinancialImpact3: return recruitingModule.getApplicationProjectFinancialImpact3Type().toClass();
				case projectFinancialImpact4: return recruitingModule.getApplicationProjectFinancialImpact4Type().toClass();
				case projectFinancialImpact5: return recruitingModule.getApplicationProjectFinancialImpact5Type().toClass();
				default: return String.class;
			}
		} else if(col >= ApplicationAttributesDelegate.COLS_OFFSET) {
			int attributesCol = col - ApplicationAttributesDelegate.COLS_OFFSET;
			List<PositionAttributeDefinition> attributeDefinitions = position.getAttributesDefinitions();
			PositionAttributeDefinition attributeDefinition = attributeDefinitions.get(attributesCol);
			if(attributeDefinition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.number
					|| attributeDefinition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.percentage) {
				return Integer.class;
			} else if(attributeDefinition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.date) {
				return Date.class;
			}
			return String.class;
		} else {
			int definitionCol = col - ExcelFields.values().length;
			if(definitionCol == definitions.size()) {
				return Double.class;
			}
			return String.class;
		}
	}
	
	private int countRating(ApplicationLight app, int ratingRef) {
		int numOfRatings = 0;
		String resSubPath = app.getKey().toString();
		
		for(UserRating rating:userRatings) {
			if(rating != null && rating.getRating() != null 
					&& rating.getResSubPath().equals(resSubPath) 
					&& rating.getRating().intValue() == ratingRef) {
				numOfRatings++;
			}
		}
		return numOfRatings;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ApplicationLight app = getObject(row);

		if(col >= 0 && col < ExcelFields.values().length) {
			ExcelFields field = ExcelFields.values()[col];
			switch(field) {
				case id: return app.getId();
				case title: {
					if(StringHelper.containsNonWhitespace(app.getPerson().getTitle())) {
						String title = translator.translate(app.getPerson().getTitle());
						if(title != null && title.length() < 15) {
							return title;
						}
						return app.getPerson().getTitle();
					} else {
						return "";
					}
				}
				case firstName: return app.getPerson().getFirstName();
				case lastName: return app.getPerson().getLastName();
				case birthday: return app.getPerson().getBirthday();
				case gender: return RecruitingHelper.formatGender(app.getPerson().getGender(), translator.getLocale());
				case maritalStatus: {
					String status = app.getPerson().getMaritalStatus();
					if(StringHelper.containsNonWhitespace(status) && !"-".equals(status)) {
						return translator.translate("edit.application.marital.status." + status);
					}
					return "-";
				}
				case disability: return app.getPerson().getDisability();
				case language: return app.getLanguage();
				case academicTitle: return app.getPerson().getAcademicTitle();
				case currentPosition: return app.getBusinessInformations().getCurrentPosition();
				case numberOfOriginalPublications: return app.getAcademicalBackground().getNumberOfOriginalPublications();
				case numberOfFirstAuthorships: return app.getAcademicalBackground().getNumberOfFirstAuthorships();
				case numberOfLastAuthorships: return app.getAcademicalBackground().getNumberOfLastAuthorships();
				case citations: return app.getAcademicalBackground().getCitations();
				case impactFactor: return RecruitingHelper.formatFactor(app.getAcademicalBackground().getImpactFactor()); 
				case hFactor: return RecruitingHelper.formatFactor(app.getAcademicalBackground().getHFactor());
				case highestDegreeType: {
					String type = app.getAcademicalBackground().getHighestDegreeType();
					if(StringHelper.containsNonWhitespace(type)) {
						if(HighestDegreeType.other.name().equals(type) && StringHelper.containsNonWhitespace(app.getAcademicalBackground().getHighestDegreeDescription())) {
							return app.getAcademicalBackground().getHighestDegreeDescription();
						}
						String transType = translator.translate("edit.application.degreetype." + type);
						if(transType.length() > 50) return type;
						return transType;
					}
					return null;
				}
				case highestDegreeYear: return toYear(app.getAcademicalBackground().getHighestDegreeDate());
				case highestDegreeYearPhD: {
					if(app.getAcademicalBackground().getHighestDegreeDate() == null) {
						return null;
					}
					String type = app.getAcademicalBackground().getHighestDegreeType();
					if(type != null && HighestDegreeType.phd.name().equals(type)) {
						return toYear(app.getAcademicalBackground().getHighestDegreeDate());
					}
					return null;
				}
				case highestDegreeInstitution: return app.getAcademicalBackground().getHighestDegreeInstitution();
				case workedInAcademiaSince: return app.getAcademicalBackground().getWorkedInAcademiaSince();
				case workedOutAcademiaSince: return app.getAcademicalBackground().getWorkedOutAcademiaSince();
				case workedOutAcademiaCareSince: return app.getAcademicalBackground().getWorkedOutAcademiaCareSince();
				case careerDescription: return app.getAcademicalBackground().getCareerDescription();
				case dissertationTitle: return app.getAcademicalBackground().getDissertationTitle();
				case dissertationDate: return toYear(app.getAcademicalBackground().getDissertationDate());
				case dissertationInstitution: return app.getAcademicalBackground().getDissertationInstitution();
				case dissertationKeyword1: return app.getAcademicalBackground().getDissertationKeyword1();
				case dissertationKeyword2: return app.getAcademicalBackground().getDissertationKeyword2();
				case dissertationKeyword3: return app.getAcademicalBackground().getDissertationKeyword3();
				case habilitationTitle: return app.getAcademicalBackground().getHabilitationTitle();
				case habilitationDate: return toYear(app.getAcademicalBackground().getHabilitationDate());
				case habilitationInstitution: return app.getAcademicalBackground().getHabilitationInstitution();
				case orcid: return app.getAcademicalBackground().getOrcid();
				case committeeRatingA: return countRating(app, 3);
				case committeeRatingB: return countRating(app, 2);
				case committeeRatingC: return countRating(app, 1);
				case abstention: return countRating(app, RecruitingService.ABSTENTION);
				case decision: {
					Integer decision = app.getDecision();
					String v = null;
					if(decision != null && decision.intValue() > 0) {
						switch(decision.intValue()) {
							case 1: v = "C"; break;
							case 2: v = "B"; break;
							case 3: v = "A"; break;
							default: {}
						}
					}
					return v;
				}
				case nationality: return app.getPerson().getNationality();
				case additionalNationalities: return RecruitingHelper.beautifyCountriesList(app.getPerson().getAdditionalNationalities());
				case mail: return app.getPerson().getMail();
				case phone: return app.getPerson().getPhone();
				case mobilePhone: return app.getPerson().getMobilePhone();
				case organization: return RecruitingHelper.mergeOrganizationAndAffiliation(app.getBusinessInformations());
				case unit: return app.getBusinessInformations().getUnit();
				case addressLine1: return app.getAddress().getAddressLine1();
				case addressLine2: return app.getAddress().getAddressLine2();
				case addressLine3: return app.getAddress().getAddressLine3();
				case zipcode: return app.getAddress().getZipCode();
				case city: return app.getAddress().getCity();
				case country: return app.getAddress().getCountry();
				case businessAddressLine1: return app.getBusinessAddress().getAddressLine1();
				case businessAddressLine2: return app.getBusinessAddress().getAddressLine2();
				case businessAddressLine3: return app.getBusinessAddress().getAddressLine3();
				case businessZipcode: return app.getBusinessAddress().getZipCode();
				case businessCity: return app.getBusinessAddress().getCity();
				case businessCountry: return app.getBusinessAddress().getCountry();
				case businessPhone: return app.getBusinessAddress().getPhone();
				case businessMail: return app.getBusinessAddress().getEmail();
				case experts: {
					ApplicationRefereeStats stats = appKeyToReviewerStats.get(app.getKey());
					return stats.getNumOfSubmittedExperts() + " / " + stats.getNumOfExperts();
				}
				case recommendations: {
					ApplicationRefereeStats stats = appKeyToReviewerStats.get(app.getKey());
					return stats.getNumOfSubmittedRecommendations() + " / " + stats.getNumOfRecommendations();
				}
				case comparativeExperts: {
					ApplicationRefereeStats stats = appKeyToReviewerStats.get(app.getKey());
					return stats.getNumOfSubmittedComparativeExperts() + " / " + stats.getNumOfComparativeExperts();
				}
				case ad: return app.getJobAd();
				case expertBlackList: return app.getExpertBlackList();
				case creationDate: return app.getCreationDate();
				case submittedByStaff: return app.isSubmittedByStaff()
						? translator.translate("application.status.submittedByStaff.short") : translator.translate("application.status.submittedByApplicant.short");
				case applicationStatus: return translator.translate("application.status.".concat(app.getApplicationStatus().name()));
				case applicationStatusDate: return app.getStatusDate();
				case projectTitle: return app.getProject().getTitle();
				case projectDescription: return app.getProject().getDescription();
				case projectAcronym: return app.getProject().getAcronym();
				case projectKeywords: return app.getProject().getKeywords();
				case projectDisciplines: return app.getProject().getDisciplines();
				case projectStartDate: return app.getProject().getStartDate();
				case projectDuration: return app.getProject().getDuration();
				case projectFinancialImpact1: return recruitingModule.getApplicationProjectFinancialImpact1Type()
						.toTypedValue(app.getProject().getFinancialImpact1());
				case projectFinancialImpact2: return recruitingModule.getApplicationProjectFinancialImpact2Type()
						.toTypedValue(app.getProject().getFinancialImpact2());
				case projectFinancialImpact3: return recruitingModule.getApplicationProjectFinancialImpact3Type()
						.toTypedValue(app.getProject().getFinancialImpact3());
				case projectFinancialImpact4: return recruitingModule.getApplicationProjectFinancialImpact4Type()
						.toTypedValue(app.getProject().getFinancialImpact4());
				case projectFinancialImpact5: return recruitingModule.getApplicationProjectFinancialImpact5Type()
						.toTypedValue(app.getProject().getFinancialImpact5());
				case categories: return getCategories(app);
				case memo: return app.getMemo();
				case committeeComment: return app.getCommitteeComment();
				default: return app;
			}
		}
		
		if(col >= ApplicationAttributesDelegate.COLS_OFFSET) {
			int index = col - ApplicationAttributesDelegate.COLS_OFFSET;
			String val = app.getAdditionalValue(index);
			PositionAttributeDefinitionConfiguration type = app.getAdditionalType(index);
			Object obj = ApplicationAttributesDelegate.getLocalizedValuesWithOthers(type, val, translator.getLocale());
			if(obj instanceof String[]) {
				return SelectAdditionalAttributeCellRenderer.render((String[])obj);
			}
			return obj;
		}
		
		int definitionCol = col - ExcelFields.values().length;
		if(definitionCol >= 0 && definitionCol < definitions.size()) {
			DecisionRubricDefinition def = definitions.get(definitionCol);
			for(DecisionRubric rubric:rubrics) {
				if(rubric.getDefinition().equals(def) && rubric.getApplication().getKey().equals(app.getKey())) {
					DecisionRubricSPI spi = keyToSpies.get(def.getType());
					if(spi != null) {
						return spi.getValue(rubric);
					}
				}	
			}
		} else if(definitionCol == definitions.size()) {
			double sum = 0;
			for(DecisionRubric rubric:rubrics) {
				if(rubric.getApplication().getKey().equals(app.getKey())) {
					DecisionRubricSPI spi = keyToSpies.get(rubric.getDefinition().getType());
					if(spi != null && rubric.getDefinition().isSum()) {
						double nValue = spi.getNumericalNormalizedValue(rubric);
						sum += (nValue * rubric.getDefinition().getWeight());
					}
				}	
			}

			return sum;
		}
		return "";
	}
	
	private String getCategories(ApplicationLight app) {
		StringBuilder tags = new StringBuilder(64);
		List<ApplicationCategoryInfos> categories = appToCategories.get(app.getKey());
		if(categories != null) {
			for(ApplicationCategoryInfos category:categories) {
				if(tags.length() > 0) tags.append(", ");
				if(category.isAdministrative()) {
					tags.append("a:");
				}
				tags.append(category.getCategory().getName());
			}
		}
		return tags.toString();
	}
	
	private Integer toYear(Date date) {
		Integer year = null;
		if(date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			year = Integer.valueOf(cal.get(Calendar.YEAR));
		}
		return year;
	}
	
	public enum ExcelFields {
		id("edit.application.id"),
		title("edit.application.title"),
		firstName("edit.application.firstName"),
		lastName("edit.application.lastName"),
		birthday("edit.application.birthday"),
		gender("edit.application.gender"),
		maritalStatus("edit.application.marital.status"),
		disability("edit.application.disability"),
		language("edit.application.language"),
		academicTitle("edit.application.academicTitle"),
		numberOfOriginalPublications("edit.application.numberOfOriginalPublications"),
		numberOfFirstAuthorships("edit.application.numberOfFirstAuthorships"),
		numberOfLastAuthorships("edit.application.numberOfLastAuthorships"),
		citations("edit.application.citations"),
		impactFactor("edit.application.impactFactor"),
		hFactor("edit.application.hFactor"),
		//degree
		highestDegreeType("table.header.highestdegreetype"),
		highestDegreeYear("table.header.highestdegreeyear"),
		highestDegreeYearPhD("table.header.highestdegreeyear"),
		highestDegreeInstitution("table.header.highestdegreeinstitution"),
		workedInAcademiaSince("table.header.workedInAcademiaSince"),
		workedOutAcademiaSince("table.header.workedOutAcademiaSince"),
		workedOutAcademiaCareSince("table.header.workedOutAcademiaCareSince"),
		careerDescription("table.header.careerDescription"),
		//dissertation
		dissertationTitle("table.header.dissertationtitle"),
		dissertationDate("table.header.dissertationyear"),
		dissertationInstitution("table.header.dissertationinstitution"),
		dissertationKeyword1("table.header.dissertationkeyword1"),
		dissertationKeyword2("table.header.dissertationkeyword2"),
		dissertationKeyword3("table.header.dissertationkeyword3"),
		//habilitation
		habilitationTitle("table.header.habilitationtitle"),
		habilitationDate("table.header.habilitationyear"),
		habilitationInstitution("table.header.habilitationinstitution"),
		//orcid
		orcid("table.header.orcid"),
		//ratings
		committeeRatingA("edit.application.committee_rating.a"),
		committeeRatingB("edit.application.committee_rating.b"),
		committeeRatingC("edit.application.committee_rating.c"),
		abstention("edit.application.committee_rating.abstention"),
		decision("edit.application.decision"),
		//address columns
		nationality("edit.application.nationality"),
		additionalNationalities("edit.application.additional.nationalities"),
		mail("edit.application.mail"),
		phone("edit.application.phone"),
		mobilePhone("edit.application.mobile.phone"),
		//business information
		organization("edit.application.organization"),
		unit("edit.application.unit"),
		currentPosition("edit.application.currentPosition"),
		//private address
		addressLine1("table.header.addressLine1"),
		addressLine2("table.header.addressLine2"),
		addressLine3("table.header.addressLine3"),
		zipcode("table.header.zipcode"),
		city("table.header.city"),
		country("table.header.country"),
		//business address
		businessAddressLine1("table.header.businessAddressLine1"),
		businessAddressLine2("table.header.businessAddressLine2"),
		businessAddressLine3("table.header.businessAddressLine3"),
		businessZipcode("table.header.businessZipcode"),
		businessCity("table.header.businessCity"),
		businessCountry("table.header.businessCountry"),
		businessPhone("table.header.businessPhone"),
		businessMail("table.header.businessMail"),
		experts("table.header.experts"),
		recommendations("table.header.recommendations"),
		comparativeExperts("table.header.comparative.experts"),
		//projects
		projectTitle("table.header.project.title"),
		projectDescription("table.header.project.description"),
		projectAcronym("table.header.project.acronym"),
		projectKeywords("table.header.project.keywords"),
		projectDisciplines("table.header.project.disciplines"),
		projectStartDate("table.header.project.start.date"),
		projectDuration("table.header.project.duration"),
		projectFinancialImpact1("table.header.project.impactFactor.1"),
		projectFinancialImpact2("table.header.project.impactFactor.2"),
		projectFinancialImpact3("table.header.project.impactFactor.3"),
		projectFinancialImpact4("table.header.project.impactFactor.4"),
		projectFinancialImpact5("table.header.project.impactFactor.5"),
		//categories
		categories("table.header.categories"),
		//others
		memo("edit.application.memo"),
		committeeComment("edit.application.committee.comment"),
		ad("edit.ad"),
		expertBlackList("table.header.expert.blacklist"),
		creationDate("application.creationDate"),
		submittedByStaff("edit.application.submittedByStaff"),
		applicationStatus("table.header.application.status"),
		applicationStatusDate("table.header.application.status.date");
		
		private final String key;
		
		private ExcelFields(String key) {
			this.key = key;
		}
		
		public String key() {
			return key;
		}
	}
}