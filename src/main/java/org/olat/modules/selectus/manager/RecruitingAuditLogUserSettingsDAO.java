/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.AuditService.NotificationIntervals;
import org.olat.modules.selectus.model.RecruitingAuditLogUserSettings;
import org.olat.modules.selectus.model.log.RecruitingAuditLogUserSettingsImpl;

/**
 * 
 * Initial date: 23 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RecruitingAuditLogUserSettingsDAO {
	
	@Autowired
	private DB dbInstance;
	
	public RecruitingAuditLogUserSettings createAndPersist(Identity identity) {
		RecruitingAuditLogUserSettingsImpl settings = new RecruitingAuditLogUserSettingsImpl();
		settings.setCreationDate(new Date());
		settings.setLastModified(settings.getCreationDate());
		settings.setEnabled(false);
		settings.setInterval(NotificationIntervals.never.name());
		settings.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(settings);
		return settings;
	}
	
	public RecruitingAuditLogUserSettings update(RecruitingAuditLogUserSettings settings) {
		((RecruitingAuditLogUserSettingsImpl)settings).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(settings);
	}
	
	public RecruitingAuditLogUserSettings findSettings(IdentityRef identity) {
		String query = "select settings from recruitingauditlogusersettings settings where settings.identity.key=:identityKey";
		
		List<RecruitingAuditLogUserSettings> settings = dbInstance.getCurrentEntityManager()
				.createQuery(query, RecruitingAuditLogUserSettings.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return settings == null || settings.isEmpty() ? null : settings.get(0);
	}
	
	public List<RecruitingAuditLogUserSettings> findEnabledNotifications() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select settings from recruitingauditlogusersettings as settings")
		  .append(" inner join fetch settings.identity as ident")
		  .append(" where settings.enabled=true");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(),  RecruitingAuditLogUserSettings.class)
				.getResultList();
	}
}
