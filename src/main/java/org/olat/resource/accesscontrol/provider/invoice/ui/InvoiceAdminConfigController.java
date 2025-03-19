/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.provider.invoice.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.provider.invoice.InvoiceModule;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InvoiceAdminConfigController extends FormBasicController {

	private TextElement deadlineDaysEl;
	private List<TextElement> cancellingFeeDefaultEls;
	
	@Autowired
	private InvoiceModule invoiceModule;
	
	public InvoiceAdminConfigController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("config.defaults");
		
		String deadlineDaysDefault = invoiceModule.getCancellingFeeDeadlineDaysDefault()!= null
				? String.valueOf(invoiceModule.getCancellingFeeDeadlineDaysDefault())
				: null;
		deadlineDaysEl = uifactory.addTextElement("config.cancelling.fee.deadline.days.default", 10, deadlineDaysDefault, formLayout);
		
		List<String> currencies = invoiceModule.getCurrencies();
		cancellingFeeDefaultEls = new ArrayList<>(currencies.size());
		for (String currency : currencies) {
			BigDecimal feeDefault = invoiceModule.getCancellingFeeDefaults().get(currency);
			String feeDefaultValue = feeDefault != null ? PriceFormat.formatMoneyForTextInput(feeDefault) : null;
			TextElement cancellingFeeEl = uifactory.addTextElement("fee_" + currency, null, 10, feeDefaultValue, formLayout);
			cancellingFeeEl.setLabel("config.cancelling.fee.default", new String[] { StringHelper.escapeHtml(currency) });
			cancellingFeeEl.setUserObject(currency);
			cancellingFeeDefaultEls.add(cancellingFeeEl);
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		deadlineDaysEl.clearError();
		if (StringHelper.containsNonWhitespace(deadlineDaysEl.getValue())
				&& (!StringHelper.isLong(deadlineDaysEl.getValue()) || Long.parseLong(deadlineDaysEl.getValue()) < 1)) {
			deadlineDaysEl.setErrorKey("form.error.nointeger");
			allOk &= false;
		}
		
		for (TextElement cancellingFeeEl : cancellingFeeDefaultEls) {
			cancellingFeeEl.clearError();
			if (StringHelper.containsNonWhitespace(cancellingFeeEl.getValue())) {
				try {
					if (Double.valueOf(cancellingFeeEl.getValue()) < 0) {
						cancellingFeeEl.setErrorKey("form.error.nofloat");
						allOk &= false;
					}
				} catch (Exception e) {
					cancellingFeeEl.setErrorKey("form.error.nofloat");
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Map<String, BigDecimal> cancellingFeeDefaults = new HashMap<>();
		for (TextElement cancellingFeeEl : cancellingFeeDefaultEls) {
			if (StringHelper.containsNonWhitespace(cancellingFeeEl.getValue())) {
				cancellingFeeDefaults.put((String)cancellingFeeEl.getUserObject(), new BigDecimal(cancellingFeeEl.getValue()));
			}
		}
		invoiceModule.setCancellingFeeDefaults(cancellingFeeDefaults);
		
		Integer cancellingFeeDeadlineDaysDefault = null;
		if (StringHelper.containsNonWhitespace(deadlineDaysEl.getValue())) {
			cancellingFeeDeadlineDaysDefault = Integer.valueOf(deadlineDaysEl.getValue());
		}
		invoiceModule.setCancellingFeeDeadlineDaysDefault(cancellingFeeDeadlineDaysDefault);
	}

}
