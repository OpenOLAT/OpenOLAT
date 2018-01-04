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

package org.olat.course.nodes.dialog.ui;

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
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;

/**
 * Description:<br>
 * controller for the tabbed pane inside the course editor for the course node 'dialog elements'
 * <P>
 * Initial Date: 02.11.2005 <br>
 * 
 * @author guido
 */
public class DialogCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	private static final String PANE_TAB_DIALOGCONFIG = "pane.tab.dialogconfig";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	
	private static final String[] paneKeys = { PANE_TAB_DIALOGCONFIG, PANE_TAB_ACCESSIBILITY };
	
	private TabbedPane myTabbedPane;
	private VelocityContainer accessContent;	
	private ConditionEditController readerCondContr, posterCondContr, moderatorCondContr;
	
	private DialogCourseNode courseNode;
	private BCCourseNode bcNode = new BCCourseNode();

	private DialogElementsEditController elementsEditCtrl;
	
	public DialogCourseNodeEditController(UserRequest ureq, WindowControl wControl, DialogCourseNode node,
			ICourse course, UserCourseEnvironment userCourseEnv) {
		super(ureq,wControl);
		this.courseNode = node;
		
		// set name of the folder we use
		bcNode.setShortTitle(translate("dialog.folder.name"));

		elementsEditCtrl = new DialogElementsEditController(ureq, getWindowControl(), course.getCourseEnvironment(), node);
		listenTo(elementsEditCtrl);

		// accessability config tab		
		accessContent = createVelocityContainer("edit_access");

		CourseEditorTreeModel editorModel = course.getEditorTreeModel();
		// Reader precondition
		Condition readerCondition = courseNode.getPreConditionReader();
		readerCondContr = new ConditionEditController(ureq, getWindowControl(), userCourseEnv, readerCondition,
				AssessmentHelper.getAssessableNodes(editorModel, courseNode));		
		listenTo(readerCondContr);
		accessContent.put("readerCondition", readerCondContr.getInitialComponent());

		// Poster precondition
		Condition posterCondition = courseNode.getPreConditionPoster();
		posterCondContr = new ConditionEditController(ureq, getWindowControl(), userCourseEnv, posterCondition,
				AssessmentHelper.getAssessableNodes(editorModel, courseNode));		
		this.listenTo(posterCondContr);
		accessContent.put("posterCondition", posterCondContr.getInitialComponent());

		// Moderator precondition
		Condition moderatorCondition = courseNode.getPreConditionModerator();
		moderatorCondContr = new ConditionEditController(ureq, getWindowControl(), userCourseEnv, moderatorCondition,
				AssessmentHelper.getAssessableNodes(editorModel, courseNode));

		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);		
		listenTo(moderatorCondContr);
		accessContent.put("moderatorCondition", moderatorCondContr.getInitialComponent());
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
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == elementsEditCtrl) {
			if (event == Event.CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == readerCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = readerCondContr.getCondition();
				courseNode.setPreConditionReader(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == posterCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = posterCondContr.getCondition();
				courseNode.setPreConditionPoster(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == moderatorCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = moderatorCondContr.getCondition();
				courseNode.setPreConditionModerator(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessContent);
		tabbedPane.addTab(translate(PANE_TAB_DIALOGCONFIG), elementsEditCtrl.getInitialComponent());
	}
}
