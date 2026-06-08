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
package org.olat.modules.selectus.ui.rejection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.EmptyUserRating;
import org.olat.modules.selectus.model.HighestDegreeType;
import org.olat.modules.selectus.model.MailLogInfos;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionConfiguration;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.UserRatingMapper;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.olat.modules.selectus.ui.comparator.ApplicationCategoryInfosListComparator;
import org.olat.modules.selectus.ui.rating.UserMapperCommitteeRatingComparator;

/**
 * 
 * Initial date: 22.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectFormEmailDataModel extends DefaultTableDataModel<ApplicationLight>
	implements FlexiTableDataModel<ApplicationLight>, SortableFlexiTableDataModel<ApplicationLight> {
	
	private static final Fields[] FIELDS = Fields.values();
	
	private final Translator translator;
	private final List<IdentityRef> committee;
	private final List<UserRating> ratings;
	private Map<Long,List<ApplicationCategoryInfos>> categoriesMap;
	private final Map<Long, Date> lastEmail = new HashMap<>();
	private FlexiTableColumnModel columnModel;
	
	private final RecruitingModule recruitingModule;

	public SelectFormEmailDataModel(List<ApplicationLight> rows, List<MailLogInfos> log, List<UserRating> ratings,
			List<IdentityRef> committee, Map<Long,List<ApplicationCategoryInfos>> categoriesMap, Translator translator,
			FlexiTableColumnModel columnModel) {
		super(rows);
		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		this.translator = translator;
		this.ratings = ratings;
		this.committee = committee;
		this.columnModel = columnModel;
		this.categoriesMap = categoriesMap;
		
		if(log != null && !log.isEmpty()) {
			for(MailLogInfos l:log) {
				Long appKey = l.getApplication().getKey();
				Date creationDate = l.getMailLog().getCreationDate();
				if(lastEmail.containsKey(appKey)) {
					if(lastEmail.get(appKey).before(creationDate)) {
						lastEmail.put(appKey, creationDate);
					}
				} else {
					lastEmail.put(appKey, creationDate);
				}
			}
		}
	}
	
	public Translator getTranslator() {
		return translator;
	}

	@Override
	public FlexiTableColumnModel getTableColumnModel() {
		return columnModel;
	}

	@Override
	public void setTableColumnModel(FlexiTableColumnModel tableColumnModel) {
		this.columnModel = tableColumnModel;
	}

	@Override
	public boolean isSelectable(int row) {
		ApplicationLight app = getObject(row);
		String mail = app.getPerson().getMail();
		return StringHelper.containsNonWhitespace(mail);
	}
	

	//TODO selectus @Override
	public String getMultiSelectAriaLabel(int row) {
		ApplicationLight app = getObject(row);
		return app.getPerson().getLastName();
	}

	@Override
	public int getColumnCount() {
		return Fields.values().length;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ApplicationLight> views = new SelectFormEmailSortDelegate(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		ApplicationLight app = getObject(row);
		return getValueAt(app, col);
	}

	@Override
	public Object getValueAt(ApplicationLight app, int col) {
		if(col >= 0 && col < FIELDS.length) {
			Fields field = Fields.values()[col];
			switch(field) {
				case id: return app.getId();
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
				case mail: {
					String mail = app.getPerson().getMail();
					if(!StringHelper.containsNonWhitespace(mail)) {
						mail = "<span class='b_with_small_icon_left b_warn_icon'>" + translator.translate("missing.mail") + "</span>";
					}
					return mail;
				}
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
				case language: return app.getLanguage();
				case organization: return RecruitingHelper.mergeOrganizationAndAffiliation(app.getBusinessInformations());
				case unit: return app.getBusinessInformations().getUnit();
				case currentPosition: return app.getBusinessInformations().getCurrentPosition();
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
					String type = app.getAcademicalBackground().getHighestDegreeType();
					if(type != null && HighestDegreeType.phd.name().equals(type)) {
						return app.getAcademicalBackground().getHighestDegreeDate();
					}
					return null;
				}
				case highestDegreeInstitution: return app.getAcademicalBackground().getHighestDegreeInstitution();
				case dissertationTitle:return app.getAcademicalBackground().getDissertationTitle();
				case dissertationDate: return toYear(app.getAcademicalBackground().getDissertationDate());
				case dissertationInstitution: return app.getAcademicalBackground().getDissertationInstitution();
				case dissertationKeyword1: return app.getAcademicalBackground().getDissertationKeyword1();
				case dissertationKeyword2: return app.getAcademicalBackground().getDissertationKeyword2();
				case dissertationKeyword3: return app.getAcademicalBackground().getDissertationKeyword3();
				case habilitationTitle: return app.getAcademicalBackground().getHabilitationTitle();
				case habilitationDate: return toYear(app.getAcademicalBackground().getHabilitationDate());
				case habilitationInstitution: return app.getAcademicalBackground().getHabilitationInstitution();
				case orcid: return app.getAcademicalBackground().getOrcid();
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
				case projectDescription: return app.getProject().getDescription();
				case categories: return categoriesMap.get(app.getKey()); 
				case committeeRating: return getUserRatingMapper(app);
				case decision: return app.getDecision();
				case emailed: return lastEmail.containsKey(app.getKey());
				case emailDate: {
					Date dateOfLastEmail = null;
					if(lastEmail != null) {
						dateOfLastEmail = lastEmail.get(app.getKey());
					}
					return dateOfLastEmail;
				}
				case applicationStatus: return translator.translate("application.status.".concat(app.getApplicationStatus().name()));
				default: return app;
			}
		}
		
		if(col >= ApplicationAttributesDelegate.COLS_OFFSET) {
			int index = col - ApplicationAttributesDelegate.COLS_OFFSET;
			String val = app.getAdditionalValue(index);
			PositionAttributeDefinitionConfiguration config = app.getAdditionalType(index);
			return ApplicationAttributesDelegate.getLocalizedValuesWithOthers(config, val, translator.getLocale());
		}
		
		return "ERROR";
	}
	
	public List<ApplicationCategoryInfos> getCategories(ApplicationLight app) {
		return categoriesMap == null ? Collections.emptyList() : categoriesMap.get(app.getKey());
	}
	
	private UserRatingMapper getUserRatingMapper(ApplicationLight app) {
		UserRatingMapper mapper = new UserRatingMapper(app);
		
		String resSubPath = app.getKey().toString();
		List<UserRating> appRatings = new ArrayList<>();
		for(IdentityRef member:committee) {
			UserRating memberRating = null;
			for(UserRating rating:ratings) {
				if(resSubPath.equals(rating.getResSubPath()) && member.getKey().equals(rating.getCreator().getKey())) {
					memberRating = rating;
					break;
				}
			}
			if(memberRating == null) {
				memberRating = new EmptyUserRating(member);
			}
			appRatings.add(memberRating);
		}
		mapper.setRatings(appRatings);
		return mapper;
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
	
	public enum Fields implements FlexiSortableColumnDef {
		id("edit.application.id", null),
		title("edit.application.title", null),
		firstName("edit.application.firstName", null),
		lastName("edit.application.lastName", null),
		mail("edit.application.mail", null),
		yearOfBirth("edit.application.birthday.pdf", null),
		birthday("edit.application.birthday", null),
		gender("edit.application.gender", null),
		language("edit.application.language", null),
		organization("edit.application.organization", RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION),
		unit("edit.application.unit", RecruitingModule.APP_BUSINESS_INFOS_UNIT),
		currentPosition("edit.application.currentPosition", RecruitingModule.APP_BUSINESS_INFOS_POSITION),
		//highest degree
		highestDegreeType("table.header.highestdegreetype", RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE, RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_TYPE),
		highestDegreeYear("table.header.highestdegreeyear", RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE, RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR),
		highestDegreeYearPhD("table.header.highestdegreeyear", RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE, RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR),
		highestDegreeInstitution("table.header.highestdegreeinstitution", RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE, RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_INSTITUTION),
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
		//project
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
		projectDescription("table.header.project.description", null),
		// categories
		categories("table.header.categories", null),
		applicationStatus("table.header.application.status", null),
		//address columns
		emailed("rejection.mail", null),
		emailDate("rejection.mail.date", null);
		
		private final String key;
		private final String group;
		private final String attribute;
		
		private Fields(String key, String attribute) {
			this(key, null, attribute);
		}
		
		private Fields(String key, String group, String attribute) {
			this.key = key;
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
			return key;
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
	
	private static class SelectFormEmailSortDelegate extends SortableFlexiTableModelDelegate<ApplicationLight> {

		private final SelectFormEmailDataModel selectTableModel;
		private final LastnameComparator lastnameComparator = new LastnameComparator();
		
		public SelectFormEmailSortDelegate(SortKey orderBy, SelectFormEmailDataModel tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
			selectTableModel = tableModel;
		}
		
		@Override
		protected void sort(List<ApplicationLight> rows) {
			int columnIndex = getColumnIndex();
			if(columnIndex >= 0 && columnIndex < FIELDS.length) {
				Fields column = FIELDS[columnIndex];
				switch(column) {
					case committeeRating: Collections.sort(rows, new ApplicationLightCommitteeRatingComparator()); break;
					case categories: Collections.sort(rows, new ApplicationCategoriesComparator(isAsc())); break;
					default: super.sort(rows);
				}
			} else if(columnIndex >= ApplicationAttributesDelegate.COLS_OFFSET) {
				super.sort(rows);
			}
		}
		
		private class ApplicationCategoriesComparator implements Comparator<ApplicationLight> {
			
			private final ApplicationCategoryInfosListComparator comparator;
			
			public ApplicationCategoriesComparator(boolean asc) {
				comparator = new ApplicationCategoryInfosListComparator(asc);
			}

			@Override
			public int compare(ApplicationLight o1, ApplicationLight o2) {
				if(o1 == null || o2 == null) {
					return compareNullObjects(o1, o2);
				}
				
				List<ApplicationCategoryInfos> c1 = selectTableModel.getCategories(o1);
				List<ApplicationCategoryInfos> c2 = selectTableModel.getCategories(o2);
				int c = comparator.compare(c1, c2);
				if(c == 0) {
					c = lastnameComparator.compare(o1, o2);
				}
				return c;
			}
		}
		
		private class ApplicationLightCommitteeRatingComparator implements Comparator<ApplicationLight> {

			private final UserMapperCommitteeRatingComparator committeeRatingComparator = new UserMapperCommitteeRatingComparator();

			@Override
			public int compare(ApplicationLight o1, ApplicationLight o2) {
				UserRatingMapper u1 = selectTableModel.getUserRatingMapper(o1);
				UserRatingMapper u2 = selectTableModel.getUserRatingMapper(o2);
				return committeeRatingComparator.compare(u1, u2);
			}
		}
		
		private class LastnameComparator implements Comparator<ApplicationLight> {

			@Override
			public int compare(ApplicationLight a1, ApplicationLight a2) {
				if(a1 == null || a2 == null) {
					return compareNullObjects(a1, a2);
				}
				
				Person p1 = a1.getPerson();
				Person p2 = a2.getPerson();
				if(p1 == null || p2 == null) {
					return compareNullObjects(p1, p2);
				}
				
				String l1 = p1.getLastName();
				String l2 = p2.getLastName();
				if(l1 == null) return 1;
				if(l2 == null) return -1;
				int result = l1.compareToIgnoreCase(l2);
				if(result == 0) {
					String f1 = p1.getFirstName();
					String f2 = p2.getFirstName();
					return f1.compareToIgnoreCase(f2);
				}
				return result;
			}
		}
	}
}