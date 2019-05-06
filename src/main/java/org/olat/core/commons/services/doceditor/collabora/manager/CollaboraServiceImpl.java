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

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.collabora.CollaboraModule;
import org.olat.core.commons.services.doceditor.collabora.CollaboraRefreshDiscoveryEvent;
import org.olat.core.commons.services.doceditor.collabora.CollaboraService;
import org.olat.core.commons.services.doceditor.wopi.Access;
import org.olat.core.commons.services.doceditor.wopi.Action;
import org.olat.core.commons.services.doceditor.wopi.Discovery;
import org.olat.core.commons.services.doceditor.wopi.WopiService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
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

	private static final OLog log = Tracing.createLoggerFor(CollaboraServiceImpl.class);
	
	private static final String LOCK_APP = "collabora";

	private Discovery discovery;
	
	@Autowired
	private CollaboraModule collaboraModule;
	@Autowired
	private WopiService wopiService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private VFSLockManager lockManager;
	
	@PostConstruct
	private void init() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, REFRESH_EVENT_ORES);
	}
	
	@Override
	public VFSLeaf getVfsLeaf(Access access) {
		return wopiService.getVfsLeaf(access);
	}

	@Override
	public Access createAccess(VFSMetadata vfsMetadata, Identity identity, DocEditorSecurityCallback secCallback) {
		return wopiService.getOrCreateAccess(vfsMetadata, identity, secCallback, LOCK_APP, null);
	}

	@Override
	public Access getAccess(String accessToken) {
		return wopiService.getAccess(accessToken);
	}

	@Override
	public void deleteAccess(Access access) {
		if (access == null) return;
		
		wopiService.deleteAccess(access.getToken());
	}

	@Override
	public boolean canUpdateContent(Access access, String fileId) {
		if (!fileId.equals(access.getMetadata().getUuid())) {
			return false;
		}
		VFSLeaf vfsLeaf = wopiService.getVfsLeaf(access);
		return !isLockedForMe(vfsLeaf, access.getIdentity());
	}

	@Override
	public boolean updateContent(Access access, InputStream fileInputStream) {
		VFSLeaf vfsLeaf = wopiService.getVfsLeaf(access);
		boolean updated = false;
		try {
			if(access.isVersionControlled() && vfsLeaf.canVersion() == VFSConstants.YES) {
				updated = vfsRepositoryService.addVersion(vfsLeaf, access.getIdentity(), "Collabora Online",
						fileInputStream);
			} else {
				updated = VFSManager.copyContent(fileInputStream, vfsLeaf);
			}
		} catch(Exception e) {
			log.error("", e);
		}
		if (updated) {
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
	public Discovery getDiscovery() {
		if (discovery == null) {
			String discoveryUrl = getDiscoveryUrl();
			discovery = wopiService.getDiscovery(discoveryUrl);
			if (discovery != null) {
				log.info("Recieved new WOPI discovery from " + discoveryUrl);
			}
				log.warn("Not able to fetch new WOPI discovery from " + discoveryUrl);
		}
		return discovery;
	}

	private String getDiscoveryUrl() {
		return collaboraModule.getBaseUrl() + wopiService.getRegularDiscoveryPath();
	}
	
	@Override
	public void event(Event event) {
		if (event instanceof CollaboraRefreshDiscoveryEvent) {
			deleteDiscovery();
		}
	}

	private void deleteDiscovery() {
		discovery = null;
		log.info("Deleted WOPI discovery. It will be refreshed with the next access.");
	}

	@Override
	public String getEditorBaseUrl(VFSMetadata vfsMetadata) {
		String suffix = FileUtils.getFileSuffix(vfsMetadata.getFilename());
		Action action = wopiService.getAction(getDiscovery(), "edit", suffix);
		if (action == null) {
			action = wopiService.getAction(getDiscovery(), "view", suffix);
		}
		return action != null? action.getUrlSrc(): null;
	}

	@Override
	public boolean accepts(String suffix, Mode mode) {
		boolean accepts = wopiService.hasAction(getDiscovery(), "edit", suffix);
		if (!accepts && Mode.VIEW.equals(mode)) {
			accepts = wopiService.hasAction(getDiscovery(), "view", suffix);
		}
		return accepts;
	}

	@Override
	public boolean isLockNeeded(Mode mode) {
		return Mode.EDIT.equals(mode);
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity) {
		return lockManager.isLockedForMe(vfsLeaf, identity, VFSLockApplicationType.collaboration, LOCK_APP);
	}

	@Override
	public LockResult lock(VFSLeaf vfsLeaf, Identity identity) {
		return lockManager.lock(vfsLeaf, identity, VFSLockApplicationType.collaboration, LOCK_APP);
	}

	@Override
	public void unlock(VFSLeaf vfsLeaf, LockResult lock) {
		if (lock == null) return;
		
		lockManager.unlock(vfsLeaf, lock);
	}

}
