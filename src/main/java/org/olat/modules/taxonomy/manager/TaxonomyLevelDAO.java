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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelImpl;
import org.olat.modules.taxonomy.model.TaxonomyLevelSearchParameters;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyLevelDAO implements InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(TaxonomyLevelDAO.class);

	private File rootDirectory, taxonomyLevelDirectory, taxonomyLevelMediaDirectory;
	
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
		taxonomyLevelMediaDirectory = new File(rootDirectory, "levels_media");
		if(!taxonomyLevelMediaDirectory.exists()) {
			taxonomyLevelMediaDirectory.mkdirs();
		}
	}
	
	/**
	 * Do not set display name and description. Use the i18nManager.
	 * Thouse values are only present here because of the Upgrader. 
	 */
	@SuppressWarnings("deprecation")
	public TaxonomyLevel createTaxonomyLevel(String identifier, String i18nSuffix, String displayName,
			String description, String externalId,
			TaxonomyLevelManagedFlag[] flags, TaxonomyLevel parent, TaxonomyLevelType type, Taxonomy taxonomy) {
		TaxonomyLevelImpl level = new TaxonomyLevelImpl();
		level.setCreationDate(new Date());
		level.setLastModified(level.getCreationDate());
		level.setEnabled(true);
		if(StringHelper.containsNonWhitespace(identifier)) {
			level.setIdentifier(identifier);
		} else {
			level.setIdentifier(UUID.randomUUID().toString());
		}
		level.setI18nSuffix(i18nSuffix);
		level.setManagedFlagsString(TaxonomyLevelManagedFlag.toString(flags));
		level.setDisplayName(displayName);
		level.setDescription(description);
		level.setExternalId(externalId);
		level.setTaxonomy(taxonomy);
		level.setType(type);
		
		dbInstance.getCurrentEntityManager().persist(level);
		String storage = createLevelStorage(taxonomy, level);
		level.setDirectoryPath(storage);
		String mediaPath = createLevelMediaStorage(taxonomy, level);
		level.setMediaPath(mediaPath);
		
		String identifiersPath = getMaterializedPathIdentifiers(parent, level);
		String keysPath = getMaterializedPathKeys(parent, level);
		level.setParent(parent);
		level.setMaterializedPathKeys(keysPath);
		level.setMaterializedPathIdentifiers(identifiersPath);

		level = dbInstance.getCurrentEntityManager().merge(level);
		level.getTaxonomy();
		return level;
	}
	
	private String getMaterializedPathIdentifiers(TaxonomyLevel parent, TaxonomyLevel level) {
		if(parent != null) {
			String parentPathOfIdentifiers = parent.getMaterializedPathIdentifiers();
			if(parentPathOfIdentifiers == null || "/".equals(parentPathOfIdentifiers)) {
				parentPathOfIdentifiers = "/";
			}
			return parentPathOfIdentifiers + level.getIdentifier()  + "/";
		}
		return "/" + level.getIdentifier()  + "/";
	}
	
	private String getMaterializedPathKeys(TaxonomyLevel parent, TaxonomyLevel level) {
		if(parent != null) {
			String parentPathOfKeys = parent.getMaterializedPathKeys();
			if(parentPathOfKeys == null || "/".equals(parentPathOfKeys)) {
				parentPathOfKeys = "";
			}
			return parentPathOfKeys + level.getKey() + "/";
		}
		return "/" + level.getKey() + "/";
	}
	
	public TaxonomyLevel loadByKey(Long key) {
		List<TaxonomyLevel> levels = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTaxonomyLevelsByKey", TaxonomyLevel.class)
				.setParameter("levelKey", key)
				.getResultList();
		return levels == null || levels.isEmpty() ? null : levels.get(0);
	}
	
	public List<TaxonomyLevel> loadLevels(Collection<? extends TaxonomyLevelRef> refs) {
		if (refs == null || refs.isEmpty()) return new ArrayList<>(0);
		
		Collection<Long> keys = refs.stream().map(TaxonomyLevelRef::getKey).collect(Collectors.toList());
		return loadLevelsByKeys(keys);
	}
	
	public List<TaxonomyLevel> loadLevelsByKeys(Collection<Long> keys) {
		if (keys == null || keys.isEmpty()) return new ArrayList<>(0);
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select level from ctaxonomylevel as level");
		sb.append(" where level.key in (:keys)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TaxonomyLevel.class)
				.setParameter("keys", keys)
				.getResultList();
	}
	
	public List<TaxonomyLevel> getLevels(Collection<? extends TaxonomyRef> refs) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select level from ctaxonomylevel as level")
		  .append(" left join fetch level.parent as parent")
		  .append(" left join fetch level.type as type")
		  .append(" inner join fetch level.taxonomy as taxonomy");
		if(refs != null && !refs.isEmpty()) {
			sb.append(" where level.taxonomy.key in :taxonomyKeys");
		}
		
		TypedQuery<TaxonomyLevel> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyLevel.class);
		if(refs != null && !refs.isEmpty()) {
			List<Long> taxonomyKeys = refs.stream().map(TaxonomyRef::getKey).collect(Collectors.toList());
			query.setParameter("taxonomyKeys", taxonomyKeys);
		}
		return query.getResultList();
	}
	
	public List<TaxonomyLevel> searchLevels(TaxonomyRef taxonomy, TaxonomyLevelSearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select level from ctaxonomylevel as level")
		  .append(" left join fetch level.parent as parent")
		  .append(" left join fetch level.type as type")
		  .append(" inner join fetch level.taxonomy as taxonomy");
		
		if (taxonomy != null) {
		  sb.where().append("level.taxonomy.key=:taxonomyKey");
		}

		//quick search
		Long quickId = null;
		String quickRefs = null;
		String quickText = null;
		Collection<String> quickI18nSuffix = null;
		if(StringHelper.containsNonWhitespace(searchParams.getQuickSearch())) {
			quickRefs = searchParams.getQuickSearch();
			quickText = PersistenceHelper.makeFuzzyQueryString(quickRefs);
			sb.where().append("(level.externalId=:quickRef or ");
			PersistenceHelper.appendFuzzyLike(sb, "level.identifier", "quickText", dbInstance.getDbVendor());
			if(StringHelper.isLong(quickRefs)) {
				try {
					quickId = Long.parseLong(quickRefs);
					sb.append(" or level.key=:quickVKey");
				} catch (NumberFormatException e) {
					//
				}
			}
			if (searchParams.getQuickSearchI18nSuffix() != null && !searchParams.getQuickSearchI18nSuffix().isEmpty()) {
				sb.append(" or level.i18nSuffix in :quickI18nSuffix");
				quickI18nSuffix = searchParams.getQuickSearchI18nSuffix();
			}
			sb.append(")");	
		}
		
		if(searchParams.isAllowedAsSubject() != null) {
			sb.where().append("type.allowedAsSubject=:subjectAllowed");
		}
		
		if(searchParams.getTaxonomyKeys() != null) {
			sb.where().append("taxonomy.key in :taxonomyKeys");
		}

		TypedQuery<TaxonomyLevel> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyLevel.class);
		if (taxonomy != null) {
			query.setParameter("taxonomyKey", taxonomy.getKey());
		}
		if(quickId != null) {
			query.setParameter("quickVKey", quickId);
		}
		if(quickRefs != null) {
			query.setParameter("quickRef", quickRefs);
		}
		if(quickText != null) {
			query.setParameter("quickText", quickText);
		}
		if(quickI18nSuffix != null) {
			query.setParameter("quickI18nSuffix", quickI18nSuffix);
		}
		if (searchParams.isAllowedAsSubject() != null) {
			query.setParameter("subjectAllowed", searchParams.isAllowedAsSubject().booleanValue());
		}
		if (searchParams.getTaxonomyKeys() != null) {
			query.setParameter("taxonomyKeys", searchParams.getTaxonomyKeys());
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
	
	public List<TaxonomyLevel> getLevelsByI18nSuffix(TaxonomyRef taxonomy, Collection<String> i18nSuffix) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select level from ctaxonomylevel as level")
		  .append(" left join fetch level.parent as parent")
		  .append(" left join fetch level.type as type")
		  .append(" inner join fetch level.taxonomy as taxonomy")
		  .append(" where level.taxonomy.key=:taxonomyKey and level.i18nSuffix in :i18nSuffix");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyLevel.class)
			.setParameter("taxonomyKey", taxonomy.getKey())
			.setParameter("i18nSuffix", i18nSuffix)
			.getResultList();
	}
	
	public List<TaxonomyLevel> getLevelsByType(TaxonomyLevelType levelType) {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select level from ctaxonomylevel as level")
		  .append(" left join fetch level.type as type")
		  .append(" where level.type.key=:typeKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyLevel.class)
			.setParameter("typeKey", levelType.getKey())
			.getResultList();
	}
	
	public TaxonomyLevel getParent(TaxonomyLevelRef taxonomyLevel) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select level.parent from ctaxonomylevel as level")
		  .append(" where level.key=:taxonomyLevelKey");
		List<TaxonomyLevel> levels = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TaxonomyLevel.class)
				.setParameter("taxonomyLevelKey", taxonomyLevel.getKey())
				.getResultList();
		return levels == null || levels.isEmpty() ? null : levels.get(0);
	}
	
	// Perhaps replace it with a select in ( materializedPathKeys.split("[/]") ) would be better
	public List<TaxonomyLevel> getParentLine(TaxonomyLevel taxonomyLevel, TaxonomyRef taxonomy) {
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
	
	public List<TaxonomyLevel> getDescendants(TaxonomyLevel taxonomyLevel, TaxonomyRef taxonomy) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select level from ctaxonomylevel as level")
		  .append(" left join fetch level.parent as parent")
		  .append(" left join fetch level.type as type")
		  .append(" where level.key!=:levelKey and level.materializedPathKeys like :materializedPath");
		if (taxonomy != null) {
			sb.append(" and level.taxonomy.key=:taxonomyKey");
		}
		
		TypedQuery<TaxonomyLevel> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyLevel.class)
			.setParameter("materializedPath", taxonomyLevel.getMaterializedPathKeys() + "%")
			.setParameter("levelKey", taxonomyLevel.getKey());
		if (taxonomy != null) {
			query.setParameter("taxonomyKey", taxonomy.getKey());
		}
		List<TaxonomyLevel> levels = query.getResultList();
		Collections.sort(levels, new PathMaterializedPathLengthComparator());
		return levels;
	}
	
	public List<TaxonomyLevel> getChildren(TaxonomyRef taxonomy) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select level from ctaxonomylevel as level")
		  .append(" inner join level.taxonomy as taxonomy")
		  .append(" where level.parent.key is null")
		  .append("   and taxonomy.key = :taxonomyKey");
		  
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyLevel.class)
			.setParameter("taxonomyKey", taxonomy.getKey())
			.getResultList();
	}
	
	public List<TaxonomyLevel> getChildren(TaxonomyLevel taxonomyLevel) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select level from ctaxonomylevel as level")
		  .append(" inner join fetch level.parent as parent")
		  .append(" where parent.key=:parentKey");
		  
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyLevel.class)
			.setParameter("parentKey", taxonomyLevel.getKey())
			.getResultList();
	}
	
	public TaxonomyLevel updateTaxonomyLevel(TaxonomyLevel level) {
		TaxonomyLevel parentLevel = getParent(level);
		String path = level.getMaterializedPathIdentifiers();
		String newPath = getMaterializedPathIdentifiers(parentLevel, level);
		boolean updatePath = !newPath.equals(path);
		if(updatePath) {
			((TaxonomyLevelImpl)level).setMaterializedPathIdentifiers(newPath);
		}

		((TaxonomyLevelImpl)level).setLastModified(new Date());
		TaxonomyLevel mergedLevel = dbInstance.getCurrentEntityManager().merge(level);
		
		if(updatePath) {
			List<TaxonomyLevel> descendants = getDescendants(mergedLevel, mergedLevel.getTaxonomy());
			for(TaxonomyLevel descendant:descendants) {
				String descendantPath = descendant.getMaterializedPathIdentifiers();
				if(descendantPath.indexOf(path) == 0) {
					String end = descendantPath.substring(path.length(), descendantPath.length());
					String updatedPath = newPath + end;
					((TaxonomyLevelImpl)descendant).setMaterializedPathIdentifiers(updatedPath);
				}
				dbInstance.getCurrentEntityManager().merge(descendant);
			}
		}
		return mergedLevel;
	}
	
	/**
	 * Move use a lock on the taxonomy to prevent concurrent changes. Therefore
	 * the method will commit the changes as soon as possible.
	 * 
	 * @param level The level to move
	 * @param newParentLevel The new parent level, if null the level move as a root
	 * @return The updated level
	 */
	public TaxonomyLevel moveTaxonomyLevel(TaxonomyLevel level, TaxonomyLevel newParentLevel) {
		@SuppressWarnings("unused")
		Taxonomy lockedTaxonomy = loadForUpdate(level.getTaxonomy());

		TaxonomyLevel parentLevel = getParent(level);
		if(parentLevel == null && newParentLevel == null) {
			return level;//already root
		} else if(parentLevel != null && parentLevel.equals(newParentLevel)) {
			return level;//same parent
		}

		String keysPath = level.getMaterializedPathKeys();
		String identifiersPath = level.getMaterializedPathIdentifiers();
		
		List<TaxonomyLevel> descendants = getDescendants(level, level.getTaxonomy());
		TaxonomyLevelImpl levelImpl = (TaxonomyLevelImpl)level;
		levelImpl.setParent(newParentLevel);
		levelImpl.setLastModified(new Date());
		String newKeysPath = getMaterializedPathKeys(newParentLevel, levelImpl);
		String newIdentifiersPath = getMaterializedPathIdentifiers(newParentLevel, levelImpl);
		levelImpl.setMaterializedPathKeys(newKeysPath);
		levelImpl.setMaterializedPathIdentifiers(newIdentifiersPath);
		levelImpl = dbInstance.getCurrentEntityManager().merge(levelImpl);

		for(TaxonomyLevel descendant:descendants) {
			String descendantKeysPath = descendant.getMaterializedPathKeys();
			String descendantIdentifiersPath = descendant.getMaterializedPathIdentifiers();
			if(descendantIdentifiersPath.indexOf(identifiersPath) == 0) {
				String end = descendantIdentifiersPath.substring(identifiersPath.length(), descendantIdentifiersPath.length());
				String updatedPath = newIdentifiersPath + end;
				((TaxonomyLevelImpl)descendant).setMaterializedPathIdentifiers(updatedPath);
			}
			if(descendantKeysPath.indexOf(keysPath) == 0) {
				String end = descendantKeysPath.substring(keysPath.length(), descendantKeysPath.length());
				String updatedPath = newKeysPath + end;
				((TaxonomyLevelImpl)descendant).setMaterializedPathKeys(updatedPath);
			}
			dbInstance.getCurrentEntityManager().merge(descendant);
		}
		
		dbInstance.commit();
		return levelImpl;
	}
	
	public boolean canDelete(TaxonomyLevelRef taxonomyLevel) {
		if(!hasChildren(taxonomyLevel) && !hasItemUsing(taxonomyLevel) && !hasCompetenceUsing(taxonomyLevel)) {
			return true;
		}
		return false;
	}
	
	public boolean delete(TaxonomyLevelRef taxonomyLevel) {
		if(canDelete(taxonomyLevel)) {
			TaxonomyLevel impl = loadByKey(taxonomyLevel.getKey());
			if(impl != null) {
				dbInstance.getCurrentEntityManager().remove(impl);
			}
			return true;
		}
		return false;
	}
	
	public boolean hasItemUsing(TaxonomyLevelRef taxonomyLevel) {
		String sb = "select item.key from questionitem item where item.taxonomyLevel.key=:taxonomyLevelKey";
		List<Long> items = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("taxonomyLevelKey", taxonomyLevel.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return items != null && items.size() > 0 && items.get(0) != null && items.get(0).longValue() > 0;
	}
	
	public boolean hasCompetenceUsing(TaxonomyLevelRef taxonomyLevel) {
		String sb = "select competence.key from ctaxonomycompetence competence where competence.taxonomyLevel.key=:taxonomyLevelKey";
		List<Long> comptences = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("taxonomyLevelKey", taxonomyLevel.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return comptences != null && comptences.size() > 0 && comptences.get(0) != null && comptences.get(0).longValue() > 0;
	}
	
	public boolean hasChildren(TaxonomyLevelRef taxonomyLevel) {
		String sb = "select level.key from ctaxonomylevel as level where level.parent.key=:taxonomyLevelKey";
		List<Long> children = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("taxonomyLevelKey", taxonomyLevel.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return children != null && children.size() > 0 && children.get(0) != null && children.get(0).longValue() > 0;
	}
	
	public Taxonomy loadForUpdate(Taxonomy taxonomy) {
		//first remove it from caches
		dbInstance.getCurrentEntityManager().detach(taxonomy);

		String query = "select taxonomy from ctaxonomy taxonomy where taxonomy.key=:taxonomyKey";
		List<Taxonomy> entries = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Taxonomy.class)
				.setParameter("taxonomyKey", taxonomy.getKey())
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getResultList();
		return entries == null || entries.isEmpty() ? null : entries.get(0);
	}
	
	public VFSContainer getDocumentsLibrary(TaxonomyLevel level) {
		String path = ((TaxonomyLevelImpl)level).getDirectoryPath();
		if(!path.startsWith("/")) {
			path = "/" + path;
		}
		path = "/" + TaxonomyService.DIRECTORY + path;
		return VFSManager.olatRootContainer(path, null);
	}
	
	public String createLevelStorage(Taxonomy taxonomy, TaxonomyLevel level) {
		File taxonomyDirectory = new File(taxonomyLevelDirectory, taxonomy.getKey().toString());
		return createLevelStorage(taxonomyDirectory, level);
	}
	
	public String createLevelMediaStorage(Taxonomy taxonomy, TaxonomyLevel level) {
		File taxonomyDirectory = new File(taxonomyLevelMediaDirectory, taxonomy.getKey().toString());
		return createLevelStorage(taxonomyDirectory, level);
	}

	private String createLevelStorage(File taxonomyDirectory, TaxonomyLevel level) {
		File storage = new File(taxonomyDirectory, level.getKey().toString());
		storage.mkdirs();
		
		Path relativePath = rootDirectory.toPath().relativize(storage.toPath());
		return relativePath.toString();
	}
	
	public VFSContainer getLevelMediaStorage(TaxonomyLevel level) {
		String path = ((TaxonomyLevelImpl)level).getMediaPath();
		if(!path.startsWith("/")) {
			path = "/" + path;
		}
		path = "/" + TaxonomyService.DIRECTORY + path;
		return VFSManager.olatRootContainer(path, null);
	}

	public VFSLeaf getBackgroundImage(TaxonomyLevel level) {
		return getFirstImage(level, "background");
	}

	public VFSLeaf getTeaserImage(TaxonomyLevel level) {
		return getFirstImage(level, "teaser");
	}
	
	public VFSLeaf getFirstImage(TaxonomyLevel level, String path) {
		if (level != null) {
			VFSContainer mediaStorage = getLevelMediaStorage(level);
			VFSContainer imageContainer = VFSManager.getOrCreateContainer(mediaStorage, path);
			if (!imageContainer.getItems().isEmpty()) {
				VFSItem vfsItem = imageContainer.getItems().get(0);
				if (vfsItem instanceof VFSLeaf) {
					return (VFSLeaf)vfsItem;
				}
			}
		}
		return null;
	}
	
	public boolean storeBackgroundImage(TaxonomyLevel level, Identity savedBy, File file, String filename) {
		return storeImage(level, "background", savedBy, file, filename);
	}
	
	public boolean storeTeaserImage(TaxonomyLevel level, Identity savedBy, File file, String filename) {
		return storeImage(level, "teaser", savedBy, file, filename);
	}
	
	public boolean storeImage(TaxonomyLevel level, String path, Identity savedBy, File file, String filename) {
		if (file == null || !file.exists() || !file.isFile()) {
			return false;
		}
		
		try {
			VFSContainer imageContainer = VFSManager.getOrCreateContainer(getLevelMediaStorage(level), path);
			tryToStore(imageContainer, savedBy, file, filename);
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
		
		return true;
	}

	public void deleteBackgroundImage(TaxonomyLevel level) {
		deleteImage(level, "background");
	}

	public void deleteTeaserImage(TaxonomyLevel level) {
		deleteImage(level, "teaser");
	}
	
	public void deleteImage(TaxonomyLevel level, String path) {
		VFSContainer imageContainer = VFSManager.getOrCreateContainer(getLevelMediaStorage(level), path);
		imageContainer.delete();
	}
	
	private void tryToStore(VFSContainer imageContainer, Identity savedBy, File file, String filename) {
		imageContainer.delete();
		
		String cleandFilename = FileUtils.cleanFilename(filename);
		VFSLeaf vfsLeaf = VFSManager.resolveOrCreateLeafFromPath(imageContainer, cleandFilename);
		VFSManager.copyContent(file, vfsLeaf, savedBy);
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
