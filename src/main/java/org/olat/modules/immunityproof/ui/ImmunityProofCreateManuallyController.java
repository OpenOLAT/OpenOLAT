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
package org.olat.modules.immunityproof.ui;

import java.util.Date;

import org.olat.admin.user.UserShortDescription;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.ImmunityProofModule.ImmunityProofType;
import org.olat.modules.immunityproof.ImmunityProofService;
import org.olat.modules.immunityproof.ui.event.ImmunityProofAddedEvent;
import org.olat.user.DisplayPortraitController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 09.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofCreateManuallyController extends FormBasicController {

	private final boolean usedByCovidCommissioner;
	private final Identity editedIdentity;
	
	private FormLayoutContainer methodLayout;
	private SingleSelection immunityMethodEl;
	
	private SelectionValue vaccination;
	private SelectionValue recovery;
	private SelectionValue testPCR;
	private SelectionValue testAntigen;
	private SelectionValue medicalCertificate;
	
	private FormLayoutContainer userInfoLayout;
	
	private FormLayoutContainer vaccinationMethod;
	private MultipleSelectionElement confirmVaccinationEl;
	private DateChooser vaccinationDateChooser;
	
	private FormLayoutContainer recoveryMethod;
	private DateChooser recoveryDateChooser;
	
	private FormLayoutContainer testPCRMethod;
	private DateChooser testPCRDateChooser;
	
	private FormLayoutContainer testAntigenMethod;
	private DateChooser testAntigenDateChooser;
	
	private FormLayoutContainer medicalCertificateMethod;
	private DateChooser medicalCertificateDateChooser;
	
	private MultipleSelectionElement confirmReminderEl;
	private MultipleSelectionElement confirmTruthEl;
	
	private ImmunityProofCreateConfirmController confirmController;
	private CloseableModalController cmc;
	
	@Autowired
	private ImmunityProofService immunityProofService;
	@Autowired
	private ImmunityProofModule immunityProofModule;
	
	public ImmunityProofCreateManuallyController(UserRequest ureq, WindowControl wControl, Identity editedIdentity, boolean usedByCovidCommissioner) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		
		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));
		
		this.usedByCovidCommissioner = usedByCovidCommissioner;
		this.editedIdentity = editedIdentity;
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// User info
		if (usedByCovidCommissioner) {
			String page = Util.getPackageVelocityRoot(getClass()) + "/immunity_proof_user_details.html";
			userInfoLayout = FormLayoutContainer.createCustomFormLayout("immunity_proof_user_details", getTranslator(), page);
			
			DisplayPortraitController displayPortraitController = new DisplayPortraitController(ureq, getWindowControl(), editedIdentity, true, true);
			userInfoLayout.put("portrait", displayPortraitController.getInitialComponent());

			UserShortDescription userShortDescriptionController = new UserShortDescription(ureq, getWindowControl(), editedIdentity, ImmunityProofModule.USER_PROPERTY_HANDLER);
			userInfoLayout.put("userShortDescription", userShortDescriptionController.getInitialComponent());
			
			formLayout.add(userInfoLayout);
		}
		
		// Immunity method
		vaccination = new SelectionValue("vaccination", translate("vaccination"));
		recovery = new SelectionValue("recovery", translate("recovery"));
		testPCR = new SelectionValue("test.pcr", translate("test.pcr"));
		testAntigen = new SelectionValue("test.antigen", translate("test.antigen"));
		medicalCertificate = new SelectionValue("medical.certificate", translate("medical.certificate"));
		
		SelectionValues methods = new SelectionValues(vaccination, recovery);
		
		if (immunityProofModule.getValidityPCR() > 0) {
			methods.add(testPCR);
		}
		
		if (immunityProofModule.getValidityAntigen() > 0) {
			methods.add(testAntigen);
		}
		
		if (usedByCovidCommissioner) {
			methods.add(medicalCertificate);
		}
		
		methodLayout = FormLayoutContainer.createDefaultFormLayout("methodLayout", getTranslator());
		methodLayout.setRootForm(mainForm);
		formLayout.add(methodLayout);
		
		immunityMethodEl = uifactory.addDropdownSingleselect("proof.method", methodLayout, methods.keys(), methods.values());
		immunityMethodEl.select(vaccination.getKey(), true);
		immunityMethodEl.addActionListener(FormEvent.ONCHANGE);
		
		
		vaccinationMethod = FormLayoutContainer.createDefaultFormLayout("vaccinationMethod", getTranslator());
		vaccinationMethod.setRootForm(mainForm);
		formLayout.add(vaccinationMethod);
		
		SelectionValue validVaccination = new SelectionValue("valid.accination", translate("vaccination.valid", new String[] { immunityProofModule.getValidVaccines() }));
		SelectionValue lastVaccination = new SelectionValue("last.vaccination", translate("vaccination.last"));
		SelectionValues vaccinationOptions = new SelectionValues(validVaccination, lastVaccination);
		
		confirmVaccinationEl = uifactory.addCheckboxesVertical("vaccination.information", vaccinationMethod, vaccinationOptions.keys(), vaccinationOptions.values(), 1);
		
		vaccinationDateChooser = uifactory.addDateChooser("vaccination.date", null, vaccinationMethod);
		
		
		recoveryMethod = FormLayoutContainer.createDefaultFormLayout("recoveryMethod", getTranslator());
		recoveryMethod.setRootForm(mainForm);
		formLayout.add(recoveryMethod);
		
		recoveryDateChooser = uifactory.addDateChooser("recovery.date", null, recoveryMethod);
		
		
		testPCRMethod = FormLayoutContainer.createDefaultFormLayout("testPCRMethod", getTranslator());
		testPCRMethod.setRootForm(mainForm);
		formLayout.add(testPCRMethod);
		
		testPCRDateChooser = uifactory.addDateChooser("test.pcr.date", null, testPCRMethod);
		testPCRDateChooser.setDateChooserTimeEnabled(true);
		
		
		testAntigenMethod = FormLayoutContainer.createDefaultFormLayout("testAntigenMethod", getTranslator());
		testAntigenMethod.setRootForm(mainForm);
		formLayout.add(testAntigenMethod);
		
		testAntigenDateChooser = uifactory.addDateChooser("test.antigen.date", null, testAntigenMethod);
		testAntigenDateChooser.setDateChooserTimeEnabled(true);
		
		
		medicalCertificateMethod = FormLayoutContainer.createDefaultFormLayout("medicalCertificate", getTranslator());
		medicalCertificateMethod.setRootForm(mainForm);
		formLayout.add(medicalCertificateMethod);
		
		medicalCertificateDateChooser = uifactory.addDateChooser("medical.certificate.date", null, medicalCertificateMethod);
		
		
		FormLayoutContainer confirmLayout = FormLayoutContainer.createDefaultFormLayout("truthLayout", getTranslator());
		confirmLayout.setRootForm(mainForm);
		formLayout.add(confirmLayout);
		
		SelectionValues truthOptions = new SelectionValues(new SelectionValue("confirm.truth", translate("confirm.truth")));
		confirmTruthEl = uifactory.addCheckboxesVertical("confirm.truth.label", confirmLayout, truthOptions.keys(), truthOptions.values(), 1);
		
		SelectionValues reminderOptions = new SelectionValues(new SelectionValue("confirm.reminder", translate("confirm.reminder")));
		confirmReminderEl = uifactory.addCheckboxesVertical("confirm.reminder.label", confirmLayout, reminderOptions.keys(), reminderOptions.values(), 1);
		confirmReminderEl.select("confirm.reminder", true);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonLayout.setRootForm(mainForm);
		confirmLayout.add(buttonLayout);
		
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == immunityMethodEl) {
			updateUI();
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmController) {
        	if (event == Event.DONE_EVENT) {
                formOK(ureq);
            } else {
            	formCancelled(ureq);
            }

            cleanUp();
        } else if (source == cmc) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmController);
		removeAsListenerAndDispose(cmc);
		
		confirmController = null;
		cmc = null;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (immunityMethodEl.getSelectedKey().equals(vaccination.getKey())) {
			confirmVaccinationEl.clearError();
			if (!confirmVaccinationEl.isAtLeastSelected(2)) {
				allOk &= false;
				confirmVaccinationEl.setErrorKey("vaccination.error.confirm", null);
			}
			
			allOk &= checkDateElement(vaccinationDateChooser, true);
		} else if (immunityMethodEl.getSelectedKey().equals(recovery.getKey())) {
			allOk &= checkDateElement(recoveryDateChooser, true);
		} else if (immunityMethodEl.getSelectedKey().equals(testPCR.getKey())) {
			allOk &= checkDateElement(testPCRDateChooser, true);
		} else if (immunityMethodEl.getSelectedKey().equals(testAntigen.getKey())) {
			allOk &= checkDateElement(testAntigenDateChooser, true);
		} else if (immunityMethodEl.getSelectedKey().equals(medicalCertificate.getKey())) {
			allOk &= checkDateElement(medicalCertificateDateChooser, false);
		}
		
		confirmTruthEl.clearError();
		if (!confirmTruthEl.isAtLeastSelected(1)) {
			allOk &= false;
			confirmTruthEl.setErrorKey("confirmation.mandatory", null);
		}
		
		ImmunityProof existingProof = immunityProofService.getImmunityProof(editedIdentity);
		
		if (allOk && existingProof != null && existingProof.isValidated()) {
			Date safeUntilDate = null;
			ImmunityProofType type = null;
			
			if (immunityMethodEl.getSelectedKey().equals(vaccination.getKey())) {
				safeUntilDate = vaccinationDateChooser.getDate();
				type = ImmunityProofType.vaccination;
			} else if (immunityMethodEl.getSelectedKey().equals(recovery.getKey())) {
				safeUntilDate = recoveryDateChooser.getDate();
				type = ImmunityProofType.recovery;
			} else if (immunityMethodEl.getSelectedKey().equals(testPCR.getKey())) {
				safeUntilDate = testPCRDateChooser.getDate();
				type = ImmunityProofType.pcrTest;
			} else if (immunityMethodEl.getSelectedKey().equals(testAntigen.getKey())) {
				safeUntilDate = testAntigenDateChooser.getDate();
				type = ImmunityProofType.antigenTest;
			}
			
			if (safeUntilDate != null && immunityProofModule.getValidity(type) != null) {
				long daysToMs = 24l * 60 * 60 * 1000;
				Date maxSafeUntilDate = new Date(safeUntilDate.getTime() + immunityProofModule.getValidity(type).getRight() * daysToMs);
				safeUntilDate = new Date(safeUntilDate.getTime() + immunityProofModule.getValidity(type).getLeft() * daysToMs);
				Date safeUntil = new Date();
				safeUntil.setTime(safeUntil.getTime() + immunityProofModule.getValidity(type).getLeft() * daysToMs);
				
				safeUntilDate = safeUntil.after(maxSafeUntilDate) ? maxSafeUntilDate : safeUntil;
			}
			
			if (safeUntilDate != null && safeUntilDate.before(existingProof.getSafeDate())) {
				allOk &= false;
				
				doAskForConfirmation(ureq);
			}
			
		}
		
		return allOk;
	}
	
	private boolean checkDateElement(DateChooser dateChooser, boolean dateInPast) {
		boolean allOk = true;
		
		dateChooser.clearError();
		Date date = dateChooser.getDate();
		
		if (date != null) {
			if (dateInPast && date.after(new Date())) {
				allOk &= false;
				dateChooser.setErrorKey("date.mandatory.past", null);
			} else if (!dateInPast && date.before(new Date())) {
				allOk &= false;
				dateChooser.setErrorKey("date.mandatory.future", null);
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		Date proofFrom = null;
		ImmunityProofType type = null;
		
		if (immunityMethodEl.getSelectedKey().equals(vaccination.getKey())) {
			proofFrom = vaccinationDateChooser.getDate();
			type = ImmunityProofType.vaccination;
		} else if (immunityMethodEl.getSelectedKey().equals(recovery.getKey())) {
			proofFrom = recoveryDateChooser.getDate();
			type = ImmunityProofType.recovery;
		} else if (immunityMethodEl.getSelectedKey().equals(testPCR.getKey())) {
			proofFrom = testPCRDateChooser.getDate();
			type = ImmunityProofType.pcrTest;
		} else if (immunityMethodEl.getSelectedKey().equals(testAntigen.getKey())) {
			proofFrom = testAntigenDateChooser.getDate();
			type = ImmunityProofType.antigenTest;
		} else if (immunityMethodEl.getSelectedKey().equals(medicalCertificate.getKey())) {
			proofFrom = medicalCertificateDateChooser.getDate();
			type = ImmunityProofType.medicalCertificate;
		}
		
		boolean sendMail = confirmReminderEl.isAtLeastSelected(1);
		
		immunityProofService.createImmunityProof(editedIdentity, type, proofFrom, null, sendMail, usedByCovidCommissioner, true);
		
		fireEvent(ureq, new ImmunityProofAddedEvent(editedIdentity));
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		if (usedByCovidCommissioner) {
			String name = "";
			User user = editedIdentity.getUser();
			
			if (StringHelper.containsNonWhitespace(user.getFirstName())) {
				name += user.getFirstName() + " ";
			} 
			
			if (StringHelper.containsNonWhitespace(user.getLastName())) {
				name += user.getLastName();
			}
			
			getWindowControl().setInfo(translate("proof.not.added.heading"), translate("proof.not.added.for", new String[] {name}));
		}
		
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void updateUI() {
		vaccinationMethod.setVisible(false);
		recoveryMethod.setVisible(false);
		testPCRMethod.setVisible(false);
		testAntigenMethod.setVisible(false);
		medicalCertificateMethod.setVisible(false);
		
		if (immunityMethodEl.getSelectedKey().equals(vaccination.getKey())) {
			vaccinationMethod.setVisible(true);
		} else if (immunityMethodEl.getSelectedKey().equals(recovery.getKey())) {
			recoveryMethod.setVisible(true);
		} else if (immunityMethodEl.getSelectedKey().equals(testPCR.getKey())) {
			testPCRMethod.setVisible(true);
		} else if (immunityMethodEl.getSelectedKey().equals(testAntigen.getKey())) {
			testAntigenMethod.setVisible(true);
		} else if (immunityMethodEl.getSelectedKey().equals(medicalCertificate.getKey())) {
			medicalCertificateMethod.setVisible(true);
		}
	}
	
	private void doAskForConfirmation(UserRequest ureq) {
		confirmController = new ImmunityProofCreateConfirmController(ureq, getWindowControl());
        cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmController.getInitialComponent(), true, translate("confirm.date.override"), true, true);
        listenTo(confirmController);
        listenTo(cmc);
        cmc.activate();
	}

}
