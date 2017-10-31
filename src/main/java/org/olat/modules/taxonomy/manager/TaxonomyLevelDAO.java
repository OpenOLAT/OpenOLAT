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

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.TypedQuery;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.model.TaxonomyLevelImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 
 * Initial date: 22 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyLevelDAO implements InitializingBean {

	private File rootDirectory, taxonomyLevelDirectory;
	
	@Autowired
	private DB dbInstance;
	
	@Override
	public void afterPropertiesSet() {
		File bcrootDirectory = new File(FolderConfig.getCanonicalRoot());
		rootDirectory = new File(bcrootDirectory, "taxonomy");
		taxonomyLevelDirectory = new File(rootDirectory, "levels");
		if(!taxonomyLevelDirectory.exists()) {
			taxonomyLevelDirectory.mkdirs();
		}
	}
	
	public TaxonomyLevel createTaxonomyLevel(String identifier, String displayName, String description,
			String externalId, TaxonomyLevelManagedFlag[] flags,
			TaxonomyLevel parent, TaxonomyLevelType type, Taxonomy taxonomy) {
		TaxonomyLevelImpl level = new TaxonomyLevelImpl();
		level.setCreationDate(new Date());
		level.setLastModified(level.getCreationDate());
		level.setEnabled(true);
		if(StringHelper.containsNonWhitespace(identifier)) {
			level.setIdentifier(identifier);
		} else {
			level.setIdentifier(UUID.randomUUID().toString());
		}
		level.setManagedFlagsString(TaxonomyLevelManagedFlag.toString(flags));
		level.setDisplayName(displayName);
		level.setDescription(description);
		level.setExternalId(externalId);
		level.setTaxonomy(taxonomy);
		level.setType(type);
		
		dbInstance.getCurrentEntityManager().persist(level);
		String storage = createLevelStorage(taxonomy, level);
		level.setDirectoryPath(storage);
		
		if(parent != null) {
			level.setParent(parent);
			
			String parentPathOfKeys = ((TaxonomyLevelImpl)parent).getMaterializedPathKeys();
			if(parentPathOfKeys == null || "/".equals(parentPathOfKeys)) {
				parentPathOfKeys = "";
			}
			String parentPathOfIdentifiers = ((TaxonomyLevelImpl)parent).getMaterializedPathIdentifiers();
			if(parentPathOfIdentifiers == null || "/".equals(parentPathOfIdentifiers)) {
				parentPathOfIdentifiers = "";
			}

			level.setMaterializedPathKeys(parentPathOfKeys + level.getKey() + "/");
			level.setMaterializedPathIdentifiers(parentPathOfIdentifiers + level.getIdentifier()  + "/");
		} else {
			level.setMaterializedPathKeys("/" + level.getKey() + "/");
			level.setMaterializedPathIdentifiers("/" + level.getIdentifier()  + "/");
		}

		level = dbInstance.getCurrentEntityManager().merge(level);
		level.getTaxonomy();
		return level;
	}
	
	public TaxonomyLevel loadByKey(Long key) {
		List<TaxonomyLevel> levels = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTaxonomyLevelsByKey", TaxonomyLevel.class)
				.setParameter("levelKey", key)
				.getResultList();
		return levels == null || levels.isEmpty() ? null : levels.get(0);
	}
	
	public List<TaxonomyLevel> getLevels(TaxonomyRef taxonomy) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select level from ctaxonomylevel as level")
		  .append(" left join fetch level.parent as parent")
		  .append(" left join fetch level.type as type")
		  .append(" inner join fetch level.taxonomy as taxonomy");
		if(taxonomy != null) {
			sb.append(" where level.taxonomy.key=:taxonomyKey");
		}
		
		TypedQuery<TaxonomyLevel> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyLevel.class);
		if(taxonomy != null) {
			query.setParameter("taxonomyKey", taxonomy.getKey());
		}
		return query.getResultList();
	}
	
	public List<TaxonomyLevel> getLevelsByExternalId(TaxonomyRef taxonomy, String externalId) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select level from ctaxonomylevel as level")
		  .append(" left join fetch level.parent as parent")
		  .append(" left join fetch level.type as type")
		  .append(" inner join fetch level.taxonomy as taxonomy")
		  .append(" where level.taxonomy.key=:taxonomyKey and level.externalId=:externalId");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyLevel.class)
			.setParameter("taxonomyKey", taxonomy.getKey())
			.setParameter("externalId", externalId)
			.getResultList();
	}
	
	public List<TaxonomyLevel> getLevelsByDisplayName(TaxonomyRef taxonomy, String displayName) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select level from ctaxonomylevel as level")
		  .append(" left join fetch level.parent as parent")
		  .append(" left join fetch level.type as type")
		  .append(" inner join fetch level.taxonomy as taxonomy")
		  .append(" where level.taxonomy.key=:taxonomyKey and level.displayName=:displayName");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyLevel.class)
			.setParameter("taxonomyKey", taxonomy.getKey())
			.setParameter("displayName", displayName)
			.getResultList();
	}
	
	// Perhaps replace it with a select in ( materializedPathKeys.split("[/]") ) would be better
	public List<TaxonomyLevel> getParentLine(TaxonomyLevel taxonomyLevel, Taxonomy taxonomy) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select level from ctaxonomylevel as level")
		  .append(" left join fetch level.parent as parent")
		  .append(" left join fetch level.type as type")
		  .append(" where level.taxonomy.key=:taxonomyKey")
		  .append(" and locate(level.materializedPathKeys,:materializedPath) = 1");
		  
		List<TaxonomyLevel> levels = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyLevel.class)
			.setParameter("materializedPath", taxonomyLevel.getMaterializedPathKeys() + "%")
			.setParameter("taxonomyKey", taxonomy.getKey())
			.getResultList();
		Collections.sort(levels, new PathMaterializedPathLengthComparator());
		return levels;
	}
	
	public TaxonomyLevel updateTaxonomyLevel(TaxonomyLevel level) {
		((TaxonomyLevelImpl)level).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(level);
	}
	
	public VFSContainer getDocumentsLibrary(TaxonomyLevel level) {
		String path = ((TaxonomyLevelImpl)level).getDirectoryPath();
		return new OlatRootFolderImpl(path, null);
	}
	
	public String createLevelStorage(Taxonomy taxonomy, TaxonomyLevel level) {
		File taxonomyDirectory = new File(taxonomyLevelDirectory, taxonomy.getKey().toString());
		File storage = new File(taxonomyDirectory, level.getKey().toString());
		
		Path relativePath = rootDirectory.toPath().relativize(storage.toPath());
		String relativePathString = relativePath.toString();
		return relativePathString;
	}
	
	private static class PathMaterializedPathLengthComparator implements Comparator<TaxonomyLevel> {
		@Override
		public int compare(TaxonomyLevel l1, TaxonomyLevel l2) {
			String s1 = l1.getMaterializedPathKeys();
			String s2 = l2.getMaterializedPathKeys();
			
			int len1 = s1 == null ? 0 : s1.length();
			int len2 = s2 == null ? 0 : s2.length();
			return len1 - len2;
		}
	}
}
