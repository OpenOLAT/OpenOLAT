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

import java.io.IOException;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;

/**
 * enclosing_type Description: <br>
 * this controller takes a component in its contructor and wraps a velocity
 * container around it with a single link/button (with a userdefined
 * displayname) which closes the dialog. <br>
 * Important: the method getMainComponent is overridden and throws an Exception,
 * since there is a different method to be used: activate(WindowController
 * wControl). This reason is the this controller is intended to be used only as
 * "a popup"/modal dialog (since it offers the 'close' button) and after
 * clicking that button, it should disappear by itself. Therefore you can only
 * use it in conjunction with a WindowsController.
 * 
 * @author Felix Jost
 */
public class WizardInfoController extends DefaultController {
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(WizardInfoController.class);

	private VelocityContainer myContent;
	private int maxSteps;

	/**
	 * @param ureq
	 * @param maxSteps
	 */
	public WizardInfoController(UserRequest ureq, int maxSteps) {
		super(null);
		this.maxSteps = maxSteps;
		Translator trans = Util.createPackageTranslator(WizardInfoController.class, ureq.getLocale());
		myContent = new VelocityContainer("genericwizard", VELOCITY_ROOT + "/wizard_steps.html", trans, null);

		myContent.contextPut("max", String.valueOf(maxSteps));
		myContent.contextPut("myself", this);
		setCurStep(1);
		setInitialComponent(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
	// nothing to listen to
	}

	/**
	 * @param i
	 */
	public void setCurStep(int i) {
		if (i > maxSteps) {
			throw new AssertException("Trying to set a step above max setps.");
		}
		myContent.contextPut("cur", String.valueOf(i));
	}
	
	/**
	 * [used by velocity!!]
	 * @param strStep
	 * @param strMaxStep
	 * @return
	 */
	public StringOutput renderWizardSteps(String strStep, String strMaxStep)
	throws IOException {
		int step = Integer.parseInt(strStep);
		int maxStep = Integer.parseInt(strMaxStep);
		StringOutput sb = new StringOutput(100);
		renderWizardSteps(sb, maxStep, step);
		return sb;
	}
	
	private void renderWizardSteps(StringOutput target, int totalSteps, int step)
	throws IOException {
		for (int i = 1; i <= totalSteps; i++) {
			target.append("<li class='")
			      .append("active", step == i)
			      .append("'><span class'badge'>")
			      .append(Integer.toString(i))
			      .append("</span><span class='chevron'></span></li>");
		}
	}
}