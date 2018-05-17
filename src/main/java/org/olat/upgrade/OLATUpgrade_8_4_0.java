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
import java.util.List;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.DirectoryFilter;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.upgrade.model.BookmarkImpl;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * upgrade code for OpenOLAT 8.3.x -> OpenOLAT 8.4.0
 * - Upgrade bookmarks to new database structure
 * - Recalculate small user avatar images to new size 
 * 
 * <P>
 * Initial Date: 24.03.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_8_4_0 extends OLATUpgrade {

	private static final String TASK_BOOKMARKS = "Upgrade bookmarks";
	private static final String TASK_AVATARS = "Upgrade avatars";
	private static final String TASK_CHECK_MAIL_DELETED_USERS = "Check the mails of deleted users";
	private static final int BATCH_SIZE = 20;
	private static final String VERSION = "OLAT_8.4.0";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private FolderModule folderModule; // needed to initialize FolderConfig
	@Autowired	
	private ImageService imageHelper;
	@Autowired
	private UserDeletionManager userDeletionManager;


	public OLATUpgrade_8_4_0() {
		super();
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) {
				return false;
			}
		}
		
		boolean allOk = upgradeBookmarks(upgradeManager, uhd);
		allOk = (allOk && upgradeAvatars(upgradeManager, uhd));
		allOk &= checkMailOfDeletedUsers(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_8_4_0 successfully!");
		} else {
			log.audit("OLATUpgrade_8_4_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeBookmarks(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_BOOKMARKS)) {
			int counter = 0;
			List<BookmarkImpl> bookmarks;
			do {
				bookmarks = findBookmarks(counter, BATCH_SIZE);
				for(BookmarkImpl bookmark:bookmarks) {
					processBookmarks(bookmark);
				}
				counter += bookmarks.size();
				log.audit("Processed context: " + bookmarks.size());
				dbInstance.intermediateCommit();
			} while(bookmarks.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(TASK_BOOKMARKS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private void processBookmarks(BookmarkImpl bookmark) {
		String resName = bookmark.getOlatrestype();
		Long resId = bookmark.getOlatreskey();
		Identity owner = bookmark.getOwner();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName, resId);
		String businessPath = null;
		if("RepositoryEntry".equals(resName) || "CatalogEntry".equals(resName)) {
			businessPath = "[" + resName + ":" + resId + "]";
		}
		markManager.setMark(ores, owner, null, businessPath);
	}
	
	private List<BookmarkImpl> findBookmarks(int counter, int batchSize) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bookmark from ").append(BookmarkImpl.class.getName()).append(" as bookmark")
		  .append(" inner join fetch bookmark.owner as identity")
		  .append(" inner join fetch identity.user as user");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BookmarkImpl.class)
				.setFirstResult(counter)
				.setMaxResults(batchSize)
				.getResultList();
	}

	private boolean upgradeAvatars(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean success = false;
		if (!uhd.getBooleanDataValue(TASK_AVATARS)) {
			// commit because this task can take a while 
			dbInstance.intermediateCommit();

			long counter = 0;
			File root = new File(FolderConfig.getCanonicalRoot() + FolderConfig.getUserHomePages());
			if (root.exists()) {
				File[] users = root.listFiles(new DirectoryFilter());
				log.audit("Starting with avatar images upgrade process for " + users.length + " users");
				for (File homepage : users) {
					File portraitDir = new File(homepage, "portrait");
					if (portraitDir.exists()) {
						File[] files = portraitDir.listFiles();
						for (File file : files) {
							// search for large portrait file and scald down to small size, overwriting already existing files
							if (file.getName().startsWith("portrait_big")) {
								String extension = FileUtils.getFileSuffix(file.getName());
								if(!StringHelper.containsNonWhitespace(extension)) {
									extension = "png";
								}
								File pSmallFile = new File(portraitDir, "portrait_small" + "." + extension);
								imageHelper.scaleImage(file, extension, pSmallFile, DisplayPortraitManager.WIDTH_PORTRAIT_SMALL, DisplayPortraitManager.WIDTH_PORTRAIT_SMALL, false);								
								counter++;
								break;
							}
						}
					}
					// else skip, no portrait, nothing to do
					if (counter % 1000 == 0) {
						// log something once in a while
						log.audit("Upgrade avatar images: " + counter + " user processed so far, please wait");						
					}
				}
				log.audit("Upgrade avatar images finished, updated " + counter + " avatar images");
				success = true;
			} else {
				log.warn("Root folder for user homepages not found::" + root.getAbsolutePath());
			}			
		}
		uhd.setBooleanDataValue(TASK_AVATARS, success);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		return success;
	}
	
	private boolean checkMailOfDeletedUsers(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_CHECK_MAIL_DELETED_USERS)) {
			int counter = 0;
			List<Identity> deletedIdentities;
			do {
				SearchIdentityParams params = new SearchIdentityParams();
				params.setStatus(new Integer(Identity.STATUS_DELETED));
				
				deletedIdentities = securityManager.getIdentitiesByPowerSearch(params, counter, BATCH_SIZE);
				for(Identity deletedIdentity:deletedIdentities) {
					User deletedUser = deletedIdentity.getUser();
					boolean changed = processDeletedIdentityEmail(deletedUser, UserConstants.EMAIL);
					changed |= processDeletedIdentityEmail(deletedUser, UserConstants.INSTITUTIONALEMAIL);
					if(changed) {
						UserManager.getInstance().updateUser(deletedUser);	
						log.audit("Update emails of deleted identity: " + deletedIdentity.getName() + " with key: " + deletedIdentity.getKey());
					}
				}
				counter += deletedIdentities.size();
				log.audit("Processed deleted identities: " + deletedIdentities.size());
				dbInstance.intermediateCommit();
			} while(deletedIdentities.size() == BATCH_SIZE);
			
			uhd.setBooleanDataValue(TASK_CHECK_MAIL_DELETED_USERS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private boolean processDeletedIdentityEmail(User deletedUser, String property) {
		String email = deletedUser.getProperty(property, null);
		if(StringHelper.containsNonWhitespace(email)) {
			deletedUser.setProperty(property, null);
			return true;
		}
		return false;
	}
}