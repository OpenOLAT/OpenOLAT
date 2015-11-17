/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.portfolio.ui.artefacts.collect;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.ui.artefacts.view.EPArtefactViewController;

/**
 * Description:<br>
 * controller for entering a reflexion about the choosen artefact 
 * 
 * can be used inside the collect-wizzard or standalone (depending on used
 * constructor)
 * 
 * <P>
 * Initial Date: 28.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCollectStepForm03 extends StepFormBasicController {


	private static final Integer REFLEXION_MAX_SIZE = 16383;
	private AbstractArtefact artefact;
	private RichTextElement reflexionEl;
	private RichTextElement reflexionOrigEl;
	private String reflexion;
	private String artefactReflexion = "";
	private boolean showNoReflexionOnStructLinkYetWarning = false;

	/**
	 * preset controller with reflexion of the artefact. used by artefact-pool
	 * @param ureq
	 * @param wControl
	 * @param artefact
	 */
	public EPCollectStepForm03(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		// set fallback translator to re-use given strings
		Translator pt = Util.createPackageTranslator(EPArtefactViewController.class, ureq.getLocale(), getTranslator());
		this.flc.setTranslator(pt);
		this.artefact = artefact;
		this.artefactReflexion = artefact.getReflexion();
		initForm(this.flc, this, ureq);
	}
	
	/**
	 * no reflexion on link yet, show warning and preset with artefacts-reflexion
	 * @param ureq
	 * @param wControl
	 * @param artefact
	 * @param showHint
	 */
	public EPCollectStepForm03(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, boolean showHint) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		// set fallback translator to re-use given strings
		Translator pt = Util.createPackageTranslator(EPArtefactViewController.class, ureq.getLocale(), getTranslator());
		this.flc.setTranslator(pt);
		this.showNoReflexionOnStructLinkYetWarning = showHint;
		this.artefact = artefact;
		this.reflexion = artefact.getReflexion();
		initForm(this.flc, this, ureq);
	}

	/**
	 * edit an existing reflexion
	 * @param ureq
	 * @param wControl
	 * @param artefact
	 * @param reflexion
	 */
	public EPCollectStepForm03(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, String reflexion) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		// set fallback translator to re-use given strings
		Translator pt = Util.createPackageTranslator(EPArtefactViewController.class, ureq.getLocale(), getTranslator());
		this.flc.setTranslator(pt);
		this.artefact = artefact;
		this.reflexion = reflexion;
		this.artefactReflexion = artefact.getReflexion();
		
		initForm(this.flc, this, ureq);		
	}

	// used while collecting an artefact
	public EPCollectStepForm03(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout,
			String customLayoutPageName, AbstractArtefact artefact) {
		super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName);
		// set fallback translator to re-use given strings
		Translator pt = Util.createPackageTranslator(EPArtefactViewController.class, ureq.getLocale(), getTranslator());
		this.flc.setTranslator(pt);
		this.artefact = artefact;
		this.artefactReflexion = artefact.getReflexion();
		initForm(ureq);
	}


	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("step3.short.descr");
		setFormContextHelp("Personal Menu#_ep_reflection");

		if (showNoReflexionOnStructLinkYetWarning) {
			uifactory.addStaticTextElement("hint", "info.no.reflexion.yet", "", formLayout);
		}
		
		String text = StringHelper.containsNonWhitespace(reflexion) ? reflexion : artefactReflexion;
		reflexionEl = uifactory.addRichTextElementForStringDataMinimalistic("reflexion", "artefact.reflexion", text, 12, -1,
				formLayout, getWindowControl());
		reflexionEl.setNotLongerThanCheck(REFLEXION_MAX_SIZE, "reflexion.too.long");
		reflexionEl.setMaxLength(REFLEXION_MAX_SIZE);

		if (!isUsedInStepWizzard()) {
			// add form buttons
			uifactory.addFormSubmitButton("stepform.submit", formLayout);
		}
		
		if(artefact != null && StringHelper.containsNonWhitespace(artefactReflexion)) {
			uifactory.addSpacerElement("reflexion-in-space", formLayout, false);
			reflexionOrigEl = uifactory.addRichTextElementForStringDataMinimalistic("reflexion_original", "artefact.reflexion.original", artefactReflexion, 12, -1,
					formLayout, getWindowControl());
			reflexionOrigEl.setEnabled(false);
		}
	}


	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		String reflexionVal = reflexionEl.getValue();
		if (isUsedInStepWizzard()) {
			artefact.setReflexion(reflexionVal);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			// form is used outside of wizzard: 
			// - changing the reflexion of the link "structure <-> artefact", therefore 
			//		the reflexion has not to be set on the artefact itself, but is transmitted with the event
			// - changing the reflexion of the artefact itself
			fireEvent(ureq, new EPReflexionChangeEvent(reflexionVal, artefact));
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing
	}


}
