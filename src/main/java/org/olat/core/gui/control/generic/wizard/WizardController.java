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
* <p>
*/ 

package org.olat.core.gui.control.generic.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Description: <BR>Generic wizard controller. Handles layout, title, cancel
 * button and steps visibility. <P/>
 * 
 * Initial Date: Sep 11, 2004
 * @author Florian Gnaegi
 */

public class WizardController extends BasicController {
	
	private VelocityContainer wizardVC;
	private WizardInfoController wic;
	
	private int steps = 0;
	private int currentStep = 0;
	private Link finishButton;
	private Link cancelButton;

	public WizardController(UserRequest ureq, WindowControl wControl, int steps) {
		super(ureq, wControl);
		
		setBasePackage(WizardController.class);
		
		this.steps = steps;
		
		wizardVC = createVelocityContainer("wizard");
		finishButton = LinkFactory.createCustomLink("finish", "cmd.wizard.cancel", "cmd.wizard.finished", Link.BUTTON, this.wizardVC, this);
		cancelButton = LinkFactory.createCustomLink("cancel", "cmd.wizard.cancel", "cmd.wizard.cancel", Link.BUTTON, this.wizardVC, this);
		
		wic = new WizardInfoController(ureq, this.steps);
		listenTo(wic);
		
		wizardVC.put("wic", wic.getInitialComponent());
		putInitialPanel(wizardVC);
	}

	public void setWizardTitle(String wizardTitle) {
		wizardVC.contextPut("wizardTitle", wizardTitle);
	}

	public void setNextWizardStep(String stepTitle, Component component) {
		this.currentStep++;
		setWizardVcContent(stepTitle, component);
	}

	private void setWizardVcContent(String title, Component component) {
		wic.setCurStep(this.currentStep);
		wizardVC.contextPut("currentStep", Integer.valueOf(currentStep));
		wizardVC.contextPut("steps", Integer.valueOf(steps));
		wizardVC.contextPut("title", title);
		if(currentStep == steps){
			wizardVC.contextPut("lastStep", Boolean.TRUE);
		}else{
			wizardVC.contextPut("lastStep", Boolean.FALSE);
		}
		wizardVC.put("component", component);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == finishButton || source == cancelButton) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
}