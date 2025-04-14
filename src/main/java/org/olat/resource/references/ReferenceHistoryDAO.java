/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.resource.references;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class ReferenceHistoryDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ReferenceHistory addReferenceHistory(OLATResource source, OLATResource target, String userdata, Identity actor) {
		ReferenceHistoryImpl ref = new ReferenceHistoryImpl();
		ref.setCreationDate(new Date());
		ref.setSource(source);
		ref.setTarget(target);
		ref.setUserdata(userdata);
		ref.setIdentity(actor);
		dbInstance.getCurrentEntityManager().persist(ref);
		return ref;
	}
	
	public List<ReferenceHistory> loadHistory(OLATResource source, String userdata) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select hist from referenceshistory hist")
		  .append(" inner join fetch hist.source as source")
		  .append(" inner join fetch hist.target as target")
		  .append(" left join fetch hist.identity as ident")
		  .append(" left join fetch ident.user as identUser")
		  .where().append("source.key=:sourceKey");
		if(StringHelper.containsNonWhitespace(userdata)) {
			sb.and().append(" hist.userdata=:userdata");
		}
		sb.append(" order by hist.creationDate desc");
		
		TypedQuery<ReferenceHistory> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), ReferenceHistory.class)
			.setParameter("sourceKey", source.getKey());
		if(StringHelper.containsNonWhitespace(userdata)) {
			query.setParameter("userdata", userdata);
		}
		return query.getResultList();
	}
	
	/**
	 * Delete all references history of an OLAT-resource as source or target.
	 * @param olatResource A resource
	 */
	public int deleteAllReferencesHistoryOf(OLATResource resource) {
		String dq = "delete from referenceshistory as refs where refs.source.key=:resourceKey or refs.target.key=:resourceKey";
		return dbInstance.getCurrentEntityManager().createQuery(dq)
				.setParameter("resourceKey", resource.getKey())
				.executeUpdate();
	}
	
	public int deleteAllReferencesHistoryOf(OLATResource resource, String userdata) {
		String dq = "delete from referenceshistory as refs where refs.userdata=:userdata and (refs.source.key=:resourceKey or refs.target.key=:resourceKey)";
		return dbInstance.getCurrentEntityManager().createQuery(dq)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("userdata", userdata)
				.executeUpdate();
	}
	
	public void delete(ReferenceHistory ref) {
		ReferenceHistoryImpl reloadedRef = dbInstance.getCurrentEntityManager()
				.getReference(ReferenceHistoryImpl.class, ref.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedRef);
	}

}
