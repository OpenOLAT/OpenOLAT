
/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.notifications;

import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;

import org.olat.core.id.OLATResourceable;

/**
 * 
 * Initial date: 16 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public interface NotificationsPushService {
	
	public static final String OPERATION = "operation";
	
	public static final String CONTEXT_RESOURCE_NAME = "contextresourcename";
	public static final String CONTEXT_RESOURCE_ID = "contextresourceid";
	public static final String CONTEXT_RESOURCE_SUBIDENT = "contextresourcesubident";
	
	public static final String PUBLISHER_TYPE = "publishertype";
	public static final String PUBLISHER_DATA = "publisherdata";
	
	public static final String OBJECT_TYPE = "objecttype";
	public static final String OBJECT_ID = "objectid";
	

	public void sendMessage(SubscriptionContext context, PublisherData data,
			OLATResourceable object, String operation);
	
	public static SubscriptionContext toContext(MapMessage message) throws JMSException {
		String resName = message.getStringProperty(CONTEXT_RESOURCE_NAME);
		Long resId = message.getLongProperty(CONTEXT_RESOURCE_ID);
		String subIdent = message.getStringProperty(CONTEXT_RESOURCE_SUBIDENT);
		return new SubscriptionContext(resName, resId, subIdent);
	}
}
