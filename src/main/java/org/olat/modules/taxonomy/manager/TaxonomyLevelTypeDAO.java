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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeRef;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.model.TaxonomyLevelTypeImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaxonomyLevelTypeDAO {

	@Autowired
	private DB dbInstance;
	
	public TaxonomyLevelType createTaxonomyLevelType(String identifier, String displayName, String description, String externalId,
			Taxonomy taxonomy) {
		TaxonomyLevelTypeImpl type = new TaxonomyLevelTypeImpl();
		type.setCreationDate(new Date());
		type.setLastModified(type.getCreationDate());
		if(StringHelper.containsNonWhitespace(identifier)) {
			type.setIdentifier(identifier);
		} else {
			type.setIdentifier(UUID.randomUUID().toString());
		}
		type.setDisplayName(displayName);
		type.setDescription(description);
		type.setExternalId(externalId);
		// default settings
		type.setDocumentsLibraryManageCompetenceEnabled(true);
		type.setDocumentsLibraryTeachCompetenceReadEnabled(true);
		type.setDocumentsLibraryTeachCompetenceReadParentLevels(0);
		type.setDocumentsLibraryTeachCompetenceWriteEnabled(false);
		type.setDocumentsLibraryHaveCompetenceReadEnabled(true);
		type.setDocumentsLibraryTargetCompetenceReadEnabled(true);
		// root
		type.setTaxonomy(taxonomy);
		
		dbInstance.getCurrentEntityManager().persist(type);
		return type;
	}
	
	public TaxonomyLevelType loadTaxonomyLevelTypeByKey(Long key) {
		List<TaxonomyLevelType> types = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTaxonomyLevelTypeByKey", TaxonomyLevelType.class)
				.setParameter("typeKey", key)
				.getResultList();
		return types == null || types.isEmpty() ? null : types.get(0);	
	}
	
	public TaxonomyLevelType cloneTaxonomyLevelType(TaxonomyLevelTypeRef typeRef) {
		TaxonomyLevelType reloadedType = loadTaxonomyLevelTypeByKey(typeRef.getKey());
		
		TaxonomyLevelTypeImpl type = new TaxonomyLevelTypeImpl();
		type.setCreationDate(new Date());
		type.setLastModified(type.getCreationDate());
		type.setIdentifier(reloadedType.getIdentifier() + " (Copy)");
		type.setDisplayName(reloadedType.getDisplayName());
		type.setDescription(reloadedType.getDescription());
		type.setExternalId("");
		// default settings
		type.setDocumentsLibraryManageCompetenceEnabled(reloadedType.isDocumentsLibraryManageCompetenceEnabled());
		type.setDocumentsLibraryTeachCompetenceReadEnabled(reloadedType.isDocumentsLibraryTargetCompetenceReadEnabled());
		type.setDocumentsLibraryTeachCompetenceReadParentLevels(reloadedType.getDocumentsLibraryTeachCompetenceReadParentLevels());
		type.setDocumentsLibraryTeachCompetenceWriteEnabled(reloadedType.isDocumentsLibraryTeachCompetenceWriteEnabled());
		type.setDocumentsLibraryHaveCompetenceReadEnabled(reloadedType.isDocumentsLibraryHaveCompetenceReadEnabled());
		type.setDocumentsLibraryTargetCompetenceReadEnabled(reloadedType.isDocumentsLibraryTargetCompetenceReadEnabled());
		// root
		type.setTaxonomy(reloadedType.getTaxonomy());
		
		dbInstance.getCurrentEntityManager().persist(type);
		return type;
	}
	
	public TaxonomyLevelType updateTaxonomyLevelType(TaxonomyLevelType type) {
		((TaxonomyLevelTypeImpl)type).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(type);
	}
	
	public List<TaxonomyLevelType> loadTaxonomyLevelTypeByTaxonomy(TaxonomyRef taxonomy) {
		String q = "select type from ctaxonomyleveltype type inner join fetch type.taxonomy taxonomy where taxonomy.key=:taxonomyKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, TaxonomyLevelType.class)
				.setParameter("taxonomyKey", taxonomy.getKey())
				.getResultList();
	}
	
	public boolean deleteTaxonomyLevelType(TaxonomyLevelTypeRef levelType) {
		TaxonomyLevelType reloadedLevel = dbInstance.getCurrentEntityManager()
			.getReference(TaxonomyLevelTypeImpl.class, levelType.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedLevel);
		return true;
	}
	
	public boolean hasLevels(TaxonomyLevelTypeRef type) {
		String sb = "select level.key from ctaxonomylevel as level where level.type.key=:typeKey";
		List<Long> levels = dbInstance.getCurrentEntityManager()
			.createQuery(sb, Long.class)
			.setParameter("typeKey", type.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return levels != null && !levels.isEmpty() && levels.get(0) != null && levels.get(0).longValue() > 0;
	}
}
