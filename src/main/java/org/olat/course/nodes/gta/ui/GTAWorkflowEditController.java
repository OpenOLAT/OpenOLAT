/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.condition.AreaSelectionController;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTARelativeToDates;
import org.olat.course.nodes.gta.GTAType;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAWorkflowEditController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] executionKeys = new String[]{ GTAType.group.name(), GTAType.individual.name() };
	
	private static final String[] optionalKeys = new String[] { "mandatory", "optional" };
	private static final String[] solutionVisibleToAllKeys = new String[] { "all", "restricted" };
	
	private CloseableModalController cmc;
	private DialogBoxController confirmChangesCtrl;
	private AreaSelectionController areaSelectionCtrl;
	private GroupSelectionController groupSelectionCtrl;
	
	private SingleSelection typeEl;
	private SingleSelection optionalEl;
	private FormLink chooseGroupButton;
	private FormLink chooseAreaButton;
	private StaticTextElement groupListEl, areaListEl;
	private DateChooser assignmentDeadlineEl, submissionDeadlineEl, solutionVisibleAfterEl;
	private MultipleSelectionElement relativeDatesEl, taskAssignmentEl, submissionEl, reviewEl, revisionEl, sampleEl, gradingEl;
	private FormLayoutContainer stepsCont, assignmentRelDeadlineCont, submissionRelDeadlineCont, solutionVisibleRelCont;
	private TextElement assignementDeadlineDaysEl, submissionDeadlineDaysEl, solutionVisibleRelDaysEl;
	private SingleSelection assignementDeadlineRelToEl;
	private SingleSelection submissionDeadlineRelToEl;
	private SingleSelection solutionVisibleRelToEl;
	private SingleSelection solutionVisibleToAllEl;
	private FormLayoutContainer documentsCont;
	private MultipleSelectionElement coachAllowedUploadEl;
	
	private final GTACourseNode gtaNode;
	private final ModuleConfiguration config;
	private boolean optional;
	private final CourseEditorEnv courseEditorEnv;
	private List<Long> areaKeys;
	private List<Long> groupKeys;
	private final RepositoryEntry courseRe;
	private final boolean isLearningPath;
	
	@Autowired
	private HelpModule helpModule;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private RepositoryService repositoryService;
	
	public GTAWorkflowEditController(UserRequest ureq, WindowControl wControl, GTACourseNode gtaNode, CourseEditorEnv courseEditorEnv) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.gtaNode = gtaNode;
		this.config = gtaNode.getModuleConfiguration();
		this.courseEditorEnv = courseEditorEnv;
		
		//reload to make sure we have the last changes
		courseRe = repositoryService
				.loadByKey(courseEditorEnv.getCourseGroupManager().getCourseEntry().getKey());
		ICourse course = CourseFactory.loadCourse(courseRe);
		isLearningPath = LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType());
		
		optional = config.getStringValue(GTACourseNode.GTASK_OBLIGATION).equals(AssessmentObligation.optional.name());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initTypeForm(formLayout);
		initStepForm(formLayout);
		initDocumentsForm(formLayout);
		initButtonsForm(formLayout, ureq);
	}
	
	private void initTypeForm(FormItemContainer formLayout) {
		String type = config.getStringValue(GTACourseNode.GTASK_TYPE);
		
		FormLayoutContainer typeCont = FormLayoutContainer.createDefaultFormLayout("type", getTranslator());
		typeCont.setFormTitle(translate("task.type.title"));
		typeCont.setFormDescription(translate("task.type.description"));
		typeCont.setElementCssClass("o_sel_course_gta_groups_areas");
		typeCont.setRootForm(mainForm);
		formLayout.add(typeCont);
		
		String[] executionValues = new String[]{
				translate("task.execution.group"),
				translate("task.execution.individual")
		};

		typeEl = uifactory.addDropdownSingleselect("execution", "task.execution", typeCont, executionKeys, executionValues, null);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		typeEl.setEnabled(false);
		if(StringHelper.containsNonWhitespace(type)) {
			for(String executionKey:executionKeys) {
				if(executionKey.equals(type)) {
					typeEl.select(executionKey, true);
				}
			}
		}
		if(!typeEl.isOneSelected()) {
			typeEl.select(executionKeys[0], true);
		}

		chooseGroupButton = uifactory.addFormLink("choose.groups", "choose.groups", "choosed.groups", typeCont, Link.BUTTON_XSMALL);
		chooseGroupButton.setCustomEnabledLinkCSS("btn btn-default o_xsmall o_form_groupchooser");
		chooseGroupButton.setElementCssClass("o_omit_margin");
		chooseGroupButton.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		if(!courseEditorEnv.getCourseGroupManager().hasBusinessGroups()){
			chooseGroupButton.setI18nKey("create.groups");
		}
		
		groupKeys = config.getList(GTACourseNode.GTASK_GROUPS, Long.class);
		String groupList = getGroupNames(groupKeys);
		groupListEl = uifactory.addStaticTextElement("group.list", null, groupList, typeCont);		
		groupListEl.setElementCssClass("text-muted");
		groupListEl.setLabel(null, null);
		
		chooseAreaButton = uifactory.addFormLink("choose.areas", "choose.areas", "choosed.areas", typeCont, Link.BUTTON_XSMALL);
		chooseAreaButton.setCustomEnabledLinkCSS("btn btn-default o_xsmall o_form_areachooser");
		chooseAreaButton.setElementCssClass("o_omit_margin");
		chooseAreaButton.setIconLeftCSS("o_icon o_icon-fw o_icon_courseareas");
		if(!courseEditorEnv.getCourseGroupManager().hasAreas()){
			chooseAreaButton.setI18nKey("create.areas");
		}
		
		areaKeys = config.getList(GTACourseNode.GTASK_AREAS, Long.class);
		String areaList = getAreaNames(areaKeys);
		areaListEl = uifactory.addStaticTextElement("areas.list", null, areaList, typeCont);
		areaListEl.setElementCssClass("text-muted");
		areaListEl.setLabel(null, null);
		
		boolean mismatch = ((GTAType.group.name().equals(type) && !gtaNode.getType().equals(GTACourseNode.TYPE_GROUP))
				|| (GTAType.individual.name().equals(type) && !gtaNode.getType().equals(GTACourseNode.TYPE_INDIVIDUAL)));
		
		if(GTAType.group.name().equals(type)) {
			typeEl.setVisible(mismatch);
		} else if(GTAType.individual.name().equals(type)) {
			if(mismatch) {
				typeEl.setVisible(true);
				chooseGroupButton.setVisible(false);
				groupListEl.setVisible(false);
				chooseAreaButton.setVisible(false);
				areaListEl.setVisible(false);
			} else {
				typeCont.setVisible(false);
			}
		}
	}
	
	private void initStepForm(FormItemContainer formLayout) {
		//Steps
		stepsCont = FormLayoutContainer.createDefaultFormLayout("steps", getTranslator());
		stepsCont.setFormTitle(translate("task.steps.title"));
		stepsCont.setFormDescription(translate("task.steps.description"));
		stepsCont.setElementCssClass("o_sel_course_gta_steps");
		stepsCont.setRootForm(mainForm);
		stepsCont.setFormContextHelp("Three Steps to Your Task#_task_configuration");
		formLayout.add(stepsCont);

		String[] optionalValues = new String[] {
				translate("task.mandatory"), translate("task.optional"),
		};
		optionalEl = uifactory.addRadiosHorizontal("obligation", "task.obligation", stepsCont, optionalKeys, optionalValues);
		optionalEl.addActionListener(FormEvent.ONCHANGE);	
		if(optional) {
			optionalEl.select(optionalKeys[1], true);
		} else {
			optionalEl.select(optionalKeys[0], true);
		}
		optionalEl.setVisible(!isLearningPath);

		relativeDatesEl = uifactory.addCheckboxesHorizontal("relative.dates", "relative.dates", stepsCont, onKeys, new String[]{ "" });
		relativeDatesEl.addActionListener(FormEvent.ONCHANGE);
		boolean useRelativeDates = config.getBooleanSafe(GTACourseNode.GTASK_RELATIVE_DATES);
		relativeDatesEl.select(onKeys[0], useRelativeDates);
		
		uifactory.addSpacerElement("s1", stepsCont, true);
		
		//assignment
		String[] assignmentValues = new String[] { translate("task.assignment.enabled") };
		taskAssignmentEl = uifactory.addCheckboxesHorizontal("task.assignment", "task.assignment", stepsCont, onKeys, assignmentValues);
		taskAssignmentEl.addActionListener(FormEvent.ONCHANGE);
		boolean assignement = config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT);
		taskAssignmentEl.select(onKeys[0], assignement);
		
		Date assignmentDeadline = config.getDateValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
		assignmentDeadlineEl = uifactory.addDateChooser("assignementdeadline", "assignment.deadline", assignmentDeadline, stepsCont);
		assignmentDeadlineEl.setDateChooserTimeEnabled(true);
		assignmentDeadlineEl.setVisible(assignement && !useRelativeDates);
		
		String relativeDatePage = velocity_root + "/assignment_relative_date.html";
		assignmentRelDeadlineCont = FormLayoutContainer.createCustomFormLayout("assignmentRelativeDeadline", getTranslator(), relativeDatePage);
		assignmentRelDeadlineCont.setRootForm(mainForm);
		assignmentRelDeadlineCont.setLabel("assignment.deadline", null);
		assignmentRelDeadlineCont.setVisible(assignement && useRelativeDates);
		stepsCont.add(assignmentRelDeadlineCont);
		
		int numOfDays = config.getIntegerSafe(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE, -1);
		String assignmentNumOfDays = numOfDays >= 0 ? Integer.toString(numOfDays) : "";
		String assignmentRelativeTo = config.getStringValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE_TO);
		assignementDeadlineDaysEl = uifactory.addTextElement("assignment.numOfDays", null, 4, assignmentNumOfDays, assignmentRelDeadlineCont);
		assignementDeadlineDaysEl.setDisplaySize(4);
		assignementDeadlineDaysEl.setDomReplacementWrapperRequired(false);
		RelativeDateKeysAndValues assignmentKeysAndValues = getRelativesDatesOption(true);
		assignementDeadlineRelToEl = uifactory
				.addDropdownSingleselect("assignmentrelativeto", "assignment.relative.to", null, assignmentRelDeadlineCont,
						assignmentKeysAndValues.getKeys(), assignmentKeysAndValues.getValues(), null);
		assignementDeadlineRelToEl.setDomReplacementWrapperRequired(false);
		
		boolean found = false;
		if(StringHelper.containsNonWhitespace(assignmentRelativeTo)) {
			for(String relativeDatesKey:assignmentKeysAndValues.getKeys()) {
				if(relativeDatesKey.equals(assignmentRelativeTo)) {
					assignementDeadlineRelToEl.select(relativeDatesKey, true);
					found = true;
				}
			}
		}
		if(!found) {
			assignementDeadlineRelToEl.select(assignmentKeysAndValues.getKeys()[0], true);
		}
		
		uifactory.addSpacerElement("s2", stepsCont, true);

		//turning in
		String[] submissionValues = new String[] { translate("submission.enabled") };
		submissionEl = uifactory.addCheckboxesHorizontal("submission", "submission", stepsCont, onKeys, submissionValues);
		submissionEl.addActionListener(FormEvent.ONCHANGE);
		boolean submit = config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT);
		submissionEl.select(onKeys[0], submit);
		
		Date submissionDeadline = config.getDateValue(GTACourseNode.GTASK_SUBMIT_DEADLINE);
		submissionDeadlineEl = uifactory.addDateChooser("submitdeadline", "submit.deadline", submissionDeadline, stepsCont);
		submissionDeadlineEl.setDateChooserTimeEnabled(true);
		submissionDeadlineEl.setVisible(submit && !useRelativeDates);

		//relative deadline
		String submitPage = velocity_root + "/submit_relative_date.html";
		submissionRelDeadlineCont = FormLayoutContainer.createCustomFormLayout("submitRelativeDeadline", getTranslator(), submitPage);
		submissionRelDeadlineCont.setRootForm(mainForm);
		submissionRelDeadlineCont.setLabel("submit.deadline", null);
		submissionRelDeadlineCont.setVisible(submit && useRelativeDates);
		stepsCont.add(submissionRelDeadlineCont);
		
		numOfDays = config.getIntegerSafe(GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE, -1);
		String submitRelDays = numOfDays >= 0 ? Integer.toString(numOfDays) : "";
		String submitRelTo = config.getStringValue(GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE_TO);
		submissionDeadlineDaysEl = uifactory.addTextElement("submit.numOfDays", null, 4, submitRelDays, submissionRelDeadlineCont);
		submissionDeadlineDaysEl.setDomReplacementWrapperRequired(false);
		submissionDeadlineDaysEl.setDisplaySize(4);
		RelativeDateKeysAndValues submissionKeysAndValues = getRelativesDatesOption(false);
		submissionDeadlineRelToEl = uifactory
				.addDropdownSingleselect("submitrelativeto", "submit.relative.to", null, submissionRelDeadlineCont,
						submissionKeysAndValues.getKeys(), submissionKeysAndValues.getValues(), null);
		submissionDeadlineRelToEl.setDomReplacementWrapperRequired(false);
		found = false;
		if(StringHelper.containsNonWhitespace(submitRelTo)) {
			for(String relativeDatesKey:submissionKeysAndValues.getKeys()) {
				if(relativeDatesKey.equals(submitRelTo)) {
					submissionDeadlineRelToEl.select(relativeDatesKey, true);
					found = true;
				}
			}
		}
		if(!found) {
			submissionDeadlineRelToEl.select(submissionKeysAndValues.getKeys()[0], true);
		}
		
		uifactory.addSpacerElement("s3", stepsCont, true);

		//review and correction
		String[] reviewValues = new String[] { translate("review.enabled") };
		reviewEl = uifactory.addCheckboxesHorizontal("review", "review.and.correction", stepsCont, onKeys, reviewValues);
		reviewEl.addActionListener(FormEvent.ONCHANGE);
		boolean review = config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION);
		reviewEl.select(onKeys[0], review);
		
		//revision
		String[] revisionValues = new String[] { translate("revision.enabled") };
		revisionEl = uifactory.addCheckboxesHorizontal("revision", "revision.period", stepsCont, onKeys, revisionValues);
		revisionEl.addActionListener(FormEvent.ONCHANGE);
		boolean revision = config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD);
		revisionEl.select(onKeys[0], revision);
		revisionEl.setVisible(review);
		
		uifactory.addSpacerElement("s4", stepsCont, true);

		//sample solution
		String[] sampleValues = new String[] { translate("sample.solution.enabled") };
		sampleEl = uifactory.addCheckboxesHorizontal("sample", "sample.solution", stepsCont, onKeys, sampleValues);
		sampleEl.addActionListener(FormEvent.ONCHANGE);
		boolean sample = config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION);
		sampleEl.select(onKeys[0], sample);
		
		Date solutionVisibleAfter = config.getDateValue(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER);
		solutionVisibleAfterEl = uifactory.addDateChooser("visibleafter", "sample.solution.visible.after", solutionVisibleAfter, stepsCont);
		solutionVisibleAfterEl.setDateChooserTimeEnabled(true);
		solutionVisibleAfterEl.setVisible(sample && !useRelativeDates);
		solutionVisibleAfterEl.addActionListener(FormEvent.ONCHANGE);

		//relative deadline
		String solutionPage = velocity_root + "/solution_relative_date.html";
		solutionVisibleRelCont = FormLayoutContainer.createCustomFormLayout("solutionRelativeDeadline", getTranslator(), solutionPage);
		solutionVisibleRelCont.setRootForm(mainForm);
		solutionVisibleRelCont.setLabel("sample.solution.visible.after", null);
		solutionVisibleRelCont.setVisible(sample && useRelativeDates);
		stepsCont.add(solutionVisibleRelCont);
		
		numOfDays = config.getIntegerSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE, -1);
		String solutionRelDays = numOfDays >= 0 ? Integer.toString(numOfDays) : "";
		String solutionRelTo = config.getStringValue(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE_TO);
		solutionVisibleRelDaysEl = uifactory.addTextElement("solution.numOfDays", null, 4, solutionRelDays, solutionVisibleRelCont);
		solutionVisibleRelDaysEl.setDisplaySize(4);
		solutionVisibleRelDaysEl.setDomReplacementWrapperRequired(false);
		RelativeDateKeysAndValues solutionKeysAndValues = getRelativesDatesOption(false);
		solutionVisibleRelToEl = uifactory
				.addDropdownSingleselect("solutionrelativeto", "solution.relative.to", null, solutionVisibleRelCont,
						solutionKeysAndValues.getKeys(), solutionKeysAndValues.getValues(), null);
		solutionVisibleRelToEl.setDomReplacementWrapperRequired(false);
		found = false;
		if(StringHelper.containsNonWhitespace(solutionRelTo)) {
			for(String relativeDatesKey:solutionKeysAndValues.getKeys()) {
				if(relativeDatesKey.equals(solutionRelTo)) {
					solutionVisibleRelToEl.select(relativeDatesKey, true);
					found = true;
				}
			}
		}
		if(!found) {
			solutionVisibleRelToEl.select(solutionKeysAndValues.getKeys()[0], true);
		}
		
		boolean solutionVisibleRelToAll = config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, false);
		String[] solutionVisibleToAllValues = getSolutionVisibleToAllValues();
		solutionVisibleToAllEl = uifactory.addRadiosVertical("visibleall", "sample.solution.visible.for", stepsCont, solutionVisibleToAllKeys, solutionVisibleToAllValues);
		solutionVisibleToAllEl.setVisible(sample && ((!useRelativeDates && solutionVisibleAfter != null) || optional));
		if(solutionVisibleRelToAll) {
			solutionVisibleToAllEl.select(solutionVisibleToAllKeys[0], true);
		} else {
			solutionVisibleToAllEl.select(solutionVisibleToAllKeys[1], true);
		}
		
		uifactory.addSpacerElement("s5", stepsCont, true);

		//grading
		String[] gradingValues = new String[] { translate("grading.enabled") };
		gradingEl = uifactory.addCheckboxesHorizontal("grading", "grading", stepsCont, onKeys, gradingValues);
		gradingEl.addActionListener(FormEvent.ONCHANGE);
		boolean grading = config.getBooleanSafe(GTACourseNode.GTASK_GRADING);
		gradingEl.select(onKeys[0], grading);
	}
	
	private String[] getSolutionVisibleToAllValues() {
		return new String[] {
			optional ? translate("sample.solution.visible.all.optional") : translate("sample.solution.visible.all"),
			translate("sample.solution.visible.upload")
		};
	}
	
	private void initDocumentsForm(FormItemContainer formLayout) {
		documentsCont = FormLayoutContainer.createDefaultFormLayout("documents", getTranslator());
		documentsCont.setFormTitle(translate("task.documents.title"));
		documentsCont.setRootForm(mainForm);
		formLayout.add(documentsCont);
		
		//coach allowed to upload documents
		String[] onValues = new String[]{ translate("task.manage.documents.coach") };
		coachAllowedUploadEl = uifactory.addCheckboxesVertical("coachTasks", "task.manage.documents", documentsCont, onKeys, onValues, 1);
		boolean coachUpload = config.getBooleanSafe(GTACourseNode.GTASK_COACH_ALLOWED_UPLOAD_TASKS, false);
		if(coachUpload) {
			coachAllowedUploadEl.select(onKeys[0], true);
		}
		
		updateDocuments();
	}
	
	private void initButtonsForm(FormItemContainer formLayout, UserRequest ureq) {
		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("buttonswrapper", getTranslator());
		buttonsWrapperCont.setRootForm(mainForm);
		formLayout.add(buttonsWrapperCont);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		buttonCont.setElementCssClass("o_sel_course_gta_save_workflow");
		buttonsWrapperCont.add(buttonCont);
		uifactory.addFormSubmitButton("save", "save", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		typeEl.clearError();
		if(!typeEl.isOneSelected()) {
			typeEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		boolean relativeDates = relativeDatesEl.isAtLeastSelected(1);
		assignementDeadlineDaysEl.clearError();
		if(relativeDates && taskAssignmentEl.isAtLeastSelected(1)) {
			allOk &= validateIntegerOrEmpty(assignementDeadlineDaysEl);
		}
		
		submissionDeadlineDaysEl.clearError();
		if(relativeDates && submissionEl.isAtLeastSelected(1)) {
			allOk &= validateIntegerOrEmpty(submissionDeadlineDaysEl);
		}
		
		solutionVisibleRelDaysEl.clearError();
		if(relativeDates && sampleEl.isAtLeastSelected(1)) {
			allOk &= validateIntegerOrEmpty(solutionVisibleRelDaysEl);
		}
		
		solutionVisibleToAllEl.clearError();
		if(solutionVisibleToAllEl.isVisible() && !solutionVisibleToAllEl.isOneSelected()) {
			typeEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		taskAssignmentEl.clearError();
		if(!taskAssignmentEl.isAtLeastSelected(1) && !submissionEl.isAtLeastSelected(1)
				&& !reviewEl.isAtLeastSelected(1) && !revisionEl.isAtLeastSelected(1)
				&& !sampleEl.isAtLeastSelected(1) && !gradingEl.isAtLeastSelected(1)) {

			taskAssignmentEl.setErrorKey("error.select.atleastonestep", null);
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean validateIntegerOrEmpty(TextElement textEl) {
		boolean allOk = true;
		textEl.clearError();
		String val = textEl.getValue();
		if(StringHelper.containsNonWhitespace(val) && !StringHelper.isLong(val)) {
			textEl.setErrorKey("integer.element.int.error", null);
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		RepositoryEntry entry = courseEditorEnv.getCourseGroupManager().getCourseEntry();
		if(gtaManager.isTasksInProcess(entry, gtaNode)) {
			doConfirmChanges(ureq);
		} else {
			commitChanges();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	private void doConfirmChanges(UserRequest ureq) {
		String title = translate("warning.tasks.in.process.title");
		String url = helpModule.getManualProvider().getURL(getLocale(), "Task - Further Configurations#_concurrent_edits");
		String text = translate("warning.tasks.in.process.text", new String[]{ url });
		confirmChangesCtrl = activateOkCancelDialog(ureq, title, text, confirmChangesCtrl);
	}
	
	private void commitChanges() {
		if(typeEl.isSelected(0)) {
			config.setStringValue(GTACourseNode.GTASK_TYPE, GTAType.group.name());
			config.setList(GTACourseNode.GTASK_AREAS, areaKeys);
			config.setList(GTACourseNode.GTASK_GROUPS, groupKeys);
		} else {
			config.setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
			config.setList(GTACourseNode.GTASK_AREAS, new ArrayList<Long>(0));
			config.setList(GTACourseNode.GTASK_GROUPS, new ArrayList<Long>(0));
		}
		
		if (optionalEl.isVisible()) {
			config.setStringValue(GTACourseNode.GTASK_OBLIGATION, AssessmentObligation.optional.name());
		}
		
		boolean relativeDates = relativeDatesEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_RELATIVE_DATES, relativeDates);
		
		boolean assignment = taskAssignmentEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_ASSIGNMENT, assignment);
		if(assignment) {
			if(relativeDates) {
				setRelativeDates(assignementDeadlineDaysEl, GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE,
						assignementDeadlineRelToEl, GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE_TO);
			} else {
				config.setDateValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE, assignmentDeadlineEl.getDate());
			}	
		} else {
			config.remove(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
		}
		
		boolean turningIn = submissionEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_SUBMIT, turningIn);
		if(turningIn) {
			if(relativeDates) {
				setRelativeDates(submissionDeadlineDaysEl, GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE,
						submissionDeadlineRelToEl, GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE_TO);
			} else {
				config.setDateValue(GTACourseNode.GTASK_SUBMIT_DEADLINE, submissionDeadlineEl.getDate());
			}
		} else {
			config.remove(GTACourseNode.GTASK_SUBMIT_DEADLINE);
		}

		boolean review = reviewEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_REVIEW_AND_CORRECTION, review);
		if(review) {
			config.setBooleanEntry(GTACourseNode.GTASK_REVISION_PERIOD, revisionEl.isAtLeastSelected(1));
		} else {
			config.setBooleanEntry(GTACourseNode.GTASK_REVISION_PERIOD, false);
		}
		
		boolean sample = sampleEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_SAMPLE_SOLUTION, sample);
		if(sample) {
			config.setBooleanEntry(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, solutionVisibleToAllEl.isSelected(0));
			if(relativeDates) {
				setRelativeDates(solutionVisibleRelDaysEl, GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE,
						solutionVisibleRelToEl, GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE_TO);
			} else {
				config.setDateValue(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER, solutionVisibleAfterEl.getDate());
			}
		} else {
			config.remove(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER);
			config.remove(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL);
		}

		config.setBooleanEntry(GTACourseNode.GTASK_GRADING, gradingEl.isAtLeastSelected(1));
		
		if (documentsCont.isVisible()) {
			boolean coachUploadAllowed = coachAllowedUploadEl.isAtLeastSelected(1);
			config.setBooleanEntry(GTACourseNode.GTASK_COACH_ALLOWED_UPLOAD_TASKS, coachUploadAllowed);
		}
	}
	
	private void setRelativeDates(TextElement daysEl, String daysKey, SingleSelection relativeToEl, String relativeToKey) {
		String val = daysEl.getValue();
		if(StringHelper.isLong(val)) {
			try {
				config.setIntValue(daysKey,  Integer.parseInt(val));
			} catch (NumberFormatException e) {
				logWarn("", e);
			}
			
			String relativeTo = relativeToEl.getSelectedKey();
			config.setStringValue(relativeToKey, relativeTo);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(submissionEl == source) {
			updateSubmissionDeadline();
		} else if(taskAssignmentEl == source) {
			updateAssignmentDeadline();
			updateDocuments();
		} else if(relativeDatesEl == source) {
			updateAssignmentDeadline();
			updateSubmissionDeadline();
			updateSolutionDeadline();
		} else if(sampleEl == source || solutionVisibleAfterEl == source || optionalEl == source) {
			updateSolutionDeadline();
			updateDocuments();
		} else if (reviewEl == source) {
			updateRevisions();
		} else if(chooseGroupButton == source) {
			doChooseGroup(ureq);
		} else if(chooseAreaButton == source) {
			doChooseArea(ureq);
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	public void onNodeConfigChanged() {
		boolean newOptional = config.getStringValue(GTACourseNode.GTASK_OBLIGATION).equals(AssessmentObligation.optional.name());
		if (newOptional != optional) {
			optional = newOptional;
			updateSolutionDeadline();
		}
	}
	
	private void updateAssignmentDeadline() {
		boolean useRelativeDate = relativeDatesEl.isAtLeastSelected(1);
		boolean assignment = taskAssignmentEl.isAtLeastSelected(1);
		assignmentDeadlineEl.setVisible(assignment && !useRelativeDate);
		assignmentRelDeadlineCont.setVisible(assignment && useRelativeDate);
		updateDeadline(assignementDeadlineRelToEl, true);
	}
	
	private void updateSubmissionDeadline() {
		boolean useRelativeDate = relativeDatesEl.isAtLeastSelected(1);
		boolean submit = submissionEl.isAtLeastSelected(1);
		submissionDeadlineEl.setVisible(submit && !useRelativeDate);
		submissionRelDeadlineCont.setVisible(submit && useRelativeDate);
		updateDeadline(submissionDeadlineRelToEl, false);
	}
	
	private void updateSolutionDeadline() {
		boolean useRelativeDate = relativeDatesEl.isAtLeastSelected(1);
		boolean solution = sampleEl.isAtLeastSelected(1);
		if (optionalEl.isVisible()) {
			optional = optionalEl.isSelected(1);
		}
		solutionVisibleAfterEl.setVisible(solution && !useRelativeDate);
		solutionVisibleRelCont.setVisible(solution && useRelativeDate);
		updateDeadline(solutionVisibleRelToEl, false);
		solutionVisibleToAllEl.setVisible(solution &&
				((!useRelativeDate && solutionVisibleAfterEl.getDate() != null) || optional));
		solutionVisibleToAllEl.setKeysAndValues(solutionVisibleToAllKeys, getSolutionVisibleToAllValues(), null);
		if(!solutionVisibleToAllEl.isOneSelected()) {
			solutionVisibleToAllEl.select(solutionVisibleToAllKeys[1], true);
		}
	}
	
	private void updateDeadline(SingleSelection selectionEl, boolean excludeAssignment) {
		String selectedKey = null;
		if(selectionEl.isOneSelected()) {
			selectedKey = selectionEl.getSelectedKey();
		}
		RelativeDateKeysAndValues keysAndValues = getRelativesDatesOption(excludeAssignment);
		selectionEl.setKeysAndValues(keysAndValues.getKeys(), keysAndValues.getValues(), null);
		
		boolean found = false;
		for(String key:keysAndValues.getKeys()) {
			if(key.equals(selectedKey)) {
				selectionEl.select(key, true);
				found = true;
			}	
		}
		
		if(!found) {
			selectionEl.select(keysAndValues.getKeys()[0], true);
		}
	}
	
	private RelativeDateKeysAndValues getRelativesDatesOption(boolean excludeAssignment) {
		List<String> optionKeys = new ArrayList<>(4);
		List<String> optionValues = new ArrayList<>(4);
		
		if(courseRe.getLifecycle() != null && courseRe.getLifecycle().getValidFrom() != null) {
			Date validFrom = courseRe.getLifecycle().getValidFrom();
			String from = Formatter.getInstance(getLocale()).formatDate(validFrom);
			optionKeys.add(GTARelativeToDates.courseStart.name());
			optionValues.add(translate("relative.to.course.start", new String[]{ from }));
		}
		
		optionKeys.add(GTARelativeToDates.courseLaunch.name());
		optionValues.add(translate("relative.to.course.launch"));
		optionKeys.add(GTARelativeToDates.enrollment.name());
		optionValues.add(translate("relative.to.enrollment"));
		
		if(!excludeAssignment) {
			boolean assignment = taskAssignmentEl.isAtLeastSelected(1);
			if(assignment) {
				optionKeys.add(GTARelativeToDates.assignment.name());
				optionValues.add(translate("relative.to.assignment"));
			}
		}
		
		String[] keys = optionKeys.toArray(new String[optionKeys.size()]);
		String[] values = optionValues.toArray(new String[optionValues.size()]);
		return new RelativeDateKeysAndValues(keys, values);
	}
	
	private void updateRevisions() {
		boolean review = reviewEl.isAtLeastSelected(1);
		revisionEl.setVisible(review);
		revisionEl.select(onKeys[0], review);
	}
	
	private void updateDocuments() {
		boolean visible = taskAssignmentEl.isAtLeastSelected(1) || sampleEl.isAtLeastSelected(1);
		documentsCont.setVisible(visible);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(groupSelectionCtrl == source) {
			if (event == Event.DONE_EVENT) {
				groupKeys = groupSelectionCtrl.getSelectedKeys();
				groupListEl.setValue(getGroupNames(groupKeys));
				if(courseEditorEnv.getCourseGroupManager().hasBusinessGroups()) {
					chooseGroupButton.setI18nKey("choose.groups");
				} else {
					chooseGroupButton.setI18nKey("create.groups");
				}
				cmc.deactivate();
				cleanUp();
				groupListEl.getRootForm().submit(ureq);
			} else if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
			}
		} else if(areaSelectionCtrl == source) {
			if (event == Event.DONE_EVENT) {
				areaKeys = areaSelectionCtrl.getSelectedKeys();
				areaListEl.setValue(getAreaNames(areaKeys));
				if(courseEditorEnv.getCourseGroupManager().hasAreas()) {
					chooseAreaButton.setI18nKey("choose.areas");
				} else {
					chooseAreaButton.setI18nKey("create.areas");
				}
				cmc.deactivate();
				cleanUp();
				areaListEl.getRootForm().submit(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
			}
		} else if(confirmChangesCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				commitChanges();
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(groupSelectionCtrl);
		removeAsListenerAndDispose(areaSelectionCtrl);
		removeAsListenerAndDispose(cmc);
		groupSelectionCtrl = null;
		areaSelectionCtrl = null;
		cmc = null;
	}
	
	private void doChooseGroup(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(groupSelectionCtrl);

		groupSelectionCtrl = new GroupSelectionController(ureq, getWindowControl(), true,
				courseEditorEnv.getCourseGroupManager(), groupKeys);
		listenTo(groupSelectionCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", groupSelectionCtrl.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseArea(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(areaSelectionCtrl);
		
		areaSelectionCtrl = new AreaSelectionController (ureq, getWindowControl(), true,
				courseEditorEnv.getCourseGroupManager(), areaKeys);
		listenTo(areaSelectionCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", areaSelectionCtrl.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private String getGroupNames(List<Long> groupKeyList) {
		StringBuilder sb = new StringBuilder(64);
		List<BusinessGroupShort> groups = businessGroupService.loadShortBusinessGroups(groupKeyList);
		for(BusinessGroupShort group:groups) {
			if(sb.length() > 0) sb.append("&nbsp;&nbsp;");
			sb.append("<i class='o_icon o_icon-fw o_icon_group'>&nbsp;</i> ")
			  .append(StringHelper.escapeHtml(group.getName()));
		}
		return sb.toString();
	}
	
	private String getAreaNames(List<Long> areaKeyList) {
		StringBuilder sb = new StringBuilder(64);
		List<BGArea> areas = areaManager.loadAreas(areaKeyList);
		for(BGArea area:areas) {
			if(sb.length() > 0) sb.append("&nbsp;&nbsp;");
			sb.append("<i class='o_icon o_icon-fw o_icon_courseareas'>&nbsp;</i> ")
			  .append(StringHelper.escapeHtml(area.getName()));
		}
		return sb.toString();
	}
	
	private class RelativeDateKeysAndValues {
		
		private final String[] keys;
		private final String[] values;
		
		public RelativeDateKeysAndValues(String[] keys, String[] values) {
			this.keys = keys;
			this.values = values;
		}

		public String[] getKeys() {
			return keys;
		}

		public String[] getValues() {
			return values;
		}
	}

}