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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.highscore.ui.HighScoreEditController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.ms.MSEditFormController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 23.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAEditController extends ActivateableTabbableDefaultController {

	public static final String PANE_TAB_WORKLOW = "pane.tab.workflow";
	public static final String PANE_TAB_ASSIGNMENT = "pane.tab.assignment";
	public static final String PANE_TAB_SUBMISSION = "pane.tab.submission";
	public static final String PANE_TAB_REVIEW_AND_CORRECTIONS = "pane.tab.review";
	public static final String PANE_TAB_GRADING = "pane.tab.grading";
	public static final String PANE_TAB_SOLUTIONS = "pane.tab.solutions";
	public static final String PANE_TAB_HIGHSCORE = "pane.tab.highscore";
	private static final String[] paneKeys = {
			PANE_TAB_WORKLOW, PANE_TAB_ASSIGNMENT, PANE_TAB_SUBMISSION, PANE_TAB_REVIEW_AND_CORRECTIONS,
			PANE_TAB_GRADING, PANE_TAB_SOLUTIONS};
	private int workflowPos, assignmentPos, submissionPos, revisionPos, gradingPos, solutionsPos, highScoreTabPosition;
	
	private TabbedPane myTabbedPane;
	private GTAWorkflowEditController workflowCtrl;
	private GTARevisionAndCorrectionEditController revisionCtrl;
	private GTAAssignmentEditController assignmentCtrl;
	private GTASubmissionEditController submissionCtrl;
	private MSEditFormController manualAssessmentCtrl;
	private GTASampleSolutionsEditController solutionsCtrl;
	private HighScoreEditController highScoreNodeConfigController;
	
	private final GTACourseNode gtaNode;
	private final ModuleConfiguration config;
	private final UserCourseEnvironment euce;
	private final CourseEnvironment courseEnv;
	private final NodeAccessType nodeAccessType;
	
	public GTAEditController(UserRequest ureq, WindowControl wControl, GTACourseNode gtaNode,
			ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		
		this.euce = euce;
		this.gtaNode = gtaNode;
		courseEnv = course.getCourseEnvironment();
		nodeAccessType = NodeAccessType.of(course);
		config = gtaNode.getModuleConfiguration();

		//workflow
		workflowCtrl = new GTAWorkflowEditController(ureq, getWindowControl(), gtaNode, euce.getCourseEditorEnv());
		listenTo(workflowCtrl);
		//assignment
		assignmentCtrl = new GTAAssignmentEditController(ureq, getWindowControl(), gtaNode, config, courseEnv, false);
		listenTo(assignmentCtrl);
		//submission
		submissionCtrl = new GTASubmissionEditController(ureq, getWindowControl(), config);
		listenTo(submissionCtrl);
		//revision
		revisionCtrl = new GTARevisionAndCorrectionEditController(ureq, getWindowControl(), config);
		listenTo(revisionCtrl);
		//grading
		manualAssessmentCtrl = createManualAssessmentCtrl(ureq);
		listenTo(manualAssessmentCtrl);
		//solutions
		solutionsCtrl = new GTASampleSolutionsEditController(ureq, getWindowControl(), gtaNode, courseEnv, false);
		listenTo(solutionsCtrl);
		//highscore
		highScoreNodeConfigController = new HighScoreEditController(ureq, wControl, config, course);
		listenTo(highScoreNodeConfigController);
		if ("group".equals(config.get(GTACourseNode.GTASK_TYPE))) {
			highScoreNodeConfigController.setFormInfoMessage("highscore.forminfo", getTranslator());			
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		workflowPos = tabbedPane.addTab(translate(PANE_TAB_WORKLOW), workflowCtrl.getInitialComponent());
		assignmentPos = tabbedPane.addTab(translate(PANE_TAB_ASSIGNMENT), assignmentCtrl.getInitialComponent());
		submissionPos = tabbedPane.addTab(translate(PANE_TAB_SUBMISSION), submissionCtrl.getInitialComponent());
		revisionPos = tabbedPane.addTab(translate(PANE_TAB_REVIEW_AND_CORRECTIONS), revisionCtrl.getInitialComponent());
		gradingPos = tabbedPane.addTab(translate(PANE_TAB_GRADING), manualAssessmentCtrl.getInitialComponent());
		solutionsPos = tabbedPane.addTab(translate(PANE_TAB_SOLUTIONS), solutionsCtrl.getInitialComponent());
		highScoreTabPosition = myTabbedPane.addTab(translate(PANE_TAB_HIGHSCORE), highScoreNodeConfigController.getInitialComponent());
		updateEnabledDisabledTabs();
	}
	
	private void updateEnabledDisabledTabs() {
		myTabbedPane.setEnabled(assignmentPos, config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT));
		myTabbedPane.setEnabled(submissionPos, config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT));
		myTabbedPane.setEnabled(revisionPos, config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION));
		myTabbedPane.setEnabled(gradingPos, config.getBooleanSafe(GTACourseNode.GTASK_GRADING));
		myTabbedPane.setEnabled(solutionsPos, config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION));
		myTabbedPane.setEnabled(highScoreTabPosition, config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD));
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
		if(workflowCtrl == source) {
			 if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				fireEvent(ureq, NodeEditController.REMINDER_VISIBILITY_EVENT);
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
				assignmentCtrl = new GTAAssignmentEditController(ureq, getWindowControl(), gtaNode, config, courseEnv, false);
				listenTo(assignmentCtrl);
				myTabbedPane.replaceTab(assignmentPos, assignmentCtrl.getInitialComponent());
			}
		} else if(submissionCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				if(revisionCtrl != null) {
					revisionCtrl.updateDefaultNumbersOfDocuments();
				}
			} else if(event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(submissionCtrl);
				submissionCtrl = new GTASubmissionEditController(ureq, getWindowControl(), config);
				listenTo(submissionCtrl);
				myTabbedPane.replaceTab(submissionPos, submissionCtrl.getInitialComponent());
			}
		} else if(revisionCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			} else if(event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(revisionCtrl);
				revisionCtrl = new GTARevisionAndCorrectionEditController(ureq, getWindowControl(), config);
				listenTo(revisionCtrl);
				myTabbedPane.replaceTab(revisionPos, revisionCtrl.getInitialComponent());
			}
		} else if(manualAssessmentCtrl == source) {
			if (event == Event.DONE_EVENT){
				manualAssessmentCtrl.updateModuleConfiguration(config);
				updateEnabledDisabledTabs();
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				fireEvent(ureq, NodeEditController.REMINDER_VISIBILITY_EVENT);
			} else if(event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(manualAssessmentCtrl);
				manualAssessmentCtrl = createManualAssessmentCtrl(ureq);
				listenTo(manualAssessmentCtrl);
				myTabbedPane.replaceTab(gradingPos, manualAssessmentCtrl.getInitialComponent());
			}
		} else if(solutionsCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == highScoreNodeConfigController){
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
		
		super.event(ureq, source, event);
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		super.dispatchEvent(ureq, source, event);
		if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
			workflowCtrl.onNodeConfigChanged();
		}
	}

	public MSEditFormController createManualAssessmentCtrl(UserRequest ureq) {
		return new MSEditFormController(ureq, getWindowControl(), config, nodeAccessType,
				translate("pane.tab.grading"), "Three Steps to Your Task#_task_configuration");
	}
	
}