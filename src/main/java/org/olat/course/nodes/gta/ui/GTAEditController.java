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

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.ms.MSEditFormController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAEditController extends ActivateableTabbableDefaultController {

	public static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	public static final String PANE_TAB_WORKLOW = "pane.tab.workflow";
	public static final String PANE_TAB_ASSIGNMENT = "pane.tab.assignment";
	public static final String PANE_TAB_SUBMISSION = "pane.tab.submission";
	public static final String PANE_TAB_GRADING = "pane.tab.grading";
	public static final String PANE_TAB_SOLUTIONS = "pane.tab.solutions";
	public static final String[] paneKeys = {
		PANE_TAB_ACCESSIBILITY, PANE_TAB_WORKLOW, PANE_TAB_ASSIGNMENT,
		PANE_TAB_SUBMISSION, PANE_TAB_GRADING, PANE_TAB_SOLUTIONS
	};
	private int workflowPos, assignmentPos, submissionPos, gradingPos, solutionsPos;
	
	private TabbedPane myTabbedPane;
	private GTAWorkflowEditController workflowCtrl;
	private GTAAssignmentEditController assignmentCtrl;
	private GTASubmissionEditController submissionCtrl;
	private MSEditFormController manualAssessmentCtrl;
	private ConditionEditController accessibilityCondCtrl;
	private GTASampleSolutionsEditController solutionsCtrl;
	
	private final File tasksDir;
	private final File solutionsDir;
	private final VFSContainer tasksContainer;
	private final VFSContainer solutionsContainer;
	
	private final GTACourseNode gtaNode;
	private final ModuleConfiguration config;
	private final UserCourseEnvironment euce;
	
	@Autowired
	private GTAManager gtaManager;
	
	public GTAEditController(UserRequest ureq, WindowControl wControl, GTACourseNode gtaNode,
			ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		
		this.euce = euce;
		this.gtaNode = gtaNode;
		config = gtaNode.getModuleConfiguration();
		
		tasksDir = gtaManager.getTasksDirectory(course.getCourseEnvironment(), gtaNode);
		tasksContainer = gtaManager.getTasksContainer(course.getCourseEnvironment(), gtaNode);
		solutionsDir = gtaManager.getSolutionsDirectory(course.getCourseEnvironment(), gtaNode);
		solutionsContainer = gtaManager.getSolutionsContainer(course.getCourseEnvironment(), gtaNode);

		// Accessibility precondition
		Condition accessCondition = gtaNode.getPreConditionAccess();
		CourseEditorTreeModel editorModel = course.getEditorTreeModel();
		accessibilityCondCtrl = new ConditionEditController(ureq, getWindowControl(),
				accessCondition, AssessmentHelper.getAssessableNodes(editorModel, gtaNode), euce);		
		listenTo(accessibilityCondCtrl);
		//workflow
		workflowCtrl = new GTAWorkflowEditController(ureq, getWindowControl(), gtaNode, euce.getCourseEditorEnv());
		listenTo(workflowCtrl);
		//assignment
		assignmentCtrl = new GTAAssignmentEditController(ureq, getWindowControl(), config, tasksDir, tasksContainer);
		listenTo(assignmentCtrl);
		//submission
		submissionCtrl = new GTASubmissionEditController(ureq, getWindowControl(), config);
		listenTo(submissionCtrl);
		//grading
		manualAssessmentCtrl = new MSEditFormController(ureq, getWindowControl(), config);
		listenTo(manualAssessmentCtrl);
		//solutions
		solutionsCtrl = new GTASampleSolutionsEditController(ureq, getWindowControl(), config, solutionsDir, solutionsContainer);
		listenTo(solutionsCtrl);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondCtrl.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
		workflowPos = tabbedPane.addTab(translate(PANE_TAB_WORKLOW), workflowCtrl.getInitialComponent());
		assignmentPos = tabbedPane.addTab(translate(PANE_TAB_ASSIGNMENT), assignmentCtrl.getInitialComponent());
		submissionPos = tabbedPane.addTab(translate(PANE_TAB_SUBMISSION), submissionCtrl.getInitialComponent());
		gradingPos = tabbedPane.addTab(translate(PANE_TAB_GRADING), manualAssessmentCtrl.getInitialComponent());
		solutionsPos = tabbedPane.addTab(translate(PANE_TAB_SOLUTIONS), solutionsCtrl.getInitialComponent());
		updateEnabledDisabledTabs();
	}
	
	private void updateEnabledDisabledTabs() {
		myTabbedPane.setEnabled(assignmentPos, config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT));
		myTabbedPane.setEnabled(submissionPos, config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT));
		myTabbedPane.setEnabled(gradingPos, config.getBooleanSafe(GTACourseNode.GTASK_GRADING));
		myTabbedPane.setEnabled(solutionsPos, config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION));
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessibilityCondCtrl) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessibilityCondCtrl.getCondition();
				gtaNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if(workflowCtrl == source) {
			 if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				updateEnabledDisabledTabs();
			} else if(event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(workflowCtrl);
				workflowCtrl = new GTAWorkflowEditController(ureq, getWindowControl(), gtaNode, euce.getCourseEditorEnv());
				listenTo(workflowCtrl);
				myTabbedPane.replaceTab(workflowPos, workflowCtrl.getInitialComponent());
			}
		} else if(assignmentCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			} else if(event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(assignmentCtrl);
				assignmentCtrl = new GTAAssignmentEditController(ureq, getWindowControl(), config, tasksDir, tasksContainer);
				listenTo(assignmentCtrl);
				myTabbedPane.replaceTab(assignmentPos, assignmentCtrl.getInitialComponent());
			}
		} else if(submissionCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			} else if(event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(submissionCtrl);
				submissionCtrl = new GTASubmissionEditController(ureq, getWindowControl(), config);
				listenTo(submissionCtrl);
				myTabbedPane.replaceTab(submissionPos, submissionCtrl.getInitialComponent());
			}
		} else if(manualAssessmentCtrl == source) {
			if (event == Event.DONE_EVENT){
				manualAssessmentCtrl.updateModuleConfiguration(config);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			} else if(event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(manualAssessmentCtrl);
				manualAssessmentCtrl = new MSEditFormController(ureq, getWindowControl(), config);
				listenTo(manualAssessmentCtrl);
				myTabbedPane.replaceTab(gradingPos, manualAssessmentCtrl.getInitialComponent());
			}
		} else if(solutionsCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
		
		super.event(ureq, source, event);
	}
}