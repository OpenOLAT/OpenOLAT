package org.olat.modules.curriculum.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.curriculum.CurriculumElement;

/**
 * 
 * Initial date: 17 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumStructureCalloutController extends FormBasicController {
	
	private final CurriculumElement curriculumElement;
	
	public CurriculumStructureCalloutController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement) {
		super(ureq, wControl, "curriculum_structure");
		this.curriculumElement = curriculumElement;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	

}
