/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.RecruitingAuditLog;
import org.olat.modules.selectus.model.log.RecruitingAuditLogReadImpl;

/**
 * 
 * Initial date: 23 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RecruitingAuditLogReadDAO {
	
	@Autowired
	private DB dbInstance;
	
	public RecruitingAuditLogReadImpl create(Identity identity, RecruitingAuditLog log) {
		RecruitingAuditLogReadImpl read = new RecruitingAuditLogReadImpl();
		read.setCreationDate(new Date());
		read.setAuditLog(log);
		read.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(read);
		return read;
	}
	
	public RecruitingAuditLogReadImpl loadByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select read.auditLog.key from recruitingauditlogread as read")
		  .append(" where read.key=:logKey");

		List<RecruitingAuditLogReadImpl> logs = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RecruitingAuditLogReadImpl.class)
				.setParameter("logKey", key)
				.getResultList();
		return logs == null || logs.isEmpty() ? null : logs.get(0);
	}
	
	public Set<Long> getRead(IdentityRef reader) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select read.auditLog.key from recruitingauditlogread as read")
		  .append(" where read.identity.key=:identityKey");

		List<Long> logKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", reader.getKey())
				.getResultList();
		return new HashSet<>(logKeys);
	}
	
	public int delete(IdentityRef reader, RecruitingAuditLog log) {
		String sb = "delete from recruitingauditlogread as read where read.identity.key=:identityKey and read.auditLog.key=:auditLogKey";
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb)
			.setParameter("identityKey", reader.getKey())
			.setParameter("auditLogKey", log.getKey())
			.executeUpdate();
	}
	
	public int delete(PositionRef position) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete from recruitingauditlogread as read")
		  .append(" where read.auditLog.key in (select log.key from recruitingauditlog as log")
		  .append("  where log.positionKey=:positionKey")
		  .append(" )");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString())
			.setParameter("positionKey", position.getKey())
			.executeUpdate();
	}
}
