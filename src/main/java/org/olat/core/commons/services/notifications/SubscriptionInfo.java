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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.commons.services.notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;

/**
 * Description:<br>
 * this object holds the items-description/title
 * the list is used for multiple elements (all the news-item itself)
 * they know how to be rendered (either html or plain-text)
 * 
 * <P>
 * Initial Date:  25.10.2004 <br>
 *
 * @author Felix Jost
 */
public class SubscriptionInfo {
	public static final String MIME_HTML = "text/html";
	public static final String MIME_PLAIN = "text/plain";
	
	private final Long key;
	private final String type;
	
	private final TitleItem title;
	private final List<SubscriptionListItem> subsList;
	private String customUrl = null;
	
	public SubscriptionInfo(Long key, String type, TitleItem title, List<SubscriptionListItem> subsList){
		this.key = key;
		this.type = type;
		this.title = title;
		if (subsList == null){
			subsList = new ArrayList<>();
		}
		this.subsList = subsList;
	}

	/**
	 * looping over List with all contained items for this subs-info
	 * 
	 * @param mimeType the mimetype of the desired output; supported are currently
	 *          text/plain and text/html
	 * @return the specific (subscriber and type) info for a notification entry.
	 *         e.g. "5 new posts" for a forum subscription, or "10 new uploads"
	 *         for a folder subscription
	 */
	public String getSpecificInfo(String mimeType, Locale locale) {
		if (!hasNews()) return "";
		StringBuilder sb = new StringBuilder();
		boolean firstDone = false;
		if (mimeType.equals(SubscriptionInfo.MIME_HTML)) sb.append("<ul class='list-unstyled'>");
		for (SubscriptionListItem subListItem : subsList) {
			if (firstDone && mimeType.equals(SubscriptionInfo.MIME_PLAIN)) {
				sb.append("\n");
			}
			// append list item itself
			sb.append(subListItem.getContent(mimeType, locale));
			firstDone = true;
		}
		if (mimeType.equals(SubscriptionInfo.MIME_HTML)) sb.append("</ul>");
		return sb.toString();
	}
	
	/**
	 * @return true if there is any news
	 */
	public boolean hasNews() {
		return subsList != null && subsList.size() > 0;
	}

	public Long getKey() {
		return key;
	}

	public String getType() {
		return type;
	}

	/**
	 * @param mimeType the mimetype of the desired output; supported are currently text/plain and text/html
	 * @return the title of a notification entry. e.g. "5 new posts" for a forum subscription, or "10 new uploads" for a folder subscription
	 */
	public String getTitle(String mimeType) {
		return title.getInfoContent(mimeType); 
	}
	
	public List<SubscriptionListItem> getSubscriptionListItems() {
		return subsList;
	}
	
	public void addSubscriptionListItem(SubscriptionListItem sI){
		if (!subsList.contains(sI))	subsList.add(sI);
	}
	
	/**
	 * @return Returns the customUrl.
	 */
	public String getCustomUrl() {
		return customUrl;
	}

	/**
	 * @param customUrl The customUrl to set.
	 */
	public void setCustomUrl(String customUrl) {
		this.customUrl = customUrl;
	}

	/**
	 * @return The number of subscription list items, meaning the number of news
	 *         items for this subscription
	 */
	public int countSubscriptionListItems() {
		return this.subsList.size();
	}
	
}

