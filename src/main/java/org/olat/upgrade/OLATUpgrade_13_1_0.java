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
package org.olat.upgrade;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_13_1_0 extends OLATUpgrade {
	
	private static final String VERSION = "OLAT_13.1.0";
	private static final String CLEAN_META_TMP = "CLEAN META TMP";
	private static final String CLEAN_META_PATH_ERRORS = "CLEAN META PATH ERRORS";
	private static final String CLEAN_META_FORUM = "CLEAN META FORUM";
	private static final String CLEAN_META_COURSES = "CLEAN META COURSES";
	private static final String CLEAN_META_RESOURCES = "CLEAN META RESOURCES";
	private static final String CLEAN_META_HOMES = "CLEAN META HOMES";
	private static final String CLEAN_META_HOMEPAGES = "CLEAN META HOMEPAGES";
	private static final String CLEAN_META_CTS = "CLEAN META CTS";
	
	@Autowired
	private DB dbInstance;
	
	public OLATUpgrade_13_1_0() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}
		
		
		boolean allOk = true;
		allOk &= cleanMetadataTmp(upgradeManager, uhd);
		allOk &= cleanMetadataPathErrors(upgradeManager, uhd);
		allOk &= cleanMetadataForum(upgradeManager, uhd);
		allOk &= cleanMetadataCourses(upgradeManager, uhd);
		allOk &= cleanMetadataRepository(upgradeManager, uhd);
		allOk &= cleanMetadataHomes(upgradeManager, uhd);
		allOk &= cleanMetadataHomepages(upgradeManager, uhd);
		allOk &= cleanMetadataCollaborationTools(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_13_1_0 successfully!");
		} else {
			log.audit("OLATUpgrade_13_1_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	/**
	 * This directory .meta/tmp/ is not used
	 * 
	 * @param upgradeManager The upgrade manager
	 * @param uhd The upgrade history
	 * @return true if successful
	 */
	private boolean cleanMetadataTmp(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CLEAN_META_TMP)) {
			try {
				dbInstance.commitAndCloseSession();
				
				File tmp = new File(FolderConfig.getCanonicalMetaRoot(), "tmp");
				deleteDirectory(tmp);
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}
			uhd.setBooleanDataValue(CLEAN_META_TMP, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	/**
	 * These directories was path errors:
	 * <ul>
	 * 	<li>.metaforum
	 * 	<li>.metalevels
	 * 	<li>.metataxonomy
	 * </ul>
	 * 
	 * @param upgradeManager The upgrade manager
	 * @param uhd The upgrade history
	 * @return true if successful
	 */
	private boolean cleanMetadataPathErrors(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CLEAN_META_PATH_ERRORS)) {
			try {
				dbInstance.commitAndCloseSession();
				
				File metaForum = new File(FolderConfig.getCanonicalRoot(), ".metaforum");
				deleteDirectory(metaForum);
				File metaLevels = new File(FolderConfig.getCanonicalRoot(), ".metalevels");
				deleteDirectory(metaLevels);
				File metaTaxonomy = new File(FolderConfig.getCanonicalRoot(), ".metataxonomy");
				deleteDirectory(metaTaxonomy);
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}
			uhd.setBooleanDataValue(CLEAN_META_PATH_ERRORS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void deleteDirectory(File dir) {
		if(dir.exists() && dir.isDirectory()) {
			FileUtils.deleteDirsAndFiles(dir, true, true);
		}
	}
	
	/**
	 * Remove metadata of deleted forums
	 * 
	 * @param upgradeManager The upgrade manager
	 * @param uhd The upgrade history
	 * @return true if successful
	 */
	private boolean cleanMetadataForum(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CLEAN_META_FORUM)) {
			try {
				int count = 0;
				Set<Long> keys = getAllForumKeys();
				dbInstance.commitAndCloseSession();
				
				File forumsMetadata = new File(FolderConfig.getCanonicalMetaRoot(), "forum");
				String[] directories = forumsMetadata.list();
				if(directories != null) {
					for(String directory:directories) {
						if(!directory.startsWith(".") && StringHelper.isLong(directory)
								&& !keys.contains(Long.valueOf(directory))) {
							deleteDirectory(new File(forumsMetadata, directory));
							count++;
						}
					}
				}
				log.info(count + " metadata directories of deleted forums cleaned");
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}
			uhd.setBooleanDataValue(CLEAN_META_FORUM, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private Set<Long> getAllForumKeys() {
		String query = "select f.key from forum as f";
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.getResultList();
		return new HashSet<>(keys);
	}
	
	/**
	 * Remove metadata of deleted courses
	 * 
	 * @param upgradeManager The upgrade manager
	 * @param uhd The upgrade history
	 * @return true if successful
	 */
	private boolean cleanMetadataCourses(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CLEAN_META_COURSES)) {
			try {
				int count = 0;
				Set<Long> keys = getAllCourseResourceIds();
				dbInstance.commitAndCloseSession();
				
				File coursesMetadata = new File(FolderConfig.getCanonicalMetaRoot(), "course");
				String[] directories = coursesMetadata.list();
				if(directories != null) {
					for(String directory:directories) {
						if(!directory.startsWith(".") && StringHelper.isLong(directory)
								&& !keys.contains(Long.valueOf(directory))) {
							deleteDirectory(new File(coursesMetadata, directory));
							count++;
						}
					}
				}
				log.info(count + " metadata directories of deleted courses cleaned");
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}
			uhd.setBooleanDataValue(CLEAN_META_COURSES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private Set<Long> getAllCourseResourceIds() {
		StringBuilder sb = new StringBuilder();
		sb.append("select res.resId from repositoryentry as v")
		  .append(" inner join v.olatResource as res")
		  .append(" where res.resName='CourseModule'");
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.getResultList();
		return new HashSet<>(keys);
	}
	
	/**
	 * Remove metadata of deleted repository entries
	 * 
	 * @param upgradeManager The upgrade manager
	 * @param uhd The upgrade history
	 * @return true if successful
	 */
	private boolean cleanMetadataRepository(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CLEAN_META_RESOURCES)) {
			try {
				int count = 0;
				Set<Long> keys = getAllResourceIds();
				dbInstance.commitAndCloseSession();
				
				File repositoryMetadata = new File(FolderConfig.getCanonicalMetaRoot(), "repository");
				String[] directories = repositoryMetadata.list();
				if(directories != null) {
					for(String directory:directories) {
						if(!directory.startsWith(".") && StringHelper.isLong(directory)
								&& !keys.contains(Long.valueOf(directory))) {
							deleteDirectory(new File(repositoryMetadata, directory));
							count++;
						}
					}
				}
				log.info(count + " metadata directories of deleted repository resources cleaned");
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}
			uhd.setBooleanDataValue(CLEAN_META_RESOURCES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private Set<Long> getAllResourceIds() {
		StringBuilder sb = new StringBuilder();
		sb.append("select res.resId from repositoryentry as v")
		  .append(" inner join v.olatResource as res");
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.getResultList();
		return new HashSet<>(keys);
	}
	
	/**
	 * Remove metadata of deleted users in homes and homepages
	 * 
	 * @param upgradeManager The upgrade manager
	 * @param uhd The upgrade history
	 * @return true if successful
	 */
	private boolean cleanMetadataHomes(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CLEAN_META_HOMES)) {
			try {
				int count = 0;
				Set<String> usernames = getAllUsernames();
				dbInstance.commitAndCloseSession();
				
				File homesMetadata = new File(FolderConfig.getCanonicalMetaRoot(), "homes");
				String[] homes = homesMetadata.list();
				if(homes != null) {
					for(String home:homes) {
						if(!home.startsWith(".") && !usernames.contains(home)) {
							deleteDirectory(new File(homesMetadata, home));
							count++;
						}
					}
				}
				log.info(count + " metadata directories of user homes cleaned");
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}
			uhd.setBooleanDataValue(CLEAN_META_HOMES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	/**
	 * Remove metadata of deleted users in homes and homepages
	 * 
	 * @param upgradeManager The upgrade manager
	 * @param uhd The upgrade history
	 * @return true if successful
	 */
	private boolean cleanMetadataHomepages(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CLEAN_META_HOMEPAGES)) {
			try {
				int count = 0;
				Set<String> usernames = getAllUsernames();
				dbInstance.commitAndCloseSession();
				
				File homepagesMetadata = new File(FolderConfig.getCanonicalMetaRoot(), "homepages");
				String[] homepages = homepagesMetadata.list();
				if(homepages != null) {
					for(String homepage:homepages) {
						if(!homepage.startsWith(".") && !usernames.contains(homepage)) {
							deleteDirectory(new File(homepagesMetadata, homepage));
							count++;
						}
					}
				}
				log.info(count + " metadata directories of user homepages cleaned");
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}
			uhd.setBooleanDataValue(CLEAN_META_HOMEPAGES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private Set<String> getAllUsernames() {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident.name from ").append(IdentityImpl.class.getName()).append(" as ident");
		List<String> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.getResultList();
		return new HashSet<>(keys);
	}
	
	/**
	 * Remove metadata of deleted groups collaboration tools (folders and wikis)
	 * 
	 * @param upgradeManager The upgrade manager
	 * @param uhd The upgrade history
	 * @return true if successful
	 */
	private boolean cleanMetadataCollaborationTools(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CLEAN_META_CTS)) {
			try {
				int count = 0;
				Set<Long> groupIds = getAllGroupIds();
				dbInstance.commitAndCloseSession();
				
				File ctsMetadata = new File(FolderConfig.getCanonicalMetaRoot(), "cts");
				File ctsFoldersMetadata = new File(new File(ctsMetadata, "folders"), "BusinessGroup");
				String[] ctsFolders = ctsFoldersMetadata.list();
				if(ctsFolders != null) {
					for(String ctsFolder:ctsFolders) {
						if(!ctsFolder.startsWith(".") && StringHelper.isLong(ctsFolder) &&  !groupIds.contains(Long.valueOf(ctsFolder))) {
							deleteDirectory(new File(ctsFoldersMetadata, ctsFolder));
							count++;
						}
					}
				}
				
				File ctsWikisMetadata = new File(new File(ctsMetadata, "wikis"), "BusinessGroup");
				String[] ctsWikis = ctsWikisMetadata.list();
				if(ctsWikis != null) {
					for(String ctsWiki:ctsWikis) {
						if(!ctsWiki.startsWith(".") && StringHelper.isLong(ctsWiki) &&  !groupIds.contains(Long.valueOf(ctsWiki))) {
							deleteDirectory(new File(ctsFoldersMetadata, ctsWiki));
							count++;
						}
					}
				}
				
				log.info(count + " metadata directories of collaboration tools of deleted groups cleaned");
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}
			uhd.setBooleanDataValue(CLEAN_META_CTS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private Set<Long> getAllGroupIds() {
		String query = "select grp.key from businessgroup as grp";
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.getResultList();
		return new HashSet<>(keys);
	}
}
