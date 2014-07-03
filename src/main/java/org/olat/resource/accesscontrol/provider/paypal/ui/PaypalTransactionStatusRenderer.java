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

import java.util.Locale;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransaction;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransactionStatus;

/**
 * 
 * Description:<br>
 * Render an icon or message for the status of a paypal transaction
 * 
 * <P>
 * Initial Date:  30 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PaypalTransactionStatusRenderer  implements CustomCellRenderer {

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if(val instanceof PaypalTransaction) {
			PaypalTransaction trx = (PaypalTransaction)val;
			String ack = trx.getAck();
			String execStatus = trx.getPaymentExecStatus();
			
			PaypalTransactionStatus status = trx.getStatus();
			String trxStatus = trx.getTransactionStatus();
			
			if("Success".equals(ack) && "CREATED".equals(execStatus)) {
				
				// – The sender’s transaction has completed
				//PENDING – The transaction is awaiting further processing
				//CREATED – The payment request was received; funds will be transferred
				//PARTIALLY_REFUNDED– Transaction was partially refunded
				//DENIED – The transaction was rejected by the receiver
				//PROCESSING – The transaction is in progress
				//REVERSED – The payment was returned to the sender
				
				if(trxStatus == null) {
					if(status == PaypalTransactionStatus.SUCCESS) {
						sb.append("<i class='o_icon o_icon-fw o_ac_status_success_icon'></i>");
					}	else if(status == PaypalTransactionStatus.PENDING) {
						sb.append("<i class='o_icon o_icon-fw o_ac_status_waiting_icon'></i>");
					} else if(status == PaypalTransactionStatus.NEW || status == PaypalTransactionStatus.PREPAYMENT) {
						sb.append("<i class='o_icon o_icon-fw o_ac_status_new_icon'></i>");
					} else if(status == PaypalTransactionStatus.CANCELED) {
						sb.append("<i class='o_icon o_icon-fw o_ac_status_canceled_icon'></i>");
					}	else {
						sb.append("<i class='o_icon o_icon-fw o_ac_status_error_icon'></i>");
					}
				} else if("SUCCESS".equalsIgnoreCase(trxStatus) || "CREATED".equalsIgnoreCase(trxStatus)
						|| "PARTIALLY_REFUNDED".equalsIgnoreCase(trxStatus) || "Completed".equalsIgnoreCase(trxStatus)) {
					sb.append("<i class='o_icon o_icon-fw o_ac_status_success_icon'></i>");
				} else if("PROCESSING".equalsIgnoreCase(trxStatus) || "PENDING".equalsIgnoreCase(trxStatus)) {
					sb.append("<i class='o_icon o_icon-fw o_ac_status_waiting_icon'></i>");
				} else {
					sb.append("<i class='o_icon o_icon-fw o_ac_status_error_icon'></i>");
				}
			} else {
				sb.append("<i class='o_icon o_icon-fw o_ac_status_error_icon'></i>");
			}
		}
	}
}
