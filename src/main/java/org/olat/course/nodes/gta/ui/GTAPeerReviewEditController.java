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
package org.olat.course.nodes.gta.ui;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.editor.CourseNodeReferenceProvider;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.ms.MSService;
import org.olat.course.nodes.ms.MinMax;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryReferenceController;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.ReferenceContentProvider;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.SettingsContentProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAPeerReviewEditController extends FormBasicController implements ReferenceContentProvider, SettingsContentProvider {

	private static final List<String> RESOURCE_TYPES = List.of(EvaluationFormResource.TYPE_NAME);

	private final BreadcrumbPanel stackPanel;
	private SingleSelection numOfReviewsEl;
	private SingleSelection formReviewEl;
	private SingleSelection assignmentEl;
	private MultipleSelectionElement relationshipEl;
	private MultipleSelectionElement automaticAssignmentPermissionEl;
	private FormToggle qualityFeedbackEnableEl;
	private SingleSelection qualityFeedbackTypeEl;
	private ComponentWrapperElement referenceEl;
	private IconPanelLabelTextContent iconPanelContent;
	private IconPanelLabelTextContent iconPanelSettings;
	
	private int numberOfAssessments = 0;
	private final ModuleConfiguration config;

	private EvaluationFormExecutionController previewCtr;
	private EvaluationFormSettingsController settingsCtrl;
	private RepositoryEntryReferenceController referenceCtrl;

	@Autowired
	private MSService msService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public GTAPeerReviewEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ModuleConfiguration config) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.config = config;
		this.stackPanel = stackPanel;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer referenceCont = uifactory.addVerticalFormLayout("reference", null, formLayout);
		referenceCont.setFormTitle(translate("peer.review.title"));
		referenceCont.setFormContextHelp("manual_user/learningresources/Course_Element_Task/#configurations");
		initReferenceForm(referenceCont, ureq);
		
		FormLayoutContainer configurationCont = uifactory.addDefaultFormLayout("configuration", null, formLayout);
		configurationCont.setFormTitle(translate("peer.review.configuration.title"));
		initConfigurationForm(configurationCont);
		initConfigurationQualityForm(configurationCont);
		
		FormLayoutContainer permissionsCont = uifactory.addDefaultFormLayout("permissions", null, formLayout);
		permissionsCont.setFormTitle(translate("peer.review.permissions.title"));
		initPermissionsForm(permissionsCont);
		
		FormLayoutContainer buttonCont = uifactory.addButtonsFormLayout("buttons", null, permissionsCont);
		uifactory.addFormSubmitButton("save", "save", buttonCont);
	}
	
	private void initReferenceForm(FormItemContainer formLayout, UserRequest ureq) {
		iconPanelContent = new IconPanelLabelTextContent("content");
		iconPanelContent.setColumnWidth(6);
		iconPanelSettings = new IconPanelLabelTextContent("content");
		iconPanelSettings.setColumnWidth(6);
		
		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withMessageTranslated(translate("no.form.resource.selected"))
				.withIconCss("o_icon o_FileResource-FORM_icon")
				.build();
		String selectionTitle = translate("select.form");
		CourseNodeReferenceProvider referenceProvider = new GTAPeerReviewReferenceProvider(repositoryService,
				RESOURCE_TYPES, emptyStateConfig, selectionTitle, this, this);
		RepositoryEntry formEntry = GTACourseNode.getPeerReviewEvaluationForm(config);
		referenceCtrl = new RepositoryEntryReferenceController(ureq, getWindowControl(), formEntry, referenceProvider);
		listenTo(referenceCtrl);
		
		referenceEl = new ComponentWrapperElement(referenceCtrl.getInitialComponent());
		formLayout.add(referenceEl);
	}
	
	private void initConfigurationForm(FormItemContainer formLayout) {
		// Form of the review
		SelectionValues formReviewPK = new SelectionValues();
		formReviewPK.add(SelectionValues.entry(GTACourseNode.GTASK_PEER_REVIEW_DOUBLE_BLINDED_REVIEW, translate("peer.review.double.blinded.review"),
				translate("peer.review.double.blinded.review.desc"), null, null, true));
		formReviewPK.add(SelectionValues.entry(GTACourseNode.GTASK_PEER_REVIEW_SINGLE_BLINDED_REVIEW, translate("peer.review.single.blinded.review"),
				translate("peer.review.single.blinded.review.desc"), null, null, true));
		formReviewPK.add(SelectionValues.entry(GTACourseNode.GTASK_PEER_REVIEW_OPEN_REVIEW, translate("peer.review.open.review"),
				translate("peer.review.open.review.desc"), null, null, true));
		formReviewEl = uifactory.addCardSingleSelectHorizontal("peer.review.form.review", "peer.review.form.review", formLayout, formReviewPK);
		formReviewEl.setElementCssClass("o_radio_cards_sm");
		String formReview = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_FORM_OF_REVIEW, GTACourseNode.GTASK_PEER_REVIEW_FORM_OF_REVIEW_DEFAULT);
		if(StringHelper.containsNonWhitespace(formReview) && formReviewPK.containsKey(formReview)) {
			formReviewEl.select(formReview, true);
		}
		
		// Assignment
		SelectionValues assignmentsPK = new SelectionValues();
		assignmentsPK.add(SelectionValues.entry(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_SAME_TASK, translate("peer.review.assignment.same.task"),
				translate("peer.review.assignment.same.task.desc"), null, null, true));
		assignmentsPK.add(SelectionValues.entry(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_OTHER_TASK, translate("peer.review.assignment.other.task"),
				translate("peer.review.assignment.other.task.desc"), null, null, true));
		assignmentsPK.add(SelectionValues.entry(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_RANDOM, translate("peer.review.assignment.random"),
				translate("peer.review.assignment.random.desc"), null, null, true));
		assignmentEl = uifactory.addCardSingleSelectHorizontal("peer.review.assignment", "peer.review.assignment", formLayout, assignmentsPK);
		assignmentEl.addActionListener(FormEvent.ONCHANGE);
		String assignment = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT,
				GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_DEFAULT);
		if(StringHelper.containsNonWhitespace(assignment) && assignmentsPK.containsKey(assignment)) {
			assignmentEl.select(assignment, true);
		}
		
		// Relation ship
		SelectionValues relationshipPK = new SelectionValues();
		boolean mutualRelationship = config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW_MUTUAL_REVIEW, false);
		relationshipPK.add(SelectionValues.entry(GTACourseNode.GTASK_PEER_REVIEW_MUTUAL_REVIEW, translate("peer.review.mutual.review")));
		relationshipEl = uifactory.addCheckboxesHorizontal("peer.review.relationship", formLayout, relationshipPK.keys(), relationshipPK.values());
		relationshipEl.select(GTACourseNode.GTASK_PEER_REVIEW_MUTUAL_REVIEW, mutualRelationship);
		
		// Number of reviews	
		SelectionValues numOfReviewsPK = new SelectionValues();
		for(int i=1; i<=5; i++) {
			String val = Integer.toString(i);
			numOfReviewsPK.add(SelectionValues.entry(val, val));
		}
		numOfReviewsEl = uifactory.addDropdownSingleselect("peer.review.num.reviews", formLayout,
				numOfReviewsPK.keys(), numOfReviewsPK.values());
		String numOfReviews = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS,
				GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS_DEFAULT);
		if(numOfReviewsPK.containsKey(numOfReviews)) {
			numOfReviewsEl.select(numOfReviews, true);
		} else {
			numOfReviewsEl.select("3", true);
		}
	}
	
	private void initConfigurationQualityForm(FormItemContainer formLayout) {
		uifactory.addSpacerElement("quality-space", formLayout, false);
		
		boolean qualityFeedback = config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW_QUALITY_FEEDBACK, false);
		qualityFeedbackEnableEl = uifactory.addToggleButton("quality.feedback.enable", "quality.feedback.enable",
				translate("on"), translate("off"), formLayout);
		qualityFeedbackEnableEl.addActionListener(FormEvent.ONCHANGE);
		qualityFeedbackEnableEl.setHelpTextKey("quality.feedback.enable.hint", null);
		qualityFeedbackEnableEl.toggle(qualityFeedback);

		String yesNoDesc = "<span class='o_gta_yes_no'><i class='o_icon o_icon_accepted'> </i> / <i class='o_icon o_icon_rejected'> </i></span>";
		SelectionValues qualityFeedbackPK = new SelectionValues();
		qualityFeedbackPK.add(SelectionValues.entry(GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_YES_NO, translate("quality.feedback.type.yes.no"),
				yesNoDesc, null, null, true));
		
		String starsDesc = "<span class='o_gta_stars'><i class='o_icon o_icon_rating_off'> </i> <i class='o_icon o_icon_rating_off'> </i> <i class='o_icon o_icon_rating_off'> </i> <i class='o_icon o_icon_rating_off'> </i> <i class='o_icon o_icon_rating_off'> </i></span>";
		qualityFeedbackPK.add(SelectionValues.entry(GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_STARS, translate("quality.feedback.type.stars"),
				starsDesc, null, null, true));
		qualityFeedbackTypeEl = uifactory.addCardSingleSelectHorizontal("quality.feedback.type", "quality.feedback.type", formLayout, qualityFeedbackPK);
		qualityFeedbackTypeEl.setVisible(qualityFeedbackEnableEl.isOn());
		qualityFeedbackTypeEl.setElementCssClass("o_gta_feedback_design");
		String feedbackType = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_QUALITY_FEEDBACK_TYPE,
				GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_YES_NO);
		if(qualityFeedbackPK.containsKey(feedbackType)) {
			qualityFeedbackTypeEl.select(feedbackType, true);
		}
	}
	
	private void initPermissionsForm(FormItemContainer formLayout) {
		SelectionValues rolesPK = new SelectionValues();
		rolesPK.add(SelectionValues.entry(GroupRoles.coach.name(), translate("automatic.assignment.role.coach")));
		automaticAssignmentPermissionEl = uifactory.addCheckboxesVertical("automatic.assignment.permissions", formLayout,
				rolesPK.keys(), rolesPK.values(), 1);
		String permissions = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_PERMISSION,
				GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_PERMISSION_DEFAULT);
		String[] roles = permissions.split(",");
		for(String role:roles) {
			if(StringHelper.containsNonWhitespace(role) && rolesPK.containsKey(role)) {
				automaticAssignmentPermissionEl.select(role, true);
			}
		}
	}
	
	@Override
	public Component getContent(RepositoryEntry repositoryEntry) {
		MinMax minMaxAvg = msService.calculateMinMaxAvg(repositoryEntry, 1.0f);
		MinMax minMaxSum = msService.calculateMinMaxSum(repositoryEntry, 1.0f);
		
		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(4);
		if (minMaxSum != null) {
			labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("score.evaluation.sum"), AssessmentHelper.getRoundedScore(minMaxSum.getMax())));
		}
		if (minMaxAvg != null) {
			labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("score.evaluation.avg"), AssessmentHelper.getRoundedScore(minMaxAvg.getMax())));
		}
		iconPanelContent.setLabelTexts(labelTexts);
		return iconPanelContent;
	}

	@Override
	public void refresh(Component cmp, RepositoryEntry repositoryEntry) {
		//
	}

	@Override
	public Component getSettingsContent(RepositoryEntry repositoryEntry) {
		String scoreKey = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM);
		String i18nScoreKey;
		if(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreKey)) {
			i18nScoreKey = "score.evaluation.points.sum";
		} else if(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreKey)) {
			i18nScoreKey = "score.evaluation.points.avg";
		} else {
			i18nScoreKey = "score.evaluation.points.undefined";
		}

		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(4);
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("score.evaluation.points"), translate(i18nScoreKey)));
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("num.of.form.assessment"), Integer.toString(numberOfAssessments)));
		iconPanelSettings.setLabelTexts(labelTexts);
		
		return iconPanelSettings;
	}

	@Override
	public void refreshSettings(Component cmp, RepositoryEntry repositoryEntry) {
		//
	}

	@Override
	public Controller getEditSettingsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry repositoryEntry) {
		removeAsListenerAndDispose(settingsCtrl);
		
		settingsCtrl = new EvaluationFormSettingsController(ureq, wControl, config,
				GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM);
		listenTo(settingsCtrl);
		return settingsCtrl;
	}
	
	private void updateSettingsPanel() {
		String scoreKey = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM);
		String i18nScoreKey;
		if(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreKey)) {
			i18nScoreKey = "score.evaluation.points.sum";
		} else if(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreKey)) {
			i18nScoreKey = "score.evaluation.points.avg";
		} else {
			i18nScoreKey = "score.evaluation.points.undefined";
		}

		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(4);
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("score.evaluation.points"), translate(i18nScoreKey)));
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("num.of.form.assessment"), Integer.toString(numberOfAssessments)));
		iconPanelSettings.setLabelTexts(labelTexts);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (referenceCtrl == source) {
			if (event == RepositoryEntryReferenceController.SELECTION_EVENT) {
				doSaveEvaluation(ureq, referenceCtrl.getRepositoryEntry());
			} else if (event == RepositoryEntryReferenceController.PREVIEW_EVENT) {
				doPreview(ureq, referenceCtrl.getRepositoryEntry());
			}
		} else if(settingsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSaveEvaluation(ureq, referenceCtrl.getRepositoryEntry());
				cleanUp();
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(settingsCtrl);
		settingsCtrl = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateFormLogic(numOfReviewsEl);
		allOk &= validateFormLogic(formReviewEl);
		allOk &= validateFormLogic(assignmentEl);
		if(qualityFeedbackTypeEl.isVisible()) {
			allOk &= validateFormLogic(qualityFeedbackTypeEl);
		}
		
		return allOk;
	}
	
	protected boolean validateFormLogic(SingleSelection select) {
		boolean allOk = true;
		
		select.clearError();
		if(!select.isOneSelected()) {
			select.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(qualityFeedbackEnableEl == source || assignmentEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateUI() {
		qualityFeedbackTypeEl.setVisible(qualityFeedbackEnableEl.isOn());
	
		boolean assignmentSameTask = assignmentEl.isOneSelected()
				&& GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_SAME_TASK.equals(assignmentEl.getSelectedKey());
		relationshipEl.setVisible(assignmentSameTask);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		commitChanges();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void commitChanges() {
		String numOfReviews = numOfReviewsEl.getSelectedKey();
		config.setStringValue(GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS, numOfReviews);
		String formReview = formReviewEl.getSelectedKey();
		config.setStringValue(GTACourseNode.GTASK_PEER_REVIEW_FORM_OF_REVIEW, formReview);
		String assignment = assignmentEl.getSelectedKey();
		config.setStringValue(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT, assignment);
		boolean mutualRelationship = relationshipEl.isVisible() && relationshipEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_PEER_REVIEW_MUTUAL_REVIEW, mutualRelationship);
		
		// Quality feedback
		boolean qualityFeedback = qualityFeedbackEnableEl.isOn();
		config.setBooleanEntry(GTACourseNode.GTASK_PEER_REVIEW_QUALITY_FEEDBACK, qualityFeedback);
		if(qualityFeedback && qualityFeedbackTypeEl.isVisible() && qualityFeedbackTypeEl.isOneSelected()) {
			String qualityFeedbackType = qualityFeedbackTypeEl.getSelectedKey();
			config.setStringValue(GTACourseNode.GTASK_PEER_REVIEW_QUALITY_FEEDBACK_TYPE, qualityFeedbackType);
		} else {
			config.remove(GTACourseNode.GTASK_PEER_REVIEW_QUALITY_FEEDBACK_TYPE);
		}
		
		//Automatic assignment
		Collection<String> roles = automaticAssignmentPermissionEl.getSelectedKeys();
		config.setStringValue(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_PERMISSION, Strings.join(roles, ','));
	}
	
	private void doSaveEvaluation(UserRequest ureq, RepositoryEntry formEntry) {
		String currentEvalScoringMethod = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM);

		GTACourseNode.setPeerReviewEvaluationFormReference(formEntry, config);
		if(!StringHelper.containsNonWhitespace(currentEvalScoringMethod)) {
			config.setStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM, MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM);
		}

		Float peerReviewScale = GTACourseNode.getFloatConfiguration(config,
				GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM_SCALE, MSCourseNode.CONFIG_DEFAULT_SCORE_SCALING);
		Float evaluationScale = GTACourseNode.getFloatConfiguration(config,
				MSCourseNode.CONFIG_KEY_EVAL_FORM_SCALE, MSCourseNode.CONFIG_DEFAULT_SCORE_SCALING);
		Float scoreProReview = GTACourseNode.getFloatConfiguration(config,
				GTACourseNode.GTASK_PEER_REVIEW_SCORE_PRO_REVIEW, null);
		Integer numOfReviews = GTACourseNode.getIntegerConfiguration(config,
				GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS,
				GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS_DEFAULT);
		Integer maxNumberCreditableReviews = GTACourseNode.getIntegerConfiguration(config,
				GTACourseNode.GTASK_PEER_REVIEW_MAX_NUMBER_CREDITABLE_REVIEWS,
				numOfReviews == null ? "1" : numOfReviews.toString());
		String scoreParts = config.getStringValue(GTACourseNode.GTASK_SCORE_PARTS, "");

		MinMax minMax = GTACourseNode.calculateMinMaxTotal(config, evaluationScale, peerReviewScale,
				maxNumberCreditableReviews, scoreProReview, scoreParts);
		if(minMax != null) {
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, minMax.getMin());
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, minMax.getMax());
		}

		updateSettingsPanel();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doPreview(UserRequest ureq, RepositoryEntry formEntry) {
		File repositoryDir = FileResourceManager.getInstance().getFileResourceZipDir(formEntry.getOlatResource());
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		DataStorage storage = evaluationFormManager.loadStorage(formEntry);
		previewCtr = new EvaluationFormExecutionController(ureq, getWindowControl(), formFile, storage,
				FormCourseNode.EMPTY_STATE);
		listenTo(previewCtr);

		stackPanel.pushController(translate("preview"), previewCtr);
	}
	
	public class GTAPeerReviewReferenceProvider extends CourseNodeReferenceProvider {
		
		private final SettingsContentProvider settingsProvider;
		
		public GTAPeerReviewReferenceProvider(RepositoryService repositoryService, List<String> resourceTypes,
				EmptyStateConfig emptyStateConfig, String selectionTitle, ReferenceContentProvider referenceContentProvider,
				SettingsContentProvider settingsProvider) {
			super(repositoryService, resourceTypes,  emptyStateConfig, selectionTitle, referenceContentProvider);
			this.settingsProvider = settingsProvider;
		}
		
		@Override
		public boolean hasSettings() {
			return true;
		}

		@Override
		public SettingsContentProvider getSettingsContentProvider() {
			return settingsProvider;
		}
		
		public boolean canCreate() {
			return numberOfAssessments == 0;
		}

		@Override
		public boolean canImport() {
			return numberOfAssessments == 0;
		}

		@Override
		public boolean isReplaceable(RepositoryEntry repositoryEntry) {
			return numberOfAssessments == 0 && super.isReplaceable(repositoryEntry);
		}

		@Override
		public boolean isEditable(RepositoryEntry repositoryEntry, Identity identity) {
			return numberOfAssessments == 0 && super.isEditable(repositoryEntry, identity);
		}

		@Override
		public boolean isSettingsEditable(RepositoryEntry repositoryEntry, Identity identity) {
			return true;
		}
	}
}
