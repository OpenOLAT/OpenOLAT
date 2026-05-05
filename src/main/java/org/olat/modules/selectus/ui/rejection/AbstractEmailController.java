/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.rejection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.MailLogInfos;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.AcademicalDateFormat;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.olat.modules.selectus.ui.components.AcademicalDateCellRenderer;
import org.olat.modules.selectus.ui.components.CategoriesCellRenderer;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.DecisionCellRenderer;
import org.olat.modules.selectus.ui.components.EmailedIconCellRenderer;
import org.olat.modules.selectus.ui.components.MultiRatingCellRenderer;
import org.olat.modules.selectus.ui.components.TooltipCellRenderer;
import org.olat.modules.selectus.ui.rejection.SelectFormEmailDataModel.Fields;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractEmailController extends StepFormBasicController implements FlexiTableCssDelegate {
	
	private Position position;
	private final boolean showDecision;
	private List<ApplicationLight> rows;
	private List<UserRating> ratings;
	private List<IdentityRef> committee;
	private List<MailLogInfos> mailLog;
	protected final RecruitingPositionSecurityCallback secCallback;
	private final List<String> excludedAttributesList;
	
	private final ApplicationAttributesDelegate projectAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.project);
	private final ApplicationAttributesDelegate personalDataAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.personalData);
	private final ApplicationAttributesDelegate academicalBackgroundAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.academicalBackground);
	private final List<ApplicationAttributesDelegate> customTabAttributesDelegates
		= new ArrayList<>();
	
	protected FlexiTableElement tableEl;
	protected SelectFormEmailDataModel applicationsDataModel;
	private final FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
	
	@Autowired
	private TaggingService taggingService;
	@Autowired
	private RecruitingModule recruitingModule;
	
	public AbstractEmailController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form,
			List<ApplicationLight> rows, List<MailLogInfos> mailLog, List<UserRating> ratings, List<IdentityRef> committee,
			Position position, boolean showDecision, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, form, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.secCallback = secCallback;
		this.rows = rows;
		this.ratings = ratings;
		this.committee = committee;
		this.showDecision = showDecision;
		this.mailLog = mailLog;
		this.position = position;
		excludedAttributesList = position.getExcludedAttributesList();
		if(recruitingModule.isPositionCustomStepsEnabled()) {
			List<Tab> customTabs = position.getCustomEnabledTabsList();
			for(Tab tab:customTabs) {
				customTabAttributesDelegates.add(new ApplicationAttributesDelegate(tab.attributesTab()));
			}
		}

		initForm(ureq);
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
		ApplicationLight app = applicationsDataModel.getObject(pos);
		StringBuilder sb = new StringBuilder(32);
		if(app.getDecision() != null && app.getDecision() > 0) {
			int decision = app.getDecision().intValue();
			switch(decision) {
				case 1: sb.append("fx_r_c_decision"); break;
				case 2: sb.append("fx_r_b_decision"); break;
				case 3: sb.append("fx_r_a_decision"); break;
				default: //
			}
		}

		sb.append(" fx_r_".concat(app.getApplicationStatus().name()));
		return sb.toString();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.id, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		
		initColumnModel(Fields.title, recruitingModule.getTableMailApplicationTitleOption());
		initColumnModel(Fields.firstName, recruitingModule.getTableMailApplicationFirstNameOption());
		initColumnModel(Fields.lastName, recruitingModule.getTableMailApplicationLastNameOption());

		if(position.isApplicationProject()) {
			initColumnModel(Fields.projectTitle, recruitingModule.getTableMailProjectTitleOption());
			initColumnModel(Fields.projectAcronym, recruitingModule.getTableMailProjectAcronymOption());
			initColumnModel(Fields.projectKeywords, recruitingModule.getTableMailProjectKeywordsOption());
			initColumnModel(Fields.projectDisciplines, recruitingModule.getTableMailProjectDisciplinesOption());
			initColumnModel(Fields.projectStartDate, recruitingModule.getTableMailProjectStartDateOption(), new DateCellRenderer());
			initColumnModel(Fields.projectDuration, recruitingModule.getTableMailProjectDurationOption());
			initColumnModel(Fields.projectFinancialImpact1, recruitingModule.getTableMailProjectFinancialImpact1Option());
			initColumnModel(Fields.projectFinancialImpact2, recruitingModule.getTableMailProjectFinancialImpact2Option());
			initColumnModel(Fields.projectFinancialImpact3, recruitingModule.getTableMailProjectFinancialImpact3Option());
			initColumnModel(Fields.projectFinancialImpact4, recruitingModule.getTableMailProjectFinancialImpact4Option());
			initColumnModel(Fields.projectFinancialImpact5, recruitingModule.getTableMailProjectFinancialImpact5Option());
			initColumnModel(Fields.projectDescription, recruitingModule.getTableMailProjectDescriptionOption(), new TooltipCellRenderer("o_icon_project_description"));
			projectAttributesDelegate.initColumnsModel(columnsModel, position, null, getLocale(), null);
		}
		
		for(ApplicationAttributesDelegate attributesDelegates:customTabAttributesDelegates) {
			attributesDelegates.initColumnsModel(columnsModel, position, null, getLocale(), null);
		}
		
		if(recruitingModule.isCategoriesEnabledFor(position)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.categories, new CategoriesCellRenderer()));
		}

		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.mail, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		if(recruitingModule.getPositionLocales().length > 1) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.language));
		}
		
		initColumnModel(Fields.organization, recruitingModule.getTableApplicationsOrganizationOption());
		initColumnModel(Fields.unit, recruitingModule.getTableApplicationsOrganizationUnitOption());
		personalDataAttributesDelegate.initColumnsModel(columnsModel,  position, null, getLocale(), null);
		
		//highest degree
		if(recruitingModule.isApplicationAcademicalBackgroundEnabled(position)
				&& recruitingModule.isApplicationAcademicalBackgroundHighestDegreeEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)) {
			
			if(!excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_TYPE)) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.highestDegreeType));
			}
			if(!excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR)) {
				if(recruitingModule.isTableApplicationsHighestDegreeYearOnlyPhDOption()) {
					columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.highestDegreeYearPhD,
							new AcademicalDateCellRenderer(AcademicalDateFormat.yearsOnly(), getLocale())));
				} else {	
					columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.highestDegreeYear,
							new AcademicalDateCellRenderer(AcademicalDateFormat.yearsOnly(), getLocale())));
				}
			}
			academicalBackgroundAttributesDelegate.initColumnsModel(columnsModel, position, null, getLocale(), null);
		}
		
		initColumnModel(Fields.applicationStatus, recruitingModule.getTableMailApplicationStatusOption());

		if(showDecision) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.decision, new DecisionCellRenderer()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.committeeRating, new MultiRatingCellRenderer(getTranslator(), null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.emailed, new EmailedIconCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.emailDate, new DateCellRenderer()));

		Map<Long,List<ApplicationCategoryInfos>> appToCategories = taggingService.getApplicationToCategories(position,
				secCallback.canSeeApplicationAdministrativeCategories());
		applicationsDataModel = new SelectFormEmailDataModel(rows, mailLog, ratings, committee, appToCategories, getTranslator(), columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "applications", applicationsDataModel, getTranslator(), formLayout);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
					.withMessageI18nKey("rejection.empty")
					.build());
		
		if(showDecision) {
			tableEl.setCssDelegate(this);
		}
	}
	
	private void initColumnModel(Fields field, RecruitingTableOption option) {
		if(!option.isDisabled() && field.visible(excludedAttributesList)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field));
		}
	}	
	
	private void initColumnModel(Fields field, RecruitingTableOption option, FlexiCellRenderer renderer) {
		if(!option.isDisabled() && field.visible(excludedAttributesList)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field, renderer));
		}
	}
}