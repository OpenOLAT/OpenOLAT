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
package org.olat.course.nodes.cl.ui;

import org.olat.core.CoreSpringFactory;
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
import org.olat.course.CourseEntryRef;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.editor.NodeEditController;
import org.olat.course.highscore.ui.HighScoreEditController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListEditController extends ActivateableTabbableDefaultController {

	public static final String PANE_TAB_CLCONFIG = "pane.tab.clconfig";
	private static final String PANE_TAB_CHECKBOX = "pane.tab.checkbox";
	public static final String PANE_TAB_HIGHSCORE = "pane.tab.highscore";
	
	private CheckListBoxListEditController checkboxListEditCtrl;
	private CheckListConfigurationController configurationCtrl;
	private HighScoreEditController highScoreNodeConfigController;
	private CheckListCourseNode courseNode;

	private TabbedPane myTabbedPane;
	private VelocityContainer configurationVC;
	private Link enableEditingLink;
	private boolean hasAssessments;
	
	private ICourse course;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private AssessmentService assessmentService;

	private static final String[] paneKeys = { PANE_TAB_CLCONFIG, PANE_TAB_CHECKBOX };

	public CheckListEditController(CheckListCourseNode courseNode, UserRequest ureq, WindowControl wControl,
			ICourse course) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.course = course;
		
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		int numOfChecks = checkboxManager.countChecks(course, courseNode.getIdent());
		
		checkboxListEditCtrl = new CheckListBoxListEditController(ureq, wControl, course, courseNode, numOfChecks > 0);
		listenTo(checkboxListEditCtrl);
		configurationCtrl = new CheckListConfigurationController(ureq, wControl, course, courseNode,
				NodeAccessType.of(course), numOfChecks > 0);
		listenTo(configurationCtrl);
		
		configurationVC = createVelocityContainer("edit_clconfig");
		configurationVC.put("assessmentform", configurationCtrl.getInitialComponent());
		enableEditingLink = LinkFactory.createButtonSmall("enable.editing", configurationVC, this);
		enableEditingLink.setPrimary(true);
		enableEditingLink.setIconLeftCSS("o_icon o_icon-fw o_icon_unlocked");

		highScoreNodeConfigController = new HighScoreEditController(ureq, wControl, courseNode.getModuleConfiguration(), course);
		listenTo(highScoreNodeConfigController);

		// if there are already assessments, make read only
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		hasAssessments = assessmentService.hasAssessments(courseEntry, courseNode.getIdent());
		configurationVC.contextPut("hasAssessments", Boolean.valueOf(hasAssessments));
		if (hasAssessments) {
			configurationCtrl.setDisplayOnly(true);
		}
		configurationVC.contextPut("isOverwriting", Boolean.valueOf(false));
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}
	
	private void updateHighscoreTab() {
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(course), courseNode);
		myTabbedPane.setEnabled(myTabbedPane.indexOfTab(highScoreNodeConfigController.getInitialComponent()),
				Mode.none != assessmentConfig.getScoreMode());
	}
	
	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_CLCONFIG), configurationVC);
		tabbedPane.addTab(translate(PANE_TAB_CHECKBOX), checkboxListEditCtrl.getInitialComponent());
		tabbedPane.addTab(translate(PANE_TAB_HIGHSCORE) , highScoreNodeConfigController.getInitialComponent());
		updateHighscoreTab();
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == enableEditingLink) {
			configurationCtrl.setDisplayOnly(false);
			configurationVC.contextPut("isOverwriting", Boolean.TRUE);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == configurationCtrl) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				fireEvent(ureq, NodeEditController.REMINDER_VISIBILITY_EVENT);
				checkboxListEditCtrl.dispatchEvent(ureq, configurationCtrl, event);
				updateHighscoreTab();
			} else if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		} else if(source == checkboxListEditCtrl) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				configurationCtrl.dispatchEvent(ureq, checkboxListEditCtrl, event);
			}
		} else if (source == highScoreNodeConfigController){
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}
}