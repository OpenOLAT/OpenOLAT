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

package org.olat.course.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.repository.RepositoryManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * is the controller for
 * 
 * @author Felix Jost
 */
public class NodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	private static final String PANE_TAB_VISIBILITY = "pane.tab.visibility";
	private static final String PANE_TAB_GENERAL = "pane.tab.general";
	
  /** Configuration key: use spash-scree start page when accessing a course node. Values: true, false **/
  public static final String CONFIG_STARTPAGE = "startpage";
  /** Configuration key: integrate component menu into course menu. Values: true, false **/
  public static final String CONFIG_COMPONENT_MENU = "menuon";
  /** Configuration key: how to integrate the course node content into the course **/
  public static final String CONFIG_INTEGRATION = "integration";
  /** To enforce the encoding of the content of the node **/
	public final static String CONFIG_CONTENT_ENCODING = "encodingContent";
  /** Try to discovery automatically the encoding of the node content **/
	public final static String CONFIG_CONTENT_ENCODING_AUTO = "auto";
  /** To enforce the encoding of the embedded javascript of the node **/
	public final static String CONFIG_JS_ENCODING = "encodingJS";
  /** Take the same encoding as the content **/
	public final static String CONFIG_JS_ENCODING_AUTO = "auto";
	
	private CourseNode courseNode;
	private VelocityContainer descriptionVc, visibilityVc;

	private NodeConfigFormController nodeConfigController;

	private ConditionEditController visibilityCondContr;
	private NoAccessExplEditController noAccessContr;
	private TabbedPane myTabbedPane;
	private TabbableController childTabsCntrllr;

	/** Event that signals that the node configuration has been changed * */
	public static final Event NODECONFIG_CHANGED_EVENT = new Event("nodeconfigchanged");
	private static final String[] paneKeys = { PANE_TAB_VISIBILITY, PANE_TAB_GENERAL };

	public NodeEditController(UserRequest ureq, WindowControl wControl, CourseEditorTreeModel editorModel, ICourse course, CourseNode luNode,
			UserCourseEnvironment euce, TabbableController childTabsController) {
		super(ureq,wControl);
		this.courseNode = luNode;
		
		addLoggingResourceable(LoggingResourceable.wrap(course));
		addLoggingResourceable(LoggingResourceable.wrap(courseNode));
		
		/*
		 * the direct child tabs.
		 */
		this.childTabsCntrllr = childTabsController;
		listenTo(childTabsCntrllr);
		
		// description and metadata component
		descriptionVc = createVelocityContainer("nodeedit");
		descriptionVc.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID
		Long repoKey = RepositoryManager.getInstance().lookupRepositoryEntryKey(course, true);
		
		StringBuilder extLink = new StringBuilder();
		extLink.append(Settings.getServerContextPathURI())
			.append("/url/RepositoryEntry/").append(repoKey)
			.append("/CourseNode/").append(luNode.getIdent());
		StringBuilder intLink = new StringBuilder();
		intLink.append("javascript:parent.gotonode(").append(luNode.getIdent()).append(")");
		
		descriptionVc.contextPut("extLink", extLink.toString());
		descriptionVc.contextPut("intLink", intLink.toString());
		descriptionVc.contextPut("nodeId", luNode.getIdent());
		
		putInitialPanel(descriptionVc);

		nodeConfigController = new NodeConfigFormController(ureq, wControl, luNode, repoKey);
		listenTo(nodeConfigController);
		descriptionVc.put("nodeConfigForm", nodeConfigController.getInitialComponent());
		
		// Visibility and no-access explanation component
		visibilityVc = createVelocityContainer("visibilityedit");

		// Visibility precondition
		Condition visibCondition = luNode.getPreConditionVisibility();
		visibilityCondContr = new ConditionEditController(ureq, getWindowControl(), euce, visibCondition, 
				AssessmentHelper.getAssessableNodes(editorModel, luNode));
		//set this useractivity logger for the visibility condition controller
		listenTo(visibilityCondContr);
		visibilityVc.put("visibilityCondition", visibilityCondContr.getInitialComponent());

		// No-Access-Explanation
		String noAccessExplanation = luNode.getNoAccessExplanation();
		noAccessContr = new NoAccessExplEditController(ureq, getWindowControl(), noAccessExplanation);
		listenTo(noAccessContr);
		visibilityVc.put("noAccessExplanationComp", noAccessContr.getInitialComponent());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// Don't do anything.
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		
		if (source == visibilityCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = visibilityCondContr.getCondition();
				courseNode.setPreConditionVisibility(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == noAccessContr) {
			if (event == Event.CHANGED_EVENT) {
				String noAccessExplanation = noAccessContr.getNoAccessExplanation();
				courseNode.setNoAccessExplanation(noAccessExplanation);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == childTabsCntrllr) {
			if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				//fire child controller request further
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == nodeConfigController) {
			if (event == Event.DONE_EVENT) {
				courseNode.setShortTitle(nodeConfigController.getMenuTitle());
				courseNode.setLongTitle(nodeConfigController.getDisplayTitle());
				courseNode.setLearningObjectives(nodeConfigController.getLearningObjectives());
				courseNode.setDisplayOption(nodeConfigController.getDisplayOption());
			}
			fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
		
		// do logging
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_EDITED, getClass());
	}

	/**
	 * Package private. Used by EditorMainController.
	 * 
	 * @return CourseNode
	 */
	CourseNode getCourseNode() {
		return courseNode;
	}

	/**
	 * Returns the component that is used to configurate the nodes description and
	 * metadata
	 * 
	 * @return The description and metadata edit component
	 */
	public Component getDescriptionEditComponent() {
		return descriptionVc;
	}

	@Override
	protected void doDispose() {
		//child controllers registered with listenTo() get disposed in BasicController	
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
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;		
		tabbedPane.addTab(translate(PANE_TAB_GENERAL), descriptionVc);
		tabbedPane.addTab(translate(PANE_TAB_VISIBILITY), visibilityVc);
		if (childTabsCntrllr != null) {
			childTabsCntrllr.addTabs(tabbedPane);
		}
	}

	@Override
	protected ActivateableTabbableDefaultController[] getChildren() {
		if (childTabsCntrllr != null && childTabsCntrllr instanceof ActivateableTabbableDefaultController) {
			return new ActivateableTabbableDefaultController[] { (ActivateableTabbableDefaultController) childTabsCntrllr };
		} else {
			return new ActivateableTabbableDefaultController[] {};
		}
	}

}
