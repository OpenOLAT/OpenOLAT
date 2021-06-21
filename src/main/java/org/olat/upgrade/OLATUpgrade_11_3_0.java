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
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.image.Size;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMetadata;
import org.olat.modules.video.manager.VideoManagerImpl;
import org.olat.modules.video.model.VideoMetaImpl;
import org.olat.modules.video.model.VideoMetadataImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_11_3_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_11_3_0.class);

	private static final String FILENAME_VIDEO_METADATA_XML = "video_metadata.xml";
	
	private static final String VIDEO_XML = "VIDEO XML";
	private static final String VERSION = "OLAT_11.3.0";
	
	@Autowired
	private DB dbInstance;
	@Autowired 
	private VideoManager videoManager;
	
	public OLATUpgrade_11_3_0() {
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
		allOk &= upgradeVideoXml(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_11_3_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_11_3_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeVideoXml(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(VIDEO_XML)) {
			
			List<RepositoryEntry> entries = videoManager.getAllVideoRepoEntries(VideoFileResource.TYPE_NAME);
			for(RepositoryEntry entry:entries) {
				if(entry == null) continue;

				allOk &= processVideoResource(entry);
				dbInstance.commitAndCloseSession();
			}
			
			uhd.setBooleanDataValue(VIDEO_XML, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean processVideoResource(RepositoryEntry entry) {
		try {
			OLATResource videoResource = entry.getOlatResource();
			if (!videoManager.hasMasterContainer(videoResource)) {
				log.error("RepoEntry: " + entry.getKey() + " has no valid master container.");
				//log error but return true to proceed
				return true;
			}
			// update track files on file system
			VFSContainer masterContainer = videoManager.getMasterContainer(videoResource);
			if (isMetadataFileValid(videoResource)) {
				VideoMetadata metafromXML = readVideoMetadataFile(videoResource);
				for (Entry<String, String> track : metafromXML.getAllTracks().entrySet()) {
					VFSItem item = masterContainer.resolve(track.getValue());
					if (item != null && item instanceof VFSLeaf) {
						String path = VideoManagerImpl.TRACK + track.getKey() + VideoManagerImpl.DOT
								+ FilenameUtils.getExtension(track.getValue());
						//check if modified track file already exists
						if (masterContainer.resolve(path) == null) {
							VFSLeaf target = masterContainer.createChildLeaf(path);
							VFSManager.copyContent((VFSLeaf) item, target, false);
						}
					}
				}
			} else {
				log.error("RepoEntry: " + entry.getKey() + " has no valid Video Metadata XML file.");
			}
			// create meta data entries on database
			if (videoManager.hasVideoFile(videoResource)) {
				File videoFile = videoManager.getVideoFile(videoResource);
				String fileName = videoFile.getName();
				long size = videoFile.length();
				String format = FilenameUtils.getExtension(fileName);
				if (videoManager.hasVideoMetadata(videoResource)) {
					VideoMetaImpl entity = new VideoMetaImpl();
					entity.setVideoResource(videoResource);
					entity.setFormat(format);
					entity.setCreationDate(new Date());
					entity.setLastModified(new Date());
					Size resolution = videoManager.getVideoResolutionFromOLATResource(videoResource);
					entity.setHeight(resolution.getHeight());
					entity.setWidth(resolution.getWidth());
					entity.setSize(size);
					entity.setLength(entry.getExpenditureOfWork());
					dbInstance.getCurrentEntityManager().persist(entity);
				}
			} else {
				log.error("RepoEntry: " + entry.getKey() + " has no valid resource.");
			}
			return true;
		} catch (Exception e) {
			log.error("Update Metadata failed",e);
			return false;
		}
	}
	
	private boolean isMetadataFileValid(OLATResource videoResource) {
		VFSContainer baseContainer = FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		VFSLeaf metaDataFile = (VFSLeaf) baseContainer.resolve(FILENAME_VIDEO_METADATA_XML);
		try {
			VideoMetadata meta = (VideoMetadata) XStreamHelper.readObject(XStreamHelper.createXStreamInstance(), metaDataFile);
			return meta != null;
		} catch (Exception e) {
			return false;
		}
	}
	
	private VideoMetadata readVideoMetadataFile(OLATResource videoResource){
		VFSContainer baseContainer= FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		VFSLeaf metaDataFile = (VFSLeaf) baseContainer.resolve(FILENAME_VIDEO_METADATA_XML);
		try {
			return (VideoMetadata) XStreamHelper.readObject(XStreamHelper.createXStreamInstance(), metaDataFile);
		} catch (Exception e) {
			log.error("Error while parsing XStream file for videoResource::{}", videoResource, e);
			// return an empty, so at least it displays something and not an error
			VideoMetadata meta =  new VideoMetadataImpl();
			meta.setWidth(800);
			meta.setHeight(600);
			return meta;
		}
	}
}
