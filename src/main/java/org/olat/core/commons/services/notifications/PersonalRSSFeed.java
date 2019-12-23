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

package org.olat.core.commons.services.notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.ui.NotificationSubscriptionController;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.user.UserManager;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.feed.synd.SyndImageImpl;

/**
 * Description:<BR>
 * RSS document that contains the users notificatons.
 * <P>
 * Initial Date: Jan 11, 2005 2004
 * 
 * @author Florian Gnägi
 */

public class PersonalRSSFeed extends SyndFeedImpl {

	private static final long serialVersionUID = -5246221887165246074L;

	/**
	 * Constructor for a RSS document that contains all the users personal
	 * notifications
	 * 
	 * @param identity The users identity
	 * @param token The users RSS-authentication token
	 */
	public PersonalRSSFeed(Identity identity) {
		super();
		setFeedType("rss_2.0");
		setEncoding(PersonalRSSServlet.DEFAULT_ENCODING);

		User user = identity.getUser();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(user.getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(NotificationSubscriptionController.class, locale);
		NotificationsManager man = CoreSpringFactory.getImpl(NotificationsManager.class);
		String fullName = UserManager.getInstance().getUserDisplayName(identity);
		
		setTitle(translator.translate("rss.title", new String[] { fullName }));
		setLink(PersonalRSSUtil.URI_SERVER);
		setDescription(translator.translate("rss.description", new String[] { fullName }));

		// create and add an image to the feed
		SyndImage image = new SyndImageImpl();
		image.setUrl(Settings.createServerURI() + StaticMediaDispatcher.createStaticURIFor("images/openolat/openolat_logo_32.png"));
		image.setTitle("OpenOLAT - infinite learning");
		image.setLink(getLink());
		setImage(image);

		List<SyndEntry> entries = new ArrayList<>();
		SyndEntry entry = new SyndEntryImpl();
		entry.setTitle(translator.translate("rss.olat.title", new String[] { NotificationHelper.getFormatedName(identity) }));
		entry.setLink(getLink());
		SyndContent description = new SyndContentImpl();
		description.setType("text/plain");
		description.setValue(translator.translate("rss.olat.description"));
		entry.setDescription(description);
		entries.add(entry);

		// notification news
		// we are only interested in subscribers which listen to a valid publisher
		List<Subscriber> subs = man.getValidSubscribers(identity);
		for (Subscriber subscriber : subs) {
			SubscriptionItem si = man.createSubscriptionItem(subscriber, locale, SubscriptionInfo.MIME_PLAIN, SubscriptionInfo.MIME_HTML);
			if (si != null) {
				SyndEntry item = new SyndEntryImpl();
				item.setTitle(si.getTitle());
				item.setLink(si.getLink());
				SyndContent itemDescription = new SyndContentImpl();
				itemDescription.setType(SubscriptionInfo.MIME_HTML);
				itemDescription.setValue(si.getDescription());
				item.setDescription(itemDescription);
				entries.add(item);
			}
		}
		setEntries(entries);
	}
}