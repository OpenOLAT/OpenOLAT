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
package org.olat.modules.wopi.manager;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.olat.core.commons.services.vfs.VFSLeafEditor.Mode;
import org.olat.core.commons.services.vfs.VFSLeafEditorSecurityCallback;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.wopi.Access;
import org.olat.modules.wopi.Action;
import org.olat.modules.wopi.App;
import org.olat.modules.wopi.Discovery;
import org.olat.modules.wopi.NetZone;
import org.olat.modules.wopi.WopiService;
import org.olat.modules.wopi.model.AccessImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class WopiServiceImpl implements WopiService {
	
	private static final OLog log = Tracing.createLoggerFor(WopiServiceImpl.class);
	
	private CacheWrapper<String, Access> accessCache;
	
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private VFSRepositoryService vfsService;

	@PostConstruct
	public void init() {
		accessCache = coordinator.getCoordinator().getCacher().getCache(WopiService.class.getSimpleName(), "access");
	}
	
	@Override
	public boolean fileExists(String fileId) {
		return vfsService.getItemFor(fileId) != null? true: false;
	}

	@Override
	public File getFile(String fileId) {
		VFSItem item = vfsService.getItemFor(fileId);
		if (item instanceof VFSLeaf) {
			VFSLeaf vfsLeaf = (VFSLeaf) item;
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
	public VFSMetadata getMetadata(String fileId) {
		File file = getFile(fileId);
		return vfsService.getMetadataFor(file);
	}

	@Override
	public Access createAccess(VFSMetadata vfsMetadata, Identity identity, VFSLeafEditorSecurityCallback secCallback) {
		String token = UUID.randomUUID().toString().replaceAll("-", "");
		String fileId = vfsMetadata.getUuid();
		
		AccessImpl access = new AccessImpl();
		access.setToken(token);
		access.setFileId(fileId);
		access.setIdentity(identity);
		access.setCanEdit(Mode.EDIT.equals(secCallback.getMode()));
		access.setCanClose(secCallback.canClose());
		accessCache.put(token, access);
		return access;
	}

	@Override
	public Access getAccess(String accessToken) {
		return accessCache.get(accessToken);
	}
	
	@Override
	public void deleteAccess(String accessToken) {
		accessCache.remove(accessToken);
	}

	@Override
	public boolean hasAction(Discovery discovery, String actionName, String suffix) {
		Action action = getAction(discovery, actionName, suffix);
		return action != null? true: false;
	}

	@Override
	public Action getAction(Discovery discovery, String actionName, String suffix) {
		if (discovery != null) {
			List<NetZone> netZones = discovery.getNetZones();
			if (netZones != null && !netZones.isEmpty()) {
				List<App> apps = netZones.get(0).getApps();
				if (apps != null) {
					for (App app: apps) {
						List<Action> actions = app.getActions();
						if (actions != null) {
							for (Action action : actions) {
								if (actionName.equalsIgnoreCase(action.getName()) && suffix.equalsIgnoreCase(action.getExt())) {
									return action;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

}
