/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.log.RecruitingAuditLogUserNotificationsImpl;

/**
 * 
 * Initial date: 23 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RecruitingAuditLogUserNotificationsDAO {
	
	@Autowired
	private DB dbInstance;
	
	public RecruitingAuditLogUserNotificationsImpl create(Identity identity, Date lastEmail) {
		RecruitingAuditLogUserNotificationsImpl notifications = new RecruitingAuditLogUserNotificationsImpl();
		notifications.setCreationDate(new Date());
		notifications.setIdentity(identity);
		notifications.setLastEmail(lastEmail);
		dbInstance.getCurrentEntityManager().persist(notifications);
		return notifications;
	}
	
	public List<RecruitingAuditLogUserNotificationsImpl> getUserNotifications() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select noti from recruitingauditlogusernotifications as noti")
		  .append(" inner join fetch noti.identity as ident");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RecruitingAuditLogUserNotificationsImpl.class)
				.getResultList();
	}
	
	public RecruitingAuditLogUserNotificationsImpl update(RecruitingAuditLogUserNotificationsImpl userNotifications) {
		return dbInstance.getCurrentEntityManager().merge(userNotifications);
	}
}
