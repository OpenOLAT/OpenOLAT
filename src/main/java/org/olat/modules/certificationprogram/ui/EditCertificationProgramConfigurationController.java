/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import java.math.BigDecimal;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.RecertificationMode;
import org.olat.modules.certificationprogram.ui.component.DurationFormItem;
import org.olat.modules.certificationprogram.ui.component.DurationType;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EditCertificationProgramConfigurationController extends FormBasicController {
	
	private FormToggle validityToggleEl;
	private DurationFormItem validityEl;
	private FormToggle recertificationToggleEl;
	private DurationFormItem recertificationWindowEl;
	private FormToggle creditPointToggleEl;
	private SpacerElement creditPointSpacerEl;
	private SingleSelection recertificationModeEl;
	private SingleSelection systemEl;
	private TextElement creditPointEl;
	private FormLayoutContainer creditPointCont;
	private SingleSelection prematureRecertificationEl;
	
	private final List<CreditPointSystem> systems;
	private CertificationProgram certificationProgram;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CreditPointService creditPointService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public EditCertificationProgramConfigurationController(UserRequest ureq, WindowControl wControl, CertificationProgram certificationProgram) {
		super(ureq, wControl);
		this.certificationProgram = certificationProgram;
		if(organisationModule.isEnabled()) {
			Roles roles = ureq.getUserSession().getRoles();
			systems = creditPointService.getCreditPointSystems(roles);
		} else {
			systems = creditPointService.getCreditPointSystems();
		}
		
		initForm(ureq);
		updateUI();
	}
	
	public CertificationProgram getCertificationProgram() {
		return certificationProgram;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer validatityCont = uifactory.addDefaultFormLayout("validity.container", null, formLayout);
		validatityCont.setFormLayout("0_12");
		validatityCont.setFormTitle(translate("validity.title"));
		initValidityForm(validatityCont);
		
		FormLayoutContainer recertificationCont = uifactory.addDefaultFormLayout("recertification.container", null, formLayout);
		recertificationCont.setFormLayout("0_12");
		recertificationCont.setFormTitle(translate("recertification.title"));
		initRecertificationForm(recertificationCont);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	protected void initValidityForm(FormItemContainer formLayout) {
		validityToggleEl = uifactory.addToggleButton("validity.enable", "validity.enable", translate("on"), translate("off"), formLayout);
		validityToggleEl.toggle(certificationProgram.isValidityEnabled());
		
		String validityDurationVal = Integer.toString(certificationProgram.getValidityTimelapse());
		DurationType validityDurationType = certificationProgram.getValidityTimelapseUnit();
		validityEl = new DurationFormItem("validity.duration", getTranslator(), false);
		validityEl.setLabel("validity.duration", null);
		validityEl.setValue(validityDurationVal, validityDurationType);
		formLayout.add(validityEl);
	}
	
	protected void initRecertificationForm(FormItemContainer formLayout) {
		recertificationToggleEl = uifactory.addToggleButton("recertification.enable", "recertification.enable", translate("on"), translate("off"), formLayout);
		recertificationToggleEl.toggle(certificationProgram.isRecertificationEnabled());
		
		String recertificationWindowVal = Integer.toString(certificationProgram.getRecertificationWindow());
		DurationType recertificationWindowType = certificationProgram.getRecertificationWindowUnit();
		recertificationWindowEl = new DurationFormItem("recertification.window", getTranslator(), true);
		recertificationWindowEl.setLabel("recertification.window", null);
		recertificationWindowEl.setHelpText(translate("recertification.window.hint"));
		recertificationWindowEl.setValue(recertificationWindowVal, recertificationWindowType);
		formLayout.add(recertificationWindowEl);
		
		// Credit point
		creditPointSpacerEl = uifactory.addSpacerElement("creditpoint1", formLayout, false);

		String points = certificationProgram.getCreditPoints() == null ? null : certificationProgram.getCreditPoints().toString();
		CreditPointSystem selectedSystem = certificationProgram.getCreditPointSystem();
		
		creditPointToggleEl = uifactory.addToggleButton("credit.point.enable", "credit.point.enable", translate("on"), translate("off"), formLayout);
		creditPointToggleEl.toggle(selectedSystem != null && StringHelper.containsNonWhitespace(points));
		
		SelectionValues modePK = new SelectionValues();
		modePK.add(SelectionValues.entry(RecertificationMode.automatic.name(), translate("recertification.mode.automatic"),
				translate("recertification.mode.automatic.descr"), null, null, true));
		modePK.add(SelectionValues.entry(RecertificationMode.manual.name(), translate("recertification.mode.manual"),
				translate("recertification.mode.manual.descr"), null, null, true));
		recertificationModeEl = uifactory.addCardSingleSelectHorizontal("recertification.mode", "recertification.mode", formLayout, modePK);
		if(certificationProgram.getRecertificationMode() != null && modePK.containsKey(certificationProgram.getRecertificationMode().name())) {
			recertificationModeEl.select(certificationProgram.getRecertificationMode().name(), true);
		}
		
		// System
		SelectionValues systemPK = new SelectionValues();
		for(CreditPointSystem system:systems) {
			if(system.getStatus() == CreditPointSystemStatus.active || system.equals(selectedSystem)) {
				systemPK.add(SelectionValues.entry(system.getKey().toString(), system.getName()));
			}
		}
		if(selectedSystem != null && !systemPK.containsKey(selectedSystem.getKey().toString())) {
			systemPK.add(SelectionValues.entry(selectedSystem.getKey().toString(), selectedSystem.getName()));
		}
		
		systemEl = uifactory.addDropdownSingleselect("credit.point.system", formLayout, systemPK.keys(), systemPK.values());
		systemEl.addActionListener(FormEvent.ONCHANGE);
		if(selectedSystem != null && systemPK.containsKey(selectedSystem.getKey().toString())) {
			systemEl.select(selectedSystem.getKey().toString(), true);
		} else if(!systemPK.isEmpty()) {
			systemEl.select(systemPK.keys()[0], true);
		}
		systemEl.addActionListener(FormEvent.ONCHANGE);
		systemEl.setMandatory(true);
		
		creditPointCont = uifactory.addInlineFormLayout("credit.point.need", "credit.point.need", formLayout);
		creditPointCont.setMandatory(true);
		
		creditPointEl = uifactory.addTextElement("credit.point.need", 8, points, creditPointCont);
		creditPointEl.setLabel(null, null);
		creditPointEl.setMaxLength(8);
		creditPointEl.setDisplaySize(8);
		creditPointEl.setMandatory(true);
		updateCreditPointUI();
		
		SelectionValues yesNoPk = new SelectionValues();
		yesNoPk.add(SelectionValues.entry(Boolean.TRUE.toString(), translate("yes")));
		yesNoPk.add(SelectionValues.entry(Boolean.FALSE.toString(), translate("no")));
		prematureRecertificationEl = uifactory.addRadiosHorizontal("recertification.premature", "recertification.premature", formLayout,
				yesNoPk.keys(), yesNoPk.values());
		prematureRecertificationEl.setHelpText(translate("recertification.premature.hint"));
		prematureRecertificationEl.select(Boolean.toString(certificationProgram.isPrematureRecertificationByUserEnabled()), true);
	}
	
	private void updateUI() {
		boolean validityEnabled = validityToggleEl.isVisible() && validityToggleEl.isOn();
		validityEl.setVisible(validityEnabled);
		
		recertificationToggleEl.setEnabled(validityEnabled);
		boolean recertificationEnabled = validityEnabled && validityToggleEl.isVisible() && recertificationToggleEl.isOn();
		recertificationWindowEl.setVisible(recertificationEnabled);
		creditPointSpacerEl.setVisible(recertificationEnabled);
		creditPointToggleEl.setVisible(recertificationEnabled);
		
		boolean creditPointEnabled = recertificationEnabled && creditPointToggleEl.isVisible() && creditPointToggleEl.isOn();
		recertificationModeEl.setVisible(creditPointEnabled);
		creditPointCont.setVisible(creditPointEnabled);
		creditPointEl.setVisible(creditPointEnabled);
		systemEl.setVisible(creditPointEnabled);
		prematureRecertificationEl.setVisible(creditPointEnabled);
	}
	
	private void updateCreditPointUI() {
		CreditPointSystem selectedSystem = getSelectedSystem();
		if(selectedSystem == null) {
			creditPointEl.setTextAddOn("", false);
		} else {
			creditPointEl.setTextAddOn(StringHelper.escapeHtml(selectedSystem.getLabel()) + "&nbsp;" + StringHelper.escapeHtml(selectedSystem.getName()), false);
		}
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		if(fiSrc != systemEl) {
			super.propagateDirtinessToContainer(fiSrc, event);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(validityToggleEl == source || recertificationToggleEl == source || creditPointToggleEl == source) {
			updateUI();
		} else if(systemEl == source) {
			updateCreditPointUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		systemEl.clearError();
		if(systemEl.isVisible() && !systemEl.isOneSelected()) {
			systemEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		systemEl.clearError();
		if(systemEl.isVisible() && !systemEl.isOneSelected()) {
			systemEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		allOk &= validateCreditPoints(creditPointEl, true);
		allOk &= validateDuration(validityEl, true);
		allOk &= validateDuration(recertificationWindowEl, false);
		
		return allOk;
	}
	
	private boolean validateDuration(DurationFormItem el, boolean mandatory) {
		boolean allOk = true;
		
		el.clearError();
		if(el.isVisible()) {
			if(el.isOneSelected()) {
				if((el.getValue() != null &&el.getValue().intValue() <= 0)
						|| (el.getType() != null && !StringHelper.isLong(el.getRawValue()))) {
					el.setErrorKey("error.integer.positive");
					allOk &= false;
				} else if(mandatory && (el.getValue() == null || el.getType() == null)) {
					el.setErrorKey("form.legende.mandatory");
					allOk &= false;
				}
			} else {
				el.setErrorKey("error.dropdown.select.one");
				allOk &= false;
			}
		}
		return allOk;
	}
	
	private boolean validateCreditPoints(TextElement el, boolean mandatory) {
		boolean allOk = true;
		
		el.clearError();
		if(el.isVisible()) {
			if(StringHelper.containsNonWhitespace(el.getValue())) {
				if(!StringHelper.isLong(el.getValue())) {
					el.setErrorKey("form.error.nointeger");
					allOk &= false;
				} else {
					try {
						int val = Integer.parseInt(el.getValue());
						if(val <= 0) {
							el.setErrorKey("error.integer.positive");
							allOk &= false;
						}
					} catch (NumberFormatException e) {
						logWarn("", e);
						el.setErrorKey("form.error.nointeger");
						allOk &= false;
					}
				}
			} else if(mandatory) {
				el.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean validityEnabled = validityToggleEl.isVisible() && validityToggleEl.isOn();
		certificationProgram.setValidityEnabled(validityEnabled);
		if(validityEnabled) {
			certificationProgram.setValidityTimelapse(validityEl.getValue().intValue());
			certificationProgram.setValidityTimelapseUnit(validityEl.getType());
		} else {
			certificationProgram.setValidityTimelapse(0);
			certificationProgram.setValidityTimelapseUnit(null);
		}
		
		Integer window = recertificationWindowEl.getValue();
		boolean recertificationEnabled = validityEnabled && recertificationToggleEl.isOn();
		if(recertificationEnabled) {
			certificationProgram.setRecertificationEnabled(true);
			DurationType type = recertificationWindowEl.getType();
			certificationProgram.setRecertificationWindowUnit(type);
			if(type == null || window == null) {
				certificationProgram.setRecertificationWindow(0);
			} else {
				certificationProgram.setRecertificationWindow(window.intValue());
			}
		} else {
			certificationProgram.setRecertificationEnabled(false);
			certificationProgram.setRecertificationWindow(0);
			certificationProgram.setRecertificationWindowUnit(null);
		}
		
		boolean creditPointEnabled = recertificationEnabled && creditPointToggleEl.isOn();
		CreditPointSystem system = getSelectedSystem();
		if(creditPointEnabled && system != null) {
			certificationProgram.setRecertificationMode(RecertificationMode.valueOf(recertificationModeEl.getSelectedKey()));
			certificationProgram.setCreditPointSystem(system);
			BigDecimal points = new BigDecimal(creditPointEl.getValue());
			certificationProgram.setCreditPoints(points);
			certificationProgram.setPrematureRecertificationByUserEnabled(Boolean.TRUE.toString().equals(prematureRecertificationEl.getSelectedKey()));
		} else {
			certificationProgram.setRecertificationMode(null);
			certificationProgram.setCreditPointSystem(null);
			certificationProgram.setCreditPoints(null);
			certificationProgram.setPrematureRecertificationByUserEnabled(false);
		}
		
		certificationProgram = certificationProgramService.updateCertificationProgram(certificationProgram);
		dbInstance.commit();
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private CreditPointSystem getSelectedSystem() {
		if(systemEl.isOneSelected()) {
			String selectedSystemKey = systemEl.getSelectedKey();
			return systems.stream()
					.filter(sys -> selectedSystemKey.equals(sys.getKey().toString()))
					.findFirst().orElse(null);
		}
		return null;
	}
}
