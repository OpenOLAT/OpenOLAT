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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.course.condition.AreaSelectionController;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAType;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAWorkflowEditController extends FormBasicController {
	
	private static final String[] keys = new String[]{ "on" };
	private static final String[] executionKeys = new String[]{ GTAType.group.name(), GTAType.individual.name() };
	
	private CloseableModalController cmc;
	private AreaSelectionController areaSelectionCtrl;
	private GroupSelectionController groupSelectionCtrl;
	
	private SingleSelection typeEl;
	private FormLink chooseGroupButton, chooseAreaButton;
	private StaticTextElement groupListEl, areaListEl;
	private DateChooser assignmentDeadlineEl, submissionDeadlineEl, solutionVisibleAfterEl;
	private MultipleSelectionElement taskAssignmentEl, submissionEl, reviewEl, revisionEl, sampleEl, gradingEl;
	private FormLayoutContainer stepsCont;
	
	private final ModuleConfiguration config;
	private final CourseEditorEnv courseEditorEnv;
	private List<Long> areaKeys;
	private List<Long> groupKeys;
	
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public GTAWorkflowEditController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, CourseEditorEnv courseEditorEnv) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.config = config;
		this.courseEditorEnv = courseEditorEnv;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer typeCont = FormLayoutContainer.createDefaultFormLayout("type", getTranslator());
		typeCont.setFormTitle(translate("task.type.title"));
		typeCont.setFormDescription(translate("task.type.description"));
		typeCont.setRootForm(mainForm);
		formLayout.add(typeCont);
		
		String[] executionValues = new String[]{
				translate("task.execution.group"),
				translate("task.execution.individual")
		};
		typeEl = uifactory.addDropdownSingleselect("execution", "task.execution", typeCont, executionKeys, executionValues, null);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		String type = config.getStringValue(GTACourseNode.GTASK_TYPE);
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
		updateTaskType();
		
		//Steps
		stepsCont = FormLayoutContainer.createDefaultFormLayout("steps", getTranslator());
		stepsCont.setFormTitle(translate("task.steps.title"));
		stepsCont.setFormDescription(translate("task.steps.description"));
		stepsCont.setRootForm(mainForm);
		formLayout.add(stepsCont);
		
		//assignment
		String[] assignmentValues = new String[] { translate("enabled") };
		taskAssignmentEl = uifactory.addCheckboxesHorizontal("task.assignment", "task.assignment", stepsCont, keys, assignmentValues);
		taskAssignmentEl.addActionListener(FormEvent.ONCHANGE);
		boolean assignement = config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT);
		taskAssignmentEl.select(keys[0], assignement);
		
		Date assignmentDeadline = config.getDateValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
		assignmentDeadlineEl = uifactory.addDateChooser("assignementdeadline", "assignment.deadline", assignmentDeadline, stepsCont);
		assignmentDeadlineEl.setDateChooserTimeEnabled(true);
		assignmentDeadlineEl.setVisible(assignement);
		
		//turning in
		String[] submissionValues = new String[] { translate("submission.enabled") };
		submissionEl = uifactory.addCheckboxesHorizontal("submission", "submission", stepsCont, keys, submissionValues);
		submissionEl.addActionListener(FormEvent.ONCHANGE);
		boolean submit = config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT);
		submissionEl.select(keys[0], submit);
		
		Date submissionDeadline = config.getDateValue(GTACourseNode.GTASK_SUBMIT_DEADLINE);
		submissionDeadlineEl = uifactory.addDateChooser("submitdeadline", "submit.deadline", submissionDeadline, stepsCont);
		submissionDeadlineEl.setDateChooserTimeEnabled(true);
		submissionDeadlineEl.setVisible(submit);
		
		//review and correction
		String[] reviewValues = new String[] { translate("review.enabled") };
		reviewEl = uifactory.addCheckboxesHorizontal("review", "review.and.correction", stepsCont, keys, reviewValues);
		reviewEl.addActionListener(FormEvent.ONCHANGE);
		boolean review = config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION);
		reviewEl.select(keys[0], review);
		
		//revision
		String[] revisionValues = new String[] { translate("revision.enabled") };
		revisionEl = uifactory.addCheckboxesHorizontal("revision", "revision.period", stepsCont, keys, revisionValues);
		revisionEl.addActionListener(FormEvent.ONCHANGE);
		boolean revision = config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD);
		revisionEl.select(keys[0], revision);
		
		//sample solution
		String[] sampleValues = new String[] { translate("enabled") };
		sampleEl = uifactory.addCheckboxesHorizontal("sample", "sample.solution", stepsCont, keys, sampleValues);
		sampleEl.addActionListener(FormEvent.ONCHANGE);
		boolean sample = config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION);
		sampleEl.select(keys[0], sample);
		
		Date solutionVisibleAfter = config.getDateValue(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER);
		solutionVisibleAfterEl = uifactory.addDateChooser("visibleafter", "sample.solution.visible.after", solutionVisibleAfter, stepsCont);
		solutionVisibleAfterEl.setDateChooserTimeEnabled(true);
		solutionVisibleAfterEl.setVisible(sample);
		
		//grading
		String[] gradingValues = new String[] { translate("enabled") };
		gradingEl = uifactory.addCheckboxesHorizontal("grading", "grading", stepsCont, keys, gradingValues);
		gradingEl.addActionListener(FormEvent.ONCHANGE);
		boolean grading = config.getBooleanSafe(GTACourseNode.GTASK_GRADING);
		gradingEl.select(keys[0], grading);
		
		//save
		FormLayoutContainer buttonCont = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", "save", buttonCont);
	}
	
	private void updateTaskType() {
		boolean groupOption = typeEl.isSelected(0);
		chooseGroupButton.setVisible(groupOption);
		groupListEl.setVisible(groupOption);
		chooseAreaButton.setVisible(groupOption);
		areaListEl.setVisible(groupOption);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		typeEl.clearError();
		if(!typeEl.isOneSelected()) {
			typeEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		typeEl.clearError();
		if(typeEl.isSelected(0)) {
			if(areaKeys.isEmpty() && groupKeys.isEmpty()) {
				typeEl.setErrorKey("error.missing.group", null);
				allOk &= false;
			}
		}
		
		taskAssignmentEl.clearError();
		if(!taskAssignmentEl.isAtLeastSelected(1) && !submissionEl.isAtLeastSelected(1)
				&& !reviewEl.isAtLeastSelected(1) && !revisionEl.isAtLeastSelected(1)
				&& !sampleEl.isAtLeastSelected(1) && !gradingEl.isAtLeastSelected(1)) {

			taskAssignmentEl.setErrorKey("error.select.atleastonestep", null);
			allOk &= false;
		}

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(typeEl.isSelected(0)) {
			config.setStringValue(GTACourseNode.GTASK_TYPE, GTAType.group.name());
			config.setList(GTACourseNode.GTASK_AREAS, areaKeys);
			config.setList(GTACourseNode.GTASK_GROUPS, groupKeys);
		} else {
			config.setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
			config.setList(GTACourseNode.GTASK_AREAS, new ArrayList<Long>(0));
			config.setList(GTACourseNode.GTASK_GROUPS, new ArrayList<Long>(0));
		}
		
		boolean assignment = taskAssignmentEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_ASSIGNMENT, assignment);
		if(assignment && assignmentDeadlineEl.getDate() != null) {
			config.setDateValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE, assignmentDeadlineEl.getDate());
		} else {
			config.remove(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
		}
		
		boolean turningIn = submissionEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_SUBMIT, turningIn);
		if(turningIn && submissionDeadlineEl.getDate() != null) {
			config.setDateValue(GTACourseNode.GTASK_SUBMIT_DEADLINE, submissionDeadlineEl.getDate());
		} else {
			config.remove(GTACourseNode.GTASK_SUBMIT_DEADLINE);
		}

		config.setBooleanEntry(GTACourseNode.GTASK_REVIEW_AND_CORRECTION, reviewEl.isAtLeastSelected(1));
		config.setBooleanEntry(GTACourseNode.GTASK_REVISION_PERIOD, revisionEl.isAtLeastSelected(1));
		
		boolean sample = sampleEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_SAMPLE_SOLUTION, sample);
		if(sample && solutionVisibleAfterEl.getDate() != null) {
			config.setDateValue(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER, solutionVisibleAfterEl.getDate());
		} else {
			config.remove(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER);
		}

		config.setBooleanEntry(GTACourseNode.GTASK_GRADING, gradingEl.isAtLeastSelected(1));
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(typeEl == source) {
			updateTaskType();
		} else if(submissionEl == source) {
			submissionDeadlineEl.setVisible(submissionEl.isAtLeastSelected(1));
		} else if(taskAssignmentEl == source) {
			assignmentDeadlineEl.setVisible(taskAssignmentEl.isAtLeastSelected(1));
		} else if(sampleEl == source) {
			solutionVisibleAfterEl.setVisible(sampleEl.isAtLeastSelected(1));
		} else if(chooseGroupButton == source) {
			doChooseGroup(ureq);
		} else if(chooseAreaButton == source) {
			doChooseArea(ureq);
		}
		
		super.formInnerEvent(ureq, source, event);
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
			}
			cmc.deactivate();
			cleanUp();
		} else if(areaSelectionCtrl == source) {
			if (event == Event.DONE_EVENT) {
				areaKeys = areaSelectionCtrl.getSelectedKeys();
				areaListEl.setValue(getAreaNames(areaKeys));
				if(courseEditorEnv.getCourseGroupManager().hasAreas()) {
					chooseAreaButton.setI18nKey("choose.areas");
				} else {
					chooseAreaButton.setI18nKey("create.areas");
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(groupSelectionCtrl);
		removeAsListenerAndDispose(cmc);
		
		groupSelectionCtrl = null;
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
	
	private String getGroupNames(List<Long> keys) {
		StringBuilder sb = new StringBuilder(64);
		List<BusinessGroupShort> groups = businessGroupService.loadShortBusinessGroups(keys);
		for(BusinessGroupShort group:groups) {
			if(sb.length() > 0) sb.append("&nbsp;&nbsp;");
			sb.append("<i class='o_icon o_icon-fw o_icon_group'>&nbsp;</i> ")
			  .append(group.getName());
		}
		return sb.toString();
	}
	
	private String getAreaNames(List<Long> keys) {
		StringBuilder sb = new StringBuilder(64);
		List<BGArea> areas = areaManager.loadAreas(keys);
		for(BGArea area:areas) {
			if(sb.length() > 0) sb.append("&nbsp;&nbsp;");
			sb.append("<i class='o_icon o_icon-fw o_icon_courseareas'>&nbsp;</i> ")
			  .append(area.getName());
		}
		return sb.toString();
	}
	
	
}