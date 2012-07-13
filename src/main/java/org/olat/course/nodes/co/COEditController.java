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

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.groupsandrights.ui.GroupAndAreaSelectController;
import org.olat.course.nodes.COCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * Description:<BR/> Edit controller for the contact form course building block
 * 
 * Initial Date: Oct 13, 2004
 * @author Felix Jost
 */
public class COEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_COCONFIG = "pane.tab.coconfig";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	private static final String[] paneKeys = {PANE_TAB_COCONFIG,PANE_TAB_ACCESSIBILITY};
	
	/** config key: to email addresses to be extracted from specified groups */
	public static final String CONFIG_KEY_EMAILTOGROUPS = "emailToGroups";
	/** config key: to email addresses to be extracted from specified learn areas */
	public static final String CONFIG_KEY_EMAILTOAREAS = "emailToAreas";
	/** config key: to email addresses to be extracted from specified groups */
	public static final String CONFIG_KEY_EMAILTOGROUP_IDS = "emailToGroupIds";
	/** config key: to email addresses to be extracted from specified learn areas */
	public static final String CONFIG_KEY_EMAILTOAREA_IDS = "emailToAreaIds";
	/** config key: email goes to partipiciants */
	public static final String CONFIG_KEY_EMAILTOPARTICIPANTS = "emailtToPartips";
	/** config key: email goes to coaches */
	public static final String CONFIG_KEY_EMAILTOCOACHES = "emailToCoaches";
	/** config key: to email address */
	public static final String CONFIG_KEY_EMAILTOADRESSES = "emailToAdresses";
	/** config key: default subject text */
	public static final String CONFIG_KEY_MSUBJECT_DEFAULT = "mSubjectDefault";
	/** config key: default body text */
	public static final String CONFIG_KEY_MBODY_DEFAULT = "mBodyDefault";
	
	private static final String JSELEMENTID = "bel_";
	
	private ModuleConfiguration moduleConfiguration;
	private VelocityContainer myContent;
	private Panel main;
	private COConfigForm configForm;	
	private COCourseNode courseNode;
	private ConditionEditController accessibilityCondContr;
	private ICourse course;
	private GroupAndAreaSelectController selectGroupsCtr;
	private GroupAndAreaSelectController selectAreasCtr;
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
		this.moduleConfiguration = config;
		resolveModuleConfigurationIssues(moduleConfiguration);
		this.courseNode = coCourseNode;
		this.course = course;

		main = new Panel("coeditpanel");

		myContent = this.createVelocityContainer("edit");

		configForm = new COConfigForm(ureq, wControl, config, euce);
		configForm.addControllerListener(this);

		myContent.put("configForm", configForm.getInitialComponent());

		// not needed: setInitialComponent(myContent);
		// Accessibility precondition
		Condition accessCondition = courseNode.getPreConditionAccess();
		accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), course.getCourseEnvironment().getCourseGroupManager(),
				accessCondition, "accessabilityConditionForm", AssessmentHelper.getAssessableNodes(course
						.getEditorTreeModel(), coCourseNode),euce);		
		this.listenTo(accessibilityCondContr);

		main.setContent(myContent);
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
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessibilityCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessibilityCondContr.getCondition();
				courseNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
		else if (source == configForm) { // those must be links
			if (event == Event.CANCELLED_EVENT) {
				return;
			} else if (event == Event.DONE_EVENT) {
				moduleConfiguration.set(CONFIG_KEY_EMAILTOGROUPS, configForm.getEmailGroups());
				moduleConfiguration.set(CONFIG_KEY_EMAILTOAREAS, configForm.getEmailAreas());
				moduleConfiguration.set(CONFIG_KEY_EMAILTOGROUP_IDS, configForm.getEmailGroupIds());
				moduleConfiguration.set(CONFIG_KEY_EMAILTOAREA_IDS, configForm.getEmailAreaIds());
				moduleConfiguration.setBooleanEntry(CONFIG_KEY_EMAILTOCOACHES, configForm.sendToCoaches());
				moduleConfiguration.setBooleanEntry(CONFIG_KEY_EMAILTOPARTICIPANTS, configForm.sendToPartips());
				moduleConfiguration.set(CONFIG_KEY_EMAILTOADRESSES, configForm.getEmailList());
				moduleConfiguration.set(CONFIG_KEY_MSUBJECT_DEFAULT, configForm.getMSubject());
				moduleConfiguration.set(CONFIG_KEY_MBODY_DEFAULT, configForm.getMBody());

				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				return;
			} else if (event.getCommand().equals("popupchoosegroups")) {
				// open a controller in a new window which only results in sending back
				// javascript
				// get preselected groups
				final String groups = (String) moduleConfiguration.get(CONFIG_KEY_EMAILTOGROUPS);
				// get group select controller
				ControllerCreator ctrlCreator = new ControllerCreator() {
					public Controller createController(UserRequest lureq, WindowControl lwControl) {
						selectGroupsCtr = new GroupAndAreaSelectController(lureq,lwControl,course.getCourseEnvironment().getCourseGroupManager(), 
				        GroupAndAreaSelectController.TYPE_GROUP, groups, 
				        JSELEMENTID + "popupchoosegroups"+configForm.hashCode());
						// use a one-column main layout
						// disposed in dispose method of COEditController!
						LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null, selectGroupsCtr.getInitialComponent(), "null");
						return layoutCtr;
					}					
				};
				//wrap the content controller into a full header layout
				ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
				//open in new browser window
				PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
				pbw.open(ureq);
				//
			} else if (event.getCommand().equals("popupchooseareas")) {
				// open a controller in a new window which only results in sending back
				// javascript
				// get preselected areas
				final String areas = (String) moduleConfiguration.get(CONFIG_KEY_EMAILTOAREAS);
				// get area select controller
				ControllerCreator ctrlCreator = new ControllerCreator() {
					public Controller createController(UserRequest lureq, WindowControl lwControl) {
						selectAreasCtr = new GroupAndAreaSelectController(lureq, lwControl, course.getCourseEnvironment().getCourseGroupManager(),
								GroupAndAreaSelectController.TYPE_AREA, areas, JSELEMENTID + "popupchooseareas" + configForm.hashCode());
						// use a one-column main layout
						// disposed in dispose method of COEditController!
						LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null, selectAreasCtr.getInitialComponent(), null);
						return layoutCtr;
					}					
				};
				//wrap the content controller into a full header layout
				ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
				//open in new browser window
				PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
				pbw.open(ureq);
				//
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
    //	child controllers registered with listenTo() get disposed in BasicController
		if (selectGroupsCtr != null) {
			selectGroupsCtr.dispose();
		}
		if (selectAreasCtr != null) {
			selectAreasCtr.dispose();
		}
	}

	/**
	 * resolving version issues of the module configuration, adds new default
	 * values for new keys
	 * 
	 * @param moduleConfiguration2
	 */
	private void resolveModuleConfigurationIssues(ModuleConfiguration moduleConfiguration2) {
		int version = moduleConfiguration2.getConfigurationVersion();
		/*
		 * if no version was set before -> version is 1
		 */
		if (version == 1) {
			// new keys and defaults are
			moduleConfiguration.set(CONFIG_KEY_EMAILTOAREAS, "");
			moduleConfiguration.set(CONFIG_KEY_EMAILTOGROUPS, "");
			moduleConfiguration.setBooleanEntry(CONFIG_KEY_EMAILTOCOACHES, false);
			moduleConfiguration.setBooleanEntry(CONFIG_KEY_EMAILTOPARTICIPANTS, false);
			//
			moduleConfiguration2.setConfigurationVersion(2);
		}

	}

	public String[] getPaneKeys() {
		return paneKeys;
	}

	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}
}