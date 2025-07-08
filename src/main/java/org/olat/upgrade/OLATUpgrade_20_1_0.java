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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherChannel;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.manager.PublisherDAO;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.topicbroker.TBEnrollmentStrategyType;
import org.olat.modules.webFeed.manager.BlogCommentNotificationsHandler;
import org.olat.modules.webFeed.manager.PodcastCommentNotificationsHandler;
import org.olat.repository.LifecycleModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Apr 10, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OLATUpgrade_20_1_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_1_0.class);

	private static final String VERSION = "OLAT_20.1.0";
	private static final String MIGRATE_LIFECYCLE_ENABLED = "MIGRATE_LIFECYCLE_ENABLED";
	private static final String UPDATE_BADGE_CLASSES = "UPDATE BADGE CLASSES";
	private static final String MIGRATE_FEED_PUBLISHERS = "MIGRATE FEED PUBLISHERS";
	private static final String UPDATE_BROKER_AUTO_STRATEGY = "UPDATE BROKER AUTO STRATEGY";

	@Autowired
	private DB dbInstance;
	@Autowired
	private PublisherDAO publisherDao;
	@Autowired
	private LifecycleModule lifecycleModule;
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
		allOk &= migrateLifecycleModuleSetting(upgradeManager, uhd);
		allOk &= updateBadgeClasses(upgradeManager, uhd);
		allOk &= migrateFeedPublishers(upgradeManager, uhd);
		allOk &= updateTopicBrokerAutoStrategy(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_1_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_1_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean migrateLifecycleModuleSetting(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(MIGRATE_LIFECYCLE_ENABLED)) {
			try {
				boolean exists = !dbInstance.getCurrentEntityManager()
						.createQuery("select rel.key from repositoryentrylifecycle rel", Long.class)
						.setMaxResults(1)
						.getResultList().isEmpty();

				lifecycleModule.setEnabled(exists);
				log.info("Set lifecycle module enabled = {}", exists);

				uhd.setBooleanDataValue(MIGRATE_LIFECYCLE_ENABLED, Boolean.TRUE);
				upgradeManager.setUpgradesHistory(uhd, VERSION);
			} catch (Exception e) {
				log.error("Error migrating lifecycle.enabled setting", e);
				return false;
			}
		}
		return true;
	}
	
	private boolean updateBadgeClasses(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(UPDATE_BADGE_CLASSES)) {
			try {
				log.info("Updating badge classes");

				String updateQuery = "update badgeclass set rootId = uuid, versionType = 'current', version = '1' where rootId is null";
				int updateCount = dbInstance.getCurrentEntityManager().createQuery(updateQuery).executeUpdate();
				dbInstance.commitAndCloseSession();
				log.info("rootId, version and versionType set for {} badge class instances", updateCount);
				
				OpenBadgesManager openBadgesManager = CoreSpringFactory.getImpl(OpenBadgesManager.class);
				updateCount = openBadgesManager.upgradeBadgeDependencyConditions();
				log.info("Badge dependency conditions updated for {} badge class instances", updateCount);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(UPDATE_BADGE_CLASSES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private boolean migrateFeedPublishers(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(MIGRATE_FEED_PUBLISHERS)) {
			try {
				log.info("Migrate feed publishers");
				AtomicInteger count = new AtomicInteger(0);
				
				allOk = migrateFeedPublishers(BlogFileResource.TYPE_NAME, BlogCommentNotificationsHandler.TYPE, count);
				allOk = migrateFeedPublishers(PodcastFileResource.TYPE_NAME, PodcastCommentNotificationsHandler.TYPE, count);

				log.info("End migration of feed publishers ({} new comments publishers)", count.get());
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(MIGRATE_FEED_PUBLISHERS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}

	private boolean migrateFeedPublishers(String feedType, String commentType, AtomicInteger count) {
		List<Publisher> feedPublishers = publisherDao.getPublisherByType(feedType);
		List<Publisher> commentPublishers = publisherDao.getPublisherByType(commentType);
		List<Publisher> feedPublisherWithComments = commentPublishers.stream()
				.map(Publisher::getParentPublisher)
				.toList();
		feedPublishers.removeAll(feedPublisherWithComments);
		
		for(Publisher feedPublisher:feedPublishers) {
			SubscriptionContext subsContext = new SubscriptionContext(feedPublisher.getResName(), feedPublisher.getResId(), feedPublisher.getSubidentifier());
			String commentBusinessPath = feedPublisher.getBusinessPath() + "[Comment:0]";
			PublisherData commentData = new PublisherData(commentType, feedPublisher.getData(), commentBusinessPath);
			Publisher commentPublisher = notificationsManager.getOrCreatePublisherWithData(subsContext, commentData, feedPublisher, PublisherChannel.PULL);
			dbInstance.commit();
			copySubscribers(feedPublisher, commentPublisher);
			dbInstance.commitAndCloseSession();
			count.incrementAndGet();
		}
		dbInstance.commitAndCloseSession();
		return true;
	}
	
	private void copySubscribers(Publisher feedPublisher, Publisher commentPublisher) {
		List<Subscriber> subscribers = getSubscribers(feedPublisher);
		for(Subscriber subscriber:subscribers) {
			Identity identity = subscriber.getIdentity();
			Subscriber commentSubscriber = notificationsManager.getSubscriber(identity, commentPublisher);
			if(commentSubscriber == null) {
				notificationsManager.subscribe(identity, commentPublisher);
				if(!subscriber.isEnabled()) {
					notificationsManager.unsubscribe(identity, commentPublisher);
				}
			}
			dbInstance.commitAndCloseSession();
		}
	}
	
	private List<Subscriber> getSubscribers(Publisher publisher) {
		String sb = """
			select sub from notisub sub
			left join fetch sub.identity subIdent
			where sub.publisher.key=:publisherKey""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Subscriber.class)
				.setParameter("publisherKey", publisher.getKey())
				.getResultList();
	}
	
	private boolean updateTopicBrokerAutoStrategy(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(UPDATE_BROKER_AUTO_STRATEGY)) {
			try {
				log.info("Updating topic broker auto strategy");
				
				String updateQuery = "update topicbrokerbroker set autoEnrollmentStrategyType = '" + TBEnrollmentStrategyType.maxPriorities.name() + "' where autoEnrollment = true";
				int updateCount = dbInstance.getCurrentEntityManager().createQuery(updateQuery).executeUpdate();
				dbInstance.commitAndCloseSession();
				
				log.info("Updating topic broker auto strategy for {} topic brokers", updateCount);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(UPDATE_BROKER_AUTO_STRATEGY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
}
