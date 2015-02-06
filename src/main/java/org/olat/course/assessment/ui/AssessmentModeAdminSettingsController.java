package org.olat.course.assessment.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.AssessmentModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeAdminSettingsController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	
	private MultipleSelectionElement enableEl;
	
	@Autowired
	private AssessmentModule assessmentModule;
	
	public AssessmentModeAdminSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("assessment.mode.title");
		
		
		String[] onValues = new String[]{ translate("assessment.mode.enabled.on") };
		enableEl = uifactory.addCheckboxesHorizontal("modeenabled", "assessment.mode.enabled", formLayout, onKeys, onValues);
		enableEl.select(onKeys[0], assessmentModule.isAssessmentModeEnabled());
		enableEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			boolean enabled = enableEl.isAtLeastSelected(1);
			assessmentModule.setAssessmentModeEnabled(enabled);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
