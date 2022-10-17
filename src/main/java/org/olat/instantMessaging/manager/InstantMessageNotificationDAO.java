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

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.OLATResourceable;
import org.olat.instantMessaging.InstantMessageNotification;
import org.olat.instantMessaging.model.InstantMessageNotificationImpl;
import org.olat.instantMessaging.model.InstantMessageNotificationTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class InstantMessageNotificationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public InstantMessageNotification createNotification(Long fromIdentityKey, Long toIdentityKey,
			OLATResourceable chatResource, String resSubPath, String channel, InstantMessageNotificationTypeEnum type) {
		InstantMessageNotificationImpl notification = new InstantMessageNotificationImpl();
		notification.setCreationDate(new Date());
		notification.setToIdentityKey(toIdentityKey);
		notification.setFromIdentityKey(fromIdentityKey);
		notification.setResourceTypeName(chatResource.getResourceableTypeName());
		notification.setResourceId(chatResource.getResourceableId());
		notification.setResSubPath(resSubPath);
		notification.setChannel(channel);
		notification.setType(type);
		dbInstance.getCurrentEntityManager().persist(notification);
		return notification;
	}
	
	public void deleteNotification(Long notificationId) {
		InstantMessageNotificationImpl notification = dbInstance.getCurrentEntityManager()
				.getReference(InstantMessageNotificationImpl.class, notificationId);
		dbInstance.getCurrentEntityManager().remove(notification);
	}
	
	public void deleteNotification(InstantMessageNotification notification) {
		dbInstance.getCurrentEntityManager().remove(notification);
	}
	
	public void deleteNotification(IdentityRef identity, OLATResourceable ores, String resSubPath, String channel) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from imnotification notification ")
		  .where().append("notification.resourceId=:resid and notification.resourceTypeName=:resname");
		if(identity != null) {
			sb.and().append("notification.toIdentityKey=:identityKey");
		}
		if(resSubPath == null) {
			sb.and().append(" notification.resSubPath is null");
		} else {
			sb.and().append(" notification.resSubPath=:resSubPath");
		}
		if(channel == null) {
			sb.and().append(" notification.channel is null");
		} else {
			sb.and().append(" notification.channel=:channel");
		}
		
		Query deleteQuery = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString())
			.setParameter("resid", ores.getResourceableId())
			.setParameter("resname", ores.getResourceableTypeName());
		if(identity != null) {
			deleteQuery.setParameter("identityKey", identity.getKey());
		}
		if(resSubPath != null) {
			deleteQuery.setParameter("resSubPath", resSubPath);
		}
		if(channel != null) {
			deleteQuery.setParameter("channel", channel);
		}
		deleteQuery.executeUpdate();
	}
	
	public List<InstantMessageNotification> getNotifications(OLATResourceable ores, String resSubPath, String channel) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select notification from imnotification notification ")
		  .where().append("notification.resourceId=:resid and notification.resourceTypeName=:resname");
		if(resSubPath == null) {
			sb.and().append(" notification.resSubPath is null");
		} else {
			sb.and().append(" notification.resSubPath=:resSubPath");
		}
		if(channel == null) {
			sb.and().append(" notification.channel is null");
		} else {
			sb.and().append(" notification.channel=:channel");
		}
		
		TypedQuery<InstantMessageNotification> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), InstantMessageNotification.class)
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName());
		if(resSubPath != null) {
			query.setParameter("resSubPath", resSubPath);
		}
		if(channel != null) {
			query.setParameter("channel", channel);
		}
		return query.getResultList();
	}
	
	public long countRequestNotifications(IdentityRef identity) {
		List<Long> count = dbInstance.getCurrentEntityManager()
				.createNamedQuery("countIMTypedNotificationByIdentity", Long.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("type", InstantMessageNotificationTypeEnum.request)
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0l : count.get(0).longValue();
	}
	
	public List<InstantMessageNotification> getRequestNotifications(IdentityRef identity) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMTypedNotificationByIdentity", InstantMessageNotification.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("type", InstantMessageNotificationTypeEnum.request)
				.getResultList();
	}
	
	public List<InstantMessageNotification> getPrivateNotifications(IdentityRef identity) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMPrivateNotificationByIdentity", InstantMessageNotification.class)
				.setParameter("identityKey", identity.getKey())
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
	}

}
