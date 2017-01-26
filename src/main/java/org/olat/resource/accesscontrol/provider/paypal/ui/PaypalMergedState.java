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
package org.olat.resource.accesscontrol.provider.paypal.ui;

import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransaction;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransactionStatus;

/**
 * 
 * Initial date: 5 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum PaypalMergedState {
	
	success("o_ac_status_success_icon"),
	waiting("o_ac_status_waiting_icon"),
	created("o_ac_status_new_icon"),
	canceled("o_ac_status_canceled_icon"),
	error("o_ac_status_error_icon");
	
	private final String cssClass;
	
	private PaypalMergedState(String cssClass) {
		this.cssClass = cssClass;
	}
	
	public String cssClass() {
		return cssClass;
	}
	
	public static final boolean isValueOf(String val) {
		if(val == null) return false;
		for(PaypalMergedState value:values()) {
			if(val.equals(value.name())) {
				return true;
			}
		}
		return false;
	}
	
	public static PaypalMergedState value(PaypalTransaction trx) {
		String ack = trx.getAck();
		String execStatus = trx.getPaymentExecStatus();
		
		PaypalTransactionStatus status = trx.getStatus();
		String trxStatus = trx.getTransactionStatus();
		
		PaypalMergedState state;
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
					state = PaypalMergedState.success;
				}	else if(status == PaypalTransactionStatus.PENDING) {
					state = PaypalMergedState.waiting;
				} else if(status == PaypalTransactionStatus.NEW || status == PaypalTransactionStatus.PREPAYMENT) {
					state = PaypalMergedState.created;
				} else if(status == PaypalTransactionStatus.CANCELED) {
					state = PaypalMergedState.canceled;
				}	else {
					state = PaypalMergedState.error;
				}
			} else if("SUCCESS".equalsIgnoreCase(trxStatus) || "CREATED".equalsIgnoreCase(trxStatus)
					|| "PARTIALLY_REFUNDED".equalsIgnoreCase(trxStatus) || "Completed".equalsIgnoreCase(trxStatus)) {
				state = PaypalMergedState.success;
			} else if("PROCESSING".equalsIgnoreCase(trxStatus) || "PENDING".equalsIgnoreCase(trxStatus)) {
				state = PaypalMergedState.waiting;
			} else {
				state = PaypalMergedState.error;
			}
		} else {
			state = PaypalMergedState.error;
		}
		return state;
	}

}
