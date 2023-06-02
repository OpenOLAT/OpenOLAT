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
package org.olat.modules.ceditor.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.ceditor.ContentAuditLog;
import org.olat.modules.ceditor.ContentAuditLog.Action;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.model.jpa.ContentAuditLogImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ContentAuditLogDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ContentAuditLog create(Action action, Page page, Identity doer) {
		ContentAuditLogImpl auditLog = new ContentAuditLogImpl();
		auditLog.setCreationDate(new Date());
		auditLog.setLastModified(auditLog.getCreationDate());
		auditLog.setAction(action);
		auditLog.setDoer(doer);
		if(page != null) {
			auditLog.setPageKey(page.getKey());
		}
		dbInstance.getCurrentEntityManager().persist(auditLog);
		return auditLog;
	}
	
	public ContentAuditLog lastChange(Page page) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select auditLog from ceauditlog auditLog")
		  .append(" left join fetch auditLog.doer doer")
		  .append(" left join fetch doer.user doerUser")
		  .append(" where auditLog.pageKey=:pageKey")
		  .append(" order by auditLog.lastModified desc");

		List<ContentAuditLog> logs = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ContentAuditLog.class)
			.setParameter("pageKey", page.getKey())
			.setFirstResult(0).setMaxResults(1)
			.getResultList();
		return logs == null || logs.isEmpty() ? null : logs.get(0);
	}
	
	

}
