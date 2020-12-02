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
package org.olat.core.commons.services.doceditor.collabora.manager;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.collabora.CollaboraEditor;
import org.olat.core.commons.services.doceditor.collabora.CollaboraModule;
import org.olat.core.commons.services.doceditor.collabora.CollaboraRefreshDiscoveryEvent;
import org.olat.core.commons.services.doceditor.collabora.CollaboraService;
import org.olat.core.commons.services.doceditor.discovery.Action;
import org.olat.core.commons.services.doceditor.discovery.Discovery;
import org.olat.core.commons.services.doceditor.discovery.DiscoveryService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
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
 * Initial date: 5 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CollaboraServiceImpl implements CollaboraService, GenericEventListener {

	private static final Logger log = Tracing.createLoggerFor(CollaboraServiceImpl.class);
	
	private Discovery discovery;
	
	@Autowired
	private CollaboraModule collaboraModule;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private DiscoveryService discoveryService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private VFSLockManager lockManager;
	
	@PostConstruct
	private void init() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, REFRESH_EVENT_ORES);
	}
	
	@Override
	public boolean canUpdateContent(Access access, String fileId) {
		if (!fileId.equals(access.getMetadata().getUuid())) {
			return false;
		}
		VFSLeaf vfsLeaf = docEditorService.getVfsLeaf(access);
		return !isLockedForMe(vfsLeaf, access.getIdentity());
	}

	@Override
	public boolean updateContent(Access access, InputStream fileInputStream) {
		VFSLeaf vfsLeaf = docEditorService.getVfsLeaf(access);
		boolean updated = false;
		try {
			if(access.isVersionControlled() && vfsLeaf.canVersion() == VFSConstants.YES) {
				updated = vfsRepositoryService.addVersion(vfsLeaf, access.getIdentity(), "Collabora Online",
						fileInputStream);
			} else {
				updated = VFSManager.copyContent(fileInputStream, vfsLeaf, access.getIdentity());
			}
		} catch(Exception e) {
			log.error("", e);
		}
		if (updated) {
			refreshLock(vfsLeaf);
			vfsRepositoryService.resetThumbnails(vfsLeaf);
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
	public Discovery getDiscovery() {
		if (discovery == null) {
			String discoveryUrl = getDiscoveryUrl();
			discovery = discoveryService.getDiscovery(discoveryUrl);
			if (discovery != null) {
				log.info("Recieved new document editor discovery from {}", discoveryUrl);
			} else {
				log.warn("Not able to fetch new document editor discovery from {}", discoveryUrl);
			}
		}
		return discovery;
	}

	private String getDiscoveryUrl() {
		return collaboraModule.getBaseUrl() + discoveryService.getRegularDiscoveryPath();
	}
	
	@Override
	public void event(Event event) {
		if (event instanceof CollaboraRefreshDiscoveryEvent) {
			deleteDiscovery();
		}
	}

	private void deleteDiscovery() {
		discovery = null;
		log.info("Deleted document editor discovery. It will be refreshed with the next access.");
	}

	@Override
	public String getEditorBaseUrl(VFSMetadata vfsMetadata) {
		String suffix = FileUtils.getFileSuffix(vfsMetadata.getFilename());
		Action action = discoveryService.getAction(getDiscovery(), "edit", suffix);
		if (action == null) {
			action = discoveryService.getAction(getDiscovery(), "view", suffix);
		}
		return action != null? action.getUrlSrc(): null;
	}

	@Override
	public boolean accepts(String suffix, Mode mode) {
		boolean accepts = discoveryService.hasAction(getDiscovery(), "edit", suffix);
		if (!accepts && !Mode.EDIT.equals(mode)) {
			accepts = discoveryService.hasAction(getDiscovery(), "view", suffix);
		}
		return accepts;
	}

	@Override
	public boolean isLockNeeded(Mode mode) {
		return Mode.EDIT.equals(mode);
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity) {
		return lockManager.isLockedForMe(vfsLeaf, identity, VFSLockApplicationType.collaboration, CollaboraEditor.TYPE);
	}
	
	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, VFSMetadata metadata, Identity identity) {
		return lockManager.isLockedForMe(vfsLeaf, metadata, identity, VFSLockApplicationType.collaboration, CollaboraEditor.TYPE);
	}

	@Override
	public LockResult lock(VFSLeaf vfsLeaf, Identity identity) {
		return lockManager.lock(vfsLeaf, identity, VFSLockApplicationType.collaboration, CollaboraEditor.TYPE);
	}

	@Override
	public void deleteAccessAndUnlock(Access access, LockResult lock) {
		if (lock == null) return;
		
		boolean openOnce = docEditorService.getAccessCount(CollaboraEditor.TYPE, access.getMetadata(), access.getIdentity()) == 1;
		docEditorService.deleteAccess(access);
		
		if (openOnce) {
			VFSLeaf vfsLeaf = docEditorService.getVfsLeaf(access);
			lockManager.unlock(vfsLeaf, lock);
		}
	}

}
