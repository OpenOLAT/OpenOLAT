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

package org.olat.resource.accesscontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.resource.accesscontrol.manager.ACMethodManager;
import org.olat.resource.accesscontrol.manager.ACOrderManager;
import org.olat.resource.accesscontrol.manager.ACTransactionManager;
import org.olat.resource.accesscontrol.manager.ACOfferManager;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessTransaction;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderLine;
import org.olat.resource.accesscontrol.model.OrderPart;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * test the order manager
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACTransactionManagerTest extends OlatTestCase {
	
	private static Identity ident1;
	private static boolean isInitialized = false;
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private ACOfferManager acOfferManager;
	
	@Autowired
	private ACFrontendManager acFrontendManager;
	
	@Autowired
	private ACMethodManager acMethodManager;

	@Autowired
	private OLATResourceManager resourceManager;
	
	@Autowired
	private ACOrderManager acOrderManager;
	
	@Autowired
	private ACTransactionManager acTransactionManager;
	
	@Before
	public void setUp() {
		if(!isInitialized) {
			ident1 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
		}
	}
	
	@After
	public void tearDown() {
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testManagers() {
		assertNotNull(acOfferManager);
		assertNotNull(acFrontendManager);
		assertNotNull(dbInstance);
		assertNotNull(acMethodManager);
		assertNotNull(acOrderManager);
		assertNotNull(acTransactionManager);
	}
	
	@Test
	public void testSaveTransaction() {
		//pick up a method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod tokenMethod = methods.get(0);
		
		//create an offer to buy
		OLATResource randomOres = createResource();
		Offer offer = acFrontendManager.createOffer(randomOres, "TestSaveTransaction");
		acFrontendManager.save(offer);
		
		dbInstance.commitAndCloseSession();
		
		//create and save an order
		Order order = acOrderManager.createOrder(ident1);
		OrderPart orderPart = acOrderManager.addOrderPart(order);
		OrderLine item = acOrderManager.addOrderLine(orderPart, offer);
		
		assertNotNull(order);
		assertNotNull(item);
		assertNotNull(order.getDelivery());
		assertEquals(ident1, order.getDelivery());
		acOrderManager.save(order);

		AccessTransaction accessTransaction = acTransactionManager.createTransaction(order, orderPart, tokenMethod);
		assertNotNull(accessTransaction);
		acTransactionManager.save(accessTransaction);
		
		dbInstance.commitAndCloseSession();

		{//test load by key
			AccessTransaction retrievedTransaction = acTransactionManager.loadTransactionByKey(accessTransaction.getKey());
			assertNotNull(retrievedTransaction);
			assertNotNull(retrievedTransaction.getOrder());
			assertEquals(order, retrievedTransaction.getOrder());
			assertNotNull(retrievedTransaction.getOrderPart());
			assertEquals(orderPart, retrievedTransaction.getOrderPart());
			assertNotNull(retrievedTransaction.getMethod());
			assertEquals(tokenMethod, retrievedTransaction.getMethod());
		}

		{//test load by order
			List<AccessTransaction> retrievedTransactions = acTransactionManager.loadTransactionsForOrders(Collections.singletonList(order));
			assertNotNull(retrievedTransactions);
			assertEquals(1, retrievedTransactions.size());
			
			AccessTransaction retrievedTransaction = retrievedTransactions.get(0);
			assertNotNull(retrievedTransaction);
			assertNotNull(retrievedTransaction.getOrder());
			assertEquals(order, retrievedTransaction.getOrder());
			assertNotNull(retrievedTransaction.getOrderPart());
			assertEquals(orderPart, retrievedTransaction.getOrderPart());
			assertNotNull(retrievedTransaction.getMethod());
			assertEquals(tokenMethod, retrievedTransaction.getMethod());
		}
	}
	
	private OLATResource createResource() {
		//create a repository entry
		OLATResourceable resourceable = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource r =  resourceManager.createOLATResourceInstance(resourceable);
		dbInstance.saveObject(r);
		return r;
	}
}
