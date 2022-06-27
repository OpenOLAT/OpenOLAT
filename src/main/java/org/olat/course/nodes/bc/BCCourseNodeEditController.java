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

package org.olat.course.nodes.bc;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.CourseEntryRef;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.condition.ConditionRemoveController;
import org.olat.course.config.CourseConfig;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * Initial Date: Apr 28, 2004
 * Updated: 22 Dez, 2015
 *
 * @author gnaegi
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public class BCCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_FOLDER = "pane.tab.folder";
	public static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	static final String[] PANE_KEYS_CUSTOM_ACCESS = { PANE_TAB_FOLDER, PANE_TAB_ACCESSIBILITY };
	static final String[] PANE_KEYS = { PANE_TAB_FOLDER };

	private TabbedPane myTabbedPane;
	private VelocityContainer accessibilityContent;

	private Controller configCtrl;
	private ConditionRemoveController conditionRemoveCtrl;
	private ConditionEditController uploaderCondContr;
	private ConditionEditController downloaderCondContr;
	
	private BCCourseNode bcNode;

	public BCCourseNodeEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			BCCourseNode bcNode, ICourse course, UserCourseEnvironment euce) {
		super(ureq,wControl);
		this.bcNode = bcNode;
		myTabbedPane = null;
		
		if (bcNode.hasCustomPreConditions()) {
			accessibilityContent = createVelocityContainer("edit");
			
			conditionRemoveCtrl = new ConditionRemoveController(ureq, getWindowControl());
			listenTo(conditionRemoveCtrl);
			accessibilityContent.put("remove", conditionRemoveCtrl.getInitialComponent());

			// Uploader precondition
			Condition uploadCondition = bcNode.getPreConditionUploaders();
			uploaderCondContr = new ConditionEditController(ureq, getWindowControl(), euce,
					uploadCondition, AssessmentHelper
							.getAssessableNodes(new CourseEntryRef(course), course.getEditorTreeModel(), bcNode));		
			listenTo(uploaderCondContr);

			CourseConfig courseConfig = course.getCourseConfig();
			if(bcNode.getModuleConfiguration().getStringValue(BCCourseNode.CONFIG_SUBPATH, "").startsWith("/_sharedfolder")
					&& courseConfig.isSharedFolderReadOnlyMount()) {
				accessibilityContent.contextPut("uploadable", false);
			} else {
				accessibilityContent.contextPut("uploadable", true);
			}
			accessibilityContent.put("uploadCondition", uploaderCondContr.getInitialComponent());

			// Uploader precondition
			Condition downloadCondition = bcNode.getPreConditionDownloaders();
			downloaderCondContr = new ConditionEditController(ureq, getWindowControl(), euce,
					downloadCondition, AssessmentHelper
							.getAssessableNodes(new CourseEntryRef(course), course.getEditorTreeModel(), bcNode));
			listenTo(downloaderCondContr);
			accessibilityContent.put("downloadCondition", downloaderCondContr.getInitialComponent());
		}

		configCtrl = new BCCourseNodeConfigsController(ureq, wControl, stackPanel, bcNode, course);
		listenTo(configCtrl);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == uploaderCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = uploaderCondContr.getCondition();
				bcNode.setPreConditionUploaders(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == downloaderCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = downloaderCondContr.getCondition();
				bcNode.setPreConditionDownloaders(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == configCtrl){
			if (accessibilityContent != null) {
				if(bcNode.getModuleConfiguration().getStringValue(BCCourseNode.CONFIG_SUBPATH, "").startsWith("/_sharedfolder")){
					accessibilityContent.contextPut("uploadable", false);
				} else {
					accessibilityContent.contextPut("uploadable", true);
				}
			}
			fireEvent(urequest, event);
		} else if (source == conditionRemoveCtrl && event == ConditionRemoveController.REMOVE) {
			bcNode.removeCustomPreconditions();
			fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_REFRESH_EVENT);
		}
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		if (accessibilityContent != null) {
			tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityContent);
		}
		tabbedPane.addTab(translate(PANE_TAB_FOLDER), configCtrl.getInitialComponent());
	}

	@Override
	public String[] getPaneKeys() {
		return accessibilityContent != null? PANE_KEYS_CUSTOM_ACCESS: PANE_KEYS;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}
}