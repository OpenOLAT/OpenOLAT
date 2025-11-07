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
package org.olat.modules.creditpoint.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemToOrganisation;
import org.olat.modules.creditpoint.model.CreditPointSystemToOrganisationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 6 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CreditPointSystemToOrganisationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CreditPointSystemToOrganisation createRelation(CreditPointSystem creditPointSystem, Organisation organisation) {
		CreditPointSystemToOrganisationImpl rel = new CreditPointSystemToOrganisationImpl();
		rel.setCreationDate(new Date());
		rel.setCreditPointSystem(creditPointSystem);
		rel.setOrganisation(organisation);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public List<CreditPointSystemToOrganisation> loadRelations(CreditPointSystem creditPointSystem) {
		String query = """
				select rel from creditpointsystemtoorganisation as rel
				inner join fetch rel.organisation as org
				inner join fetch rel.creditPointSystem as system
				where system.key=:systemKey""";
		return dbInstance.getCurrentEntityManager().createQuery(query, CreditPointSystemToOrganisation.class)
				.setParameter("systemKey", creditPointSystem.getKey())
				.getResultList();
	}
	
	public void deleteRelation(CreditPointSystemToOrganisation rel) {
		dbInstance.getCurrentEntityManager().remove(rel);
	}
	
	public void deleteRelations(CreditPointSystem creditPointSystem) {
		String query = """
			delete from creditpointsystemtoorganisation as rel
			where rel.creditPointSystem.key=:systemKey""";
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("systemKey", creditPointSystem.getKey())
				.executeUpdate();
	}
	
	public Map<Long,List<Long>> getOrganisationsMap(List<Long> systemsKeys) {
		String query = """
				select rel.creditPointSystem.key, rel.organisation.key from creditpointsystemtoorganisation rel
				where rel.creditPointSystem.key in (:identityKeys)""";
		
		int count = 0;
		int batch = 5000;

		Map<Long,List<Long>> map = new HashMap<>();
		TypedQuery<Object[]> rawObjectsQuery = dbInstance.getCurrentEntityManager()
				.createQuery(query, Object[].class);
		
		do {
			int toIndex = Math.min(count + batch, systemsKeys.size());
			List<Long> toLoad = systemsKeys.subList(count, toIndex);
			List<Object[]> rawObjectsList = rawObjectsQuery
					.setParameter("identityKeys", toLoad)
					.setFlushMode(FlushModeType.COMMIT)
					.getResultList();
			
			for(Object[] rawObjects:rawObjectsList) {
				Long identityKey = (Long)rawObjects[0];
				Long organisationKey = (Long)rawObjects[1];
				map.computeIfAbsent(identityKey, key -> new ArrayList<>(3))
					.add(organisationKey);
			}
			count += batch;
		} while(count < systemsKeys.size());

		return map;
	}
}
