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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.gui.control.winmgr.ScrollTopCommand;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.GenericEventListener;

/**
 * @author patrickb
 * 
 */
public class StepsMainRunController extends FormBasicController implements GenericEventListener {

	public final static Step DONE_UNCHANGED = new Step(){
	
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
	public final static Step DONE_MODIFIED = new Step(){
		
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
	
	
	private FormLink prevButton;
	private FormLink nextButton;
	private FormLink finishButton;
	private FormLink cancelButton;
	private FormLink closeLink;
	
	private final StepsRunContext stepsContext = new DefaultStepsRunContext();
	private final StepRunnerCallback cancel;
	private final StepRunnerCallback finish;
	private final List<FormItem> stepTitleLinks = new ArrayList<>();
	private final List<FormItem> mainStepTitles = new ArrayList<>();
	private final Map<FormItem, FormItem> stepToParentTitle = new HashMap<>();
	private final Map<FormItem, List<FormItem>> parentToChildrenTitle = new HashMap<>();
	private final Deque<FormItem> stepPages = new ArrayDeque<>();
	private final Deque<StepFormController> stepPagesController = new ArrayDeque<>();
	private final Deque<Step> steps = new ArrayDeque<>();
	private final Step startStep;
	private Step nextStep;
	private int currentStepIndex = -1;
	private Event lastEvent;
	private boolean doAfterDispatch;
	private ControllerCreator nextChildCreator;
	private boolean finishCycle = false;


	public StepsMainRunController(UserRequest ureq, WindowControl control, Step startStep, StepRunnerCallback finish,
			StepRunnerCallback cancel, String wizardTitle, String elementCssClass) {
		this(ureq, control, startStep, finish, cancel, wizardTitle, elementCssClass, "");
	}
	
	public StepsMainRunController(UserRequest ureq, WindowControl control, Step startStep, StepRunnerCallback finish,
			StepRunnerCallback cancel, String wizardTitle, String elementCssClass, String contextHelpPage) {
		super(ureq, control, "stepslayout");
		this.finish = finish;
		this.cancel = cancel;
		this.startStep = startStep;
		
		flc.contextPut("wizardTitle", wizardTitle);
		flc.contextPut("elementCssClass", elementCssClass);
		if (StringHelper.containsNonWhitespace(contextHelpPage)) {
			flc.contextPut("helpPage", contextHelpPage);
		}
		
		initForm(ureq);
		updateTitleItems();
		addNextStep(startStep.getStepController(ureq, getWindowControl(), this.stepsContext, this.mainForm), startStep);
		
		getWindowControl().getWindowBackOffice().addCycleListener(this);
	}
	
	public StepsRunContext getRunContext() {
		return stepsContext;
	}

	@Override
	protected void doDispose() {
		getWindowControl().getWindowBackOffice().removeCycleListener(this);
	}

	@Override
	protected void formOK(UserRequest ureq) {
	// unused
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == cancelButton || source == closeLink) {
			if (cancel != null) {
				// execute some cancel / rollback code
				// a wizard is expected not to touch / change data in the cancel
				// case undo your work here.
				Step returnStep = cancel.execute(ureq, getWindowControl(), stepsContext);
				if (returnStep != Step.NOSTEP) {
					// error case FIXME:pb finish wizard for this case
				} else {
					// fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			}
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if (source == nextButton) {
			// submit and let current unsaved step do its work
			flc.getRootForm().submitAndNext(ureq);
			getWindowControl().getWindowBackOffice()
				.sendCommandTo(new ScrollTopCommand());
			// the current step decides whether to proceed to the next step or
			// not.
		} else if (source == finishButton) {
			// submit and let last unsaved step do its work
			finishCycle = true;
			flc.getRootForm().submitAndFinish(ureq);
			getWindowControl().getWindowBackOffice()
				.sendCommandTo(new ScrollTopCommand());
			// the current step decides whether to proceed or not
			// an end step will fire FINISH
			// a intermediate step will fire NEXT .. but NEXT && FINISHCYCLE
			// means also finish
		} else if (source == prevButton) {
			lastEvent = StepsEvent.ACTIVATE_PREVIOUS;
			doAfterDispatch = true;
			getWindowControl().getWindowBackOffice()
				.sendCommandTo(new JSCommand("try { o_scrollToElement('.o_wizard.modal.show.in'); } catch(e){ }"));
		} else {
			int whichTitleClickedIndex = stepTitleLinks.indexOf(source);
			if (whichTitleClickedIndex < 0) {
				if (parentToChildrenTitle.containsKey(source)) {
					FormItem firstSubTitle = parentToChildrenTitle.get(source).get(0);
					whichTitleClickedIndex = stepTitleLinks.indexOf(firstSubTitle);
				}
			}
			if (whichTitleClickedIndex >= 0) {
				// remove all steps until the clicked one
				for (int from = currentStepIndex; from > whichTitleClickedIndex; from--) {
					stepPages.pop();
					steps.pop();
					currentStepIndex--;
					StepFormController controller = stepPagesController.pop();
					controller.back();
					removeAsListenerAndDispose(controller);
				}
				activateTitle(currentStepIndex);
				flc.add("FFO_CURRENTSTEPPAGE", stepPages.peek());
				PrevNextFinishConfig pnfConf = steps.peek().getInitialPrevNextFinishConfig();
				prevButton.setEnabled(pnfConf.isBackIsEnabled());
				nextButton.setEnabled(pnfConf.isNextIsEnabled());
				finishButton.setEnabled(pnfConf.isFinishIsEnabled());
			}
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// steps/wizard navigation .. as start most of buttons are disabled
		// they must be enabled by the first step according to its rules
		// cancel button is not possible to disable
		prevButton = new FormLinkImpl("back");
		prevButton.setCustomEnabledLinkCSS("btn btn-default o_wizard_button_prev");
		prevButton.setCustomDisabledLinkCSS("btn btn-default o_wizard_button_prev");
		prevButton.setIconLeftCSS("o_icon o_icon_previous_step o_icon-fw");
		nextButton = new FormLinkImpl("next");
		nextButton.setCustomEnabledLinkCSS("btn btn-default o_wizard_button_next");
		nextButton.setCustomDisabledLinkCSS("btn btn-default o_wizard_button_next");
		nextButton.setIconRightCSS("o_icon o_icon_next_step o_icon-fw");
		finishButton = new FormLinkImpl("finish");
		finishButton.setCustomEnabledLinkCSS("btn btn-default o_wizard_button_finish");
		finishButton.setCustomDisabledLinkCSS("btn btn-default o_wizard_button_finish");
		cancelButton = new FormLinkImpl("cancel");
		cancelButton.setCustomEnabledLinkCSS("btn btn-default o_wizard_button_cancel");
		cancelButton.setCustomDisabledLinkCSS("btn btn-default o_wizard_button_cancel");
		closeLink = new FormLinkImpl("closeIcon", "close", "", Link.NONTRANSLATED);
		closeLink.setIconLeftCSS("o_icon o_icon_close");
		formLayout.add(prevButton);
		formLayout.add(nextButton);
		formLayout.add(finishButton);
		formLayout.add(cancelButton);
		formLayout.add(closeLink);
	}
	
	private void updateTitleItems() {
		stepTitleLinks.clear();
		mainStepTitles.clear();
		stepToParentTitle.clear();
		parentToChildrenTitle.clear();
		Step tmp = startStep;
		do {
			FormItem title = tmp.getStepTitle();
			stepTitleLinks.add(title);
			StepCollection stepCollection = tmp.getStepCollection();
			if (stepCollection == null) {
				mainStepTitles.add(title);
			} else {
				FormItem collectionTitle = stepCollection.getTitle();
				stepToParentTitle.put(title, collectionTitle);
				List<FormItem> childrenTitles = parentToChildrenTitle.get(collectionTitle);
				if (childrenTitles == null) {
					childrenTitles = new ArrayList<>();
					parentToChildrenTitle.put(collectionTitle, childrenTitles);
					mainStepTitles.add(collectionTitle);
				}
				childrenTitles.add(title);
			}
			tmp = tmp.nextStep();
		} while (tmp != Step.NOSTEP);
		
		flc.add("stepLinks", stepTitleLinks);
		flc.add("parentLinks", mainStepTitles);
	}
	
	private void addNextStep(StepFormController child, Step nextStep) {
		activateTitle(++currentStepIndex);

		listenTo(child);
		steps.push(nextStep);
		stepPages.push(child.getStepFormItem());
		stepPagesController.push(child);
		flc.add("FFO_CURRENTSTEPPAGE", stepPages.peek());

		PrevNextFinishConfig pnfConf = nextStep.getInitialPrevNextFinishConfig();
		prevButton.setEnabled(pnfConf.isBackIsEnabled());
		nextButton.setEnabled(pnfConf.isNextIsEnabled());//
		finishButton.setEnabled(pnfConf.isFinishIsEnabled());
	}
	
	private void activateTitle(int stepIndex) {
		// Enable titles of previous steps
		for (int titlesIndex = 0; titlesIndex < stepTitleLinks.size(); titlesIndex++) {
			boolean enabled = titlesIndex < stepIndex;
			FormItem titleLink = stepTitleLinks.get(titlesIndex);
			titleLink.setEnabled(enabled);
			if (stepToParentTitle.containsKey(titleLink)) {
				stepToParentTitle.get(titleLink).setEnabled(enabled);
			}
		}
		
		// Reset sub step titles
		flc.contextRemove("subStepPos");
		flc.contextRemove("subStepTitles");
		FormItem currentTitle = stepTitleLinks.get(stepIndex);
		FormItem mainTitle = currentTitle;
		if (stepToParentTitle.containsKey(currentTitle)) {
			FormItem parentTitle = stepToParentTitle.get(currentTitle);
			List<FormItem> subStepTitles = parentToChildrenTitle.get(parentTitle);
			int subStepPos = subStepTitles.indexOf(currentTitle);
			if (subStepPos >= 0) {
				mainTitle = parentTitle;
				flc.contextPut("subStepTitles", subStepTitles);
				flc.contextPut("subStepPos", subStepPos);
			}
		}
		
		// Reset main titles
		int mainStepPos = mainStepTitles.indexOf(mainTitle);
		flc.contextPut("mainStepTitles", mainStepTitles);
		flc.contextPut("mainStepPos", mainStepPos);
	}

	@Override
	protected void event(final UserRequest ureq, Controller source, Event event) {
		if (source == stepPagesController.peek()) {
			if (event == StepsEvent.ACTIVATE_NEXT && !finishCycle) {
				// intermediate steps wants to proceed - and next link was clicked
				lastEvent = event;
				doAfterDispatch = true;
				// activate next event on source
				Step current = steps.peek();
				nextStep = current.nextStep();
				if(nextStep == Step.NOSTEP) {
					//next but no more step -> finish
					finishWizard(ureq);
				} else {
					nextChildCreator = new ControllerCreator() {
						private final UserRequest ureqForAfterDispatch = ureq;
						@Override
						public Controller createController(UserRequest lureq, WindowControl lwControl) {
							// lureq unused as the remembered ureqForAfterDispatch is
							// taken
							return nextStep.getStepController(ureqForAfterDispatch, lwControl, stepsContext, mainForm);
						}
					};
				}
				// creation of controller and setting the controller is deferred to
				// the afterDispatch Cycle
				//
			} else if (event == StepsEvent.ACTIVATE_NEXT && finishCycle) {
				// intermediate step wants to proceed - but finish link was clicked
				// this means current step validated and we are ready to terminate
				// the wizard.
				finishWizard(ureq);
			} else if (event == StepsEvent.INFORM_FINISHED) {
				// finish link was clicked -> step form controller has valid data ->
				// fires
				// FINISH EVENT
				// all relevant data for finishing the wizards work must now be
				// present in the stepsContext
				finishWizard(ureq);
			} else if (event == StepsEvent.STEPS_CHANGED) {
				updateTitleItems();
			}
		}
	}

	private void finishWizard(final UserRequest ureq) {
		if (finish == null) { throw new AssertException(
				"You must provide a finish callback - a wizard only makes sense to commit work at the end. Do not change data allong the steps."); }
		Step returnStep = finish.execute(ureq, getWindowControl(), stepsContext);
		if(returnStep == DONE_MODIFIED){
			//finish tells that really some data was changed in this wizard
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(returnStep == DONE_UNCHANGED){
			//finish called but nothing was modified
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			//special step comes back
			throw new AssertException("FIXME:pb treat special error steps");
		}
	}

	@Override
	public void event(Event event) {
		/*
		 * activate a new step immediate after dispatch - a new step form controller
		 * can only be added to surrounding steps runner after dispatching -
		 * otherwise a concurrent modification exception within the form container
		 * occurs.
		 */
		if (event == Window.END_OF_DISPATCH_CYCLE && doAfterDispatch) {
			doAfterDispatch = false;
			if (lastEvent == StepsEvent.ACTIVATE_NEXT) {
				// create with null as UserRequest, the controller creator was
				// created
				// during dispatching and controller creation was deferred to
				// the end of
				// dispatch cycle.
				if(nextStep == Step.NOSTEP) {
					nextButton.setEnabled(false);
				} else {
					addNextStep((StepFormController) nextChildCreator.createController(null, getWindowControl()), nextStep);
				}
			} else if (lastEvent == StepsEvent.ACTIVATE_PREVIOUS) {
				if(currentStepIndex <= 0) {
					return;// the case is possible with FireFox and users who use the keyboard and the enter key.
				}
				
				stepPages.pop();
				steps.pop();
				activateTitle(--currentStepIndex);
				StepFormController controller = stepPagesController.pop();
				controller.back();
				removeAsListenerAndDispose(controller);
				flc.add("FFO_CURRENTSTEPPAGE", stepPages.peek());
				PrevNextFinishConfig pnfConf = steps.peek().getInitialPrevNextFinishConfig();
				prevButton.setEnabled(pnfConf.isBackIsEnabled());
				nextButton.setEnabled(pnfConf.isNextIsEnabled());
				finishButton.setEnabled(pnfConf.isFinishIsEnabled());
			}
		}
	}
}
