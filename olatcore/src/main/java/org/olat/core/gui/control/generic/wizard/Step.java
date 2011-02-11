/**
 * 
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
	
		public Step nextStep() {
			throw new IllegalAccessError("not to be called on NOSTEP");
		}
	
		public PrevNextFinishConfig getInitialPrevNextFinishConfig(){
			throw new IllegalAccessError("not to be called on NOSTEP");
		}
		
		public FormItem getStepTitle() {
			throw new IllegalAccessError("not to be called on NOSTEP");
		}
	
		public FormItem getStepShortDescription() {
			throw new IllegalAccessError("not to be called on NOSTEP");
		}
	
		@SuppressWarnings("unused")
		public StepFormController getStepController(UserRequest ureq, WindowControl windowControl,StepsRunContext stepsRunContext, Form form) {
			throw new IllegalAccessError("not to be called on NOSTEP");
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
