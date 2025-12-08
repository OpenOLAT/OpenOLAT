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
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.model.CreditPointTransactionAndWallet;
import org.olat.modules.creditpoint.ui.component.ExpirationFormItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AddTransactionController extends FormBasicController {

	private TextElement amountEl;
	private TextElement commentEl;
	private ExpirationFormItem expirationEl;
	
	private CreditPointWallet wallet;
	private final CreditPointSystem system;
	
	@Autowired
	private CreditPointService creditPointService;
	
	public AddTransactionController(UserRequest ureq, WindowControl wControl, CreditPointSystem system, CreditPointWallet wallet) {
		super(ureq, wControl);
		this.system = system;
		this.wallet = wallet;
		initForm(ureq);
	}
	
	public CreditPointWallet getWallet() {
		return wallet;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String systemName = StringHelper.escapeHtml(system.getName());
		uifactory.addStaticTextElement("credit.point.system.name", systemName, formLayout);
		
		amountEl = uifactory.addTextElement("credit.point.amount", 4, "", formLayout);
		amountEl.setMandatory(true);
		
		expirationEl = new ExpirationFormItem("credit.point.validity", true, getTranslator());
		expirationEl.setLabel("credit.point.validity", null);
		expirationEl.setVisible(system.getDefaultExpiration() != null && system.getDefaultExpirationUnit() != null);
		formLayout.add(expirationEl);
		
		commentEl = uifactory.addTextAreaElement("credit.point.comment", "credit.point.comment", 1000, 3, 60, false, false, "", formLayout);
		commentEl.setMandatory(true);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("add.transaction", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateInteger(amountEl, true);

		commentEl.clearError();
		if(!StringHelper.containsNonWhitespace(commentEl.getValue())) {
			commentEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateInteger(TextElement el, boolean mandatory) {
		boolean allOk = true;
		
		el.clearError();
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
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		BigDecimal amount = new BigDecimal(amountEl.getValue());
		// Only positive amount
		if(amount.compareTo(BigDecimal.ZERO) < 0) {
			amount = amount.abs();
		}
		
		Date validity = expirationEl.isVisible()
				? creditPointService.calculateExpirationDate(expirationEl.getValue(), expirationEl.getType(),
						ureq.getRequestTimestamp(), system)
				: null;
		CreditPointTransactionAndWallet trx = creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit,
				amount, validity, commentEl.getValue(), wallet, getIdentity(), null, null, null, null, null);
		wallet = trx.wallet();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
