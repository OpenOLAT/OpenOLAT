/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.copy;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.ui.committee.imp.ChoosePositionController;

/**
 * 
 * 
 * Initial date: 7 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopyApplication1PositionStep extends BasicStep {
	
	private final Translator translator;
	private final CopyApplicationContext copyContext;
	
	public CopyApplication1PositionStep(UserRequest ureq, CopyApplicationContext copyContext, Translator translator) {
		super(ureq);
		this.copyContext = copyContext;
		this.translator = translator;
		
		setI18nTitleAndDescr("wizard.choose_position.title", "wizard.choose_position.description");
		setNextStep(new CopyApplication2DataStep(ureq, copyContext));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl,
			StepsRunContext runContext, Form form) {
		ChoosePositionController ctrl = new ChoosePositionController(ureq, wControl, runContext, form, copyContext);
		ctrl.setFormTranslatedDescription(translator.translate("choose.position.hint"));
		return ctrl;
	}
}
