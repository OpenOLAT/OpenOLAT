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

import java.util.Collections;
import java.util.List;

import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoSubscriptionManager;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * Implementation of the subscriptions manager for the messages.
 * 
 * <P>
 * Initial Date:  27 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class InfoSubscriptionManagerImpl implements InfoSubscriptionManager {
	
	private static final String PUBLISHER_TYPE = OresHelper.calculateTypeName(InfoMessage.class);

	@Autowired
	private NotificationsManager notificationsManager;

	@Override
	public SubscriptionContext getInfoSubscriptionContext(OLATResourceable resource, String subPath) {
		String resName = resource.getResourceableTypeName();
		Long resId = resource.getResourceableId();
		return new SubscriptionContext(resName, resId, subPath);
	}

	@Override
	public PublisherData getInfoPublisherData(OLATResourceable resource, String businessPath) {
		String resId = resource.getResourceableId() == null ? "0" : resource.getResourceableId().toString();
		return new PublisherData(PUBLISHER_TYPE, resId, businessPath);
	}
	
	@Override
	public List<Identity> getInfoSubscribers(OLATResourceable resource, String subPath) {
		SubscriptionContext context = getInfoSubscriptionContext(resource, subPath);
		Publisher publisher = notificationsManager.getPublisher(context);
		if(publisher == null) {
			return Collections.emptyList();
		}
		
		return notificationsManager.getSubscriberIdentities(publisher, true);
	}

	@Override
	public void subscribe(OLATResourceable resource, String resSubPath, String businessPath, Identity identity) {
		PublisherData data = getInfoPublisherData(resource, businessPath);
		SubscriptionContext context = getInfoSubscriptionContext(resource, resSubPath);
		notificationsManager.subscribe(identity, context, data);
	}

	@Override
	public void unsubscribe(OLATResourceable resource, String subPath, Identity identity) {
		SubscriptionContext context = getInfoSubscriptionContext(resource, subPath);
		notificationsManager.unsubscribe(identity, context);
	}
	
	@Override
	public void markPublisherNews(OLATResourceable resource, String subPath) {
		SubscriptionContext context = getInfoSubscriptionContext(resource, subPath);
		notificationsManager.markPublisherNews(context, null, true);
	}
	
	@Override
	public void deleteSubscriptionContext(SubscriptionContext context) {
		notificationsManager.delete(context);
	}
}
