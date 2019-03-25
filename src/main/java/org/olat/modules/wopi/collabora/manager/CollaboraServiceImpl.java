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
package org.olat.modules.wopi.collabora.manager;

import java.io.File;

import javax.annotation.PostConstruct;

import org.olat.core.commons.services.vfs.VFSLeafEditorSecurityCallback;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.modules.wopi.Access;
import org.olat.modules.wopi.Action;
import org.olat.modules.wopi.Discovery;
import org.olat.modules.wopi.WopiDiscoveryClient;
import org.olat.modules.wopi.WopiService;
import org.olat.modules.wopi.collabora.CollaboraModule;
import org.olat.modules.wopi.collabora.CollaboraRefreshDiscoveryEvent;
import org.olat.modules.wopi.collabora.CollaboraService;
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
	
	private Discovery discovery;
	
	@Autowired
	private CollaboraModule collaboraModule;
	@Autowired
	private WopiService wopiService;
	@Autowired
	private WopiDiscoveryClient discoveryClient;
	
	@PostConstruct
	private void init() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, REFRESH_EVENT_ORES);
	}

	@Override
	public boolean fileExists(String fileId) {
		return wopiService.fileExists(fileId);
	}

	@Override
	public File getFile(String fileId) {
		return wopiService.getFile(fileId);
	}

	@Override
	public VFSMetadata getMetadata(String fileId) {
		return wopiService.getMetadata(fileId);
	}

	@Override
	public Access createAccess(VFSMetadata vfsMetadata, Identity identity, VFSLeafEditorSecurityCallback secCallback) {
		return wopiService.createAccess(vfsMetadata, identity, secCallback);
	}

	@Override
	public Access getAccess(String accessToken) {
		return wopiService.getAccess(accessToken);
	}

	@Override
	public void deleteAccess(Access access) {
		wopiService.deleteAccess(access.getToken());
	}

	@Override
	public Discovery getDiscovery() {
		if (discovery == null) {
			String discoveryUrl = getDiscoveryUrl();
			discovery = discoveryClient.getDiscovery(discoveryUrl);
			log.info("Recieved new WOPI discovery from " + discoveryUrl);
		}
		return discovery;
	}

	private String getDiscoveryUrl() {
		return collaboraModule.getBaseUrl() + discoveryClient.getRegularDiscoveryPath();
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
	public String getEditorBaseUrl(File file) {
		String suffix = FileUtils.getFileSuffix(file.getName());
		Action action = wopiService.getAction(getDiscovery(), "edit", suffix);
		return action != null? action.getUrlSrc(): null;
	}

	@Override
	public boolean accepts(String suffix) {
		return wopiService.hasAction(getDiscovery(), "edit", suffix);
	}

}
