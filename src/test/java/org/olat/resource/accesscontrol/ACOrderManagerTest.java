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
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.manager.ACOfferDAO;
import org.olat.resource.accesscontrol.manager.ACOrderDAO;
import org.olat.resource.accesscontrol.manager.ACTransactionDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessTransactionStatus;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.resource.accesscontrol.model.RawOrderItem;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.resource.accesscontrol.ui.OrdersDataModel.OrderCol;
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
public class ACOrderManagerTest extends OlatTestCase {
	
	private static Identity ident1, ident2, ident3;
	private static Identity ident4, ident5, ident6;
	private static Identity ident7, ident8;
	private static boolean isInitialized = false;
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private ACOfferDAO acOfferManager;
	
	@Autowired
	private ACTransactionDAO acTransactionManager;
	
	@Autowired
	private ACService acService;
	
	@Autowired
	private ACMethodDAO acMethodManager;

	@Autowired
	private OLATResourceManager resourceManager;
	
	@Autowired
	private ACOrderDAO acOrderManager;
	
	@Before
	public void setUp() {
		if(!isInitialized) {
			ident1 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident2 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident3 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident4 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident5 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident6 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident7 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
			ident8 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString().replace("-", ""));
		}
	}
	
	@Test
	public void testManagers() {
		assertNotNull(acOfferManager);
		assertNotNull(acService);
		assertNotNull(dbInstance);
		assertNotNull(acMethodManager);
		assertNotNull(acOrderManager);
	}
	
	@Test
	public void testSaveOrder() {
		//create an offer to buy
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "TestSaveOrder");
		offer = acService.save(offer);
		
		dbInstance.commitAndCloseSession();
		
		//create and save an order
		Order order = acOrderManager.createOrder(ident1);
		OrderPart part = acOrderManager.addOrderPart(order);
		OrderLine item = acOrderManager.addOrderLine(part, offer);
		
		assertNotNull(order);
		assertNotNull(order.getDelivery());
		assertEquals(ident1, order.getDelivery());
		acOrderManager.save(order);
		
		dbInstance.commitAndCloseSession();
		
		//check what's on DB
		Order retrievedOrder = acOrderManager.loadOrderByKey(order.getKey());
		assertNotNull(retrievedOrder);
		assertNotNull(retrievedOrder.getDelivery());
		assertEquals(ident1, retrievedOrder.getDelivery());
		assertEquals(order, retrievedOrder);
		
		List<OrderPart> parts = retrievedOrder.getParts();
		assertNotNull(parts);
		assertEquals(1, parts.size());
		assertEquals(part, parts.get(0));
		
		OrderPart retrievedPart = parts.get(0);
		assertNotNull(retrievedPart.getOrderLines());
		assertEquals(1, retrievedPart.getOrderLines().size());
		assertEquals(item, retrievedPart.getOrderLines().get(0));
		
		OrderLine retrievedItem = retrievedPart.getOrderLines().get(0);
		assertNotNull(retrievedItem.getOffer());
		assertEquals(offer, retrievedItem.getOffer());
	}
	
	@Test
	public void findOrderItems_1() {
		//create an offer to buy
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod tokenMethod = methods.get(0);
		
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "TestSaveOrder");
		offer = acService.save(offer);
		
		dbInstance.commitAndCloseSession();
		
		//create and save an order
		Order order = acOrderManager.createOrder(ident1);
		OrderPart part = acOrderManager.addOrderPart(order);
		OrderLine line = acOrderManager.addOrderLine(part, offer);
		order = acOrderManager.save(order);
		Assert.assertNotNull(order);
		Assert.assertNotNull(order.getDelivery());
		Assert.assertNotNull(line);
		Assert.assertEquals(ident1, order.getDelivery());
		
		dbInstance.commitAndCloseSession();
		
		AccessTransaction accessTransaction = acTransactionManager.createTransaction(order, part, tokenMethod);
		assertNotNull(accessTransaction);
		acTransactionManager.save(accessTransaction);

		AccessTransaction accessTransaction2 = acTransactionManager.createTransaction(order, part, tokenMethod);
		assertNotNull(accessTransaction2);
		acTransactionManager.save(accessTransaction2);

		dbInstance.commitAndCloseSession();
		acTransactionManager.update(accessTransaction, AccessTransactionStatus.NEW);
		acTransactionManager.update(accessTransaction2, AccessTransactionStatus.CANCELED);

		long start = System.nanoTime();
		List<RawOrderItem> items = acOrderManager.findNativeOrderItems(randomOres, null, null, null, null, null, 0, -1, null);
		CodeHelper.printMilliSecondTime(start, "Order itemized");
		Assert.assertNotNull(items);
		
		//check the order by
		for(OrderCol col:OrderCol.values()) {
			List<RawOrderItem> rawItems = acOrderManager.findNativeOrderItems(randomOres, null, null, null, null, null,
					0, -1, null, new SortKey(col.sortKey(), false));
			Assert.assertNotNull(rawItems);
		}
	}
	
	@Test
	public void findOrderItems() {
		//create an offer to buy

		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "TestSaveOrder");
		offer = acService.save(offer);
		
		dbInstance.commitAndCloseSession();
		
		//create and save an order
		Order order = acOrderManager.createOrder(ident1);
		OrderPart part = acOrderManager.addOrderPart(order);
		OrderLine line = acOrderManager.addOrderLine(part, offer);
		order = acOrderManager.save(order);
		Assert.assertNotNull(order);
		Assert.assertNotNull(order.getDelivery());
		Assert.assertNotNull(line);
		Assert.assertEquals(ident1, order.getDelivery());

		dbInstance.commitAndCloseSession();
		
		long start = System.nanoTime();
		List<RawOrderItem> items = acOrderManager.findNativeOrderItems(randomOres, null, null, null, null, null, 0, -1, null);
		CodeHelper.printMilliSecondTime(start, "Order itemized");
		Assert.assertNotNull(items);
	}
	
	@Test
	public void testSaveOneClickOrders() {
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer1 = acService.createOffer(randomOres1, "TestSaveOneClickOrders 1");
		offer1 = acService.save(offer1);
		
		OLATResource randomOres2 = createResource();
		Offer offer2 = acService.createOffer(randomOres2, "TestSaveOneClickOrders 2");
		offer2 = acService.save(offer2);
		
		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access1 = acMethodManager.createOfferAccess(offer1, method);
		acMethodManager.save(access1);
		
		OfferAccess access2 = acMethodManager.createOfferAccess(offer2, method);
		acMethodManager.save(access2);

		dbInstance.commitAndCloseSession();
		
		//one clicks
		acOrderManager.saveOneClick(ident1, access1);
		acOrderManager.saveOneClick(ident2, access2);
		acOrderManager.saveOneClick(ident3, access1);
		acOrderManager.saveOneClick(ident3, access2);
		
		dbInstance.commitAndCloseSession();
		
		//retrieves by identity
		List<Order> ordersIdent3 = acOrderManager.findOrdersByDelivery(ident3);
		assertEquals(2, ordersIdent3.size());
		assertEquals(ident3, ordersIdent3.get(0).getDelivery());
		assertEquals(ident3, ordersIdent3.get(1).getDelivery());
		
		//retrieves by resource
		List<Order> ordersResource2 = acOrderManager.findOrdersByResource(randomOres2);
		assertEquals(2, ordersResource2.size());
	}
	
	@Test
	public void testSaveOneClickOrder() {
	//make extensiv test on one order
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer = acService.createOffer(randomOres1, "TestSaveOneClickOrder 1");
		offer = acService.save(offer);

		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		Assert.assertNotNull(methods);
		Assert.assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access1 = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access1);

		dbInstance.commitAndCloseSession();
		
		//one clicks
		Order order = acOrderManager.saveOneClick(ident7, access1);
		
		dbInstance.commitAndCloseSession();
		
		//load order
		Order retrivedOrder = acOrderManager.loadOrderByKey(order.getKey());
		
		Assert.assertNotNull(retrivedOrder);
		Assert.assertNotNull(retrivedOrder.getCreationDate());
		Assert.assertNotNull(retrivedOrder.getDelivery());
		Assert.assertNotNull(retrivedOrder.getOrderNr());
		Assert.assertNotNull(retrivedOrder.getParts());
		
		Assert.assertEquals(ident7, retrivedOrder.getDelivery());
		Assert.assertEquals(1, retrivedOrder.getParts().size());
		
		OrderPart orderPart = retrivedOrder.getParts().get(0);
		Assert.assertNotNull(orderPart);
		Assert.assertEquals(1, orderPart.getOrderLines().size());
		
		OrderLine line = orderPart.getOrderLines().get(0);
		Assert.assertNotNull(line);
		Assert.assertNotNull(line.getOffer());
		Assert.assertEquals(offer, line.getOffer());
	}
	
	@Test
	public void testSaveOneClickOrderWithPrice() {
	//make extensiv test on one order
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer1 = acService.createOffer(randomOres1, "TestSaveOneClickOrder 1");
		Price price1 = new PriceImpl(new BigDecimal("20.00"), "CHF");
		offer1.setPrice(price1);
		offer1 = acService.save(offer1);

		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access1 = acMethodManager.createOfferAccess(offer1, method);
		acMethodManager.save(access1);

		dbInstance.commitAndCloseSession();
		
		//one clicks
		Order order = acOrderManager.saveOneClick(ident7, access1);
		
		dbInstance.commitAndCloseSession();
		
		//load order
		Order retrivedOrder = acOrderManager.loadOrderByKey(order.getKey());
		
		//check order
		assertNotNull(retrivedOrder);
		assertNotNull(retrivedOrder.getCreationDate());
		assertNotNull(retrivedOrder.getDelivery());
		assertNotNull(retrivedOrder.getOrderNr());
		assertNotNull(retrivedOrder.getParts());
		assertNotNull(retrivedOrder.getTotal());
		assertNotNull(retrivedOrder.getTotalOrderLines());

		assertEquals(ident7, retrivedOrder.getDelivery());
		assertEquals(1, retrivedOrder.getParts().size());
		
		assertEquals(price1.getAmount().setScale(2, RoundingMode.HALF_EVEN), retrivedOrder.getTotalOrderLines().getAmount().setScale(2, RoundingMode.HALF_EVEN));
		assertEquals(price1.getCurrencyCode(), retrivedOrder.getTotalOrderLines().getCurrencyCode());
		assertEquals(price1.getAmount().setScale(2, RoundingMode.HALF_EVEN), retrivedOrder.getTotal().getAmount().setScale(2, RoundingMode.HALF_EVEN));
		assertEquals(price1.getCurrencyCode(), retrivedOrder.getTotal().getCurrencyCode());	
		
		//check order part
		OrderPart orderPart = retrivedOrder.getParts().get(0);
		assertNotNull(orderPart);
		assertNotNull(orderPart.getTotal());
		assertNotNull(orderPart.getTotalOrderLines());
		assertEquals(1, orderPart.getOrderLines().size());
		assertEquals(price1.getAmount().setScale(2, RoundingMode.HALF_EVEN), orderPart.getTotalOrderLines().getAmount().setScale(2, RoundingMode.HALF_EVEN));
		assertEquals(price1.getCurrencyCode(), orderPart.getTotalOrderLines().getCurrencyCode());
		assertEquals(price1.getAmount().setScale(2, RoundingMode.HALF_EVEN), orderPart.getTotal().getAmount().setScale(2, RoundingMode.HALF_EVEN));
		assertEquals(price1.getCurrencyCode(), orderPart.getTotal().getCurrencyCode());	
		
		//check order line
		OrderLine line = orderPart.getOrderLines().get(0);
		Assert.assertNotNull(line);
		Assert.assertNotNull(line.getOffer());
		Assert.assertNotNull(line.getUnitPrice());
		Assert.assertNotNull(line.getTotal());
		Assert.assertEquals(offer1, line.getOffer());
		Assert.assertEquals(price1.getAmount().setScale(2, RoundingMode.HALF_EVEN), line.getUnitPrice().getAmount().setScale(2, RoundingMode.HALF_EVEN));
		Assert.assertEquals(price1.getCurrencyCode(), line.getUnitPrice().getCurrencyCode());
		Assert.assertEquals(price1.getAmount().setScale(2, RoundingMode.HALF_EVEN), line.getTotal().getAmount().setScale(2, RoundingMode.HALF_EVEN));
		Assert.assertEquals(price1.getCurrencyCode(), line.getTotal().getCurrencyCode());	
	}
	
	@Test
	public void testLoadBy() {
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer1 = acService.createOffer(randomOres1, "TestLoadBy 1");
		offer1 = acService.save(offer1);
		
		OLATResource randomOres2 = createResource();
		Offer offer2 = acService.createOffer(randomOres2, "TestLoadBy 2");
		offer2 = acService.save(offer2);
		
		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access1 = acMethodManager.createOfferAccess(offer1, method);
		acMethodManager.save(access1);
		
		OfferAccess access2 = acMethodManager.createOfferAccess(offer2, method);
		acMethodManager.save(access2);

		dbInstance.commitAndCloseSession();
		
		//one clicks
		Order order1 = acOrderManager.saveOneClick(ident4, access1);
		Order order2 = acOrderManager.saveOneClick(ident5, access2);
		Order order3_1 = acOrderManager.saveOneClick(ident6, access1);
		Order order3_2 = acOrderManager.saveOneClick(ident6, access2);
		
		dbInstance.commitAndCloseSession();
		
		//load by delivery: ident 1
		List<Order> retrivedOrder1 = acOrderManager.findOrdersByDelivery(ident4);
		assertNotNull(retrivedOrder1);
		assertEquals(1, retrivedOrder1.size());
		assertEquals(order1, retrivedOrder1.get(0));
		
		//load by delivery: ident 2
		List<Order> retrievedOrder2 = acOrderManager.findOrdersByDelivery(ident5);
		assertNotNull(retrievedOrder2);
		assertEquals(1, retrievedOrder2.size());
		assertEquals(order2, retrievedOrder2.get(0));
		
		//load by delivery: ident 3
		List<Order> retrievedOrder3 = acOrderManager.findOrdersByDelivery(ident6);
		assertNotNull(retrievedOrder3);
		assertEquals(2, retrievedOrder3.size());
		assertTrue(order3_1.equals(retrievedOrder3.get(0)) || order3_1.equals(retrievedOrder3.get(1)));
		assertTrue(order3_2.equals(retrievedOrder3.get(0)) || order3_2.equals(retrievedOrder3.get(1)));

		dbInstance.commitAndCloseSession();
		
		//load by resource: ores 1
		List<Order> retrievedOrderOres1 = acOrderManager.findOrdersByResource(randomOres1);
		assertNotNull(retrievedOrderOres1);
		assertEquals(2, retrievedOrderOres1.size());
		assertTrue(order1.equals(retrievedOrderOres1.get(0)) || order1.equals(retrievedOrderOres1.get(1)));
		assertTrue(order3_1.equals(retrievedOrderOres1.get(0)) || order3_1.equals(retrievedOrderOres1.get(1)));
		
		//load by resource: ores 2
		List<Order> retrievedOrderOres2 = acOrderManager.findOrdersByResource(randomOres2);
		assertNotNull(retrievedOrderOres2);
		assertEquals(2, retrievedOrderOres2.size());
		assertTrue(order2.equals(retrievedOrderOres2.get(0)) || order2.equals(retrievedOrderOres2.get(1)));
		assertTrue(order3_2.equals(retrievedOrderOres2.get(0)) || order3_2.equals(retrievedOrderOres2.get(1)));
		
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testDeleteResource() {
		//create some offers to buy
		OLATResource randomOres1 = createResource();
		Offer offer1 = acService.createOffer(randomOres1, "TestDeleteResource 1");
		offer1 = acService.save(offer1);

		dbInstance.commitAndCloseSession();
		
		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		AccessMethod method = methods.get(0);
		
		OfferAccess access = acMethodManager.createOfferAccess(offer1, method);
		acMethodManager.save(access);

		dbInstance.commitAndCloseSession();
		
		//save an order
		Order order1 = acOrderManager.saveOneClick(ident8, access);
		dbInstance.commitAndCloseSession();

		//delete the resource
		randomOres1 = dbInstance.getCurrentEntityManager().find(OLATResourceImpl.class, randomOres1.getKey());
		dbInstance.deleteObject(randomOres1);
		
		dbInstance.commitAndCloseSession();
		
		//load order
		Order retrievedOrder1 = acOrderManager.loadOrderByKey(order1.getKey());
		assertNotNull(retrievedOrder1);
	}
	
	private OLATResource createResource() {
		//create a repository entry
		OLATResourceable resourceable = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource r =  resourceManager.createOLATResourceInstance(resourceable);
		dbInstance.saveObject(r);
		return r;
	}
}
