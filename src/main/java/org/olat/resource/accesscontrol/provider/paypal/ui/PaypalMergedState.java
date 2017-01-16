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
