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
package org.olat.core.commons.services.notifications.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherChannel;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.SubscriptionItem;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This controller shows the list of the news generated by the users
 * subscriptions. The news interval can be changed by the user by setting an
 * appropriate date. To manage the users subscription the
 * NotificationSubscriptionController can be used.
 * 
 * <P>
 * Initial Date: 22.12.2009 <br>
 * 
 * @author gnaegi
 */
public class NotificationNewsController extends BasicController implements Activateable2 {
	
	private VelocityContainer newsVC;
	private Date compareDate;
	private String newsType;
	private Identity subscriberIdentity;
	private DateChooserController dateChooserCtr;
	private Link emailLink;
	private Map<Subscriber, SubscriptionInfo> subsInfoMap;
	
	@Autowired
	private NotificationsManager notificationsManager;

	/**
	 * Constructor
	 * 
	 * @param subscriberIdentity
	 *          The identity which news are displayed
	 * @param ureq
	 * @param wControl
	 * @param newsSinceDate
	 *          The lower date boundary to collect the news or NULL to use the
	 *          user defined notification interval
	 */
	public NotificationNewsController(Identity subscriberIdentity, UserRequest ureq,
			WindowControl wControl, Date newsSinceDate) {
		super(ureq, wControl);
		this.subscriberIdentity = subscriberIdentity;
		if (newsSinceDate == null) {
			compareDate = notificationsManager.getCompareDateFromInterval(notificationsManager
					.getUserIntervalOrDefault(ureq.getIdentity()));
		} else {
			compareDate = newsSinceDate;
		}
		compareDate = CalendarUtils.removeTime(compareDate);
		
		// Main view is a velocity container
		newsVC = createVelocityContainer("notificationsNews");
		// Fetch data from DB and update datamodel and reuse subscribers
		List<Subscriber> subs = updateNewsDataModel();
		// Add date and type chooser
		dateChooserCtr = new DateChooserController(ureq, getWindowControl(), compareDate);
		dateChooserCtr.setSubscribers(subs);
		listenTo(dateChooserCtr);
		newsVC.put("dateChooserCtr", dateChooserCtr.getInitialComponent());
		// Add email link
		boolean userHasEmailAddress = StringHelper.containsNonWhitespace(ureq.getIdentity().getUser().getEmail());
		if (userHasEmailAddress) {
			emailLink = LinkFactory.createButton("emailLink", newsVC, this);
		}
		//
		putInitialPanel(newsVC);
	}

	/**
	 * Update the new data model and refresh the GUI
	 */
	protected List<Subscriber> updateNewsDataModel() {
		if(compareDate == null) {
			return Collections.emptyList();//compare date is mandatory
		}
		List<String> notiTypes = new ArrayList<>();
		if (StringHelper.containsNonWhitespace(newsType) && !newsType.equals("all")) {
			notiTypes.add(newsType);
		}

		List<Subscriber> subs = notificationsManager.getSubscribers(subscriberIdentity, notiTypes, PublisherChannel.PULL, true, true);

		newsVC.contextPut("subs", subs);
		subsInfoMap = NotificationHelper.getSubscriptionMap(getLocale(), true,
				compareDate, subs);
		NotificationSubscriptionAndNewsFormatter subsFormatter = new NotificationSubscriptionAndNewsFormatter(
				getTranslator(), subsInfoMap);
		newsVC.contextPut("subsFormatter", subsFormatter);
		return subs;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dateChooserCtr) {
			if (event == Event.CHANGED_EVENT) {
				compareDate = dateChooserCtr.getChoosenDate();
				newsType = dateChooserCtr.getType();
				updateNewsDataModel();
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == emailLink) {
			// send email to user with the currently visible date
			List<SubscriptionItem> infoList = new ArrayList<>();
			List<Subscriber> subsList = new ArrayList<>();
			for (Subscriber subscriber : subsInfoMap.keySet()) {
				subsList.add(subscriber);
				SubscriptionItem item = notificationsManager.createSubscriptionItem(subscriber,
						getLocale(), SubscriptionInfo.MIME_HTML,
						SubscriptionInfo.MIME_HTML, compareDate);
				if (item != null) {
					infoList.add(item);
				}
			}
			if (notificationsManager.sendMailToUserAndUpdateSubscriber(subscriberIdentity, infoList,
					getTranslator(), subsList)) {
				showInfo("email.ok");
			} else {
				showError("email.nok");
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries,
			StateEntry state) {
		if (entries == null || entries.isEmpty())
			return;

		boolean changed = false;
		String path = entries.get(0).getOLATResourceable()
				.getResourceableTypeName();
		if (path.startsWith("type=")) {
			newsType = extractValue("type=", path);
			dateChooserCtr.setType(newsType);
			changed = true;
			// consume the entry
			entries = entries.subList(1, entries.size());
		}
		if (!entries.isEmpty()) {
			String dateEntry = entries.get(0).getOLATResourceable()
					.getResourceableTypeName();
			if (dateEntry.startsWith("date=")) {
				try {
					String date = extractValue("date=", dateEntry);
					DateFormat format = new SimpleDateFormat("yyyyMMdd");
					compareDate = format.parse(date);
					dateChooserCtr.setDate(compareDate);
					changed = true;
				} catch (ParseException e) {
					logWarn("Error parsing the date after activate: " + dateEntry, e);
				}
			}
		}

		if (changed) {
			updateNewsDataModel();
		}
	}

	private String extractValue(String str, String identifier) {
		if (identifier.startsWith(str)) {
			int sepIndex = identifier.indexOf(':');
			int lastIndex = (sepIndex > 0 ? sepIndex : identifier.length());
			return identifier.substring(str.length(), lastIndex);
		}
		return null;
	}
}
