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
package org.olat.modules.creditpoint.ui;

import java.math.BigDecimal;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.RepositoryEntryCreditPointConfiguration;
import org.olat.modules.creditpoint.ui.component.ExpirationFormItem;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointRepositoryEntryConfigController extends FormBasicController {
	
	private static final String DEFAULT_KEY = "odef";
	private static final String OVERRIDE_KEY = "ooverr";
	
	private FormToggle enabledEl;
	private SingleSelection systemEl;
	private TextElement creditPointEl;
	private ExpirationFormItem expirationEl;
	private SingleSelection overrideExpirationEl;

	private final boolean editable;
	private final RepositoryEntry entry;
	private final List<CreditPointSystem> systems;
	private RepositoryEntryCreditPointConfiguration creditPointConfig;
	
	@Autowired
	private CreditPointService creditPointService;
	
	public CreditPointRepositoryEntryConfigController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean editable) {
		super(ureq, wControl);
		this.entry = entry;
		this.editable = editable;
		creditPointConfig = creditPointService.getOrCreateConfiguration(entry);
		systems = creditPointService.getCreditPointSystems();
		
		initForm(ureq);
		updateUI();
		updateOverrideExpirationUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("options.creditpoint.title");
		setFormInfo("options.creditpoint.hint");
		formLayout.setElementCssClass("o_sel_creditpoint_settings");
		
		boolean managedCP = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.creditpoints);
		
		enabledEl = uifactory.addToggleButton("enabled", "options.creditpoint.enable", translate("on"), translate("off"), formLayout);
		enabledEl.addActionListener(FormEvent.ONCLICK);
		enabledEl.setEnabled(editable && !managedCP);
		enabledEl.toggle(creditPointConfig.isEnabled());
		
		// System
		SelectionValues systemPK = new SelectionValues();
		for(CreditPointSystem system:systems) {
			systemPK.add(SelectionValues.entry(system.getKey().toString(), system.getName()));
		}
		
		systemEl = uifactory.addDropdownSingleselect("options.creditpoint.system", formLayout, systemPK.keys(), systemPK.values());
		if(creditPointConfig != null && creditPointConfig.getCreditPointSystem() != null
				&& systemPK.containsKey(creditPointConfig.getCreditPointSystem().getKey().toString())) {
			systemEl.select(creditPointConfig.getCreditPointSystem().getKey().toString(), true);
		}
		systemEl.setEnabled(editable && !managedCP);
		systemEl.setMandatory(true);
		
		String points = creditPointConfig.getCreditPoints() == null ? null : creditPointConfig.getCreditPoints().toString();
		creditPointEl = uifactory.addTextElement("options.creditpoint.award", 8, points, formLayout);
		creditPointEl.setEnabled(editable && !managedCP);
		creditPointEl.setMandatory(true);

		// Override + Expiration
		SelectionValues overridePK = new SelectionValues();
		String defaultVal = "-";
		String defaultValUnit = "";
		overridePK.add(SelectionValues.entry(DEFAULT_KEY, translate("options.creditpoint.override.default", defaultVal, defaultValUnit)));
		overridePK.add(SelectionValues.entry(OVERRIDE_KEY, translate("options.creditpoint.override.overwrite")));
		overrideExpirationEl = uifactory.addRadiosHorizontal("options.creditpoint.override.expiration", "options.creditpoint.override.expiration",
				formLayout, overridePK.keys(), overridePK.values());
		overrideExpirationEl.addActionListener(FormEvent.ONCLICK);
		overrideExpirationEl.setEnabled(editable && !managedCP);

		String exp = "";
		if(creditPointConfig != null && creditPointConfig.getExpiration() != null) {
			exp = creditPointConfig.getExpiration().toString();
			overrideExpirationEl.select(OVERRIDE_KEY, true);
		} else {
			overrideExpirationEl.select(DEFAULT_KEY, true);
		}
		expirationEl = new ExpirationFormItem("options.creditpoint.expiration", getTranslator());
		expirationEl.setEnabled(editable && !managedCP);
		expirationEl.setLabel("options.creditpoint.expiration", null);
		expirationEl.setValue(exp);
		formLayout.add(expirationEl);
		
		if(editable) {
			FormLayoutContainer buttonCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
			uifactory.addFormSubmitButton("save", buttonCont);
		}
	}
	
	private void updateUI() {
		boolean enabled = enabledEl.isOn();
		
		systemEl.setVisible(enabled);
		creditPointEl.setVisible(enabled);
		overrideExpirationEl.setVisible(enabled);
		
		String overrideExpiration = overrideExpirationEl.isOneSelected()
				? overrideExpirationEl.getSelectedKey()
				: DEFAULT_KEY;
		boolean defaultExpiration = DEFAULT_KEY.equals(overrideExpiration);
		expirationEl.setVisible(enabled && !defaultExpiration);
	}
	
	private void updateOverrideExpirationUI() {
		CreditPointSystem system = getSelectedSystem();
		SelectionValues overridePK = getOverrideExpirationPK(system);
		String overrideExpiration = overrideExpirationEl.isOneSelected()
				? overrideExpirationEl.getSelectedKey()
				: DEFAULT_KEY;
		overrideExpirationEl.setKeysAndValues(overridePK.keys(), overridePK.values(), null);
		overrideExpirationEl.select(overrideExpiration, true);
		
		boolean defaultExpiration = DEFAULT_KEY.equals(overrideExpiration);
		expirationEl.setVisible(enabledEl.isOn() && !defaultExpiration);
	}
	
	private SelectionValues getOverrideExpirationPK(CreditPointSystem system) {
		SelectionValues overridePK = new SelectionValues();

		String defaultVal;
		if(system != null && system.getDefaultExpiration() != null) {
			Integer val = system.getDefaultExpiration();
			String unit = system.getDefaultExpirationUnit() == null
					? ""
					: translate(system.getDefaultExpirationUnit().i18n(val));
			defaultVal = translate("options.creditpoint.override.default", val.toString(), unit);
		} else {
			defaultVal = translate("options.creditpoint.override.no.default");
		}
		overridePK.add(SelectionValues.entry(DEFAULT_KEY, defaultVal));
		overridePK.add(SelectionValues.entry(OVERRIDE_KEY, translate("options.creditpoint.override.overwrite")));
		return overridePK;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		systemEl.clearError();
		creditPointEl.clearError();
		overrideExpirationEl.clearError();
		if(enabledEl.isOn()) {
			
			if(!systemEl.isOneSelected()) {
				systemEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
			
			if(StringHelper.containsNonWhitespace(creditPointEl.getValue())) {
				try {
					int val = Integer.valueOf(creditPointEl.getValue());
					if(val <= 0) {
						creditPointEl.setErrorKey("form.error.positive.integer");
						allOk &= false;
					}
				} catch (NumberFormatException e) {
					creditPointEl.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			} else {
				creditPointEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
			
			if(!overrideExpirationEl.isOneSelected()) {
				overrideExpirationEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enabledEl == source) {
			updateUI();
		} else if(overrideExpirationEl == source) {
			updateOverrideExpirationUI();
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		creditPointConfig = creditPointService.getOrCreateConfiguration(entry);
		creditPointConfig.setEnabled(enabledEl.isOn());
		if (enabledEl.isOn()) {
			CreditPointSystem system = getSelectedSystem();
			creditPointConfig.setCreditPointSystem(system);
			
			BigDecimal points = new BigDecimal(creditPointEl.getValue());
			creditPointConfig.setCreditPoints(points);
			
			if(DEFAULT_KEY.equals(overrideExpirationEl.getSelectedKey())) {
				creditPointConfig.setExpiration(null);
				creditPointConfig.setExpirationType(CreditPointExpirationType.DEFAULT);
			} else {
				creditPointConfig.setExpiration(expirationEl.getValue());
				creditPointConfig.setExpirationType(expirationEl.getType());
			}
		} else {
			creditPointConfig.setCreditPointSystem(null);
			creditPointConfig.setCreditPoints(null);
		}
		
		creditPointConfig = creditPointService.updateConfiguration(creditPointConfig);
		
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
