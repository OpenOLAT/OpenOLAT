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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
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
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.PersistingCourseImpl;
import org.olat.fileresource.FileResourceManager;
import org.olat.group.GroupLoggingAction;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.manager.RepositoryEntryEducationalTypeDAO;
import org.olat.repository.manager.RepositoryEntryQueries;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.manager.RepositoryEntryToOrganisationDAO;
import org.olat.repository.manager.RepositoryEntryToTaxonomyLevelDAO;
import org.olat.repository.model.RepositoryEntryEducationalTypeStat;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.repository.model.RepositoryEntryMembershipModifiedEvent;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;
import org.olat.repository.model.RepositoryEntrySecurityImpl;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
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

	private static final Logger log = Tracing.createLoggerFor(RepositoryManager.class);

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
	private RepositoryEntryToOrganisationDAO repositoryEntryToOrganisationDao;
	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryEntryToTaxonomyLevelDAO;
	@Autowired
	private RepositoryEntryEducationalTypeDAO repositoryEntryEducationalTypeDao;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private LifeFullIndexer lifeIndexer;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private ACService acService;
	@Autowired
	private AutoAccessManager autoAccessManager;
	@Autowired
	private RepositoryEntryQueries repositoryEntryQueries;
	
	@PostConstruct
	public void init() {
		// Mandatory default type
		initEducationalType("exam.course", "o_exam_course", false);
		// Optional default types that can be deleted by admin user
		Set<String> educationalTypes = repositoryModule.getEnabledEducationalDefaultTypes();
		for (String educationalType : educationalTypes) {
			// remove unwanted input
			educationalType = educationalType.trim().toLowerCase().replaceAll("[^a-z]", "");
			initEducationalType(educationalType + ".course", "o_" + educationalType + "_course", true);
		}
	}

	private void initEducationalType(String identifier, String cssClass, boolean deletable) {
		RepositoryEntryEducationalType type = repositoryEntryEducationalTypeDao.loadByIdentifier(identifier);
		if (type == null) {
			if (deletable) {
				repositoryEntryEducationalTypeDao.create(identifier, cssClass);      				
			} else {				
				repositoryEntryEducationalTypeDao.createPredefined(identifier, cssClass);      
			}
		}
	}

	/**
	 * @return Singleton.
	 */
	public static RepositoryManager getInstance() {
		return CoreSpringFactory.getImpl(RepositoryManager.class);
	}

	/**
	 * Copy the repo entry image from the source to the target repository entry.
	 * If the source repo entry does not exists, nothing will happen
	 * @param target
	 * @param savedBy
	 * @param src
	 *
	 * @return
	 */
	public boolean copyImage(RepositoryEntry source, RepositoryEntry target, Identity savedBy) {
		VFSLeaf srcFile = getImage(source);
		if(srcFile == null) {
			return false;
		}

		VFSLeaf targetFile = getImage(target);
		if(targetFile != null) {
			targetFile.delete();
		}

		String sourceImageSuffix = FileUtils.getFileSuffix(srcFile.getName());
		VFSContainer targetMediaDir = this.getMediaDirectory(target.getOlatResource());
		VFSLeaf newImage = targetMediaDir.createChildLeaf(target.getResourceableId() + "." + sourceImageSuffix);
		if (newImage != null) {
			return VFSManager.copyContent(srcFile, newImage, false, savedBy);
		}
		return false;
	}

	public void deleteImage(RepositoryEntry re) {
		VFSLeaf imgFile = getImage(re);
		if (imgFile != null) {
			imgFile.delete();
		}
	}

	public VFSLeaf getImage(RepositoryEntry re) {
		return getImage(re.getKey(), re.getOlatResource());
	}
	
	public VFSLeaf getImage(Long repoEntryKey, OLATResourceable ores) {
		VFSContainer repositoryHome = getMediaDirectory(ores);
		
		String imageName = repoEntryKey + ".jpg";
		VFSItem image = repositoryHome.resolve(imageName);
		if(image instanceof VFSLeaf) {
			return (VFSLeaf)image;
		}
		imageName = repoEntryKey + ".png";
		image = repositoryHome.resolve(imageName);
		if(image instanceof VFSLeaf) {
			return (VFSLeaf)image;
		}
		return null;
	}
	
	private VFSContainer getMediaDirectory(OLATResourceable re) {
		File fResourceFileroot;
		if("CourseModule".equals(re.getResourceableTypeName())) {
			fResourceFileroot = new File(FolderConfig.getCanonicalRoot(), PersistingCourseImpl.COURSE_ROOT_DIR_NAME);
			fResourceFileroot = new File(fResourceFileroot, re.getResourceableId().toString());
		} else {
			fResourceFileroot = FileResourceManager.getInstance().getFileResourceRoot(re);
		}
		File mediaHome = new File(fResourceFileroot, "media");
		return new LocalFolderImpl(mediaHome);
	}

	public boolean setImage(VFSLeaf newImageFile, RepositoryEntry re, Identity changedBy) {
		VFSLeaf currentImage = getImage(re);
		if(currentImage != null) {
			currentImage.deleteSilently();//no versions for this
		}

		if(newImageFile == null || !newImageFile.exists() || newImageFile.getSize() <= 0) {
			return false;
		}

		String targetExtension = ".png";
		String extension = FileUtils.getFileSuffix(newImageFile.getName());
		if("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)) {
			targetExtension = ".jpg";
		}
		
		VFSContainer repositoryHome = getMediaDirectory(re.getOlatResource());
		VFSLeaf repoImage = repositoryHome.createChildLeaf(re.getResourceableId() + targetExtension);
		if(repoImage == null) {
			VFSItem item = repositoryHome.resolve(re.getResourceableId() + targetExtension);
			if(item instanceof VFSLeaf) {
				repoImage = (VFSLeaf)item;
			}
		}

		if(targetExtension.equals(".png") || targetExtension.equals(".jpg")) {
			Size newImageSize = imageHelper.getSize(newImageFile, extension);
			if(newImageSize != null && newImageSize.getWidth() <= PICTURE_WIDTH && newImageSize.getHeight() <= PICTURE_HEIGHT) {
				return VFSManager.copyContent(newImageFile, repoImage, false, changedBy);
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

		return dbInstance.getCurrentEntityManager().createQuery(query.toString(), String.class)
				.setParameter("externalIds", externalIds)
				.getResultList();
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
		RepositoryEntrySecurity reSecurity = isAllowed(identity, roles, re);
		return reSecurity.canLaunch();
	}

	public RepositoryEntrySecurity isAllowed(Identity identity, Roles roles, RepositoryEntry re) {
		boolean isOwner = false;
		boolean isCourseCoach = false;
		boolean isGroupCoach = false;
		boolean isCurriculumCoach = false;
		boolean isCourseParticipant = false;
		boolean isGroupParticipant = false;
		boolean isCurriculumParticipant = false;
		boolean isGroupWaiting = false;

		boolean isAuthor = false;
		boolean isEntryAdmin = false;
		boolean isPrincipal = false;
		boolean isAdministrator = false;
		boolean isLearnRessourceManager = false;
		boolean isMasterCoach = false;
		
		boolean canLaunch = false;
		RepositoryEntryStatusEnum status = re.getEntryStatus();
		if (roles.isGuestOnly()) {
			// allow for guests if access granted for guests
			canLaunch = re.isPublicVisible()
					&& acService.isAccessible(re, identity, null, true, false).isAccessible();
		} else {
			// allow if identity is owner
			List<Object[]> roleAndDefs = repositoryEntryRelationDao.getRoleAndDefaults(identity, re);
			for(Object[] roleAndDef:roleAndDefs) {
				String role = (String)roleAndDef[0];
				Boolean def = (Boolean)roleAndDef[1];
				Object curriculumElementKey = roleAndDef[2];
				
				if(GroupRoles.isValue(role)) {
					switch(GroupRoles.valueOf(role)) {
						case owner: {
							isOwner = true;
							break;
						}
						case coach: {
							boolean d = def != null && def.booleanValue();
							if(d) {
								isCourseCoach = true;
							} else if(curriculumElementKey != null) {
								isCurriculumCoach = true;
							} else {
								isGroupCoach = true;
							}
							break;
						}
						case participant: {
							boolean d = def != null &&def.booleanValue();
							if(d) {
								isCourseParticipant = true;
							} else if(curriculumElementKey != null) {
								isCurriculumParticipant = true;
							} else {
								isGroupParticipant = true;
							}
							break;
						}
						case waiting: {
							isGroupWaiting = true;
							break;
						}
						default: break;
					}
				} else if(OrganisationRoles.isValue(role)) {
					switch(OrganisationRoles.valueOf(role)) {
						case administrator:
							isAdministrator = true;
							break;
						case author:
							isAuthor = true;
							break;
						case learnresourcemanager:
							isLearnRessourceManager = true;
							break;
						case principal:
							isPrincipal = true;
							break;
						default: break;
					}
				} else if(CurriculumRoles.isValueOf(role)) {
					switch(CurriculumRoles.valueOf(role)) {
						case mastercoach:
							isMasterCoach = true;
							break;
						default: break;
					}
				}
			}

			// allow if access limit matches identity's role
			// allow for olat administrators
			// allow for institutional resource manager
			if(isOwner || isAdministrator || isLearnRessourceManager) {
				canLaunch = true;
				isEntryAdmin = true;
			} else if(isPrincipal) {
				canLaunch = true;
			} else {
				if (isAuthor) {
					// allow for authors if access granted at least for authors
					if(re.getCanCopy() || re.getCanDownload() || re.getCanReference() ) {
						canLaunch = status == RepositoryEntryStatusEnum.review
								|| status == RepositoryEntryStatusEnum.coachpublished
								|| status == RepositoryEntryStatusEnum.published
								|| status == RepositoryEntryStatusEnum.closed;
					}
				}
				
				if(!canLaunch && (isGroupCoach || isCourseCoach || isCurriculumCoach || isMasterCoach)) {
					canLaunch = status == RepositoryEntryStatusEnum.coachpublished
							|| status == RepositoryEntryStatusEnum.published
							|| status == RepositoryEntryStatusEnum.closed;
				}
				
				if(!canLaunch && (isGroupParticipant  || isCourseParticipant || isCurriculumParticipant)) {
					canLaunch = status == RepositoryEntryStatusEnum.published
							|| status == RepositoryEntryStatusEnum.closed;
				}
				
				if(!canLaunch && re.isPublicVisible()) {
					canLaunch = acService.isAccessible(re, identity, Boolean.FALSE, false, false).isAccessible();
				}
			}
		}

		boolean readOnly = re.getEntryStatus().decommissioned();

		return new RepositoryEntrySecurityImpl(isEntryAdmin, isOwner,
				isCourseParticipant, isCourseCoach,
				isGroupParticipant, isGroupCoach, isGroupWaiting,
				isCurriculumParticipant, isCurriculumCoach, isMasterCoach,
				isAuthor, isAdministrator, isLearnRessourceManager, isPrincipal, canLaunch, readOnly);
	}
	
	public RepositoryEntry setAccess(final RepositoryEntry re, boolean publicVisible,
			RepositoryEntryAllowToLeaveOptions leaveSetting, boolean canCopy, boolean canReference, boolean canDownload,
			List<Organisation> organisations) {
		RepositoryEntry reloadedRe = repositoryEntryDao.loadForUpdate(re);
		if(reloadedRe == null) {
			return null;
		}
		
		reloadedRe.setLastModified(new Date());
		reloadedRe.setPublicVisible(publicVisible);
		reloadedRe.setAllowToLeaveOption(leaveSetting);
		reloadedRe.setCanCopy(canCopy);
		reloadedRe.setCanReference(canReference);
		reloadedRe.setCanDownload(canDownload);
		
		if (!publicVisible) {
			acService.deleteOffers(reloadedRe.getOlatResource());
		}
		
		if(organisations != null) {
			// sync the relation re_to_group
			List<Organisation> currentOrganisationsByGroups = repositoryEntryRelationDao.getOrganisations(reloadedRe);
			for(Organisation currentOrganisation:currentOrganisationsByGroups) {
				if(!organisations.contains(currentOrganisation)) {
					repositoryEntryRelationDao.removeRelation(currentOrganisation.getGroup(), reloadedRe);
				}
			}
			for(Organisation organisation:organisations) {
				if(!currentOrganisationsByGroups.contains(organisation)) {
					RepositoryEntryToGroupRelation relToGroup = repositoryEntryRelationDao.createRelation(organisation.getGroup(), reloadedRe);
					reloadedRe.getGroups().add(relToGroup);
				}
			}
			
			// sync the relation repository entry to organisation	
			Set<RepositoryEntryToOrganisation> currentRelations = reloadedRe.getOrganisations();
			List<RepositoryEntryToOrganisation> copyRelations = new ArrayList<>(currentRelations);
			List<Organisation> currentOrganisationsByRelations = new ArrayList<>();
			for(RepositoryEntryToOrganisation relation:copyRelations) {
				if(!organisations.contains(relation.getOrganisation())) {
					repositoryEntryToOrganisationDao.delete(relation);
					currentRelations.remove(relation);
				} else {
					currentOrganisationsByRelations.add(relation.getOrganisation());
				}
			}
			
			for(Organisation organisation:organisations) {
				if(!currentOrganisationsByRelations.contains(organisation)) {
					RepositoryEntryToOrganisation newRelation = repositoryEntryToOrganisationDao.createRelation(organisation, reloadedRe, false);
					currentRelations.add(newRelation);
				}
			}
			reloadedRe.setOrganisations(currentRelations);
		}
		
		RepositoryEntry updatedRe = dbInstance.getCurrentEntityManager().merge(reloadedRe);
		//fetch the values
		updatedRe.getStatistics().getLaunchCounter();
		if(updatedRe.getLifecycle() != null) {
			updatedRe.getLifecycle().getCreationDate();
		}
		dbInstance.commit();
		lifeIndexer.indexDocument(RepositoryEntryDocument.TYPE, updatedRe.getKey());
		return updatedRe;
	}
	
	public RepositoryEntry setStatus(final RepositoryEntry re, RepositoryEntryStatusEnum status) {
		RepositoryEntry reloadedRe = repositoryEntryDao.loadForUpdate(re);
		if(reloadedRe == null) {
			return null;
		}
		reloadedRe.setEntryStatus(status);

		reloadedRe.setLastModified(new Date());
		//properties
		RepositoryEntry updatedRe = dbInstance.getCurrentEntityManager().merge(reloadedRe);
		//fetch the values
		updatedRe.getStatistics().getLaunchCounter();
		if(updatedRe.getLifecycle() != null) {
			updatedRe.getLifecycle().getCreationDate();
		}
		dbInstance.commit();
		return updatedRe;
	}
	
	public RepositoryEntry setTechnicalType(RepositoryEntry re, String technicalType) {
		RepositoryEntry reloadedRe = repositoryEntryDao.loadForUpdate(re);
		if(reloadedRe == null) {
			return null;
		}
		reloadedRe.setTechnicalType(technicalType);

		reloadedRe.setLastModified(new Date());
		//properties
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
	
	public RepositoryEntry setDescriptionAndName(final RepositoryEntry re, String displayName, String description) {
		RepositoryEntry reloadedRe = repositoryEntryDao.loadForUpdate(re);
		if(reloadedRe == null) {
			return null;
		}
		
		reloadedRe.setDisplayname(displayName);
		reloadedRe.setDescription(description);
		
		RepositoryEntry updatedRe = dbInstance.getCurrentEntityManager().merge(reloadedRe);

		dbInstance.commit();
		lifeIndexer.indexDocument(RepositoryEntryDocument.TYPE, updatedRe.getKey());
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
			String teaser, String location, String authors, String externalId, String externalRef, String managedFlags,
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
		if(StringHelper.containsNonWhitespace(teaser)) {
			reloadedRe.setTeaser(teaser);
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
	
	public RepositoryEntry setExpenditureOfWork(final RepositoryEntry re,String expenditureOfWork) {
		RepositoryEntry reloadedRe = repositoryEntryDao.loadForUpdate(re);
		if(reloadedRe == null) {
			return null;
		}
		reloadedRe.setExpenditureOfWork(expenditureOfWork);
		
		RepositoryEntry updatedRe = dbInstance.getCurrentEntityManager().merge(reloadedRe);

		//fetch the values
		updatedRe.getStatistics().getLaunchCounter();
		if(updatedRe.getLifecycle() != null) {
			updatedRe.getLifecycle().getCreationDate();
		}
		dbInstance.commit();
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
	 * @param taxonomyLevels 
	 * @param educationalType 
	 * @return
	 */
	public RepositoryEntry setDescriptionAndName(final RepositoryEntry re,
			String displayName, String externalRef, String authors, String description, String teaser,
			String objectives, String requirements, String credits, String mainLanguage,
			String location, String expenditureOfWork, RepositoryEntryLifecycle cycle,
			List<Organisation> organisations, Set<TaxonomyLevel> taxonomyLevels, 
			RepositoryEntryEducationalType educationalType) {
		RepositoryEntry reloadedRe = repositoryEntryDao.loadForUpdate(re);
		if(reloadedRe == null) {
			return null;
		}
		reloadedRe.setDisplayname(displayName);
		reloadedRe.setAuthors(authors);
		reloadedRe.setDescription(description);
		reloadedRe.setTeaser(teaser);
		reloadedRe.setExternalRef(externalRef);
		reloadedRe.setObjectives(objectives);
		reloadedRe.setRequirements(requirements);
		reloadedRe.setCredits(credits);
		reloadedRe.setMainLanguage(mainLanguage);
		reloadedRe.setExpenditureOfWork(expenditureOfWork);
		reloadedRe.setLocation(location);
		reloadedRe.setEducationalType(educationalType);

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
		
		if (taxonomyLevels != null) {
			List<TaxonomyLevel> currentTaxonomyLevels = repositoryEntryToTaxonomyLevelDAO.getTaxonomyLevels(reloadedRe);
			
			Collection<TaxonomyLevel> addLevels = new HashSet<>(taxonomyLevels);
			addLevels.removeAll(currentTaxonomyLevels);
			for (TaxonomyLevel addLevel : addLevels) {
				repositoryEntryToTaxonomyLevelDAO.createRelation(reloadedRe, addLevel);
			}
			Collection<TaxonomyLevel> removeLevels = new HashSet<>(currentTaxonomyLevels);
			removeLevels.removeAll(taxonomyLevels);
			for (TaxonomyLevel removeLevel: removeLevels) {
				repositoryEntryToTaxonomyLevelDAO.deleteRelation(reloadedRe, removeLevel);
			}
		}

		if(organisations != null) {
			// sync the relation re_to_group
			List<Organisation> currentOrganisationsByGroups = repositoryEntryRelationDao.getOrganisations(reloadedRe);
			for(Organisation currentOrganisation:currentOrganisationsByGroups) {
				if(!organisations.contains(currentOrganisation)) {
					repositoryEntryRelationDao.removeRelation(currentOrganisation.getGroup(), reloadedRe);
				}
			}
			for(Organisation organisation:organisations) {
				if(!currentOrganisationsByGroups.contains(organisation)) {
					RepositoryEntryToGroupRelation relToGroup = repositoryEntryRelationDao.createRelation(organisation.getGroup(), reloadedRe);
					reloadedRe.getGroups().add(relToGroup);
				}
			}
			
			// sync the relation repository entry to organisation	
			Set<RepositoryEntryToOrganisation> currentRelations = reloadedRe.getOrganisations();
			List<RepositoryEntryToOrganisation> copyRelations = new ArrayList<>(currentRelations);
			List<Organisation> currentOrganisationsByRelations = new ArrayList<>();
			for(RepositoryEntryToOrganisation relation:copyRelations) {
				if(!organisations.contains(relation.getOrganisation())) {
					repositoryEntryToOrganisationDao.delete(relation);
					currentRelations.remove(relation);
				} else {
					currentOrganisationsByRelations.add(relation.getOrganisation());
				}
			}
			
			for(Organisation organisation:organisations) {
				if(!currentOrganisationsByRelations.contains(organisation)) {
					RepositoryEntryToOrganisation newRelation = repositoryEntryToOrganisationDao.createRelation(organisation, reloadedRe, false);
					currentRelations.add(newRelation);
				}
			}
			reloadedRe.setOrganisations(currentRelations);
		}
		dbInstance.commit();

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
	 * Count by type, exclude deleted.
	 * @param restrictedType
	 * @param Roles
	 * @return Number of repo entries
	 */
	public int countByType(String restrictedType) {
		QueryBuilder query = new QueryBuilder(400);
		query.append("select count(*) from repositoryentry v")
			 .append(" inner join v.olatResource res")
		     .append(" where res.resName=:restrictedType and v.status ").in(RepositoryEntryStatusEnum.preparationToClosed());
		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Number.class)
				.setParameter("restrictedType", restrictedType)
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? null : count.get(0).intValue();
	}

	public int countPublished(String restrictedType) {
		StringBuilder query = new StringBuilder(400);
		query.append("select count(*) from repositoryentry v")
		     .append(" inner join v.olatResource res")
		     .append(" where res.resName=:restrictedType")
		     .append(" and v.status='").append(RepositoryEntryStatusEnum.published).append("'");

		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Number.class)
				.setParameter("restrictedType", restrictedType)
				.setFlushMode(FlushModeType.COMMIT)
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? null : count.get(0).intValue();
	}

	/**
	 * Query by ownership, optionally limit by type.
	 *
	 * @param identity
	 * @param limitType
	 * @return Results
	 */
	public List<RepositoryEntry> queryByOwner(IdentityRef identity, boolean follow, IdentityRef asParticipant, String... limitTypes) {
		if (identity == null) throw new AssertException("identity can not be null!");
		QueryBuilder sb = new QueryBuilder(400);
		sb.append("select v from repositoryentry v")
		  .append(" inner join fetch v.olatResource as res")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as relGroup ").append(" on relGroup.defaultGroup=true", !follow)
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership on membership.role='").append(GroupRoles.owner.name()).append("'")
		  .append(" where membership.identity.key=:identityKey and v.status ").in(RepositoryEntryStatusEnum.preparationToClosed());
		if (limitTypes != null && limitTypes.length > 0) {
			sb.append(" and res.resName in (:types)");
		}
		// only as participant
		if (asParticipant != null) { // fuzzy author search
			sb.append(" and exists (select relpart from repoentrytogroup as relpart, bgroupmember as participant")
		      .append("   where relpart.entry.key=v.key and participant.group.key=relpart.group.key")
		      .append("   and participant.role='").append(GroupRoles.participant.name()).append("'")
		      .append("   and participant.identity.key=:participantKey")
		      .append(" )");
		}

		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey());
		if(limitTypes != null && limitTypes.length > 0) {
			List<String> types = new ArrayList<>();
			for(String type:limitTypes) {
				types.add(type);
			}
			query.setParameter("types", types);
		}
		if(asParticipant != null) {
			query.setParameter("participantKey", asParticipant.getKey());
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
	public List<RepositoryEntry> queryByMembership(IdentityRef identity, boolean owner, boolean coach, boolean participant, String limitType) {
		if (identity == null) throw new AssertException("identity can not be null!");
		if (!owner && !coach && !participant) return Collections.emptyList();

		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select v from repositoryentry v ")
		  .append(" inner join fetch v.olatResource as res")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where res.resName=:type and membership.identity.key=:identityKey and (");
		if(owner) {
			sb.append(" (v.status ").in(RepositoryEntryStatusEnum.preparationToClosed()).append(" and membership.role='").append(GroupRoles.owner).append("')");
		}
		if(coach) {
			if(owner) {
				sb.append(" or");
			}
			sb.append(" (v.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed()).append(" and membership.role='").append(GroupRoles.coach).append("')");
		}
		if(participant) {
			if(owner || coach) {
				sb.append(" or");
			}
			sb.append(" (v.status ").in(RepositoryEntryStatusEnum.publishedAndClosed()).append(" and membership.role='").append(GroupRoles.participant).append("')");
		}
		sb.append(")");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("type", limitType)
				.getResultList();
	}

	/**
	 * Query by initial-author
	 * @param restrictedType
	 * @param Roles
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
	 * This is an administrative query which need author, learn resource manager, quality manager
	 * or higher permissions.
	 * 
	 * @param identity The searcher
	 * @param roles The roles of the searcher
	 * @param organisationWildCard No roles check but only organization membership instead of roles
	 * @param resourceTypes
	 * @param displayName
	 * @param author
	 * @param desc
	 * @param idAndRefs TODO
	 * @param checkCanReference
	 * @param checkCanCopy
	 * @return
	 */
	public List<RepositoryEntry> queryResourcesLimitType(Identity identity, Roles roles, boolean organisationWildCard,
			List<String> resourceTypes, String displayName, String author, String desc, String idAndRefs,
			IdentityRef asParticipant, boolean checkCanReference, boolean checkCanCopy) {
		if(!roles.isAuthor() && !roles.isLearnResourceManager() && !roles.isAdministrator() && !roles.isQualityManager()) {
			return Collections.emptyList();
		}

		// Build the query
		// 1) Joining tables
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select distinct v from repositoryentry v ")
		  .append(" inner join fetch v.olatResource as res" )
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" left join v.groups as relGroup")
		  .append(" left join relGroup.group as baseGroup")
		  .append(" left join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey")
		  .append(" and (");
		//owner, learn resource manager, administrator
		sb.append("(membership.role ").in(GroupRoles.owner, OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager)
		  .append("  and v.status ").in(RepositoryEntryStatusEnum.preparationToClosed()).append(")");
		//author
		if(roles.isAuthor()) {
			sb.append(" or (membership.role ").in(OrganisationRoles.author)
			  .append(" and v.status ").in(RepositoryEntryStatusEnum.reviewToClosed())
			  .append(" and v.canReference=true", checkCanReference)
			  .append(" and v.canCopy=true", checkCanCopy)
			  .append(")");
		}
		if(roles.isQualityManager()) {
			sb.append(" or (membership.role ").in(OrganisationRoles.qualitymanager)
			  .append(" and v.status ").in(RepositoryEntryStatusEnum.published)
			  .append(" and v.canReference=true", checkCanReference)
			  .append(" and v.canCopy=true", checkCanCopy)
			  .append(")");
		}
		if(organisationWildCard) {
			sb.append(" or (membership.role ").in(OrganisationRoles.user)
			  .append(" and v.status ").in(RepositoryEntryStatusEnum.preparationToClosed())
			  .append(" and v.canReference=true", checkCanReference)
			  .append(" and v.canCopy=true", checkCanCopy)
			  .append(")");
		}
		
		sb.append(")");
		 
		// restrict on type
		if (resourceTypes != null) {
			sb.append(" and res.resName in (:resourcetypes)");
		}
		// restrict on author
		if (StringHelper.containsNonWhitespace(author)) { // fuzzy author search
			author = author.replace('*','%');
			author = '%' + author + '%';
			sb.append(" and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership, ")
			  .append(IdentityImpl.class.getName()).append(" as identity, ").append(UserImpl.class.getName()).append(" as user")
		      .append("    where rel.entry.key=v.key and rel.group.key=baseGroup.key and membership.group.key=baseGroup.key and membership.identity=identity and user.identity.key=identity.key")
		         .append("      and membership.role='").append(GroupRoles.owner.name()).append("'")
		         .append("      and (user.firstName like :author or user.lastName like :author or identity.name like :author)")
		         .append("  )");
		}
		// only as participant
		if (asParticipant != null) { // fuzzy author search
			sb.append(" and exists (select relpart from repoentrytogroup as relpart, bgroupmember as participant")
		      .append("   where relpart.entry.key=v.key and participant.group.key=relpart.group.key")
		      .append("   and participant.role='").append(GroupRoles.participant.name()).append("'")
		      .append("   and participant.identity.key=:participantKey")
		      .append(" )");
		}
		
		// restrict on resource name
		if (StringHelper.containsNonWhitespace(displayName)) {
			displayName = displayName.replace('*','%');
			displayName = '%' + displayName + '%';
			sb.append(" and v.displayname like :displayname");
		}
		// restrict on resource description
		if (StringHelper.containsNonWhitespace(desc)) {
			desc = desc.replace('*','%');
			desc = '%' + desc + '%';
			sb.append(" and v.description like :desc");
		}
		// restrict on id and refs
		Long quickId = null;
		String quickRefs = null;
		String quickText = null;
		if(StringHelper.containsNonWhitespace(idAndRefs)) {
			quickRefs = idAndRefs;
			quickText = PersistenceHelper.makeFuzzyQueryString(idAndRefs);
			sb.append(" and (v.externalId=:quickRef or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.externalRef", "quickText", dbInstance.getDbVendor());
			sb.append(" or v.softkey=:quickRef ");
			if(StringHelper.isLong(idAndRefs)) {
				try {
					quickId = Long.parseLong(idAndRefs);
					sb.append(" or v.key=:quickVKey or res.resId=:quickVKey");
				} catch (NumberFormatException e) {
					//
				}
			}
			sb.append(")");	
		}

		// create query an set query data
		TypedQuery<RepositoryEntry> dbquery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey());
		if (StringHelper.containsNonWhitespace(author)) {
			dbquery.setParameter("author", author);
		}
		if (StringHelper.containsNonWhitespace(displayName)) {
			dbquery.setParameter("displayname", displayName);
		}
		if (StringHelper.containsNonWhitespace(desc)) {
			dbquery.setParameter("desc", desc);
		}
		if(quickId != null) {
			dbquery.setParameter("quickVKey", quickId);
		}
		if(quickRefs != null) {
			dbquery.setParameter("quickRef", quickRefs);
		}
		if(quickText != null) {
			dbquery.setParameter("quickText", quickText);
		}
		if (resourceTypes != null) {
			dbquery.setParameter("resourcetypes", resourceTypes);
		}
		if (asParticipant != null) {
			dbquery.setParameter("participantKey", asParticipant.getKey());
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
	public List<RepositoryEntry> queryByOwnerLimitAccess(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select v from repositoryentry v")
		  .append(" inner join fetch v.olatResource as res")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership on membership.role='").append(GroupRoles.owner.name()).append("'")
		  .append(" where membership.identity.key=:identityKey and v.status ").in(RepositoryEntryStatusEnum.preparationToClosed());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public int countGenericANDQueryWithRolesRestriction(SearchRepositoryEntryParameters params) {
		return repositoryEntryQueries.countEntries(params);
	}

	public List<RepositoryEntry> genericANDQueryWithRolesRestriction(SearchRepositoryEntryParameters params, int firstResult, int maxResults, boolean orderBy) {
		return repositoryEntryQueries.searchEntries(params, firstResult, maxResults, orderBy);
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
			removeParticipant(identity, identity, re, mailing, true);
			RepositoryEntryMembershipModifiedEvent deferredEvent = RepositoryEntryMembershipModifiedEvent.removed(identity, re);
			dbInstance.commit();
			sendDeferredEvent(deferredEvent, re);
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
		List<Identity> reallyAddedId = new ArrayList<>();
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
				log.info(Tracing.M_AUDIT, "Identity(.key):" + ureqIdentity.getKey() + " added identity '" + identity.getKey()
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
	
	private void sendDeferredEvent(MultiUserEvent event, RepositoryEntry ores) {
		sendDeferredEvents(List.of(event), ores);
	}

	private void sendDeferredEvents(List<? extends MultiUserEvent> events, RepositoryEntry ores) {
		EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		for(MultiUserEvent event:events) {
			eventBus.fireEventToListenersOf(event, ores);
		}
	}

	private void removeOwner(Identity ureqIdentity, Identity identity, RepositoryEntry re, MailPackage mailing) {
		int rows = repositoryEntryRelationDao.removeRole(identity, re, GroupRoles.owner.name());
		if(rows > 0) {
			RepositoryMailing.sendEmail(ureqIdentity, identity, re, RepositoryMailing.Type.removeTutor, mailing);
	
			ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
			ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
			try{
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_REMOVED, getClass(),
						LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry), LoggingResourceable.wrap(identity));
			} finally {
				ThreadLocalUserActivityLogger.setStickyActionType(actionType);
			}
	
			log.info(Tracing.M_AUDIT, "Identity(.key):{} removed identity '{}' as owner from repositoryentry with key {}",
					ureqIdentity.getKey(), identity.getKey(), re.getKey());
		}
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
		List<Identity> reallyAddedId = new ArrayList<>();
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
		log.info(Tracing.M_AUDIT, "Identity(.key):" + ureqIdentity.getKey() + " added identity '" + identity.getKey()
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
		int rows = repositoryEntryRelationDao.removeRole(identity, re, GroupRoles.coach.name());
		if(rows > 0) {
			RepositoryMailing.sendEmail(ureqIdentity, identity, re, RepositoryMailing.Type.removeTutor, mailing);
			
			ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
			ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
			try{
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_REMOVED, getClass(),
						LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry), LoggingResourceable.wrap(identity));
			} finally {
				ThreadLocalUserActivityLogger.setStickyActionType(actionType);
			}
			log.info(Tracing.M_AUDIT, "Identity(.key):" + ureqIdentity.getKey() + " removed identity '" + identity.getKey()
					+ "' as coach from repositoryentry with key " + re.getKey());
		}
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
		List<Identity> reallyAddedId = new ArrayList<>();
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
						
						dbInstance.commit();
						RepositoryEntryMembershipModifiedEvent event = RepositoryEntryMembershipModifiedEvent.roleParticipantAddPending(identityToAdd, re);
						sendDeferredEvent(event, re);
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
		
		RepositoryEntryMembershipModifiedEvent deferredEvent = RepositoryEntryMembershipModifiedEvent.roleParticipantAdded(identity, re);
		dbInstance.commit();
		sendDeferredEvent(deferredEvent, re);

		ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
		ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
		try{
			ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_ADDED, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry), LoggingResourceable.wrap(identity));
		} finally {
			ThreadLocalUserActivityLogger.setStickyActionType(actionType);
		}
		log.info(Tracing.M_AUDIT, "Identity(.key):{} added identity '{}' to repositoryentry with key {}",
				ureqIdentity.getKey(), identity.getKey(), re.getKey());
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
		int rows = repositoryEntryRelationDao.removeRole(identity, re, GroupRoles.participant.name());
		if(rows > 0) {
			if(sendMail) {
				RepositoryMailing.sendEmail(ureqIdentity, identity, re, RepositoryMailing.Type.removeParticipant, mailing);
			}
	
			ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
			ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
			try{
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_PARTICIPANT_REMOVED, getClass(),
						LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry), LoggingResourceable.wrap(identity));
			} finally {
				ThreadLocalUserActivityLogger.setStickyActionType(actionType);
			}
		
			log.info(Tracing.M_AUDIT, "Identity(.key):" + ureqIdentity.getKey() + " removed identity '" + identity.getKey()
				+ "' as participant from repositoryentry with key " + re.getKey());
		}
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
			log.info(Tracing.M_AUDIT, sb.toString());
		}

		for(Identity identity:members) {
			RepositoryMailing.sendEmail(ureqIdentity, identity, re, RepositoryMailing.Type.removeParticipant, mailing);
		}
		return allOk;
	}

	public int countLearningResourcesAsStudent(IdentityRef identity, String type) {
		QueryBuilder sb = new QueryBuilder(1200);
		sb.append("select count(distinct v) from repositoryentry as v")
		  .append(" inner join v.olatResource as res")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey and membership.role='").append(GroupRoles.participant).append("'")
		  .append(" and v.status ").in(RepositoryEntryStatusEnum.publishedAndClosed()).append(" and res.resName=:resourceType");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("resourceType", type)
				.getSingleResult().intValue();
	}

	/**
	 * Gets all learning resources where the user is in a learning group as participant.
	 * @param identity
	 * @return list of RepositoryEntries
	 */
	public List<RepositoryEntry> getLearningResourcesAsStudent(Identity identity, String type, int firstResult, int maxResults,
			RepositoryEntryOrder... orderby) {
		QueryBuilder sb = new QueryBuilder(1200);
		sb.append("select distinct v from repositoryentry as v ")
		  .append(" inner join fetch v.olatResource as res")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey and membership.role ").in(GroupRoles.participant)
		  .append(" and v.status ").in(RepositoryEntryStatusEnum.publishedAndClosed());
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
		return query.getResultList();
	}

	/**
	 * Gets all learning resources where the user is in a learning group as participant.
	 * 
	 * @param identity The identity (mandatory)
	 * @param type The resource type (mandatory)
	 * @return list of RepositoryEntries
	 */
	public List<RepositoryEntry> getLearningResourcesAsParticipantAndCoach(Identity identity, String type) {
		QueryBuilder sb = new QueryBuilder(1200);
		sb.append("select distinct v from repositoryentry as v")
		  .append(" inner join fetch v.olatResource as res")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey and (")
		  .append("   (membership.role='").append(GroupRoles.participant.name()).append("' and v.status ").in(RepositoryEntryStatusEnum.publishedAndClosed()).append(")")
		  .append("   or")
		  .append("   (membership.role='").append(GroupRoles.coach.name()).append("' and v.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed()).append(")")
		  .append(" ) and res.resName=:resourceType");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("resourceType", type)
				.getResultList();
	}

	/**
	 * This method only returns entries with a valid membership.
	 * 
	 * @param identity The identity
	 * @param roles The roles of the identity
	 * @param type The type of resource to search for
	 * @param firstResult The first result
	 * @param maxResults The max. numbers of results to return or -1 if all
	 * @return A list of repository entries
	 */
	public List<RepositoryEntry> getLearningResourcesAsBookmarkedMember(Identity identity, Roles roles, String type, int firstResult, int maxResults) {
		if(roles.isGuestOnly()) {
			return Collections.emptyList();
		}

		QueryBuilder sb = new QueryBuilder(1200);
		sb.append("select v from repositoryentry as v")
		  .append(" inner join fetch v.olatResource as res")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where exists (select mark.key from ").append(MarkImpl.class.getName()).append(" as mark")
		  .append("   where mark.creator.key=:identityKey and mark.resId=v.key and mark.resName='RepositoryEntry'")
		  .append(" ) ")
		  .append(" and res.resName=:resourceType")
		  .append(" and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership")
		  .append("   where rel.entry.key=v.key and rel.group.key=baseGroup.key and membership.group.key=baseGroup.key and membership.identity.key=:identityKey")
		  .append("   and (")
		  .append("     (")
		  .append("      membership.role ").in(OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager, GroupRoles.owner).append(" and v.status").in(RepositoryEntryStatusEnum.preparationToClosed())
		  .append("     ) or (")
		  .append("      membership.role ").in(GroupRoles.coach).append(" and v.status").in(RepositoryEntryStatusEnum.coachPublishedToClosed())
		  .append("     ) or (")
		  .append("      membership.role ").in(GroupRoles.participant).append(" and v.status").in(RepositoryEntryStatusEnum.publishedAndClosed())
		  .append("     )")
		  .append("   )")
		  .append(" )");
		
		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("resourceType", type)
				.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}

	public List<RepositoryEntry> getParticipantRepositoryEntry(IdentityRef identity, int maxResults, RepositoryEntryOrder... orderby) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select v from repositoryentry as v")
		  .append(" inner join fetch v.olatResource as res");
		if(dbInstance.isMySQL()) {
			sb.append(" where v.key in (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership")
			  .append("   where rel.group.key=membership.group.key and membership.identity.key=:identityKey");
		} else {
			sb.append(" where exists (select rel from repoentrytogroup as rel, bgroupmember as membership")
			  .append("   where rel.entry.key=v.key and rel.group.key=membership.group.key and membership.identity.key=:identityKey");
		}
		sb.append("   and membership.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" )")
		  .append(" and v.status ").in(RepositoryEntryStatusEnum.publishedAndClosed());
		
		// Public access and offers
		List<OrganisationRef> offerOrganisations = acService.getOfferOrganisations(identity);
		Date offerValidAt = null;
		if (!offerOrganisations.isEmpty()) {
			sb.append(" or (");
			sb.append(" res.key in (");
			sb.append("   select resource.key");
			sb.append("     from acoffer offer");
			sb.append("     inner join offer.resource resource");
			sb.append("     inner join repositoryentry re2");
			sb.append("        on re2.olatResource.key = resource.key");
			sb.append("       and re2.publicVisible = true");
			sb.append("     inner join offertoorganisation oto");
			sb.append("        on oto.offer.key = offer.key");
			sb.append("    where offer.valid = true");
			sb.append("      and offer.openAccess = true");
			sb.append("      and re2.status ").in(ACService.RESTATUS_ACTIVE_OPEN);
			sb.append("      and oto.organisation.key in :organisationKeys");
			sb.append(")"); // in
			sb.append(")"); // or
			
			// Access methods
			if (acModule.isEnabled()) {
				sb.append(" or (");
				sb.append(" res.key in (");
				sb.append("   select resource.key");
				sb.append("     from acofferaccess access");
				sb.append("     inner join access.offer offer");
				sb.append("     inner join offer.resource resource");
				sb.append("     inner join repositoryentry re2");
				sb.append("        on re2.olatResource.key = resource.key");
				sb.append("       and re2.publicVisible = true");
				sb.append("     inner join offertoorganisation oto");
				sb.append("        on oto.offer.key = offer.key");
				sb.append("   where offer.valid = true");
				sb.append("     and offer.openAccess = false");
				sb.append("     and offer.guestAccess = false");
				sb.append("     and access.method.enabled = true");
				sb.append("     and oto.organisation.key in :organisationKeys");
				
				offerValidAt = new Date();
				sb.append(" and (");
				sb.append(" re2.status ").in(ACService.RESTATUS_ACTIVE_METHOD_PERIOD);
				sb.append(" and (offer.validFrom is not null or offer.validTo is not null)");
				sb.append(" and (offer.validFrom is null or offer.validFrom<=:offerValidAt)");
				sb.append(" and (offer.validTo is null or offer.validTo>=:offerValidAt)");
				sb.append(" or");
				sb.append(" re2.status ").in(ACService.RESTATUS_ACTIVE_METHOD);
				sb.append(" and offer.validFrom is null and offer.validTo is null");
				sb.append(" )");
				sb.append(")"); // in
				sb.append(")"); // or
			}
		}
		
		appendOrderBy(sb, "v", orderby);

		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey());
		if (!offerOrganisations.isEmpty()) {
			query.setParameter("organisationKeys", offerOrganisations.stream().map(OrganisationRef::getKey).collect(Collectors.toList()));
		}
		if (offerValidAt != null) {
			query.setParameter( "offerValidAt", offerValidAt);
		}
		
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
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
		QueryBuilder sb = new QueryBuilder(1200);
		sb.append("select count(v) from repositoryentry v")
		  .append(" inner join v.olatResource as res");
		whereClauseLearningResourcesAsTeacher(sb);

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", identity.getKey())
				.getSingleResult().intValue();
	}

	public List<RepositoryEntry> getLearningResourcesAsTeacher(Identity identity, int firstResult, int maxResults, RepositoryEntryOrder... orderby) {
		QueryBuilder sb = new QueryBuilder(1200);
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
		return query.getResultList();
	}

	/**
	 * Write the where clause for countLearningResourcesAsTeacher and getLearningResourcesAsTeacher
	 * @param sb
	 */
	private final void whereClauseLearningResourcesAsTeacher(QueryBuilder sb) {
		sb.append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.role ='").append(GroupRoles.coach.name()).append("' and membership.identity.key=:identityKey")
		  .append(" and v.status ").in(RepositoryEntryStatusEnum.coachpublished, RepositoryEntryStatusEnum.published, RepositoryEntryStatusEnum.closed);
	}

	public int countFavoritLearningResourcesAsTeacher(Identity identity, List<String> types) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(v) from repositoryentry v")
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
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct v from repositoryentry v")
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
	
    public int countLearningResourcesAsOwner(IdentityRef identity) {
        QueryBuilder sb = new QueryBuilder(1200);
        sb.append("select count(v.key) from repositoryentry v")
          .append(" inner join v.olatResource as res");
        whereClauseLearningResourcesAsOwner(sb);

        return dbInstance.getCurrentEntityManager()
                .createQuery(sb.toString(), Number.class)
                .setParameter("identityKey", identity.getKey())
                .getSingleResult().intValue();
    }

    public List<RepositoryEntry> getLearningResourcesAsOwner(Identity identity, int firstResult, int maxResults, RepositoryEntryOrder... orderby) {
        QueryBuilder sb = new QueryBuilder(1200);
        sb.append("select distinct v from ").append(RepositoryEntry.class.getName()).append(" v ")
          .append(" inner join fetch v.olatResource as res ")
          .append(" inner join fetch v.statistics as statistics")
          .append(" left join fetch v.lifecycle as lifecycle");
        whereClauseLearningResourcesAsOwner(sb);
        appendOrderBy(sb, "v", orderby);

        TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
                .createQuery(sb.toString(), RepositoryEntry.class)
                .setParameter("identityKey", identity.getKey())
                .setFirstResult(firstResult);
        if(maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        return query.getResultList();
    }

    /**
     * Write the where clause for countLearningResourcesAsOwner and getLearningResourcesAsOwner
     * @param sb
     */
    private final void whereClauseLearningResourcesAsOwner(QueryBuilder sb) {
        sb.append(" inner join v.groups as relGroup")
          .append(" inner join relGroup.group as baseGroup")
          .append(" inner join baseGroup.members as membership")
          .append(" where membership.role ='").append(GroupRoles.owner.name()).append("' and membership.identity.key=:identityKey");
    }


	/**
	 * Need a repository entry or identites to return a list.
	 * @param re
	 * @param identity
	 * @return
	 */
	public List<RepositoryEntryMembership> getRepositoryEntryMembership(RepositoryEntryRef re, IdentityRef identity) {
		if(re == null || identity == null) return Collections.emptyList();

		QueryBuilder sb = new QueryBuilder(400);
		sb.append("select distinct membership from repoentrymembership as membership")
		  .append(" where membership.identityKey=:identityKey");
		if(re != null) {
			sb.append(" and membership.repoKey=:repoKey");
		}

		TypedQuery<RepositoryEntryMembership> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryMembership.class)
				.setParameter("identityKey", identity.getKey());
		if(re != null) {
			query.setParameter("repoKey", re.getKey());
		}
		return query.getResultList();
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

		Map<Long, RepositoryEntryMembership> memberships = new HashMap<>();
		for(Object[] membership:members) {
			Long identityKey = (Long)membership[0];
			Date lastModified = (Date)membership[1];
			Date creationDate = (Date)membership[2];
			Object role = membership[3];

			RepositoryEntryMembership mb = memberships.computeIfAbsent(identityKey, key -> {
				RepositoryEntryMembership rmb = new RepositoryEntryMembership();
				rmb.setIdentityKey(identityKey);
				rmb.setRepoKey(re.getKey());
				return rmb;
			});
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

		return new ArrayList<>(memberships.values());
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

	private void appendOrderBy(QueryBuilder sb, String var, RepositoryEntryOrder... orderby) {
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

	public RepositoryEntryEducationalType createEducationalType(String identifier) {
		return repositoryEntryEducationalTypeDao.create(identifier);
	}
	
	public RepositoryEntryEducationalType updateEducationalType(RepositoryEntryEducationalType educationalType) {
		return repositoryEntryEducationalTypeDao.save(educationalType);
	}
	
	public List<RepositoryEntryEducationalType> getAllEducationalTypes() {
		return repositoryEntryEducationalTypeDao.loadAll();
	}

	public RepositoryEntryEducationalType getEducationalType(Long key) {
		return repositoryEntryEducationalTypeDao.loadByKey(key);
	}
	
	public RepositoryEntryEducationalType getEducationalType(String identifier) {
		return repositoryEntryEducationalTypeDao.loadByIdentifier(identifier);
	}

	public boolean isEducationalTypeIdentifierAvailable(String identifier) {
		return repositoryEntryEducationalTypeDao.loadByIdentifier(identifier) == null;
	}

	public List<RepositoryEntryEducationalTypeStat> getEducationalTypeStats() {
		return repositoryEntryEducationalTypeDao.loadStats();
	}

	public void deleteEducationalType(RepositoryEntryEducationalType educationalType) {
		repositoryEntryDao.removeEducationalType(educationalType);
		repositoryEntryEducationalTypeDao.delete(educationalType);
	}

}