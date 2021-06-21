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
package org.olat.modules.dcompensation.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationAuditLog;
import org.olat.modules.dcompensation.model.DisadvantageCompensationAuditLogImpl;
import org.olat.modules.dcompensation.model.DisadvantageCompensationImpl;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 24 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DisadvantageCompensationAuditLogDAO {
	
	private static final XStream disadvantageCompensationXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(disadvantageCompensationXStream);
		disadvantageCompensationXStream.alias("disadvantageCompensation", DisadvantageCompensationImpl.class);
		disadvantageCompensationXStream.ignoreUnknownElements();
		
		disadvantageCompensationXStream.omitField(DisadvantageCompensationImpl.class, "identity");
		disadvantageCompensationXStream.omitField(DisadvantageCompensationImpl.class, "entry");
		disadvantageCompensationXStream.omitField(IdentityImpl.class, "user");
	}

	@Autowired
	private DB dbInstance;
	
	public DisadvantageCompensationAuditLog create(String action, String before, String after,
			DisadvantageCompensation compensation, IdentityRef doer) {
		DisadvantageCompensationAuditLogImpl log = new DisadvantageCompensationAuditLogImpl();
		log.setCreationDate(new Date());
		log.setAction(action);
		log.setBefore(before);
		log.setAfter(after);
		log.setCompensationKey(compensation.getKey());
		if(compensation.getIdentity() != null) {
			log.setIdentityKey(compensation.getIdentity().getKey());
		}
		log.setSubIdent(compensation.getSubIdent());
		if(compensation.getEntry() != null) {
			log.setEntryKey(compensation.getEntry().getKey());
		}
		if(doer != null) {
			log.setAuthorKey(doer.getKey());
		}
		dbInstance.getCurrentEntityManager().persist(log);
		return log;
	}
	
	public List<DisadvantageCompensationAuditLog> getAuditLogs(IdentityRef identity, RepositoryEntryRef entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select auditLog from dcompensationauditlog as auditLog")
		  .append(" where auditLog.identityKey=:identityKey")
		  .append(" and auditLog.entryKey=:entryKey and auditLog.subIdent=:subIdent");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), DisadvantageCompensationAuditLog.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("entryKey", entry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
	}
	
	public String toXml(DisadvantageCompensationImpl compensation) {
		if(compensation == null) return null;
		return disadvantageCompensationXStream.toXML(compensation);
	}
}
