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

package org.olat.course.nodes.co;

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
import org.olat.course.nodes.COCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * Description:<BR/> Edit controller for the contact form course building block
 * 
 * Initial Date: Oct 13, 2004
 * @author Felix Jost
 * @author Dirk Furrer
 */
public class COEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_COCONFIG = "pane.tab.coconfig";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	private static final String[] paneKeys = {PANE_TAB_COCONFIG,PANE_TAB_ACCESSIBILITY};
	/** deprecated */
	public static final String CONFIG_KEY_EMAILTOAREAS = "emailToAreas";
	public static final String CONFIG_KEY_EMAILTOAREA_IDS = "emailToAreaIds";
	public static final String CONFIG_KEY_EMAILTOPARTICIPANTS = "emailtToPartips";
	public static final String CONFIG_KEY_EMAILTOCOACHES = "emailToCoaches";
	public static final String CONFIG_KEY_EMAILTOGROUP_IDS = "emailToGroupIds";
	public static final String CONFIG_KEY_EMAILTOGROUPS = "emailToGroups";
	
	/** config key: to email addresses to be extracted from specified learn areas */
	public static final String CONFIG_KEY_EMAILTOCOACHES_AREA = "emailToAreaCoaches";
	/** config key: to email addresses to be extracted from specified learn areas */
	public static final String CONFIG_KEY_EMAILTOCOACHES_AREA_IDS = "emailToAreaCoachesIds";
	
	/** config key: keys of the course participants list */
	public static final String CONFIG_KEY_EMAILTOPARTICIPANTS_ALL = "emailToParticipantsAll";
	/** config key: keys of the group participants list */
	public static final String CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP_ID = "emailToGroupParticipantsIds";
	/** config key: email goes to group participants */
	public static final String CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP = "emailToGroupParticipants";
	public static final String CONFIG_KEY_EMAILTOPARTICIPANTS_AREA_ID = "emailToAreaParticipantsIds";
	/** config key: email goes to group participants */
	public static final String CONFIG_KEY_EMAILTOPARTICIPANTS_AREA = "emailToAreaParticipants";
	/** config key: email goes to course participants */
	public static final String CONFIG_KEY_EMAILTOPARTICIPANTS_COURSE = "emailToCourseParticipants";
	/** config key: email goes to group coaches */
	public static final String CONFIG_KEY_EMAILTOCOACHES_GROUP = "emailToGroupCoaches";
	/** config key: key of the group coaches list */
	public static final String CONFIG_KEY_EMAILTOCOACHES_GROUP_ID = "emailToGroupCoachesIds";
	/** config key: key of the course coaches list */
	public static final String CONFIG_KEY_EMAILTOCOACHES_ALL = "emailToCoachesAll";
	/** config key: email goes to course coaches */
	public static final String CONFIG_KEY_EMAILTOCOACHES_COURSE = "emailToCourseCoaches";
	/** config key: email goes to course owners */
	public static final String CONFIG_KEY_EMAILTOOWNERS = "emailToOwners";
	/** config key: email goes to email address */
	public static final String CONFIG_KEY_EMAILTOADRESSES = "emailToAdresses";
	/** config key: default subject text */
	public static final String CONFIG_KEY_MSUBJECT_DEFAULT = "mSubjectDefault";
	/** config key: default body text */
	public static final String CONFIG_KEY_MBODY_DEFAULT = "mBodyDefault";
	/** config key: sent from course */
	public static final String CONFIG_KEY_SENT_FROM_COURSE = "sentFromCourse";
	
	private final VelocityContainer myContent;
	private COConfigForm configForm;	
	private COCourseNode courseNode;
	private ConditionEditController accessibilityCondContr;
	private TabbedPane myTabbedPane;

	/**
	 * Constructor for a contact form edit controller
	 * 
	 * @param config
	 * @param ureq
	 * @param coCourseNode
	 * @param course
	 */
	public COEditController(ModuleConfiguration config, UserRequest ureq, WindowControl wControl, COCourseNode coCourseNode, ICourse course, UserCourseEnvironment euce) {
		super(ureq,wControl);
		this.courseNode = coCourseNode;
		
		myContent = createVelocityContainer("edit");

		configForm = new COConfigForm(ureq, wControl, config, euce);
		configForm.addControllerListener(this);
		myContent.put("configForm", configForm.getInitialComponent());

		// not needed: setInitialComponent(myContent);
		// Accessibility precondition
		Condition accessCondition = courseNode.getPreConditionAccess();
		accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), euce,
				accessCondition, AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), coCourseNode));		
		listenTo(accessibilityCondContr);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessibilityCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessibilityCondContr.getCondition();
				courseNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
		else if (source == configForm) { // those must be links
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableDefaultController#addTabs(org.olat.core.gui.components.TabbedPane)
	 */
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
		tabbedPane.addTab(translate(PANE_TAB_COCONFIG), myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
    //nothing to do
	}

	public String[] getPaneKeys() {
		return paneKeys;
	}

	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}
}