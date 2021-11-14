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
package org.olat.gui.demo.guidemo.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.dev.controller.SourceViewController;
import org.olat.gui.demo.guidemo.GuiDemoFlexiForm;

/**
 * Initial Date: 10.01.2008 <br>
 * 
 * @author patrickb
 */
public class GuiDemoStepsRunner extends BasicController {
	private VelocityContainer mainVC;
	private Link startLink;
	private StepsMainRunController smrc;

	public GuiDemoStepsRunner(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		mainVC = createVelocityContainer("stepsrunnerindex");
		startLink = LinkFactory.createButton("start", mainVC, this);
		
		//add source view control
    Controller sourceview = new SourceViewController(ureq, control, this.getClass(), mainVC);
    mainVC.put("sourceview", sourceview.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == smrc) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(smrc);
				showInfo("cancel");
			} else if (event == Event.DONE_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(smrc);
				showInfo("ok");
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == startLink) {
			/*
			 * start step which spawns the whole wizard
			 */
			Step start = new StartStepImpl(ureq);
			/*
			 * wizard finish callback called after "finish" is called
			 */
			StepRunnerCallback finishCallback = new StepRunnerCallback() {
				public Step execute(UserRequest ureq2, WindowControl control, StepsRunContext runContext) {
					// here goes the code which reads out the wizards data from the
					// runContext and then does some wizardry
					//
					// after successfully finishing -> send a DONE_CHANGED or DONE_UNCHANGED to indicate proper
					// finishing
					return StepsMainRunController.DONE_UNCHANGED;
				}

			};
			smrc = new StepsMainRunController(ureq, getWindowControl(), start, finishCallback, null, "A Workflow", "o_sel_demo_wizard");
			listenTo(smrc);
			getWindowControl().pushAsModalDialog(smrc.getInitialComponent());

		}
	}

	/**
	 * step classes
	 */

	private final class StartStepImpl extends BasicStep {

		public StartStepImpl(UserRequest ureq) {
			super(ureq);
			//set name of step and a short description
			setI18nTitleAndDescr("start", "start.short.desc");
			setNextStep(new StepTwo(ureq));
		}

		public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext runContext, Form form) {
			StepFormController stepP = new StartStepForm(ureq, windowControl, form, runContext);
			return stepP;
		}

		public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
			return new PrevNextFinishConfig(false, true, false);
		}
	}

	private final class StartStepForm extends StepFormBasicController {
		private TextElement firstName;
		private TextElement lastName;
		private TextElement institution;

		StartStepForm(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_DEFAULT, null);
			setBasePackage(GuiDemoFlexiForm.class);
			flc.setTranslator(getTranslator());
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("guidemo_flexi_form_simpleform");
			final int defaultDisplaySize = 32;
			//
			firstName = uifactory.addTextElement("firstname", "guidemo.flexi.form.firstname", 256, "Patrick", formLayout);
			firstName.setDisplaySize(defaultDisplaySize);
			firstName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
			firstName.setMandatory(true);

			lastName = uifactory.addTextElement("lastname", "guidemo.flexi.form.lastname", 256, "Brunner", formLayout);
			lastName.setDisplaySize(defaultDisplaySize);
			lastName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
			lastName.setMandatory(true);

			institution = uifactory.addTextElement("institution", "guidemo.flexi.form.institution", 256, "insti", formLayout);
			institution.setDisplaySize(defaultDisplaySize);

		}

		@Override
		protected void formOK(UserRequest ureq) {
			// form has no more errors
			// save info in run context for next step.
			addToRunContext("firstname", firstName.getValue());
			addToRunContext("lastname", lastName.getValue());
			addToRunContext("institution", institution.getValue());
			// inform surrounding Step runner to proceed
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}

	private final class StepTwo extends BasicStep {

		public StepTwo(UserRequest ureq) {
			super(ureq);
			setI18nTitleAndDescr("step.two", "step.two.short.desc");
		}

		public Step nextStep() {
			// indicate that no next step is possible
			return Step.NOSTEP;
		}

		public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext runContext, Form form) {
			StepFormController stepP = new StepTwoForm(ureq, windowControl, form, runContext);
			return stepP;
		}

		public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
			return new PrevNextFinishConfig(true, false, true);
		}

	}

	private final class StepTwoForm extends StepFormBasicController {

		private TextElement firstName;
		private TextElement lastName;
		private TextElement institution;

		public StepTwoForm(UserRequest ureq, WindowControl control, Form mainForm, StepsRunContext runContext) {
			super(ureq, control, mainForm, runContext, LAYOUT_DEFAULT, null);
			setBasePackage(GuiDemoFlexiForm.class);
			flc.setTranslator(getTranslator());
			initForm(ureq);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			// some code to commit the changes to database
			/*
			 * after all, tell that this was last step, and that we are finished
			 */
			fireEvent(ureq, StepsEvent.INFORM_FINISHED);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			//
			firstName = uifactory.addTextElement("firstname", null, 256, (String) getFromRunContext("firstname"), formLayout);
			firstName.setEnabled(false);

			lastName = uifactory.addTextElement("lastname", null, 256, (String) getFromRunContext("lastname"), formLayout);
			lastName.setEnabled(false);

			institution = uifactory.addTextElement("institution", null, 256, (String) getFromRunContext("institution"), formLayout);
			institution.setEnabled(false);
		}

	}

}
