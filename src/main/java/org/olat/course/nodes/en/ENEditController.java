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

package org.olat.course.nodes.en;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * Description:<BR/> The enrollment edit controller implements the enrollment
 * specific tabs to configure an enrollement node. <P/>
 * 
 * Initial Date: Sep 8, 2004
 * @author Felix Jost, gnaegi
 */
public class ENEditController extends ActivateableTabbableDefaultController implements ControllerEventListener{

	public static final String PANE_TAB_ENCONFIG = "pane.tab.enconfig";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";

	private ModuleConfiguration moduleConfiguration;
	private VelocityContainer myContent;
	
	private ENCourseNode courseNode;
	private ConditionEditController accessibilityCondContr;
	private UserCourseEnvironment euce;
	private TabbedPane myTabbedPane;
	private ENEditGroupAreaFormController easyGroupEditCtrllr;
	final static String[] paneKeys = {PANE_TAB_ENCONFIG,PANE_TAB_ACCESSIBILITY};

	/**
	 * @param config
	 * @param ureq
	 * @param enCourseNode
	 * @param course
	 */
	public ENEditController(ModuleConfiguration config, UserRequest ureq, WindowControl wControl, ENCourseNode enCourseNode, ICourse course,
			UserCourseEnvironment euce) {
		super(ureq,wControl);
		init(config, ureq, enCourseNode, course, euce);
	}

	/**
	 * @param config
	 * @param ureq
	 * @param enCourseNode
	 * @param course
	 * @param euce
	 */
	private void init(ModuleConfiguration config, UserRequest ureq, ENCourseNode enCourseNode, ICourse courseP, UserCourseEnvironment euceP) {
		this.moduleConfiguration = config;
		this.courseNode = enCourseNode;
		this.euce = euceP;
		
		myContent = createVelocityContainer("edit");
		doFormInit(ureq);

		// Accessibility precondition
		Condition accessCondition = courseNode.getPreConditionAccess();
		accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), euceP, accessCondition,
				AssessmentHelper.getAssessableNodes(courseP.getEditorTreeModel(), enCourseNode));		
		listenTo(accessibilityCondContr);

		// not needed: setInitialComponent(myContent) since tabbable controller
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// 
	}

	private void doFormInit(UserRequest ureq) {
		easyGroupEditCtrllr = new ENEditGroupAreaFormController(ureq, getWindowControl(), moduleConfiguration, euce.getCourseEditorEnv());
		easyGroupEditCtrllr.addControllerListener(this);
		myContent.put("groupnameform",easyGroupEditCtrllr.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == accessibilityCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessibilityCondContr.getCondition();
				courseNode.setPreConditionAccess(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}else if (source == easyGroupEditCtrllr) {
			//somehting changed in the nodeconfig
			moduleConfiguration = easyGroupEditCtrllr.getModuleConfiguration();
			fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableDefaultController#addTabs(org.olat.core.gui.components.TabbedPane)
	 */
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;

		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
		tabbedPane.addTab(translate(PANE_TAB_ENCONFIG), myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
    //	child controllers registered with listenTo() get disposed in BasicController
	}

	/**
	 * @param moduleConfiguration
	 * @return true if module configuration is valid
	 */
	public static boolean isConfigValid(ModuleConfiguration moduleConfiguration) {
		String groupNames = (String)moduleConfiguration.get(ENCourseNode.CONFIG_GROUPNAME);
		String areaNames = (String)moduleConfiguration.get(ENCourseNode.CONFIG_AREANAME);
		List<Long> groupKeys = moduleConfiguration.getList(ENCourseNode.CONFIG_GROUP_IDS, Long.class);
		List<Long> areaKeys = moduleConfiguration.getList(ENCourseNode.CONFIG_AREA_IDS, Long.class);
		return StringHelper.containsNonWhitespace(groupNames) || StringHelper.containsNonWhitespace(areaNames)
				|| (groupKeys != null && groupKeys.size() > 0) || (areaKeys != null && areaKeys.size() > 0);
	}

	public String[] getPaneKeys() {
		return paneKeys;
	}

	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}
	
	
	
}
