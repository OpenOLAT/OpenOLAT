/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.registration.DisclaimerFormController;
import org.olat.registration.RegistrationModule;

/**
 * 
 * Initial date: 3 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RefereeDisclaimerController extends FormBasicController {
	
	public  static final String DCL_CHECKBOX_KEY = "dclchkbox";
	public  static final String DCL_CHECKBOX_KEY2 = "dclchkbox2";
	public  static final String DCL_ACCEPT = "dcl.accept";
	private static final String NLS_DISCLAIMER_ACKNOWLEDGED = "disclaimer.acknowledged";
	private static final String NLS_DISCLAIMER_OK = "disclaimer.ok";
	private static final String NLS_DISCLAIMER_NOK = "disclaimer.nok";
	private static final String ACKNOWLEDGE_CHECKBOX_NAME = "acknowledge_checkbox";
	private static final String ADDITIONAL_CHECKBOX_NAME = "additional_checkbox";
	
	private MultipleSelectionElement acceptCheckbox;
	private MultipleSelectionElement additionalCheckbox;
	
	private boolean readOnly;
	
	public RefereeDisclaimerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, null, "disclaimer", Util.createPackageTranslator(DisclaimerFormController.class, ureq.getLocale()));
		readOnly = false;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.contextPut("showLegend", Boolean.TRUE);
		
		FormLayoutContainer dclFormLayout = FormLayoutContainer.createVerticalFormLayout("dclform", getTranslator());
		formLayout.add(dclFormLayout);
		formLayout.add("dclform", dclFormLayout);
		
		// Add the "accept" checkbox to the form.
		acceptCheckbox = uifactory.addCheckboxesVertical(ACKNOWLEDGE_CHECKBOX_NAME, null, dclFormLayout, new String[] {DCL_CHECKBOX_KEY}, new String[] {translate(NLS_DISCLAIMER_ACKNOWLEDGED)}, 1);
		acceptCheckbox.setEscapeHtml(false);
		acceptCheckbox.setMandatory(false);
		acceptCheckbox.select(DCL_CHECKBOX_KEY, readOnly);
		
		// Add the additional checkbox to the form (depending on the configuration)
		if(CoreSpringFactory.getImpl(RegistrationModule.class).isDisclaimerAdditionalCheckbox()) {
			String additionalCheckboxText = translate("disclaimer.additionalcheckbox");
			if (additionalCheckboxText != null) {
				additionalCheckbox = uifactory.addCheckboxesVertical(ADDITIONAL_CHECKBOX_NAME, null, dclFormLayout, new String[] {DCL_CHECKBOX_KEY2}, new String[] {additionalCheckboxText}, 1);
				additionalCheckbox.setEscapeHtml(false);
				additionalCheckbox.select(DCL_CHECKBOX_KEY2, readOnly);
			}
		}
				
		if (readOnly) {
			// Disable when set to read only
			formLayout.setEnabled(!readOnly);
		} else {
			// Create submit and cancel buttons
			final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
			formLayout.add(buttonLayout);
			buttonLayout.setElementCssClass("o_sel_disclaimer_buttons");
			uifactory.addFormSubmitButton(DCL_ACCEPT, NLS_DISCLAIMER_OK, buttonLayout);
			uifactory.addFormCancelButton(NLS_DISCLAIMER_NOK, buttonLayout, ureq, getWindowControl());			
		}
	}
	
	public boolean isAccepted() {
		return acceptCheckbox.isAtLeastSelected(1) && (additionalCheckbox == null || additionalCheckbox.isAtLeastSelected(1));
	}
	
	public String disableLegend() {
		flc.contextPut("showLegend", Boolean.FALSE);
		return translate("disclaimer.terms.of.usage");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formNOK(UserRequest ureq) {
		fireEvent(ureq, Event.FAILED_EVENT);
	}
}
