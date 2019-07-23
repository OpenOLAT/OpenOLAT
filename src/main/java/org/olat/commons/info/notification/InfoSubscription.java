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

package org.olat.commons.info.notification;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.prefs.Preferences;

/**
 * 
 * Description:<br>
 * Helper class to manage the opt-out of info messages subscriptions
 * 
 * <P>
 * Initial Date:  3 aug. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoSubscription {
	
	private static final String KEY_SUBSCRIPTION = "subs";
	private static final String KEY_UN_SUBSCRIPTION = "notdesired";
	
	private Preferences preferences;
	
	public InfoSubscription(Preferences preferences) {
		this.preferences = preferences;
	}
	
	public boolean isSubscribed(String businessPath) {
		return getSubscribedInfos().contains(businessPath);
	}
	
	public boolean subscribed(String businessPath, boolean force) {
		if (!isSubscribed(businessPath)) {
			// subscribe to the actual calendar
			List<String> infoSubscriptions = getSubscribedInfos();
			List<String> infoUnSubscriptions = getUnsubscribedInfos();
			if(!infoUnSubscriptions.contains(businessPath) || force) {
				infoSubscriptions.add(businessPath);
				infoUnSubscriptions.remove(businessPath);
				persistAllSubscriptionInfos(infoSubscriptions, infoUnSubscriptions);
			}
		}
		
		return getSubscribedInfos().contains(businessPath);
	}
	
	public void unsubscribed(String businessPath) {
		// subscribe to the actual calendar
		List<String> infoSubscriptions = getSubscribedInfos();
		List<String> infoUnSubscriptions = getUnsubscribedInfos();
		infoSubscriptions.remove(businessPath);
		infoUnSubscriptions.add(businessPath);
		persistAllSubscriptionInfos(infoSubscriptions, infoUnSubscriptions);
	}
	
	private List<String> getSubscribedInfos() {
		@SuppressWarnings("unchecked")
		List<String> infoSubscriptions = (List<String>)preferences.get(InfoSubscription.class, KEY_SUBSCRIPTION);
		if (infoSubscriptions == null) {
			infoSubscriptions = new ArrayList<>();
		}
		return infoSubscriptions;
	}
	
	private List<String> getUnsubscribedInfos() {
		@SuppressWarnings("unchecked")
		List<String> infoSubscriptions = (List<String>)preferences.get(InfoSubscription.class, KEY_UN_SUBSCRIPTION);
		if (infoSubscriptions == null) {
			infoSubscriptions = new ArrayList<>();
		}
		return infoSubscriptions;
	}
	
	private void persistAllSubscriptionInfos(List<String> infoSubscriptions, List<String> infoUnsubscriptions) {
		preferences.put(InfoSubscription.class, KEY_SUBSCRIPTION, infoSubscriptions);
		preferences.put(InfoSubscription.class, KEY_UN_SUBSCRIPTION, infoUnsubscriptions);
		preferences.save();
	}

}
