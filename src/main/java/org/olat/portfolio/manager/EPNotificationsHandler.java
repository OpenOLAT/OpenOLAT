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

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.notifications.NotificationsHandler;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.notifications.SubscriptionInfo;
import org.olat.core.util.notifications.items.SubscriptionListItem;
import org.olat.core.util.notifications.items.TitleItem;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.EPDefaultMap;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;

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

	private static OLog logger = Tracing.createLoggerFor(EPNotificationsHandler.class);

	public static final String TYPENNAME = EPStructuredMap.class.getSimpleName();

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		SubscriptionInfo si = null;

		Publisher p = subscriber.getPublisher();
		EPAbstractMap amap = EPNotificationsHelper.findMapOfAnyType(p.getResId());

		if (amap != null) {
			// only do that if a map was found.

			// init the helper;
			EPNotificationsHelper helper = new EPNotificationsHelper(p.getBusinessPath(), locale, subscriber.getIdentity());

			// gather all changes for the current map
			if (NotificationsManager.getInstance().isPublisherValid(p)) {
				si = new SubscriptionInfo(subscriber.getKey(), p.getType(), getTitleItem(amap), null);

				List<SubscriptionListItem> allItems = new ArrayList<SubscriptionListItem>(0);
				// get subscriptionListItems according to map type
				if (amap instanceof EPDefaultMap || amap instanceof EPStructuredMapTemplate) {
					allItems = helper.getAllSubscrItems_Default(compareDate, amap);
				} else if (amap instanceof EPStructuredMap) {
					allItems = helper.getAllSubscrItems_Structured(compareDate, (EPStructuredMap) amap);
				}

				for (SubscriptionListItem item : allItems) {
					si.addSubscriptionListItem(item);
				}
			}
		}

		if (si == null) {
			// no info, return empty
			si = NotificationsManager.getInstance().getNoSubscriptionInfo();
		}

		return si;
	}

	/**
	 * returns a TitleItem instance for the given Publisher p If you already
	 * have a reference to the map, use
	 * <code>getTitleItem(EPAbstractMap amap)</code>
	 * 
	 * @param p
	 * @return
	 */
	private TitleItem getTitleItem(Publisher p) {
		Long resId = p.getResId();
		if (logger.isDebug())
			logger.debug("loading map with resourceableid: " + resId);

		EPAbstractMap map = EPNotificationsHelper.findMapOfAnyType(resId);
		return getTitleItem(map);
	}

	/**
	 * returns a TitleItem instance for the given AbstractMap
	 * 
	 * @param amap
	 * @return
	 */
	private TitleItem getTitleItem(EPAbstractMap amap) {
		StringBuilder sbTitle = new StringBuilder();
		if (amap != null) {
			sbTitle.append(amap.getTitle());
			sbTitle.append(" <span class=\"o_ep_notif_owner\">(").append(EPFrontendManager.getFirstOwnerAsString(amap));
			sbTitle.append(")</span> ");
			if (amap instanceof EPDefaultMap) {
				// title is ok as it is
			} else if (amap instanceof EPStructuredMap) {
				// title += " (Portfolioaufgabe)";
			} else if (amap instanceof EPStructuredMapTemplate) {
				// sbTitle.append(" (Portfoliovorlage)");
			}
		}
		return new TitleItem(sbTitle.toString(), "o_ep_icon");
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		TitleItem title = getTitleItem(subscriber.getPublisher());
		return title.getInfoContent("text/plain");
	}

	@Override
	public String getType() {
		return TYPENNAME;
	}

}
