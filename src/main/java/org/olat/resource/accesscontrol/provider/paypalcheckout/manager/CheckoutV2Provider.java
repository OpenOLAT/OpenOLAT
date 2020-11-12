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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutModule;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutStatus;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutTransaction;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.CheckoutRequest;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.http.exceptions.HttpException;
import com.paypal.http.serializer.Json;
import com.paypal.orders.AmountBreakdown;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersAuthorizeRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;
import com.paypal.payments.AuthorizationsCaptureRequest;
import com.paypal.payments.Capture;
import com.paypal.payments.Money;
import com.paypal.payments.StatusDetails;

/**
 * 
 * Initial date: 25 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CheckoutV2Provider {
	
	private static final Logger log = Tracing.createLoggerFor(CheckoutV2Provider.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PaypalCheckoutModule paypalCheckoutModule;
	@Autowired
	private PaypalCheckoutTransactionDAO transactionDao;
	
	public PaypalCheckoutTransaction createOrder(org.olat.resource.accesscontrol.Order order, PaypalCheckoutTransaction trx) {
		ApplicationContext applicationContext = buildApplicationContext();
		OrderRequest orderRequest = buildOrderRequest(order, "AUTHORIZE", applicationContext);
		OrdersCreateRequest request = buildOrdersCreateRequest(orderRequest);

		try {
			HttpResponse<Order> orderResponse = client().execute(request);
			if (orderResponse.statusCode() == 201) {
				Order paypalOrder = orderResponse.result();
				trx.setPaypalOrderId(paypalOrder.id());
				trx.setPaypalOrderStatus(paypalOrder.status());
				trx.setStatus(PaypalCheckoutStatus.INPROCESS);
				log.info(Tracing.M_AUDIT, "Create Paypal order: id:{} status:{}", paypalOrder.id(), paypalOrder.status());
				for (LinkDescription link : orderResponse.result().links()) {
					log.debug("Create Paypal link: rel:{} href:{}", link.rel(), link.href());
					log.debug("Create Paypal link: rel:{} media:{} for schema:{}", link.rel(), link.mediaType(),
							link.schema());
				}
			} else {
				log.error(Tracing.M_AUDIT, "Create Paypal order status:{}", orderResponse.statusCode());
			}
		} catch (HttpException e) {
            JSONObject message = new JSONObject(e.getMessage());
            log.error(Tracing.M_AUDIT, prettyPrint(message, ""));
			log.error(Tracing.M_AUDIT, "Create Paypal order", e);
			trx.setStatus(PaypalCheckoutStatus.ERROR);
			trx.setPaypalOrderStatus(PaypalCheckoutStatus.ERROR.name());
		} catch (IOException e) {
			log.error(Tracing.M_AUDIT, "Create Paypal order", e);
			trx.setStatus(PaypalCheckoutStatus.ERROR);
			trx.setPaypalOrderStatus(PaypalCheckoutStatus.ERROR.name());
		}
		trx = transactionDao.update(trx);
		dbInstance.commit();
        return trx;
	}
	
	public CheckoutRequest paymentUrl(String url, org.olat.resource.accesscontrol.Order order, PaypalCheckoutTransaction trx, String sessionId) {
		
		String returnURL = url + "/" + trx.getSecureSuccessUUID() + ".html;jsessionid=" + sessionId + "?status=success";
		String cancelURL = url + "/" + trx.getSecureCancelUUID() + ".html;jsessionid=" + sessionId + "?status=cancel";
		ApplicationContext applicationContext = buildApplicationContext()
				.cancelUrl(cancelURL)
				.returnUrl(returnURL);

		OrderRequest orderRequest = buildOrderRequest(order, "AUTHORIZE", applicationContext);
		OrdersCreateRequest request = buildOrdersCreateRequest(orderRequest);

		CheckoutRequest checkoutRequest = new CheckoutRequest();
		try {
			HttpResponse<Order> orderResponse = client().execute(request);
			if (orderResponse.statusCode() == 201) {
				Order paypalOrder = orderResponse.result();
				trx.setPaypalOrderId(paypalOrder.id());
				trx.setPaypalOrderStatus(paypalOrder.status());
				trx.setStatus(PaypalCheckoutStatus.INPROCESS);
				log.info(Tracing.M_AUDIT, "Create Paypal order: id:{} status:{}", paypalOrder.id(), paypalOrder.status());
				for (LinkDescription link : orderResponse.result().links()) {
					log.debug("Create Paypal link: rel:{} href:{}", link.rel(), link.href());
					log.debug("Create Paypal link: rel:{} media:{} for schema:{}", link.rel(), link.mediaType(),
							link.schema());
					if ("approve".equals(link.rel())) {
						checkoutRequest.setRedirectToPaypalUrl(link.href());
						checkoutRequest.setStatus(PaypalCheckoutStatus.CREATED.name());
					}
				}
			} else {
				log.error(Tracing.M_AUDIT, "Create Paypal order status:{}", orderResponse.statusCode());
			}
		} catch (HttpException e) {
            JSONObject message = new JSONObject(e.getMessage());
            log.error(Tracing.M_AUDIT, prettyPrint(message, ""));
			log.error(Tracing.M_AUDIT, "Create Paypal order", e);
			checkoutRequest.setStatus(PaypalCheckoutStatus.ERROR.name());
			trx.setStatus(PaypalCheckoutStatus.ERROR);
			trx.setPaypalOrderStatus(PaypalCheckoutStatus.ERROR.name());
		} catch (IOException e) {
			log.error(Tracing.M_AUDIT, "Create Paypal order", e);
			checkoutRequest.setStatus(PaypalCheckoutStatus.ERROR.name());
			trx.setStatus(PaypalCheckoutStatus.ERROR);
			trx.setPaypalOrderStatus(PaypalCheckoutStatus.ERROR.name());
		}
		trx = transactionDao.update(trx);
		dbInstance.commit();
        checkoutRequest.setCheckoutTransactionn(trx);
        return checkoutRequest;
	}
	
	private ApplicationContext buildApplicationContext() {
		ApplicationContext context = new ApplicationContext();
		context.shippingPreference("NO_SHIPPING");
		return context;
	}
	
	private OrdersCreateRequest buildOrdersCreateRequest(OrderRequest orderRequest) {
		OrdersCreateRequest request = new OrdersCreateRequest();
		request.header("prefer", "return=representation");
		request.requestBody(orderRequest);
		return request;
	}
	
	private OrderRequest buildOrderRequest(org.olat.resource.accesscontrol.Order order, String intent, ApplicationContext applicationContext) {
		OrderRequest orderRequest = new OrderRequest();
		orderRequest.checkoutPaymentIntent(intent);
		orderRequest.applicationContext(applicationContext);

		String price = PriceFormat.format(order.getTotal());
		String currencyCode = order.getCurrencyCode();
		
		AmountWithBreakdown amount = new AmountWithBreakdown()
				.currencyCode(currencyCode).value(price)
				.amountBreakdown(new AmountBreakdown().itemTotal(new com.paypal.orders.Money().currencyCode(currencyCode).value(price)));

		List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<>();
		PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
				.amountWithBreakdown(amount);
		purchaseUnitRequests.add(purchaseUnitRequest);
		orderRequest.purchaseUnits(purchaseUnitRequests);

		return orderRequest;
	}
	
	public PaypalCheckoutTransaction authorizeUrl(PaypalCheckoutTransaction trx)  {
		OrdersAuthorizeRequest request = new OrdersAuthorizeRequest(trx.getPaypalOrderId());
		request.requestBody(new OrderRequest());
		
		try {
			HttpResponse<Order> response = client().execute(request);
			String authId = null;
			if (response.statusCode() == 201) {
			    com.paypal.orders.Authorization authorization = response.result().purchaseUnits().get(0).payments().authorizations().get(0);
			    authId = authorization.id();
			    trx.setPaypalAuthorizationId(authId);
				trx.setStatus(PaypalCheckoutStatus.INPROCESS);
			    trx.setPaypalOrderStatus(authorization.status());
			    log.info(Tracing.M_AUDIT, "Authorization ID 201: id:{} status:{}", authId, authorization.status());
			} else {
			    log.error(Tracing.M_AUDIT, "Authorization HTTP code: {}", response.statusCode());
				trx.setStatus(PaypalCheckoutStatus.ERROR);
				trx.setPaypalOrderStatus(PaypalCheckoutStatus.ERROR.name());
				trx.setPaypalOrderStatusReason("Code: " + response.statusCode());
			}
		} catch (HttpException e) {
            JSONObject message = new JSONObject(e.getMessage());
            log.error(Tracing.M_AUDIT, prettyPrint(message, ""));
			log.error(Tracing.M_AUDIT, "Authorization", e);
			trx.setStatus(PaypalCheckoutStatus.ERROR);
			trx.setPaypalOrderStatus(PaypalCheckoutStatus.ERROR.name());
		} catch (IOException e) {
			log.error(Tracing.M_AUDIT, "Authorization", e);
			trx.setStatus(PaypalCheckoutStatus.ERROR);
			trx.setPaypalOrderStatus(PaypalCheckoutStatus.ERROR.name());
		}

        trx = transactionDao.update(trx);
        dbInstance.commit();
		return trx;
	}
	
	public PaypalCheckoutTransaction captureOrder(PaypalCheckoutTransaction trx) throws IOException {
		AuthorizationsCaptureRequest request = new AuthorizationsCaptureRequest(trx.getPaypalAuthorizationId());
		request.requestBody(new OrderRequest());

		try {
			HttpResponse<Capture> response = client().execute(request);
			if(log.isDebugEnabled()) {
				log.debug("Status: code:{} status:{} capture id:{}", response.statusCode(), response.result().status(), response.result().id());
				log.debug(new JSONObject(new Json().serialize(response.result())).toString(4));
			}

			if (response.statusCode() == 201) {
				Capture capture = response.result();
				String status = capture.status();
				String captureId = capture.id();
				String invoiceId = capture.invoiceId();
				String statusReason = null;
				StatusDetails statusDetails = capture.statusDetails();
				if (statusDetails != null) {
					statusReason = statusDetails.reason();
				}
				trx.setCapturePrice(getCapturedPrice(capture));
				trx.setPaypalCaptureId(captureId);
				trx.setPaypalInvoiceId(invoiceId);
				trx.setPaypalOrderStatus(status);
				trx.setPaypalOrderStatusReason(statusReason);
				if(PaypalCheckoutStatus.PENDING.name().equals(status)) {
					trx.setStatus(PaypalCheckoutStatus.PENDING);
				} else if(PaypalCheckoutStatus.COMPLETED.name().equals(status)) {
					trx.setStatus(PaypalCheckoutStatus.COMPLETED);
				} else  {
					trx.setStatus(PaypalCheckoutStatus.ERROR);
				}

				log.info(Tracing.M_AUDIT, "Capture: id:{} invoiceId:{} status:{} reason:{}", captureId, invoiceId, status, statusReason);
			} else {
				trx.setStatus(PaypalCheckoutStatus.ERROR);
				trx.setPaypalOrderStatus(PaypalCheckoutStatus.ERROR.name());
				trx.setPaypalOrderStatusReason("Code: " + response.statusCode());
			}
		} catch (HttpException e) {
            JSONObject message = new JSONObject(e.getMessage());
            log.error(Tracing.M_AUDIT, prettyPrint(message, ""));
			log.error(Tracing.M_AUDIT, "Capture", e);
			trx.setStatus(PaypalCheckoutStatus.ERROR);
			trx.setPaypalOrderStatus(PaypalCheckoutStatus.ERROR.name());
		} catch (IOException e) {
			log.error(Tracing.M_AUDIT, "Capture", e);
			trx.setStatus(PaypalCheckoutStatus.ERROR);
			trx.setPaypalOrderStatus(PaypalCheckoutStatus.ERROR.name());
		}

		trx = transactionDao.update(trx);
		dbInstance.commit();
		return trx;
	}
	
	private Price getCapturedPrice(Capture capture) {
		if(capture.amount() == null) {
			return null;
		}
		
		Money capturedAmount = capture.amount();
		try {
			log.info(Tracing.M_AUDIT, "Capture price: amout:{} currency:{}", capturedAmount.value(), capturedAmount.currencyCode());
			return new PriceImpl(new BigDecimal(capturedAmount.value()), capturedAmount.currencyCode());
		} catch (Exception e) {
			log.error(Tracing.M_AUDIT, "Capture price: amout:{} currency:{}", capturedAmount.value(), capturedAmount.currencyCode());
			return null;
		}
	}
	
	private PayPalHttpClient client() {
		String clientId = paypalCheckoutModule.getClientId();
		String clientSecret = paypalCheckoutModule.getClientSecret();

		PayPalEnvironment environment;
		if(paypalCheckoutModule.isSandbox()) {
			environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
		} else {
			environment = new PayPalEnvironment.Live(clientId, clientSecret);
		}
		return new PayPalHttpClient(environment);
	}
	
	public String prettyPrint(JSONObject jo, String pre) {
		StringBuilder pretty = new StringBuilder(2048);
		for (Iterator<String> keys = jo.keys(); keys.hasNext(); ) {
			String key = keys.next();
			pretty.append(String.format("%s%s: ", pre, key));
			if (jo.get(key) instanceof JSONObject) {
				pretty.append(prettyPrint(jo.getJSONObject(key), pre + "\t"));
			} else if (jo.get(key) instanceof JSONArray) {
				int sno = 1;
				for (Object jsonObject : jo.getJSONArray(key)) {
					pretty.append(String.format("%n%s\t%d:%n", pre, sno++));
					pretty.append(prettyPrint((JSONObject) jsonObject, pre + "\t\t"));
				}
			} else {
				pretty.append(String.format("%s%n", jo.getString(key)));
			}
		}
		return pretty.toString();
	}
}
