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
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.commons.services.doceditor.wopi.WopiService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
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
	
	private static DateFormat LAST_MODIFIED = new SimpleDateFormat("yyyyMMddHHmmSS");

	@Autowired
	private WopiService wopiService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private BaseSecurityManager securityManager;

	@Override
	public boolean fileExists(String fileId) {
		return wopiService.fileExists(fileId);
	}

	@Override
	public File getFile(String fileId) {
		return wopiService.getFile(fileId);
	}

	@Override
	public boolean canUpdateContent(String fileId, Identity identity) {
		return true;
		//TODO uh check lock
//		VFSLeaf vfsLeaf = wopiService.getVfsLeaf(fileId);
//		return !isLockedForMe(vfsLeaf, access.getIdentity());
	}

	@Override
	public boolean updateContent(String fileId, Identity identity, String url) {
		VFSLeaf vfsLeaf = wopiService.getVfsLeaf(fileId);
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
			log.debug("File updated. File ID: " + fileId);
			//TODO uh lock
//			refreshLock(vfsLeaf);
		}
		return updated;
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
