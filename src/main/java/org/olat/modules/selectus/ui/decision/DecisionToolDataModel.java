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
package org.olat.modules.selectus.ui.decision;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.DecisionRubric;
import org.olat.modules.selectus.model.DecisionRubricDefinition;
import org.olat.modules.selectus.model.HighestDegreeType;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionConfiguration;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.olat.modules.selectus.ui.components.SelectAdditionalAttributeCellRenderer;

/**
 * 
 * Initial date: 18 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DecisionToolDataModel extends DefaultFlexiTableDataModel<ApplicationRubricsRow>
	implements SortableFlexiTableDataModel<ApplicationRubricsRow>, FilterableFlexiTableModel {
	
	private static final Logger log = Tracing.createLoggerFor(DecisionToolDataModel.class);
	
	private static final RubricCols[] RUBRICS = RubricCols.values();
	public static final int RUBRIC_OFFSET = 500;
	
	private final Locale locale;
	private final boolean editable;
	private final Translator translator;
	private final Map<Long,List<ApplicationCategoryInfos>> appToCategories;
	private final List<DecisionRubricDefinition> definitions;
	
	private List<ApplicationRubricsRow> backupList = new ArrayList<>();
	
	private final RecruitingModule recruitingModule;
	
	public DecisionToolDataModel(List<ApplicationRubricsRow> rows,
			List<DecisionRubricDefinition> definitions, Map<Long,List<ApplicationCategoryInfos>> appToCategories,
			boolean editable, Translator translator, Locale locale, FlexiTableColumnModel columnsModel) {
		super(rows, columnsModel);
		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		this.locale = locale;
		this.editable = editable;
		this.translator = translator;
		this.definitions = definitions;
		this.appToCategories = appToCategories;
		backupList = new ArrayList<>(rows);
	}
	
	public List<DecisionRubricDefinition> getDefinitions() {
		return new ArrayList<>(definitions);
	}
	
	public boolean replaceRubric(DecisionRubric rubricToReplace) {
		synchronized(this) {
			List<ApplicationRubricsRow> rows = getObjects();
			for(ApplicationRubricsRow row:rows) {
				if(row.getApplication().equals(rubricToReplace.getApplication())) {
					List<ApplicationRubric> rubrics = row.getApplicationRubrics();
					for(ApplicationRubric rubric:rubrics) {
						if(rubric.getDefinition().getKey().equals(rubricToReplace.getDefinition().getKey())) {
							rubric.setRubric(rubricToReplace);
							recalculateSum(row);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	protected void recalculateSum(ApplicationRubricsRow row) {
		double sum = 0.0d;
		List<ApplicationRubric> rubrics = row.getApplicationRubrics();
		for(ApplicationRubric appRubric:rubrics) {
			if(appRubric.getRubric() != null) {
				if(appRubric.getDefinition().isSum()) {
					double nValue = appRubric.getDecisionRubricSpi().getNumericalNormalizedValue(appRubric.getRubric());
					sum += (nValue * appRubric.getDefinition().getWeight());
				}
			}
			
		}
		row.setSum(sum);
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			synchronized(this) {
				try {// catch concurrent sort / change on the data model
					List<ApplicationRubricsRow> apps = new DecisionToolSortDelegate(orderBy, this, locale).sort();
					setObjects(apps);
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(filters != null && !filters.isEmpty()) {
			final Set<String> decisions = getFilteredList(filters, DecisionToolController.FILTER_DECISION);
			
			List<ApplicationRubricsRow> filteredRows = new ArrayList<>(backupList.size());
			for(ApplicationRubricsRow row:backupList) {
				boolean accept = acceptDecision(decisions, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private Set<String> getFilteredList(List<FlexiTableFilter> filters, String filterName) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			return filterValues != null && !filterValues.isEmpty() ? Set.copyOf(filterValues) : Set.of();
		}
		return Set.of();
	}
	
	private boolean acceptDecision(Set<String> status, ApplicationRubricsRow row) {
		if(status == null || status.isEmpty()) return true;
		
		Integer decision = row.getApplication().getDecision();
		if((decision == null || decision.intValue() <= 0) && status.contains(DecisionToolController.FILTER_NULL_KEY)
				|| (decision != null && status.contains(decision.toString()))) {
			return true;
		}
		return false;
	}

	@Override
	public Object getValueAt(int row, int col) {
		ApplicationRubricsRow rubric = getObject(row);
		return getValueAt(rubric, col);
	}

	@Override
	public Object getValueAt(ApplicationRubricsRow rubric, int col) {
		if(col >= 0 && col < RUBRICS.length) {
			ApplicationLight app = rubric.getApplication();
			
			switch(RUBRICS[col]) {
				case id: return rubric.getApplication().getId();
				case title: {
					if(StringHelper.containsNonWhitespace(app.getPerson().getTitle())) {
						String title = translator.translate(app.getPerson().getTitle());
						if(title != null && title.length() < 15) {
							return title;
						}
					}
					return app.getPerson().getTitle();
				}
				case firstName: return app.getPerson().getFirstName();
				case lastName: return app.getPerson().getLastName();
				case yearOfBirth: {
					if(app.getPerson().getBirthday() != null) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(app.getPerson().getBirthday());
						return Integer.toString(cal.get(Calendar.YEAR));
					}
					return "";
				}
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
				case academicTitle: return app.getPerson().getAcademicTitle();
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
							return Formatter.truncate(app.getAcademicalBackground().getHighestDegreeDescription(), 25);
						}
						String transType = translator.translate("edit.application.degreetype." + type);
						if(transType.length() > 50) return type;
						return transType;
					}
					return null;
				}
				case highestDegreeYear: return app.getAcademicalBackground().getHighestDegreeDate();
				case highestDegreeYearPhD: {
					if(app.getAcademicalBackground().getHighestDegreeDate() == null) {
						return null;
					}
					String type = app.getAcademicalBackground().getHighestDegreeType();
					if(type != null && HighestDegreeType.phd.name().equals(type)) {
						return app.getAcademicalBackground().getHighestDegreeDate();
					}
					return "";
				}
				case highestDegreeInstitution: return app.getAcademicalBackground().getHighestDegreeInstitution();
				case workedInAcademiaSince: return app.getAcademicalBackground().getWorkedInAcademiaSince();
				case workedOutAcademiaSince: return app.getAcademicalBackground().getWorkedOutAcademiaSince();
				case workedOutAcademiaCareSince: return app.getAcademicalBackground().getWorkedOutAcademiaCareSince();
				case careerDescription: return app.getAcademicalBackground().getCareerDescription();
				case dissertationTitle:return app.getAcademicalBackground().getDissertationTitle();
				case dissertationDate: return app.getAcademicalBackground().getDissertationDate();
				case dissertationInstitution: return app.getAcademicalBackground().getDissertationInstitution();
				case dissertationKeyword1: return app.getAcademicalBackground().getDissertationKeyword1();
				case dissertationKeyword2: return app.getAcademicalBackground().getDissertationKeyword2();
				case dissertationKeyword3: return app.getAcademicalBackground().getDissertationKeyword3();
				case habilitationTitle: return app.getAcademicalBackground().getHabilitationTitle();
				case habilitationDate: return app.getAcademicalBackground().getHabilitationDate();
				case habilitationInstitution: return app.getAcademicalBackground().getHabilitationInstitution();
				case orcid: return app.getAcademicalBackground().getOrcid();
				case decision: return app.getDecision();
				case nationality: return app.getPerson().getNationality();
				case additionalNationalities: return RecruitingHelper.beautifyCountriesList(app.getPerson().getAdditionalNationalities());
				case mail: return app.getPerson().getMail();
				case phone: return app.getPerson().getPhone();
				case mobilePhone: return app.getPerson().getMobilePhone();
				case organization: return RecruitingHelper.mergeOrganizationAndAffiliation(app.getBusinessInformations());
				case unit: return app.getBusinessInformations().getUnit();
				case currentPosition: return app.getBusinessInformations().getCurrentPosition();
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
				case project: return app.getProject();
				case projectTitle: return app.getProject().getTitle();
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
				case categories: return appToCategories.get(app.getKey());
				case committeeRating: return rubric.getRatingOverviewItem();
				case sum: return rubric.getSum();
			}
		} else if(col >= ApplicationAttributesDelegate.COLS_OFFSET) {
			int index = col - ApplicationAttributesDelegate.COLS_OFFSET;
			String val = rubric.getApplication().getAdditionalValue(index);
			PositionAttributeDefinitionConfiguration type = rubric.getApplication().getAdditionalType(index);
			Object obj = ApplicationAttributesDelegate.getLocalizedValuesWithOthers(type, val, translator.getLocale());
			if(obj instanceof String[]) {
				return SelectAdditionalAttributeCellRenderer.render((String[])obj);
			}
			return obj;
		} else if(col >= RUBRIC_OFFSET) {
			int index = col - RUBRIC_OFFSET;
			ApplicationRubric appRubric = rubric.getApplicationRubric(index);
			if(appRubric != null) {
				return editable ? appRubric.getFormItem() : appRubric.getValue();
			}
		}
		return null;
	}
	
	public List<ApplicationCategoryInfos> getCategories(ApplicationRubricsRow row) {
		return appToCategories == null ? null : appToCategories.get(row.getApplication().getKey());
	}
	
	@Override
	public void setObjects(List<ApplicationRubricsRow> objects) {
		backupList = new ArrayList<>(objects);
		super.setObjects(objects);
	}
	
	public enum RubricCols implements FlexiSortableColumnDef {
		id("edit.application.id", null),
		title("edit.application.title", null),
		firstName("edit.application.firstName", null),
		lastName("edit.application.lastName", null),
		yearOfBirth("edit.application.birthday.pdf", null),
		birthday("edit.application.birthday", null),
		gender("edit.application.gender", null),
		maritalStatus("edit.application.marital.status", null),
		disability("edit.application.disability", null),
		academicTitle("edit.application.academicTitle", null),
		numberOfOriginalPublications("edit.application.numberOfOriginalPublications", null),
		numberOfFirstAuthorships("edit.application.numberOfFirstAuthorships", null),
		numberOfLastAuthorships("edit.application.numberOfLastAuthorships", null),
		citations("edit.application.citations", null),
		impactFactor("edit.application.impactFactor", null),
		hFactor("edit.application.hFactor", null),
		//degree
		highestDegreeType("table.header.highestdegreetype", RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE, RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_TYPE),
		highestDegreeYear("table.header.highestdegreeyear", RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE, RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR),
		highestDegreeYearPhD("table.header.highestdegreeyear", RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE, RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR),
		highestDegreeInstitution("table.header.highestdegreeinstitution", RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE, RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_INSTITUTION),
		workedInAcademiaSince("table.header.workedInAcademiaSince", null),
		workedOutAcademiaSince("table.header.workedOutAcademiaSince", null),
		workedOutAcademiaCareSince("table.header.workedOutAcademiaCareSince", null),
		careerDescription("table.header.careerDescription", null),
		
		//dissertation
		dissertationTitle("table.header.dissertationtitle", RecruitingModule.APP_ACADEMIC_DISSERTATION),
		dissertationDate("table.header.dissertationyear", RecruitingModule.APP_ACADEMIC_DISSERTATION),
		dissertationInstitution("table.header.dissertationinstitution", RecruitingModule.APP_ACADEMIC_DISSERTATION),
		dissertationKeyword1("table.header.dissertationkeyword1", null),
		dissertationKeyword2("table.header.dissertationkeyword2", null),
		dissertationKeyword3("table.header.dissertationkeyword3", null),
		//habilitation
		habilitationTitle("table.header.habilitationtitle", null),
		habilitationDate("table.header.habilitationyear", null),
		habilitationInstitution("table.header.habilitationinstitution", null),
		//orcid
		orcid("table.header.orcid", null),
		//ratings
		committeeRating("edit.application.committee_rating", null),
		decision("edit.application.decision", null),
		//address columns
		nationality("edit.application.nationality", null),
		additionalNationalities("edit.application.additional.nationalities", null),
		mail("edit.application.mail", null),
		phone("edit.application.phone", null),
		mobilePhone("edit.application.mobile.phone", null),
		//business informations
		organization("edit.application.organization", RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION),
		unit("edit.application.unit", RecruitingModule.APP_BUSINESS_INFOS_UNIT),
		currentPosition("edit.application.currentPosition", RecruitingModule.APP_BUSINESS_INFOS_POSITION),
		//private address
		addressLine1("table.header.addressLine1", null),
		addressLine2("table.header.addressLine2", null),
		addressLine3("table.header.addressLine3", null),
		zipcode("table.header.zipcode", null),
		city("table.header.city", null),
		country("table.header.country", null),
		//business address
		businessAddressLine1("table.header.businessAddressLine1", null),
		businessAddressLine2("table.header.businessAddressLine2", null),
		businessAddressLine3("table.header.businessAddressLine3", null),
		businessZipcode("table.header.businessZipcode", null),
		businessCity("table.header.businessCity", null),
		businessCountry("table.header.businessCountry", null),
		businessPhone("table.header.businessPhone", null),
		businessMail("table.header.businessMail", null),
		//project
		project("project", null),
		projectTitle("table.header.project.title", null),
		projectAcronym("table.header.project.acronym", null),
		projectKeywords("table.header.project.keywords", null),
		projectDisciplines("table.header.project.disciplines", null),
		projectStartDate("table.header.project.start.date", null),
		projectDuration("table.header.project.duration", null),
		projectFinancialImpact1("table.header.project.impactFactor.1", null),
		projectFinancialImpact2("table.header.project.impactFactor.2", null),
		projectFinancialImpact3("table.header.project.impactFactor.3", null),
		projectFinancialImpact4("table.header.project.impactFactor.4", null),
		projectFinancialImpact5("table.header.project.impactFactor.5", null),
		// categories
		categories("table.header.categories", null),
		
		sum("rubric.sum", null);
		
		private final String i18nKey;
		private final String group;
		private final String attribute;
		
		private RubricCols(String i18nKey, String attribute) {
			this(i18nKey, null, attribute);
		}
		
		private RubricCols(String i18nKey, String group, String attribute) {
			this.i18nKey = i18nKey;
			this.group = group;
			this.attribute = attribute;
		}
		
		public boolean visible(List<String> exclusionList) {
			if(exclusionList == null) {
				return true;
			}
			return (attribute == null || !exclusionList.contains(attribute))
					&& (group == null || !exclusionList.contains(group));
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
