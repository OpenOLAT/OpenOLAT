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
package org.olat.user.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.user.AbsenceLeave;
import org.olat.user.model.AbsenceLeaveImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AbsenceLeaveDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AbsenceLeave createAbsenceLeave(Identity identity, Date from, Date to, OLATResourceable resourceable, String subIdent) {
		AbsenceLeaveImpl absence = new AbsenceLeaveImpl();
		absence.setCreationDate(new Date());
		absence.setLastModified(absence.getCreationDate());
		absence.setAbsentFrom(from);
		absence.setAbsentTo(to);
		absence.setIdentity(identity);
		if(resourceable != null) {
			absence.setResName(resourceable.getResourceableTypeName());
			absence.setResId(resourceable.getResourceableId());
		}
		absence.setSubIdent(subIdent);
		dbInstance.getCurrentEntityManager().persist(absence);
		return absence;
	}
	
	public AbsenceLeave loadAbsenceLeaveByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select leave from userabsenceleave as leave")
		  .append(" inner join fetch leave.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" where leave.key=:absenceLeaveKey");
		
		List<AbsenceLeave> leaves = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AbsenceLeave.class)
				.setParameter("absenceLeaveKey", key)
				.getResultList();
		return leaves != null && !leaves.isEmpty() ? leaves.get(0) : null;
	}
	
	public List<AbsenceLeave> getAbsenceLeaves(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select leave from userabsenceleave as leave")
		  .append(" inner join fetch leave.identity as ident")
		  .append(" where leave.identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AbsenceLeave.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
}
