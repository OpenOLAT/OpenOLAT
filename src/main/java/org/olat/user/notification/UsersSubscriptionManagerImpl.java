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
package org.olat.user.notification;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.PermissionOnResourceable;
import org.olat.basesecurity.events.NewIdentityCreatedEvent;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;


/**
 * 
 * Description:<br>
 * This implementation help to manager the subscription for notification of
 * "new identity created".
 * <P>
 * Initial Date:  18 august 2009 <br>
 *
 * @author srosse
 */
public class UsersSubscriptionManagerImpl extends UsersSubscriptionManager implements GenericEventListener {
	public static final String NEW = "NewIdentityCreated";
	public static final Long RES_ID = new Long(0);
	public static final String RES_NAME = OresHelper.calculateTypeName(User.class);
	public static final OLATResourceable IDENTITY_EVENT_CHANNEL = OresHelper.lookupType(Identity.class);
	
	private boolean autoSubscribe;
	
	public UsersSubscriptionManagerImpl() {
		//
	}
	
	public void setCoordinator(CoordinatorManager coordinatorManager) {
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, IDENTITY_EVENT_CHANNEL);
	}
	
	public void setAutoSubscribe(boolean autoSubscribe) {
		this.autoSubscribe = autoSubscribe;
	}
	
	/**
	 * Receive the event after the creation of new identities
	 */
	public void event(Event event) {
		if (event instanceof NewIdentityCreatedEvent) {
			markPublisherNews();
			if(autoSubscribe) {
				NewIdentityCreatedEvent e = (NewIdentityCreatedEvent)event;
				Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(e.getIdentityId());
				subscribe(identity);
			}
		}
	}

	/**
	 * 
	 */
	public SubscriptionContext getNewUsersSubscriptionContext() {
		return new SubscriptionContext(RES_NAME, RES_ID, NEW);
	}
	
	public PublisherData getNewUsersPublisherData() {
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(new OLATResourceable() {
			@Override
			public Long getResourceableId() { return 0l; }
			@Override
			public String getResourceableTypeName() { return "NewIdentityCreated"; }
		});
		PublisherData publisherData = new PublisherData(RES_NAME, NEW, ce.toString());
		return publisherData;
	}
	
	public Subscriber getNewUsersSubscriber(Identity identity) {
		SubscriptionContext context = getNewUsersSubscriptionContext();
		Publisher publisher = NotificationsManager.getInstance().getPublisher(context);
		if(publisher == null) {
			return null;
		}
		return NotificationsManager.getInstance().getSubscriber(identity, publisher);
	}
	
	/**
	 * Subscribe to notifications of new identity created
	 */
	public void subscribe(Identity identity) {
		PublisherData data = getNewUsersPublisherData();
		SubscriptionContext context = getNewUsersSubscriptionContext();
		NotificationsManager.getInstance().subscribe(identity, context, data);
	}
	
	/**
	 * Unsubscribe to notifications of new identity created
	 */
	public void unsubscribe(Identity identity) {
		SubscriptionContext context = getNewUsersSubscriptionContext();
		NotificationsManager.getInstance().unsubscribe(identity, context);	
	}
	
	/**
	 * Call this method if there is new identities
	 */
	public void markPublisherNews() {
		SubscriptionContext context = getNewUsersSubscriptionContext();
		NotificationsManager.getInstance().markPublisherNews(context, null, true);
	}

	/**
	 * The search in the ManagerFactory is date based and not timestamp based.
	 * The guest are also removed from the list.
	 */
	@Override
	public List<Identity> getNewIdentityCreated(Date from) {
		if(from == null)
			return Collections.emptyList();
		
		BaseSecurity manager = BaseSecurityManager.getInstance();
		PermissionOnResourceable[] permissions = {new PermissionOnResourceable(Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY)};
		List<Identity> guests = manager.getIdentitiesByPowerSearch(null, null, true, null, permissions, null, from, null, null, null, Identity.STATUS_VISIBLE_LIMIT);
		List<Identity> identities = manager.getIdentitiesByPowerSearch(null, null, true, null, null, null, from, null, null, null, Identity.STATUS_VISIBLE_LIMIT);
		if(!identities.isEmpty() && !guests.isEmpty()) {
			identities.removeAll(guests);
		}
		
		for(Iterator<Identity> identityIt=identities.iterator(); identityIt.hasNext(); ) {
			Identity identity = identityIt.next();
			if(identity.getCreationDate().before(from)) {
				identityIt.remove();
			}
		}
		
		return identities;
	}
}