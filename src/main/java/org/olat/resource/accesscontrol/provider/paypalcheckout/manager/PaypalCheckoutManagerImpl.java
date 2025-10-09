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
package org.olat.resource.accesscontrol.provider.paypalcheckout.manager;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailPackage;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessTransaction;
import org.olat.resource.accesscontrol.ConfirmationByEnum;
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
import org.olat.resource.accesscontrol.model.AccessTransactionStatus;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutManager;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutModule;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutStatus;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutTransaction;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.CheckoutRequest;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.CreateSmartOrder;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.PaypalCheckoutAccessMethod;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.PaypalCheckoutTransactionWithDelivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paypal.payments.Capture;

/**
 * 
 * Initial date: 23 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PaypalCheckoutManagerImpl implements PaypalCheckoutManager {

	private static final Logger log = Tracing.createLoggerFor(PaypalCheckoutManagerImpl.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private ACOrderDAO orderManager;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private CheckoutV2Provider checkoutProvider;
	@Autowired
	private PaypalCheckoutModule checkoutModule;
	@Autowired
	private ACTransactionDAO transactionManager;
	@Autowired
	private PaypalCheckoutTransactionDAO transactionDao;
	

	@Override
	public String getPreferredLocale(Locale locale) {
		String val;
		List<String> preferredCountries = checkoutModule.getPreferredCountriesList();
		if(preferredCountries.isEmpty()) {
			Locale regionalizedLocale = i18nManager.getRegionalizedLocale(locale);
			val = regionalizedLocale.toString();
		} else {
			String language = locale.getLanguage();
			String country = locale.getCountry();
			if(!StringHelper.containsNonWhitespace(country)) {
				Locale[] allLocales = Locale.getAvailableLocales();
				for(String preferredCountry:preferredCountries) {
					if(exists(language, preferredCountry, allLocales)) {
						country = preferredCountry;
						break;
					}
				}
			}

			if(StringHelper.containsNonWhitespace(country)) {
				val = language + "_" + country.toUpperCase();
			} else {
				Locale regionalizedLocale = i18nManager.getRegionalizedLocale(locale);
				val = regionalizedLocale.toString();
			}
		}
		
		if(!StringHelper.containsNonWhitespace(val)) {
			val = "de_CH";
		}
		return val;
	}
	
	private boolean exists(String language, String country, Locale[] allLocales) {
		for(Locale locale:allLocales) {
			if(locale.getCountry().equals(country) && locale.getLanguage().equals(language)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public CheckoutRequest request(Identity delivery, OfferAccess offerAccess, String mapperUri, String sessionId) {
		StringBuilder url = new StringBuilder();
		url.append(Settings.createServerURI()).append(mapperUri);
		
		Offer offer = offerAccess.getOffer();
		Price amount = offer.getPrice();

		Order order = orderManager.saveOneClick(delivery, offerAccess, OrderStatus.PREPAYMENT, null, null, null);
		PaypalCheckoutTransaction trx = transactionDao.createTransaction(amount, order, order.getParts().get(0), offerAccess.getMethod());
		dbInstance.commit();// secure first step
		return checkoutProvider.paymentUrl(url.toString(), order, trx, sessionId);
	}

	@Override
	public CreateSmartOrder createOrder(Identity delivery, OfferAccess offerAccess) {
		Offer offer = offerAccess.getOffer();
		Price amount = offer.getPrice();
		Date expirationDate = DateUtils.addHours(new Date(), 1);
		if(acService.reserveAccessToResource(delivery, offerAccess.getOffer(), offerAccess.getMethod(),
				ConfirmationByEnum.PAYMENT_PROCESSOR, expirationDate, null, delivery, null)) {
			Order order = orderManager.saveOneClick(delivery, offerAccess, OrderStatus.PREPAYMENT, null, null, null);
			PaypalCheckoutTransaction trx = transactionDao.createTransaction(amount, order, order.getParts().get(0), offerAccess.getMethod());
			trx = checkoutProvider.createOrder(order, trx);
			return new CreateSmartOrder(trx.getPaypalOrderId(), true);
		}
		log.info(Tracing.M_AUDIT, "Can reserve: {}", delivery);
		return new CreateSmartOrder(null, false);
	}
	
	public PaypalCheckoutTransaction approveOrder(PaypalCheckoutTransaction trx) {
		try {
			trx = checkoutProvider.captureOrder(trx);
			if(PaypalCheckoutStatus.COMPLETED.name().equals(trx.getPaypalOrderStatus())) {
				completeTransactionSucessfully(trx);
			} else if(PaypalCheckoutStatus.PENDING.name().equals(trx.getPaypalOrderStatus())) {
				pendingTransaction(trx);
			} else {
				completeDeniedTransaction(trx);
			}
		} catch (IOException e) {
			log.error("", e);
		}
		return trx;
	}

	@Override
	public void updateTransaction(String uuid) {
		PaypalCheckoutTransaction trx = loadTransactionByUUID(uuid);
		
		if(uuid.equals(trx.getSecureSuccessUUID())) {
			log.info(Tracing.M_AUDIT, "Paypal Checkout transaction success: {}", trx);
			completeTransaction(trx);
		} else if (uuid.equals(trx.getSecureCancelUUID())) {
			//cancel -> return to the access panel
			log.info(Tracing.M_AUDIT, "Paypal Checkout transaction canceled by user: {}", trx);
			cancelTransaction(trx);
		}
	}
	
	@Override
	public void approveTransaction(String paypalOrderId) {
		PaypalCheckoutTransaction trx = transactionDao.loadTransactionByPaypalOrderId(paypalOrderId);
		if(trx != null) {
			log.info(Tracing.M_AUDIT, "Paypal Checkout transaction approved: {}", trx);
			completeTransaction(trx);
		} else {
			log.error("Paypal Checkout transaction not found for approval: {} (Paypal order id)", paypalOrderId);
		}
	}
	
	@Override
	public void approveAuthorization(String paypalAuthorizationId, Capture capture) {
		PaypalCheckoutTransaction trx = transactionDao.loadTransactionByAuthorizationId(paypalAuthorizationId);
		if(trx != null) {
			if(PaypalCheckoutStatus.PENDING.name().equals(trx.getPaypalOrderStatus())) {
				// transfer data from capture to our transaction
				checkoutProvider.captureToTransaction(capture, trx);
				trx = transactionDao.update(trx);
				dbInstance.commit();
				
				log.info(Tracing.M_AUDIT, "Paypal Checkout transaction approved: {}", trx);
				completeTransactionSucessfully(trx);
			} else if(PaypalCheckoutStatus.COMPLETED.name().equals(trx.getPaypalOrderStatus())) {
				log.info(Tracing.M_AUDIT, "Paypal Checkout transaction already completed: {}", trx);
			} else {
				String status = trx.getPaypalOrderStatus();
				log.error(Tracing.M_AUDIT, "Paypal Checkout transaction with status {} cannot be completed: {}", status, trx);
			}
		} else {
			log.error("Paypal Checkout transaction not found for approval: {} (Paypal authorization id)", paypalAuthorizationId);
		}
	}
	
	@Override
	public boolean canSkippedReservation(IdentityRef delivery, OLATResource resource, Date referenceDate) {
		List<PaypalCheckoutTransaction> trxList = transactionDao.loadTransactionByIdentityAndResource(delivery, resource, referenceDate);
		if(trxList.isEmpty()) {
			return true;
		}
		
		for(PaypalCheckoutTransaction trx:trxList) {
			PaypalCheckoutStatus status = trx.getStatus();
			String payPalOrderStatus = trx.getPaypalOrderStatus();
			if(status == PaypalCheckoutStatus.PENDING
					|| status == PaypalCheckoutStatus.COMPLETED
					|| PaypalCheckoutStatus.PENDING.name().equals(payPalOrderStatus)
					|| PaypalCheckoutStatus.COMPLETED.name().equals(payPalOrderStatus)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void cancelTransaction(String paypalOrderId) {
		PaypalCheckoutTransaction trx = transactionDao.loadTransactionByPaypalOrderId(paypalOrderId);
		if(trx != null) {
			log.info(Tracing.M_AUDIT, "Paypal Checkout transaction cancelled: {}", trx);
			cancelTransaction(trx);
		} else {
			log.error("Paypal Checkout transaction not found for cancellation: {} (Paypal order id)", paypalOrderId);
		}
	}

	@Override
	public void errorTransaction(String paypalOrderId) {
		PaypalCheckoutTransaction trx = transactionDao.loadTransactionByPaypalOrderId(paypalOrderId);
		if(trx != null) {
			log.info(Tracing.M_AUDIT, "Paypal Checkout transaction error: {}", trx);
			completeDeniedTransaction(trx);
		} else {
			log.error("Paypal Checkout transaction not found for error: {} (Paypal order id)", paypalOrderId);
		}
	}

	private PaypalCheckoutTransaction completeTransaction(PaypalCheckoutTransaction trx) {
		try {
			trx = checkoutProvider.authorizeUrl(trx);
			if(PaypalCheckoutStatus.CREATED.name().equals(trx.getPaypalOrderStatus())) {
				trx = checkoutProvider.captureOrder(trx);
				if(PaypalCheckoutStatus.COMPLETED.name().equals(trx.getPaypalOrderStatus())) {
					completeTransactionSucessfully(trx);
				} else if(PaypalCheckoutStatus.PENDING.name().equals(trx.getPaypalOrderStatus())) {
					pendingTransaction(trx);
				} else {
					completeDeniedTransaction(trx);
				}
			} else if(PaypalCheckoutStatus.PENDING.name().equals(trx.getPaypalOrderStatus())) {
				pendingTransaction(trx);
			} else {
				completeDeniedTransaction(trx);
			}
		} catch (IOException e) {
			log.error("", e);
		}
		return trx;
	}
	
	private void cancelTransaction(PaypalCheckoutTransaction trx) {
		if(PaypalCheckoutStatus.COMPLETED.name().equals(trx.getPaypalOrderStatus())) {
			//already completed: let it in this state
			return;
		}

		trx.setStatus(PaypalCheckoutStatus.CANCELED);
		trx.setPaypalOrderStatus(PaypalCheckoutStatus.CANCELED.name());
		trx = transactionDao.update(trx);
		
		Order order = orderManager.loadOrderByNr(trx.getOrderNr());
		orderManager.save(order, OrderStatus.CANCELED);
		
		//cancel the reservations
		PaypalCheckoutAccessMethod method = getMethodSecure(trx.getMethodId());
		Identity identity = order.getDelivery();
		for(OrderPart part:order.getParts()) {
			if(part.getKey().equals(trx.getOrderPartId())) {
				AccessTransaction transaction = transactionManager.createTransaction(order, part, method);
				transactionManager.update(transaction, AccessTransactionStatus.CANCELED);
				for(OrderLine line:part.getOrderLines()) {
					OLATResource resource = line.getOffer().getResource();
					ResourceReservation reservation = acService.getReservation(identity, resource);
					if(reservation != null) {
						acService.removeReservation(identity, identity, reservation, null);
						log.info(Tracing.M_AUDIT, "Remove reservation after cancellation for: {} to {}", reservation, identity);
					}
				}
			}
		}
	}
	
	private void pendingTransaction(PaypalCheckoutTransaction trx) {
		Order order = orderManager.loadOrderByNr(trx.getOrderNr());
		order = orderManager.save(order, OrderStatus.PREPAYMENT);
		
		PaypalCheckoutAccessMethod method = getMethodSecure(trx.getMethodId());
		if(order.getKey().equals(trx.getOrderId())) {
			Identity identity = order.getDelivery();
			for(OrderPart part:order.getParts()) {
				if(part.getKey().equals(trx.getOrderPartId())) {
					AccessTransaction transaction = transactionManager.createTransaction(order, part, method);
					transaction = transactionManager.update(transaction, AccessTransactionStatus.PENDING);
					if(checkoutModule.isAcceptPendingReview()) {
						allowAccessToResource(identity, part, transaction, method);
					}
				}
			}
		} else {
			log.error("Order not in sync with PaypalTransaction");
		}
		dbInstance.commit();
	}
	
	private void completeDeniedTransaction(PaypalCheckoutTransaction trx) {
		Order order = orderManager.loadOrderByNr(trx.getOrderNr());
		order = orderManager.save(order, OrderStatus.ERROR);
		
		PaypalCheckoutAccessMethod method = getMethodSecure(trx.getMethodId());
		if(order.getKey().equals(trx.getOrderId())) {
			//make accessible
			Identity identity = order.getDelivery();
			for(OrderPart part:order.getParts()) {
				if(part.getKey().equals(trx.getOrderPartId())) {
					AccessTransaction transaction = transactionManager.createTransaction(order, part, method);
					transactionManager.update(transaction, AccessTransactionStatus.ERROR);
					for(OrderLine line:part.getOrderLines()) {
						acService.denyAccesToResource(identity, line.getOffer());
						log.info(Tracing.M_AUDIT, "Paypal payed access revoked for: {} to {}",  buildLogMessage(line, method), identity);

						ResourceReservation reservation = reservationDao.loadReservation(identity, line.getOffer().getResource());
						if(reservation != null) {
							acService.removeReservation(identity, identity, reservation, null);
							log.info(Tracing.M_AUDIT, "Remove reservation after cancellation for: {} to {}", reservation, identity);
						}
					}
				}
			}
		} else {
			log.error("Order not in sync with PaypalTransaction");
		}
		dbInstance.commit();
	}
	
	private void completeTransactionSucessfully(PaypalCheckoutTransaction trx) {
		Order order = orderManager.loadOrderByNr(trx.getOrderNr());
		order = orderManager.save(order, OrderStatus.PAYED);

		PaypalCheckoutAccessMethod method = getMethodSecure(trx.getMethodId());
		if(order.getKey().equals(trx.getOrderId())) {
			//make accessible
			Identity identity = order.getDelivery();
			for(OrderPart part:order.getParts()) {
				if(part.getKey().equals(trx.getOrderPartId())) {
					AccessTransaction transaction = transactionManager.createTransaction(order, part, method);
					transaction = transactionManager.save(transaction);
					allowAccessToResource(identity, part, transaction, method);
				}
			}
		} else {
			log.error("Order not in sync with Paypal Checkout Transaction");
		}
		dbInstance.commit();
	}
	
	private void allowAccessToResource(Identity identity, OrderPart part, AccessTransaction transaction, PaypalCheckoutAccessMethod method) {
		for(OrderLine line:part.getOrderLines()) {
			MailPackage mailing = new MailPackage(line.getOffer().isConfirmationEmail());
			if(acService.allowAccesToResource(identity, line.getOffer(), method, mailing, identity, null)) {
				log.info(Tracing.M_AUDIT, "Paypal Checkout payed access granted for: {} to {}", buildLogMessage(line, method), identity);
				transaction = transactionManager.update(transaction, AccessTransactionStatus.SUCCESS);
				reservationDao.deleteReservation(line.getOffer().getResource(), identity);
			} else {
				log.error("Paypal Checkout payed access refused for: {} to {}", buildLogMessage(line, method), identity);
				transaction = transactionManager.update(transaction, AccessTransactionStatus.ERROR);
			}
		}
	}
	
	private PaypalCheckoutAccessMethod getMethodSecure(Long key) {
		PaypalCheckoutAccessMethod smethod = null;
		List<PaypalCheckoutAccessMethod> methods = getPaypalCheckoutMethods();
		for(PaypalCheckoutAccessMethod method:methods) {
			if(key != null && key.equals(method.getKey())) {
				smethod = method;
			}
		}
		
		if(smethod == null) {
			if(methods.isEmpty()) {
				smethod = new PaypalCheckoutAccessMethod();
				smethod.setCreationDate(new Date());
				smethod.setLastModified(smethod.getCreationDate());
				dbInstance.getCurrentEntityManager().persist(smethod);
			} else {
				smethod = methods.get(0);
			}
		}
		
		return smethod;
	}
	
	private List<PaypalCheckoutAccessMethod> getPaypalCheckoutMethods() {
		String q = "select method from accheckoutmethod method";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, PaypalCheckoutAccessMethod.class)
				.getResultList();
	}
	
	private String buildLogMessage(OrderLine line, PaypalCheckoutAccessMethod method) {
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

	@Override
	public PaypalCheckoutTransaction loadTransactionByUUID(String uuid) {
		return transactionDao.loadTransactionBySecureUuid(uuid);
	}

	@Override
	public PaypalCheckoutTransaction loadTransaction(Order order, OrderPart part) {
		return transactionDao.loadTransactionBy(order, part);
	}

	@Override
	public List<PSPTransaction> loadTransactions(List<Order> orders) {
		return transactionDao.loadTransactionBy(orders);
	}

	@Override
	public List<PaypalCheckoutTransactionWithDelivery> searchTransactions(String id) {
		return transactionDao.searchTransactions(id);
	}

	@Override
	public Order getOrder(PaypalCheckoutTransaction trx) {
		if(trx == null || trx.getOrderId() == null) {
			return null;
		}
		return orderManager.loadOrderByKey(trx.getOrderId());
	}
}
