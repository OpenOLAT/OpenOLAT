/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.user.UserImpl;
import org.olat.user.UserManagerImpl;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Apr 10, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.comm
 *
 */
public class OLATUpgrade_20_0_2 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_0_2.class);
	
	private static final String VERSION = "OLAT_20.0.2";

	private static final String MOVE_USER_PORTRAIT = "MOVE USER PORTRAIT";
	private static final String INIT_USER_INITIALS_COLOR = "INIT USER INITIALS COLOR";
	private static final String CURRICULUM_IMPLEMENTATIONS_BOOKMARKS = "CURRICULUM IMPLEMENTATION BOOKMARKS";
	
	private static final int BATCH_SIZE = 1000;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserPortraitService userPortraitService;
	@Autowired
	private UserManagerImpl userManager;
	@Autowired
	private MarkManager markManager;

	public OLATUpgrade_20_0_2() {
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
		allOk &= moveUserPortrait(upgradeManager, uhd);
		allOk &= initUserInitialsColor(upgradeManager, uhd);
		allOk &= initBookmarksImplementations(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_0_2 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_0_2 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean moveUserPortrait(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MOVE_USER_PORTRAIT)) {
			try {
				log.info("Start move user portraits.");
				
				int counter = 0;
				int newPortraits = 0;
				List<Identity> identities;
				do {
					identities = getIdentities(counter, BATCH_SIZE);
					if(!identities.isEmpty()) {
						for(int i=0; i<identities.size(); i++) {
							Identity identity = identities.get(i);
							if(moveUserPortrait(identity)) {
								newPortraits++;
							}
							if(i % 25 == 0) {
								dbInstance.commitAndCloseSession();
							}
						}
						counter += identities.size();
						log.info(Tracing.M_AUDIT, "Portrait searched and moved of {} users", counter);
					}
					dbInstance.commitAndCloseSession();
				} while (!identities.isEmpty());
				
				log.info("Moved portraits of {} users.", newPortraits);
				
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
		
			uhd.setBooleanDataValue(MOVE_USER_PORTRAIT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private boolean moveUserPortrait(Identity identity) {
		boolean portraitAvailable = false;
		
		if (!StringHelper.containsNonWhitespace(identity.getUser().getPortraitPath())) {
			File masterPortrait = getImage(identity.getName(), "portrait_master");
			if (masterPortrait != null && masterPortrait.exists()) {
				portraitAvailable = true;
				userPortraitService.storePortraitImage(identity, identity, masterPortrait, masterPortrait.getName());
				deleteImages(identity, "portrait");
			}
		}
		
		if (!StringHelper.containsNonWhitespace(identity.getUser().getLogoPath())) {
			File masterLogo = getImage(identity.getName(), "logo_master");
			if (masterLogo != null && masterLogo.exists()) {
				portraitAvailable = true;
				userPortraitService.storeLogoImage(identity, identity, masterLogo, masterLogo.getName());
				deleteImages(identity, "logo");
			}
		}
		
		File portraitDir = getPortraitDir(identity.getName());
		if (portraitDir != null && portraitDir.exists()) {
			LocalFolderImpl portraitCont = new LocalFolderImpl(portraitDir);
			if (portraitCont.getItems(new VFSSystemItemFilter()).isEmpty()) {
				portraitCont.deleteSilently();
			}
		}
		
		return portraitAvailable;
	}
	
	private File getImage(String username, String prefix) {
		File portraitDir = getPortraitDir(username);
		if(portraitDir != null && portraitDir.exists()) {
			File[] portraits = portraitDir.listFiles();
			if(portraits.length > 0) {
				for(File file:portraits) {
					if(file.getName().startsWith(prefix)) {
						return file;
					}
				}
			}
		}
		return null;
	}
	
	private File getPortraitDir(String username) {
		String portraitPath = FolderConfig.getCanonicalRoot() + FolderConfig.getUserHomePage(username); 
		File portraitDir = new File(portraitPath, "portrait");
		return portraitDir;
	}
	
	private void deleteImages(Identity identity, String prefix) {
		File directory = getPortraitDir(identity.getName());
		if(directory != null && directory.exists()) {
			for(File file:directory.listFiles()) {
				String filename = file.getName();
				if(filename.startsWith(prefix)) {
					// Use VFS to delete metadata and thumbnails as well
					new LocalFileImpl(file).deleteSilently();
				}
			}
		}
	}
	
	private boolean initUserInitialsColor(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INIT_USER_INITIALS_COLOR)) {
			try {
				log.info("Start init user initials color.");
				
				int counter = 0;
				int newPortraits = 0;
				List<Identity> identities;
				do {
					identities = getIdentities(counter, BATCH_SIZE);
					if(!identities.isEmpty()) {
						for(int i=0; i<identities.size(); i++) {
							Identity identity = identities.get(i);
							if(initUserInitialsColor(identity)) {
								newPortraits++;
							}
							if(i % 25 == 0) {
								dbInstance.commitAndCloseSession();
							}
						}
						counter += identities.size();
						log.info("Checked the initials color of {} users.", counter);
					}
					dbInstance.commitAndCloseSession();
				} while (!identities.isEmpty());
				
				log.info("User initials color {} users initialized.", newPortraits);
				
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
		
			uhd.setBooleanDataValue(INIT_USER_INITIALS_COLOR, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean initUserInitialsColor(Identity identity) {
		if (StringHelper.containsNonWhitespace(identity.getUser().getInitialsCssClass())) {
			return false;
		}
		
		if (identity.getUser() instanceof UserImpl impl) {
			userManager.getRandomInitialsColorCss();
			impl.setInitialsCssClass(userManager.getRandomInitialsColorCss());
			userManager.updateUser(identity, impl);
			return true;
		}
		
		return false;
	}

	private List<Identity> getIdentities(int offset, int maxResults) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident");
		sb.append(" inner join fetch ident.user user");
		sb.append(" order by ident.key");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setFirstResult(offset)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private boolean initBookmarksImplementations(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CURRICULUM_IMPLEMENTATIONS_BOOKMARKS)) {
			List<Long> implementationsKeys = getImplementationsKeys();
			log.info("Start init bookmarks for implementations.");
			int count = 0;
			for(Long implementationKey:implementationsKeys) {
				List<Identity> members = getMembersInImplementations(implementationKey);
				initBookmarks(members, implementationKey);
				List<Identity> reservations = getReservationsInImplementations(implementationKey);
				initBookmarks(reservations, implementationKey);
				
				if(++count % 25 == 0) {
					log.info("Bookmarks for {} implementations on {}.", count, implementationsKeys.size());	
				}
				dbInstance.commitAndCloseSession();
			}
			log.info("End init bookmarks for {} implementations.", implementationsKeys.size());
			uhd.setBooleanDataValue(CURRICULUM_IMPLEMENTATIONS_BOOKMARKS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void initBookmarks(List<Identity> members, Long implementationKey) {
		OLATResourceable item = OresHelper.createOLATResourceableInstance(CurriculumElement.class, implementationKey);
		
		int count = 0;
		for(Identity member:members) {
			if(!markManager.isMarked(item, member, null)) {
				markManager.setMark(item, member, null, "[MyCoursesSite:0][Implementation:" + implementationKey + "]");
			}
			if(count++ % 25 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
	}
	
	private List<Identity> getReservationsInImplementations(Long implementationKey) {
		String query = """
			select distinct ident from curriculumelement el
			inner join el.group baseGroup
			inner join resourcereservation as reservation on (reservation.resource.key=el.resource.key)
			inner join reservation.identity ident
			where el.materializedPathKeys like :materializedPath""";

		return dbInstance.getCurrentEntityManager().createQuery(query, Identity.class)
				.setParameter("materializedPath", "/" + implementationKey + "/%")
				.getResultList();
	}
	
	private List<Identity> getMembersInImplementations(Long implementationKey) {
		String query = """
			select distinct member from curriculumelement el
			inner join el.group baseGroup
			inner join baseGroup.members membership
			inner join membership.identity member
			where membership.role=:role and el.materializedPathKeys like :materializedPath""";

		return dbInstance.getCurrentEntityManager().createQuery(query, Identity.class)
				.setParameter("materializedPath", "/" + implementationKey + "/%")
				.setParameter("role", GroupRoles.participant.name())
				.getResultList();
	}
	
	private List<Long> getImplementationsKeys() {
		String sb = """
				select el.key from curriculumelement el
				where el.parent.key is null
				""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, Long.class)
				.getResultList();
	}
}
