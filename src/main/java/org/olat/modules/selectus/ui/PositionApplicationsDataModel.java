/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.HighestDegreeType;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.Project;
import org.olat.modules.selectus.model.application.ParallelApplication;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.SelectAdditionalAttributeCellRenderer;
import org.olat.modules.selectus.ui.events.SelectPositionLightEvent;
import org.olat.modules.selectus.ui.main.ExportTableDataModel;
import org.olat.modules.selectus.ui.model.AppToCategory;
import org.olat.modules.selectus.ui.model.ApplicationRow;
import org.olat.modules.selectus.ui.rating.RatingsOverviewFormItem;

/**
 * 
 * Description:<br>
 * The data model for the controller
 * 
 * <P>
 * Initial Date:  8 avr. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionApplicationsDataModel extends DefaultFlexiTableDataModel<ApplicationRow>
	implements SortableFlexiTableDataModel<ApplicationRow>, FilterableFlexiTableModel,
	 ExportTableDataModel<ApplicationRow>, FlexiBusinessPathModel {
	
	private static final Logger log = Tracing.createLoggerFor(PositionApplicationsDataModel.class);
	private static final Fields[] FIELDS = Fields.values();

	public final Object decisionLock = new Object();
	
	private final Position position;
	private final Identity identity;
	private final Translator translator;

	private List<UserRating> ratings;
	private List<ApplicationRow> backupRows;
	
	private int[] exportedColumnIndex;
	
	private final RecruitingModule recruitingModule;
	private final RecruitingPositionSecurityCallback secCallback;

	public PositionApplicationsDataModel(Identity identity, Position position, RecruitingPositionSecurityCallback secCallback,
			Translator translator,
			FlexiTableColumnModel columnsModel) {
		super(new ArrayList<>(), columnsModel);
		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		this.position = position;
		this.identity = identity;
		this.translator = translator;
		this.secCallback = secCallback;
	}
	
	public Translator getTranslator() {
		return translator;
	}
	
	public int getApplicationIndex(ApplicationRef app) {
		List<ApplicationRow> rows = getObjects();
		int index = -1;
		for(ApplicationRow row:rows) {
			index++;
			if(app.getKey().equals(row.getApplication().getKey())) {
				break;
			}
		}
		return index;
	}
	
	/**
	 * Search the row in the backup list, not only in the
	 * visible ones.
	 * 
	 * @param app The application reference
	 * @return The row
	 */
	public ApplicationRow getApplicationRow(ApplicationRef app) {
		List<ApplicationRow> rows = new ArrayList<>(backupRows);
		ApplicationRow appRow = null;
		for(ApplicationRow row:rows) {
			if(app.getKey().equals(row.getApplication().getKey())) {
				appRow = row;
				break;
			}
		}
		return appRow;
	}
	
	public void setData(List<ApplicationRow> rows, List<UserRating> ratings) {
		setObjects(rows);
		this.ratings = ratings;
	}

	@Override
	public int[] getExportColumnIndex() {
		return exportedColumnIndex;
	}
	
	public void setExportColumnIndex(int[] index) {
		exportedColumnIndex = index;
	}

	@Override
	public String getHeader(int col) {
		if(col >= 0 && col < FIELDS.length) {
			Fields field = FIELDS[col];
			return translator.translate(field.i18nHeaderKey());
		}
		
		FlexiColumnModel colModel = getTableColumnModel().getColumnModelByIndex(col);
		if(colModel != null && StringHelper.containsNonWhitespace(colModel.getHeaderLabel())) {
			return colModel.getHeaderLabel();
		}
		return "ERROR";
	}

	@Override
	public String getFieldNameAt(int col) {
		if(col >= 0 && col < FIELDS.length) {
			Fields field = Fields.values()[col];
			return field.name();
		}
		
		FlexiColumnModel colModel = getTableColumnModel().getColumnModelByIndex(col);
		if(colModel != null && StringHelper.containsNonWhitespace(colModel.getHeaderLabel())) {
			return colModel.getHeaderLabel();
		}
		return "ERROR";
	}

	@Override
	public Class<?> getTypeAt(int row, int col) {
		if(col >= 0 && col < FIELDS.length) {
			Fields field = FIELDS[col];
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
				case birthday: return Date.class;
				case applicationStatusDate: return Date.class;
				case submittedDate: return Date.class;
				case project: return Boolean.class;
				case projectStartDate: return Date.class;
				case projectFinancialImpact1: return recruitingModule.getApplicationProjectFinancialImpact1Type().toClass();
				case projectFinancialImpact2: return recruitingModule.getApplicationProjectFinancialImpact2Type().toClass();
				case projectFinancialImpact3: return recruitingModule.getApplicationProjectFinancialImpact3Type().toClass();
				case projectFinancialImpact4: return recruitingModule.getApplicationProjectFinancialImpact4Type().toClass();
				case projectFinancialImpact5: return recruitingModule.getApplicationProjectFinancialImpact5Type().toClass();
				default: return String.class;
			}
		}
		
		return String.class;
	}

	@Override
	public void setObjects(List<ApplicationRow> objects) {
		this.backupRows = objects;
		super.setObjects(objects);
	}

	@Override
	public void filter(String searchString,  List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || (filters != null && !filters.isEmpty())) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();
			//final Set<String> decisions = getFilteredList(filters, PositionApplicationsController.FILTER_DECISION);
			final Set<String> assignees = getFilteredList(filters, PositionApplicationsController.FILTER_ASSIGNEE);
			final Set<String> myRating = getFilteredList(filters, PositionApplicationsController.FILTER_MY_RATING);
			final Set<String> withSentEmails = getFilteredList(filters, PositionApplicationsController.FILTER_WITH_SENT_EMAILS);
			final Set<String> withoutSentEmails = getFilteredList(filters, PositionApplicationsController.FILTER_WITHOUT_SENT_EMAILS);
			final List<FieldFilter> fieldsFilters = getFilteredField(filters);
			
			List<ApplicationRow> filteredRows = new ArrayList<>(backupRows.size());
			for(ApplicationRow row:backupRows) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptMyRating(myRating, row)
						&& acceptAssignee(assignees, row)
						&& acceptWithSentEmails(withSentEmails, row)
						&& acceptWithoutSentEmails(withoutSentEmails, row)
						&& acceptFieldFilters(fieldsFilters, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupRows);
		}
	}
	
	private record FieldFilter(Fields field, Set<String> set) {
		//
	}
	
	private List<FieldFilter> getFilteredField(List<FlexiTableFilter> filters) {
		List<FieldFilter> fieldFilter = new ArrayList<>();
		
		for(FlexiTableFilter filter:filters) {
			if(Fields.isValue(filter.getFilter())) {
				Fields field = Fields.valueOf(filter.getFilter());
				FieldFilter values = getFilteredField(filter, field);
				if(values != null) {
					fieldFilter.add(values);
				}
			}
		}
		
		return fieldFilter;
	}
	
	private FieldFilter getFilteredField(FlexiTableFilter filter, Fields field) {
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			Set<String> set =  filterValues != null && !filterValues.isEmpty() ? Set.copyOf(filterValues) : Set.of();
			return new FieldFilter(field, set);
		}
		return null;
	}
	
	private Set<String> getFilteredList(List<FlexiTableFilter> filters, String filterName) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			return filterValues != null && !filterValues.isEmpty() ? Set.copyOf(filterValues) : Set.of();
		}
		return Set.of();
	}
	
	private boolean acceptWithSentEmails(Set<String> mails, ApplicationRow row) {
		if(mails == null || mails.isEmpty()) return true;
		
		String[] sentTemplates = row.getSentEmailTemplates();
		if(sentTemplates != null && sentTemplates.length > 0) {
			for(String sentTemplate:sentTemplates) {
				if(mails.contains(sentTemplate)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean acceptWithoutSentEmails(Set<String> mails, ApplicationRow row) {
		if(mails == null || mails.isEmpty()) return true;
		
		String[] sentTemplates = row.getSentEmailTemplates();
		if(sentTemplates != null && sentTemplates.length > 0) {
			for(String sentTemplate:sentTemplates) {
				if(mails.contains(sentTemplate)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean acceptAssignee(Set<String> assignees, ApplicationRow row) {
		if(assignees == null || assignees.isEmpty()) return true;

		if(row.getNumOfAssignments() > 0) {
			String[] assigneesKeys = row.getAssigneeKeysArray();
			if(assigneesKeys != null && assigneesKeys.length > 0) {
				for(String assigneeKey:assigneesKeys) {
					if(assignees.contains(assigneeKey)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean acceptMyRating(Set<String> ratings, ApplicationRow row) {
		if(ratings == null || ratings.isEmpty()) return true;
		
		String stringuifiedRating = getMyRating(row);
		if(PositionApplicationsController.FILTER_NULL_KEY.equals(stringuifiedRating)) {
			if(ratings.contains(PositionApplicationsController.FILTER_NULL_KEY)) {
				// Application with decision cannot be rated
				Integer decision = row.getDecision();
				return decision == null;
			}
			return false;
		}
		
		return ratings.contains(stringuifiedRating);
	}
	
	private String getMyRating(ApplicationRow row) {
		UserRating rating = row.getCurrentRating();
		if(rating == null || rating.getRating() == null) {
			return PositionApplicationsController.FILTER_NULL_KEY;
		}
		int currentRating = rating.getRating().intValue();
		return switch(currentRating) {
			case 3 -> "A";
			case 2 -> "B";
			case 1 -> "C";
			case -32 -> PositionApplicationsController.FILTER_ABSTAIN_KEY;
			default -> PositionApplicationsController.FILTER_NULL_KEY;
		};
	}
	
	private boolean acceptFieldFilters(List<FieldFilter> fieldsFilters, ApplicationRow row) {
		if(fieldsFilters == null || fieldsFilters.isEmpty()) return true;
		
		boolean allOk = true;
		for(FieldFilter fieldFilter:fieldsFilters) {
			if(fieldFilter.set() != null) {
				allOk &= acceptField(fieldFilter.set(), row, fieldFilter.field()); 
			}
		}
		
		return allOk;
	}
	
	private boolean acceptField(Set<String> searchValues, ApplicationRow row, Fields field) {
		if(searchValues == null || searchValues.isEmpty()) return true;
		
		Object val = getRawValueAt(row, field.ordinal());
		if(val == null) {
			return searchValues.contains(PositionApplicationsController.FILTER_NULL_KEY);
		}
		if(val instanceof String str) {
			if(StringHelper.containsNonWhitespace(str)) {
				return searchValues.contains(str);
			}
			return searchValues.contains(PositionApplicationsController.FILTER_NULL_KEY);
		}
		return false;
	}
	
	private boolean accept(String searchValue, ApplicationRow row) {
		if(searchValue == null) return true;
		
		return accept(searchValue, row.getApplication().getPerson().getFirstName())
				|| accept(searchValue, row.getApplication().getPerson().getLastName())
				|| accept(searchValue, row.getApplication().getPerson().getMail());
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}
	
	public void reset() {
		super.setObjects(backupRows);
	}

	protected static final String toYear(Object val) {
		if(val instanceof Date) {
			Date date = (Date)val;
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			return Integer.toString(cal.get(Calendar.YEAR));
		}
		return val == null ? null : val.toString();
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			try {
				List<ApplicationRow> apps = new PositionApplicationsSortDelegate(orderBy, this, translator.getLocale()).sort();
				super.setObjects(apps);
			} catch (IllegalArgumentException e) {
				log.error("Cannot sort: {}", orderBy.getKey(), e);
			}
		}
	}

	public String getRowCssClass(int row) {
		StringBuilder sb = new StringBuilder(32);
		ApplicationRow appRow = getObject(row);
		ApplicationLight app = appRow.getApplication();
		if(app.getDecision() != null && app.getDecision() > 0) {
			int decision = app.getDecision().intValue();
			switch(decision) {
				case 1: sb.append("fx_r_c_decision"); break;
				case 2: sb.append("fx_r_b_decision"); break;
				case 3: sb.append("fx_r_a_decision"); break;
				default: break;
			}
		}
		sb.append(" fx_r_".concat(app.getApplicationStatus().name()));
		return sb.toString();
	}

	@Override
	public int getColumnCount() {
		return Fields.values().length;
	}

	@Override
	public Object getValueAt(int row, int col) {
		ApplicationRow app = getObject(row);
		return getValueAt(app, col);
	}
	
	/* TODO selectus
	@Override
	public String getMultiSelectAriaLabel(int row) {
		ApplicationRow app = getObject(row);
		Integer id = app.getApplication().getId();
		return id == null ? null : id.toString();
	}
	*/

	@Override
	public Object getValueForExportAt(int row, int col) {
		ApplicationRow app = getObject(row);
		if(col >= 0 && col < FIELDS.length) {
			Fields field = FIELDS[col];
			switch(field) {
				case decision: return ratingToString(app.getDecision());
				case experts: return app.getNumOfSubmittedExperts();
				case recommendations: return app.getNumOfSubmittedRecommendations();
				case comparativeExperts: return app.getNumOfSubmittedComparativeExperts();
				case providedExpertsRecommendations: return app.getTotalSubmitted();
				case categories: return categoriesToString(app);
				case committeeRating: return getCommitteeRatingAsString(app.getRatingOverviewItem());
				case applicationStatus: return translator.translate("application.status.".concat(app.getApplication().getApplicationStatus().name()));
				case applicationStatusDate: return app.getApplication().getStatusDate();
				case dissertationDate: return toYear(app.getApplication().getAcademicalBackground().getDissertationDate());
				case habilitationDate: return toYear(app.getApplication().getAcademicalBackground().getHabilitationDate());
				case highestDegreeYear: return toYear(app.getApplication().getAcademicalBackground().getHighestDegreeDate());
				case highestDegreeYearPhD: return toYear(getHighestDegreeYearPhD(app.getApplication()));
				case parallelApplications: return parallelApplicationsToString(app.getParallelApplications());
				default: return getValueAt(app, col);
			}
		}
		
		if(col >= ApplicationAttributesDelegate.COLS_OFFSET) {
			int index = col - ApplicationAttributesDelegate.COLS_OFFSET;
			Object obj = app.getAdditionalValue(index);
			if(obj instanceof String) {
				return obj;
			} else if(obj instanceof String[]) {
				return SelectAdditionalAttributeCellRenderer.render((String[])obj);
			}
			return obj;
		}
		
		return getValueAt(app, col);
	}
	
	private String parallelApplicationsToString(List<ParallelApplication> parallelApplications) {
		StringBuilder apps = new StringBuilder();
		if(parallelApplications != null && !parallelApplications.isEmpty()) {
			for(ParallelApplication app:parallelApplications) {
				if(apps.length() > 0) {
					apps.append(", ");
				}
				PositionLight position = app.getPosition();
				if(StringHelper.containsNonWhitespace(position.getPlaningsNumber())) {
					apps.append(position.getPlaningsNumber()).append(": ");
				}
				String title = position.getMLTitle(translator.getLocale());
				if(StringHelper.containsNonWhitespace(title)) {
					apps.append(title);
				}
			}
		}
		return apps.toString();
	}
	
	private String categoriesToString(ApplicationRow appRow) {
		StringBuilder tags = new StringBuilder(32);
		List<AppToCategory> categories = appRow.getCategories();
		if(categories != null) {
			for(AppToCategory category:categories) {
				if(tags.length() > 0) tags.append(", ");
				if(category.isAdministrative()) {
					tags.append("a:");
				}
				tags.append(category.getCategoryName());
			}
		}
		return tags.toString();
	}
	
	private String getCommitteeRatingAsString(RatingsOverviewFormItem ratingItem) {
		StringBuilder sb = new StringBuilder(128);
		if(ratingItem != null && ratingItem.getRatings() != null) {
			for(UserRating rating:ratingItem.getRatings()) {
				if(rating.getRating() != null && rating.getRating().intValue() > 0) {
					if(sb.length() > 0) sb.append(",");
					sb.append(ratingToString(rating.getRating()));
				}
			}	
		}
		return sb.toString();
	}
	
	private String ratingToString(Integer rating) {
		if(rating == null) return "";
		
		switch(rating) {
			case 1: return "C";
			case 2: return "B";
			case 3: return "A";
			default: return "";
		}
	}

	@Override
	public String getUrl(Component source, Object object, String action) {
		if(SelectPositionLightEvent.SELECT_POSITION.equals(action) && object instanceof ApplicationRow) {
			return ((ApplicationRow)object).getUrl();
		}
		return null;
	}
	
	public Object getRawValueAt(ApplicationRow appRow, int col) {
		ApplicationLight app = appRow.getApplication();
		if(col >= 0 && col < FIELDS.length) {
			return switch(FIELDS[col]) {
				case applicationStatus -> app.getApplicationStatus().name();
				case decision -> app.getDecision() == null ? null : app.getDecision().toString();
				default -> getValueAt(appRow, col);
			};
		}
		
		return getValueAt(appRow, col);
	}

	@Override
	public Object getValueAt(ApplicationRow appRow, int col) {
		ApplicationLight app = appRow.getApplication();
		if(col >= 0 && col < FIELDS.length) {
			switch(FIELDS[col]) {
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
				case yearOfBirth: {
					if(app.getPerson().getBirthday() != null) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(app.getPerson().getBirthday());
						return Integer.valueOf(cal.get(Calendar.YEAR));
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
				case highestDegreeYearPhD: return getHighestDegreeYearPhD(app);
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
				case reviews: return appRow.getNumOfReviews() < 0 ? 0 : appRow.getNumOfReviews();
				case reviewButton: return appRow.getReviewButton();
				case myRating: {
					if(appRow.getRatingItem() != null && appRow.isAllowed()) {
						return appRow.getRatingItem();
					}
					return appRow.getCurrentRating();
				}
				case committeeRating: return secCallback.canSeeCommitteeRatings() ? appRow.getRatingOverviewItem() : null;
				case decision: return appRow.getDecision();
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
				case experts: return appRow.getRefereesStats();
				case recommendations: return appRow.getRefereesStats();
				case comparativeExperts: return appRow.getRefereesStats();
				case providedExpertsRecommendations: return appRow.getTotalSubmitted();
				case ad: return app.getJobAd();
				case project: return Boolean.valueOf(hasProject(app));
				case projectTitle: return app.getProject().getTitle();
				case projectAcronym: return app.getProject().getAcronym();
				case projectKeywords: return app.getProject().getKeywords();
				case projectDisciplines: return app.getProject().getDisciplines();
				case projectStartDate: return app.getProject().getStartDate();
				case projectDuration: return app.getProject().getDuration();
				case projectFinancialImpact1: return recruitingModule.getApplicationProjectFinancialImpact1Type()
						.toTypedValue(app.getProject().getFinancialImpact1());
				case projectFinancialImpact2: return recruitingModule.getApplicationProjectFinancialImpact1Type()
						.toTypedValue(app.getProject().getFinancialImpact2());
				case projectFinancialImpact3: return recruitingModule.getApplicationProjectFinancialImpact1Type()
						.toTypedValue(app.getProject().getFinancialImpact3());
				case projectFinancialImpact4: return recruitingModule.getApplicationProjectFinancialImpact1Type()
						.toTypedValue(app.getProject().getFinancialImpact4());
				case projectFinancialImpact5: return recruitingModule.getApplicationProjectFinancialImpact1Type()
						.toTypedValue(app.getProject().getFinancialImpact5());
				case projectDescription: return app.getProject().getDescription();
				case assignments: return appRow.getNumOfAssignments();
				case categories: return appRow.getCategories();
				case memo: return app.getMemo();
				case committeeComment: return app.getCommitteeComment();
				case notes: return getNotes(appRow);
				case submittedDate: return app.getCreationDate();
				case submittedByStaff: return app.isSubmittedByStaff()
						? translator.translate("application.status.submittedByStaff.short") : translator.translate("application.status.submittedByApplicant.short");
				case applicationStatus: return translator.translate("application.status.".concat(app.getApplicationStatus().name()));
				case applicationStatusDate: return app.getStatusDate();
				case parallelApplications: return appRow.getParallelApplications();
				case withdrawn: return app.getApplicationStatus() == ApplicationStatus.withdrawn ? DateCellRenderer.format(app.getWithdrawnDate()) : null;
				case withoutSentEmails:
				case withSentEmails: return appRow.getSentEmailTemplates();
				case withoutCEmails: return appRow.getSentEmailTemplates() == null || appRow.getSentEmailTemplates().length == 0;
				default: return app;
			}
		}
		
		if(col >= ApplicationAttributesDelegate.COLS_OFFSET) {
			int index = col - ApplicationAttributesDelegate.COLS_OFFSET;
			return appRow.getAdditionalValue(index);
		}
		return "ERROR";
	}
	
	private Date getHighestDegreeYearPhD(ApplicationLight app)  {
		if(app.getAcademicalBackground().getHighestDegreeDate() == null) {
			return null;
		}
		String type = app.getAcademicalBackground().getHighestDegreeType();
		if(type != null && HighestDegreeType.phd.name().equals(type)) {
			return app.getAcademicalBackground().getHighestDegreeDate();
		}
		return null;
	}
	
	private boolean hasProject(ApplicationLight app) {
		Project project = app.getProject();
		return project != null && (StringHelper.containsNonWhitespace(project.getTitle())
				|| StringHelper.containsNonWhitespace(project.getFinancialImpact1())
				|| StringHelper.containsNonWhitespace(project.getDescription()));
	}
	
	private String getNotes(ApplicationRow appRow) {
		String notes = null;
		if(appRow.getNotes() != null) {
			String content = appRow.getNotes().getContent();
			if(StringHelper.containsNonWhitespace(content)) {
				notes = Formatter.escWithBR(content).toString();
			}
		}
		return notes;
	}
	
	public void setDecision(Long applicationKey, int decision) {
		List<ApplicationRow> rows = getObjects();
		for(ApplicationRow row:rows) {
			if(row.getApplication().getKey().equals(applicationKey)) {
				row.setDecision(decision);
				if(row.getReviewButton() != null) {
					row.getReviewButton().setVisible(decision < 1);
				}
			}
		}
	}
	
	@Override
	public PositionApplicationsDataModel createCopyWithEmptyList() {
		PositionApplicationsDataModel copy = new PositionApplicationsDataModel(identity, position, secCallback,
				translator, getTableColumnModel());
		List<UserRating> ratingsCopy = ratings == null ? new ArrayList<>() : new ArrayList<>(ratings);
		copy.setData(new ArrayList<>(), ratingsCopy);
		return copy;
	}
	
	public enum Fields implements FlexiSortableColumnDef {
		id("edit.application.id", null),
		title("edit.application.title", null),
		firstName("edit.application.firstName", null, null),
		lastName("edit.application.lastName", null, null),
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
		workedInAcademiaSince("table.header.workedInAcademiaSince", null, "yearsInAcademia"),
		workedOutAcademiaSince("table.header.workedOutAcademiaSince", null, "yearsOutsideAcademia"),
		workedOutAcademiaCareSince("table.header.workedOutAcademiaCareSince", null, "yearsCare"),
		careerDescription("table.header.careerDescription", null),
		//dissertation
		dissertationTitle("table.header.dissertationtitle", RecruitingModule.APP_ACADEMIC_DISSERTATION),
		dissertationDate("table.header.dissertationyear", RecruitingModule.APP_ACADEMIC_DISSERTATION, "dissertationYear"),
		dissertationInstitution("table.header.dissertationinstitution", RecruitingModule.APP_ACADEMIC_DISSERTATION),
		dissertationKeyword1("table.header.dissertationkeyword1", null),
		dissertationKeyword2("table.header.dissertationkeyword2", null),
		dissertationKeyword3("table.header.dissertationkeyword3", null),
		
		//habilitation
		habilitationTitle("table.header.habilitationtitle", null),
		habilitationDate("table.header.habilitationyear", null, "habilitationYear"),
		habilitationInstitution("table.header.habilitationinstitution", null),
		//orcid
		orcid("table.header.orcid", null, null),
		//reviews
		reviews("table.header.reviews", null),
		reviewButton("table.header.review.button", null, null),
		//ratings
		myRating("edit.application.my_rating", null),
		committeeRating("edit.application.committee_rating", null, null),
		decision("edit.application.decision", null),
		//address columns
		nationality("edit.application.nationality", null),
		additionalNationalities("edit.application.additional.nationalities", null),
		mail("edit.application.mail", null, "email"),
		phone("edit.application.phone", null, null),
		mobilePhone("edit.application.mobile.phone", null, null),
		//business informations
		organization("edit.application.organization", RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION),
		unit("edit.application.unit", RecruitingModule.APP_BUSINESS_INFOS_UNIT),
		currentPosition("edit.application.currentPosition", RecruitingModule.APP_BUSINESS_INFOS_POSITION),
		//private address
		addressLine1("table.header.addressLine1", null, null),
		addressLine2("table.header.addressLine2", null, null),
		addressLine3("table.header.addressLine3", null, null),
		zipcode("table.header.zipcode", null, null),
		city("table.header.city", null),
		country("table.header.country", null),
		//business address
		businessAddressLine1("table.header.businessAddressLine1", null, null),
		businessAddressLine2("table.header.businessAddressLine2", null, null),
		businessAddressLine3("table.header.businessAddressLine3", null, null),
		businessZipcode("table.header.businessZipcode", null, null),
		businessCity("table.header.businessCity", null),
		businessCountry("table.header.businessCountry", null),
		businessPhone("table.header.businessPhone", null),
		businessMail("table.header.businessMail", null),
		//referees
		experts("table.header.experts", null),
		recommendations("table.header.recommendations", null, "referees"),
		comparativeExperts("table.header.comparative.experts", null),
		providedExpertsRecommendations("table.header.provided.experts.recommendations", null),
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
		projectDescription("table.header.project.description", null),
		//tags
		categories("table.header.categories", null, "tags"),
		ad("edit.ad", null),
		notes("edit.notes", null),
		memo("see.memo", null),
		committeeComment("see.committee.comment", null),
		submittedDate("edit.application.submittedDate", null),
		submittedByStaff("edit.application.submittedByStaff", null, "submittedBy"),
		applicationStatus("table.header.application.status", null),
		applicationStatusDate("table.header.application.status.date", null),
		parallelApplications("table.header.parallel.applications", null),
		edit("", null, null),
		delete("", null, null),
		assignments("table.header.assignments.stats", null),
		
		// additional fields for search
		withoutSentEmails("filter.not.sent.email", null, "withoutSentEmails"),
		withoutCEmails("filter.c.email", null, "withoutCEmails"),
		withSentEmails("filter.sent.email", null, "withSentEmails"),
		assignee("filter.assignments", null),
		withdrawn("edit.application.withdrawn", null),
		myAssignment("", null, null)
		;
		
		private final String key;
		private final String group;
		private final String attribute;
		
		private Fields(String key, String attribute) {
			this.key = key;
			this.group = null;
			this.attribute = attribute;
		}
		
		private Fields(String key, String group, String attribute) {
			this.key = key;
			this.group = group;
			this.attribute = attribute;
		}
		
		public String key() {
			return key;
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
		
		public static final boolean isValue(String val) {
			for(Fields f:values()) {
				if(f.name().equals(val)) {
					return true;
				}
			}
			return false;
		}
	}
}