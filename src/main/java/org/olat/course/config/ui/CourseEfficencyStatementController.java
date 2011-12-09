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

package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.course.config.CourseConfig;

/**
 * Description:<br>
 * TODO: patrick Class Description for CourseEfficencyStatementController
 * <P>
 * Initial Date: Aug 12, 2005 <br>
 * 
 * @author patrick
 */
public class CourseEfficencyStatementController extends BasicController {
	
	private CourseEfficencyStatementForm efficencyForm;
	private VelocityContainer myContent;

	private DialogBoxController disableEfficiencyDC, enableEfficiencyDC;
	private boolean previousValue;
	
	private CourseConfig courseConfig;
	private ILoggingAction loggingAction;

	/**
	 * @param course
	 * @param ureq
	 * @param wControl
	 */
	public CourseEfficencyStatementController(UserRequest ureq, WindowControl wControl, CourseConfig courseConfig) {
		super(ureq, wControl);		
		this.courseConfig = courseConfig;
		//
		myContent = createVelocityContainer("CourseEfficencyStatement");
		efficencyForm = new CourseEfficencyStatementForm(ureq, wControl, courseConfig.isEfficencyStatementEnabled());
		previousValue = courseConfig.isEfficencyStatementEnabled();
		listenTo(efficencyForm);
		myContent.put("efficencyForm", efficencyForm.getInitialComponent());
		
		putInitialPanel(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == disableEfficiencyDC) {
			if (DialogBoxUIFactory.isOkEvent(event)) {				
				// yes disable!				
				courseConfig.setEfficencyStatementIsEnabled(efficencyForm.isEnabledEfficencyStatement());				
				previousValue = efficencyForm.isEnabledEfficencyStatement();				
				loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_DISABLED;
				this.fireEvent(ureq, Event.CHANGED_EVENT);
				
			} else {
				// roll back in form
				efficencyForm.setEnabledEfficencyStatement(true);
			}
		} else if (source == enableEfficiencyDC) {
			if (DialogBoxUIFactory.isOkEvent(event)) {				
				// yes enable!				
				courseConfig.setEfficencyStatementIsEnabled(efficencyForm.isEnabledEfficencyStatement());				
				previousValue = efficencyForm.isEnabledEfficencyStatement();
				loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_ENABLED;
				this.fireEvent(ureq, Event.CHANGED_EVENT);
				
			} else {
				// roll back in form
				efficencyForm.setEnabledEfficencyStatement(false);
			}
		} else if (source == efficencyForm) {
			if ((event == Event.DONE_EVENT) && (previousValue != efficencyForm.isEnabledEfficencyStatement())) {
				// only real changes trigger
				if (previousValue) {
					// a change from enabled Efficiency to disabled
					disableEfficiencyDC = activateYesNoDialog(ureq, null, translate("warning.change.todisabled"), disableEfficiencyDC);
				} else {
					// a change from disabled Efficiency
					enableEfficiencyDC = activateYesNoDialog(ureq, null, translate("warning.change.toenable"), enableEfficiencyDC);
				}
			}
		}
	}

	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	protected void doDispose() {
		//
	}

	/**
	 * 
	 * @return Returns LOG_EFFICIENCY_STATEMENT_ENABLED or LOG_EFFICIENCY_STATEMENT_DISABLED or null if nothing changed.
	 */
	public ILoggingAction getLoggingAction() {
		return loggingAction;
	}

}
