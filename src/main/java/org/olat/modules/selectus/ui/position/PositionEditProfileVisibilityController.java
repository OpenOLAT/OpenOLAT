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
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectAllColumnEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.UnselectAllColumnEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.position.PositionEditProfileVisibilityHelper.Visibility;
import org.olat.modules.selectus.ui.position.model.EditVisibilityBundle;
import org.olat.modules.selectus.ui.position.model.EditVisibilityRow;
import org.olat.modules.selectus.ui.position.model.EditVisibilityStepSettings;

/**
 * 
 * Initial date: 15 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditProfileVisibilityController extends FormBasicController implements PositionEditableController {

	private static final int CHECKBOX_OFFSET = 500;
	
	private static final String CHECK_DOCS_REFEREES = "check_docs_referees";
	private static final String CHECK_DOCS_EXPERTS = "check_docs_experts";
	private static final String CHECK_DOCS_COMPARATIVE_ASSESSMENTS = "check_docs_comparative_assessments";
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };

	public static final String CUSTOMIZE = "customize";
	
	private FormLink previewLink;
	private SelectionValues visibilityPK = new SelectionValues();
	private FlexiTableColumnModel fieldsColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
	private FlexiTableColumnModel documentsColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
	
	private EditVisibilityBundle projectBundle;
	private EditVisibilityBundle documentsBundle;
	private EditVisibilityBundle personalDataBundle;
	private EditVisibilityBundle academicalBackgroudBundle;
	private final Map<Tab,EditVisibilityBundle> customStepsBundle = new EnumMap<>(Tab.class);

	private boolean readOnly;
	private Position position;
	private List<ApplicationsFeedbackConfiguration> configurations;
	private final PositionEditProfileVisibilityHelper visibilityHelper;
	
	private CloseableModalController cmc;
	private PositionPreviewProfileVisibilityController previewCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionEditProfileVisibilityController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.readOnly = readOnly;
		configurations = feedbackService.getApplicationsFeedbackConfigurations(position);
		visibilityHelper = new PositionEditProfileVisibilityHelper();
		
		updateColumnsModelAndCheckboxModel();
		initForm(ureq);
		loadModel();
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		position = updatedPosition;

		configurations = feedbackService.getApplicationsFeedbackConfigurations(position);
		updateColumnsModelAndCheckboxModel();
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("profil.visibility.description");
		
		personalDataBundle = initPersonalDataVisibilityForm(formLayout);
		academicalBackgroudBundle = initAcademicalBackgroundVisibilityForm(formLayout);
		documentsBundle = initDocumentsVisibilityForm(formLayout);
		projectBundle = initProjectVisibilityForm(formLayout);
		
		customStepsBundle.clear();
		for(Tab customStep:position.getCustomEnabledTabsList()) {
			customStepsBundle.put(customStep, initCustomTabVisibilityForm(customStep, formLayout));
		}
		
		previewLink = uifactory.addFormLink("edit.template.preview", formLayout, Link.LINK);
		previewLink.setDomReplacementWrapperRequired(false);
		previewLink.setIconLeftCSS("o_icon o_icon_preview");
		previewLink.setVisible(!readOnly);

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private EditVisibilityBundle initPersonalDataVisibilityForm(FormItemContainer formLayout) {
		SingleSelection visibilityEl = initVisibilitySingleSelection("personal.data", formLayout);
		initSingleSelectionSelectedForFields(RecruitingModule.APP_SECTION_PERSON, visibilityEl);

		String page = velocity_root + "/documents_table.html";
		FormLayoutContainer tableLayoutCont = FormLayoutContainer.createCustomFormLayout("tablePersonData", getTranslator(), page);
		formLayout.add(tableLayoutCont);
		
		VisibilityDataModel tableModel = new VisibilityDataModel(fieldsColumnsModel);
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 50, false, getTranslator(), tableLayoutCont);
		tableEl.setExportEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);

		return EditVisibilityBundle.valueOf(tableEl, tableModel, visibilityEl, tableLayoutCont);
	}
	
	private EditVisibilityBundle initAcademicalBackgroundVisibilityForm(FormItemContainer formLayout) {
		SingleSelection visibilityEl = initVisibilitySingleSelection("academical.background", formLayout);
		initSingleSelectionSelectedForFields(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, visibilityEl);

		String page = velocity_root + "/documents_table.html";
		FormLayoutContainer tableLayoutCont = FormLayoutContainer.createCustomFormLayout("tableAcademicalBackground", getTranslator(), page);
		formLayout.add(tableLayoutCont);
		
		VisibilityDataModel tableModel = new VisibilityDataModel(fieldsColumnsModel);
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 50, false, getTranslator(), tableLayoutCont);
		tableEl.setExportEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);

		return EditVisibilityBundle.valueOf(tableEl, tableModel, visibilityEl, tableLayoutCont);
	}
	
	private EditVisibilityBundle initProjectVisibilityForm(FormItemContainer formLayout) {
		SingleSelection visibilityEl = initVisibilitySingleSelection("project", formLayout);
		initSingleSelectionSelectedForFields(RecruitingModule.APP_SECTION_PROJECT, visibilityEl);

		String page = velocity_root + "/documents_table.html";
		FormLayoutContainer tableLayoutCont = FormLayoutContainer.createCustomFormLayout("tableProject", getTranslator(), page);
		formLayout.add(tableLayoutCont);
		
		VisibilityDataModel tableModel = new VisibilityDataModel(fieldsColumnsModel);
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 50, false, getTranslator(), tableLayoutCont);
		tableEl.setExportEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);

		return EditVisibilityBundle.valueOf(tableEl, tableModel, visibilityEl, tableLayoutCont);
	}

	private EditVisibilityBundle initCustomTabVisibilityForm(Tab step, FormItemContainer formLayout) {
		SingleSelection visibilityEl = initVisibilitySingleSelection("custom.step." + step, formLayout);
		initSingleSelectionSelectedForFields(step.name(), visibilityEl);
		
		String title = position.getTabConfiguration(step).getTitle(getLocale());
		visibilityEl.setLabel("custom.step.visibility", new String[] { title });
		
		String page = velocity_root + "/documents_table.html";
		FormLayoutContainer tableLayoutCont = FormLayoutContainer.createCustomFormLayout("tableCustom" + step, getTranslator(), page);
		formLayout.add(tableLayoutCont);
		
		VisibilityDataModel tableModel = new VisibilityDataModel(fieldsColumnsModel);
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 50, false, getTranslator(), tableLayoutCont);
		tableEl.setExportEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);

		return EditVisibilityBundle.valueOf(tableEl, tableModel, visibilityEl, tableLayoutCont);
	}
	
	private EditVisibilityBundle initDocumentsVisibilityForm(FormItemContainer formLayout) {
		SingleSelection visibilityEl = initVisibilitySingleSelection("documents", formLayout);
		initSingleSelectionSelectedForDocuments(visibilityEl);
		
		String page = velocity_root + "/documents_table.html";
		FormLayoutContainer tableLayoutCont = FormLayoutContainer.createCustomFormLayout("table", getTranslator(), page);
		formLayout.add(tableLayoutCont);
		
		VisibilityDataModel tableModel = new VisibilityDataModel(documentsColumnsModel);
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 50, false, getTranslator(), tableLayoutCont);
		tableEl.setExportEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		
		return EditVisibilityBundle.valueOf(tableEl, tableModel, visibilityEl, tableLayoutCont);
	}
	
	private SingleSelection initVisibilitySingleSelection(String id, FormItemContainer formLayout) {
		SelectionValues visibilitySectionsPK = new SelectionValues();
		visibilitySectionsPK.add(SelectionValues.entry(RecruitingModule.NONE, translate("reference.visibility.none")));
		visibilitySectionsPK.add(SelectionValues.entry(RecruitingModule.ALL, translate("reference.visibility.all")));
		visibilitySectionsPK.add(SelectionValues.entry(CUSTOMIZE, translate("reference.visibility.customize")));
		SingleSelection visibilityEl = uifactory.addRadiosHorizontal("visibility." + id, id + ".visibility", formLayout, visibilitySectionsPK.keys(), visibilitySectionsPK.values());
		visibilityEl.addActionListener(FormEvent.ONCHANGE);
		visibilityEl.setEnabled(!readOnly);
		return visibilityEl;
	}
	
	private void initSingleSelectionSelectedForFields(String section, SingleSelection el) {
		Set<String> fields = new HashSet<>();
		fields.addAll(position.getRefereeRecommendationFields());
		fields.addAll(position.getExpertRecommendationFields());
		fields.addAll(position.getComparativeAssessmentExpertFields());
		for(ApplicationsFeedbackConfiguration configuration:configurations) {
			fields.addAll(configuration.getFields());
		}

		if(fields.contains(section + RecruitingModule.NONE) ) {
			el.select(RecruitingModule.NONE, true);
		} else if(fields.contains(section + RecruitingModule.ALL)) {
			el.select(RecruitingModule.ALL, true);
		} else {
			el.select(CUSTOMIZE, true);
		}
	}
	
	private void initSingleSelectionSelectedForDocuments(SingleSelection el) {
		Set<String> refereesDocs = position.getRefereeRecommendationDocuments();
		Set<String> expertsDocs = position.getExpertRecommendationDocuments();
		if(refereesDocs.size() == 1 && expertsDocs.size() == 1
				&& refereesDocs.contains(RecruitingModule.NONE) && expertsDocs.contains(RecruitingModule.NONE)) {
			el.select(RecruitingModule.NONE, true);
		} else if(refereesDocs.size() == 1 && expertsDocs.size() == 1
				&& refereesDocs.contains(RecruitingModule.ALL) && expertsDocs.contains(RecruitingModule.ALL)) {
			el.select(RecruitingModule.ALL, true);
		} else {
			el.select(CUSTOMIZE, true);
		}
	}
	
	private void updateColumnsModelAndCheckboxModel() {
		visibilityPK.clear();
		fieldsColumnsModel.clear();
		documentsColumnsModel.clear();
		
		DefaultFlexiColumnModel fieldsTitleColumn = new DefaultFlexiColumnModel(VisibilityCols.title);
		fieldsTitleColumn.setHeaderLabel(translate("field.label"));
		fieldsColumnsModel.addFlexiColumnModel(fieldsTitleColumn);
		documentsColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(VisibilityCols.title));

		if(recruitingModule.isReferenceEnabled() && position.isRefereeRecommendationEnabled()) {
			DefaultFlexiColumnModel refereesCol = new DefaultFlexiColumnModel(VisibilityCols.referees);
			refereesCol.setSelectAll(!readOnly);
			fieldsColumnsModel.addFlexiColumnModel(refereesCol);
			documentsColumnsModel.addFlexiColumnModel(refereesCol);
			visibilityPK.add(SelectionValues.entry(VisibilityCols.referees.name(), "Referee"));
		}
		
		if(recruitingModule.isReferenceEnabled() && position.isExpertRecommendationEnabled()) {
			DefaultFlexiColumnModel expertCol = new DefaultFlexiColumnModel(VisibilityCols.experts);
			expertCol.setSelectAll(!readOnly);
			fieldsColumnsModel.addFlexiColumnModel(expertCol);
			documentsColumnsModel.addFlexiColumnModel(expertCol);
			visibilityPK.add(SelectionValues.entry(VisibilityCols.experts.name(), "Experts"));
		}
		
		if(recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled()) {
			DefaultFlexiColumnModel comparativeExpertCol = new DefaultFlexiColumnModel(VisibilityCols.comparativeExperts);
			comparativeExpertCol.setSelectAll(!readOnly);
			fieldsColumnsModel.addFlexiColumnModel(comparativeExpertCol);
			documentsColumnsModel.addFlexiColumnModel(comparativeExpertCol);
			visibilityPK.add(SelectionValues.entry(VisibilityCols.comparativeExperts.name(), "Comparative experts"));
		}
		
		if(recruitingModule.isMembersFeedbackEnabled()) {	
			for(int i=0; i<configurations.size(); i++) {
				ApplicationsFeedbackConfiguration configuration = configurations.get(i);
				if(configuration.isEnabled()) {
					String key = "config_" + i;
					DefaultFlexiColumnModel feedbackCol = new DefaultFlexiColumnModel(key, CHECKBOX_OFFSET + i, false, null);
					String header = translate("table.document.header.visible", new String[] { configuration.getConfigurationName().toLowerCase() });
					feedbackCol.setHeaderLabel(header);
					feedbackCol.setSelectAll(true);
	
					fieldsColumnsModel.addFlexiColumnModel(feedbackCol);
					documentsColumnsModel.addFlexiColumnModel(feedbackCol);
					visibilityPK.add(SelectionValues.entry(key, "Config " + i));
				}
			}
		}
		
		if(recruitingModule.isPublicFeedbackEnabled() && position.isPublicFeedbackEnabled()) {
			DefaultFlexiColumnModel publicFeedbackCol = new DefaultFlexiColumnModel(VisibilityCols.publicFeedback);
			publicFeedbackCol.setSelectAll(false);

			fieldsColumnsModel.addFlexiColumnModel(publicFeedbackCol);
			documentsColumnsModel.addFlexiColumnModel(publicFeedbackCol);
			visibilityPK.add(SelectionValues.entry(VisibilityCols.publicFeedback.name(), "Public feedback"));
		}
	}
	
	private void loadModel() {
		EditVisibilityStepSettings settings = new EditVisibilityStepSettings(position, configurations);
		List<String> excludedAttributesList = position.getExcludedAttributesList();

		List<Visibility> availablePersonalData = visibilityHelper.getPersonalDataFields(excludedAttributesList);
		loadBundleDataModel(settings, availablePersonalData, Tab.personalData, personalDataBundle);
		List<Visibility> availableAcademicBackground = visibilityHelper.getAcademicalBackgroundFields(excludedAttributesList);
		loadBundleDataModel(settings, availableAcademicBackground, Tab.academicalBackground, academicalBackgroudBundle);
		List<Visibility> availableProject = visibilityHelper.getProjectFields();
		loadBundleDataModel(settings, availableProject, Tab.project, projectBundle);
		
		loadCustomStepsModel(settings);
		loadDocumentsModel();
		
		updateVisibility();
	}
	
	private void updateVisibility() {
		boolean academicalBackgroudEnabled = recruitingModule.isApplicationAcademicalBackgroundEnabled(position);
		if(!academicalBackgroudEnabled) {
			academicalBackgroudBundle.tableLayoutCont().setVisible(false);
			academicalBackgroudBundle.visibilityEl().setVisible(false);
		}
		
		boolean projectEnabled = recruitingModule.isApplicationProjectEnabled() && position.isApplicationProject();
		if(!projectEnabled) {
			projectBundle.tableLayoutCont().setVisible(false);
			projectBundle.visibilityEl().setVisible(false);
		}
	}
	
	private void loadBundleDataModel(EditVisibilityStepSettings settings, List<Visibility> available, Tab tab, EditVisibilityBundle bundle) {
		FormItemContainer formLayout = bundle.tableLayoutCont();

		List<EditVisibilityRow> rows = new ArrayList<>();
		for(Visibility fieldVisibility:available) {
			String field = fieldVisibility.field();
			if(!recruitingModule.isFieldEnabled(field)) continue;
			
			String title = translate(fieldVisibility.i18nKey());
			if(fieldVisibility.wrapperI18nKey() != null) {
				title = translate(fieldVisibility.wrapperI18nKey(), title);
			}
			EditVisibilityRow row = forgeFieldVisibilityRow(title, field, fieldVisibility.always(), settings, formLayout);
			rows.add(row);
		}
		
		List<PositionAttributeDefinition> definitions = position.getAttributesDefinitions(tab.attributesTab());
		for(PositionAttributeDefinition definition:definitions) {
			if(!definition.getTypeEnum().valueType()) {
				continue;
			}
			
			Long key = definition.getKey();
			String title = definition.getLabel(getLocale(), true);
			EditVisibilityRow row = forgeFieldVisibilityRow(title, RecruitingModule.APP_CUSTOM_FIELD_PREFIX + key, false, settings, formLayout);
			rows.add(row);
		}
		
		bundle.tableModel().setObjects(rows);
		bundle.tableEl().reset(true, true, true);
	}
	
	private void loadCustomStepsModel(EditVisibilityStepSettings settings) {
		List<Tab> customSteps = position.getCustomEnabledTabsList();
		for(Tab customStep:customSteps) {
			EditVisibilityBundle bundle = customStepsBundle.get(customStep);
			TabConfiguration config = position.getTabConfiguration(customStep);
			loadCustomStepModel(settings, config, bundle);
		}
	}
	
	private void loadCustomStepModel(EditVisibilityStepSettings settings, TabConfiguration config, EditVisibilityBundle bundle) {
		List<PositionAttributeDefinition> definitions = position.getAttributesDefinitions(config.getTab().attributesTab());
		FormLayoutContainer formLayout = bundle.tableLayoutCont();

		List<EditVisibilityRow> rows = new ArrayList<>();	
		for(PositionAttributeDefinition definition:definitions) {
			if(!definition.getTypeEnum().valueType()) {
				continue;
			}
			
			Long key = definition.getKey();
			String title = definition.getLabel(getLocale(), true);
			EditVisibilityRow row = forgeFieldVisibilityRow(title, RecruitingModule.APP_CUSTOM_FIELD_PREFIX + key, false, settings, formLayout);
			rows.add(row);
		}
		
		bundle.tableModel().setObjects(rows);
		bundle.tableEl().reset(true, true, true);
	}
		
	private EditVisibilityRow forgeFieldVisibilityRow(String title, String field, boolean always,
			EditVisibilityStepSettings settings, FormItemContainer formLayout) {

		MultipleSelectionElement expertsEl = forgeCheckBox("check_experts_".concat(field), field, always, settings.getExpertsFields(), formLayout);
		MultipleSelectionElement refereesEl = forgeCheckBox("check_referees_".concat(field), field, always, settings.getRefereesFields(), formLayout);
		MultipleSelectionElement comparativeExpertsEl = forgeCheckBox("check_comp_experts_".concat(field), field, always, settings.getComparativeExpertFields(), formLayout);
		
		MultipleSelectionElement[] feedbacksEls = new MultipleSelectionElement[configurations.size()];
		for(int i=0; i<configurations.size(); i++) {
			String feedbackId = "feedbacks_" + field + "_" + i;
			feedbacksEls[i] = forgeCheckBox(feedbackId, field, always, settings.getFacultyMembersFields(i), formLayout);
		}
		
		MultipleSelectionElement publicFeedbackEl = null;
		if(always) {
			publicFeedbackEl = forgeCheckBox("check_public_feedback_".concat(field), field, true, Collections.singletonList(field), formLayout);
		}
		
		return new EditVisibilityRow(field, title, expertsEl, refereesEl, comparativeExpertsEl, feedbacksEls, publicFeedbackEl);
	}
	
	
	private void loadDocumentsModel() {
		FormItemContainer formLayout = documentsBundle.tableLayoutCont();
		
		List<EditVisibilityRow> rows = new ArrayList<>();
		Set<String> available = position.getAvailableDocuments();
		Set<String> expertsDocs = position.getExpertRecommendationDocuments();
		Set<String> refereesDocs = position.getRefereeRecommendationDocuments();
		Set<String> comparativeExpertsDocs = position.getComparativeAssessmentExpertDocuments();
		
		for(DocumentOption docOption:recruitingModule.getDocumentOptions(position)) {
			DocumentEnum doc = docOption.getDoc();
			if(available.contains(doc.name())) {
				String title = position.getDocumentName(doc, getLocale());
				if(!StringHelper.containsNonWhitespace(title)) {
					title = translate(doc.i18nKey());
				}
				String expertId = "check_experts_".concat(doc.name());
				MultipleSelectionElement expertsEl = forgeCheckBox(expertId, doc.name(), false, expertsDocs, formLayout);

				String refereeId = "check_referees_".concat(doc.name());
				MultipleSelectionElement refereesEl = forgeCheckBox(refereeId, doc.name(), false, refereesDocs, formLayout);
				
				String comparativeExpertId = "check_comp_experts_".concat(doc.name());
				MultipleSelectionElement comparativeExpertsEl = forgeCheckBox(comparativeExpertId, doc.name(), false, comparativeExpertsDocs, formLayout);

				MultipleSelectionElement[] feedbacksEls = new MultipleSelectionElement[configurations.size()];
				for(int i=0; i<configurations.size(); i++) {
					ApplicationsFeedbackConfiguration  configuration = configurations.get(i);
					Set<String> docs = configuration.getDocuments();
					String feedbackId = "feedbacks_" + doc.name() + "_" + i;
					feedbacksEls[i] = forgeCheckBox(feedbackId, doc.name(), false, docs, formLayout);
				}
				rows.add(new EditVisibilityRow(doc, title, expertsEl, refereesEl, comparativeExpertsEl, feedbacksEls, null));
			}
		}
		
		// Experts documents only for feedbacks / faculty members
		if(recruitingModule.isReferenceEnabled()) {
			if(position.isRefereeRecommendationEnabled()) {
				String label = translate("edit.application.document.referees.docs");
				rows.add(forgeRecommendationForFacultyMembers(CHECK_DOCS_REFEREES, label, formLayout));
			}
			if(position.isExpertRecommendationEnabled()) {
				String label = translate("edit.application.document.experts.docs");
				rows.add(forgeRecommendationForFacultyMembers(CHECK_DOCS_EXPERTS, label, formLayout));
			}
			if(recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled()) {
				String label = translate("edit.application.document.comparative.assessment.docs");
				rows.add(forgeRecommendationForFacultyMembers(CHECK_DOCS_COMPARATIVE_ASSESSMENTS, label, formLayout));
			}
		}
		
		documentsBundle.tableModel().setObjects(rows);
		documentsBundle.tableEl().reset(true, true, true);
	}
	
	private EditVisibilityRow forgeRecommendationForFacultyMembers(String id, String title, FormItemContainer formLayout) {
		MultipleSelectionElement[] feedbacksEls = new MultipleSelectionElement[configurations.size()];
		for(int i=0; i<configurations.size(); i++) {
			ApplicationsFeedbackConfiguration  configuration = configurations.get(i);
			Set<String> docs = configuration.getDocuments();
			String feedbackId = id + "_" + i;
			feedbacksEls[i] = forgeCheckBox(feedbackId, id, false, docs, formLayout);
			
			if((CHECK_DOCS_REFEREES.equals(id) && configuration.isRefereesDocs())
					|| (CHECK_DOCS_EXPERTS.equals(id) && configuration.isExpertsDocs())
					|| (CHECK_DOCS_COMPARATIVE_ASSESSMENTS.equals(id) && configuration.isExpertsComparativeAssessmentDocs())) {
				feedbacksEls[i].select(onKeys[0], true);
			}
		}
		return new EditVisibilityRow(id, title, null, null, null, feedbacksEls, null);
	}
	
	private MultipleSelectionElement forgeCheckBox(String id, String elementName, boolean always,
			Collection<String> elements, FormItemContainer formLayout) {
		MultipleSelectionElement element = uifactory.addCheckboxesHorizontal(id, null, formLayout, onKeys, onValues);
		element.setEnabled(!readOnly);
		element.setAjaxOnly(true);
		if(elements.contains("all") || elements.contains(elementName) || always) {
			element.select(onKeys[0], true);
		}
		element.setEnabled(!always);
		return element;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == previewCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(previewCtrl);
		removeAsListenerAndDispose(cmc);
		previewCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == previewLink) {
			 doPreview(ureq);
		} else if(source instanceof SingleSelection && source.getUserObject() instanceof EditVisibilityBundle) {
			doToogleVisibility((SingleSelection)source, (EditVisibilityBundle)source.getUserObject());
		} else if(source instanceof MultipleSelectionElement) {
			markDirty();
		} else if(event instanceof SelectAllColumnEvent && source instanceof FlexiTableElement) {
			doSelectAllColumn((FlexiTableElement)source, ((SelectAllColumnEvent)event).getColumnIndex(), true);
			markDirty();
		} else if(event instanceof UnselectAllColumnEvent && source instanceof FlexiTableElement) {
			doSelectAllColumn((FlexiTableElement)source, ((UnselectAllColumnEvent)event).getColumnIndex(), false);
			markDirty();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelectAllColumn(FlexiTableElement table, int columnIndex, boolean selectAll) {
		Object uobject = table.getUserObject();
		if(uobject instanceof EditVisibilityBundle) {
			EditVisibilityBundle bundle = (EditVisibilityBundle)uobject;
			for(int i=bundle.tableModel().getRowCount(); i-->0; ) {
				Object val = bundle.tableModel().getValueAt(i, columnIndex);
				if(val instanceof MultipleSelectionElement) {
					MultipleSelectionElement select = (MultipleSelectionElement)val;
					if(select.isEnabled()) {
						if(selectAll) {
							select.selectAll();
						} else {
							select.uncheckAll();
						}
					}
				}
			}
			table.reset(false, false, true);
		}
	}
	
	private void doToogleVisibility(SingleSelection visibilityEl, EditVisibilityBundle bundle) {
		String key = visibilityEl.getSelectedKey();
		boolean customize = CUSTOMIZE.equals(key);
		bundle.tableLayoutCont().setVisible(customize);
		bundle.tableEl().setVisible(customize);
		markDirty();
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Event doneEvent = Event.DONE_EVENT;
		if(position.getKey() != null) {
			position = recruitingService.getPosition(position.getKey());
		} else {
			doneEvent = new NewPositionSavedEvent();
		}

		String before = auditService.toAuditXml(position);
		
		commitDocuments();
		commitFields();
		
		position = recruitingService.savePosition(position);
		
		for(int i=configurations.size(); i-->0; ) {
			ApplicationsFeedbackConfiguration configuration = feedbackService.updateApplicationsFeedbackConfiguration(configurations.get(i));
			configurations.set(i, configuration);
		}
		
		dbInstance.commit();
		getLogger().info(Tracing.M_AUDIT, "Update profile visibility: {}", position.toStringFull());
		
		String after = auditService.toAuditXml(position);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}
		
		fireEvent(ureq, doneEvent);
	}
	
	private void commitFields() {
		EditVisibilityStepSettings settings = new EditVisibilityStepSettings(configurations.size());
		commitFields(settings);
		position.setExpertRecommendationFields(settings.getExpertsFields());
		position.setRefereeRecommendationFields(settings.getRefereesFields());
		position.setComparativeAssessmentExpertFields(settings.getComparativeExpertFields());

		int numOfConfigurations = configurations.size();
		for(int i=0;i<numOfConfigurations; i++) {
			configurations.get(i).setFields(new HashSet<>(settings.getFacultyMembersFields(i)));
		}
	}
	

	private void commitFields(EditVisibilityStepSettings settings) {
		commitFields(personalDataBundle, RecruitingModule.APP_SECTION_PERSON, settings);
		commitFields(academicalBackgroudBundle, RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, settings);
		commitFields(projectBundle, RecruitingModule.APP_SECTION_PROJECT, settings);
		
		List<Tab> customSteps = position.getCustomEnabledTabsList();
		for(Tab customStep:customSteps) {
			EditVisibilityBundle bundle = customStepsBundle.get(customStep);
			commitFields(bundle, customStep.name(), settings);
		}
	}
	
	private void commitFields(EditVisibilityBundle bundle, String prefix, EditVisibilityStepSettings settings) {
		SingleSelection visibilityEl = bundle.visibilityEl();
		String visibility = visibilityEl.getSelectedKey();
		int numOfConfigurations = configurations.size();

		if(CUSTOMIZE.equals(visibility)) {
			List<EditVisibilityRow> rows = bundle.tableModel().getObjects();
			for(EditVisibilityRow row:rows) {
				String field = (String)row.getUserObject();

				addVisibility(settings.getExpertsFields(), field, row.isExpertsSelected());
				addVisibility(settings.getRefereesFields(), field, row.isRefereesElSelected());
				addVisibility(settings.getComparativeExpertFields(), field, row.isComparativeExpertsElSelected());
				for(int i=0;i<numOfConfigurations; i++) {
					addVisibility(settings.getFacultyMembersFields(i), field, row.isFacultyMembersElsSelected(i));
				}
			}
		} else {
			addVisibility(settings.getExpertsFields(), prefix + visibility, true);
			addVisibility(settings.getRefereesFields(), prefix + visibility, true);
			addVisibility(settings.getComparativeExpertFields(), prefix + visibility, true);
			for(int i=0;i<numOfConfigurations; i++) {
				addVisibility(settings.getFacultyMembersFields(i), prefix + visibility, true);
			}
		}
	}
	
	private void commitDocuments() {
		SingleSelection visibilityEl = documentsBundle.visibilityEl();
		EditVisibilityStepSettings settings = new EditVisibilityStepSettings(configurations.size());
		
		commitDocuments(visibilityEl, settings);
		
		position.setRefereeRecommendationDocuments(settings.getRefereesDocuments());
		position.setExpertRecommendationDocuments(settings.getExpertsDocuments());
		position.setComparativeAssessmentExpertDocuments(settings.getComparativeExpertDocuments());
		
		int numOfConfigurations = configurations.size();
		for(int i=0;i<numOfConfigurations; i++) {
			configurations.get(i).setDocuments(new HashSet<>(settings.getFacultyMembersDocuments(i)));
			configurations.get(i).setRefereesDocs(settings.getFacultyMembersRefereesDocuments(i));
			configurations.get(i).setExpertsDocs(settings.getFacultyMembersExpertsDocuments(i));
			configurations.get(i).setExpertsComparativeAssessmentDocs(settings.getFacultyMembersExpertsComparativeAssessmentsDocuments(i));
		}
	}

	private void commitDocuments(SingleSelection visibilityEl, EditVisibilityStepSettings settings) {
		String visibility = visibilityEl.getSelectedKey();
		
		List<String> expertSettings = settings.getExpertsDocuments();
		List<String> refereeSettings = settings.getRefereesDocuments();
		List<String> comparativeExpertSettings = settings.getComparativeExpertDocuments();
		int numOfConfigurations = configurations.size();

		if(CUSTOMIZE.equals(visibility)) {
			List<EditVisibilityRow> rows = documentsBundle.tableModel().getObjects();
			for(EditVisibilityRow row:rows) {
				Object uobject = row.getUserObject();
				if(uobject instanceof DocumentEnum) {
					DocumentEnum doc = (DocumentEnum)uobject;
					addVisibility(expertSettings, doc.name(), row.isExpertsSelected());
					addVisibility(refereeSettings, doc.name(), row.isRefereesElSelected());
					addVisibility(comparativeExpertSettings, doc.name(), row.isComparativeExpertsElSelected());
					for(int i=0;i<numOfConfigurations; i++) {
						addVisibility(settings.getFacultyMembersDocuments(i), doc.name(), row.isFacultyMembersElsSelected(i));
					}
				} else if(CHECK_DOCS_REFEREES .equals(uobject)) {
					for(int i=0;i<numOfConfigurations; i++) {
						settings.setFacultyMembersRefereesDocuments(i, row.isFacultyMembersElsSelected(i));
					}
				} else if(CHECK_DOCS_EXPERTS.equals(uobject)) {
					for(int i=0;i<numOfConfigurations; i++) {
						settings.setFacultyMembersExpertsDocuments(i, row.isFacultyMembersElsSelected(i));
					}
				} else if(CHECK_DOCS_COMPARATIVE_ASSESSMENTS.equals(uobject)) {
					for(int i=0;i<numOfConfigurations; i++) {
						settings.setFacultyMembersExpertsComparativeAssessmentsDocuments(i, row.isFacultyMembersElsSelected(i));
					}
				}
			}
		} else {
			expertSettings.add(visibility);
			refereeSettings.add(visibility);
			comparativeExpertSettings.add(visibility);
			for(int i=0;i<numOfConfigurations; i++) {
				settings.getFacultyMembersDocuments(i).add(visibility);
			}
		}
	}
	
	private void addVisibility(List<String> settings, String object, boolean selected) {
		if(selected) {
			settings.add(object);
		} else {
			settings.remove(object);
		}
	}
	
	private void doPreview(UserRequest ureq) {
		EditVisibilityStepSettings settings = new EditVisibilityStepSettings(configurations.size());
		commitFields(settings);
		commitDocuments(documentsBundle.visibilityEl(), settings);
		
		
		previewCtrl = new PositionPreviewProfileVisibilityController(ureq, getWindowControl(),
				position, configurations, settings);
		listenTo(previewCtrl);
		
		String title = translate("edit.template.preview");
		cmc = new CloseableModalController(getWindowControl(), "c", previewCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	public static class VisibilityDataModel extends DefaultFlexiTableDataModel<EditVisibilityRow> {
		
		private static final VisibilityCols[] COLS = VisibilityCols.values();
		
		public VisibilityDataModel(FlexiTableColumnModel columnsModel) {
			super(columnsModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			EditVisibilityRow docSettings = getObject(row);

			if(col >= 0 && col < COLS.length) {
				switch(COLS[col]) {
					case title: return docSettings.getTitle();
					case experts: return docSettings.expertsEl();
					case referees: return docSettings.refereesEl();
					case comparativeExperts: return docSettings.comparativeExpertsEl();
					case publicFeedback: return docSettings.publicFeedbackEl();
					default: return "ERROR";
				}
			}
			
			int checkboxIndex = col - CHECKBOX_OFFSET;
			if(checkboxIndex >= 0 && checkboxIndex < docSettings.facultyMembersEls().length) {
				return docSettings.facultyMembersEls()[checkboxIndex];
			}
			return "ERROR";
		}
	}
	
	public enum VisibilityCols implements FlexiSortableColumnDef {
		title("document.title"),
		experts("document.experts"),
		referees("document.referees"),
		comparativeExperts("document.comparative.experts"),
		publicFeedback("document.public.feedback");

		private final String key;
		
		private VisibilityCols(String key) {
			this.key = key;
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
}
