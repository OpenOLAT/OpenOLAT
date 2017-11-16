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
package org.olat.modules.taxonomy.manager;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeRef;
import org.olat.modules.taxonomy.TaxonomyLevelTypeToType;
import org.olat.modules.taxonomy.model.TaxonomyLevelTypeToTypeImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyLevelTypeToTypeDAO {
	
	@Autowired
	private DB dbInstance;
	
	public int disallowedSubType(TaxonomyLevelType parentType, TaxonomyLevelType disallowedSubType) {
		String q = "delete from ctaxonomyleveltypetotype type2type where type2type.taxonomyLevelType.key=:typeKey and type2type.allowedSubTaxonomyLevelType.key=:subTypeKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("typeKey", parentType.getKey())
				.setParameter("subTypeKey", disallowedSubType.getKey())
				.executeUpdate();
	}
	
	public void setAllowedSubType(TaxonomyLevelType parentType, List<TaxonomyLevelType> allowSubTypes) {
		List<TaxonomyLevelTypeToType> typeToTypes = getAllowedSubTypes(parentType);
		for(TaxonomyLevelTypeToType typeToType:typeToTypes) {
			boolean found = false;
			for(TaxonomyLevelType allowSubType:allowSubTypes) {
				if(typeToType.getAllowedSubTaxonomyLevelType().equals(allowSubType)) {
					found = true;
					break;
				}
			}

			if(!found) {
				dbInstance.getCurrentEntityManager().remove(typeToType);
			}
		}

		for(TaxonomyLevelType allowSubType:allowSubTypes) {
			boolean found = false;
			for(TaxonomyLevelTypeToType typeToType:typeToTypes) {
				if(typeToType.getAllowedSubTaxonomyLevelType().equals(allowSubType)) {
					found = true;
					break;
				}
			}

			if(!found) {
				addAllowedSubType(parentType, allowSubType);
			}
		}
	}
	
	public List<TaxonomyLevelTypeToType> getAllowedSubTypes(TaxonomyLevelTypeRef parentType) {
		String q = "select type2type from ctaxonomyleveltypetotype type2type inner join fetch type2type.allowedSubTaxonomyLevelType subType where type2type.taxonomyLevelType.key=:typeKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, TaxonomyLevelTypeToType.class)
				.setParameter("typeKey", parentType.getKey())
				.getResultList();
	}
	
	public List<TaxonomyLevelTypeToType> getAllowedSubTypes(TaxonomyLevelType parentType, TaxonomyLevelType allowedSubType) {
		String q = "select type2type from ctaxonomyleveltypetotype type2type where type2type.taxonomyLevelType.key=:typeKey and type2type.allowedSubTaxonomyLevelType.key=:subTypeKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, TaxonomyLevelTypeToType.class)
				.setParameter("typeKey", parentType.getKey())
				.setParameter("subTypeKey", allowedSubType.getKey())
				.getResultList();
	}
	
	public int deleteAllowedSubTypes(TaxonomyLevelTypeRef parentType) {
		String q = "delete from ctaxonomyleveltypetotype where taxonomyLevelType.key=:typeKey";
		int rows = dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("typeKey", parentType.getKey())
				.executeUpdate();
		
		String qReverse = "delete from ctaxonomyleveltypetotype where allowedSubTaxonomyLevelType.key=:typeKey";
		rows += dbInstance.getCurrentEntityManager()
				.createQuery(qReverse)
				.setParameter("typeKey", parentType.getKey())
				.executeUpdate();
		
		return rows;
	}
	
	public void addAllowedSubType(TaxonomyLevelType parentType, TaxonomyLevelType allowedSubType) {
		TaxonomyLevelTypeToTypeImpl reloadedParentType = new TaxonomyLevelTypeToTypeImpl();
		reloadedParentType.setTaxonomyLevelType(parentType);
		reloadedParentType.setAllowedSubTaxonomyLevelType(allowedSubType);
		dbInstance.getCurrentEntityManager().persist(reloadedParentType);
	}
}
