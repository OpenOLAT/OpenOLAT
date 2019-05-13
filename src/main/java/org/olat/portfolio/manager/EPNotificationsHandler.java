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
package org.olat.portfolio.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.portfolio.model.structel.EPMapShort;
import org.olat.portfolio.model.structel.EPStructuredMap;

/**
 * 
 * The NotificationsHandler for the ePortfolio<br />
 * ( bean definition in "portfolioContext.xml" )
 * 
 * 
 * FXOLAT-431, FXOLAT-432
 * 
 * @author strentini, sergio.trentini@frentix.com, www.frentix.com
 * 
 */
public class EPNotificationsHandler implements NotificationsHandler {

	private static final Logger logger = Tracing.createLoggerFor(EPNotificationsHandler.class);

	public static final String TYPENNAME = EPStructuredMap.class.getSimpleName();

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		SubscriptionInfo si = null;

		Publisher publisher = subscriber.getPublisher();
		EPFrontendManager epMgr = CoreSpringFactory.getImpl(EPFrontendManager.class);
		EPMapShort amap = epMgr.loadMapShortByResourceId(publisher.getResId());

		if (isInkoveValid(amap, compareDate, publisher)) {

			// init the helper;
			String rootBusinessPath = "[EPDefaultMap:" + amap.getKey() + "]";
			EPNotificationsHelper helper = new EPNotificationsHelper(rootBusinessPath, locale);
			String resName = amap.getOlatResource().getResourceableTypeName();

			si = new SubscriptionInfo(subscriber.getKey(), publisher.getType(), getTitleItemForMap(amap), null);

			List<SubscriptionListItem> allItems = new ArrayList<>(0);
			// get subscriptionListItems according to map type
			if ("EPDefaultMap".equals(resName) || "EPStructuredMapTemplate".equals(resName)) {
				allItems = helper.getAllSubscrItemsDefault(compareDate, amap);
			} else if ("EPStructuredMap".equals(resName)) {
				allItems = helper.getAllSubscrItemsStructured(compareDate, amap);
			}

			for (SubscriptionListItem item : allItems) {
				si.addSubscriptionListItem(item);
			}
		}

		if (si == null) {
			// no info, return empty
			si = NotificationsManager.getInstance().getNoSubscriptionInfo();
		}

		return si;
	}

	private boolean isInkoveValid(EPMapShort map, Date compareDate, Publisher publisher) {
		// only do that if a map was found.
		// OO-191 only do if compareDate is not null.
		return (map != null && compareDate != null && NotificationsManager.getInstance().isPublisherValid(publisher));
	}

	/**
	 * returns a TitleItem instance for the given Publisher p If you already
	 * have a reference to the map, use
	 * <code>getTitleItemForMap(EPAbstractMap amap)</code>
	 * 
	 * @param p
	 * @return
	 */
	private TitleItem getTitleItemForPublisher(Publisher p) {
		Long resId = p.getResId();
		logger.debug("loading map with resourceableid: {}", resId);

		EPFrontendManager epMgr = CoreSpringFactory.getImpl(EPFrontendManager.class);
		EPMapShort map = epMgr.loadMapShortByResourceId(resId);
		return getTitleItemForMap(map);
	}

	/**
	 * returns a TitleItem instance for the given AbstractMap
	 * 
	 * @param amap
	 * @return
	 */
	private TitleItem getTitleItemForMap(EPMapShort amap) {
		StringBuilder sbTitle = new StringBuilder();
		if (amap != null) {
			sbTitle.append(StringHelper.escapeHtml(amap.getTitle()));
			EPFrontendManager epMgr = CoreSpringFactory.getImpl(EPFrontendManager.class);
			String firstOwner = epMgr.getFirstOwnerAsString(amap);
			sbTitle.append(" (").append(StringHelper.escapeHtml(firstOwner)).append(")");
		}
		return new TitleItem(sbTitle.toString(), "o_EPStructuredMapTemplate_icon");
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		TitleItem title = getTitleItemForPublisher(subscriber.getPublisher());
		return title.getInfoContent("text/plain");
	}

	@Override
	public String getType() {
		return TYPENNAME;
	}

}
