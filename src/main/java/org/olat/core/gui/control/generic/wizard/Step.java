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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;

/**
 * @author patrickb
 *
 */
public interface Step {
	/**
	 * defines the no-op step, e.q. this should be used in the case where
	 * nextStep has to say -> sorry no more steps available. The NOSTEP is to be 
	 * used instead of <code>null</code>
	 */
	public final static Step NOSTEP = new Step() {
		@Override
		public Step nextStep() {
			throw new IllegalAccessError("not to be called on NOSTEP");
		}

		@Override
		public PrevNextFinishConfig getInitialPrevNextFinishConfig(){
			throw new IllegalAccessError("not to be called on NOSTEP");
		}

		@Override
		public FormItem getStepTitle() {
			throw new IllegalAccessError("not to be called on NOSTEP");
		}

		@Override
		public FormItem getStepShortDescription() {
			throw new IllegalAccessError("not to be called on NOSTEP");
		}

		@Override
		public StepFormController getStepController(UserRequest ureq, WindowControl windowControl,StepsRunContext stepsRunContext, Form form) {
			throw new IllegalAccessError("not to be called on NOSTEP");
		}

		@Override
		public StepCollection getStepCollection() {
			return null;
		}
	
	};
	
	/**
	 * next step
	 * @return
	 */
	public Step nextStep();

	/**
	 * get the initial configuration of the wizard/steps navigation buttons
	 * @return
	 */
	public PrevNextFinishConfig getInitialPrevNextFinishConfig();
	
	public StepCollection getStepCollection();
	
	/**
	 * descriptive and short title of input mask or info mask presented by this step.
	 * i.e. Choose colors, Review Changes, Send E-Mail.
	 * @return
	 */
	public FormItem getStepTitle();
	
	/**
	 * tells in one or two sentences what the title could not express.
	 * @return
	 */
	public FormItem getStepShortDescription();
	
	/**
	 * the step controller, which also provides the content for the page as formitem
	 * @param form 
	 * @param windowControl 
	 * @param ureq 
	 * @param stepsRunContext 
	 * @return
	 */
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form);
	
}
