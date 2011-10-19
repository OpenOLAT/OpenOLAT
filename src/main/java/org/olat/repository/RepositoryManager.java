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
* <p>
*/ 

package org.olat.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.bookmark.BookmarkManager;
import org.olat.catalog.CatalogManager;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.async.BackgroundTaskQueueManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.GroupLoggingAction;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.repository.async.IncrementDownloadCounterBackgroundTask;
import org.olat.repository.async.IncrementLaunchCounterBackgroundTask;
import org.olat.repository.async.SetAccessBackgroundTask;
import org.olat.repository.async.SetDescriptionNameBackgroundTask;
import org.olat.repository.async.SetLastUsageBackgroundTask;
import org.olat.repository.async.SetPropertiesBackgroundTask;
import org.olat.repository.controllers.RepositoryEntryImageController;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Initial Date:  Mar 31, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class RepositoryManager extends BasicManager {

	private static RepositoryManager INSTANCE;
	private BaseSecurity securityManager;
	private static BackgroundTaskQueueManager taskQueueManager;

	/**
	 * [used by spring]
	 */
	private RepositoryManager(BaseSecurity securityManager, BackgroundTaskQueueManager taskQueueManager) {
		this.securityManager = securityManager;
		this.taskQueueManager = taskQueueManager;
		INSTANCE = this;
	}

	/**
	 * @return Singleton.
	 */
	public static RepositoryManager getInstance() { return INSTANCE; }

	/**
	 * @param initialAuthor
	 * @return A repository instance which has not been persisted yet.
	 */
	public RepositoryEntry createRepositoryEntryInstance(String initialAuthor) {
		return createRepositoryEntryInstance(initialAuthor, null, null);
	}
	
	/**
	 * @param initialAuthor
	 * @param resourceName
	 * @param description
	 * @return A repository instance which has not been persisted yet, initialized with given data.
	 */
	public RepositoryEntry createRepositoryEntryInstance(String initialAuthor, String resourceName, String description) {
		RepositoryEntry re = new RepositoryEntry();
		re.setInitialAuthor(initialAuthor);
		re.setResourcename(resourceName == null ? "" : resourceName);
		re.setDescription(description == null ? "" : description);
		re.setLastUsage(new Date());
		return re;
	}
	
	/**
	 * @param repositoryEntryStatusCode
	 */
	public RepositoryEntryStatus createRepositoryEntryStatus(int repositoryEntryStatusCode) {
		return new RepositoryEntryStatus(repositoryEntryStatusCode);
	}
	
	/**
	 * Save repo entry.
	 * @param re
	 */
	public void saveRepositoryEntry(RepositoryEntry re) {
		if (re.getOwnerGroup() == null) {
			throw new AssertException("try to save RepositoryEntry without owner-group! Plase initialize owner-group.");
		}
		re.setLastModified(new Date());
		DBFactory.getInstance().saveObject(re);
	}
	
	/**
	 * Update repo entry.
	 * @param re
	 */
	public void updateRepositoryEntry(RepositoryEntry re) {
		re.setLastModified(new Date());
		DBFactory.getInstance().updateObject(re);
	}
	
	/**
	 * Delete repo entry.
	 * @param re
	 */
	public void deleteRepositoryEntry(RepositoryEntry re) {
		re = (RepositoryEntry) DBFactory.getInstance().loadObject(re,true);
		DBFactory.getInstance().deleteObject(re);
		//TODO:pb:b this should be called in a  RepoEntryImageManager.delete
		//instead of a controller.
		RepositoryEntryImageController.deleteImage(re);
	}
	
	/**
	 * 
	 * @param addedEntry
	 */
	public void deleteRepositoryEntryAndBasesecurity(RepositoryEntry entry) {
		entry = (RepositoryEntry) DBFactory.getInstance().loadObject(entry,true);
		DBFactory.getInstance().deleteObject(entry);
		OLATResourceManager.getInstance().deleteOLATResourceable(entry);
		SecurityGroup ownerGroup = entry.getOwnerGroup();
		if (ownerGroup != null) {
			// delete secGroup
			Tracing.logDebug("deleteRepositoryEntry deleteSecurityGroup ownerGroup=" + ownerGroup, this.getClass());
			BaseSecurityManager.getInstance().deleteSecurityGroup(ownerGroup);
			OLATResourceManager.getInstance().deleteOLATResourceable(ownerGroup);
		}
		//TODO:pb:b this should be called in a  RepoEntryImageManager.delete
		//instead of a controller.
		RepositoryEntryImageController.deleteImage(entry);
	}
	
	/**
	 * clean up a repo entry with all children and associated data like bookmarks and user references to it
	 * @param ureq
	 * @param wControl
	 * @param entry
	 * @return
	 * FIXME: we need a delete method without ureq, wControl for manager use. In general, very bad idea to pass ureq and
	 * wControl down to the manger layer.
	 */
	public boolean deleteRepositoryEntryWithAllData(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		// invoke handler delete callback
		Tracing.logDebug("deleteRepositoryEntry start entry=" + entry, this.getClass());
		entry = (RepositoryEntry) DBFactory.getInstance().loadObject(entry,true);
		Tracing.logDebug("deleteRepositoryEntry after load entry=" + entry, this.getClass());
		Tracing.logDebug("deleteRepositoryEntry after load entry.getOwnerGroup()=" + entry.getOwnerGroup(), this.getClass());
		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(entry);
		OLATResource ores = entry.getOlatResource();
		if (!handler.readyToDelete(ores, ureq, wControl)) return false;

		// start transaction
		// delete entry picture
		File uploadDir = new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome());
		File picFile = new File(uploadDir, entry.getKey() + ".jpg");
		if (picFile.exists()) {
			picFile.delete();
		}
		// delete all bookmarks referencing deleted entry
		BookmarkManager.getInstance().deleteAllBookmarksFor(entry);
		// delete all catalog entries referencing deleted entry
		CatalogManager.getInstance().resourceableDeleted(entry);
		// delete the entry
		entry = (RepositoryEntry) DBFactory.getInstance().loadObject(entry,true);
		Tracing.logDebug("deleteRepositoryEntry after reload entry=" + entry, this.getClass());
		deleteRepositoryEntryAndBasesecurity(entry);
		// inform handler to do any cleanup work... handler must delete the
		// referenced resourceable aswell.
		handler.cleanupOnDelete(entry.getOlatResource());
		Tracing.logDebug("deleteRepositoryEntry Done" , this.getClass());
		return true;
	}
	
	/**
	 * Lookup repo entry by key.
	 * @param the repository entry key (not the olatresourceable key)
	 * @return Repo entry represented by key or null if no such entry or key is null.
	 */
	public RepositoryEntry lookupRepositoryEntry(Long key) {
		if (key == null) return null;
		return (RepositoryEntry)DBFactory.getInstance().findObject(RepositoryEntry.class, key);
	}

	/**
	 * Lookup the repository entry which references the given olat resourceable.
	 * @param resourceable
	 * @param strict true: throws exception if not found, false: returns null if not found
	 * @return the RepositorEntry or null if strict=false
	 * @throws AssertException if the softkey could not be found (strict=true)
	 */
	public RepositoryEntry lookupRepositoryEntry(OLATResourceable resourceable, boolean strict) {
		OLATResource ores = OLATResourceManager.getInstance().findResourceable(resourceable);
		if (ores == null) {
			if (!strict) return null;
			throw new AssertException("Unable to fetch OLATResource for resourceable: " + resourceable.getResourceableTypeName() + ", " + resourceable.getResourceableId());
		}
		
		String query = "select v from org.olat.repository.RepositoryEntry v"+
			" inner join fetch v.olatResource as ores"+
			" where ores.key = :oreskey";
		DBQuery dbQuery = DBFactory.getInstance().createQuery(query);
		dbQuery.setLong("oreskey", ores.getKey().longValue());
		dbQuery.setCacheable(true);
		
		List result = dbQuery.list();
		int size = result.size();
		if (strict) {
			if (size != 1)
				throw new AssertException("Repository resourceable lookup returned zero or more than one result: " + size);
		}
		else { // not strict -> return null if zero entries found
			if (size > 1)
				throw new AssertException("Repository resourceable lookup returned more than one result: " + size);
			if (size == 0) {
				return null;
			}
		}
		return (RepositoryEntry)result.get(0);
	}

	/**
	 * Lookup a repository entry by its softkey.
	 * @param softkey
	 * @param strict true: throws exception if not found, false: returns null if not found
	 * @return the RepositorEntry or null if strict=false
	 * @throws AssertException if the softkey could not be found (strict=true)
	 */
	public RepositoryEntry lookupRepositoryEntryBySoftkey(String softkey, boolean strict) {
		String query = "select v from org.olat.repository.RepositoryEntry v" +
			" inner join fetch v.olatResource as ores"+
			" where v.softkey = :softkey";
		
		DBQuery dbQuery = DBFactory.getInstance().createQuery(query);
		dbQuery.setString("softkey", softkey);
		dbQuery.setCacheable(true);
		List result = dbQuery.list();
		
		int size = result.size();
		if (strict) {
			if (size != 1)
				throw new AssertException("Repository softkey lookup returned zero or more than one result: " + size+", softKey = "+softkey);
		}
		else { // not strict -> return null if zero entries found
			if (size > 1)
				throw new AssertException("Repository softkey lookup returned more than one result: " + size+", softKey = "+softkey);
			if (size == 0) {
				return null;
			}
		}
		return (RepositoryEntry)result.get(0);
	}
	
	/**
	 * Convenience method to access the repositoryEntry displayname by the referenced OLATResourceable id.
	 * This only works if a repository entry has an referenced olat resourceable like a course or an content package repo entry
	 * @param resId
	 * @return the repositoryentry displayname or null if not found
	 */
	public String lookupDisplayNameByOLATResourceableId(Long resId) {
		String query = "select v from org.olat.repository.RepositoryEntry v"+
			" inner join fetch v.olatResource as ores"+
			" where ores.resId = :resid";
		DBQuery dbQuery = DBFactory.getInstance().createQuery(query);
		dbQuery.setLong("resid", resId.longValue());
		dbQuery.setCacheable(true);
		
		List<RepositoryEntry> result = dbQuery.list();
		int size = result.size();
		if (size > 1) throw new AssertException("Repository lookup returned zero or more than one result: " + size);
		else if (size == 0) return null;
		RepositoryEntry entry = result.get(0);
		return entry.getDisplayname();
	}
	
	/**
	 * Test a repo entry if identity is allowed to launch.
	 * @param ureq
	 * @param re
	 * @return True if current identity is allowed to launch the given repo entry.
	 */
	public boolean isAllowedToLaunch(UserRequest ureq, RepositoryEntry re) {
		return isAllowedToLaunch(ureq.getIdentity(), ureq.getUserSession().getRoles(), re);
	}

	/**
	 * Test a repo entry if identity is allowed to launch.
	 * @param identity
	 * @param roles
	 * @param re
	 * @return True if current identity is allowed to launch the given repo entry.
	 */
	public boolean isAllowedToLaunch(Identity identity, Roles roles, RepositoryEntry re) {
		if (!re.getCanLaunch()) return false; // deny if not launcheable
		// allow if identity is owner
		if (BaseSecurityManager.getInstance().isIdentityPermittedOnResourceable(
				identity, Constants.PERMISSION_ACCESS, re.getOwnerGroup())) return true;
		// allow if access limit matches identity's role
		// allow for olat administrators
		if (roles.isOLATAdmin()) return true;
		// allow for institutional resource manager
		if (isInstitutionalRessourceManagerFor(re, identity)) return true;
		// allow for authors if access granted at least for authors
		if (roles.isAuthor() && re.getAccess() >= RepositoryEntry.ACC_OWNERS_AUTHORS) return true;
		// allow for guests if access granted for guests
		if (roles.isGuestOnly()) {
			if (re.getAccess() >= RepositoryEntry.ACC_USERS_GUESTS) return true;
			else return false;
		}
		// else allow if access granted for users
		return re.getAccess() >= RepositoryEntry.ACC_USERS;
		
	}

	/**
	 * Increment the launch counter.
	 * @param re
	 */
	public void incrementLaunchCounter(RepositoryEntry re) {
		taskQueueManager.addTask(new IncrementLaunchCounterBackgroundTask(re));
	}
	
	/**
	 * Increment the download counter.
	 * @param re
	 */
	public void incrementDownloadCounter(final RepositoryEntry re) {
		taskQueueManager.addTask(new IncrementDownloadCounterBackgroundTask(re));
	}

	/**
	 * Set last-usage date to to now for certain repository-entry.
	 * @param 
	 */
	public static void setLastUsageNowFor(final RepositoryEntry re) {
		if (re != null) {
			taskQueueManager.addTask(new SetLastUsageBackgroundTask(re));
		}
	}

	public void setAccess(final RepositoryEntry re, int access ) {
		SetAccessBackgroundTask task = new SetAccessBackgroundTask(re, access);
		taskQueueManager.addTask(task);
		task.waitForDone();
	}

	public void setDescriptionAndName(final RepositoryEntry re, String displayName, String description ) {
		SetDescriptionNameBackgroundTask task = new SetDescriptionNameBackgroundTask(re, displayName, description);
		taskQueueManager.addTask(task);
		task.waitForDone();
	}

	public void setProperties(final RepositoryEntry re, boolean canCopy, boolean canReference, boolean canLaunch, boolean canDownload ) {
		SetPropertiesBackgroundTask task = new SetPropertiesBackgroundTask(re, canCopy, canReference, canLaunch, canDownload);
		taskQueueManager.addTask(task);
		task.waitForDone();
	}
	
	/**
	 * Count by type, limit by role accessability.
	 * @param restrictedType
	 * @param roles
	 * @return Number of repo entries
	 */
	public int countByTypeLimitAccess(String restrictedType, int restrictedAccess) {
		StringBuilder query = new StringBuilder(400);
		query.append("select count(*) from" +
			" org.olat.repository.RepositoryEntry v, " +
			" org.olat.resource.OLATResourceImpl res " +
		  " where v.olatResource = res and res.resName= :restrictedType and v.access >= :restrictedAccess ");
		DBQuery dbquery = DBFactory.getInstance().createQuery(query.toString());
		dbquery.setString("restrictedType", restrictedType);
		dbquery.setInteger("restrictedAccess", restrictedAccess);
		dbquery.setCacheable(true);
		return ((Long)dbquery.list().get(0)).intValue();
	}

	
	/**
	 * Query by type without any other limitations
	 * @param restrictedType
	 * @param roles
	 * @return Results
	 */
	public List queryByType(String restrictedType) {
		String query = "select v from" +
			" org.olat.repository.RepositoryEntry v" +
			" inner join fetch v.olatResource as res"+
		  " where res.resName= :restrictedType";
		DBQuery dbquery = DBFactory.getInstance().createQuery(query);
		dbquery.setString("restrictedType", restrictedType);
		dbquery.setCacheable(true);
		return dbquery.list();
	}

	/**
	 * Query by type, limit by ownership or role accessability.
	 * @param restrictedType
	 * @param roles
	 * @return Results
	 */
	public List queryByTypeLimitAccess(String restrictedType, Roles roles) {
		StringBuilder query = new StringBuilder(400);
		query.append("select distinct v from" +
			" org.olat.repository.RepositoryEntry v" +
			" inner join fetch v.olatResource as res"+
		  " where res.resName= :restrictedType and v.access >= ");

		if (roles.isOLATAdmin()) query.append(RepositoryEntry.ACC_OWNERS); // treat admin special b/c admin is author as well
		else {
			if (roles.isAuthor()) query.append(RepositoryEntry.ACC_OWNERS_AUTHORS);
			else if (roles.isGuestOnly()) query.append(RepositoryEntry.ACC_USERS_GUESTS);
			else query.append(RepositoryEntry.ACC_USERS);
		}
		DBQuery dbquery = DBFactory.getInstance().createQuery(query.toString());
		dbquery.setString("restrictedType", restrictedType);
		dbquery.setCacheable(true);
		return dbquery.list();
	}
	
	/**
	 * Query by type, limit by ownership or role accessability.
	 * @param restrictedType
	 * @param roles
	 * @return Results
	 */
	public List queryByTypeLimitAccess(String restrictedType, UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		String institution = "";
		institution = ureq.getIdentity().getUser().getProperty("institutionalName", null);
		if(!roles.isOLATAdmin() && institution != null && institution.length() > 0 && roles.isInstitutionalResourceManager()) {
			StringBuilder query = new StringBuilder(400);
			query.append("select distinct v from org.olat.repository.RepositoryEntry v inner join fetch v.olatResource as res"
					+ ", org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi"
					+ ", org.olat.basesecurity.IdentityImpl identity"
					+ ", org.olat.user.UserImpl user "
					+ " where sgmsi.securityGroup = v.ownerGroup"
					+ " and sgmsi.identity = identity"
					+ " and identity.user = user"
					+" and user.properties['institutionalName']= :institutionCourseManager "
					+ " and res.resName= :restrictedType and v.access = 1");
			
			DBQuery dbquery = DBFactory.getInstance().createQuery(query.toString());
			dbquery.setString("restrictedType", restrictedType);
			dbquery.setString("institutionCourseManager", institution);
			dbquery.setCacheable(true);
			
			long start = System.currentTimeMillis();
			List result = dbquery.list();
			long timeQuery1 = System.currentTimeMillis() - start;
			Tracing.logInfo("Repo-Perf: queryByTypeLimitAccess#3 takes " + timeQuery1, this.getClass());
			start = System.currentTimeMillis();
			result.addAll(queryByTypeLimitAccess(restrictedType, roles));
			long timeQuery2 = System.currentTimeMillis() - start;
			Tracing.logInfo("Repo-Perf: queryByTypeLimitAccess#3 takes " + timeQuery2, this.getClass());
			return result;
			
		} else {
			long start = System.currentTimeMillis();
			List result = queryByTypeLimitAccess(restrictedType, roles);
			long timeQuery3 = System.currentTimeMillis() - start;
			Tracing.logInfo("Repo-Perf: queryByTypeLimitAccess#3 takes " + timeQuery3, this.getClass());
			return result;
		}
	}

	/**
	 * Query by ownership, optionally limit by type.
	 * 
	 * @param identity
	 * @param limitType
	 * @return Results
	 */
	public List queryByOwner(Identity identity, String limitType) {
		return queryByOwner( identity, new String[]{limitType});
	}

	public List queryByOwner(Identity identity, String[] limitTypes) {
		if (identity == null) throw new AssertException("identity can not be null!");
		StringBuffer query = new StringBuffer(400);
		query.append("select v from" + " org.olat.repository.RepositoryEntry v inner join fetch v.olatResource as res,"
				+ " org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi" + " where " + " v.ownerGroup = sgmsi.securityGroup and"
				+ " sgmsi.identity = :identity");
		if (limitTypes != null && limitTypes.length > 0) {
			for (int i = 0; i < limitTypes.length; i++) {
				String limitType = limitTypes[i];
				if (i == 0) {
					query.append(" and ( res.resName= '" + limitType + "'");
				} else {
					query.append(" or res.resName= '" + limitType + "'");
				}

			}
			query.append(" )");
		}
		DBQuery dbquery = DBFactory.getInstance().createQuery(query.toString());
		dbquery.setEntity("identity", identity);
		return dbquery.list();
	}

	/**
	 * Query by initial-author
	 * @param restrictedType
	 * @param roles
	 * @return Results
	 */
	public List queryByInitialAuthor(String initialAuthor) {
		String query = "select v from" +
			" org.olat.repository.RepositoryEntry v" +
		  " where v.initialAuthor= :initialAuthor";
		DBQuery dbquery = DBFactory.getInstance().createQuery(query);
		dbquery.setString("initialAuthor", initialAuthor);
		dbquery.setCacheable(true);
		return dbquery.list();
	}

	/**
	 * Search for resources that can be referenced by an author. This is the case:
	 * 1) the user is the owner of the resource
	 * 2) the user is author and the resource is at least visible to authors (BA) 
	 *    and the resource is set to canReference
	 * @param identity The user initiating the query
	 * @param roles The current users role set
	 * @param resourceTypes Limit search result to this list of repo types. Can be NULL
	 * @param displayName Limit search to this repo title. Can be NULL
	 * @param author Limit search to this user (Name, firstname, loginname). Can be NULL
	 * @param desc Limit search to description. Can be NULL
	 * @return List of repository entries
	 */	
	public List queryReferencableResourcesLimitType(Identity identity, Roles roles, List resourceTypes, String displayName, String author, String desc) {
		if (identity == null) {
			throw new AssertException("identity can not be null!");
		}
		if (!roles.isAuthor()) {
			// if user has no author right he can not reference to any resource at all
			return new ArrayList();
		}

		// cleanup some data: use null values if emtpy
		if (resourceTypes != null && resourceTypes.size() == 0) resourceTypes = null;
		if ( ! StringHelper.containsNonWhitespace(displayName)) displayName = null;
		if ( ! StringHelper.containsNonWhitespace(author)) author = null;
		if ( ! StringHelper.containsNonWhitespace(desc)) desc = null;
			
		// Build the query
		// 1) Joining tables 
		StringBuilder query = new StringBuilder(400);
		query.append("select distinct v from");
		query.append(" org.olat.repository.RepositoryEntry v inner join fetch v.olatResource as res" );
		query.append(", org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi");
		if (author != null) {
			query.append(", org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi2");
			query.append(", org.olat.basesecurity.IdentityImpl identity");
			query.append(", org.olat.user.UserImpl user ");
		}
		// 2) where clause
		query.append(" where "); 
		// the join of v.ownerGropu and sgmsi.securityGroup mus be outside the sgmsi.identity = :identity
		// otherwhise the join is not present in the second part of the or clause and the cross product will
		// be to large (does not work when more than 100 repo entries present!)
		query.append(" v.ownerGroup = sgmsi.securityGroup"); 
		// restrict on ownership or referencability flag
		query.append(" and ( sgmsi.identity = :identity "); 
		query.append(" or ");
		query.append(" (v.access >= :access and v.canReference = true) )");				
		// restrict on type
		if (resourceTypes != null) {
			query.append(" and res.resName in (:resourcetypes)");
		}
		// restrict on author
		if (author != null) { // fuzzy author search
			author = author.replace('*','%');
			author = '%' + author + '%';
			query.append(" and (sgmsi2.securityGroup = v.ownerGroup and "+
			"sgmsi2.identity = identity and "+
			"identity.user = user and "+
			"(user.properties['firstName'] like :author or user.properties['lastName'] like :author or identity.name like :author))");
		}
		// restrict on resource name
		if (displayName != null) {
			displayName = displayName.replace('*','%');
			displayName = '%' + displayName + '%';
			query.append(" and v.displayname like :displayname");
		}
		// restrict on resource description
		if (desc != null) {
			desc = desc.replace('*','%');
			desc = '%' + desc + '%';
			query.append(" and v.description like :desc");
		}
		
		// create query an set query data
		DBQuery dbquery = DBFactory.getInstance().createQuery(query.toString());
		dbquery.setEntity("identity", identity);
		dbquery.setInteger("access", RepositoryEntry.ACC_OWNERS_AUTHORS);
		if (author != null) {
			dbquery.setString("author", author);
		}
		if (displayName != null) {
			dbquery.setString("displayname", displayName);
		}
		if (desc != null) {
			dbquery.setString("desc", desc);
		}
		if (resourceTypes != null) {
			dbquery.setParameterList("resourcetypes", resourceTypes, Hibernate.STRING);
		}
		return dbquery.list();		
		
	}

	
	/**
	 * Query by ownership, limit by access.
	 * 
	 * @param identity
	 * @param limitAccess
	 * @return Results
	 */
	public List queryByOwnerLimitAccess(Identity identity, int limitAccess) {
		String query = "select v from" +
			" org.olat.repository.RepositoryEntry v inner join fetch v.olatResource as res," + 
			" org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi" +
			" where" +
			" v.ownerGroup = sgmsi.securityGroup "+
		  " and sgmsi.identity = :identity and v.access >= :limitAccess";
		
		DBQuery dbquery = DBFactory.getInstance().createQuery(query);
		dbquery.setEntity("identity", identity);
		dbquery.setInteger("limitAccess", limitAccess);
		return dbquery.list();		
	}
	
	/**
	 * check ownership of identiy for a resource
	 * @return true if the identity is member of the security group of the repository entry
	 */
	public boolean isOwnerOfRepositoryEntry(Identity identity, RepositoryEntry entry){
		//TODO:gs:a transform into direct hibernate query
		SecurityGroup ownerGroup = lookupRepositoryEntry(entry.getOlatResource(), true).getOwnerGroup();
		return BaseSecurityManager.getInstance().isIdentityInSecurityGroup(identity, ownerGroup);
	}
	
	/**
	 * Query repository
	 * 
	 * If any input data contains "*", then it replaced by "%" (search me*er -> sql: me%er) 
	 * 
	 * @param displayName null -> no restriction
	 * @param author			null -> no restriction
	 * @param desc				null -> no restriction
	 * @param resourceTypes	NOTE: for null -> no restriction, or a list of resourceTypeNames
	 * @param roles The calling user's roles
	 * @return Results as List containing RepositoryEntries
	 */
	private List runGenericANDQueryWithRolesRestriction(String displayName, String author, String desc, List resourceTypes, Roles roles) {
		StringBuilder query = new StringBuilder(400);
		
		boolean var_author = (author != null && author.length() != 0);
		boolean var_displayname = (displayName != null && displayName.length() != 0);
		boolean var_desc = (desc != null && desc.length() != 0);
		boolean var_resourcetypes = (resourceTypes != null && resourceTypes.size() > 0);

		// Use two different select prologues... 
		if (var_author) { // extended query for user search
			query.append("select distinct v from" +
			" org.olat.repository.RepositoryEntry v inner join fetch v.olatResource as res," + 
			" org.olat.basesecurity.SecurityGroupMembershipImpl sgmsi, " + 
			" org.olat.basesecurity.IdentityImpl identity," +
			" org.olat.user.UserImpl user ");
		} else { // simple query
			query.append("select distinct v from" +
				" org.olat.repository.RepositoryEntry v " +
				" inner join fetch v.olatResource as res  ");
		} 

		boolean isFirstOfWhereClause = false;
  	query.append("where v.access != 0 "); // access == 0 means invalid repo-entry (not complete created)    
		if (var_author) { // fuzzy author search
			author = author.replace('*','%');
			author = '%' + author + '%';
			if (!isFirstOfWhereClause) query.append(" and ");
			query.append("sgmsi.securityGroup = v.ownerGroup and "+
			"sgmsi.identity = identity and "+
			"identity.user = user and "+
			"(user.properties['firstName'] like :author or user.properties['lastName'] like :author or identity.name like :author)");
			isFirstOfWhereClause = false;
		}

		if (var_displayname) {
			displayName = displayName.replace('*','%');
			displayName = '%' + displayName + '%';
			if (!isFirstOfWhereClause) query.append(" and ");
			query.append("v.displayname like :displayname");
			isFirstOfWhereClause = false;
		}

		if (var_desc) {
			desc = desc.replace('*','%');
			desc = '%' + desc + '%';
			if (!isFirstOfWhereClause) query.append(" and ");
			query.append("v.description like :desc");
			isFirstOfWhereClause = false;
		}

		if (var_resourcetypes) {
			if (!isFirstOfWhereClause) query.append(" and ");
			query.append("res.resName in (:resourcetypes)");
			isFirstOfWhereClause = false;
		}

		// finally limit on roles, if not olat admin
		if (!roles.isOLATAdmin()) {
			if (!isFirstOfWhereClause) query.append(" and ");
			query.append("v.access >= ");
			if (roles.isAuthor()) query.append(RepositoryEntry.ACC_OWNERS_AUTHORS);
			else if (roles.isGuestOnly()) query.append(RepositoryEntry.ACC_USERS_GUESTS);
			else query.append(RepositoryEntry.ACC_USERS);
			isFirstOfWhereClause = false;
		}
		
		DBQuery dbQuery = DBFactory.getInstance().createQuery(query.toString());
		if (var_author) {
			dbQuery.setString("author", author);
		}
		if (var_displayname) {
			dbQuery.setString("displayname", displayName);
		}
		if (var_desc) {
			dbQuery.setString("desc", desc);
		}
		if (var_resourcetypes) {
			dbQuery.setParameterList("resourcetypes", resourceTypes, Hibernate.STRING);
		}
		return dbQuery.list();
	}
	
	/**
	 * Query repository
	 * 
	 * If any input data contains "*", then it replaced by "%" (search me*er -> sql: me%er).
	 * 
	 * @deprecated Use genericANDQueryWithRolesRestriction with paging instead
	 * @param ureq
	 * @param displayName null -> no restriction
	 * @param author			null -> no restriction
	 * @param desc				null -> no restriction
	 * @param resourceTypes	NOTE: for null -> no restriction, or a list of resourceTypeNames
	 * @param roles The calling user's roles
	 * @param institution null -> no restriction
	 * @return Results as List containing RepositoryEntries
	 */
	public List genericANDQueryWithRolesRestriction(String displayName, String author, String desc, List resourceTypes, Roles roles, String institution) {
		if (!roles.isOLATAdmin() && institution != null && institution.length() > 0 && roles.isInstitutionalResourceManager()) {
			StringBuilder query = new StringBuilder(400);
			if(author == null || author.length() == 0) author = "*";
			boolean var_author = true;
			boolean var_displayname = (displayName != null && displayName.length() != 0);
			boolean var_desc = (desc != null && desc.length() != 0);
			boolean var_resourcetypes = (resourceTypes != null && resourceTypes.size() > 0);
			// Use two different select prologues... 
			if (var_author) { // extended query for user search
				query.append("select distinct v from" 
						+ " org.olat.repository.RepositoryEntry v inner join fetch v.olatResource as res,"
						+ " org.olat.basesecurity.SecurityGroupMembershipImpl sgmsi, " 
						+ " org.olat.basesecurity.IdentityImpl identity,"
						+ " org.olat.user.UserImpl user ");
			} else { // simple query
				query.append("select distinct v from" + " org.olat.repository.RepositoryEntry v " + " inner join fetch v.olatResource as res  ");
			}
			boolean isFirstOfWhereClause = false;
			query.append("where v.access != 0 "); // access == 0 means invalid repo-entry (not complete created)    
			if (var_author) { // fuzzy author search
				author = author.replace('*', '%');
				author = '%' + author + '%';
				if (!isFirstOfWhereClause) query.append(" and ");
				query.append("sgmsi.securityGroup = v.ownerGroup and " 
						+ "sgmsi.identity = identity and " 
						+ "identity.user = user and " 
						+ "(user.properties['firstName'] like :author or user.properties['lastName'] like :author or identity.name like :author)");
				isFirstOfWhereClause = false;
			}
			if (var_displayname) {
				displayName = displayName.replace('*', '%');
				displayName = '%' + displayName + '%';
				if (!isFirstOfWhereClause) query.append(" and ");
				query.append("v.displayname like :displayname");
				isFirstOfWhereClause = false;
			}
			if (var_desc) {
				desc = desc.replace('*', '%');
				desc = '%' + desc + '%';
				if (!isFirstOfWhereClause) query.append(" and ");
				query.append("v.description like :desc");
				isFirstOfWhereClause = false;
			}
			if (var_resourcetypes) {
				if (!isFirstOfWhereClause) query.append(" and ");
				query.append("res.resName in (:resourcetypes)");
				isFirstOfWhereClause = false;
			}
			
			if (!isFirstOfWhereClause) query.append(" and ");
			query.append("v.access = 1 and user.properties['institutionalName']= :institution ");
			isFirstOfWhereClause = false;
			
			DBQuery dbQuery = DBFactory.getInstance().createQuery(query.toString());
			dbQuery.setString("institution", institution);
			if (var_author) {
				dbQuery.setString("author", author);
			}
			if (var_displayname) {
				dbQuery.setString("displayname", displayName);
			}
			if (var_desc) {
				dbQuery.setString("desc", desc);
			}
			if (var_resourcetypes) {
				dbQuery.setParameterList("resourcetypes", resourceTypes, Hibernate.STRING);
			}
			List result = dbQuery.list();
			result.addAll(runGenericANDQueryWithRolesRestriction(displayName, author, desc, resourceTypes, roles));
			return result;
		} else {
			return runGenericANDQueryWithRolesRestriction(displayName, author, desc, resourceTypes, roles);
		}
	}
	
	public int countGenericANDQueryWithRolesRestriction(String displayName, String author, String desc, List<String> resourceTypes, Roles roles,
			String institution, boolean orderBy) {
		DBQuery dbQuery = createGenericANDQueryWithRolesRestriction(displayName, author, desc, resourceTypes, roles, institution, orderBy, true);
		Number count = (Number)dbQuery.uniqueResult();
		return count.intValue();
	}
	
	public List<RepositoryEntry> genericANDQueryWithRolesRestriction(String displayName, String author, String desc, List<String> resourceTypes, Roles roles,
			String institution, int firstResult, int maxResults, boolean orderBy) {
		
		DBQuery dbQuery = createGenericANDQueryWithRolesRestriction(displayName, author, desc, resourceTypes, roles, institution, orderBy, false);
		dbQuery.setFirstResult(firstResult);
		if(maxResults > 0) {
			dbQuery.setMaxResults(maxResults);
		}
		List<RepositoryEntry> res = dbQuery.list();
		return res;
	}
	
	private DBQuery createGenericANDQueryWithRolesRestriction(String displayName, String author, String desc, List<String> resourceTypes, Roles roles,
			String institution, boolean orderBy, boolean count) {
		
		boolean institut = (!roles.isOLATAdmin() && institution != null && institution.length() > 0 && roles.isInstitutionalResourceManager());
		boolean var_author = StringHelper.containsNonWhitespace(author);
		boolean var_displayname = StringHelper.containsNonWhitespace(displayName);
		boolean var_desc = StringHelper.containsNonWhitespace(desc);
		boolean var_resourcetypes = (resourceTypes != null && resourceTypes.size() > 0);
		
		StringBuilder query = new StringBuilder();
		if(count) {
			query.append("select count(v.key) from ").append(RepositoryEntry.class.getName()).append(" v ");
			query.append(" inner join v.olatResource as res");
		} else {
			query.append("select distinct v from ").append(RepositoryEntry.class.getName()).append(" v ");
			query.append(" inner join fetch v.olatResource as res");
		}

		//access rules
		if(roles.isOLATAdmin()) {
			query.append(" where v.access!=0 ");
		} else if(institut) {
			query.append(" where ((v.access >=");
			if (roles.isAuthor()) query.append(RepositoryEntry.ACC_OWNERS_AUTHORS);
			else if (roles.isGuestOnly()) query.append(RepositoryEntry.ACC_USERS_GUESTS);
			else query.append(RepositoryEntry.ACC_USERS);
			query.append(") or (");
			
			query.append("v.access=1 and v.ownerGroup in (select ms.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" ms, ")
			     .append(" org.olat.basesecurity.IdentityImpl msid,")
			     .append(" org.olat.user.UserImpl msuser ")
			     .append(" where ms.identity = msid and msid.user = msuser and ")
			     .append(" msuser.properties['institutionalName']=:institution)")
			     .append("))");
		} else {
			query.append(" where v.access>=");
			if (roles.isAuthor()) query.append(RepositoryEntry.ACC_OWNERS_AUTHORS);
			else if (roles.isGuestOnly()) query.append(RepositoryEntry.ACC_USERS_GUESTS);
			else query.append(RepositoryEntry.ACC_USERS);
		}
		
		if (var_author) { // fuzzy author search
			author = '%' + author.replace('*', '%') + '%';
			query.append(" and v.ownerGroup in (select msauth.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" msauth, ")
		         .append(" org.olat.basesecurity.IdentityImpl msauthid,")
		         .append(" org.olat.user.UserImpl msauthuser ")
		         .append(" where msauth.identity = msauthid and msauthid.user = msauthuser and ")
		         .append(" (msauthuser.properties['firstName'] like :author or msauthuser.properties['lastName'] like :author or msauthid.name like :author))");
		}
		if (var_displayname) {
			displayName = '%' + displayName.replace('*', '%') + '%';
			query.append(" and v.displayname like :displayname");
		}
		if (var_desc) {
			desc = '%' + desc.replace('*', '%') + '%';
			query.append(" and v.description like :desc");
		}
		if (var_resourcetypes) {
			query.append(" and res.resName in (:resourcetypes)");
		}

		if(orderBy) {
			query.append(" order by v.displayname, v.key");
		}

		DBQuery dbQuery = DBFactory.getInstance().createQuery(query.toString());
		if(institut) {
			dbQuery.setString("institution", institution);
		}
		if (var_author) {
			dbQuery.setString("author", author);
		}
		if (var_displayname) {
			dbQuery.setString("displayname", displayName);
		}
		if (var_desc) {
			dbQuery.setString("desc", desc);
		}
		if (var_resourcetypes) {
			dbQuery.setParameterList("resourcetypes", resourceTypes, Hibernate.STRING);
		}
		return dbQuery;
	}
	
	/**
	 * add provided list of identities as owners to the repo entry. silently ignore
	 * if some identities were already owners before.
	 * @param ureqIdentity
	 * @param addIdentities
	 * @param re
	 * @param userActivityLogger
	 */
	public void addOwners(Identity ureqIdentity, IdentitiesAddEvent iae, RepositoryEntry re) {
		List<Identity> addIdentities = iae.getAddIdentities();
		List<Identity> reallyAddedId = new ArrayList<Identity>();
		SecurityGroup group = re.getOwnerGroup();
		for (Identity identity : addIdentities) {
			if (!securityManager.isIdentityInSecurityGroup(identity, re.getOwnerGroup())) {
				securityManager.addIdentityToSecurityGroup(identity, re.getOwnerGroup());
				reallyAddedId.add(identity);
				ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
				ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
				try{
					ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_ADDED, getClass(),
							LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry), LoggingResourceable.wrap(identity));
				} finally {
					ThreadLocalUserActivityLogger.setStickyActionType(actionType);
				}
				Tracing.logAudit("Idenitity(.key):" + ureqIdentity.getKey() + " added identity '" + identity.getName()
						+ "' to securitygroup with key " + re.getOwnerGroup().getKey(), this.getClass());
			}//else silently ignore already owner identities
		}
		iae.setIdentitiesAddedEvent(reallyAddedId);
	}
	
	/**
	 * remove list of identities as owners of given repository entry.
	 * @param ureqIdentity
	 * @param removeIdentities
	 * @param re
	 * @param logger
	 */
	public void removeOwners(Identity ureqIdentity, List<Identity> removeIdentities, RepositoryEntry re){
    for (Identity identity : removeIdentities) {
    	securityManager.removeIdentityFromSecurityGroup(identity, re.getOwnerGroup());
			String details = "Remove Owner from RepoEntry:"+re.getKey()+" USER:" + identity.getName();

			ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
			ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
			try{
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_REMOVED, getClass(),
						LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry), LoggingResourceable.wrap(identity));
			} finally {
				ThreadLocalUserActivityLogger.setStickyActionType(actionType);
			}
			Tracing.logAudit("Idenitity(.key):" + ureqIdentity.getKey() + " removed identity '" + identity.getName()
					+ "' from securitygroup with key " + re.getOwnerGroup().getKey(), this.getClass());
    }
	}

	/**
	 * has one owner of repository entry the same institution like the resource manager
	 * @param RepositoryEntry repositoryEntry
	 * @param Identity identity
	 */
	public boolean isInstitutionalRessourceManagerFor(RepositoryEntry repositoryEntry, Identity identity) {
		if(repositoryEntry == null || repositoryEntry.getOwnerGroup() == null) return false;
		BaseSecurity secMgr = BaseSecurityManager.getInstance();
		// list of owners
		List<Identity> listIdentities = secMgr.getIdentitiesOfSecurityGroup(repositoryEntry.getOwnerGroup());
		String currentUserInstitutionalName = identity.getUser().getProperty("institutionalName", null);
		boolean isInstitutionalResourceManager = BaseSecurityManager.getInstance().isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_INSTORESMANAGER);
		boolean sameInstitutional = false;
		String identInstitutionalName = "";
		for (Identity ident : listIdentities) {
			identInstitutionalName = ident.getUser().getProperty("institutionalName", null);
			if ((identInstitutionalName != null) && (identInstitutionalName.equals(currentUserInstitutionalName))) {
				sameInstitutional = true;
				break;
			}
		}
		return isInstitutionalResourceManager && sameInstitutional;
	}

	
	/**
	 * Gets all learning resources where the user is in a learning group as participant.
	 * @param identity
	 * @return list of RepositoryEntries
	 */
	public List<RepositoryEntry> getLearningResourcesAsStudent(Identity identity) {
		List<RepositoryEntry> allRepoEntries = new ArrayList<RepositoryEntry>();
		List<BusinessGroup>groupList = BusinessGroupManagerImpl.getInstance().findBusinessGroupsAttendedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
		for (BusinessGroup group : groupList) {
			BGContext bgContext = group.getGroupContext();
			if (bgContext == null) continue;
			List<RepositoryEntry> repoEntries = BGContextManagerImpl.getInstance().findRepositoryEntriesForBGContext(bgContext);
			if (repoEntries == null || repoEntries.size() == 0) continue;
			for (RepositoryEntry repositoryEntry : repoEntries) {
				// only find resources that are published
				if ( ! PersistenceHelper.listContainsObjectByKey(allRepoEntries, repositoryEntry) && repositoryEntry.getAccess() >= RepositoryEntry.ACC_USERS ) {
					allRepoEntries.add(repositoryEntry);
				}				
			}
		}
		return allRepoEntries;
	}
	
	/**
	 * Gets all learning resources where the user is coach of a learning group or
	 * where he is in a rights group or where he is in the repository entry owner 
	 * group (course administrator)
	 * 
	 * @param identity
	 * @return list of RepositoryEntries
	 */
	public List<RepositoryEntry> getLearningResourcesAsTeacher(Identity identity) {
		List<RepositoryEntry> allRepoEntries = new ArrayList<RepositoryEntry>();
		// 1: search for all learning groups where user is coach
		List<BusinessGroup>groupList = BusinessGroupManagerImpl.getInstance().findBusinessGroupsOwnedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
		for (BusinessGroup group : groupList) {
			BGContext bgContext = group.getGroupContext();
			if (bgContext == null) continue;
			List<RepositoryEntry> repoEntries = BGContextManagerImpl.getInstance().findRepositoryEntriesForBGContext(bgContext);
			if (repoEntries.size() == 0) continue;
			for (RepositoryEntry repositoryEntry : repoEntries) {
				// only find resources that are published
				if ( ! PersistenceHelper.listContainsObjectByKey(allRepoEntries, repositoryEntry) && repositoryEntry.getAccess() >= RepositoryEntry.ACC_USERS) {
					allRepoEntries.add(repositoryEntry);
				}				
			}
		}
		// 2: search for all learning groups where user is coach
		List<BusinessGroup>rightGrougList = BusinessGroupManagerImpl.getInstance().findBusinessGroupsAttendedBy(BusinessGroup.TYPE_RIGHTGROUP, identity, null);
		for (BusinessGroup group : rightGrougList) {
			BGContext bgContext = group.getGroupContext();
			if (bgContext == null) continue;
			List<RepositoryEntry> repoEntries = BGContextManagerImpl.getInstance().findRepositoryEntriesForBGContext(bgContext);
			if (repoEntries.size() == 0) continue;
			for (RepositoryEntry repositoryEntry : repoEntries) {
				// only find resources that are published
				if ( ! PersistenceHelper.listContainsObjectByKey(allRepoEntries, repositoryEntry) && repositoryEntry.getAccess() >= RepositoryEntry.ACC_USERS) {
					allRepoEntries.add(repositoryEntry);
				}				
			}
		}
		// 3) search for all published learning resources that user owns
		List<RepositoryEntry> repoEntries = RepositoryManager.getInstance().queryByOwnerLimitAccess(identity, RepositoryEntry.ACC_USERS);
		for (RepositoryEntry repositoryEntry : repoEntries) {
			if ( ! PersistenceHelper.listContainsObjectByKey(allRepoEntries, repositoryEntry)) {
				allRepoEntries.add(repositoryEntry);
			}				
		}
		return allRepoEntries;
	}
	
}