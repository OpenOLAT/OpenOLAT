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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderLine;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.manager.ACOrderDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutTransaction;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.PaypalCheckoutAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutTransactionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private ACOrderDAO acOrderManager;
	@Autowired
	private ACMethodDAO acMethodManager;
	@Autowired
	private PaypalCheckoutTransactionDAO transactionDao;
	
	@Test
	public void createTransaction() {
		//pick up a method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(PaypalCheckoutAccessMethod.class);
		Assert.assertNotNull(methods);
		Assert.assertEquals(1, methods.size());
		AccessMethod checkoutMethod = methods.get(0);

		//create an offer to buy
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("customer-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Offer offer = acService.createOffer(entry.getOlatResource(), "TestSaveTransaction");
		offer = acService.save(offer);
		
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("customer-1");
		dbInstance.commit();
		
		//create and save an order
		Order order = acOrderManager.createOrder(identity);
		OrderPart orderPart = acOrderManager.addOrderPart(order);
		OrderLine item = acOrderManager.addOrderLine(orderPart, offer);
		order = acOrderManager.save(order);
		dbInstance.commit();
		Assert.assertNotNull(item);
		
		Price amount = new PriceImpl(new BigDecimal("5.00"), "CHF");
		PaypalCheckoutTransaction trx = transactionDao.createTransaction(amount, order, orderPart, checkoutMethod);
		dbInstance.commit();
		
		Assert.assertNotNull(trx);
		Assert.assertNotNull(trx.getCreationDate());
		Assert.assertNotNull(trx.getLastModified());
		Assert.assertNotNull(trx.getSecureCancelUUID());
		Assert.assertNotNull(trx.getSecureSuccessUUID());
		Assert.assertEquals(order.getOrderNr(), trx.getOrderNr());
		Assert.assertEquals(order.getKey(), trx.getOrderId());
		Assert.assertEquals(orderPart.getKey(), trx.getOrderPartId());
		Assert.assertEquals(checkoutMethod.getKey(), trx.getMethodId());
		Assert.assertEquals(new BigDecimal("5.00"), trx.getSecurePrice().getAmount());
		Assert.assertEquals("CHF", trx.getSecurePrice().getCurrencyCode());
	}
	
	@Test
	public void loadTransactionBySecureUuid() {
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(PaypalCheckoutAccessMethod.class);
		AccessMethod checkoutMethod = methods.get(0);

		//create an offer to buy
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("customer-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Offer offer = acService.createOffer(entry.getOlatResource(), "TestSaveTransaction");
		offer = acService.save(offer);
		
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("customer-1");
		dbInstance.commit();
		
		//create and save an order
		Order order = acOrderManager.createOrder(identity);
		OrderPart orderPart = acOrderManager.addOrderPart(order);
		OrderLine item = acOrderManager.addOrderLine(orderPart, offer);
		order = acOrderManager.save(order);
		dbInstance.commit();
		Assert.assertNotNull(item);
		
		Price amount = new PriceImpl(new BigDecimal("5.00"), "CHF");
		PaypalCheckoutTransaction trx = transactionDao.createTransaction(amount, order, orderPart, checkoutMethod);
		dbInstance.commitAndCloseSession();
		
		String cancelUuid = trx.getSecureCancelUUID();
		String successUuid = trx.getSecureSuccessUUID();

		PaypalCheckoutTransaction canceledTrx = transactionDao.loadTransactionBySecureUuid(cancelUuid);
		Assert.assertNotNull(canceledTrx);
		Assert.assertEquals(trx, canceledTrx);
		
		PaypalCheckoutTransaction successTrx = transactionDao.loadTransactionBySecureUuid(successUuid);
		Assert.assertNotNull(successTrx);
		Assert.assertEquals(trx, successTrx);
		
		PaypalCheckoutTransaction unkownTrx = transactionDao.loadTransactionBySecureUuid("something-uuid-but-not-real");
		Assert.assertNull(unkownTrx);
	}
	
	@Test
	public void loadTransactionByPaypalOrderId() {
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(PaypalCheckoutAccessMethod.class);
		AccessMethod checkoutMethod = methods.get(0);

		//create an offer to buy
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("customer-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Offer offer = acService.createOffer(entry.getOlatResource(), "TestSaveTransaction");
		offer = acService.save(offer);
		
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("customer-1");
		dbInstance.commit();
		
		//create and save an order
		Order order = acOrderManager.createOrder(identity);
		OrderPart orderPart = acOrderManager.addOrderPart(order);
		OrderLine item = acOrderManager.addOrderLine(orderPart, offer);
		order = acOrderManager.save(order);
		dbInstance.commit();
		Assert.assertNotNull(item);
		
		String paypalOrderId = UUID.randomUUID().toString();
		Price amount = new PriceImpl(new BigDecimal("5.00"), "CHF");
		PaypalCheckoutTransaction trx = transactionDao.createTransaction(amount, order, orderPart, checkoutMethod);
		trx.setPaypalOrderId(paypalOrderId);
		dbInstance.commitAndCloseSession();

		PaypalCheckoutTransaction orderedTrx = transactionDao.loadTransactionByPaypalOrderId(paypalOrderId);
		Assert.assertNotNull(orderedTrx);
		Assert.assertEquals(trx, orderedTrx);
	}

	@Test
	public void loadTransactionByOrder() {
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(PaypalCheckoutAccessMethod.class);
		AccessMethod checkoutMethod = methods.get(0);

		//create an offer to buy
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("customer-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Offer offer = acService.createOffer(entry.getOlatResource(), "TestSaveTransaction");
		offer = acService.save(offer);
		
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("customer-1");
		dbInstance.commit();
		
		//create and save an order
		Order order = acOrderManager.createOrder(identity);
		OrderPart orderPart = acOrderManager.addOrderPart(order);
		OrderLine item = acOrderManager.addOrderLine(orderPart, offer);
		order = acOrderManager.save(order);
		dbInstance.commit();
		Assert.assertNotNull(item);
		
		Price amount = new PriceImpl(new BigDecimal("5.00"), "CHF");
		PaypalCheckoutTransaction trx = transactionDao.createTransaction(amount, order, orderPart, checkoutMethod);
		dbInstance.commitAndCloseSession();

		PaypalCheckoutTransaction orderedTrx = transactionDao.loadTransactionBy(order, orderPart);
		Assert.assertNotNull(orderedTrx);
		Assert.assertEquals(trx, orderedTrx);
	}
	
	@Test
	public void loadTransactionByOrders() {
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(PaypalCheckoutAccessMethod.class);
		AccessMethod checkoutMethod = methods.get(0);

		//create an offer to buy
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("customer-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Offer offer = acService.createOffer(entry.getOlatResource(), "TestSaveTransaction");
		offer = acService.save(offer);
		
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("customer-1");
		dbInstance.commit();
		
		//create and save an order
		Order order = acOrderManager.createOrder(identity);
		OrderPart orderPart = acOrderManager.addOrderPart(order);
		OrderLine item = acOrderManager.addOrderLine(orderPart, offer);
		order = acOrderManager.save(order);
		dbInstance.commit();
		Assert.assertNotNull(item);
		
		Price amount = new PriceImpl(new BigDecimal("5.00"), "CHF");
		PaypalCheckoutTransaction trx = transactionDao.createTransaction(amount, order, orderPart, checkoutMethod);
		dbInstance.commitAndCloseSession();
		
		List<Order> orders = new ArrayList<>();
		orders.add(order);
		List<PSPTransaction> orderedTrx = transactionDao.loadTransactionBy(orders);
		Assert.assertNotNull(orderedTrx);
		Assert.assertEquals(1, orderedTrx.size());
		Assert.assertEquals(trx, orderedTrx.get(0));
	}
}
