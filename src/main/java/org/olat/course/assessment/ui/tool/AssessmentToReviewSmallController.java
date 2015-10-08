package org.olat.course.assessment.ui.tool;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentToReviewSmallController extends FormBasicController {
	
	private final AssessmentToolSecurityCallback assessmentCallback;;
	
	public AssessmentToReviewSmallController(UserRequest ureq, WindowControl wControl,
			AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		this.assessmentCallback = assessmentCallback;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}


	
	

}
