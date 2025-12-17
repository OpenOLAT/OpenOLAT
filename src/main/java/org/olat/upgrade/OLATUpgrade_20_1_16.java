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

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.SubscriptionMail;
import org.olat.core.commons.services.notifications.manager.SubscriptionMailDAO;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.model.DBMailRecipient;
import org.olat.properties.Property;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 déc. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_20_1_16 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_1_16.class);

	private static final int BATCH_SIZE = 5000;
	private static final String VERSION = "OLAT_20.1.16";
	private static final String MIGRATION_MAIL_PUBLISHER = "MIGRATION MAIL PUBLISHER";
	private static final String MIGRATION_LAST_MAIL = "MIGRATION LAST MAIL";
	private static final String MIGRATION_LAST_MAIL_SUBSCRIBER = "MIGRATION LAST MAIL BY SUBSCRIBER";

	@Autowired
	private DB dbInstance;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private SubscriptionMailDAO subscriptionMailDao;
	@Autowired
	private NotificationsManager notificationsManager;
	
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
		allOk &= migrateMailPublisher(upgradeManager, uhd);
		allOk &= migrateLastMailWithProperty(upgradeManager, uhd);
		allOk &= migrateLastMailWithSubscriber(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_1_16 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_1_16 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean migrateLastMailWithProperty(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(MIGRATION_LAST_MAIL)) {
			try {

				log.info("Migration of last notification mail date");
				
				int count = 0;
				List<Property> props = getLastMailProperties(0, BATCH_SIZE);
				do {
					for(Property prop:props) {
						Identity identity = prop.getIdentity();
						SubscriptionMail mail = subscriptionMailDao.loadByIdentity(identity);
						if(mail == null) {
							Date lastMail = new Date(prop.getLongValue());
							createSubscriptionMail(identity, lastMail);
						}
						
						if(count++ % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					props = getLastMailProperties(count, BATCH_SIZE);
					
				} while(!props.isEmpty());
				
				log.info("End migration of last notification mail date: {}", count);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(MIGRATION_LAST_MAIL, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private List<Property> getLastMailProperties(int firstResult, int maxResults) {
		String sb = """
			select v from property as v
			inner join fetch v.identity as ident
			inner join fetch ident.user as identUser
			where v.name='noti_latest_email'
			order by v.key asc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, Property.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private boolean migrateLastMailWithSubscriber(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(MIGRATION_LAST_MAIL_SUBSCRIBER)) {
			try {

				log.info("Migration of last notification mail date by subscriber");
				
				int count = 0;
				List<LastMailSubscriber> props = getLastMailSubscribers(0, BATCH_SIZE);
				do {
					for(LastMailSubscriber prop:props) {
						if(prop.lastMail() == null) continue;
						
						Identity identity = prop.identity();
						SubscriptionMail mail = subscriptionMailDao.loadByIdentity(identity);
						if(mail == null) {
							createSubscriptionMail(identity, prop.lastMail());
						}
						
						if(count++ % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					props = getLastMailSubscribers(count, BATCH_SIZE);
					
				} while(!props.isEmpty());
				
				log.info("End migration of last notification mail date by subscriber: {}", count);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(MIGRATION_LAST_MAIL_SUBSCRIBER, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private void createSubscriptionMail(Identity identity, Date lastMail) {
		Date nextMail;
		String interval = identity.getUser().getPreferences().getNotificationInterval();
		if("never".equals(interval) || !StringHelper.containsNonWhitespace(interval)) {
			nextMail = null;
		} else {
			nextMail = notificationsManager.getNextMail(lastMail, interval);
		}
		subscriptionMailDao.create(identity, lastMail, nextMail);
	}
	
	private List<LastMailSubscriber> getLastMailSubscribers(int firstResult, int maxResults) {
		String sb = """
			select new LastMailSubscriber(ident, max(sub.latestEmailed)) from notisub as sub
			inner join sub.identity as ident
			group by ident.key
			order by ident.key asc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, LastMailSubscriber.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	public record LastMailSubscriber(Identity identity, Date lastMail) {
		//
	}

	private boolean migrateMailPublisher(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(MIGRATION_MAIL_PUBLISHER)) {
			try {
				int count = 0;
				log.info("Migration of mail publisher");
				SubscriptionContext sharedContext = new SubscriptionContext("Inbox", 0l, "");
				Publisher publisher = notificationsManager.getPublisher(sharedContext);
				if(publisher != null) {
					List<Subscriber> subscribers = getSubscribers(publisher, count, BATCH_SIZE);
					do {
						for(Subscriber subscriber: subscribers) {
							Identity identity = subscriber.getIdentity();
							SubscriptionContext idContext = mailManager.getSubscriptionContext(identity);
							Publisher idPublisher = notificationsManager.getPublisher(idContext);
							if(idPublisher == null) {
								PublisherData idData = mailManager.getPublisherData(identity);
								Subscriber idSubscriber = notificationsManager.subscribe(identity, idContext, idData);
								if(!subscriber.isEnabled()) {
									notificationsManager.updateSubscriber(idSubscriber, false);
								}
								Date lastNews = getLastNews(identity);
								if(lastNews != null && (idSubscriber.getPublisher().getLatestNewsDate() == null
										|| lastNews.before(idSubscriber.getPublisher().getLatestNewsDate()))) {
									idPublisher = idSubscriber.getPublisher();
									idPublisher.setLatestNewsDate(lastNews);
									dbInstance.getCurrentEntityManager().merge(idPublisher);
								}
							}
							
							if(count++ % 25 == 0) {
								dbInstance.commitAndCloseSession();
							}
						}
						subscribers = getSubscribers(publisher, count, BATCH_SIZE);
					} while(!subscribers.isEmpty());

					dbInstance.commitAndCloseSession();
					
					notificationsManager.deactivate(publisher);
					dbInstance.commitAndCloseSession();
				}
				
				log.info("End migration of mail publisher: {} changed", count);
				allOk = true;
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(MIGRATION_MAIL_PUBLISHER, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private Date getLastNews(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select max(recipient.creationDate) from ").append(DBMailRecipient.class.getSimpleName()).append(" recipient")
		  .append(" where recipient.recipient.key=:identityKey");
		List<Date> dates = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Date.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return dates != null && !dates.isEmpty() ? dates.get(0) : null;
	}
	
	private List<Subscriber> getSubscribers(Publisher publisher, int firstResult, int maxResults) {
		String sb = """
			select sub from notisub sub
			inner join fetch sub.identity as ident
			where sub.publisher.key=:publisherKey
			order by sub.key asc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Subscriber.class)
				.setParameter("publisherKey", publisher.getKey())
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
}
