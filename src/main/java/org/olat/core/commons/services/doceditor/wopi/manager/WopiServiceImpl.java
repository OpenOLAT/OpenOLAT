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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.wopi.Access;
import org.olat.core.commons.services.doceditor.wopi.Action;
import org.olat.core.commons.services.doceditor.wopi.App;
import org.olat.core.commons.services.doceditor.wopi.Discovery;
import org.olat.core.commons.services.doceditor.wopi.NetZone;
import org.olat.core.commons.services.doceditor.wopi.WopiService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
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
	
	@Autowired
	private WopiDiscoveryClient dicoveryClient;
	@Autowired
	private AccessDAO accessDao;
	@Autowired
	private VFSRepositoryService vfsService;

	@Override
	public String getRegularDiscoveryPath() {
		return dicoveryClient.getRegularDiscoveryPath();
	}

	@Override
	public Discovery getDiscovery(String discoveryUrl) {
		return dicoveryClient.getDiscovery(discoveryUrl);
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

	@Override
	public Access getOrCreateAccess(VFSMetadata vfsMetadata, Identity identity, DocEditorSecurityCallback secCallback, Date expiresAt) {
		Access access = accessDao.loadAccess(vfsMetadata, identity);
		if (access != null) {
			if (accessUnchanged(access, secCallback) && !expired(access)) {
				access = accessDao.updateExpiresAt(access, expiresAt);
				return access;
			}
			accessDao.deleteAccess(access.getToken());
		}
		
		return accessDao.createAccess(vfsMetadata, identity, createToke(), getCanEdit(secCallback), secCallback.canClose(), secCallback.isVersionControlled(), expiresAt);
	}

	private boolean accessUnchanged(Access access, DocEditorSecurityCallback secCallback) {
		if (access.isCanEdit() != getCanEdit(secCallback)) return false;
		if (access.isCanClose() != secCallback.canClose()) return false;
		if (access.isVersionControlled() != secCallback.isVersionControlled()) return false;
		return true;
	}

	private String createToke() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	private boolean getCanEdit(DocEditorSecurityCallback secCallback) {
		return Mode.EDIT.equals(secCallback.getMode());
	}

	@Override
	public Access getAccess(String accessToken) {
		Access access = accessDao.loadAccess(accessToken);
		if (expired(access)) {
			accessDao.deleteAccess(accessToken);
			access = null;
		}
		return access;
	}

	private boolean expired(Access access) {
		return access != null && access.getExpiresAt() != null && access.getExpiresAt().before(new Date());
	}
	
	@Override
	public VFSLeaf getVfsLeaf(Access access) {
		VFSItem item = vfsService.getItemFor(access.getMetadata());
		if (item instanceof VFSLeaf) {
			return (VFSLeaf) item;
		}
		return null;
	}
	
	@Override
	public void deleteAccess(String accessToken) {
		accessDao.deleteAccess(accessToken);
	}

}
