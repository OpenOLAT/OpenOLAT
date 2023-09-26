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
package org.olat.core.commons.services.doceditor.drawio.manager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.drawio.DrawioEditor;
import org.olat.core.commons.services.doceditor.drawio.DrawioModule;
import org.olat.core.commons.services.doceditor.drawio.DrawioService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.lock.LockInfo;
import org.olat.core.util.vfs.lock.LockResult;
import org.olat.restapi.security.RestSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DrawioServiceImpl implements DrawioService {
	
	private static final Logger log = Tracing.createLoggerFor(DrawioServiceImpl.class);
	
	@Autowired
	private DrawioModule drawioModule;
	@Autowired
	private VFSLockManager lockManager;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	@Override
	public String getFileInfoUrl(Access access) {
		return getFileUrl(access, "info").toString();
	}
	
	@Override
	public String getFileContentUrl(Access access) {
		return getFileUrl(access, "content").toString();
	}

	private StringBuilder getFileUrl(Access access, String path) {
		StringBuilder fileUrl = new StringBuilder();
		fileUrl.append(Settings.getServerContextPathURI());
		fileUrl.append(RestSecurityHelper.SUB_CONTEXT);
		fileUrl.append("/drawio/files/");
		fileUrl.append(access.getMetadata().getKey());
		fileUrl.append("/");
		fileUrl.append(path);
		fileUrl.append("?access_token=").append(access.getKey());
		return fileUrl;
	}
	
	@Override
	public String getContent(VFSLeaf vfsLeaf) {
		String suffix = FileUtils.getFileSuffix(vfsLeaf.getName());
		boolean isPng = "png".equalsIgnoreCase(suffix);
		boolean isSvg = "svg".equalsIgnoreCase(suffix);
		
		String xml;
		if (isPng) {
			xml = Base64.getEncoder().encodeToString(loadPng(vfsLeaf));
			xml = PNG_BASE64_PREFIX + xml;
		} else if (isSvg) {
			xml = FileUtils.load(vfsLeaf.getInputStream(), "utf-8");
			xml = Base64.getEncoder().encodeToString(xml.getBytes());
			xml = SVG_BASE64_PREFIX + xml;
		} else {
			xml = FileUtils.load(vfsLeaf.getInputStream(), "utf-8");
		}
		
		return xml;
	}
	
	private byte[] loadPng(VFSLeaf vfsLeaf) {
		try {
			return FileUtils.loadAsBytes(vfsLeaf.getInputStream());
		} catch (Exception e) {
			log.warn("Cannot load png file ", vfsLeaf.getRelPath());
			log.warn("", e);
		}
		return new byte[0];
	}
	
	@Override
	public boolean updateContent(Access access, Identity identity, byte[] content) {
		VFSLeaf vfsLeaf = docEditorService.getVfsLeaf(access);
		
		log.debug("Update content from draw.io: " + vfsLeaf.getRelPath());
		boolean updated = false;
		
		if (content.length > 0) {
			try (ByteArrayInputStream contentStream = new ByteArrayInputStream(content)) {
				if (access.isVersionControlled() && vfsLeaf.canVersion() == VFSConstants.YES) {
					updated = vfsRepositoryService.addVersion(vfsLeaf, identity, true, "drawio", contentStream);
				} else {
					updated = VFSManager.copyContent(contentStream, vfsLeaf, identity);
				}
			} catch (Exception e) {
				log.warn("Update content from draw.io failed. File: " + vfsLeaf.getRelPath());
				log.error("", e);
			}
		}
		
		if (updated) {
			docEditorService.documentSaved(access);
			vfsRepositoryService.resetThumbnails(vfsLeaf);
		}
		
		return updated;
	}
	
	private void saveStableVersion(VFSLeaf vfsLeaf, Identity identity) {
		if (vfsLeaf.canVersion() == VFSConstants.YES && vfsLeaf.getMetaInfo().getRevisionTempNr() != null) {
			try {
				File tmpFile = File.createTempFile("drawio", ".tmp");
				FileUtils.bcopy(vfsLeaf.getInputStream(), tmpFile, "drawio");
				try(InputStream temp = new FileInputStream(tmpFile)) {
					boolean updated = vfsRepositoryService.addVersion(vfsLeaf, identity, false, "draw.io", temp);
					if (!updated) {
						log.warn("Update content from draw.io failed. VFSMetadata (key={}), Identity: ({})", 
								vfsLeaf.getMetaInfo().getKey(), identity.getKey());
					}
				}
				Files.deleteIfExists(tmpFile.toPath());
			} catch(Exception e) {
				log.error("", e);
			}
		}
	}
	
	@Override
	public boolean isLockNeeded(Mode mode) {
		return Mode.EDIT.equals(mode);
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity) {
		return lockManager.isLockedForMe(vfsLeaf, identity, getVFSLockApplicationType(), DrawioEditor.TYPE);
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, VFSMetadata metadata, Identity identity) {
		return lockManager.isLockedForMe(vfsLeaf, metadata, identity, getVFSLockApplicationType(), DrawioEditor.TYPE);
	}

	@Override
	public LockResult lock(VFSLeaf vfsLeaf, Identity identity) {
		LockResult lock = lockManager.lock(vfsLeaf, identity, getVFSLockApplicationType(), DrawioEditor.TYPE);
		log.debug("Locked file. File name: " + vfsLeaf.getName() + ", Identity: " + identity);
		return lock;
	}

	@Override
	public void unlock(VFSLeaf vfsLeaf, Identity identity) {
		LockInfo lock = lockManager.getLock(vfsLeaf);
		if (lock != null && DrawioEditor.TYPE.equals(lock.getAppName())) {
			lock.getTokens().clear();
			lockManager.unlock(vfsLeaf, getVFSLockApplicationType());
			saveStableVersion(vfsLeaf, identity);
			log.debug("Unlocked file. File name: " + vfsLeaf.getName());
		}
	}
	
	private VFSLockApplicationType getVFSLockApplicationType() {
		return drawioModule.isCollaborationEnabled()? VFSLockApplicationType.collaboration: VFSLockApplicationType.exclusive;
	}

}
