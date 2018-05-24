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

package org.olat.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.group.GroupLoggingAction;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.repository.model.RepositoryEntryMembershipModifiedEvent;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.model.RepositoryEntryShortImpl;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.provider.auto.AutoAccessManager;
import org.olat.search.service.document.RepositoryEntryDocument;
import org.olat.search.service.indexer.LifeFullIndexer;
import org.olat.user.UserImpl;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial Date:  Mar 31, 2004
 *
 * @author Mike Stock
 *
 * Comment:
 *
 */
@Service("repositoryManager")
public class RepositoryManager {

	private static final OLog log = Tracing.createLoggerFor(RepositoryManager.class);

	public static final int PICTURE_WIDTH = 570;
	public static final int PICTURE_HEIGHT = (PICTURE_WIDTH / 3) * 2;

	@Autowired
	private ImageService imageHelper;
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private LifeFullIndexer lifeIndexer;
	@Autowired
	private AutoAccessManager autoAccessManager;

	/**
	 * @return Singleton.
	 */
	public static RepositoryManager getInstance() {
		return CoreSpringFactory.getImpl(RepositoryManager.class);
	}

	/**
	 * @param repositoryEntryStatusCode
	 */
	public RepositoryEntryStatus createRepositoryEntryStatus(int repositoryEntryStatusCode) {
		return new RepositoryEntryStatus(repositoryEntryStatusCode);
	}

	/**
	 * Copy the repo entry image from the source to the target repository entry.
	 * If the source repo entry does not exists, nothing will happen
	 *
	 * @param src
	 * @param target
	 * @return
	 */
	public boolean copyImage(RepositoryEntry source, RepositoryEntry target) {
		VFSLeaf srcFile = getImage(source);
		if(srcFile == null) {
			return false;
		}

		VFSLeaf targetFile = getImage(target);
		if(targetFile != null) {
			targetFile.delete();
		}

		String sourceImageSuffix = FileUtils.getFileSuffix(srcFile.getName());
		VFSContainer repositoryHome = new LocalFolderImpl(new File(FolderConfig.getCanonicalRepositoryHome()));
		VFSLeaf newImage = repositoryHome.createChildLeaf(target.getResourceableId() + "." + sourceImageSuffix);
		if (newImage != null) {
			return VFSManager.copyContent(srcFile, newImage);
		}
		return false;
	}

	public void deleteImage(RepositoryEntry re) {
		VFSLeaf imgFile =  getImage(re);
		if (imgFile != null) {
			if(imgFile instanceof MetaTagged) {
				MetaInfo info = ((MetaTagged)imgFile).getMetaInfo();
				if(info != null) {
					info.clearThumbnails();
				}
			}
			imgFile.delete();
		}
	}

	public VFSLeaf getImage(OLATResourceable re) {
		VFSContainer repositoryHome = new LocalFolderImpl(new File(FolderConfig.getCanonicalRepositoryHome()));
		String imageName = re.getResourceableId() + ".jpg";
		VFSItem image = repositoryHome.resolve(imageName);
		if(image instanceof VFSLeaf) {
			return (VFSLeaf)image;
		}
		imageName = re.getResourceableId() + ".png";
		image = repositoryHome.resolve(imageName);
		if(image instanceof VFSLeaf) {
			return (VFSLeaf)image;
		}
		return null;
	}

	public boolean setImage(VFSLeaf newImageFile, RepositoryEntry re) {
		VFSLeaf currentImage = getImage(re);
		if(currentImage != null) {
			if(currentImage instanceof MetaTagged) {
				MetaInfo info = ((MetaTagged)currentImage).getMetaInfo();
				if(info != null) {
					info.clearThumbnails();
				}
			}
			currentImage.delete();
		}

		if(newImageFile == null || !newImageFile.exists() || newImageFile.getSize() <= 0) {
			return false;
		}

		String targetExtension = ".png";
		String extension = FileUtils.getFileSuffix(newImageFile.getName());
		if("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)) {
			targetExtension = ".jpg";
		}

		VFSContainer repositoryHome = new LocalFolderImpl(new File(FolderConfig.getCanonicalRepositoryHome()));
		VFSLeaf repoImage = repositoryHome.createChildLeaf(re.getResourceableId() + targetExtension);

		if(targetExtension.equals(".png") || targetExtension.equals(".jpg")) {
			Size newImageSize = imageHelper.getSize(newImageFile, extension);
			if(newImageSize != null && newImageSize.getWidth() <= PICTURE_WIDTH && newImageSize.getHeight() <= PICTURE_HEIGHT) {
				boolean success = VFSManager.copyContent(newImageFile, repoImage);
				return success;
			}
		}

		Size size = imageHelper.scaleImage(newImageFile, repoImage, PICTURE_WIDTH, PICTURE_WIDTH, false);
		return size != null;
	}


	/**
	 * Lookup repo entry by key.
	 * @param the repository entry key (not the olatresourceable key)
	 * @return Repo entry represented by key or null if no such entry or key is null.
	 */
	public RepositoryEntry lookupRepositoryEntry(Long key) {
		if (key == null) return null;
		return lookupRepositoryEntry(key, false) ;
	}

	/**
	 * Lookup repo entry by key.
	 * @param the repository entry key (not the olatresourceable key)
	 * @return Repo entry represented by key or null if no such entry or key is null.
	 */
	public RepositoryEntry lookupRepositoryEntry(Long key, boolean strict) {
		if (key == null) return null;
		if(strict) {
			return lookupRepositoryEntry(key);
		}
		List<RepositoryEntry> entries = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadRepositoryEntryByKey", RepositoryEntry.class)
				.setParameter("repoKey", key)
				//.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}

	public OLATResource lookupRepositoryEntryResource(Long key) {
		if (key == null) return null;
		StringBuilder query = new StringBuilder();
		query.append("select v.olatResource from ").append(RepositoryEntry.class.getName()).append(" as v ")
		     .append(" where v.key = :repoKey");

		List<OLATResource> entries = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), OLATResource.class)
				.setParameter("repoKey", key)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}

	public List<RepositoryEntry> lookupRepositoryEntries(Collection<Long> keys) {
		if (keys == null || keys.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where v.key in (:repoKey)");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("repoKey", keys)
				.getResultList();
	}

	/**
	 * Lookup the repository entry which references the given olat resourceable.
	 * @param resourceable
	 * @param strict true: throws exception if not found, false: returns null if not found
	 * @return the RepositorEntry or null if strict=false
	 * @throws AssertException if the softkey could not be found (strict=true)
	 */
	public RepositoryEntry lookupRepositoryEntry(OLATResourceable resourceable, boolean strict) {
		OLATResource ores = (resourceable instanceof OLATResource) ? (OLATResource)resourceable
				: OLATResourceManager.getInstance().findResourceable(resourceable);
		if (ores == null) {
			if (!strict) return null;
			throw new AssertException("Unable to fetch OLATResource for resourceable: " + resourceable.getResourceableTypeName() + ", " + resourceable.getResourceableId());
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where ores.key = :oreskey");

		List<RepositoryEntry> result = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("oreskey", ores.getKey())
				.getResultList();
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
		return result.get(0);
	}

	public Long lookupRepositoryEntryKey(OLATResourceable resourceable, boolean strict) {
		OLATResource ores = (resourceable instanceof OLATResource) ? (OLATResource)resourceable
				: OLATResourceManager.getInstance().findResourceable(resourceable);
		if (ores == null) {
			if (!strict) return null;
			throw new AssertException("Unable to fetch OLATResource for resourceable: " + resourceable.getResourceableTypeName() + ", " + resourceable.getResourceableId());
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select v.key from ").append(RepositoryEntry.class.getName()).append(" v ")
		  .append(" where v.olatResource.key=:oreskey");

		List<Long> result = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("oreskey", ores.getKey())
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
		int size = result.size();
		if (strict) {
			if (size != 1)
				throw new AssertException("Repository resourceable lookup returned zero or more than one result: " + size);
		} else { // not strict -> return null if zero entries found
			if (size > 1)
				throw new AssertException("Repository resourceable lookup returned more than one result: " + size);
			if (size == 0) {
				return null;
			}
		}
		return result.get(0);
	}

	/**
	 * Lookup a repository entry by its softkey.
	 * @param softkey
	 * @param strict true: throws exception if not found, false: returns null if not found
	 * @return the RepositorEntry or null if strict=false
	 * @throws AssertException if the softkey could not be found (strict=true)
	 */
	public RepositoryEntry lookupRepositoryEntryBySoftkey(String softkey, boolean strict) {
		if(softkey == null || "sf.notconfigured".equals(softkey)) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" v")
		  .append(" inner join fetch v.olatResource as ores ")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where v.softkey=:softkey");

		List<RepositoryEntry> result = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("softkey", softkey)
				.getResultList();

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
		return result.get(0);
	}

	/**
	 * Convenience method to access the repositoryEntry displayname by the referenced OLATResourceable id.
	 * This only works if a repository entry has an referenced olat resourceable like a course or an content package repo entry
	 * @param resId
	 * @return the repositoryentry displayname or null if not found
	 */
	public String lookupDisplayNameByOLATResourceableId(Long resId) {
		List<String> displaynames = dbInstance.getCurrentEntityManager()
				.createNamedQuery("getDisplayNameByOlatResourceRedId", String.class)
				.setParameter("resid", resId)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();

		if (displaynames.size() > 1) throw new AssertException("Repository lookup returned zero or more than one result: " + displaynames.size());
		else if (displaynames.isEmpty()) return null;
		return displaynames.get(0);
	}
	
	public String lookupDisplayNameByResourceKey(Long resourceKey) {
		List<String> displaynames = dbInstance.getCurrentEntityManager()
				.createNamedQuery("getDisplayNameByResourceKey", String.class)
				.setParameter("resKey", resourceKey)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();

		if (displaynames.size() > 1) throw new AssertException("Repository lookup returned zero or more than one result: " + displaynames.size());
		else if (displaynames.isEmpty()) return null;
		return displaynames.get(0);
	}
	
	/**
	 * Convenience method to access the repositoryEntry displayname by the referenced OLATResourceable id.
	 * This only works if a repository entry has an referenced olat resourceable like a course or an content package repo entry
	 * @param resId
	 * @return the repositoryentry displayname or null if not found
	 */
	public String lookupDisplayName(Long reId) {
		List<String> displaynames = dbInstance.getCurrentEntityManager()
				.createNamedQuery("getDisplayNameByRepositoryEntryKey", String.class)
				.setParameter("reKey", reId)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();

		if (displaynames.size() > 1) throw new AssertException("Repository lookup returned zero or more than one result: " + displaynames.size());
		else if (displaynames.isEmpty()) return null;
		return displaynames.get(0);
	}

    /**
     * Check if (and which) external IDs already exist.
     * @param a collection of external IDs to check if already existing
     * @return a list of already existing external IDs (or an emtpy list).
     */
    public List<String> lookupExistingExternalIds(Collection<String> externalIds) {
            if (externalIds == null || externalIds.isEmpty()) {
                    return Collections.emptyList();
            }

            StringBuilder query = new StringBuilder();
            query.append("select v.externalId from ").append(RepositoryEntry.class.getName()).append(" as v ")
                    .append("where v.externalId in (:externalIds)");

            List<String> existingExternalIds =
                    dbInstance.getCurrentEntityManager().createQuery(query.toString(), String.class)
                    .setParameter("externalIds", externalIds)
                    .getResultList();

            return existingExternalIds;
    }

	/**
	 * Load a list of repository entry without all the security groups ...
	 * @param resources
	 * @return
	 */
	public List<RepositoryEntryShort> loadRepositoryEntryShorts(List<OLATResource> resources) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntryShortImpl.class.getName()).append(" v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" where ores.key in (:resKeys)");

		List<Long> resourceKeys = PersistenceHelper.toKeys(resources);
		List<RepositoryEntryShort> shorties = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryShort.class)
				.setParameter("resKeys", resourceKeys)
				.getResultList();
		return shorties;
	}

	/**
	 * Load a list of repository entry without all the security groups ...
	 * @param resources
	 * @return
	 */
	public List<RepositoryEntryShortImpl> loadRepositoryEntryShortsByResource(Collection<Long> resIds, String resourceType) {
		List<RepositoryEntryShortImpl> shorties = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadRepositoryEntryShortsByResourceableIds", RepositoryEntryShortImpl.class)
				.setParameter("resIds", resIds)
				.setParameter("resName", resourceType)
				.getResultList();
		return shorties;
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

	public RepositoryEntrySecurity isAllowed(UserRequest ureq, RepositoryEntry re) {
		return isAllowed(ureq.getIdentity(), ureq.getUserSession().getRoles(), re);
	}

	/**
	 * Test a repo entry if identity is allowed to launch.
	 * @param identity
	 * @param roles
	 * @param re
	 * @return True if current identity is allowed to launch the given repo entry.
	 */
	public boolean isAllowedToLaunch(Identity identity, Roles roles, RepositoryEntry re) {
		// allow if identity is owner
		if (repositoryEntryRelationDao.hasRole(identity, re, GroupRoles.owner.name())) {
			return true;
		}
		// allow if access limit matches identity's role
		// allow for olat administrators
		if (roles.isOLATAdmin()) return true;
		// allow for institutional resource manager
		if (isInstitutionalRessourceManagerFor(identity, roles, re)) return true;
		// allow for authors if access granted at least for authors
		if (roles.isAuthor() && re.getAccess() >= RepositoryEntry.ACC_OWNERS_AUTHORS) return true;
		// allow for guests if access granted for guests
		if (roles.isGuestOnly()) {
			if (re.getAccess() >= RepositoryEntry.ACC_USERS_GUESTS) return true;
			else return false;
		}
		// else allow if access granted for users
		if(re.getAccess() >= RepositoryEntry.ACC_USERS) {
			return true;
		} else if (re.getAccess() == RepositoryEntry.ACC_OWNERS && re.isMembersOnly()) {
			return repositoryEntryRelationDao.isMember(identity, re);
		}

		return false;
	}

	public RepositoryEntrySecurity isAllowed(Identity identity, Roles roles, RepositoryEntry re) {
		boolean isOwner = false;
		boolean isCourseCoach = false;
		boolean isGroupCoach = false;
		boolean isCourseParticipant = false;
		boolean isGroupParticipant = false;
		boolean isGroupWaiting = false;

		boolean isEntryAdmin = false;
		boolean canLaunch = false;

		if (roles.isGuestOnly()) {
			if (re.getAccess() >= RepositoryEntry.ACC_USERS_GUESTS) {
				// allow for guests if access granted for guests
				canLaunch = true;
			}
		} else {
			// allow if identity is owner
			List<Object[]> roleAndDefs = repositoryEntryRelationDao.getRoleAndDefaults(identity, re);
			for(Object[] roleAndDef:roleAndDefs) {
				String role = (String)roleAndDef[0];
				Boolean def = (Boolean)roleAndDef[1];
				if(GroupRoles.isValueOf(role)) {
					switch(GroupRoles.valueOf(role)) {
						case owner: {
							isOwner = true;
							break;
						}
						case coach: {
							boolean d = (def == null ? false : def.booleanValue());
							if(d) {
								isCourseCoach = true;
							} else {
								isGroupCoach = true;
							}
							break;
						}
						case participant: {
							boolean d = (def == null ? false : def.booleanValue());
							if(d) {
								isCourseParticipant = true;
							} else {
								isGroupParticipant = true;
							}
							break;
						}
						case waiting: {
							isGroupWaiting = true;
							break;
						}
						case invitee: break;
	
					}
				}
			}

			if(isOwner) {
				canLaunch = true;
				isEntryAdmin = true;
			}
			// allow if access limit matches identity's role
			// allow for olat administrators
			else if (roles.isOLATAdmin()) {
				canLaunch = true;
				isEntryAdmin = true;
			}
			// allow for institutional resource manager
			else if (isInstitutionalRessourceManagerFor(identity, roles, re)) {
				canLaunch = true;
				isEntryAdmin = true;
			}
			if (roles.isAuthor() && re.getAccess() >= RepositoryEntry.ACC_OWNERS_AUTHORS) {
				// allow for authors if access granted at least for authors
				canLaunch = true;
			} else if(re.getAccess() >= RepositoryEntry.ACC_USERS) {
				// allow if access granted for users
				canLaunch = true;
			} else if (re.getAccess() == RepositoryEntry.ACC_OWNERS && re.isMembersOnly()) {
				if(!canLaunch) {
					//is member?
					if(isGroupParticipant || isGroupCoach ||
							isCourseParticipant || isCourseCoach) {
						canLaunch = true;
					}
				}
			}
		}

		boolean readOnly = new RepositoryEntryStatus(re.getStatusCode()).isClosed();

		return new RepositoryEntrySecurity(isEntryAdmin, isOwner,
				isCourseParticipant, isCourseCoach,
				isGroupParticipant, isGroupCoach,
				isGroupWaiting, canLaunch, readOnly);
	}

	public RepositoryEntry setAccess(final RepositoryEntry re, int access, boolean membersOnly) {
		RepositoryEntry reloadedRe = repositoryEntryDao.loadForUpdate(re);
		if(reloadedRe == null) {
			return null;
		}
		reloadedRe.setAccess(access);
		reloadedRe.setMembersOnly(membersOnly);
		reloadedRe.setLastModified(new Date());
		RepositoryEntry updatedRe = dbInstance.getCurrentEntityManager().merge(reloadedRe);
		dbInstance.commit();
		lifeIndexer.indexDocument(RepositoryEntryDocument.TYPE, updatedRe.getKey());
		return updatedRe;
	}

	public RepositoryEntry setAccessAndProperties(final RepositoryEntry re,
			int access, boolean membersOnly,
			boolean canCopy, boolean canReference, boolean canDownload) {
		RepositoryEntry reloadedRe = repositoryEntryDao.loadForUpdate(re);
		if(reloadedRe == null) {
			return null;
		}
		//access
		reloadedRe.setAccess(access);
		reloadedRe.setMembersOnly(membersOnly);
		reloadedRe.setLastModified(new Date());
		//properties
		reloadedRe.setCanCopy(canCopy);
		reloadedRe.setCanReference(canReference);
		reloadedRe.setCanDownload(canDownload);
		RepositoryEntry updatedRe = dbInstance.getCurrentEntityManager().merge(reloadedRe);

		//fetch the values
		updatedRe.getStatistics().getLaunchCounter();
		if(updatedRe.getLifecycle() != null) {
			updatedRe.getLifecycle().getCreationDate();
		}

		dbInstance.commit();
		return updatedRe;
	}

	public RepositoryEntry setLeaveSetting(final RepositoryEntry re,
			RepositoryEntryAllowToLeaveOptions setting) {
		RepositoryEntry reloadedRe = repositoryEntryDao.loadForUpdate(re);
		if(reloadedRe == null) {
			return null;
		}
		reloadedRe.setAllowToLeaveOption(setting);
		RepositoryEntry updatedRe = dbInstance.getCurrentEntityManager().merge(reloadedRe);
		updatedRe.getStatistics().getLaunchCounter();
		if(updatedRe.getLifecycle() != null) {
			updatedRe.getLifecycle().getCreationDate();
		}
		dbInstance.commit();
		return updatedRe;
	}

	/**
	 * This method doesn't update empty and null values! ( Reserved to unit tests
	 * and REST API)
	 * @param re
	 * @param displayName
	 * @param description
	 * @param externalId
	 * @param externalRef
	 * @param managedFlags
	 * @param cycle
	 * @return
	 */
	public RepositoryEntry setDescriptionAndName(final RepositoryEntry re, String displayName, String description,
			String location, String authors, String externalId, String externalRef, String managedFlags,
			RepositoryEntryLifecycle cycle) {
		RepositoryEntry reloadedRe = repositoryEntryDao.loadForUpdate(re);
		if(reloadedRe == null) {
			return null;
		}

		if(StringHelper.containsNonWhitespace(displayName)) {
			reloadedRe.setDisplayname(displayName);
		}
		if(StringHelper.containsNonWhitespace(description)) {
			reloadedRe.setDescription(description);
		}
		if(StringHelper.containsNonWhitespace(authors)) {
			reloadedRe.setAuthors(authors);
		}
		if(StringHelper.containsNonWhitespace(location)) {
			reloadedRe.setLocation(location);
		}
		if(StringHelper.containsNonWhitespace(externalId)) {
			reloadedRe.setExternalId(externalId);
		}
		if(StringHelper.containsNonWhitespace(externalRef)) {
			reloadedRe.setExternalRef(externalRef);
		}
		if(StringHelper.containsNonWhitespace(managedFlags)) {
			reloadedRe.setManagedFlagsString(managedFlags);
			if(RepositoryEntryManagedFlag.isManaged(reloadedRe, RepositoryEntryManagedFlag.membersmanagement)) {
				reloadedRe.setAllowToLeaveOption(RepositoryEntryAllowToLeaveOptions.never);
			}
		}

		RepositoryEntryLifecycle cycleToDelete = null;
		RepositoryEntryLifecycle currentCycle = reloadedRe.getLifecycle();
		if(currentCycle != null) {
			// currently, it's a private cycle
			if(currentCycle.isPrivateCycle()) {
				//the new one is none or public, remove the private cycle
				if(cycle == null || !cycle.isPrivateCycle()) {
					cycleToDelete = currentCycle;
				}
			}
		}
		reloadedRe.setLifecycle(cycle);
		reloadedRe.setLastModified(new Date());
		RepositoryEntry updatedRe = dbInstance.getCurrentEntityManager().merge(reloadedRe);
		if(cycleToDelete != null) {
			dbInstance.getCurrentEntityManager().remove(cycleToDelete);
		}

		dbInstance.commit();
		lifeIndexer.indexDocument(RepositoryEntryDocument.TYPE, updatedRe.getKey());
		autoAccessManager.grantAccess(updatedRe);
		return updatedRe;
	}

	/**
	 * The method updates empty and null values!
	 * @param re
	 * @param displayName
	 * @param externalRef
	 * @param authors
	 * @param description
	 * @param objectives
	 * @param requirements
	 * @param credits
	 * @param mainLanguage
	 * @param expenditureOfWork
	 * @param cycle
	 * @return
	 */
	public RepositoryEntry setDescriptionAndName(final RepositoryEntry re,
			String displayName, String externalRef, String authors, String description,
			String objectives, String requirements, String credits, String mainLanguage,
			String location, String expenditureOfWork, RepositoryEntryLifecycle cycle) {
		RepositoryEntry reloadedRe = repositoryEntryDao.loadForUpdate(re);
		if(reloadedRe == null) {
			return null;
		}
		reloadedRe.setDisplayname(displayName);
		reloadedRe.setAuthors(authors);
		reloadedRe.setDescription(description);
		reloadedRe.setExternalRef(externalRef);
		reloadedRe.setObjectives(objectives);
		reloadedRe.setRequirements(requirements);
		reloadedRe.setCredits(credits);
		reloadedRe.setMainLanguage(mainLanguage);
		reloadedRe.setExpenditureOfWork(expenditureOfWork);
		reloadedRe.setLocation(location);

		RepositoryEntryLifecycle cycleToDelete = null;
		RepositoryEntryLifecycle currentCycle = reloadedRe.getLifecycle();
		if(currentCycle != null) {
			// currently, it's a private cycle
			if(currentCycle.isPrivateCycle()) {
				//the new one is none or public, remove the private cycle
				if(cycle == null || !cycle.isPrivateCycle()) {
					cycleToDelete = currentCycle;
				}
			}
		}
		reloadedRe.setLifecycle(cycle);
		reloadedRe.setLastModified(new Date());
		RepositoryEntry updatedRe = dbInstance.getCurrentEntityManager().merge(reloadedRe);
		if(cycleToDelete != null) {
			dbInstance.getCurrentEntityManager().remove(cycleToDelete);
		}

		//fetch the values
		updatedRe.getStatistics().getLaunchCounter();
		if(updatedRe.getLifecycle() != null) {
			updatedRe.getLifecycle().getCreationDate();
		}

		dbInstance.commit();
		lifeIndexer.indexDocument(RepositoryEntryDocument.TYPE, updatedRe.getKey());
		autoAccessManager.grantAccess(updatedRe);
		return updatedRe;
	}

	public void triggerIndexer(RepositoryEntryRef re) {
		lifeIndexer.indexDocument(RepositoryEntryDocument.TYPE, re.getKey());
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
		DBQuery dbquery = dbInstance.createQuery(query.toString());
		dbquery.setString("restrictedType", restrictedType);
		dbquery.setInteger("restrictedAccess", restrictedAccess);
		dbquery.setCacheable(true);
		return ((Long)dbquery.list().get(0)).intValue();
	}

	public long countPublished(String restrictedType) {
		StringBuilder query = new StringBuilder(400);
		query.append("select count(*) from org.olat.repository.RepositoryEntry v")
		     .append(" inner join v.olatResource res")
		     .append(" where res.resName=:restrictedType ")
		     .append(" and ((v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true) or v.access>=").append(RepositoryEntry.ACC_USERS).append(")");

		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Number.class)
				.setParameter("restrictedType", restrictedType)
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? null : count.get(0).longValue();
	}

	/**
	 * Query by type, limit by ownership or role accessability.
	 * @param identity
	 * @param restrictedType The type cannot be empty, no type, no return
	 * @param roles
	 * @return
	 */
	public List<RepositoryEntry> queryByTypeLimitAccess(Identity identity, List<String> restrictedType, Roles roles) {
		if(restrictedType == null | restrictedType.isEmpty()) return Collections.emptyList();
		if(roles.isOLATAdmin()) {
			identity = null;//not need for the query as administrator
		}

		StringBuilder sb = new StringBuilder(400);
		sb.append("select distinct v from ").append(RepositoryEntry.class.getName()).append(" v ")
		  .append(" inner join fetch v.olatResource as res")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where res.resName in (:restrictedType) and ");

		boolean setIdentity = false;
		if (roles.isOLATAdmin()) {
			sb.append("v.access>=").append(RepositoryEntry.ACC_OWNERS); // treat admin special b/c admin is author as well
		} else {
			setIdentity = appendAccessSubSelects(sb, identity, roles);
		}

		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("restrictedType", restrictedType);
		if(setIdentity) {
			query.setParameter("identityKey", identity.getKey());
		}
		return query.getResultList();
	}

	/**
	 * Query by type, limit by ownership or role accessability and institution.
	 * @param identity
	 * @param roles
	 * @param restrictedType The types cannot be empty, no type, nothing to return
	 * @return
	 */
	public List<RepositoryEntry> queryByTypeLimitAccess(Identity identity, Roles roles, List<String> restrictedType) {
		if(restrictedType == null | restrictedType.isEmpty()) return Collections.emptyList();

		String institution = identity.getUser().getProperty(UserConstants.INSTITUTIONALNAME, null);
		List<RepositoryEntry> results = new ArrayList<RepositoryEntry>();
		if(!roles.isOLATAdmin() && institution != null && institution.length() > 0 && roles.isInstitutionalResourceManager()) {
			StringBuilder sb = new StringBuilder(400);
			sb.append("select distinct v from ").append(RepositoryEntry.class.getName()).append(" v")
			  .append(" inner join fetch v.olatResource as res")
			  .append(" inner join fetch v.statistics as statistics")
			  .append(" left join fetch v.lifecycle as lifecycle")
			  .append(" inner join v.groups as relGroup")
			  .append(" inner join relGroup.group as baseGroup")
			  .append(" inner join baseGroup.members as membership")
			  .append(" inner join membership.identity as identity")
			  .append(" inner join identity.user as user")
			  .append(" where user.institutionalName=:institutionCourseManager")
			  .append(" and res.resName in (:restrictedType) and v.access = 1");

			List<RepositoryEntry> institutionalResults = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), RepositoryEntry.class)
					.setParameter("restrictedType", restrictedType)
					.setParameter("institutionCourseManager", institution)
					.getResultList();
			results.addAll(institutionalResults);
		}

		long start = System.currentTimeMillis();
		List<RepositoryEntry> genericResults = queryByTypeLimitAccess(identity, restrictedType, roles);
		long timeQuery3 = System.currentTimeMillis() - start;
		log.info("Repo-Perf: queryByTypeLimitAccess#3 takes " + timeQuery3);

		if(results.isEmpty()) {
			results.addAll(genericResults);
		} else {
			for(RepositoryEntry genericResult:genericResults) {
				if(!PersistenceHelper.listContainsObjectByKey(results, genericResult)) {
					results.add(genericResult);
				}
			}
		}
		return results;
	}

	/**
	 * Query by ownership, optionally limit by type.
	 *
	 * @param identity
	 * @param limitType
	 * @return Results
	 */
	public List<RepositoryEntry> queryByOwner(IdentityRef identity, String... limitTypes) {
		if (identity == null) throw new AssertException("identity can not be null!");
		StringBuilder sb = new StringBuilder(400);
		sb.append("select v from repositoryentry v ")
		  .append(" inner join fetch v.olatResource as res ")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as relGroup on relGroup.defaultGroup=true")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership on membership.role='").append(GroupRoles.owner.name()).append("'")
		  .append(" where v.access>0 and membership.identity.key=:identityKey");
		if (limitTypes != null && limitTypes.length > 0) {
			sb.append(" and res.resName in (:types)");
		}

		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey());
		if(limitTypes != null && limitTypes.length > 0) {
			List<String> types = new ArrayList<String>();
			for(String type:limitTypes) {
				types.add(type);
			}
			query.setParameter("types", types);
		}
		return query.getResultList();
	}

	/**
	 * Return the entries visible by the specified roles as member. The query
	 * take the business groups in account.
	 *
	 * @param identity
	 * @param owner
	 * @param coach
	 * @param participant
	 * @param limitTypes
	 * @return The entries or an empty list if no role is specified
	 */
	public List<RepositoryEntry> queryByMembership(IdentityRef identity, boolean owner, boolean coach, boolean participant, String... limitTypes) {
		if (identity == null) throw new AssertException("identity can not be null!");
		if (!owner && !coach && !participant) return Collections.emptyList();

		StringBuilder sb = new StringBuilder(512);
		sb.append("select v from repositoryentry v ")
		  .append(" inner join fetch v.olatResource as res ")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey and (");
		if(owner) {
			sb.append(" (v.access >= ").append(RepositoryEntry.ACC_OWNERS).append(" and membership.role='").append(GroupRoles.owner.name()).append("')");
		}
		if(coach || participant) {
			if(owner) {
				sb.append(" or");
			}
			sb.append(" ((v.access >= ").append(RepositoryEntry.ACC_USERS).append(" or ")
			  .append("  (v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true))")
			  .append("   and membership.role");
			if(coach && participant) {
				sb.append(" in ('").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')");
			} else {
				sb.append(" ='").append(coach ? GroupRoles.coach.name() : GroupRoles.participant.name()).append("'");
			}
			sb.append(")");
		}
		sb.append(")");

		if (limitTypes != null && limitTypes.length > 0) {
			sb.append(" and res.resName in (:types)");
		}

		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey());
		if(limitTypes != null && limitTypes.length > 0) {
			List<String> types = new ArrayList<String>();
			for(String type:limitTypes) {
				types.add(type);
			}
			query.setParameter("types", types);
		}
		return query.getResultList();
	}

	/**
	 * Query by initial-author
	 * @param restrictedType
	 * @param roles
	 * @return Results
	 */
	public List<RepositoryEntry> queryByInitialAuthor(String initialAuthor) {
		String query = "select v from org.olat.repository.RepositoryEntry v where v.initialAuthor= :initialAuthor";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, RepositoryEntry.class)
				.setParameter("initialAuthor", initialAuthor)
				.getResultList();
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
	public List<RepositoryEntry> queryReferencableResourcesLimitType(Identity identity, Roles roles, List<String> resourceTypes,
			String displayName, String author, String desc) {
		if (identity == null) {
			throw new AssertException("identity can not be null!");
		}
		if (!roles.isAuthor()) {
			// if user has no author right he can not reference to any resource at all
			return new ArrayList<RepositoryEntry>();
		}
		return queryResourcesLimitType(identity, resourceTypes, displayName, author, desc, true, false);
	}

	/**
	 * Search for resources that can be copied by an author. This is the case:
	 * 1) the user is the owner of the resource
	 * 2) the user is author and the resource is at least visible to authors (BA)
	 *    and the resource is set to canCopy
	 * @param identity The user initiating the query
	 * @param roles The current users role set
	 * @param resourceTypes Limit search result to this list of repo types. Can be NULL
	 * @param displayName Limit search to this repo title. Can be NULL
	 * @param author Limit search to this user (Name, firstname, loginname). Can be NULL
	 * @param desc Limit search to description. Can be NULL
	 * @return List of repository entries
	 */
	public List<RepositoryEntry> queryCopyableResourcesLimitType(Identity identity, Roles roles, List<String> resourceTypes,
			String displayName, String author, String desc) {
		if (identity == null) {
			throw new AssertException("identity can not be null!");
		}
		if (!roles.isAuthor()) {
			// if user has no author right he can not reference to any resource at all
			return new ArrayList<RepositoryEntry>();
		}
		return queryResourcesLimitType(identity, resourceTypes, displayName, author, desc, false, true);
	}

	public List<RepositoryEntry> queryResourcesLimitType(Identity identity, List<String> resourceTypes,
			String displayName, String author, String desc, boolean checkCanReference, boolean checkCanCopy) {

		// cleanup some data: use null values if emtpy
		if (resourceTypes != null && resourceTypes.size() == 0) resourceTypes = null;
		if ( ! StringHelper.containsNonWhitespace(displayName)) displayName = null;
		if ( ! StringHelper.containsNonWhitespace(author)) author = null;
		if ( ! StringHelper.containsNonWhitespace(desc)) desc = null;

		// Build the query
		// 1) Joining tables
		StringBuilder query = new StringBuilder(400);
		query.append("select distinct v from ").append(RepositoryEntry.class.getName()).append(" v ")
		     .append(" inner join fetch v.olatResource as res" )
			 .append(" inner join fetch v.statistics as statistics")
		     .append(" left join fetch v.lifecycle as lifecycle");
		// 2) where clause
		query.append(" where ");
		// restrict on ownership or referencability flag

		int access;
		if(identity != null) {
			access = RepositoryEntry.ACC_OWNERS_AUTHORS;
			query.append(" (v.access>=:access");
			if(checkCanReference) {
				query.append(" and v.canReference=true");
			}
			if(checkCanCopy) {
				query.append(" and v.canCopy=true");
			}
			query.append("  or (v.access>0 and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership  ")
			     .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup and membership.identity.key=:identityKey ")
			     .append("      and membership.role in ('").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')")
			     .append("  ))")
			     .append(" )");
		} else {
			access = RepositoryEntry.ACC_OWNERS;
			query.append(" v.access>=:access ");
		}

		// restrict on type
		if (resourceTypes != null) {
			query.append(" and res.resName in (:resourcetypes)");
		}
		// restrict on author
		if (author != null) { // fuzzy author search
			author = author.replace('*','%');
			author = '%' + author + '%';
			query.append(" and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership, ")
			     .append(IdentityImpl.class.getName()).append(" as identity, ").append(UserImpl.class.getName()).append(" as user")
		         .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup and membership.identity=identity and user.identity.key=identity.key")
		         .append("      and membership.role='").append(GroupRoles.owner.name()).append("'")
		         .append("      and (user.firstName like :author or user.lastName like :author or identity.name like :author)")
		         .append("  )");
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
		TypedQuery<RepositoryEntry> dbquery = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), RepositoryEntry.class);
		if(identity != null) {
			dbquery.setParameter("identityKey", identity.getKey());
		}
		dbquery.setParameter("access", access);
		if (author != null) {
			dbquery.setParameter("author", author);
		}
		if (displayName != null) {
			dbquery.setParameter("displayname", displayName);
		}
		if (desc != null) {
			dbquery.setParameter("desc", desc);
		}
		if (resourceTypes != null) {
			dbquery.setParameter("resourcetypes", resourceTypes);
		}
		return dbquery.getResultList();
	}


	/**
	 * Query by ownership, limit by access.
	 *
	 * @param identity
	 * @param limitAccess
	 * @return Results
	 */
	public List<RepositoryEntry> queryByOwnerLimitAccess(Identity identity, int limitAccess, Boolean membersOnly) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" v ")
		  .append(" inner join fetch v.olatResource as res ")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as relGroup on relGroup.defaultGroup=true")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership on membership.role='").append(GroupRoles.owner.name()).append("'")
		  .append(" where membership.identity.key=:identityKey and (v.access>=:limitAccess");
		if(limitAccess != RepositoryEntry.ACC_OWNERS && membersOnly != null && membersOnly.booleanValue()) {
			sb.append(" or (v.access=1 and v.membersOnly=true)");
		}
		sb.append(")");

		List<RepositoryEntry> entries = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("limitAccess", limitAccess)
				.getResultList();
		return entries;
	}

	/**
	 * check ownership of identity for a resource
	 * @return true if the identity is member of the security group of the repository entry
	 */
	public boolean isOwnerOfRepositoryEntry(IdentityRef identity, RepositoryEntryRef entry) {
		if(entry == null || identity == null) {
			return false;
		}
		return repositoryEntryRelationDao.hasRole(identity, entry, GroupRoles.owner.name());
	}

	/**
	 * This query need the repository entry as v, v.olatResource as res
	 * and v.baseGroup as baseGroup
	 * @param sb
	 * @param identity
	 * @param roles
	 * @return
	 */
	public static boolean appendAccessSubSelects(StringBuilder sb, IdentityRef identity, Roles roles) {
		sb.append("(v.access >= ");
		if (roles.isAuthor()) {
			sb.append(RepositoryEntry.ACC_OWNERS_AUTHORS);
		} else if (roles.isGuestOnly()) {
			sb.append(RepositoryEntry.ACC_USERS_GUESTS);
		} else {
			sb.append(RepositoryEntry.ACC_USERS);
		}

		//+ membership
		boolean setIdentity = false;
		if(!roles.isGuestOnly() && identity != null) {
			setIdentity = true;
			//sub select are very quick
			sb.append(" or (")
			  .append("  v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true")
			  .append("  and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership  ")
			  .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup and membership.identity.key=:identityKey ")
			  .append("      and membership.role in ('").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')")
			  .append("  )")
			  .append(" )");
		}
		sb.append(")");
		return setIdentity;
	}

	public int countGenericANDQueryWithRolesRestriction(SearchRepositoryEntryParameters params) {
		TypedQuery<Number> dbQuery = createGenericANDQueryWithRolesRestriction(params, false, Number.class);
		Number count = dbQuery.getSingleResult();
		return count.intValue();
	}

	public List<RepositoryEntry> genericANDQueryWithRolesRestriction(SearchRepositoryEntryParameters params, int firstResult, int maxResults, boolean orderBy) {

		TypedQuery<RepositoryEntry> dbQuery = createGenericANDQueryWithRolesRestriction(params, orderBy, RepositoryEntry.class);
		dbQuery.setFirstResult(firstResult);
		if(maxResults > 0) {
			dbQuery.setMaxResults(maxResults);
		}
		List<RepositoryEntry> res = dbQuery.getResultList();
		return res;
	}

	private <T> TypedQuery<T> createGenericANDQueryWithRolesRestriction(SearchRepositoryEntryParameters params, boolean orderBy, Class<T> type) {
		String displayName = params.getDisplayName();
		String author = params.getAuthor();
		String desc = params.getDesc();
		final List<String> resourceTypes = params.getResourceTypes();
		final Identity identity = params.getIdentity();
		final Roles roles = params.getRoles();
		final String institution = params.getInstitution();

		boolean institut = (!roles.isOLATAdmin() && institution != null && institution.length() > 0 && roles.isInstitutionalResourceManager());
		boolean var_author = StringHelper.containsNonWhitespace(author);
		boolean var_displayname = StringHelper.containsNonWhitespace(displayName);
		boolean var_desc = StringHelper.containsNonWhitespace(desc);
		boolean var_resourcetypes = (resourceTypes != null && resourceTypes.size() > 0);
		boolean count = Number.class.equals(type);

		StringBuilder query = new StringBuilder();
		if(count) {
			query.append("select count(v.key) from ").append(RepositoryEntry.class.getName()).append(" v ");
			query.append(" inner join v.olatResource as res");
		} else {
			if(params.getParentEntry() != null) {
				query.append("select v from ").append(CatalogEntry.class.getName()).append(" cei ")
				     .append(" inner join cei.parent parentCei")
				     .append(" inner join cei.repositoryEntry v")
				     .append(" inner join fetch v.olatResource as res")
				     .append(" inner join fetch v.statistics as statistics")
				     .append(" left join fetch v.lifecycle as lifecycle");
			} else {
				query.append("select distinct v from ").append(RepositoryEntry.class.getName()).append(" v ")
				     .append(" inner join fetch v.olatResource as res")
				     .append(" inner join fetch v.statistics as statistics")
				     .append(" left join fetch v.lifecycle as lifecycle");
			}
		}

		boolean setIdentity = false;

		//access rules
		if(roles.isOLATAdmin()) {
			query.append(" where v.access!=0 ");
		} else if(institut) {
			query.append(" where (v.access >=");
			if (roles.isAuthor()) {
				query.append(RepositoryEntry.ACC_OWNERS_AUTHORS);
			} else if (roles.isGuestOnly()) {
				query.append(RepositoryEntry.ACC_USERS_GUESTS);
			} else{
				query.append(RepositoryEntry.ACC_USERS);
			}
			query.append(" or (");
			query.append("v.access=1 and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership, ")
			     .append(IdentityImpl.class.getName()).append(" as identity, ").append(UserImpl.class.getName()).append(" as user")
	             .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup and membership.identity=identity and user.identity.key=identity.key")
	             .append("      and user.institutionalName=:institution and membership.role='").append(GroupRoles.owner.name()).append("'")
	             .append(")))");

		} else if (params.isOnlyOwnedResources()) {
			query.append(" where (v.access>0 and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership  ")
		         .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup")
		         .append("      and membership.identity.key=:identityKey and membership.role='").append(GroupRoles.owner.name()).append("'")
		         .append("  ))");
			setIdentity = true;
		} else if (params.isOnlyExplicitMember()) {
			query.append(" where  (v.access>=").append(RepositoryEntry.ACC_USERS)
			     .append(" or (")
			     .append("  v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true")
			     .append("  and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership  ")
			     .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup and membership.identity.key=:identityKey ")
			     .append("      and membership.role in ('").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')")
			     .append("  )")
			     .append(" ))");

			setIdentity = true;
		} else {
			query.append(" where ");
			setIdentity = appendAccessSubSelects(query, identity, roles);
		}

		if(params.getParentEntry() != null) {
			query.append(" and parentCei.key=:parentCeiKey");
		}

		if (var_author) { // fuzzy author search
			author = PersistenceHelper.makeFuzzyQueryString(author);

			query.append(" and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership, ")
			     .append(IdentityImpl.class.getName()).append(" as identity, ").append(UserImpl.class.getName()).append(" as user")
		         .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup and membership.identity.key=identity.key and user.identity.key=identity.key")
		         .append("      and membership.role='").append(GroupRoles.owner.name()).append("'")
		         .append("      and (");
			PersistenceHelper.appendFuzzyLike(query, "user.firstName", "author", dbInstance.getDbVendor());
			query.append(" or ");
			PersistenceHelper.appendFuzzyLike(query, "user.lastName", "author", dbInstance.getDbVendor());
			query.append(" or ");
			PersistenceHelper.appendFuzzyLike(query, "identity.name", "author", dbInstance.getDbVendor());
			query.append(" ))");
		}

		if (var_displayname) {
			//displayName = '%' + displayName.replace('*', '%') + '%';
			//query.append(" and v.displayname like :displayname");
			displayName = PersistenceHelper.makeFuzzyQueryString(displayName);
			query.append(" and ");
			PersistenceHelper.appendFuzzyLike(query, "v.displayname", "displayname", dbInstance.getDbVendor());
		}

		if (var_desc) {
			//desc = '%' + desc.replace('*', '%') + '%';
			//query.append(" and v.description like :desc");
			desc = PersistenceHelper.makeFuzzyQueryString(desc);
			query.append(" and ");
			PersistenceHelper.appendFuzzyLike(query, "v.description", "desc", dbInstance.getDbVendor());
		}

		if (var_resourcetypes) {
			query.append(" and res.resName in (:resourcetypes)");
		}

		if(params.getRepositoryEntryKeys() != null && !params.getRepositoryEntryKeys().isEmpty()) {
			query.append(" and v.key in (:entryKeys)");
		}

		if(params.getManaged() != null) {
			if(params.getManaged().booleanValue()) {
				query.append(" and v.managedFlagsString is not null");
			} else {
				query.append(" and v.managedFlagsString is null");
			}
		}

		if(StringHelper.containsNonWhitespace(params.getExternalId())) {
			query.append(" and v.externalId=:externalId");
		}

		if(StringHelper.containsNonWhitespace(params.getExternalRef())) {
			query.append(" and v.externalRef=:externalRef");
		}

		if(params.getMarked() != null) {
			setIdentity = true;
			query.append(" and v.key ").append(params.getMarked().booleanValue() ? "" : "not").append(" in (")
           .append("   select mark.resId from ").append(MarkImpl.class.getName()).append(" mark ")
           .append("     where mark.resName='RepositoryEntry' and mark.creator.key=:identityKey")
			     .append(" )");
		}

		if(!count && orderBy) {
			query.append(" order by v.displayname, v.key ASC");
		}

		TypedQuery<T> dbQuery = dbInstance.getCurrentEntityManager().createQuery(query.toString(), type);
		if(institut) {
			dbQuery.setParameter("institution", institution);
		}
		if(params.getParentEntry() != null) {
			dbQuery.setParameter("parentCeiKey", params.getParentEntry().getKey());
		}
		if (var_author) {
			dbQuery.setParameter("author", author);
		}
		if (var_displayname) {
			dbQuery.setParameter("displayname", displayName);
		}
		if (var_desc) {
			dbQuery.setParameter("desc", desc);
		}
		if (var_resourcetypes) {
			dbQuery.setParameter("resourcetypes", resourceTypes);
		}
		if(params.getRepositoryEntryKeys() != null && !params.getRepositoryEntryKeys().isEmpty()) {
			dbQuery.setParameter("entryKeys", params.getRepositoryEntryKeys());
		}
		if(StringHelper.containsNonWhitespace(params.getExternalId())) {
			dbQuery.setParameter("externalId", params.getExternalId());
		}
		if(StringHelper.containsNonWhitespace(params.getExternalRef())) {
			dbQuery.setParameter("externalRef", params.getExternalRef());
		}

		if(setIdentity) {
			dbQuery.setParameter("identityKey", identity.getKey());
		}
		return dbQuery;
	}

	/**
	 * Leave the course, commit to the database and send events
	 *
	 * @param identity
	 * @param re
	 * @param status
	 * @param mailing
	 */
	public void leave(Identity identity, RepositoryEntry re, LeavingStatusList status, MailPackage mailing) {
		if(RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.membersmanagement)) {
			status.setWarningManagedCourse(true);
		} else {
			List<RepositoryEntryMembershipModifiedEvent> deferredEvents = new ArrayList<>();
			removeParticipant(identity, identity, re, mailing, true);
			deferredEvents.add(RepositoryEntryMembershipModifiedEvent.removed(identity, re));
			dbInstance.commit();
			sendDeferredEvents(deferredEvents, re);
		}
	}

	/**
	 * add provided list of identities as owners to the repo entry. silently ignore
	 * if some identities were already owners before.
	 * @param ureqIdentity
	 * @param addIdentities
	 * @param re
	 * @param userActivityLogger
	 */
	public void addOwners(Identity ureqIdentity, IdentitiesAddEvent iae, RepositoryEntry re, MailPackage mailing) {
		List<Identity> addIdentities = iae.getAddIdentities();
		List<Identity> reallyAddedId = new ArrayList<Identity>();
		for (Identity identity : addIdentities) {
			if (!repositoryEntryRelationDao.hasRole(identity, re, GroupRoles.owner.name())) {
				repositoryEntryRelationDao.addRole(identity, re, GroupRoles.owner.name());
				reallyAddedId.add(identity);
				ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
				ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
				try{
					ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_ADDED, getClass(),
							LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry), LoggingResourceable.wrap(identity));
				} finally {
					ThreadLocalUserActivityLogger.setStickyActionType(actionType);
				}

				RepositoryMailing.sendEmail(ureqIdentity, identity, re, RepositoryMailing.Type.addOwner, mailing);
				log.audit("Identity(.key):" + ureqIdentity.getKey() + " added identity '" + identity.getKey()
						+ "' to repoentry with key " + re.getKey());
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
	public void removeOwners(Identity ureqIdentity, List<Identity> removeIdentities, RepositoryEntry re, MailPackage mailing) {
		List<RepositoryEntryMembershipModifiedEvent> deferredEvents = new ArrayList<>();

		for (Identity identity : removeIdentities) {
			removeOwner(ureqIdentity, identity, re, mailing);
			deferredEvents.add(RepositoryEntryMembershipModifiedEvent.removed(identity, re));
		}

		dbInstance.commit();
		sendDeferredEvents(deferredEvents, re);
	}

	private void sendDeferredEvents(List<? extends MultiUserEvent> events, OLATResourceable ores) {
		EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		for(MultiUserEvent event:events) {
			eventBus.fireEventToListenersOf(event, ores);
			eventBus.fireEventToListenersOf(event, OresHelper.lookupType(RepositoryEntry.class));
		}
	}

	private void removeOwner(Identity ureqIdentity, Identity identity, RepositoryEntry re, MailPackage mailing) {
		repositoryEntryRelationDao.removeRole(identity, re, GroupRoles.owner.name());

		RepositoryMailing.sendEmail(ureqIdentity, identity, re, RepositoryMailing.Type.removeTutor, mailing);

		ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
		ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
		try{
			ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_REMOVED, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry), LoggingResourceable.wrap(identity));
		} finally {
			ThreadLocalUserActivityLogger.setStickyActionType(actionType);
		}
		log.audit("Identity(.key):" + ureqIdentity.getKey() + " removed identity '" + identity.getKey()
				+ "' from repositoryentry with key " + re.getKey());
	}

	public void acceptPendingParticipation(Identity ureqIdentity, Identity identityToAdd, OLATResource resource, ResourceReservation reservation) {
		RepositoryEntry re = lookupRepositoryEntry(resource, false);
		if(re != null) {
			if("repo_participant".equals(reservation.getType())) {
				IdentitiesAddEvent iae = new IdentitiesAddEvent(identityToAdd);
				//roles is not needed as I add myself as participant
				addParticipants(ureqIdentity, null, iae, re, null);
			} else if("repo_tutors".equals(reservation.getType())) {
				IdentitiesAddEvent iae = new IdentitiesAddEvent(identityToAdd);
				//roles is not needed as I add myself as tutor
				addTutors(ureqIdentity, null, iae, re, null);
			}
			reservationDao.deleteReservation(reservation);
		}
	}

	/**
	 * add provided list of identities as tutor to the repo entry. silently ignore
	 * if some identities were already tutor before.
	 * @param ureqIdentity
	 * @param addIdentities
	 * @param re
	 * @param userActivityLogger
	 */
	public void addTutors(Identity ureqIdentity, Roles ureqRoles, IdentitiesAddEvent iae, RepositoryEntry re, MailPackage mailing) {
		List<Identity> addIdentities = iae.getAddIdentities();
		List<Identity> reallyAddedId = new ArrayList<Identity>();
		for (Identity identityToAdd : addIdentities) {
			if (!repositoryEntryRelationDao.hasRole(identityToAdd, re, GroupRoles.coach.name())) {

				boolean mustAccept = true;
				if(ureqIdentity != null && ureqIdentity.equals(identityToAdd)) {
					mustAccept = false;//adding itself, we hope that he knows what he makes
				} else if(ureqRoles == null || ureqIdentity == null) {
					mustAccept = false;//administrative task
				} else {
					mustAccept = repositoryModule.isAcceptMembership(ureqRoles);
				}

				if(mustAccept) {
					ResourceReservation olderReservation = reservationDao.loadReservation(identityToAdd, re.getOlatResource());
					if(olderReservation == null) {
						Calendar cal = Calendar.getInstance();
						cal.add(Calendar.MONTH, 6);
						Date expiration = cal.getTime();
						ResourceReservation reservation =
								reservationDao.createReservation(identityToAdd, "repo_tutors", expiration, re.getOlatResource());
						if(reservation != null) {
							RepositoryMailing.sendEmail(ureqIdentity, identityToAdd, re, RepositoryMailing.Type.addTutor, mailing);
						}
					}
				} else {
					addInternalTutors(ureqIdentity, identityToAdd, re, reallyAddedId);
					RepositoryMailing.sendEmail(ureqIdentity, identityToAdd, re, RepositoryMailing.Type.addTutor, mailing);
				}

			}//else silently ignore already owner identities
		}
		iae.setIdentitiesAddedEvent(reallyAddedId);
	}

	/**
	 * Internal method to add tutors, it makes no check.
	 * @param ureqIdentity
	 * @param identity
	 * @param re
	 * @param reallyAddedId
	 */
	private void addInternalTutors(Identity ureqIdentity, Identity identity, RepositoryEntry re, List<Identity> reallyAddedId) {
		repositoryEntryRelationDao.addRole(identity, re, GroupRoles.coach.name());
		reallyAddedId.add(identity);
		ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
		ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
		try{
			ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_ADDED, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry), LoggingResourceable.wrap(identity));
		} finally {
			ThreadLocalUserActivityLogger.setStickyActionType(actionType);
		}
		log.audit("Identity(.key):" + ureqIdentity.getKey() + " added identity '" + identity.getKey()
				+ "' to repositoryentry with key " + re.getKey());
	}

	/**
	 * remove list of identities as tutor of given repository entry.
	 * @param ureqIdentity
	 * @param removeIdentities
	 * @param re
	 * @param logger
	 */
	public void removeTutors(Identity ureqIdentity, List<Identity> removeIdentities, RepositoryEntry re, MailPackage mailing) {
		List<RepositoryEntryMembershipModifiedEvent> deferredEvents = new ArrayList<>();
		for (Identity identity : removeIdentities) {
			removeTutor(ureqIdentity, identity, re, mailing);
			deferredEvents.add(RepositoryEntryMembershipModifiedEvent.removed(identity, re));
		}
		dbInstance.commit();
		sendDeferredEvents(deferredEvents, re);
	}

	private void removeTutor(Identity ureqIdentity, Identity identity, RepositoryEntry re, MailPackage mailing) {
		repositoryEntryRelationDao.removeRole(identity, re, GroupRoles.coach.name());

		RepositoryMailing.sendEmail(ureqIdentity, identity, re, RepositoryMailing.Type.removeTutor, mailing);
		
		ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
		ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
		try{
			ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_REMOVED, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry), LoggingResourceable.wrap(identity));
		} finally {
			ThreadLocalUserActivityLogger.setStickyActionType(actionType);
		}
		log.audit("Identity(.key):" + ureqIdentity.getKey() + " removed identity '" + identity.getKey()
				+ "' from repositoryentry with key " + re.getKey());
	}

	/**
	 * add provided list of identities as participant to the repo entry. silently ignore
	 * if some identities were already participant before.
	 * @param ureqIdentity
	 * @param addIdentities
	 * @param re
	 * @param userActivityLogger
	 */
	public void addParticipants(Identity ureqIdentity, Roles ureqRoles, IdentitiesAddEvent iae, RepositoryEntry re, MailPackage mailing) {
		List<Identity> addIdentities = iae.getAddIdentities();
		List<Identity> reallyAddedId = new ArrayList<Identity>();
		for (Identity identityToAdd : addIdentities) {
			if (!repositoryEntryRelationDao.hasRole(identityToAdd, re, GroupRoles.participant.name())) {

				boolean mustAccept = true;
				if(ureqIdentity != null && ureqIdentity.equals(identityToAdd)) {
					mustAccept = false;//adding itself, we hope that he knows what he makes
				} else if(ureqRoles == null || ureqIdentity == null) {
					mustAccept = false;//administrative task
				} else {
					mustAccept = repositoryModule.isAcceptMembership(ureqRoles);
				}

				if(mustAccept) {
					ResourceReservation olderReservation = reservationDao.loadReservation(identityToAdd, re.getOlatResource());
					if(olderReservation == null) {
						Calendar cal = Calendar.getInstance();
						cal.add(Calendar.MONTH, 6);
						Date expiration = cal.getTime();
						ResourceReservation reservation =
								reservationDao.createReservation(identityToAdd, "repo_participant", expiration, re.getOlatResource());
						if(reservation != null) {
							RepositoryMailing.sendEmail(ureqIdentity, identityToAdd, re, RepositoryMailing.Type.addParticipant, mailing);
						}
					}
				} else {
					addInternalParticipant(ureqIdentity, identityToAdd, re);
					reallyAddedId.add(identityToAdd);
					RepositoryMailing.sendEmail(ureqIdentity, identityToAdd, re, RepositoryMailing.Type.addParticipant, mailing);
				}
			}
		}
		iae.setIdentitiesAddedEvent(reallyAddedId);
	}

	/**
	 * This is for internal usage only. The method dosn't make any check.
	 * @param ureqIdentity
	 * @param identity
	 * @param re
	 */
	private void addInternalParticipant(Identity ureqIdentity, Identity identity, RepositoryEntry re) {
		repositoryEntryRelationDao.addRole(identity, re, GroupRoles.participant.name());

		ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
		ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
		try{
			ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_ADDED, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry), LoggingResourceable.wrap(identity));
		} finally {
			ThreadLocalUserActivityLogger.setStickyActionType(actionType);
		}
		log.audit("Identity(.key):" + ureqIdentity.getKey() + " added identity '" + identity.getKey()
				+ "' to repositoryentry with key " + re.getKey());
	}

	/**
	 * remove list of identities as participant of given repository entry.
	 * @param ureqIdentity
	 * @param removeIdentities
	 * @param re
	 * @param logger
	 */
	public void removeParticipants(Identity ureqIdentity, List<Identity> removeIdentities, RepositoryEntry re, MailPackage mailing, boolean sendMail) {
		List<RepositoryEntryMembershipModifiedEvent> deferredEvents = new ArrayList<>();
		for (Identity identity : removeIdentities) {
			removeParticipant(ureqIdentity, identity, re, mailing, sendMail);
			deferredEvents.add(RepositoryEntryMembershipModifiedEvent.removed(identity, re));
		}
		dbInstance.commit();
		sendDeferredEvents(deferredEvents, re);
	}

	private void removeParticipant(Identity ureqIdentity, Identity identity, RepositoryEntry re, MailPackage mailing, boolean sendMail) {
		repositoryEntryRelationDao.removeRole(identity, re, GroupRoles.participant.name());

		if(sendMail) {
			RepositoryMailing.sendEmail(ureqIdentity, identity, re, RepositoryMailing.Type.removeParticipant, mailing);
		}

		ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
		ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
		try{
			ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_REMOVED, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry), LoggingResourceable.wrap(identity));
		} finally {
			ThreadLocalUserActivityLogger.setStickyActionType(actionType);
		}
		log.audit("Identity(.key):" + ureqIdentity.getKey() + " removed identity '" + identity.getKey()
				+ "' from repositoryentry with key " + re.getKey());
	}

	/**
	 * Remove the identities as members of the repository and from
	 * all connected business groups.
	 *
	 * @param members
	 * @param re
	 */
	public boolean removeMembers(Identity ureqIdentity, List<Identity> members, RepositoryEntry re, MailPackage mailing) {
		//log the action
		ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
		ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
		for(Identity identity:members) {
			try{
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_MEMBER_REMOVED, getClass(),
						LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry), LoggingResourceable.wrap(identity));
			} finally {
				ThreadLocalUserActivityLogger.setStickyActionType(actionType);
			}
		}

		List<ResourceReservation> reservations = reservationDao.loadReservations(Collections.singletonList(re.getOlatResource()));
		for(ResourceReservation reservation:reservations) {
			if(members.contains(reservation.getIdentity())) {
				reservationDao.deleteReservation(reservation);
			}
		}

		boolean allOk = repositoryEntryRelationDao.removeMembers(re, members);
		if (allOk) {
			int count = 0;
			List<RepositoryEntryMembershipModifiedEvent> deferredEvents = new ArrayList<>();
			for(Identity identity:members) {
				deferredEvents.add(RepositoryEntryMembershipModifiedEvent.removed(identity, re));
				if(++count % 100 == 0) {
					dbInstance.commitAndCloseSession();
				}
			}
			dbInstance.commit();
			sendDeferredEvents(deferredEvents, re);
		}
		if (allOk) {
			// do logging - not optimal but
			StringBuilder sb = new StringBuilder();
			sb.append("Identity(.key):").append(ureqIdentity.getKey()).append("removed multiple identities from security groups. Identities:: " );
			for (Identity member : members) {
				sb.append(member.getKey()).append(", ");
			}
			log.audit(sb.toString());
		}

		for(Identity identity:members) {
			RepositoryMailing.sendEmail(ureqIdentity, identity, re, RepositoryMailing.Type.removeParticipant, mailing);
		}
		return allOk;
	}

	/**
	 * has one owner of repository entry the same institution like the resource manager
	 * @param RepositoryEntry repositoryEntry
	 * @param Identity identity
	 */
	public boolean isInstitutionalRessourceManagerFor(Identity identity, Roles roles, RepositoryEntryRef repositoryEntry) {
		if(repositoryEntry == null) {
			return false;
		}

		String currentUserInstitutionalName = identity.getUser().getProperty(UserConstants.INSTITUTIONALNAME, null);
		if(!StringHelper.containsNonWhitespace(currentUserInstitutionalName)) {
			return false;
		}

		if(!roles.isInstitutionalResourceManager()) {
			return false;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select count(v) from ").append(RepositoryEntry.class.getName()).append(" v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join membership.identity as identity")
		  .append(" inner join identity.user as user")
		  .append(" where v.key=:repoKey and user.institutionalName=:institutionCourseManager");

		Number count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("repoKey", repositoryEntry.getKey())
				.setParameter("institutionCourseManager", currentUserInstitutionalName)
				.getSingleResult();
		return count == null ? false : count.intValue() > 0;
	}

	public int countLearningResourcesAsStudent(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(1200);
		sb.append("select count(v) from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership on membership.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" where (v.access>=3 or (v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true))")
		  .append(" and membership.identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", identity.getKey())
				.getSingleResult().intValue();
	}

	/**
	 * Gets all learning resources where the user is in a learning group as participant.
	 * @param identity
	 * @return list of RepositoryEntries
	 */
	public List<RepositoryEntry> getLearningResourcesAsStudent(Identity identity, String type, int firstResult, int maxResults, RepositoryEntryOrder... orderby) {
		StringBuilder sb = new StringBuilder(1200);
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join fetch v.olatResource as res ")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where (v.access>=3 or (v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true))")
		  .append(" and membership.identity.key=:identityKey and membership.role='").append(GroupRoles.participant.name()).append("'");
		if(StringHelper.containsNonWhitespace(type)) {
			sb.append(" and res.resName=:resourceType");
		}

		appendOrderBy(sb, "v", orderby);

		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey())
				.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		if(StringHelper.containsNonWhitespace(type)) {
			query.setParameter("resourceType", type);
		}
		List<RepositoryEntry> repoEntries = query.getResultList();
		return repoEntries;
	}

	/**
	 * Gets all learning resources where the user is in a learning group as participant.
	 * @param identity
	 * @return list of RepositoryEntries
	 */
	public List<RepositoryEntry> getLearningResourcesAsParticipantAndCoach(Identity identity, String type) {
		StringBuilder sb = new StringBuilder(1200);
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join fetch v.olatResource as res ")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where (v.access>=3 or (v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true))")
		  .append(" and membership.identity.key=:identityKey and membership.role in('").append(GroupRoles.participant.name()).append("','").append(GroupRoles.coach.name()).append("')");
		if(StringHelper.containsNonWhitespace(type)) {
			sb.append(" and res.resName=:resourceType");
		}

		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey());

		if(StringHelper.containsNonWhitespace(type)) {
			query.setParameter("resourceType", type);
		}
		return query.getResultList();
	}

	public List<RepositoryEntry> getLearningResourcesAsBookmark(Identity identity, Roles roles, String type, int firstResult, int maxResults, RepositoryEntryOrder... orderby) {
		if(roles.isGuestOnly()) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder(1200);
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join fetch v.olatResource as res ")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where exists (select mark.key from ").append(MarkImpl.class.getName()).append(" as mark ")
		  .append("   where mark.creator.key=:identityKey and mark.resId=v.key and mark.resName='RepositoryEntry'")
		  .append(" ) ");
		if(StringHelper.containsNonWhitespace(type)) {
			sb.append(" and res.resName=:resourceType");
		}
		sb.append(" and (v.access >= ");
		if (roles.isAuthor()) {
			sb.append(RepositoryEntry.ACC_OWNERS_AUTHORS);
		} else {
			sb.append(RepositoryEntry.ACC_USERS);
		}
		sb.append(" or (")
		  .append("  v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true")
		  .append("  and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership")
		  .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup and membership.identity.key=:identityKey")
		  .append("      and membership.role in ('").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')")
		  .append("  )")
		  .append(" )")
		  .append(")");

		appendOrderBy(sb, "v", orderby);

		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey())
				.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		if(StringHelper.containsNonWhitespace(type)) {
			query.setParameter("resourceType", type);
		}
		List<RepositoryEntry> repoEntries = query.getResultList();
		return repoEntries;
	}

	public List<RepositoryEntryLight> getParticipantRepositoryEntry(IdentityRef identity, int maxResults, RepositoryEntryOrder... orderby) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select v from repoentrylight as v ")
		  .append(" inner join fetch v.olatResource as res ")
		  .append(" where exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership")
		  .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup and membership.identity.key=:identityKey")
		  .append("      and membership.role='").append(GroupRoles.participant.name()).append("')")
		  .append(" and (v.access>=3 or (v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true))");
		appendOrderBy(sb, "v", orderby);

		TypedQuery<RepositoryEntryLight> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryLight.class)
				.setParameter("identityKey", identity.getKey());
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}

	public List<RepositoryEntryLight> getTutorRepositoryEntry(IdentityRef identity, int maxResults, RepositoryEntryOrder... orderby) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select v from repoentrylight as v ")
		  .append(" inner join fetch v.olatResource as res ")
		  .append(" where exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership")
		  .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup and membership.identity.key=:identityKey")
		  .append("      and membership.role='").append(GroupRoles.coach.name()).append("')")
		  .append("  and (v.access>=3 or (v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true))");
		appendOrderBy(sb, "v", orderby);

		TypedQuery<RepositoryEntryLight> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryLight.class)
				.setParameter("identityKey", identity.getKey());
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}

		return query.getResultList();
	}

	public int countLearningResourcesAsOwner(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(200);
		sb.append("select count(v) from ").append(RepositoryEntry.class.getName()).append(" v ")
		  .append(" inner join v.groups as relGroup on relGroup.defaultGroup=true")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where v.access>=0 and membership.identity.key=:identityKey and membership.role='").append(GroupRoles.owner.name()).append("'");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", identity.getKey())
				.getSingleResult().intValue();
	}

	/**
	 * Gets all learning resources where the user is coach of a learning group or
	 * where he is in a rights group or where he is in the repository entry owner
	 * group (course administrator)
	 *
	 * @param identity
	 * @return list of RepositoryEntries
	 */
	public boolean hasLearningResourcesAsTeacher(IdentityRef identity) {
		return countLearningResourcesAsTeacher(identity) > 0;
	}

	public int countLearningResourcesAsTeacher(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(1200);
		sb.append("select count(v) from ").append(RepositoryEntry.class.getName()).append(" v ")
		  .append(" inner join v.olatResource as res ");
		whereClauseLearningResourcesAsTeacher(sb);

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", identity.getKey())
				.getSingleResult().intValue();
	}

	public List<RepositoryEntry> getLearningResourcesAsTeacher(Identity identity, int firstResult, int maxResults, RepositoryEntryOrder... orderby) {
		StringBuilder sb = new StringBuilder(1200);
		sb.append("select distinct v from ").append(RepositoryEntry.class.getName()).append(" v ")
		  .append(" inner join fetch v.olatResource as res ")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle");
		whereClauseLearningResourcesAsTeacher(sb);
		appendOrderBy(sb, "v", orderby);

		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey())
				.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		List<RepositoryEntry> entries = query.getResultList();
		return entries;
	}

	/**
	 * Write the where clause for countLearningResourcesAsTeacher and getLearningResourcesAsTeacher
	 * @param sb
	 */
	private final void whereClauseLearningResourcesAsTeacher(StringBuilder sb) {
		sb.append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership on membership.role ='").append(GroupRoles.coach.name()).append("'")
		  .append(" where (v.access>=3 or (v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true))")
		  .append(" and membership.identity.key=:identityKey");
	}

	public int countFavoritLearningResourcesAsTeacher(Identity identity, List<String> types) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(v) from ").append(RepositoryEntry.class.getName()).append(" v ")
		  .append(" inner join v.olatResource as res ")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership on membership.role in ('").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("')")
		  .append(" where membership.identity.key=:identityKey and v.key in (")
		  .append("   select mark.resId from ").append(MarkImpl.class.getName()).append(" mark where mark.creator.key=:identityKey and mark.resName='RepositoryEntry'")
		  .append(" )");
		if(types != null && !types.isEmpty()) {
			sb.append(" and res.resName in (:types)");
		}

		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", identity.getKey());
		if(types != null && !types.isEmpty()) {
			query.setParameter("types", types);
		}
		return query.getSingleResult().intValue();
	}

	public List<RepositoryEntry> getFavoritLearningResourcesAsTeacher(IdentityRef identity, List<String> types, int firstResult, int maxResults,
			RepositoryEntryOrder... orderby) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct v from ").append(RepositoryEntry.class.getName()).append(" v ")
		  .append(" inner join fetch v.olatResource as res ")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership on membership.role in ('").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("')")
		  .append(" where membership.identity.key=:identityKey and v.key in (")
		  .append("   select mark.resId from ").append(MarkImpl.class.getName()).append(" mark where mark.creator.key=:identityKey and mark.resName='RepositoryEntry'")
		  .append(" )");
		if(types != null && !types.isEmpty()) {
			sb.append(" and res.resName in (:types)");
		}
		appendOrderBy(sb, "v", orderby);

		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey());
		if(types != null && !types.isEmpty()) {
			query.setParameter("types", types);
		}
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}

	/**
	 * Need a repository entry or identites to return a list.
	 * @param re
	 * @param identity
	 * @return
	 */
	public List<RepositoryEntryMembership> getRepositoryEntryMembership(RepositoryEntryRef re, Identity... identity) {
		if(re == null && (identity == null || identity.length == 0)) return Collections.emptyList();

		StringBuilder sb = new StringBuilder(400);
		sb.append("select distinct membership from repoentrymembership as membership ");
		boolean and = false;
		if(re != null) {
			and = and(sb, and);
			sb.append("membership.repoKey=:repoKey");
		}
		if(identity != null && identity.length > 0) {
			and = and(sb, and);
			sb.append("membership.identityKey=:identityKeys");
		}

		TypedQuery<RepositoryEntryMembership> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryMembership.class);
		if(re != null) {
			query.setParameter("repoKey", re.getKey());
		}
		if(identity != null && identity.length > 0) {
			List<Long> ids = new ArrayList<Long>(identity.length);
			for(Identity id:identity) {
				ids.add(id.getKey());
			}
			query.setParameter("identityKeys", ids);
		}

		List<RepositoryEntryMembership> entries = query.getResultList();
		return entries;
	}

	public List<RepositoryEntryMembership> getRepositoryEntryMembership(RepositoryEntryRef re) {
		if(re == null) return Collections.emptyList();

		StringBuilder sb = new StringBuilder();
		sb.append("select membership.identity.key, membership.creationDate, membership.lastModified, membership.role ")
		  .append(" from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join v.groups as relGroup on relGroup.defaultGroup=true")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where v.key=:repoKey");

		List<Object[]> members = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("repoKey", re.getKey())
				.getResultList();

		Map<Long, RepositoryEntryMembership> memberships = new HashMap<Long, RepositoryEntryMembership>();
		for(Object[] membership:members) {
			Long identityKey = (Long)membership[0];
			Date lastModified = (Date)membership[1];
			Date creationDate = (Date)membership[2];
			Object role = membership[3];

			RepositoryEntryMembership mb = memberships.get(identityKey);
			if(mb == null) {
				mb = new RepositoryEntryMembership();
				mb.setIdentityKey(identityKey);
				mb.setRepoKey(re.getKey());
				memberships.put(identityKey, mb);
			}
			mb.setCreationDate(creationDate);
			mb.setLastModified(lastModified);

			if(GroupRoles.participant.name().equals(role)) {
				mb.setParticipant(true);
			} else if(GroupRoles.coach.name().equals(role)) {
				mb.setCoach(true);
			} else if(GroupRoles.owner.name().equals(role)) {
				mb.setOwner(true);
			}
		}

		return new ArrayList<RepositoryEntryMembership>(memberships.values());
	}

	public List<RepositoryEntryMembership> getOwnersMembership(List<RepositoryEntry> res) {
		if(res== null || res.isEmpty()) return Collections.emptyList();

		StringBuilder sb = new StringBuilder(400);
		sb.append("select distinct membership from ").append(RepositoryEntryMembership.class.getName()).append(" membership ")
		  .append(" where membership.repoKey in (:repoKey)");

		List<Long> repoKeys = PersistenceHelper.toKeys(res);
		TypedQuery<RepositoryEntryMembership> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryMembership.class)
				.setParameter("repoKey", repoKeys);

		List<RepositoryEntryMembership> entries = query.getResultList();
		return entries;
	}

	public void updateRepositoryEntryMemberships(Identity ureqIdentity, Roles ureqRoles, RepositoryEntry re,
			List<RepositoryEntryPermissionChangeEvent> changes, MailPackage mailing) {

		int count = 0;
		List<RepositoryEntryMembershipModifiedEvent> deferredEvents = new ArrayList<>();
		for(RepositoryEntryPermissionChangeEvent e:changes) {
			updateRepositoryEntryMembership(ureqIdentity, ureqRoles, re, e, mailing, deferredEvents);
			if(++count % 100 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}

		dbInstance.commitAndCloseSession();
		sendDeferredEvents(deferredEvents, re);
	}

	private void updateRepositoryEntryMembership(Identity ureqIdentity, Roles ureqRoles, RepositoryEntry re,
			RepositoryEntryPermissionChangeEvent changes, MailPackage mailing,
			List<RepositoryEntryMembershipModifiedEvent> deferredEvents) {

		if(changes.getRepoOwner() != null) {
			if(changes.getRepoOwner().booleanValue()) {
				addOwners(ureqIdentity, new IdentitiesAddEvent(changes.getMember()), re, mailing);
			} else {
				removeOwner(ureqIdentity, changes.getMember(), re, mailing);
				deferredEvents.add(RepositoryEntryMembershipModifiedEvent.removed(changes.getMember(), re));
			}
		}

		if(changes.getRepoTutor() != null) {
			if(changes.getRepoTutor().booleanValue()) {
				addTutors(ureqIdentity, ureqRoles, new IdentitiesAddEvent(changes.getMember()), re, mailing);
			} else {
				removeTutor(ureqIdentity, changes.getMember(), re, mailing);
				deferredEvents.add(RepositoryEntryMembershipModifiedEvent.removed(changes.getMember(), re));
			}
		}

		if(changes.getRepoParticipant() != null) {
			if(changes.getRepoParticipant().booleanValue()) {
				addParticipants(ureqIdentity, ureqRoles, new IdentitiesAddEvent(changes.getMember()), re, mailing);
			} else {
				removeParticipant(ureqIdentity, changes.getMember(), re, mailing, true);
				deferredEvents.add(RepositoryEntryMembershipModifiedEvent.removed(changes.getMember(), re));
			}
		}
	}

	private final boolean and(StringBuilder sb, boolean and) {
		if(and) sb.append(" and ");
		else sb.append(" where ");
		return true;
	}

	private void appendOrderBy(StringBuilder sb, String var, RepositoryEntryOrder... orderby) {
		if(orderby != null && orderby.length > 0) {
			sb.append(" order by ");
			for(RepositoryEntryOrder o:orderby) {
				switch(o) {
					case nameAsc: sb.append(var).append(".displayname asc").append(","); break;
					case nameDesc: sb.append(var).append(".displayname desc").append(","); break;
				}
			}
			sb.append(var).append(".key asc");
		}
	}

	public boolean isIdentityInTutorSecurityGroup(Identity identity, RepositoryEntryRef resource) {
		return repositoryEntryRelationDao.hasRole(identity, resource, GroupRoles.coach.name());
	}
}