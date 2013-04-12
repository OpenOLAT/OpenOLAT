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

package org.olat.catalog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.configuration.Initializable;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.Resourceable;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;

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
public class CatalogManager extends BasicManager implements UserDataDeletable, Initializable {
	private static CatalogManager catalogManager;
	/**
	 * Default value for the catalog root <code>CATALOGROOT</code>
	 */
	public static final String CATALOGROOT = "CATALOG ROOT";
	/**
	 * Resource identifyer for catalog entries
	 */
	public static final String CATALOGENTRY = "CatalogEntry";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;

	/**
	 * [spring]
	 * @param userDeletionManager
	 */
	private CatalogManager(UserDeletionManager userDeletionManager) {
		// singleton
		userDeletionManager.registerDeletableUserData(this);
		catalogManager = this;
	}

	/**
	 * @return Return singleton instance
	 */
	public static CatalogManager getInstance() {
		return catalogManager;
	}

	/**
	 * @return transient catalog entry object
	 */
	public CatalogEntry createCatalogEntry() {
		return new CatalogEntryImpl();
	}

	/**
	 * Children of this CatalogEntry as a list of CatalogEntries
	 * @param ce
	 * @return List of catalog entries that are childern entries of given entry
	 */
	public List<CatalogEntry> getChildrenOf(CatalogEntry ce) {
		return getChildrenOf(ce, 0, -1, CatalogEntry.OrderBy.name, true);
	}

	public List<CatalogEntry> getChildrenOf(CatalogEntry ce, int firstResult, int maxResults, CatalogEntry.OrderBy orderBy, boolean asc) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cei from ").append(CatalogEntryImpl.class.getName()).append(" as cei ")
		  .append(" inner join fetch cei.ownerGroup as ownerGroup")
		  .append(" left join fetch cei.repositoryEntry as repositoryEntry")
		  .append(" left join fetch repositoryEntry.ownerGroup as repoOwnerGroup")
		  .append(" left join fetch repositoryEntry.tutorGroup as repoTutorGroup")
		  .append(" left join fetch repositoryEntry.participantGroup as repoParticipantGroup")
		  .append(" where cei.parent.key=:parentKey");
		if(orderBy != null) {
			sb.append(" order by cei.").append(orderBy.name()).append(asc ? " ASC" : " DESC");
		}

		TypedQuery<CatalogEntry> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogEntry.class)
				.setParameter("parentKey", ce.getKey())
				.setFirstResult(0);
		if(maxResults > 0) {
			dbQuery.setMaxResults(maxResults);
		}

		List<CatalogEntry> entries = dbQuery.getResultList();
		return entries;
	}
	
	public List<CatalogEntry> getShortChildrenOf(CatalogEntry ce, int firstResult, int maxResults, CatalogEntry.OrderBy orderBy, boolean asc) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cei from ").append(CatalogEntryImpl.class.getName()).append(" as cei ")
		  .append(" inner join fetch cei.ownerGroup as ownerGroup")
		  .append(" left join fetch cei.repositoryEntry as repositoryEntry")
		  .append(" left join fetch repositoryEntry.ownerGroup as repoOwnerGroup")
		  .append(" left join fetch repositoryEntry.tutorGroup as repoTutorGroup")
		  .append(" left join fetch repositoryEntry.participantGroup as repoParticipantGroup")
		  .append(" where cei.parent.key=:parentKey");
		if(orderBy != null) {
			sb.append(" order by cei.").append(orderBy.name()).append(asc ? " ASC" : " DESC");
		}

		TypedQuery<CatalogEntry> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogEntry.class)
				.setParameter("parentKey", ce.getKey())
				.setFirstResult(0);
		if(maxResults > 0) {
			dbQuery.setMaxResults(maxResults);
		}

		List<CatalogEntry> entries = dbQuery.getResultList();
		return entries;
	}

	/**
	 * Returns a list catalog categories
	 * 
	 * @return List of catalog entries of type CatalogEntry.TYPE_NODE
	 */
	public List<CatalogEntry> getAllCatalogNodes() {
		StringBuilder sb = new StringBuilder();
		sb.append("select cei from ").append(CatalogEntryImpl.class.getName()).append(" as cei ")
		  .append(" inner join fetch cei.ownerGroup as ownerGroup")
		  .append(" left join fetch cei.repositoryEntry as repositoryEntry")
		  .append(" left join fetch repositoryEntry.ownerGroup as repoOwnerGroup")
		  .append(" left join fetch repositoryEntry.tutorGroup as repoTutorGroup")
		  .append(" left join fetch repositoryEntry.participantGroup as repoParticipantGroup")
		  .append(" where cei.type= :type");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogEntry.class)
				.setParameter("type", CatalogEntry.TYPE_NODE)
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
		query.append("select count(cei) from ").append(CatalogEntryImpl.class.getName()).append(" as cei ")
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
		List<CatalogEntry> ownedEntries = new ArrayList<CatalogEntry>();
		for(CatalogEntry cate:catalogEntries) {
			if (cate.getType() == CatalogEntry.TYPE_LEAF) {
				RepositoryEntry repe = cate.getRepositoryEntry();
				SecurityGroup secGroup = repe.getOwnerGroup();
				if (securityManager.isIdentityInSecurityGroup(identity, secGroup)) {
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
	public void saveCatalogEntry(CatalogEntry ce) {
		dbInstance.getCurrentEntityManager().persist(ce);
	}

	/**
	 * update catalog entry on db
	 * 
	 * @param ce
	 */
	public CatalogEntry updateCatalogEntry(CatalogEntry ce) {
		return dbInstance.getCurrentEntityManager().merge(ce);
	}

	/**
	 * delete a catalog entry and a potentially referenced substructure from db.
	 * Be aware of how to use this deletion, as all the referenced substructure is
	 * deleted.
	 * 
	 * @param ce
	 */
	public void deleteCatalogEntry(CatalogEntry ce) {
		boolean debug = isLogDebugEnabled();
		if(debug) logDebug("deleteCatalogEntry start... ce=" + ce);
		
		if (ce.getType() == CatalogEntry.TYPE_LEAF) {
			//delete catalog entry, then delete owner group
			SecurityGroup owner = ce.getOwnerGroup();
			dbInstance.getCurrentEntityManager().remove(ce);
			if (owner != null) {
				getLogger().debug("deleteCatalogEntry case_1: delete owner-group=" + owner);
				securityManager.deleteSecurityGroup(owner);
			}
		} else {
			List<SecurityGroup> secGroupsToBeDeleted = new ArrayList<SecurityGroup>();
			//FIXME pb: the transaction must also include the deletion of the security
			// groups. Why not using this method as a recursion and seperating the 
			// deletion of the ce and the groups by collecting the groups? IMHO there 
			// are not less db queries. This way the code is much less clear, e.g. the method
			// deleteCatalogSubtree does not really delete the subtree, it leaves the 
			// security groups behind. I would preferre to have one delete method that 
			// deletes its children first by calling itself on the children and then deletes
			// itself ant its security group. The nested transaction that occures is actually 
			// not a problem, the DB object can handel this.
			deleteCatalogSubtree(ce,secGroupsToBeDeleted);
			// after deleting all entries, delete all secGroups corresponding
			for (Iterator<SecurityGroup> iter = secGroupsToBeDeleted.iterator(); iter.hasNext();) {
				SecurityGroup grp = (SecurityGroup) iter.next();
				if(debug) logDebug("deleteCatalogEntry case_2: delete groups of deleteCatalogSubtree grp=" + grp);
				securityManager.deleteSecurityGroup(grp);
			}
		}
		if(debug) logDebug("deleteCatalogEntry END");
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
		//TODO delete marks
		dbInstance.getCurrentEntityManager().remove(ce);
	}

	/**
	 * find all catalog entries referencing the supplied Repository Entry.
	 * 
	 * @param repoEntry
	 * @return List of catalog entries
	 */
	public List<CatalogEntry> getCatalogEntriesReferencing(RepositoryEntry repoEntry) {
		String sqlQuery = "select cei from " + " org.olat.catalog.CatalogEntryImpl as cei " + " ,org.olat.repository.RepositoryEntry as re "
				+ " where cei.repositoryEntry = re AND re.key= :reKey ";

		return dbInstance.getCurrentEntityManager()
				.createQuery(sqlQuery, CatalogEntry.class)
				.setParameter("reKey", repoEntry.getKey())
				.getResultList();
	}

	/**
	 * find all catalog categorie that the given repository entry is a child of
	 * 
	 * @param repoEntry
	 * @return List of catalog entries
	 */
	public List<CatalogEntry> getCatalogCategoriesFor(RepositoryEntry repoEntry) {
		String sqlQuery = "from org.olat.catalog.CatalogEntryImpl where id in "
				+ " (select distinct parent.id from org.olat.catalog.CatalogEntryImpl as parent " 
					+ ", org.olat.catalog.CatalogEntryImpl as cei "
					+ ", org.olat.repository.RepositoryEntry as re " 
					+ " where cei.repositoryEntry = re " 
					+ " and re.key= :reKey " 
					+ " and cei.parent = parent)";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sqlQuery, CatalogEntry.class)
				.setParameter("reKey", repoEntry.getKey())
				.getResultList();
	}
	
	public CatalogEntry getCatalogEntryByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cei from ").append(CatalogEntryImpl.class.getName()).append(" as cei")
		  .append(" left join fetch cei.repositoryEntry as entry")
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

	/**
	 * Find catalog entries for certain identity
	 * 
	 * @param binderName
	 * @return List of catalog entries
	 */
	public List<CatalogEntry> getCatalogEntriesOwnedBy(Identity identity) {
		String sqlQuery = "select cei from org.olat.catalog.CatalogEntryImpl as cei inner join fetch cei.ownerGroup, " + 
			" org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi" +
			" where cei.ownerGroup = sgmsi.securityGroup and sgmsi.identity.key = :identityKey";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sqlQuery, CatalogEntry.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<Identity> getOwnersOfParentLine(CatalogEntry entry) {
		List<CatalogEntry> parentLine = getCategoryParentLine(entry);
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		for(CatalogEntry parent:parentLine) {
			if(parent.getOwnerGroup() != null) {
				secGroups.add(parent.getOwnerGroup());
			}
		}
		return securityManager.getIdentitiesOfSecurityGroups(secGroups);
	}
	
	private final List<CatalogEntry> getCategoryParentLine(CatalogEntry entry) {
		List<CatalogEntry> parentLine = new ArrayList<CatalogEntry>();
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
		boolean debug = isLogDebugEnabled();
		if(debug) logDebug("addCatalogEntry parent=" + parent);
		newEntry.setParent(parent);
		if(debug) logDebug("addCatalogEntry newEntry=" + newEntry);
		if(debug) logDebug("addCatalogEntry newEntry.getOwnerGroup()=" + newEntry.getOwnerGroup());
		saveCatalogEntry(newEntry);
	}

	/**
	 * Find all CatalogEntries which can act as catalog roots. Frankly speaking
	 * only one is found up to now, but for later stages one can think of getting
	 * more such roots. An empty list indicates an error.
	 * 
	 * @return List of catalog entries
	 */
	public List<CatalogEntry> getRootCatalogEntries() {
		String sqlQuery = "select cei from org.olat.catalog.CatalogEntryImpl as cei where cei.parent is null";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sqlQuery, CatalogEntry.class)
				.getResultList();
	}

	/**
	 * init called on module start-up
	 */
	public void init() {
		List<CatalogEntry> roots = getRootCatalogEntries();
		if (roots.isEmpty()) { // not initialized yet
			/*
			 * copy a snapshot of olatAdmins into catalogAdmins do not put
			 * secMgr.findSecurityGroupByName(Constants.GROUP_ADMIN) directly into a
			 * CatalogEntry!!
			 */
			SecurityGroup olatAdmins = securityManager.findSecurityGroupByName(Constants.GROUP_ADMIN);
			List<Identity> olatAdminIdents = securityManager.getIdentitiesOfSecurityGroup(olatAdmins);
			SecurityGroup catalogAdmins = securityManager.createAndPersistSecurityGroup();
			for (int i = 0; i < olatAdminIdents.size(); i++) {
				securityManager.addIdentityToSecurityGroup((Identity) olatAdminIdents.get(i), catalogAdmins);
			}
			/*
			 * start with something called CATALOGROOT, you can rename it to whatever
			 * name you like later as OLATAdmin
			 */
			// parent == null -> no parent -> I am a root node.
			saveCatEntry(CATALOGROOT, null, CatalogEntry.TYPE_NODE, catalogAdmins, null, null);
			dbInstance.intermediateCommit();
		}
	}

	private CatalogEntry saveCatEntry(String name, String desc, int type, SecurityGroup ownerGroup, RepositoryEntry repoEntry,
			CatalogEntry parent) {
		CatalogEntry ce = createCatalogEntry();
		ce.setName(name);
		ce.setDescription(desc);
		ce.setOwnerGroup(ownerGroup);
		ce.setRepositoryEntry(repoEntry);
		ce.setParent(parent);
		ce.setType(type);
		saveCatalogEntry(ce);
		return ce;
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
		// set new parent and save
		toBeMovedEntry.setParent(newParentEntry);
		updateCatalogEntry(toBeMovedEntry);
		return true;
	}


	/**
	 * @param repositoryEntry
	 */
	public void resourceableDeleted(RepositoryEntry repositoryEntry) {
		// if a repository entry gets deleted, the referencing Catalog Entries gets
		// retired to
		if(isLogDebugEnabled()) logDebug("sourceableDeleted start... repositoryEntry=" + repositoryEntry);
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
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		// Remove as owner
		List<CatalogEntry> catalogEntries = getCatalogEntriesOwnedBy(identity);
		for (CatalogEntry catalogEntry:catalogEntries) {
			
			securityManager.removeIdentityFromSecurityGroup(identity, catalogEntry.getOwnerGroup());
			if (securityManager.countIdentitiesOfSecurityGroup(catalogEntry.getOwnerGroup()) == 0 ) {
				// This group has no owner anymore => add OLAT-Admin as owner
				securityManager.addIdentityToSecurityGroup(UserDeletionManager.getInstance().getAdminIdentity(), catalogEntry.getOwnerGroup());
				logInfo("Delete user-data, add Administrator-identity as owner of catalogEntry=" + catalogEntry.getName());
			}
		}
		if(isLogDebugEnabled()) logDebug("All owner entries in catalog deleted for identity=" + identity);
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
	public void updateReferencedRepositoryEntry(RepositoryEntry re) {
		RepositoryEntry reloaded = repositoryManager.setDescriptionAndName(re, re.getDisplayname(), re.getDescription());
		// inform anybody interested about this change
    MultiUserEvent modifiedEvent = new EntryChangedEvent(reloaded, EntryChangedEvent.MODIFIED_DESCRIPTION);
    CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, reloaded);
	}
}
