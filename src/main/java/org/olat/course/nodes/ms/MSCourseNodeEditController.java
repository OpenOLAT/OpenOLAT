/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.ms;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.util.Util;
import org.olat.course.CourseEntryRef;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.editor.NodeEditController;
import org.olat.course.highscore.ui.HighScoreEditController;
import org.olat.course.nodes.MSCourseNode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  Initial Date:  Jun 16, 2004
 *  @author gnaegi
 */
public class MSCourseNodeEditController extends ActivateableTabbableDefaultController {

	public static final String PANE_TAB_CONFIGURATION = "pane.tab.configuration";
	public static final String PANE_TAB_HIGHSCORE = "pane.tab.highscore";
	private static final String[] paneKeys = { PANE_TAB_CONFIGURATION };

	private final MSCourseNode msNode;
	private final ICourse course;
	private VelocityContainer configurationVC;
	private MSConfigController configController;
	private HighScoreEditController highScoreNodeConfigController;

	private TabbedPane myTabbedPane;
	
	private boolean hasLogEntries;
	private Link editScoringConfigButton;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	/**
	 * Constructor for a manual scoring course edit controller
	 * 
	 * @param ureq The user request
	 * @param msNode The manual scoring course node
	 * @param course
	 */
	public MSCourseNodeEditController(UserRequest ureq, WindowControl wControl, MSCourseNode msNode, ICourse course) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(HighScoreEditController.class, getLocale(), getTranslator()));
		this.msNode = msNode;
		this.course = course;
		
		configurationVC = createVelocityContainer("edit");
		editScoringConfigButton = LinkFactory.createButtonSmall("scoring.config.enable.button", configurationVC, this);
		
		configController = new MSConfigController(ureq, wControl, course, msNode);
		listenTo(configController);
		configurationVC.put("mseditform", configController.getInitialComponent());
		
		highScoreNodeConfigController = new HighScoreEditController(ureq, wControl, msNode.getModuleConfiguration(), course);
		listenTo(highScoreNodeConfigController);
		
		// if there is already user data available, make for read only
		UserNodeAuditManager auditManager = course.getCourseEnvironment().getAuditManager();
		hasLogEntries = auditManager.hasUserNodeLogs(msNode);
		configurationVC.contextPut("hasLogEntries", Boolean.valueOf(hasLogEntries));
		if (hasLogEntries) {
			configController.setDisplayOnly(true);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == editScoringConfigButton) {
			configController.setDisplayOnly(false);
			configurationVC.contextPut("isOverwriting", Boolean.TRUE);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == configController) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				fireEvent(ureq, NodeEditController.REMINDER_VISIBILITY_EVENT);
			} else if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
			updateHighscoreTab();
		} else if (source == highScoreNodeConfigController){
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}
	
	private void updateHighscoreTab() {
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(course), msNode);
		myTabbedPane.setEnabled(myTabbedPane.indexOfTab(highScoreNodeConfigController.getInitialComponent()),
				Mode.none != assessmentConfig.getScoreMode());
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_CONFIGURATION), configurationVC);
		tabbedPane.addTab(translate(PANE_TAB_HIGHSCORE) , highScoreNodeConfigController.getInitialComponent());
		updateHighscoreTab();

	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

}
