/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.resource.accesscontrol.OrderLine;
import org.olat.resource.accesscontrol.model.OfferImpl;
import org.olat.resource.accesscontrol.model.OrderLineImpl;
import org.olat.resource.accesscontrol.model.OrderPartImpl;
import org.olat.resource.accesscontrol.provider.invoice.model.InvoiceAccessMethod;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 2 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_21_1_2 extends OLATUpgrade {
	
	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_21_1_2.class);
	
	private static final String VERSION = "OLAT_21.1.2";
	
	private static final String DISABLE_CANCELLING_NOT_INVOICE_OFFERS = "DISABLE CANCELLING NOT INVOICE OFFERS";
	private static final String DISABLE_CANCELLING_NOT_INVOICE_ORDERS = "DISABLE CANCELLING NOT INVOICE ORDERS";
	private static final String OFFERS_LAST_KEY = "DISABLE CANCELLING NOT INVOICE OFFERS LAST KEY";
	private static final String OFFERS_MAX_KEY = "DISABLE CANCELLING NOT INVOICE OFFERS MAX KEY";
	private static final String ORDER_PARTS_LAST_KEY = "DISABLE CANCELLING NOT INVOICE ORDER PARTS LAST KEY";
	private static final String ORDER_PARTS_MAX_KEY = "DISABLE CANCELLING NOT INVOICE ORDER PARTS MAX KEY";
	
	private static final int BATCH_SIZE = 1000;
	private static final String NOT_INVOICE_OFFER = " not exists (select access.key from acofferaccess access"
			+ " inner join access.method method"
			+ " where access.offer.key=offer.key and type(method)=" + InvoiceAccessMethod.class.getName() + ")";
	
	@Autowired
	private DB dbInstance;
	
	@Override
	public String getVersion() {
		return VERSION;
	}
	
	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}
		
		boolean allOk = true;
		allOk &= disableCancellingNotInvoiceOffers(upgradeManager, uhd);
		allOk &= disableCancellingNotInvoiceOrders(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		
		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_21_1_2 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_21_1_2 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean disableCancellingNotInvoiceOffers(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(DISABLE_CANCELLING_NOT_INVOICE_OFFERS)) {
			try {
				log.info("Disable cancelling of offers without invoice method");
				
				Long maxKey = getOrInitMaxKey(upgradeManager, uhd, OFFERS_MAX_KEY, "select max(offer.key) from acoffer offer");
				Long lastKey = getLastKey(uhd, OFFERS_LAST_KEY);
				long total = countCancellingNotInvoiceOffers(lastKey, maxKey);
				int count = 0;
				List<OfferImpl> offers = loadCancellingNotInvoiceOffers(lastKey, maxKey);
				while (!offers.isEmpty()) {
					for (OfferImpl offer : offers) {
						offer.setCancellingEnabled(false);
						dbInstance.getCurrentEntityManager().merge(offer);
						lastKey = offer.getKey();
						count++;
					}
					log.info("Disable cancelling of offers without invoice method: {} / {}", count, total);
					dbInstance.commitAndCloseSession();
					saveLastKey(upgradeManager, uhd, OFFERS_LAST_KEY, lastKey);
					offers = loadCancellingNotInvoiceOffers(lastKey, maxKey);
				}
				
				log.info("End disable cancelling of offers without invoice method: {}", count);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(DISABLE_CANCELLING_NOT_INVOICE_OFFERS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private long countCancellingNotInvoiceOffers(Long lastKey, Long maxKey) {
		String query = "select count(offer.key) from acoffer offer"
				+ " where offer.key>:lastKey and offer.key<=:maxKey and offer.cancellingEnabled=true"
				+ " and" + NOT_INVOICE_OFFER;
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("lastKey", lastKey)
				.setParameter("maxKey", maxKey)
				.getSingleResult().longValue();
	}
	
	private List<OfferImpl> loadCancellingNotInvoiceOffers(Long lastKey, Long maxKey) {
		String query = "select offer from acoffer offer"
				+ " where offer.key>:lastKey and offer.key<=:maxKey and offer.cancellingEnabled=true"
				+ " and" + NOT_INVOICE_OFFER
				+ " order by offer.key";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, OfferImpl.class)
				.setParameter("lastKey", lastKey)
				.setParameter("maxKey", maxKey)
				.setMaxResults(BATCH_SIZE)
				.getResultList();
	}
	
	private boolean disableCancellingNotInvoiceOrders(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(DISABLE_CANCELLING_NOT_INVOICE_ORDERS)) {
			try {
				log.info("Disable cancelling of orders without invoice method");
				
				Long maxKey = getOrInitMaxKey(upgradeManager, uhd, ORDER_PARTS_MAX_KEY, "select max(part.key) from acorderpart part");
				Long lastKey = getLastKey(uhd, ORDER_PARTS_LAST_KEY);
				long total = countCancellingNotInvoiceOrderLines(lastKey, maxKey);
				int count = 0;
				List<Long> partKeys = loadCancellingNotInvoiceOrderPartKeys(lastKey, maxKey);
				while (!partKeys.isEmpty()) {
					Set<Long> lineKeys = new HashSet<>(loadCancellingNotInvoiceOrderLineKeys(partKeys));
					for (Long partKey : partKeys) {
						OrderPartImpl part = dbInstance.getCurrentEntityManager().find(OrderPartImpl.class, partKey);
						boolean partCancellingEnabled = false;
						for (OrderLine line : part.getOrderLines()) {
							if (line instanceof OrderLineImpl lineImpl && lineKeys.contains(lineImpl.getKey())) {
								lineImpl.setCancellingEnabled(false);
								dbInstance.getCurrentEntityManager().merge(lineImpl);
								count++;
							}
							partCancellingEnabled |= line.isCancellingEnabled();
						}
						part.setCancellingEnabled(partCancellingEnabled);
						dbInstance.getCurrentEntityManager().merge(part);
						lastKey = partKey;
					}
					log.info("Disable cancelling of order lines without invoice method: {} / {}", count, total);
					dbInstance.commitAndCloseSession();
					saveLastKey(upgradeManager, uhd, ORDER_PARTS_LAST_KEY, lastKey);
					partKeys = loadCancellingNotInvoiceOrderPartKeys(lastKey, maxKey);
				}
				
				log.info("End disable cancelling of orders without invoice method: {} order lines", count);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(DISABLE_CANCELLING_NOT_INVOICE_ORDERS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private long countCancellingNotInvoiceOrderLines(Long lastKey, Long maxKey) {
		String query = "select count(line.key) from acorderpart part"
				+ " inner join part.lines line"
				+ " inner join line.offer offer"
				+ " where part.key>:lastKey and part.key<=:maxKey and line.cancellingEnabled=true"
				+ " and" + NOT_INVOICE_OFFER;
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("lastKey", lastKey)
				.setParameter("maxKey", maxKey)
				.getSingleResult().longValue();
	}
	
	private List<Long> loadCancellingNotInvoiceOrderPartKeys(Long lastKey, Long maxKey) {
		String query = "select distinct part.key from acorderpart part"
				+ " inner join part.lines line"
				+ " inner join line.offer offer"
				+ " where part.key>:lastKey and part.key<=:maxKey and line.cancellingEnabled=true"
				+ " and" + NOT_INVOICE_OFFER
				+ " order by part.key";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("lastKey", lastKey)
				.setParameter("maxKey", maxKey)
				.setMaxResults(BATCH_SIZE)
				.getResultList();
	}
	
	private List<Long> loadCancellingNotInvoiceOrderLineKeys(List<Long> partKeys) {
		String query = "select line.key from acorderpart part"
				+ " inner join part.lines line"
				+ " inner join line.offer offer"
				+ " where part.key in (:partKeys) and line.cancellingEnabled=true"
				+ " and" + NOT_INVOICE_OFFER;
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("partKeys", partKeys)
				.getResultList();
	}
	
	/**
	 * The upper bound is fixed at the first run. A restart must not touch records
	 * created after the upgrade started, e.g. offers set to cancelable by an author.
	 */
	private Long getOrInitMaxKey(UpgradeManager upgradeManager, UpgradeHistoryData uhd, String dataKey, String query) {
		if (uhd.getDataValue(dataKey) instanceof Long maxKey) {
			return maxKey;
		}
		Long maxKey = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.getSingleResult();
		if (maxKey == null) {
			maxKey = Long.valueOf(0);
		}
		uhd.setDataValue(dataKey, maxKey);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		return maxKey;
	}
	
	private Long getLastKey(UpgradeHistoryData uhd, String dataKey) {
		return uhd.getDataValue(dataKey) instanceof Long lastKey ? lastKey : Long.valueOf(0);
	}
	
	private void saveLastKey(UpgradeManager upgradeManager, UpgradeHistoryData uhd, String dataKey, Long lastKey) {
		uhd.setDataValue(dataKey, lastKey);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
	}
}
