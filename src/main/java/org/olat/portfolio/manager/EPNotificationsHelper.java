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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.portfolio.model.structel.EPMapShort;
import org.olat.portfolio.ui.structel.view.EPChangelogController;

/**
 * 
 * EPNotificationsHelper provides functionality to gather SubscriptionListItems
 * for given Maps.<br />
 * 
 * FXOLAT-431, FXOLAT-432<br />
 * this also triggered: OO-111
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 * 
 */
public class EPNotificationsHelper {

//	private static final Logger logger = Tracing.createLoggerFor(EPNotificationsHelper.class);
	private Translator translator;
	private String rootBusinessPath;

	/**
	 * sets up the helper. provide a locale and an Identity
	 * 
	 * @param locale
	 * @param identity
	 */
	public EPNotificationsHelper(String rootBusinessPath, Locale locale) {
		this.translator = Util.createPackageTranslator(EPChangelogController.class, locale);
		this.rootBusinessPath = rootBusinessPath;
	}
	
	/**
	 * 
	 * @param compareDate
	 * @param map
	 * @return
	 */
	public List<SubscriptionListItem> getAllSubscrItemsDefault(Date compareDate, EPMapShort map) {
		EPNotificationManager mgr = CoreSpringFactory.getImpl(EPNotificationManager.class);
		List<SubscriptionListItem> allItems = new ArrayList<SubscriptionListItem>();
		List<Long> mapKeys = Collections.singletonList(map.getKey());
		//structure elements
		List<SubscriptionListItem> notis1 = mgr.getPageSubscriptionListItem(map.getKey(), rootBusinessPath, compareDate, translator);	
		allItems.addAll(notis1);
		//artefacts
		List<SubscriptionListItem> notis2 = mgr.getArtefactNotifications(mapKeys, rootBusinessPath, compareDate, translator);
		allItems.addAll(notis2);
		//ratings
		List<SubscriptionListItem> notis3 = mgr.getRatingNotifications(mapKeys, rootBusinessPath, compareDate, translator);
		allItems.addAll(notis3);
		//comments
		List<SubscriptionListItem> notis4 = mgr.getCommentNotifications(mapKeys, rootBusinessPath, compareDate, translator);
		allItems.addAll(notis4);

		//sort
		Collections.sort(allItems, new SubscriptionListItemComparator());
		return allItems;
	}
	
	public List<SubscriptionListItem> getAllSubscrItemsStructured(Date compareDate, EPMapShort map) {
		EPNotificationManager mgr = CoreSpringFactory.getImpl(EPNotificationManager.class);
		// at this moment, map is not yet synchronized. check the "parent"
		// templateMap for map/structure changes
	
		List<SubscriptionListItem> allItems = new ArrayList<SubscriptionListItem>();

		//structure elements
		List<SubscriptionListItem> notis1 = mgr.getPageSubscriptionListItem(map.getSourceMapKey(), rootBusinessPath, compareDate, translator);	
		allItems.addAll(notis1);
		
		List<Long> mapKeys = new ArrayList<Long>();
		mapKeys.add(map.getKey());
		mapKeys.add(map.getSourceMapKey());
		
		//artefacts
		List<SubscriptionListItem> notis2 = mgr.getArtefactNotifications(mapKeys, rootBusinessPath, compareDate, translator);
		allItems.addAll(notis2);
		//ratings
		List<SubscriptionListItem> notis3 = mgr.getRatingNotifications(mapKeys, rootBusinessPath, compareDate, translator);
		allItems.addAll(notis3);
		//comments
		List<SubscriptionListItem> notis4 = mgr.getCommentNotifications(mapKeys, rootBusinessPath, compareDate, translator);
		allItems.addAll(notis4);

		//sort
		Collections.sort(allItems, new SubscriptionListItemComparator());
		return allItems;
	}

	/**
	 * compares two SubscriptionListItems according to their date.
	 * 
	 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
	 * 
	 */
	private class SubscriptionListItemComparator implements Comparator<SubscriptionListItem> {

		@Override
		public int compare(SubscriptionListItem o1, SubscriptionListItem o2) {
			if (o1.getDate().after(o2.getDate()))
				return -1;
			return 1;
		}

	}

}
