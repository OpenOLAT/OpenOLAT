/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.repository.manager;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.Resourceable;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.repository.CatalogEntry;
import org.olat.repository.CatalogEntryRef;
import org.olat.repository.RepositoryDeletionModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.model.CatalogEntryImpl;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description: <br>
 * The CatalogManager is responsible for the persistence of CatalogEntries.
 * Further it provides access methods to retrieve structures of CatalogEntries
 * for a given CatalogEntry, e.g. children, catalog entries which act as roots,
 * delete subcategory structure.
 * <p>
 * Moreover it also has access methods providing all catalog entries referencing
 * a given repository entry.
 * <p>
 * The CatalogManager also provides hooks used by the repository entry manager
 * to signal changes on a repository entry which might have changed. Such
 * changes can invoke the removal from the catalog, e.g. restricting access,
 * deleting a repository entry.
 * 
 * Date: 2005/10/14 13:21:42<br>
 * @author Felix Jost
 */
@Service("catalogManager")
public class CatalogManager implements UserDataDeletable, InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(CatalogManager.class);
	
	/**
	 * Default value for the catalog root <code>CATALOGROOT</code>
	 */
	public static final String CATALOGROOT = "CATALOG ROOT";
	/**
	 * Resource identifier for catalog entries
	 */
	public static final String CATALOGENTRY = "CatalogEntry";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ImageService imageHelper;
	@Autowired
	private SecurityGroupDAO securityGroupDao;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;


	/**
	 * @return transient catalog entry object
	 */
	public CatalogEntry createCatalogEntry() {
		CatalogEntryImpl entry = new CatalogEntryImpl();
		
		entry.setOwnerGroup(securityGroupDao.createAndPersistSecurityGroup());
		return entry;
	}
	
	public List<CatalogEntry> getNodesChildrenOf(CatalogEntry ce) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cei from catalogentry as cei ")
		  .append(" inner join fetch cei.ownerGroup as ownerGroup")
		  .append(" inner join fetch cei.parent as parentCei")
		  .append(" inner join fetch parentCei.ownerGroup as parentOwnerGroup")
		  .append(" where parentCei.key=:parentKey and cei.type=").append(CatalogEntry.TYPE_NODE);

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogEntry.class)
				.setParameter("parentKey", ce.getKey())
				.getResultList();
	}

	/**
	 * Children of this CatalogEntry as a list of CatalogEntries
	 * @param ce
	 * @return List of catalog entries that are childern entries of given entry
	 */
	public List<CatalogEntry> getChildrenOf(CatalogEntry ce) {
		List<CatalogEntry> children = getChildrenOf(ce, 0, -1, CatalogEntry.OrderBy.position, true);

		if (isCategorySortingManually(ce) || isEntrySortingManually(ce)) {
			// Create 3 lists: Categories, entries, closed entries
			List<CatalogEntry> categories = children.stream()
					.filter(catalogEntry -> catalogEntry.getType() == CatalogEntry.TYPE_NODE)
					.collect(Collectors.toList());

			List<CatalogEntry> entries = children.stream().filter(catalogEntry -> catalogEntry.getType() == CatalogEntry.TYPE_LEAF
					&& (catalogEntry.getRepositoryEntry() == null || !RepositoryEntryStatusEnum.closed.equals(catalogEntry.getRepositoryEntry().getEntryStatus())))
					.collect(Collectors.toList());
			// To be sure only correct entries are in the final list, the last step is also filtered
			List<CatalogEntry> closedEntries = children.stream().filter(catalogEntry -> catalogEntry.getType() == CatalogEntry.TYPE_LEAF
					&& catalogEntry.getRepositoryEntry() != null && RepositoryEntryStatusEnum.closed.equals(catalogEntry.getRepositoryEntry().getEntryStatus()))
					.collect(Collectors.toList());
			// Now remove all remaining entries

			Collator collator = Collator.getInstance();
			collator.setStrength(Collator.IDENTICAL);

			if (isCategorySortingManually(ce)) {
				categories.sort(new CatalogEntryNameComparator(collator));
			}
			if (isEntrySortingManually(ce)) {
				entries.sort(new CatalogEntryNameComparator(collator));
				closedEntries.sort(new CatalogEntryNameComparator(collator));
			}

			children = new ArrayList<>(children.size());
			children.addAll(categories);
			children.addAll(entries);
			children.addAll(closedEntries);
		}

		return children;
	}

	/**
	 * Return the nodes
	 * @param ce
	 * @param firstResult
	 * @param maxResults
	 * @param orderBy
	 * @param asc
	 * @return
	 */
	public List<CatalogEntry> getChildrenOf(CatalogEntry ce, int firstResult, int maxResults,
			CatalogEntry.OrderBy orderBy, boolean asc) {
		if(ce == null) {// nothing have no children
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select cei from catalogentry as cei ")
		  .append(" inner join fetch cei.ownerGroup as ownerGroup")
		  .append(" inner join fetch cei.parent as parentCei")
		  .append(" inner join fetch parentCei.ownerGroup as parentOwnerGroup")
		  .append(" left join fetch cei.repositoryEntry as repositoryEntry")
		  .append(" left join fetch repositoryEntry.lifecycle as lifecycle")
		  .append(" left join fetch repositoryEntry.statistics as statistics")
		  .append(" left join fetch repositoryEntry.olatResource as resource")
		  .append(" where parentCei.key=:parentKey");
		if(orderBy != null) {
			sb.append(" order by cei.").append(orderBy.name()).append(asc ? " ASC" : " DESC");
		}

		TypedQuery<CatalogEntry> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogEntry.class)
				.setParameter("parentKey", ce.getKey())
				.setFirstResult(firstResult);
		if(maxResults > 0) {
			dbQuery.setMaxResults(maxResults);
		}

		return dbQuery.getResultList();
	}

	/**
	 * Returns a list catalog categories
	 * 
	 * @return List of catalog entries of type CatalogEntry.TYPE_NODE
	 */
	public List<CatalogEntry> getAllCatalogNodes() {
		StringBuilder sb = new StringBuilder();
		sb.append("select cei from catalogentry as cei ")
		  .append(" inner join fetch cei.ownerGroup as ownerGroup")
		  .append(" where cei.type=").append(CatalogEntry.TYPE_NODE);
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogEntry.class)
				.getResultList();
	}

	
	/**
	 * Checks if the given catalog entry has any child of the given type. The
	 * query will be cached.
	 * 
	 * @param ce
	 * @param type CatalogEntry.TYPE_LEAF or CatalogEntry.TYPE_NODE
	 * @return true: entry has at least one child of type node
	 */
	public boolean hasChildEntries(CatalogEntry ce, int type) {
		return countChildrenOf(ce, type) > 0; 
	}
	/**
	 * 
	 * @param ce
	 * @param type (-1) if you want all types
	 * @return
	 */
	public int countChildrenOf(CatalogEntry ce, int type) {
		StringBuilder query = new StringBuilder();
		query.append("select count(cei) from catalogentry as cei ")
		     .append(" where cei.parent.key=:parentKey");
		if(type >= 0) {
			query.append(" and cei.type=:type");
		}

		TypedQuery<Number> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Number.class)
				.setParameter("parentKey", ce.getKey());
		if(type >= 0) {
			dbQuery.setParameter("type", type);
		}

		Number totalCount = dbQuery.getSingleResult();
		return totalCount.intValue(); 
	}

	/**
	 * Filters all catalog entries of type leaf that are owned by the given user
	 * @param identity
	 * @param catalogEntries List of catalog entries to be filtered
	 * @return List of catalog entries
	 */
	public List<CatalogEntry> filterOwnedLeafs(Identity identity, List<CatalogEntry> catalogEntries) {
		List<CatalogEntry> ownedEntries = new ArrayList<>();
		for(CatalogEntry cate:catalogEntries) {
			if (cate.getType() == CatalogEntry.TYPE_LEAF) {
				RepositoryEntry repe = cate.getRepositoryEntry();
				if (repositoryService.hasRole(identity, repe, GroupRoles.owner.name())) {
					ownedEntries.add(cate);
				}
			}
		}
		return ownedEntries;
	}

	
	/**
	 * Reload the given catalog entry from db or from hibernate second level cache
	 * @param catalogEntry
	 * @return reloaded catalog entry
	 */
	public CatalogEntry loadCatalogEntry(CatalogEntry catalogEntry) {
		return dbInstance.getCurrentEntityManager().find(CatalogEntryImpl.class, catalogEntry.getKey());
	}

	/**
	 * Load the catalog entry by the given ID
	 * @param catEntryId
	 * @return
	 */
	public CatalogEntry loadCatalogEntry(Long catEntryId) {
		return dbInstance.getCurrentEntityManager().find(CatalogEntryImpl.class, catEntryId);
	}
	
	/**
	 * persist catalog entry
	 * 
	 * @param ce
	 */
	public CatalogEntry saveCatalogEntry(CatalogEntry ce) {
		dbInstance.getCurrentEntityManager().persist(ce);
		return ce;
	}

	/**
	 * update catalog entry on db
	 * 
	 * @param ce
	 */
	public CatalogEntry updateCatalogEntry(CatalogEntry ce) {
		return dbInstance.getCurrentEntityManager().merge(ce);
	}
	
	public void deleteCatalogEntry(RepositoryEntryRef entry, CatalogEntry parent) {
		CatalogEntry ce = getCatalogEntryBy(entry, parent);
		parent = loadCatalogEntry(parent);
		
		if(ce != null) {
			SecurityGroup owner = ce.getOwnerGroup();
			List<CatalogEntry> catalogEntries = parent.getChildren();
			
			catalogEntries.remove(ce);
			updateCatalogEntry(parent);
			
			dbInstance.getCurrentEntityManager().remove(ce);
			
			if (owner != null) {
				log.debug("deleteCatalogEntry case_1: delete owner-group={}", owner);
				securityGroupDao.deleteSecurityGroup(owner);
			}
		} 
	}

	/**
	 * delete a catalog entry and a potentially referenced substructure from db.
	 * Be aware of how to use this deletion, as all the referenced substructure is
	 * deleted.
	 * 
	 * @param ce
	 */
	public void deleteCatalogEntry(CatalogEntry ce) {
		log.debug("deleteCatalogEntry start... ce={}", ce);
		
		//reload the detached catalog entry, delete it and then the owner group
		ce = getCatalogEntryByKey(ce.getKey());
		
		if (ce.getType() == CatalogEntry.TYPE_LEAF) {
			if(ce != null) {
				SecurityGroup owner = ce.getOwnerGroup();
				
				if (ce.getParent() != null) {
					CatalogEntry parent = ce.getParent();
					List<CatalogEntry> catalogEntries = parent.getChildren();
					catalogEntries.remove(ce);
					updateCatalogEntry(parent);
				}
				
				dbInstance.getCurrentEntityManager().remove(ce);
				if (owner != null) {
					log.debug("deleteCatalogEntry case_1: delete owner-group={}", owner);
					securityGroupDao.deleteSecurityGroup(owner);
				}
			}
		} else {
			List<SecurityGroup> secGroupsToBeDeleted = new ArrayList<>();
			
			deleteCatalogSubtree(ce,secGroupsToBeDeleted);
			// after deleting all entries, delete all secGroups corresponding
			for (Iterator<SecurityGroup> iter = secGroupsToBeDeleted.iterator(); iter.hasNext();) {
				SecurityGroup grp = iter.next();
				log.debug("deleteCatalogEntry case_2: delete groups of deleteCatalogSubtree grp={}", grp);
				securityGroupDao.deleteSecurityGroup(grp);
			}
			
			if (ce.getParent() != null) {
				CatalogEntry parent = ce.getParent();
				List<CatalogEntry> catalogEntries = parent.getChildren();
				catalogEntries.remove(ce);
				updateCatalogEntry(parent);
			}
		}
		log.debug("deleteCatalogEntry END");
	}

	/**
	 * recursively delete the structure starting from the catalog entry.
	 * 
	 * @param ce
	 */
	private void deleteCatalogSubtree(CatalogEntry ce, List<SecurityGroup> secGroupsToBeDeleted) {
		List<CatalogEntry> children = getChildrenOf(ce);
		for (CatalogEntry nextCe:children) {
			deleteCatalogSubtree(nextCe,secGroupsToBeDeleted);
		}
		ce = dbInstance.getCurrentEntityManager().find(CatalogEntryImpl.class, ce.getKey());
		//mark owner group for deletion.
		SecurityGroup owner = ce.getOwnerGroup();
		if (owner != null) {
			secGroupsToBeDeleted.add(owner);
		}
		dbInstance.getCurrentEntityManager().remove(ce);
	}

	/**
	 * find all catalog entries referencing the supplied Repository Entry.
	 * 
	 * @param repoEntry
	 * @return List of catalog entries
	 */
	public List<CatalogEntry> getCatalogEntriesReferencing(RepositoryEntryRef repoEntry) {
		String sqlQuery = "select cei from catalogentry as cei, repositoryentry as re "
				+ " where cei.repositoryEntry = re AND re.key=:repoEntryKey ";

		return dbInstance.getCurrentEntityManager()
				.createQuery(sqlQuery, CatalogEntry.class)
				.setParameter("repoEntryKey", repoEntry.getKey())
				.getResultList();
	}

	/**
	 * find all catalog categorie that the given repository entry is a child of
	 * 
	 * @param repoEntry
	 * @return List of catalog entries
	 */
	public List<CatalogEntry> getCatalogCategoriesFor(RepositoryEntryRef repoEntry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select parentCei from catalogentry as cei ")
		  .append(" inner join cei.ownerGroup ownerGroup ")
		  .append(" inner join cei.parent parentCei ")
		  .append(" inner join fetch parentCei.ownerGroup parentOwnerGroup ")
		  .append(" inner join cei.repositoryEntry re ")
		  .append(" where re.key=:repoEntryKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogEntry.class)
				.setParameter("repoEntryKey", repoEntry.getKey())
				.getResultList();
	}
	
	public CatalogEntry getCatalogEntryByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cei from catalogentry as cei")
		  .append(" left join fetch cei.repositoryEntry as entry")
		  .append(" left join fetch cei.ownerGroup ownerGroup ")
		  .append(" left join fetch cei.parent parentCei ")
		  .append(" left join fetch parentCei.ownerGroup parentOwnerGroup ")
		  .append(" where cei.key=:key");

		List<CatalogEntry> entries = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogEntry.class)
				.setParameter("key", key)
				.getResultList();
		
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}
	
	public CatalogEntry getCatalogEntryBy(RepositoryEntryRef entry, CatalogEntry parent) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cei from catalogentry as cei")
		  .append(" left join fetch cei.repositoryEntry as entry")
		  .append(" left join fetch cei.ownerGroup ownerGroup ")
		  .append(" inner join fetch cei.parent parentCei ")
		  .append(" left join fetch parentCei.ownerGroup parentOwnerGroup ")
		  .append(" where parentCei.key=:parentKey and entry.key=:repoEntryKey");

		List<CatalogEntry> entries = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogEntry.class)
				.setParameter("parentKey", parent.getKey())
				.setParameter("repoEntryKey", entry.getKey())
				.getResultList();
		
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}
	
	public CatalogEntry getCatalogNodeByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cei from catalogentry as cei")
		  .append(" left join fetch cei.ownerGroup ownerGroup ")
		  .append(" left join fetch cei.parent parentCei ")
		  .append(" left join fetch parentCei.ownerGroup parentOwnerGroup ")
		  .append(" where cei.key=:key");

		List<CatalogEntry> entries = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogEntry.class)
				.setParameter("key", key)
				.getResultList();
		
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}
	
	public boolean isOwner(CatalogEntry catalogEntry, Identity identity) {
		return securityGroupDao.isIdentityInSecurityGroup(identity, catalogEntry.getOwnerGroup());
	}
	
	public List<Identity> getOwners(CatalogEntry catalogEntry) {
		return securityGroupDao.getIdentitiesOfSecurityGroup(catalogEntry.getOwnerGroup());
	}
	
	public boolean addOwner(CatalogEntry catalogEntry, Identity identity) {
		if (!securityGroupDao.isIdentityInSecurityGroup(identity, catalogEntry.getOwnerGroup())) {
			securityGroupDao.addIdentityToSecurityGroup(identity, catalogEntry.getOwnerGroup());
			return true;
		}
		return false;
	}
	
	public void removeOwner(CatalogEntry catalogEntry, Identity identity) {
		securityGroupDao.removeIdentityFromSecurityGroup(identity, catalogEntry.getOwnerGroup());
	}

	/**
	 * Find catalog entries for certain identity
	 * 
	 * @param binderName
	 * @return List of catalog entries
	 */
	public List<CatalogEntry> getCatalogEntriesOwnedBy(Identity identity) {
		String sqlQuery = "select cei from catalogentry as cei inner join fetch cei.ownerGroup, " + 
			" org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi" +
			" where cei.ownerGroup = sgmsi.securityGroup and sgmsi.identity.key = :identityKey";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sqlQuery, CatalogEntry.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public boolean isOwner(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cei.key from catalogentry as cei ")
		  .append(" where exists (select sgmsi.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi ")
		  .append("   where  cei.ownerGroup=sgmsi.securityGroup and sgmsi.identity.key=:identityKey")
		  .append(" )");

		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setFlushMode(FlushModeType.COMMIT)
				.setFirstResult(0)
				.setMaxResults(1)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return count != null && !count.isEmpty() && count.get(0) != null && count.get(0).longValue() > 0;
	}
	
	public List<Identity> getOwnersOfParentLine(CatalogEntry entry) {
		List<CatalogEntry> parentLine = getCategoryParentLine(entry);
		List<SecurityGroup> secGroups = new ArrayList<>();
		for(CatalogEntry parent:parentLine) {
			if(parent.getOwnerGroup() != null) {
				secGroups.add(parent.getOwnerGroup());
			}
		}
		return securityGroupDao.getIdentitiesOfSecurityGroups(secGroups);
	}
	
	private final List<CatalogEntry> getCategoryParentLine(CatalogEntry entry) {
		List<CatalogEntry> parentLine = new ArrayList<>();
		parentLine.add(entry);
		
		CatalogEntry current = entry;
		while(current.getParent() != null) {
			parentLine.add(current.getParent());
			current = current.getParent();
		}
		return parentLine;
	}

	/**
	 * add a catalog entry to the specified parent
	 * 
	 * @param parent
	 * @param newEntry
	 */
	public void addCatalogEntry(CatalogEntry parent, CatalogEntry newEntry) {
		parent = loadCatalogEntry(parent);
		
		boolean debug = log.isDebugEnabled();
		if(debug) log.debug("addCatalogEntry parent={}", parent);
		if(debug) log.debug("addCatalogEntry newEntry={}", newEntry);
		if(debug) log.debug("addCatalogEntry newEntry.getOwnerGroup()={}", newEntry.getOwnerGroup());
		
		newEntry.setParent(parent);
		saveCatalogEntry(newEntry);
		
		addToChildren(parent, newEntry);
		
		dbInstance.commitAndCloseSession();
	}
	
	private void addToChildren(CatalogEntry parentEntry, CatalogEntry newEntry) {
		parentEntry = loadCatalogEntry(parentEntry);
		newEntry = loadCatalogEntry(newEntry);
		List<CatalogEntry> catEntries = parentEntry.getChildren();
		RepositoryEntry repoEntry2 = newEntry.getRepositoryEntry();
		
		if(catEntries.isEmpty()) {
			catEntries.add(newEntry);
			return;
		}

		cleanNullEntries(catEntries);

		// Create 3 lists: Categories, entries, closed entries
		List<CatalogEntry> categories = catEntries.stream()
				.filter(catalogEntry -> catalogEntry.getType() == CatalogEntry.TYPE_NODE)
				.collect(Collectors.toList());

		List<CatalogEntry> entries = catEntries.stream()
				.filter(catalogEntry -> catalogEntry.getType() == CatalogEntry.TYPE_LEAF
				&& (catalogEntry.getRepositoryEntry() == null || !RepositoryEntryStatusEnum.closed.equals(catalogEntry.getRepositoryEntry().getEntryStatus())))
				.collect(Collectors.toList());

		// To be sure only correct entries are in the final list, the last step is also filtered
		List<CatalogEntry> closedEntries = catEntries.stream()
				.filter(catalogEntry -> catalogEntry.getType() == CatalogEntry.TYPE_LEAF
				&& catalogEntry.getRepositoryEntry() != null && RepositoryEntryStatusEnum.closed.equals(catalogEntry.getRepositoryEntry().getEntryStatus()))
				.collect(Collectors.toList());
		// Now remove all remaining entries
		catEntries.clear();

		// Add to categories
		if (newEntry.getType() == CatalogEntry.TYPE_NODE) {
			// If added on top or alphabetically
			if ((parentEntry.getCategoryAddPosition() == null && repositoryModule.getCatalogAddCategoryPosition() == 1)
				|| (parentEntry.getCategoryAddPosition() != null && parentEntry.getCategoryAddPosition() == 1)) {
				categories.add(0, newEntry);
			}
			// If added in the end
			else {
				categories.add(newEntry);
			}

		}
		// Add to entries
		else if (newEntry.getType() == CatalogEntry.TYPE_LEAF &&
				(newEntry.getRepositoryEntry() == null || !RepositoryEntryStatusEnum.closed.equals(newEntry.getRepositoryEntry().getEntryStatus()))) {
			// If added on top or alphabetically
			if ((parentEntry.getEntryAddPosition() == null && repositoryModule.getCatalogAddEntryPosition() == 1)
				|| (parentEntry.getEntryAddPosition() != null && parentEntry.getEntryAddPosition() == 1)) {
				entries.add(0, newEntry);
			}
			// If added in the end
			else {
				entries.add(newEntry);
			}
		}
		// Add to closed entries
		else {
			// If added on top or alphabetically
			if ((parentEntry.getEntryAddPosition() == null && repositoryModule.getCatalogAddEntryPosition() == 1)
					|| (parentEntry.getEntryAddPosition() != null && parentEntry.getEntryAddPosition() == 1)) {
				closedEntries.add(0, newEntry);
			}
			// If added in the end
			else {
				closedEntries.add(newEntry);
			}
		}

		catEntries.addAll(categories);
		catEntries.addAll(entries);
		catEntries.addAll(closedEntries);

		updateCatalogEntry(parentEntry);
	}

	/**
	 * Find all CatalogEntries which can act as catalog roots. Frankly speaking
	 * only one is found up to now, but for later stages one can think of getting
	 * more such roots. An empty list indicates an error.
	 * 
	 * @return List of catalog entries
	 */
	public List<CatalogEntry> getRootCatalogEntries() {
		StringBuilder sb = new StringBuilder();
		sb.append("select cei from catalogentry as cei ")
		  .append("inner join fetch cei.ownerGroup ownerGroup ")
		  .append("where cei.parent is null");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogEntry.class)
				.getResultList();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		List<CatalogEntry> roots = getRootCatalogEntries();
		if (roots.isEmpty()) { // not initialized yet
			/*
			 * copy a snapshot of olatAdmins into catalogAdmins do not put
			 * secMgr.findSecurityGroupByName(Constants.GROUP_ADMIN) directly into a
			 * CatalogEntry!!
			 */
			List<Identity> olatAdminIdents = organisationService.getDefaultsSystemAdministator();
			SecurityGroup catalogAdmins = securityGroupDao.createAndPersistSecurityGroup();
			for (int i = 0; i < olatAdminIdents.size(); i++) {
				securityGroupDao.addIdentityToSecurityGroup(olatAdminIdents.get(i), catalogAdmins);
			}
			/*
			 * start with something called CATALOGROOT, you can rename it to whatever
			 * name you like later as OLATAdmin
			 */
			// parent == null -> no parent -> I am a root node.
			saveRootCatalogEntry(CATALOGROOT, CatalogEntry.TYPE_NODE, catalogAdmins);
			dbInstance.intermediateCommit();
		}
	}

	private CatalogEntry saveRootCatalogEntry(String name, int type, SecurityGroup ownerGroup) {
		CatalogEntry ce = createCatalogEntry();
		ce.setName(name);
		ce.setOwnerGroup(ownerGroup);
		ce.setType(type);
		saveCatalogEntry(ce);
		return ce;
	}
	
	public CatalogEntry addCatalogCategory(CatalogEntry ce, CatalogEntry parentCe) {
		if(parentCe != null) {
			parentCe = loadCatalogEntry(parentCe);
		}
		ce.setParent(parentCe);
		ce = saveCatalogEntry(ce);	
		
		addToChildren(parentCe, ce);
		
		dbInstance.commitAndCloseSession();
		return ce;
	}
	
	private void cleanNullEntries(List<CatalogEntry> catEntries) {
		for(Iterator<CatalogEntry> entryIt=catEntries.iterator(); entryIt.hasNext(); ) {
			if(entryIt.next() == null) {
				entryIt.remove();
			}
		}
	}

	/**
	 * Move the given catalog entry to the new parent
	 * @param toBeMovedEntry
	 * @param newParentEntry
	 * return true: success; false: failure
	 */
	public boolean moveCatalogEntry(CatalogEntry toBeMovedEntry, CatalogEntry newParentEntry) {
		// reload current item to prevent stale object modification
		toBeMovedEntry = loadCatalogEntry(toBeMovedEntry);
		newParentEntry = loadCatalogEntry(newParentEntry);	
		
		// check that the new parent is not a leaf
		if (newParentEntry.getType() == CatalogEntry.TYPE_LEAF) return false;
		// check that the new parent is not a child of the to be moved entry
		CatalogEntry tempEntry = newParentEntry;
		while (tempEntry != null) {
			if (tempEntry.getKey().equals(toBeMovedEntry.getKey())) {
				// ups, the new parent is within the to be moved entry - abort
				return false;
			}
			tempEntry = tempEntry.getParent();
		}
		// Check that the new parent doesn't contain the entry already and remove it from its list
		List<CatalogEntry> newParentChildren = newParentEntry.getChildren();
		for (CatalogEntry newParentChild : newParentChildren) {
			if (newParentChild != null && newParentChild.getType() == CatalogEntry.TYPE_LEAF
					&& newParentChild.getRepositoryEntry() != null
					&& newParentChild.getRepositoryEntry().equals(toBeMovedEntry.getRepositoryEntry())) {
				// Entry is already existing
				return false;
			}
		}
		// Get the old parent and remove it from its list
		CatalogEntry oldParentEntry;
		if (toBeMovedEntry.getParent() != null) {
			oldParentEntry = toBeMovedEntry.getParent();
			List<CatalogEntry> oldChildren = oldParentEntry.getChildren();
			oldChildren.remove(toBeMovedEntry);
			updateCatalogEntry(oldParentEntry);
		}
		// set new parent and save
		toBeMovedEntry.setParent(newParentEntry);
		toBeMovedEntry = updateCatalogEntry(toBeMovedEntry);
		addToChildren(newParentEntry, toBeMovedEntry);
		dbInstance.commitAndCloseSession();
		return true;
	}


	/**
	 * @param repositoryEntry
	 */
	public void resourceableDeleted(RepositoryEntry repositoryEntry) {
		// if a repository entry gets deleted, the referencing Catalog Entries gets
		// retired to
		log.debug("sourceableDeleted start... repositoryEntry={}", repositoryEntry);
		List<CatalogEntry> references = getCatalogEntriesReferencing(repositoryEntry);
		if (references != null && !references.isEmpty()) {
			for (int i = 0; i < references.size(); i++) {
				deleteCatalogEntry(references.get(i));
			}
		}
	}

	/**
	 * Remove identity as owner of catalog-entry.
	 * If there is no other owner, the olat-administrator (define in spring config) will be added as owner.
	 *  
	 * @see org.olat.user.UserDataDeletable#deleteUserData(org.olat.core.id.Identity)
	 */
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		// Remove as owner
		List<CatalogEntry> catalogEntries = getCatalogEntriesOwnedBy(identity);
		for (CatalogEntry catalogEntry:catalogEntries) {
			
			securityGroupDao.removeIdentityFromSecurityGroup(identity, catalogEntry.getOwnerGroup());
			if (securityGroupDao.countIdentitiesOfSecurityGroup(catalogEntry.getOwnerGroup()) == 0 ) {
				// This group has no owner anymore => add OLAT-Admin as owner
				Identity admin = CoreSpringFactory.getImpl(RepositoryDeletionModule.class).getAdminUserIdentity();
				securityGroupDao.addIdentityToSecurityGroup(admin, catalogEntry.getOwnerGroup());
				log.info("Delete user-data, add Administrator-identity as owner of catalogEntry={}", catalogEntry.getName());
			}
		}
		log.debug("All owner entries in catalog deleted for identity={}", identity);
	}

	/**
	 * checks if the given catalog entry is within one of the given catalog
	 * categories
	 * 
	 * @param toBeCheckedEntry
	 * @param entriesList
	 * @return
	 */
	public boolean isEntryWithinCategory(CatalogEntry toBeCheckedEntry, List<CatalogEntry> entriesList) {
		CatalogEntry tempEntry = toBeCheckedEntry;
		while (tempEntry != null) {						
			if (PersistenceHelper.listContainsObjectByKey(entriesList, tempEntry)) {
				return true;
			}
			tempEntry = tempEntry.getParent();
		}
		return false;
	}

	/**
	 * Create a volatile OLATResourceable for a given catalog entry that can be
	 * used to create a bookmark to this catalog entry
	 * 
	 * @param currentCatalogEntry
	 * @return
	 */
	public OLATResourceable createOLATResouceableFor(final CatalogEntry currentCatalogEntry) {
		if (currentCatalogEntry == null) return null;
		return new Resourceable(CATALOGENTRY, currentCatalogEntry.getKey());
	}

	/**
	 * 
	 * @param re
	 */
	public void notifyReferencedRepositoryEntryChanges(RepositoryEntry re) {
		// inform anybody interested about this change
		MultiUserEvent modifiedEvent = new EntryChangedEvent(re, null, Change.modifiedDescription, "CatalogManager");
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, re);
	}
	
	public VFSLeaf getImage(CatalogEntryRef entry) {
		VFSContainer catalogResourceHome = getCatalogResourcesHome();
		String imageName = entry.getKey() + ".png";
		VFSItem image = catalogResourceHome.resolve(imageName);
		if(image instanceof VFSLeaf) {
			return (VFSLeaf)image;
		}
		imageName = entry.getKey() + ".jpg";
		image = catalogResourceHome.resolve(imageName);
		if(image instanceof VFSLeaf) {
			return (VFSLeaf)image;
		}
		imageName = entry.getKey() + ".gif";
		image = catalogResourceHome.resolve(imageName);
		if(image instanceof VFSLeaf) {
			return (VFSLeaf)image;
		}
		return null;
	}
	
	public void deleteImage(CatalogEntryRef entry) {
		VFSLeaf imgFile =  getImage(entry);
		if (imgFile != null) {
			if(imgFile.canMeta() == VFSConstants.YES) {
				vfsRepositoryService.resetThumbnails(imgFile);
			}
			imgFile.delete();
		}
	}
	
	public boolean setImage(VFSLeaf newImageFile, CatalogEntryRef re, Identity savedBy) {
		VFSLeaf currentImage = getImage(re);
		if(currentImage != null) {
			if(currentImage.canMeta() == VFSConstants.YES) {
				vfsRepositoryService.resetThumbnails(currentImage);
			}
			currentImage.delete();
		}
		
		String extension = FileUtils.getFileSuffix(newImageFile.getName());
		if(StringHelper.containsNonWhitespace(extension)) {
			extension = extension.toLowerCase();
		}

		boolean ok = false;
		VFSContainer catalogResourceHome = getCatalogResourcesHome();
		try {
			if("jpeg".equals(extension) || "jpg".equals(extension)) {
				VFSLeaf repoImage = catalogResourceHome.createChildLeaf(re.getKey() + ".jpg");
				ok = VFSManager.copyContent(newImageFile, repoImage, false, savedBy);
			} else if("png".equals(extension)) {
				VFSLeaf repoImage = catalogResourceHome.createChildLeaf(re.getKey() + ".png");
				ok = VFSManager.copyContent(newImageFile, repoImage, false, savedBy);
			} else if("gif".equals(extension)) {
				VFSLeaf repoImage = catalogResourceHome.createChildLeaf(re.getKey() + ".gif");
				ok = VFSManager.copyContent(newImageFile, repoImage, false, savedBy);
			} else {
				//scale to default and png
				VFSLeaf repoImage = catalogResourceHome.createChildLeaf(re.getKey() + ".png");
				Size size = imageHelper.scaleImage(newImageFile, repoImage, 570, 570, true);
				ok = size != null;
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return ok;
	}
	
	public VFSContainer getCatalogResourcesHome() {
		return new LocalFolderImpl(new File(FolderConfig.getCanonicalResourcesHome(), "catalog"));
	}
	
	public int reorderCatalogEntry(Long parentKey, Long entryKey, boolean moveUp) {
		CatalogEntry catParent = getCatalogEntryByKey(parentKey);
		List<CatalogEntry> catChildren = catParent.getChildren();
		
		for (CatalogEntry cat : catChildren) {
			if (cat != null && cat.getKey().equals(entryKey)) {
				int catIndex = catChildren.indexOf(cat);
				
				// Move down
				if (!moveUp && catIndex < catChildren.size() - 1 && catChildren.get(catIndex + 1).getType() == cat.getType()) {
					CatalogEntry a = catChildren.get(catIndex);
					catChildren.remove(catIndex);
					catChildren.add(catIndex + 1, a);
				} 
				// Move up
				else if (moveUp && catIndex > 0 && catChildren.get(catIndex - 1).getType() == cat.getType()) {
					CatalogEntry a = catChildren.get(catIndex - 1);
					catChildren.remove(catIndex - 1);
					catChildren.add(catIndex, a);
				}
				updateCatalogEntry(catParent);
				dbInstance.commitAndCloseSession();
				
				return 0;
			}
		}
		
		return 1;
	}
	
	public int setPosition(Long childEntryKey, int position) {
		CatalogEntry childEntry = getCatalogEntryByKey(childEntryKey);
		CatalogEntry parentEntry = childEntry.getParent();
		List<CatalogEntry> children = parentEntry.getChildren();
		
		if (position >= 0 && position < children.size()) {
			children.remove(children.indexOf(childEntry));
			children.add(position, childEntry);
			
			updateCatalogEntry(parentEntry);		
			dbInstance.commitAndCloseSession();
			
			return 0;
		} else if (position < 0) {
			return 1;
		} else if (position >= children.size()) {
			return 2;
		} else {
			return -1;
		}
	}

	public boolean isEntrySortingManually(CatalogEntry ce) {
		return !((ce.getEntryAddPosition() != null && ce.getEntryAddPosition() == 0) || (ce.getEntryAddPosition() == null && repositoryModule.getCatalogAddEntryPosition() == 0));
	}

	public boolean isCategorySortingManually(CatalogEntry ce) {
		return !((ce.getCategoryAddPosition() != null && ce.getCategoryAddPosition() == 0) || (ce.getCategoryAddPosition() == null && repositoryModule.getCatalogAddCategoryPosition() == 0));
	}
	
	public static class CatalogEntryNameComparator implements Comparator<CatalogEntry> {
		
		private final Collator collator;
		
		public CatalogEntryNameComparator(Collator collator) {
			this.collator = collator;
		}

		@Override
		public int compare(CatalogEntry o1, CatalogEntry o2) {
			int c = 0;
			if(o1 == null || o2 == null) {
				c = compareNullObjects(o1, o2);
			} else {
				String n1 = o1.getName();
				String n2 = o2.getName();
				if(n1 == null || n2 == null) {
					c = compareNullObjects(n1, n2);
				} else {
					c = collator.compare(n1, n2);
				}
				
				if(c == 0) {
					Long k1 = o1.getKey();
					Long k2 = o2.getKey();
					if(k1 == null || k2 == null) {
						c = compareNullObjects(k1, k2);
					} else {
						c = k1.compareTo(k2);
					}
				}
			}
			return c;
		}
		
		private final int compareNullObjects(final Object a, final Object b) {
			boolean ba = (a == null);
			boolean bb = (b == null);
			return ba? (bb? 0: -1):(bb? 1: 0);
		}
	}
}
