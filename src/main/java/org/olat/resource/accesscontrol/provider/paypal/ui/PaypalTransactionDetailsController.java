/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.resource.accesscontrol.provider.paypal.ui;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransaction;
import org.olat.resource.accesscontrol.ui.FormController;
import org.olat.resource.accesscontrol.ui.PriceFormat;

/**
 * 
 * Description:<br>
 * All the ugly details of a paypal transaction
 * 
 * <P>
 * Initial Date:  27 mai 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PaypalTransactionDetailsController extends FormBasicController implements FormController {


	private final PaypalTransaction transaction;
	
	public PaypalTransactionDetailsController(UserRequest ureq, WindowControl wControl, PaypalTransaction transaction) {
		super(ureq, wControl);
		this.transaction = transaction;
		initForm(ureq);
	}
	
	public PaypalTransactionDetailsController(UserRequest ureq, WindowControl wControl, PaypalTransaction transaction, Form form) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, form);
		this.transaction = transaction;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Formatter format = Formatter.getInstance(getLocale());

		String ack = transaction.getAck();
		uifactory.addStaticTextElement("ack", "paypal.transaction.ack", ack, formLayout);
		
		String execStatus = transaction.getPaymentExecStatus();
		uifactory.addStaticTextElement("exec.status", "paypal.transaction.exec.status", execStatus, formLayout);

		Date respDate = transaction.getPayResponseDate();
		String respDateStr = format.formatDateAndTime(respDate);
		uifactory.addStaticTextElement("resp.date", "paypal.transaction.response.date", respDateStr, formLayout);
		
		Price securePrice = transaction.getSecurePrice();
		String securePriceStr = PriceFormat.fullFormat(securePrice);
		uifactory.addStaticTextElement("amount", "paypal.transaction.amount", securePriceStr, formLayout);
		
		uifactory.addSpacerElement("ipn-spacer", formLayout, false);
		
		String transactionId = transaction.getTransactionId();
		uifactory.addStaticTextElement("trx-id", "paypal.transaction.id", removeNull(transactionId) , formLayout);

		String trxStatus = transaction.getTransactionStatus();
		uifactory.addStaticTextElement("trx-status", "paypal.transaction.status", removeNull(trxStatus), formLayout);
		
		String senderTransactionId = transaction.getSenderTransactionId();
		uifactory.addStaticTextElement("trx-sender-id", "paypal.transaction.sender.id", removeNull(senderTransactionId), formLayout);
		
		String senderTrxStatus = transaction.getSenderTransactionStatus();
		uifactory.addStaticTextElement("trx-sender-status", "paypal.transaction.sender.status", removeNull(senderTrxStatus), formLayout);
		
		String pendingReason = transaction.getPendingReason();
		uifactory.addStaticTextElement("trx-pending-reason", "paypal.transaction.pending.reason", removeNull(pendingReason), formLayout);

		String senderEmail = transaction.getSenderEmail();
		uifactory.addStaticTextElement("trx-sender", "paypal.transaction.sender", removeNull(senderEmail), formLayout);

	}
	
	private String removeNull(String string) {
		return string == null ? "" : string;
	}

	@Override
	public FormItem getInitialFormItem() {
		return flc;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
