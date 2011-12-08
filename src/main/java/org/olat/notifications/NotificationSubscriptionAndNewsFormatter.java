/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.notifications;

import java.util.Map;

import org.olat.ControllerFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.notifications.SubscriptionInfo;
import org.olat.core.util.notifications.SubscriptionItem;

/**
 * Description:<br>
 * This class provides helper methods to render notification news details
 * 
 * <P>
 * Initial Date: 22.12.2009 <br>
 * 
 * @author gnaegi
 */
public class NotificationSubscriptionAndNewsFormatter {

	private Translator translator;
	private Map<Subscriber, SubscriptionInfo> subsInfoMap;

	NotificationSubscriptionAndNewsFormatter(Translator translator, Map<Subscriber, SubscriptionInfo> subsInfoMap) {
		this.translator = translator;
		this.subsInfoMap = subsInfoMap;
	}

	public String getType(Subscriber sub) {
		Publisher pub = sub.getPublisher();
		String innerType = pub.getType();
		String typeName = ControllerFactory.translateResourceableTypeName(innerType, translator.getLocale());
		return typeName;
	}

	public String getContainerType(Subscriber sub) {
		Publisher pub = sub.getPublisher();
		String containerType = pub.getResName();
		String containerTypeTrans = ControllerFactory.translateResourceableTypeName(containerType, translator.getLocale());
		return containerTypeTrans;
	}

	public boolean hasNews(Subscriber sub) {
		return subsInfoMap.containsKey(sub);
	}
	
	public String getNewsAsHTML(Subscriber sub) {
		return getNews(sub, SubscriptionInfo.MIME_HTML);
	}

	public String getNewsAsTxt(Subscriber sub) {
		return getNews(sub, SubscriptionInfo.MIME_PLAIN);
	}

	private String getNews(Subscriber sub, String mimeType) {
		SubscriptionInfo subsInfo = subsInfoMap.get(sub);
		if (subsInfo == null || !subsInfo.hasNews()) return translator.translate("news.no.news");
		return subsInfo.getSpecificInfo(mimeType, translator.getLocale());
	}

	public String getTitleAsHTML(Subscriber sub) {
		return getTitle(sub, SubscriptionInfo.MIME_HTML);
	}

	public String getTitleAsTxt(Subscriber sub) {
		return getTitle(sub, SubscriptionInfo.MIME_PLAIN);
	}

	private String getTitle(Subscriber sub, String mimeType) {
		SubscriptionInfo subsInfo = subsInfoMap.get(sub);
		if (subsInfo == null) return "";
		return subsInfo.getTitle(mimeType);
	}
	
	public String getCustomUrl(Subscriber sub) {
		SubscriptionInfo subsInfo = subsInfoMap.get(sub);
		return subsInfo.getCustomUrl();
	}

	public SubscriptionItem getSubscriptionItem(Subscriber sub) {
		SubscriptionInfo subsInfo = subsInfoMap.get(sub);
		NotificationsManager notiMgr = NotificationsManager.getInstance();
		SubscriptionItem subscrItem = notiMgr.createSubscriptionItem(subsInfo, sub, translator.getLocale(), SubscriptionInfo.MIME_HTML, SubscriptionInfo.MIME_HTML);
		return subscrItem;		
	}
}