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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.manager.IdentityDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.manager.VFSRepositoryServiceImpl;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.video.VideoManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_14_2_10 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_14_2_10.class);

	private static final String VERSION = "OLAT_14.2.10";
	private static final String VIDEO_VFS_METADATA = "VIDEO VFS METADATA";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private IdentityDAO identityDao;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private VFSRepositoryServiceImpl vfsRepositoryService;

	public OLATUpgrade_14_2_10() {
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
		allOk &= createVideoVfsMetadata(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_14_2_10 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_14_2_10 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean createVideoVfsMetadata(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(VIDEO_VFS_METADATA)) {
			
			List<String> videoTypes = Arrays.asList(VideoFileResource.TYPE_NAME);
			List<OLATResource> videos = OLATResourceManager.getInstance().findResourceByTypes(videoTypes);
			log.info("Number of video to upgrade: {}", videos.size());
			for (OLATResource ores : videos) {
				createVideoVfsMetadata(ores);
				dbInstance.commitAndCloseSession();
			}

			uhd.setBooleanDataValue(VIDEO_VFS_METADATA, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void createVideoVfsMetadata(OLATResource ores) {
		try {
			File resourceDir = FileResourceManager.getInstance().getFileResourceRoot(ores);
			vfsRepositoryService.migrateDirectories(resourceDir);
			
			VFSLeaf masterFile = videoManager.getMasterVideoFile(ores);
			if(masterFile == null) {
				return;
			}
			
			VFSMetadata vfsMetadata = masterFile.getMetaInfo();
			if(vfsMetadata != null && vfsMetadata.getFileInitializedBy() == null) {
				RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(ores, false);
				if(entry != null && StringHelper.containsNonWhitespace(entry.getInitialAuthor())) {
					Identity author = identityDao.findIdentityByName(entry.getInitialAuthor());
					if(vfsMetadata instanceof VFSMetadataImpl && author != null) {
						((VFSMetadataImpl)vfsMetadata).setFileInitializedBy(author);
						vfsRepositoryService.updateMetadata(vfsMetadata);
					}
				}
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
