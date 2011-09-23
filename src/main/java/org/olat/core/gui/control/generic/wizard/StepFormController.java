/**
 * 
 */
package org.olat.core.gui.control.generic.wizard;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.control.Controller;

/**
 * @author patrickb
 *
 */
public interface StepFormController extends Controller{

	/**
	 * the "sub"form container which is placed in the steps main run controller's
	 * place for this steps content. It must be initialized with the surround
	 * Form of he StepsMainRunController.
	 * @return
	 */
	FormItem getStepFormItem();
	
	void back();
	
}
