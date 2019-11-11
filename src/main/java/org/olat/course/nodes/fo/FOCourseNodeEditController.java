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

package org.olat.course.nodes.fo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;

/**
 * Initial Date: Apr 7, 2004
 * 
 * @author gnaegi
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class FOCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	private static final String PANE_TAB_SETTINGS = "pane.tab.settings";
	private static final String[] paneKeys = { PANE_TAB_ACCESSIBILITY, PANE_TAB_SETTINGS };
	
	private final FOCourseNode foNode;
	private final VelocityContainer myContent;

	private SettingsController settingsCtrl;
	private final ConditionEditController readerCondContr, posterCondContr, moderatorCondContr;
	private TabbedPane myTabbedPane;

	public FOCourseNodeEditController(UserRequest ureq, WindowControl wControl, FOCourseNode forumNode, ICourse course,
			UserCourseEnvironment euce) {
		super(ureq, wControl);
		this.foNode = forumNode;
		
		myContent = createVelocityContainer("edit");		

		CourseEditorTreeModel editorModel = course.getEditorTreeModel();
		// Reader precondition
		Condition readerCondition = foNode.getPreConditionReader();
		readerCondContr = new ConditionEditController(ureq, getWindowControl(), euce, readerCondition,
				AssessmentHelper.getAssessableNodes(editorModel, forumNode));		
		listenTo(readerCondContr);
		myContent.put("readerCondition", readerCondContr.getInitialComponent());

		// Poster precondition
		Condition posterCondition = foNode.getPreConditionPoster();
		posterCondContr = new ConditionEditController(ureq, getWindowControl(), euce, posterCondition,
				AssessmentHelper.getAssessableNodes(editorModel, forumNode));		
		listenTo(posterCondContr);
		myContent.put("posterCondition", posterCondContr.getInitialComponent());

		// Moderator precondition
		Condition moderatorCondition = foNode.getPreConditionModerator();
		moderatorCondContr = new ConditionEditController(ureq, getWindowControl(), euce, moderatorCondition,
				AssessmentHelper.getAssessableNodes(editorModel, forumNode));		
		listenTo(moderatorCondContr);
		myContent.put("moderatorCondition", moderatorCondContr.getInitialComponent());
		
		//Settings
		settingsCtrl = new SettingsController(ureq, getWindowControl(), forumNode);
		listenTo(settingsCtrl);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == readerCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = readerCondContr.getCondition();
				foNode.setPreConditionReader(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == posterCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = posterCondContr.getCondition();
				foNode.setPreConditionPoster(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == moderatorCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = moderatorCondContr.getCondition();
				foNode.setPreConditionModerator(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == settingsCtrl) {
			if (event == Event.CHANGED_EVENT) {
				String pseudoAllowed = settingsCtrl.isPseudonymPostAllowed() ? "true" : "false";
				foNode.getModuleConfiguration().setStringValue(FOCourseNode.CONFIG_PSEUDONYM_POST_ALLOWED, pseudoAllowed);
				String defaultPseudo = settingsCtrl.isDefaultPseudonym() ? "true" : "false";
				foNode.getModuleConfiguration().setStringValue(FOCourseNode.CONFIG_PSEUDONYM_POST_DEFAULT, defaultPseudo);
				String guestAllowed = settingsCtrl.isGuestPostAllowed() ? "true" : "false";
				foNode.getModuleConfiguration().setStringValue(FOCourseNode.CONFIG_GUEST_POST_ALLOWED, guestAllowed);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), myContent);
		if(settingsCtrl != null) {
			tabbedPane.addTab(translate(PANE_TAB_SETTINGS), settingsCtrl.getInitialComponent());
		}
	}

	@Override
	protected void doDispose() {
		//
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