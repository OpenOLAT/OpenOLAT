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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.creditpoint.CreditPointFormat;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.model.CreditPointTransactionAndWallet;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ConfirmCancelTransactionController extends FormBasicController {
	
	private static final String CONFIRM_KEY = "confirm";
	
	private MultipleSelectionElement confirmationEl;
	
	private CreditPointWallet wallet;
	private final CreditPointSystem system;
	private final CreditPointTransaction transactionToCancel;
	
	@Autowired
	private CreditPointService creditPointService;
	
	public ConfirmCancelTransactionController(UserRequest ureq, WindowControl wControl,
			CreditPointTransaction transactionToCancel, CreditPointWallet wallet, CreditPointSystem system) {
		super(ureq, wControl, "confirm_cancel");
		this.wallet = wallet;
		this.system = system;
		this.transactionToCancel = transactionToCancel;
		
		initForm(ureq);
	}
	
	public CreditPointWallet getWallet() {
		return wallet;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer defCont = uifactory.addDefaultFormLayout("content", null, formLayout);

		if(formLayout instanceof FormLayoutContainer layoutCont) {
			SelectionValues confirmationPK = new SelectionValues();
			if(transactionToCancel.getTransactionType() == CreditPointTransactionType.deposit
					&& transactionToCancel.getAmount().compareTo(wallet.getBalance()) > 0) {
				
				String[] args = new String[] {
						transactionToCancel.getOrderNumber().toString(),
						StringHelper.escapeHtml(CreditPointFormat.format(transactionToCancel.getAmount(), system)),
						transactionToCancel.getRemainingAmount().subtract(wallet.getBalance()).toString(),
						wallet.getBalance().toString()
					};
				
				confirmationPK.add(SelectionValues.entry(CONFIRM_KEY, translate("confirm.cancel.transaction.confirmation.not.enough", args)));
				layoutCont.contextPut("infosMsg", translate("confirm.cancel.transaction", args));
				layoutCont.contextPut("warningMsg", translate("confirm.cancel.transaction.not.enough.warning", args));
			} else {
				String[] args = new String[] {
						transactionToCancel.getOrderNumber().toString(),
						StringHelper.escapeHtml(CreditPointFormat.format(transactionToCancel.getAmount(), wallet.getCreditPointSystem()))
					};
				confirmationPK.add(SelectionValues.entry(CONFIRM_KEY, translate("confirm.cancel.transaction.confirmation", args)));
				layoutCont.contextPut("infosMsg", translate("confirm.cancel.transaction"));
			}
			
			confirmationEl = uifactory.addCheckboxesHorizontal("confirmation", "confirmation", defCont,
					confirmationPK.keys(), confirmationPK.values());
			confirmationEl.setEscapeHtml(false);
		}
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, defCont);
		uifactory.addFormSubmitButton("reverse", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		confirmationEl.clearError();
		if(!confirmationEl.isAtLeastSelected(1)) {
			confirmationEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		CreditPointTransactionAndWallet result = creditPointService.cancelCreditPointTransaction(wallet, transactionToCancel, getIdentity());
		wallet = result.wallet();	
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
