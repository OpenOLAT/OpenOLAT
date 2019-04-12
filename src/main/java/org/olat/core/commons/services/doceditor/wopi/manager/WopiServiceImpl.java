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
package org.olat.core.commons.services.doceditor.wopi.manager;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.wopi.Access;
import org.olat.core.commons.services.doceditor.wopi.Action;
import org.olat.core.commons.services.doceditor.wopi.App;
import org.olat.core.commons.services.doceditor.wopi.Discovery;
import org.olat.core.commons.services.doceditor.wopi.NetZone;
import org.olat.core.commons.services.doceditor.wopi.WopiService;
import org.olat.core.commons.services.doceditor.wopi.model.AccessImpl;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
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
	private WopiDiscoveryClient dicoveryClient;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private VFSRepositoryService vfsService;

	@PostConstruct
	public void init() {
		accessCache = coordinator.getCoordinator().getCacher().getCache(WopiService.class.getSimpleName(), "access");
	}

	@Override
	public String getRegularDiscoveryPath() {
		return dicoveryClient.getRegularDiscoveryPath();
	}

	@Override
	public Discovery getDiscovery(String discoveryUrl) {
		return dicoveryClient.getDiscovery(discoveryUrl);
	}
	
	@Override
	public boolean fileExists(String fileId) {
		return vfsService.getItemFor(fileId) != null? true: false;
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
		VFSItem item = vfsService.getItemFor(fileId);
		if (item instanceof VFSLeaf) {
			return (VFSLeaf) item;
		}
		return null;
	}

	@Override
	public VFSMetadata getMetadata(String fileId) {
		VFSLeaf vfsLeaf = getVfsLeaf(fileId);
		return vfsService.getMetadataFor(vfsLeaf);
	}

	@Override
	public Access createAccess(VFSMetadata vfsMetadata, Identity identity, DocEditorSecurityCallback secCallback) {
		String token = UUID.randomUUID().toString().replaceAll("-", "");
		String fileId = vfsMetadata.getUuid();
		
		AccessImpl access = new AccessImpl();
		access.setToken(token);
		access.setFileId(fileId);
		access.setIdentity(identity);
		access.setCanEdit(Mode.EDIT.equals(secCallback.getMode()));
		access.setVersionControlled(secCallback.isVersionControlled());
		access.setCanClose(secCallback.canClose());
		accessCache.put(token, access);
		return access;
	}

	@Override
	public Access getAccess(String accessToken) {
		if (StringHelper.containsNonWhitespace(accessToken)) {
			return accessCache.get(accessToken);
		}
		return null;
	}
	
	@Override
	public void deleteAccess(String accessToken) {
		if (StringHelper.containsNonWhitespace(accessToken)) {
			accessCache.remove(accessToken);
		}
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
