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
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.basesecurity.events.NewIdentityCreatedEvent;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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
@Service
public class UsersSubscriptionManagerImpl implements UsersSubscriptionManager, GenericEventListener {
	public static final String NEW = "NewIdentityCreated";
	public static final Long RES_ID = Long.valueOf(0);
	public static final String RES_NAME = OresHelper.calculateTypeName(User.class);
	public static final OLATResourceable IDENTITY_EVENT_CHANNEL = OresHelper.lookupType(Identity.class);
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private NotificationsManager notificationsManager;
	
	@Autowired
	public UsersSubscriptionManagerImpl(CoordinatorManager coordinatorManager) {
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, IDENTITY_EVENT_CHANNEL);
	}
	
	/**
	 * Receive the event after the creation of new identities
	 */
	@Override
	public void event(Event event) {
		if (event instanceof NewIdentityCreatedEvent) {
			markPublisherNews();
		}
	}

	@Override
	public SubscriptionContext getNewUsersSubscriptionContext() {
		return new SubscriptionContext(RES_NAME, RES_ID, NEW);
	}

	@Override
	public PublisherData getNewUsersPublisherData() {
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(new OLATResourceable() {
			@Override
			public Long getResourceableId() { return 0l; }
			@Override
			public String getResourceableTypeName() { return "NewIdentityCreated"; }
		});
		return new PublisherData(RES_NAME, NEW, ce.toString());
	}

	@Override
	public Subscriber getNewUsersSubscriber(Identity identity) {
		SubscriptionContext context = getNewUsersSubscriptionContext();
		Publisher publisher = notificationsManager.getPublisher(context);
		if(publisher == null) {
			return null;
		}
		return notificationsManager.getSubscriber(identity, publisher);
	}
	
	/**
	 * Subscribe to notifications of new identity created
	 */
	@Override
	public void subscribe(Identity identity) {
		PublisherData data = getNewUsersPublisherData();
		SubscriptionContext context = getNewUsersSubscriptionContext();
		notificationsManager.subscribe(identity, context, data);
	}
	
	/**
	 * Unsubscribe to notifications of new identity created
	 */
	@Override
	public void unsubscribe(Identity identity) {
		SubscriptionContext context = getNewUsersSubscriptionContext();
		notificationsManager.unsubscribe(identity, context);	
	}
	
	/**
	 * Call this method if there is new identities
	 */
	@Override
	public void markPublisherNews() {
		SubscriptionContext context = getNewUsersSubscriptionContext();
		notificationsManager.markPublisherNews(context, null, true);
	}

	/**
	 * The search in the ManagerFactory is date based and not timestamp based.
	 * The guest are also removed from the list.
	 */
	@Override
	public List<Identity> getNewIdentityCreated(Date from, Identity actingIdentity, Roles roles) {
		if(from == null || (!roles.isUserManager() && !roles.isOLATAdmin())) return Collections.emptyList();

		List<Organisation> userManagerOrganisations = null;
		if(!roles.isOLATAdmin()) {
			userManagerOrganisations = organisationDao
					.getOrganisations(actingIdentity, Collections.singletonList(OrganisationRoles.usermanager.name()));
			if(userManagerOrganisations.isEmpty()) {
				return Collections.emptyList();
			}
		}
		
		SearchIdentityParams params = new SearchIdentityParams();
		params.setExcludedRoles(new OrganisationRoles[]{ OrganisationRoles.guest });
		params.setCreatedAfter(from);
		params.setStatus(Identity.STATUS_VISIBLE_LIMIT);
		params.setOrganisationParents(userManagerOrganisations);
		return securityManager.getIdentitiesByPowerSearch(params, 0, -1);
	}
}