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
package org.olat.instantMessaging.manager;

import java.util.Date;
import java.util.List;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.instantMessaging.InstantMessage;
import org.olat.instantMessaging.InstantMessageNotification;
import org.olat.instantMessaging.model.InstantMessageImpl;
import org.olat.instantMessaging.model.InstantMessageNotificationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 07.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class InstantMessageDAO {
	
	private static final OLog log = Tracing.createLoggerFor(InstantMessageDAO.class);
	
	@Autowired
	private DB dbInstance;
	
	public InstantMessage createMessage(Identity from, String fromNickName, boolean anonym, String body, OLATResourceable chatResource) {
		InstantMessageImpl msg = new InstantMessageImpl();
		msg.setBody(body);
		msg.setFromKey(from.getKey());
		msg.setFromNickName(fromNickName);
		msg.setAnonym(anonym);
		msg.setResourceTypeName(chatResource.getResourceableTypeName());
		msg.setResourceId(chatResource.getResourceableId());
		msg.setCreationDate(new Date());
		dbInstance.getCurrentEntityManager().persist(msg);
		return msg;
	}

	public InstantMessageImpl loadMessageById(Long key) {
		List<InstantMessageImpl> msgs = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMessageByKey", InstantMessageImpl.class)
				.setParameter("key", key)
				.getResultList();
		
		if(msgs.isEmpty()) {
			return null;
		}
		return msgs.get(0);
	}
	
	public List<InstantMessage> loadMessageBy(IdentityRef identity) {
		String query = "select msg from instantmessage msg where msg.fromKey=:identityKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, InstantMessage.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	public List<InstantMessage> getMessages(OLATResourceable ores, Date from, int firstResult, int maxResults) {
		String queryName = (from == null ? "loadIMessageByResource" : "loadIMessageByResourceAndDate");
		TypedQuery<InstantMessage> query = dbInstance.getCurrentEntityManager()
				.createNamedQuery(queryName, InstantMessage.class)
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName())
				.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		if(from != null) {
			query.setParameter("from", from, TemporalType.TIMESTAMP);
		}
		return query.getResultList();
	}
	
	public int deleteMessages(OLATResourceable ores) {
		int count = dbInstance.getCurrentEntityManager()
				.createQuery("delete from instantmessage msg where msg.resourceId=:resid and msg.resourceTypeName=:resname")
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName())
				.executeUpdate();
		if(count > 0) {
			log.audit(count + " IM messages delete for resource: " + ores);
		}
		return count;
	}
	
	public int deleteMessages(IdentityRef identity) {
		int count = dbInstance.getCurrentEntityManager()
				.createQuery("delete from instantmessage msg where msg.fromKey=:identityKey")
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
		if(count > 0) {
			log.audit(count + " IM messages delete for identity: " + identity.getKey());
		}
		return count;
	}
	
	public InstantMessageNotification createNotification(Long fromIdentityKey, Long toIdentityKey, OLATResourceable chatResource) {
		InstantMessageNotificationImpl notification = new InstantMessageNotificationImpl();
		notification.setToIdentityKey(toIdentityKey);
		notification.setFromIdentityKey(fromIdentityKey);
		notification.setResourceTypeName(chatResource.getResourceableTypeName());
		notification.setResourceId(chatResource.getResourceableId());
		notification.setCreationDate(new Date());
		dbInstance.getCurrentEntityManager().persist(notification);
		return notification;
	}
	
	public void deleteNotification(Long notificationId) {
		InstantMessageNotificationImpl notification = dbInstance.getCurrentEntityManager()
				.getReference(InstantMessageNotificationImpl.class, notificationId);
		dbInstance.getCurrentEntityManager().remove(notification);
	}
	
	public void deleteNotification(Identity identity, OLATResourceable ores) {
		dbInstance.getCurrentEntityManager()
			.createQuery("delete from imnotification notification where notification.toIdentityKey=:identityKey and notification.resourceId=:resid and notification.resourceTypeName=:resname")
			.setParameter("identityKey", identity.getKey())
			.setParameter("resid", ores.getResourceableId())
			.setParameter("resname", ores.getResourceableTypeName())
			.executeUpdate();
	}
	
	public List<InstantMessageNotification> getNotifications(Identity identity) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMNotificationByIdentity", InstantMessageNotification.class)
				.setParameter("identityKey", identity.getKey())
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
	}
}