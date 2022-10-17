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
package org.olat.resource.accesscontrol.provider.paypal.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessTransaction;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderLine;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACOrderDAO;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.manager.ACTransactionDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessTransactionStatus;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.provider.paypal.PaypalModule;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalAccessMethod;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransaction;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paypal.svcs.services.AdaptivePaymentsService;
import com.paypal.svcs.types.ap.ConvertCurrencyRequest;
import com.paypal.svcs.types.ap.ConvertCurrencyResponse;
import com.paypal.svcs.types.ap.CurrencyCodeList;
import com.paypal.svcs.types.ap.CurrencyConversionList;
import com.paypal.svcs.types.ap.CurrencyList;
import com.paypal.svcs.types.ap.PayRequest;
import com.paypal.svcs.types.ap.PayResponse;
import com.paypal.svcs.types.ap.PaymentDetailsRequest;
import com.paypal.svcs.types.ap.PaymentDetailsResponse;
import com.paypal.svcs.types.ap.Receiver;
import com.paypal.svcs.types.ap.ReceiverList;
import com.paypal.svcs.types.common.AckCode;
import com.paypal.svcs.types.common.ClientDetailsType;
import com.paypal.svcs.types.common.CurrencyType;
import com.paypal.svcs.types.common.DetailLevelCode;
import com.paypal.svcs.types.common.RequestEnvelope;
import com.paypal.svcs.types.common.ResponseEnvelope;


/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  23 mai 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */

@Service("paypalManager")
public class PaypalManagerImpl  implements PaypalManager {
	
	private static final Logger log = Tracing.createLoggerFor(PaypalManagerImpl.class);
	
	private static final String X_PAYPAL_SECURITY_USERID = "acct1.UserName";
	private static final String X_PAYPAL_SECURITY_CREDENTIAL = "acct1.Password";
	private static final String X_PAYPAL_SECURITY_SIGNATURE = "acct1.Signature";
	private static final String X_PAYPAL_APPLICATION_ID = "acct1.AppId";
	private static final String X_PAYPAL_SANDBOX_EMAIL_ADDRESS = "sandbox.EmailAddress";
	private static final String X_PAYPAL_DEVICE_IPADDRESS = "X-PAYPAL-DEVICE-IPADDRESS";
	
	private static final String API_BASE_ENDPOINT = "service.EndPoint.AdaptivePayments";
	
	private static final String USE_PROXY = "http.UseProxy";
	private static final String PROXY_HOST = "http.ProxyHost";
	private static final String PROXY_PORT = "http.ProxyPort";
	
	private static final String SANDBOX_API_BASE_ENDPOINT = "https://svcs.sandbox.paypal.com";
	private static final String LIFE_API_BASE_ENDPOINT = "https://svcs.paypal.com";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACOrderDAO orderManager;
	@Autowired
	private ACService acService;
	@Autowired
	private ACTransactionDAO transactionManager;
	@Autowired
	private PaypalModule paypalModule;
	@Autowired
	private ACReservationDAO reservationDao;
	
	private Properties getAccountProperties() {
		boolean sandboxed = paypalModule.isSandbox();
		
		Properties accountProps = new Properties();
		accountProps.setProperty(X_PAYPAL_SECURITY_USERID, paypalModule.getPaypalSecurityUserId());
		accountProps.setProperty(X_PAYPAL_SECURITY_CREDENTIAL, paypalModule.getPaypalSecurityPassword());
		accountProps.setProperty(X_PAYPAL_SECURITY_SIGNATURE, paypalModule.getPaypalSecuritySignature());
		accountProps.setProperty(X_PAYPAL_APPLICATION_ID, paypalModule.getPaypalApplicationId());
		
		try {
			accountProps.setProperty(X_PAYPAL_DEVICE_IPADDRESS, paypalModule.getDeviceIpAddress());
		} catch (Exception e) {
			log.error("Cannot resolve the ip address from the server domain name", e);
		}
		
		if(sandboxed) {
			accountProps.setProperty(X_PAYPAL_SANDBOX_EMAIL_ADDRESS, paypalModule.getPaypalSandboxEmailAddress());
			accountProps.setProperty(API_BASE_ENDPOINT, SANDBOX_API_BASE_ENDPOINT);
		} else {
			accountProps.setProperty(API_BASE_ENDPOINT, LIFE_API_BASE_ENDPOINT);
		}

		accountProps.setProperty(USE_PROXY, paypalModule.isUseProxy() ? "TRUE" : "FALSE");
		accountProps.setProperty(PROXY_HOST, "");
		accountProps.setProperty(PROXY_PORT, "8080");
		return accountProps;
	}
	
	private ClientDetailsType getAppDetails() {
		ClientDetailsType cl = new ClientDetailsType();
		cl.setDeviceId(Settings.getNodeInfo());
		cl.setIpAddress("127.0.0.1");
		cl.setApplicationId(paypalModule.getPaypalApplicationId());
		return cl;
	}

	private RequestEnvelope getAppRequestEnvelope() {
		RequestEnvelope en = new RequestEnvelope();
		en.setDetailLevel(DetailLevelCode.RETURNALL);
		en.setErrorLanguage("en");
		return en;
	}
	
	private void save(PaypalAccessMethod accessMethod) {
		if(accessMethod.getKey() == null) {
			dbInstance.saveObject(accessMethod);
		} else {
			dbInstance.updateObject(accessMethod);
		}
	}
	
	private PaypalAccessMethod getMethodSecure(Long key) {
		PaypalAccessMethod smethod = null;
		List<PaypalAccessMethod> methods = getPaypalMethods();
		if(!methods.isEmpty()) {
			smethod = methods.get(0);
		} else {
			smethod = new PaypalAccessMethod();
			smethod.setCreationDate(new Date());
			smethod.setLastModified(smethod.getCreationDate());
			save(smethod);
		}
		for(PaypalAccessMethod method:methods) {
			if(key != null && key.equals(method.getKey())) {
				smethod = method;
			}
		}
		return smethod;
	}

	private List<PaypalAccessMethod> getPaypalMethods() {
		StringBuilder sb = new StringBuilder();
		sb.append("select method from ").append(PaypalAccessMethod.class.getName()).append(" method");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PaypalAccessMethod.class)
				.getResultList();
	}
	
	@Override
	public PaypalTransaction loadTransactionByUUID(String uuid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select trx from ").append(PaypalTransaction.class.getName()).append(" as trx where ");
		sb.append("(trx.secureCancelUUID=:uuid or trx.secureSuccessUUID=:uuid)");
		
		TypedQuery<PaypalTransaction> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), PaypalTransaction.class);
		if(StringHelper.containsNonWhitespace(uuid)) {
			query.setParameter("uuid", uuid);
		}
		List<PaypalTransaction> transactions = query.getResultList();
		if(transactions.isEmpty()) return null;
		return transactions.get(0);
	}
	
	public PaypalTransaction loadTransactionByInvoiceId(String invoiceId) {
		StringBuilder sb = new StringBuilder();
		sb.append("select trx from ").append(PaypalTransaction.class.getName()).append(" as trx where ");
		sb.append("trx.refNo=:invoiceId");
		
		List<PaypalTransaction> transactions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PaypalTransaction.class)
				.setParameter("invoiceId", invoiceId)
				.getResultList();
		if(transactions.isEmpty()) return null;
		return transactions.get(0);
	}
	
	@Override
	public PaypalTransaction loadTransaction(Order order, OrderPart part) {
		StringBuilder sb = new StringBuilder();
		sb.append("select trx from ").append(PaypalTransaction.class.getName()).append(" as trx where ");
		sb.append("(trx.orderId=:orderId and trx.orderPartId=:orderPartId)");
		
		List<PaypalTransaction> transactions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PaypalTransaction.class)
				.setParameter("orderId", order.getKey())
				.setParameter("orderPartId", part.getKey())
				.getResultList();
		if(transactions.isEmpty()) return null;
		return transactions.get(0);
	}
	
	@Override
	public List<PSPTransaction> loadTransactions(List<Order> orders) {
		if(orders == null || orders.isEmpty()) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select trx from ").append(PaypalTransaction.class.getName()).append(" as trx where ");
		sb.append("trx.orderId in (:orderIds)");
		
		List<Long> orderIds = new ArrayList<>(orders.size());
		for(Order order:orders) {
			orderIds.add(order.getKey());
		}
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PSPTransaction.class)
				.setParameter("orderIds", orderIds)
				.getResultList();
	}

	@Override
	public List<PaypalTransaction> findTransactions(String transactionId) {
		StringBuilder sb = new StringBuilder();
		sb.append("select trx from ").append(PaypalTransaction.class.getName()).append(" as trx ");
		
		boolean where = false;
		if(StringHelper.containsNonWhitespace(transactionId)) {
			where = appendAnd(sb, where);
			sb.append(" (trx.transactionId=:transactionId or trx.senderTransactionId=:transactionId or trx.refNo=:transactionId) ");
		}
		sb.append(" order by trx.payResponseDate asc");
		
		TypedQuery<PaypalTransaction> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), PaypalTransaction.class);
		if(StringHelper.containsNonWhitespace(transactionId)) {
			query.setParameter("transactionId", transactionId);
		}

		return query.getResultList();
	}
	
	private boolean appendAnd(StringBuilder sb, boolean where) {
		if(where) sb.append(" and ");
		else sb.append(" where ");
		return true;
	}

	private void updateTransaction(PayResponse payResp, PaypalTransaction trx) {
		trx.setPayKey(payResp.getPayKey());
		trx.setPaymentExecStatus(payResp.getPaymentExecStatus());
		
		ResponseEnvelope resEnv = payResp.getResponseEnvelope();
		AckCode ack = resEnv.getAck();
		trx.setAck(ack == null ? null : ack.getValue());
		trx.setBuild(resEnv.getBuild());
		trx.setCoorelationId(resEnv.getCorrelationId());
		trx.setPayResponseDate(new Date());

		dbInstance.updateObject(trx);
	}
	
	@Override
	public void updateTransaction(String uuid) {
		PaypalTransaction trx = loadTransactionByUUID(uuid);
		
		if(uuid.equals(trx.getSecureSuccessUUID())) {
			log.info(Tracing.M_AUDIT, "Paypal transaction success: " + trx);
			completeTransaction(trx, null);
		} else if (uuid.equals(trx.getSecureCancelUUID())) {
			//cancel -> return to the access panel
			log.info(Tracing.M_AUDIT, "Paypal transaction canceled by user: " + trx);
			cancelTransaction(trx);
		}
	}
	
	@Override
	public void updateTransactionByNotification(Map<String,String> values, boolean verified) {
		if(verified) {
			
			//CREATED - The payment request was received; funds will be transferred
			//COMPLETED - The payment was successful
			//INCOMPLETE - Some transfers succeeded and some failed for a parallel payment or, for a delayed chained payment, secondary receivers have not been paid
			//ERROR - The payment failed and all attempted transfers failed or all
			//REVERSALERROR - One or more transfers failed when attempting to
			//PROCESSING - The payment is in progress
			//PENDING - The payment is awaiting processing
			//String status = values.get("status");
			//if("COMPLETED".equals(status))
			String invoiceId = values.get("transaction[0].invoiceId");
			PaypalTransaction trx = loadTransactionByInvoiceId(invoiceId);
			if(trx != null) {
				completeTransaction(trx, values);
			} else {
				log.error("Paypal IPN Transaction not found: " + values);
			}
		} else {
			String invoiceId = values.get("transaction[0].invoiceId");
			log.error("Paypal IPN Transaction not verified: " + invoiceId + " raw values: " + values);
		}
	}
	
	private synchronized void cancelTransaction(PaypalTransaction trx) {
		if(trx.getStatus() == PaypalTransactionStatus.SUCCESS || trx.getStatus() == PaypalTransactionStatus.CANCELED) {
			//already completed: if successed -> let it in this state
			return;
		}
		
		updateTransaction(trx, PaypalTransactionStatus.CANCELED);
		Order order = orderManager.loadOrderByNr(trx.getRefNo());
		orderManager.save(order, OrderStatus.CANCELED);
		
		//cancel the reservations
		Identity identity = order.getDelivery();
		for(OrderPart part:order.getParts()) {
			if(part.getKey().equals(trx.getOrderPartId())) {
				for(OrderLine line:part.getOrderLines()) {
					OLATResource resource = line.getOffer().getResource();
					ResourceReservation reservation = acService.getReservation(identity, resource);
					if(reservation != null) {
						acService.removeReservation(identity, identity, reservation);
						log.info(Tracing.M_AUDIT, "Remove reservation after cancellation for: " + reservation + " to " + identity);
					}
				}
			}
		}
	}
	
	/**
	 * Success -> save order and authorize the access
	 * Pending -> save order and authorize the access
	 * Denied  -> save order and revert authorization to the access
	 * @param trx
	 */
	private synchronized void completeTransaction(PaypalTransaction trx, Map<String,String> values) {
		//access already authorized
		if(trx.getStatus() == PaypalTransactionStatus.SUCCESS || trx.getStatus() == PaypalTransactionStatus.PENDING) {
			if(appendInfos(trx, values)) {
				dbInstance.updateObject(trx);
			}
			
			//check if the status changes
			String trxStatus = trx.getTransactionStatus();
			if(trxStatus.equalsIgnoreCase("DENIED")) {
				completeDeniedTransaction(trx);
			} else {
				return;//already completed
			}
		}
		
		appendInfos(trx, values);
		
		//SUCCESS – The sender’s transaction has completed
		//PENDING – The transaction is awaiting further processing
		//CREATED – The payment request was received; funds will be transferred
		//PARTIALLY_REFUNDED– Transaction was partially refunded
		//DENIED – The transaction was rejected by the receiver
		//PROCESSING – The transaction is in progress
		//REVERSED – The payment was returned to the sender
		//null, Success, Pending -> authorize
		String trxStatus = trx.getTransactionStatus();
		if(trxStatus != null && "DENIED".equalsIgnoreCase(trxStatus)) {
			completeDeniedTransaction(trx);
		} else {
			completeTransactionSucessfully(trx, trxStatus);
		}
	}
	
	private void completeDeniedTransaction(PaypalTransaction trx) {
		updateTransaction(trx, PaypalTransactionStatus.DENIED);
		Order order = orderManager.loadOrderByNr(trx.getRefNo());
		order = orderManager.save(order, OrderStatus.ERROR);
		
		PaypalAccessMethod method = getMethodSecure(trx.getMethodId());
		if(order.getKey().equals(trx.getOrderId())) {
			//make accessible
			Identity identity = order.getDelivery();
			for(OrderPart part:order.getParts()) {
				if(part.getKey().equals(trx.getOrderPartId())) {
					AccessTransaction transaction = transactionManager.createTransaction(order, part, method);
					transaction = transactionManager.update(transaction, AccessTransactionStatus.ERROR);
					for(OrderLine line:part.getOrderLines()) {
						acService.denyAccesToResource(identity, line.getOffer());
						log.info(Tracing.M_AUDIT, "Paypal payed access revoked for: " + buildLogMessage(line, method) + " to " + identity);

						ResourceReservation reservation = reservationDao.loadReservation(identity, line.getOffer().getResource());
						if(reservation != null) {
							acService.removeReservation(identity, identity, reservation);
							log.info(Tracing.M_AUDIT, "Remove reservation after cancellation for: " + reservation + " to " + identity);
						}
					}
				}
			}
		} else {
			log.error("Order not in sync with PaypalTransaction");
		}
	}
	
	private void completeTransactionSucessfully(PaypalTransaction trx, String trxStatus) {
		Order order = orderManager.loadOrderByNr(trx.getRefNo());
		if("PENDING".equalsIgnoreCase(trxStatus)) {
			updateTransaction(trx, PaypalTransactionStatus.PENDING);
		} else {
			updateTransaction(trx, PaypalTransactionStatus.SUCCESS);
		}
		order = orderManager.save(order, OrderStatus.PAYED);

		PaypalAccessMethod method = getMethodSecure(trx.getMethodId());
		if(order.getKey().equals(trx.getOrderId())) {
			//make accessible
			Identity identity = order.getDelivery();
			for(OrderPart part:order.getParts()) {
				if(part.getKey().equals(trx.getOrderPartId())) {
					AccessTransaction transaction = transactionManager.createTransaction(order, part, method);
					transaction = transactionManager.save(transaction);
					for(OrderLine line:part.getOrderLines()) {
						if(acService.allowAccesToResource(identity, line.getOffer())) {
							log.info(Tracing.M_AUDIT, "Paypal payed access granted for: " + buildLogMessage(line, method) + " to " + identity);
							transaction = transactionManager.update(transaction, AccessTransactionStatus.SUCCESS);
						} else {
							log.error("Paypal payed access refused for: " + buildLogMessage(line, method) + " to " + identity);
							transaction = transactionManager.update(transaction, AccessTransactionStatus.ERROR);
						}
					}
				}
			}
		} else {
			log.error("Order not in sync with PaypalTransaction");
		}
	}
	
	private String buildLogMessage(OrderLine line, PaypalAccessMethod method) {
		StringBuilder sb = new StringBuilder();
		Offer offer = line.getOffer();
		sb.append("OrderLine[key=").append(line.getKey()).append("]")
			.append("[method=").append(method.getClass().getSimpleName()).append("]");
		if(offer == null) {
			sb.append("[resource=null]");
		} else {
			sb.append("[resource=").append(offer.getResourceId()).append(":").append(offer.getResourceTypeName()).append(":").append(offer.getResourceDisplayName()).append("]");
		}
		return sb.toString();
	}
	
	private boolean appendInfos(PaypalTransaction trx, Map<String,String> values) {
		if(values == null) return false;

		boolean append = false;
		String senderTrxId = values.get("transaction[0].id_for_sender_txn");
		if(StringHelper.containsNonWhitespace(senderTrxId)) {
			trx.setSenderTransactionId(senderTrxId);
			append = true;
		}

		String statusSenderTrx = values.get("transaction[0].status_for_sender_txn");
		if(StringHelper.containsNonWhitespace(statusSenderTrx)) {
			trx.setSenderTransactionStatus(statusSenderTrx);
			append = true;
		}

		String receiverTrxId = values.get("transaction[0].id");
		if(StringHelper.containsNonWhitespace(receiverTrxId)) {
			trx.setTransactionId(receiverTrxId);
			append = true;
		}
		
		String senderEmail = values.get("sender_email");
		if(StringHelper.containsNonWhitespace(senderEmail)) {
			trx.setSenderEmail(senderEmail);
			append = true;
		}
		
		String verifySign = values.get("verify_sign");
		if(StringHelper.containsNonWhitespace(verifySign)) {
			trx.setVerifySign(verifySign);
			append = true;
		}
		
		String pendingReason = values.get("transaction[0].pending_reason");
		if(StringHelper.containsNonWhitespace(pendingReason)) {
			trx.setPendingReason(pendingReason);
			append = true;
		}
		
		String transactionStatus = values.get("transaction[0].status");
		if(StringHelper.containsNonWhitespace(transactionStatus)) {
			trx.setTransactionStatus(transactionStatus);
			append = true;
		}

		return append;
	}

	@Override
	public void updateTransaction(PaypalTransaction transaction, PaypalTransactionStatus status) {
		transaction.setStatus(status);
		dbInstance.updateObject(transaction);
	}

	/*
	 * @return
	 */
	private PaypalTransaction createAndPersistTransaction(Price amount, Order order, OrderPart part, AccessMethod method) {
		PaypalTransaction transaction = new PaypalTransaction();
		transaction.setRefNo(order.getOrderNr());
		transaction.setSecureSuccessUUID(UUID.randomUUID().toString().replace("-", ""));
		transaction.setSecureCancelUUID(UUID.randomUUID().toString().replace("-", ""));
		transaction.setStatus(PaypalTransactionStatus.NEW);
		transaction.setOrderId(order.getKey());
		transaction.setOrderPartId(part.getKey());
		transaction.setMethodId(method.getKey());
		transaction.setSecurePrice(amount);
		dbInstance.saveObject(transaction);
		return transaction;
	}

	@Override
	public boolean convertCurrency() {
		try {
			String[] fromcodes = new String[]{"CHF"};
			String[] tocodes = new String[]{"USD"};
			BigDecimal[] amountItems = new BigDecimal[]{ new BigDecimal("20.00")};
			CurrencyList list = new CurrencyList();

			CurrencyCodeList cclist = new CurrencyCodeList();
			for (int i = 0; i < amountItems.length; i++) {
				CurrencyType ct = new CurrencyType();
				ct.setAmount(amountItems[i].doubleValue());
				ct.setCode(fromcodes[i]);
				list.getCurrency().add(ct);

			}
			for (int i = 0; i < tocodes.length; i++) {
				cclist.getCurrencyCode().add(tocodes[i]);

			}
			
			ConvertCurrencyRequest req = new ConvertCurrencyRequest();
			req.setBaseAmountList(list);
			req.setConvertToCurrencyList(cclist);
			req.setRequestEnvelope(getAppRequestEnvelope());
			
			AdaptivePaymentsService ap = new AdaptivePaymentsService(getAccountProperties());
			ConvertCurrencyResponse resp = ap.convertCurrency(req);

			for (Iterator<CurrencyConversionList> iterator = resp.getEstimatedAmountTable().getCurrencyConversionList().iterator(); iterator.hasNext();) {
				CurrencyConversionList ccclist = iterator.next();
				log.info(ccclist.getBaseAmount().getCode() + " :: "+ ccclist.getBaseAmount().getAmount());

				List<CurrencyType> l = ccclist.getCurrencyList().getCurrency();
				for (int i = 0; i < l.size(); i++) {
					CurrencyType ct = l.get(i);
					log.info(ct.getCode() + " :: "+ ct.getAmount());
				}
			}
			return true;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	@Override
	public String getPayRedirectUrl(PayResponse response) {
		String testEnv = "";
		if(paypalModule.isSandbox()) {
			testEnv = "sandbox.";
		}
		
		String payKey = response.getPayKey();
		return "https://www." + testEnv + "paypal.com/cgi-bin/webscr?cmd=_ap-payment&paykey=" + payKey;
	}
	
	@Override
	public String getIpnVerificationUrl() {
		String testEnv = "";
		if(paypalModule.isSandbox()) {
			testEnv = "sandbox.";
		}
		return "https://www." + testEnv + "paypal.com/cgi-bin/webscr";
	}
	
	@Override
	public PaymentDetailsResponse paymentDetails(String key) {
		try {
			PaymentDetailsRequest paydetailReq = new PaymentDetailsRequest();
			paydetailReq.setPayKey(key);
			paydetailReq.setRequestEnvelope(getAppRequestEnvelope());
			AdaptivePaymentsService apd = new AdaptivePaymentsService(getAccountProperties());
			PaymentDetailsResponse paydetailsResp = apd.paymentDetails(paydetailReq);
			return paydetailsResp;
		} catch (Exception fe) {
			log.error("", fe);
			return null;
		}
	}

	@Override
	public PayResponse request(Identity delivery, OfferAccess offerAccess, String mapperUri, String sessionId) {
		StringBuilder url = new StringBuilder();
		url.append(Settings.createServerURI()).append(mapperUri);
		
		Offer offer = offerAccess.getOffer();
		Price amount = offer.getPrice();

		Order order = orderManager.saveOneClick(delivery, offerAccess, OrderStatus.PREPAYMENT);
		PaypalTransaction trx = createAndPersistTransaction(amount, order, order.getParts().get(0), offerAccess.getMethod());

		//!!!! make a trace of the process
		dbInstance.commit();

		ReceiverList list = new ReceiverList();
		Receiver rec1 = new Receiver();
		rec1.setAmount(amount.getAmount().doubleValue());
		rec1.setEmail(paypalModule.getPaypalFirstReceiverEmailAddress());
		rec1.setInvoiceId(order.getOrderNr());
		list.getReceiver().add(rec1);
		
		String returnURL = url.toString() + "/" + trx.getSecureSuccessUUID() + ".html;jsessionid=" + sessionId + "?status=success";
		String cancelURL = url.toString() + "/" + trx.getSecureCancelUUID() + ".html;jsessionid=" + sessionId + "?status=cancel";

		PayRequest payRequest = new PayRequest();
		payRequest.setCancelUrl(cancelURL);
		payRequest.setReturnUrl(returnURL);
		payRequest.setTrackingId(order.getOrderNr());
		payRequest.setCurrencyCode(amount.getCurrencyCode());
		payRequest.setClientDetails(getAppDetails());
		payRequest.setReceiverList(list);
		payRequest.setRequestEnvelope(getAppRequestEnvelope());
		payRequest.setActionType("PAY");
		payRequest.setIpnNotificationUrl(Settings.getServerContextPathURI() + "/paypal/ipn");

		PayResponse payResp = null;
		try {
			AdaptivePaymentsService ap = new AdaptivePaymentsService(getAccountProperties());
			payResp = ap.pay(payRequest);
			log.info(Tracing.M_AUDIT, "Paypal send PayRequest: " + (payResp == null ? "no response" : payResp.getPayKey() + "/" + payResp.getPaymentExecStatus()));
			return payResp;
		} catch (Exception e) {
			log.error("Paypal error", e);
		} finally {
			if(payResp == null) {
				updateTransaction(trx, PaypalTransactionStatus.ERROR);
			} else {
				updateTransaction(payResp, trx);
			}
		}
		return null;
	}
}
