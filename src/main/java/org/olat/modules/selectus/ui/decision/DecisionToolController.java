/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.decision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.selectus.AddressOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.manager.DecisionRubricSPI;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.DecisionRubric;
import org.olat.modules.selectus.model.DecisionRubricDefinition;
import org.olat.modules.selectus.model.EmptyUserRating;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.AcademicalDateFormat;
import org.olat.modules.selectus.ui.BatchDecisionController;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.UserRatingMapper;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.olat.modules.selectus.ui.components.AcademicalDateCellRenderer;
import org.olat.modules.selectus.ui.components.CategoriesCellRenderer;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.DecisionCellRenderer;
import org.olat.modules.selectus.ui.components.DisabilityCellRenderer;
import org.olat.modules.selectus.ui.components.GenderCellRenderer;
import org.olat.modules.selectus.ui.components.SumCellRenderer;
import org.olat.modules.selectus.ui.decision.DecisionToolDataModel.RubricCols;
import org.olat.modules.selectus.ui.events.DecisionEvent;
import org.olat.modules.selectus.ui.events.DecisionRubricDefinitionEvent;
import org.olat.modules.selectus.ui.events.DecisionRubricEvent;
import org.olat.modules.selectus.ui.events.FinalDecisionChangeEvent;
import org.olat.modules.selectus.ui.rating.RatingComparator;
import org.olat.modules.selectus.ui.rating.RatingsOverviewFormItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DecisionToolController extends FormBasicController implements FlexiTableCssDelegate, GenericEventListener {

	protected static final String FILTER_DECISION = "decision";
	protected static final String FILTER_NULL_KEY = "NULL";
	
	private final RatingComparator ratingComparator = new RatingComparator();
	
	private FlexiTableElement tableEl;
	private DecisionToolDataModel dataModel;
	private FormLink createRubrikButton;
	private FormLink batchDecisionButton;
	private final FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

	private CloseableModalController cmc;
	private BatchDecisionController batchDecisionCtrl;
	private DecisionRubricEditorController decisionEditorCtrl;
	
	private final ApplicationAttributesDelegate projectAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.project);
	private final ApplicationAttributesDelegate personalDataAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.personalData);
	private final ApplicationAttributesDelegate academicalBackgroundAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.academicalBackground);
	private final List<ApplicationAttributesDelegate> customTabAttributesDelegates
		= new ArrayList<>();
	
	private int counter = 0;
	private boolean reload = false;
	private Position position;
	private SortKey[] sortKeys;
	private final boolean editable;
	private final AddressOption privateOption;
	private final AddressOption businessOption;
	private final OLATResourceable positionOres;
	private final OLATResourceable decisionRubricOres;
	private final RecruitingPositionSecurityCallback secCallback;
	private final Map<String,DecisionRubricSPI> keyToSpies = new HashMap<>();
	private final List<String> excludedAttributesList;
	
	private final Preferences guiPreferences;
	private static final String COLUMN_PREFS = "decRubricListRev4";

	@Autowired
	private TaggingService taggingService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingFrontendManager;
	
	public DecisionToolController(UserRequest ureq, WindowControl wControl, Position position,
			RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, "decision_tool", Util
				.createPackageTranslator(PositionController.class, ureq.getLocale()));

		this.position = position;
		this.secCallback = secCallback;
		editable = secCallback.canEditDecisionRubrics();
		guiPreferences = ureq.getUserSession().getGuiPreferences();
		privateOption = recruitingModule.getApplicationAddressPrivateOption();
		businessOption = recruitingModule.getApplicationAddressBusinessOption();
		excludedAttributesList = position.getExcludedAttributesList();
		List<DecisionRubricSPI> spies = recruitingModule.getDecisionRubricSpies();
		for(DecisionRubricSPI spi:spies) {
			keyToSpies.put(spi.getKey(), spi);
		}
		if(recruitingModule.isPositionCustomStepsEnabled()) {
			List<Tab> customTabs = position.getCustomEnabledTabsList();
			for(Tab tab:customTabs) {
				customTabAttributesDelegates.add(new ApplicationAttributesDelegate(tab.attributesTab()));
			}
		}
		
		initForm(ureq);
		loadTableAndModel();
		
		decisionRubricOres = OresHelper.createOLATResourceableInstance(DecisionRubric.class, position.getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), decisionRubricOres);

		positionOres = OresHelper.clone(position);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), positionOres);
	}
	
	public Position getPosition() {
		return position;
	}
	
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
	}
	
	public void updateApplications(Position updatedPosition) {
		this.position = updatedPosition;
		
		List<DecisionRubricDefinition> definitions = recruitingFrontendManager.getDecisionRubricDefinition(updatedPosition);
		List<ApplicationRubricsRow> rows = getApplicationRubricsRows(definitions);
		dataModel.setObjects(rows);
		tableEl.reset();
	}
	
	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, decisionRubricOres);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(secCallback.canConfigureDecisionTool()) {
			createRubrikButton = uifactory.addFormLink("create.decision.tool", formLayout, Link.BUTTON);
			createRubrikButton.setElementCssClass("o_sel_decision_manage_rubrics");
		}
		if(secCallback.canEditCommitteeDecision()) {
			batchDecisionButton = uifactory.addFormLink("batch.decision", formLayout, Link.BUTTON);
			batchDecisionButton.setElementCssClass("o_sel_decision_batch");
		}
	}
	
	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		ApplicationRubricsRow row = dataModel.getObject(pos);

		StringBuilder sb = new StringBuilder(32);
		ApplicationLight app = row.getApplication();
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
		return sb.length() == 0 ? null : sb.toString();
	}

	private void loadTableAndModel() {
		columnsModel.clear();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricCols.id));
		initColumnsModelApplicant();
		initAdditionalPersonalData();
		initColumnsModelAddress();
		initColumnsModelOrganization();
		initColumnsModelBusinessAddress();
		initColumnsModelPrivateAddress();
		if(recruitingModule.isApplicationAcademicalBackgroundEnabled(position)) {
			initColumnsModelAcademicalBackground();
		}
		if(position.isApplicationProject()) {
			initColumnsModelProject();
		}
		initColumnsModelCustomTabs();
		initColumnsModelCategories();

		if(secCallback.canSeeCommitteeRatings()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricCols.committeeRating));
		}
		//ratings
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.decision, new DecisionCellRenderer()));

		//rubric
		boolean hasSum = false;
		List<DecisionRubricDefinition> definitions = recruitingFrontendManager.getDecisionRubricDefinition(position);
		for(int i=0; i<definitions.size(); i++) {
			DecisionRubricDefinition def = definitions.get(i);
			DefaultFlexiColumnModel columnDef = new DefaultFlexiColumnModel("rubric.name", DecisionToolDataModel.RUBRIC_OFFSET + i, true, def.getRubric());
			columnDef.setHeaderLabel(def.getRubric());
			columnsModel.addFlexiColumnModel(columnDef);
			hasSum |= def.isSum();
		}
		
		//sum
		if(hasSum) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricCols.sum, new SumCellRenderer()));
		}
		
		List<ApplicationRubricsRow> rows = getApplicationRubricsRows(definitions);
		Map<Long,List<ApplicationCategoryInfos>> appToCategories = taggingService.getApplicationToCategories(position,
				secCallback.canSeeApplicationAdministrativeCategories());

		dataModel = new DecisionToolDataModel(rows, definitions, appToCategories, editable, getTranslator(), getLocale(), columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 25, false, getTranslator(), flc);
		tableEl.setMultiSelect(batchDecisionButton != null);
		tableEl.setAndLoadPersistedPreferences(guiPreferences, COLUMN_PREFS);
		tableEl.setCssDelegate(this);
		if(sortKeys != null && sortKeys.length > 0 && sortKeys[0] != null) {
			tableEl.setSortSettings(new FlexiTableSortOptions(true, sortKeys[0]));
			tableEl.sort(sortKeys[0].getKey(), sortKeys[0].isAsc());
		}
	
		initFilters();
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues decisionKV = new SelectionValues();
		decisionKV.add(SelectionValues.entry(FILTER_NULL_KEY, translate("decision.0.filter")));
		decisionKV.add(SelectionValues.entry("3", translate("decision.3.filter")));
		decisionKV.add(SelectionValues.entry("2", translate("decision.2.filter")));
		decisionKV.add(SelectionValues.entry("1", translate("decision.1.filter")));
		decisionKV.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.decision"),
				FILTER_DECISION, decisionKV, true));

		tableEl.setFilters(true, filters, true, true);
	}
	
	private void initColumnsModelApplicant() {
		initColumnModel(RubricCols.title, recruitingModule.getTableDecisionApplicationTitleOption());
		initColumnModel(RubricCols.firstName, recruitingModule.getTableDecisionApplicationFirstNameOption());
		initColumnModel(RubricCols.lastName, recruitingModule.getTableDecisionApplicationLastNameOption());
		
		if(recruitingModule.isApplicationPersonGenderEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.gender, new GenderCellRenderer()));
		}
		
		initOptionalColumnModel(RubricCols.maritalStatus, recruitingModule.getTableApplicationsMaritalStatusOption());
		initOptionalColumnModel(RubricCols.yearOfBirth, recruitingModule.getTableApplicationsYearOfBirthOption());
		initOptionalColumnModel(RubricCols.birthday, recruitingModule.getTableApplicationsBirthdayOption(), new DateCellRenderer());

		if(recruitingModule.isApplicationPersonAcademicTitleEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.academicTitle));
		}
	}
	
	private void initAdditionalPersonalData() {
		personalDataAttributesDelegate.initColumnsModel(columnsModel, position, null, getLocale());
	}

	private void initColumnsModelAddress() {
		initOptionalColumnModel(RubricCols.nationality, recruitingModule.getTableApplicationsNationalityOption());
		initOptionalColumnModel(RubricCols.additionalNationalities, recruitingModule.getTableApplicationsAdditionalNationalitiesOption());
		initOptionalColumnModel(RubricCols.mail, recruitingModule.getTableApplicationsPhoneOption());
		initOptionalColumnModel(RubricCols.phone, recruitingModule.getTableApplicationsPhoneOption());
		initOptionalColumnModel(RubricCols.mobilePhone, recruitingModule.getTableApplicationsMobilePhoneOption());
		initOptionalColumnModel(RubricCols.disability, recruitingModule.getTableApplicationsDisabilityOption(), new DisabilityCellRenderer());
	}
	
	private void initColumnsModelOrganization() {
		initOptionalColumnModel(RubricCols.organization, recruitingModule.getTableApplicationsOrganizationOption());
		initOptionalColumnModel(RubricCols.unit, recruitingModule.getTableApplicationsOrganizationUnitOption());
		initOptionalColumnModel(RubricCols.currentPosition, recruitingModule.getTableApplicationsOrganizationCurrentPositionOption());
	}
	
	private void initColumnsModelBusinessAddress() {
		if(AddressOption.enabled.equals(businessOption) || AddressOption.optional.equals(businessOption)) {
			initOptionalColumnModel(RubricCols.businessAddressLine1, recruitingModule.getTableApplicationsBusinessAddressLinesOption());
			initOptionalColumnModel(RubricCols.businessAddressLine2, recruitingModule.getTableApplicationsBusinessAddressLinesOption());
			initOptionalColumnModel(RubricCols.businessAddressLine3, recruitingModule.getTableApplicationsBusinessAddressLinesOption());
			initOptionalColumnModel(RubricCols.businessZipcode, recruitingModule.getTableApplicationsBusinessZipcodeOption());
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.businessCity));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.businessCountry));
			if(recruitingModule.isApplicationBusinessPhoneEnabled()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.businessPhone));
			}
			if(recruitingModule.isApplicationBusinessMailEnabled()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.businessMail));
			}
		}
	}
	
	private void initColumnsModelPrivateAddress() {
		if(!AddressOption.disabled.equals(privateOption)) {
			initOptionalColumnModel(RubricCols.addressLine1, recruitingModule.getTableApplicationsAddressLinesOption());
			initOptionalColumnModel(RubricCols.addressLine2, recruitingModule.getTableApplicationsAddressLinesOption());
			initOptionalColumnModel(RubricCols.addressLine3, recruitingModule.getTableApplicationsAddressLinesOption());
			initOptionalColumnModel(RubricCols.zipcode, recruitingModule.getTableApplicationsZipcodeOption());
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.city));
			if (recruitingModule.isApplicationAddressCountryEnabled()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.country));
			}
		}
	}
	
	private void initColumnsModelAcademicalBackground() {
		if(recruitingModule.isApplicationAcademicalBackgroundNumberOfOriginalPublicationsEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.numberOfOriginalPublications));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.numberOfFirstAuthorships));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundNumberOfLastAuthorshipsEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.numberOfLastAuthorships));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundCitationsEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.citations));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundImpactFactorEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.impactFactor));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundHFactorEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.hFactor));
		}

		//degree
		if(recruitingModule.isApplicationAcademicalBackgroundHighestDegreeEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)) {
			if(!excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_TYPE)) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.highestDegreeType));
			}

			if(!excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR)) {
				RecruitingTableOption highestDegreeYearOption = recruitingModule.getTableApplicationsHighestDegreeYearOption();
				if(!highestDegreeYearOption.isDisabled()) {
					if(recruitingModule.isTableApplicationsHighestDegreeYearOnlyPhDOption()) {
						columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.highestDegreeYearPhD,
							new AcademicalDateCellRenderer(AcademicalDateFormat.yearsOnly(), getLocale())));
					} else {
						columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.highestDegreeYear,
							new AcademicalDateCellRenderer(AcademicalDateFormat.yearsOnly(), getLocale())));
					}
				}
			}

			if(!excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_INSTITUTION)) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.highestDegreeInstitution));
			}
		}
		if(recruitingModule.isApplicationAcademicalBackgroundWorkedInAcademiaSinceEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.workedInAcademiaSince));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaSinceEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.workedOutAcademiaSince));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.workedOutAcademiaCareSince));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundCareerDescriptionEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.careerDescription));
		}

		//dissertation
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationTitleEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.dissertationTitle));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationDateEnabled()) {	
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.dissertationDate,
					new AcademicalDateCellRenderer(recruitingModule.getApplicationAcademicalBackgroundDissertationDateFormat(), getLocale())));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationInstitutionEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.dissertationInstitution));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword1Enabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.dissertationKeyword1));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword2Enabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.dissertationKeyword2));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword3Enabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.dissertationKeyword3));
		}
		
		//habilitation
		if(recruitingModule.isApplicationAcademicalBackgroundHabilitationEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.habilitationTitle));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.habilitationDate,
					new AcademicalDateCellRenderer(AcademicalDateFormat.yearsOnly(), getLocale())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.habilitationInstitution));
		}
		//orcid
		if(recruitingModule.isApplicationAcademicalBackgroundOrcidEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.orcid));
		}
		
		academicalBackgroundAttributesDelegate.initColumnsModel(columnsModel, position, null, getLocale());
	}
	
	private void initColumnsModelProject() {
		initColumnModel(RubricCols.projectTitle, recruitingModule.getTableDecisionProjectTitleOption());
		initColumnModel(RubricCols.projectAcronym, recruitingModule.getTableDecisionProjectAcronymOption());
		initColumnModel(RubricCols.projectKeywords, recruitingModule.getTableDecisionProjectKeywordsOption());
		initColumnModel(RubricCols.projectDisciplines, recruitingModule.getTableDecisionProjectDisciplinesOption());
		initColumnModel(RubricCols.projectStartDate, recruitingModule.getTableDecisionProjectStartDateOption(), new DateCellRenderer());
		initColumnModel(RubricCols.projectDuration, recruitingModule.getTableDecisionProjectDurationOption());
		initColumnModel(RubricCols.projectFinancialImpact1, recruitingModule.getTableDecisionProjectFinancialImpact1Option());
		initColumnModel(RubricCols.projectFinancialImpact2, recruitingModule.getTableDecisionProjectFinancialImpact2Option());
		initColumnModel(RubricCols.projectFinancialImpact3, recruitingModule.getTableDecisionProjectFinancialImpact3Option());
		initColumnModel(RubricCols.projectFinancialImpact4, recruitingModule.getTableDecisionProjectFinancialImpact4Option());
		initColumnModel(RubricCols.projectFinancialImpact5, recruitingModule.getTableDecisionProjectFinancialImpact5Option());
		
		projectAttributesDelegate.initColumnsModel(columnsModel, position, null, getLocale());
	}
	
	private void initColumnsModelCustomTabs() {
		for(ApplicationAttributesDelegate attributesDelegates:customTabAttributesDelegates) {
			attributesDelegates.initColumnsModel(columnsModel, position, null, getLocale());
		}
	}
	
	private void initColumnsModelCategories() {
		if(recruitingModule.isCategoriesEnabledFor(position)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RubricCols.categories, new CategoriesCellRenderer()));
		}
	}
	
	private void initColumnModel(RubricCols field, RecruitingTableOption option) {
		if(!option.isDisabled() && field.visible(excludedAttributesList)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field));
		}
	}
	
	private void initColumnModel(RubricCols field, RecruitingTableOption option, FlexiCellRenderer renderer) {
		if(!option.isDisabled() && field.visible(excludedAttributesList)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field, renderer));
		}
	}
	
	private void initOptionalColumnModel(RubricCols field, RecruitingTableOption option) {
		if(!option.isDisabled() && field.visible(excludedAttributesList)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, field));
		}
	}
	
	private void initOptionalColumnModel(RubricCols field, RecruitingTableOption option, FlexiCellRenderer renderer) {
		if(!option.isDisabled() && field.visible(excludedAttributesList)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, field, renderer));
		}
	}
	
	private List<ApplicationRubricsRow> getApplicationRubricsRows(List<DecisionRubricDefinition> definitions) {
		List<ApplicationLight> applications = recruitingFrontendManager.getApplications(position, new ArrayList<>());
		List<ApplicationRubricsRow> rows = new ArrayList<>(applications.size());
		
		PositionRole[] ratingRoles = recruitingModule.getRolesAllowedToRate();
		List<Identity> committee = recruitingFrontendManager.getCommittee(position, ratingRoles);
		List<UserRating> ratings = recruitingFrontendManager.getRatings(position, committee);
		List<DecisionRubric> rubrics = recruitingFrontendManager.getDecisionRubric(position);
		Map<DecisionRubricKey, DecisionRubric> rubricMap = new HashMap<>();
		for(DecisionRubric rubric:rubrics) {
			DecisionRubricKey key = new DecisionRubricKey(rubric.getDefinition().getKey(), rubric.getApplication().getKey());
			rubricMap.put(key, rubric);
		}

		for(ApplicationLight application:applications) {
			ApplicationRubricsRow row = new ApplicationRubricsRow(application);
			forgeRow(row, committee, ratings, definitions, rubricMap);
			rows.add(row);
		}
		return rows;
	}
	
	private void forgeRow(ApplicationRubricsRow row, List<Identity> committee, List<UserRating> ratings,
			List<DecisionRubricDefinition> definitions, Map<DecisionRubricKey, DecisionRubric> rubricMap) {
		RatingsOverviewFormItem overviewItem = new RatingsOverviewFormItem("rating-o-" + (++counter));
		List<UserRating> appRatings = new ArrayList<>(committee.size());
		String resSubPath = row.getApplication().getKey().toString();

		UserRatingMapper userRatingMapper = new UserRatingMapper(row.getApplication());
		for(Identity member:committee) {
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

		Collections.sort(appRatings, ratingComparator);
		overviewItem.setRatings(appRatings);
		userRatingMapper.setRatings(appRatings);
		row.setRatingOverviewItem(overviewItem);
		row.setUserRatingMapper(userRatingMapper);
		
		double sum = 0;
		for(DecisionRubricDefinition definition:definitions) {
			DecisionRubricSPI rubricSpi = null;
			if(StringHelper.containsNonWhitespace(definition.getType())) {
				rubricSpi = keyToSpies.get(definition.getType());
			}
			
			ApplicationRubric appRubric = new ApplicationRubric(definition, row.getApplication(), row, rubricSpi);
			row.addApplicationRubric(appRubric);
			
			DecisionRubricKey key = new DecisionRubricKey(definition.getKey(), row.getApplication().getKey());
			DecisionRubric rubric = rubricMap.get(key);
			if(rubric != null) {
				appRubric.setRubric(rubric);
				if(definition.isSum()) {
					double nValue = rubricSpi.getNumericalNormalizedValue(rubric);
					sum += (nValue * definition.getWeight());
				}
			}
			
			if(editable) {
				FormItem fItem = rubricSpi.createElement(rubric, flc, uifactory);
				fItem.addActionListener(FormEvent.ONCHANGE);
				fItem.setUserObject(appRubric);
				appRubric.setFormItem(fItem);
			}
		}
		row.setSum(sum);
	}
	
	@Override
	public void event(Event event) {
		if(event instanceof DecisionRubricEvent) {
			DecisionRubricEvent dre = (DecisionRubricEvent)event;
			if(!dre.getIdentitySenderKey().equals(getIdentity().getKey())) {
				processRubricChanges(dre);
			}
		} else if(event instanceof DecisionRubricDefinitionEvent) {
			DecisionRubricDefinitionEvent dre = (DecisionRubricDefinitionEvent)event;
			if(!dre.getIdentitySenderKey().equals(getIdentity().getKey())) {
				processDefinitionsChanged();
			}
		} else if(FinalDecisionChangeEvent.FINAL_DECISION.equals(event.getCommand())
				&& event instanceof FinalDecisionChangeEvent) {
			doProcess((FinalDecisionChangeEvent)event);
		}
	}
	
	private void processRubricChanges(DecisionRubricEvent event) {
		DecisionRubric rubric = event.getRubric();
		if(dataModel.replaceRubric(rubric)) {
			tableEl.getComponent().setDirty(true);
		}
	}
	
	private void processDefinitionsChanged() {
		loadTableAndModel();
	}
	
	private void doProcess(FinalDecisionChangeEvent changeEvent) {
		if(changeEvent.getEmitterKey() != null && getIdentity().getKey().equals(changeEvent.getEmitterKey())) return;
		
		if(!editable) {
			updateApplications(position);
		} else {
			reload = true;
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(decisionEditorCtrl == source) {
			if(event == Event.DONE_EVENT) {
				position = decisionEditorCtrl.getPosition();
				loadTableAndModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
				
				DecisionRubricDefinitionEvent e = new DecisionRubricDefinitionEvent(getIdentity().getKey());
				CoordinatorManager.getInstance().getCoordinator().getEventBus()
					.fireEventToListenersOf(e, decisionRubricOres);
			}
			cmc.deactivate();
			cleanUp();
		} else if(batchDecisionCtrl == source) {
			if(event instanceof DecisionEvent) {
				loadTableAndModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(decisionEditorCtrl);
		removeAsListenerAndDispose(batchDecisionCtrl);
		removeAsListenerAndDispose(cmc);
		decisionEditorCtrl = null;
		batchDecisionCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(createRubrikButton == source) {
			doOpenRubrikEditor(ureq);
		} else if(batchDecisionButton == source) {
			Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
			if(selectedIndexes == null || selectedIndexes.isEmpty()) {
				showWarning("batch.decision.atleastone");
			} else {
				List<ApplicationRubricsRow> applications = new ArrayList<>(selectedIndexes.size());
				for(Integer selectedIndex:selectedIndexes) {
					ApplicationRubricsRow row = dataModel.getObject(selectedIndex.intValue());
					applications.add(row);
				}
				doSetDecision(ureq, applications);
			}
		} else if(tableEl == source) {
			sortKeys = tableEl.getOrderBy();
			
			if(event instanceof FlexiTableSearchEvent ftse) {
				dataModel.filter(ftse.getSearch(), ftse.getFilters());
				tableEl.reset(true, true, false);
			}
		} else {
			Object uobject = source.getUserObject();
			if(uobject instanceof ApplicationRubric) {
				ApplicationRubric appRubric = (ApplicationRubric)uobject;
				doCommitRow(appRubric);
				DecisionRubricEvent e = new DecisionRubricEvent(appRubric.getRubric(), getIdentity().getKey());
				CoordinatorManager.getInstance().getCoordinator().getEventBus()
					.fireEventToListenersOf(e, decisionRubricOres);
			}
			
			if(editable && reload) {
				updateApplications(position);
				reload = false;
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	
	private void doCommitRow(ApplicationRubric appRubric) {
		commit(appRubric);
		DecisionRubric savedRubric = recruitingFrontendManager.saveDecisionRubric(appRubric.getRubric());
		appRubric.setRubric(savedRubric);
		logAudit("Set rubric: " + savedRubric + " for application: " + appRubric.getApplication().toString(), null);
		
		//recalculate sum
		List<ApplicationRubric> rubrics = appRubric.getApplicationRubricsRow().getApplicationRubrics();
		double sum = 0;
		for(ApplicationRubric aRubric:rubrics) {
			DecisionRubricSPI rubricSpi = aRubric.getDecisionRubricSpi();
			DecisionRubric rubric = aRubric.getRubric();
			if(rubric != null && rubric.getDefinition().isSum()) {
				double nValue = rubricSpi.getNumericalNormalizedValue(rubric);
				sum += (nValue * aRubric.getDefinition().getWeight());
			}
		}
		appRubric.getApplicationRubricsRow().setSum(sum);
	}
	
	private void commit(ApplicationRubric appRubric) {
		DecisionRubricDefinition definition = appRubric.getDefinition();
		DecisionRubricSPI rubricSpi = keyToSpies.get(definition.getType());
		DecisionRubric rubric = appRubric.getRubric();
		if(rubric == null) {
			rubric = recruitingFrontendManager.createDecisionRubric(definition, appRubric.getApplication());
			appRubric.setRubric(rubric);
		}
		rubricSpi.commitValue(rubric, appRubric.getFormItem());
	}
	
	private void doOpenRubrikEditor(UserRequest ureq) {
		if(guardModalController(decisionEditorCtrl)) return;
		
		decisionEditorCtrl = new DecisionRubricEditorController(ureq, getWindowControl(), position);
		listenTo(decisionEditorCtrl);

		String title = translate("create.decision.tool");
		cmc = new CloseableModalController(getWindowControl(), "c", decisionEditorCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}

	private void doSetDecision(UserRequest ureq, List<ApplicationRubricsRow> applications) {
		if(guardModalController(batchDecisionCtrl)) return;
		
		batchDecisionCtrl = new BatchDecisionController(ureq, getWindowControl(), applications, position);
		listenTo(batchDecisionCtrl);
		
		String title = translate("batch.decision.title", new String[]{ Integer.toString(applications.size())} );
		cmc = new CloseableModalController(getWindowControl(), "c", batchDecisionCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private final class DecisionRubricKey {
		
		private final Long definitionKey;
		private final Long applicationKey;
		
		public DecisionRubricKey(Long definitionKey, Long applicationKey) {
			this.definitionKey = definitionKey;
			this.applicationKey = applicationKey;
		}

		@Override
		public int hashCode() {
			return (definitionKey == null ? 387465 : definitionKey.hashCode())
					+ (applicationKey == null ? 19087772 : applicationKey.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof DecisionRubricKey) {
				DecisionRubricKey r = (DecisionRubricKey)obj;
				return definitionKey != null && definitionKey.equals(r.definitionKey)
						&& applicationKey != null && applicationKey.equals(r.applicationKey);
			}
			return false;
		}
	}
}
