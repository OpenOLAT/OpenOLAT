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
package org.olat.core.commons.services.doceditor.onlyoffice.manager;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.lock.LockInfo;
import org.olat.core.util.vfs.lock.LockResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OnlyOfficeServiceImpl implements OnlyOfficeService {

	private static final OLog log = Tracing.createLoggerFor(OnlyOfficeServiceImpl.class);
	
	private static final String LOCK_APP_NAME = "onlyoffice";

	private static DateFormat LAST_MODIFIED = new SimpleDateFormat("yyyyMMddHHmmSS");

	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private VFSLockManager lockManager;
	@Autowired
	private BaseSecurityManager securityManager;

	@Override
	public boolean fileExists(String fileId) {
		return vfsRepositoryService.getItemFor(fileId) != null? true: false;
	}

	@Override
	public File getFile(String fileId) {
		VFSLeaf vfsLeaf = getVfsLeaf(fileId);
		if (vfsLeaf != null) {
			String uri = vfsLeaf.getMetaInfo().getUri();
			try {
				return Paths.get(new URL(uri).toURI()).toFile();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return null;
	}

	@Override
	public VFSLeaf getVfsLeaf(String fileId) {
		VFSItem item = vfsRepositoryService.getItemFor(fileId);
		if (item instanceof VFSLeaf) {
			return (VFSLeaf) item;
		}
		return null;
	}

	@Override
	public boolean canUpdateContent(VFSLeaf vfsLeaf, Identity identity, String documentKey) {
		String currentDocumentKey = getDocumentKey(vfsLeaf.getMetaInfo());
		return currentDocumentKey.equals(documentKey) && !isLockedForMe(vfsLeaf, identity);
	}

	@Override
	public boolean updateContent(VFSLeaf vfsLeaf, Identity identity, String url) {
		boolean updated = false;
		try (InputStream in = new URL(url).openStream()) {
			//TODO uh versionCntrolled
			if(//access.isVersionControlled() && 
					vfsLeaf.canVersion() == VFSConstants.YES) {
				updated = vfsRepositoryService.addVersion(vfsLeaf, identity, "OnlyOffice", in);
			} else {
				updated = VFSManager.copyContent(in, vfsLeaf);
			}
		} catch(Exception e) {
			log.error("", e);
		}
		if (updated) {
			log.debug("File updated. File name: " + vfsLeaf.getName());
			refreshLock(vfsLeaf);
		}
		return updated;
	}
	
	private void refreshLock(VFSLeaf vfsLeaf) {
		LockInfo lock = lockManager.getLock(vfsLeaf);
		if (lock != null) {
			long inADay = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
			lock.setExpiresAt(inADay);
		}
	}
	
	@Override
	public boolean isLockNeeded(Mode mode) {
		return Mode.EDIT.equals(mode);
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity) {
		return lockManager.isLockedForMe(vfsLeaf, identity, VFSLockApplicationType.collaboration, LOCK_APP_NAME);
	}

	@Override
	public LockResult lock(VFSLeaf vfsLeaf, Identity identity) {
		LockResult lock = lockManager.lock(vfsLeaf, identity, VFSLockApplicationType.collaboration, LOCK_APP_NAME);
		log.debug("Locked file. File name: " + vfsLeaf.getName() + ", Identity: " + identity);
		return lock;
	}

	@Override
	public void unlock(VFSLeaf vfsLeaf) {
		LockInfo lock = lockManager.getLock(vfsLeaf);
		if (lock != null && LOCK_APP_NAME.equals(lock.getAppName())) {
			lock.getTokens().clear();
			lockManager.unlock(vfsLeaf, VFSLockApplicationType.collaboration);
			log.debug("Unlocked file. File name: " + vfsLeaf.getName());
		}
	}

	@Override
	public boolean isSupportedFormat(String suffix, Mode mode) {
		return Formats.isSupportedFormat(suffix, mode);
	}

	@Override
	public String getEditorDocumentType(String suffix) {
		return Formats.getEditorType(suffix);
	}

	@Override
	public String getDocumentKey(VFSMetadata metadata) {
		String lastModified = LAST_MODIFIED.format(metadata.getLastModified());
		return metadata.getUuid() + "-" + lastModified;
	}

	@Override
	public Identity getIdentity(String identityId) {
		try {
			Long identityKey = Long.valueOf(identityId);
			return securityManager.loadIdentityByKey(identityKey);
		} catch (NumberFormatException e) {
			log.warn("Try to load identity with key " + identityId, e);
		}
		return null;
	}
	
}
