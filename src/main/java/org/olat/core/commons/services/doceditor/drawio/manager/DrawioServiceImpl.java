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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.drawio.DrawioEditor;
import org.olat.core.commons.services.doceditor.drawio.DrawioService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSConstants;
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
 * Initial date: 21 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DrawioServiceImpl implements DrawioService {
	
	private static final Logger log = Tracing.createLoggerFor(DrawioServiceImpl.class);
	
	@Autowired
	private VFSLockManager lockManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	@Override
	public boolean updateContent(VFSLeaf vfsLeaf, Identity identity, String xml, boolean versionControlled) {
		log.debug("Update content from draw.io: " + vfsLeaf.getRelPath());
		boolean updated = false;
		
		if (StringHelper.containsNonWhitespace(xml)) {
			try (ByteArrayInputStream content = new ByteArrayInputStream(xml.getBytes())) {
				if (versionControlled && vfsLeaf.canVersion() == VFSConstants.YES) {
					updated = vfsRepositoryService.addVersion(vfsLeaf, identity, false, "drawio", content);
				} else {
					updated = VFSManager.copyContent(content, vfsLeaf, identity);
				}
			} catch (Exception e) {
				log.warn("Update content from draw.io failed. File: " + vfsLeaf.getRelPath());
				log.error("", e);
			}
		}
		
		return updated;
	}
	
	@Override
	public boolean isLockNeeded(Mode mode) {
		return Mode.EDIT.equals(mode);
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity) {
		return lockManager.isLockedForMe(vfsLeaf, identity, VFSLockApplicationType.collaboration, DrawioEditor.TYPE);
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, VFSMetadata metadata, Identity identity) {
		return lockManager.isLockedForMe(vfsLeaf, metadata, identity, VFSLockApplicationType.collaboration, DrawioEditor.TYPE);
	}

	@Override
	public LockResult lock(VFSLeaf vfsLeaf, Identity identity) {
		LockResult lock = lockManager.lock(vfsLeaf, identity, VFSLockApplicationType.collaboration, DrawioEditor.TYPE);
		log.debug("Locked file. File name: " + vfsLeaf.getName() + ", Identity: " + identity);
		return lock;
	}

	@Override
	public void unlock(VFSLeaf vfsLeaf) {
		LockInfo lock = lockManager.getLock(vfsLeaf);
		if (lock != null && DrawioEditor.TYPE.equals(lock.getAppName())) {
			lock.getTokens().clear();
			lockManager.unlock(vfsLeaf, VFSLockApplicationType.collaboration);
			log.debug("Unlocked file. File name: " + vfsLeaf.getName());
		}
	}

}
