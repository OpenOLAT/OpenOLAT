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
package org.olat.basesecurity.manager;

import java.util.List;

import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.OrganisationTypeRef;
import org.olat.basesecurity.OrganisationTypeToType;
import org.olat.basesecurity.model.OrganisationTypeToTypeImpl;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OrganisationTypeToTypeDAO {
	
	@Autowired
	private DB dbInstance;
	
	public int disallowedSubType(OrganisationType parentType, OrganisationType disallowedSubType) {
		String q = "delete from organisationtypetotype type2type where type2type.organisationType.key=:typeKey and type2type.allowedSubOrganisationType.key=:subTypeKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("typeKey", parentType.getKey())
				.setParameter("subTypeKey", disallowedSubType.getKey())
				.executeUpdate();
	}
	
	public void setAllowedSubType(OrganisationType parentType, List<OrganisationType> allowSubTypes) {
		List<OrganisationTypeToType> typeToTypes = getAllowedSubTypes(parentType);
		for(OrganisationTypeToType typeToType:typeToTypes) {
			boolean found = false;
			for(OrganisationType allowSubType:allowSubTypes) {
				if(typeToType.getAllowedSubOrganisationType().equals(allowSubType)) {
					found = true;
					break;
				}
			}

			if(!found) {
				dbInstance.getCurrentEntityManager().remove(typeToType);
			}
		}

		for(OrganisationType allowSubType:allowSubTypes) {
			boolean found = false;
			for(OrganisationTypeToType typeToType:typeToTypes) {
				if(typeToType.getAllowedSubOrganisationType().equals(allowSubType)) {
					found = true;
					break;
				}
			}

			if(!found) {
				addAllowedSubType(parentType, allowSubType);
			}
		}
	}
	
	public List<OrganisationTypeToType> getAllowedSubTypes(OrganisationTypeRef parentType) {
		String q = "select type2type from organisationtypetotype type2type inner join fetch type2type.allowedSubOrganisationType subType where type2type.organisationType.key=:typeKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, OrganisationTypeToType.class)
				.setParameter("typeKey", parentType.getKey())
				.getResultList();
	}
	
	public List<OrganisationTypeToType> getAllowedSubTypes(OrganisationType parentType, OrganisationType allowedSubType) {
		String q = "select type2type from organisationtypetotype type2type where type2type.organisationType.key=:typeKey and type2type.allowedSubOrganisationType.key=:subTypeKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, OrganisationTypeToType.class)
				.setParameter("typeKey", parentType.getKey())
				.setParameter("subTypeKey", allowedSubType.getKey())
				.getResultList();
	}
	
	public int deleteAllowedSubTypes(OrganisationTypeRef parentType) {
		String q = "delete from organisationtypetotype where organisationType.key=:typeKey";
		int rows = dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("typeKey", parentType.getKey())
				.executeUpdate();
		
		String qReverse = "delete from organisationtypetotype where allowedSubOrganisationType.key=:typeKey";
		rows += dbInstance.getCurrentEntityManager()
				.createQuery(qReverse)
				.setParameter("typeKey", parentType.getKey())
				.executeUpdate();
		
		return rows;
	}
	
	public void addAllowedSubType(OrganisationType parentType, OrganisationType allowedSubType) {
		OrganisationTypeToTypeImpl reloadedParentType = new OrganisationTypeToTypeImpl();
		reloadedParentType.setOrganisationType(parentType);
		reloadedParentType.setAllowedSubOrganisationType(allowedSubType);
		dbInstance.getCurrentEntityManager().persist(reloadedParentType);
	}
}
