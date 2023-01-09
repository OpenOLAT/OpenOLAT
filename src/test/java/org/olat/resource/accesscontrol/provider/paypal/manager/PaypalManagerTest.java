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
package org.olat.resource.accesscontrol.provider.paypal.manager;

import java.math.BigDecimal;
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
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalAccessMethod;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransaction;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransactionStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private ACOrderDAO acOrderManager;
	@Autowired
	private ACMethodDAO acMethodManager;
	@Autowired
	private PaypalManagerImpl paypalManager;
	
	@Test
	public void createTransaction() {
		//pick up a method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(PaypalAccessMethod.class);
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
		
		Price price = new PriceImpl(BigDecimal.valueOf(2.0d), "CHF");
		PaypalTransaction transaction = paypalManager.createAndPersistTransaction(price, order, orderPart, checkoutMethod);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(transaction);
		
		PaypalTransaction loadedTransaction = paypalManager.loadTransactionByInvoiceId(order.getOrderNr());
		Assert.assertNotNull(loadedTransaction);
		Assert.assertEquals(price.getAmount(), loadedTransaction.getSecurePrice().getAmount());
		Assert.assertEquals(price.getCurrencyCode(), loadedTransaction.getSecurePrice().getCurrencyCode());
	}
	
	@Test
	public void loadTransactionByUUIDs() {
		Offer offer = createOffer();
		
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("paypal-3");
		dbInstance.commit();
		
		//create and save an order
		Order order = acOrderManager.createOrder(identity);
		OrderPart orderPart = acOrderManager.addOrderPart(order);
		acOrderManager.addOrderLine(orderPart, offer);
		order = acOrderManager.save(order);
		dbInstance.commit();
		
		Price price = new PriceImpl(BigDecimal.valueOf(3.5d), "CHF");
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(PaypalAccessMethod.class);
		AccessMethod checkoutMethod = methods.get(0);
		
		String uuid = UUID.randomUUID().toString().replace("-", "");
		uuid = uuid.substring(8, uuid.length());
		
		PaypalTransaction transaction = paypalManager.createAndPersistTransaction(price, order, orderPart, checkoutMethod);
		transaction.setSecureCancelUUID("cancel-" + uuid);
		transaction.setSecureSuccessUUID("success-" + uuid);
		paypalManager.updateTransaction(transaction, PaypalTransactionStatus.SUCCESS);
		dbInstance.commitAndCloseSession();
		
		// Check load methods
		PaypalTransaction transactionByCancel = paypalManager.loadTransactionByUUID("cancel-" + uuid);
		Assert.assertNotNull(transactionByCancel);
		Assert.assertEquals(transaction, transactionByCancel);
		PaypalTransaction transactionBySuccess = paypalManager.loadTransactionByUUID("success-" + uuid);
		Assert.assertNotNull(transactionBySuccess);
		Assert.assertEquals(transaction, transactionBySuccess);
		
		// Check some details
		Assert.assertEquals(price.getAmount(), transactionBySuccess.getSecurePrice().getAmount());
		Assert.assertEquals(price.getCurrencyCode(), transactionBySuccess.getSecurePrice().getCurrencyCode());
	}
	
	private Offer createOffer() {
		//create an offer to buy
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("paypal-2");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Offer offer = acService.createOffer(entry.getOlatResource(), "TestSaveTransaction");
		return acService.save(offer);
	}
}
