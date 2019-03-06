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

import javax.annotation.PostConstruct;

import org.olat.core.gui.control.Event;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.modules.wopi.Discovery;
import org.olat.modules.wopi.WopiDiscoveryClient;
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
	
	private Discovery discoveryImpl;
	
	@Autowired
	private CollaboraModule collaboraModule;
	@Autowired
	private WopiDiscoveryClient discoveryClient;
	
	@PostConstruct
	private void init() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, REFRESH_EVENT_ORES);
	}

	@Override
	public Discovery getDiscovery() {
		if (discoveryImpl == null) {
			String discoveryUrl = getDiscoveryUrl();
			discoveryImpl = discoveryClient.getDiscovery(discoveryUrl);
			log.info("Recieved new WOPI discovery from " + discoveryUrl);
		}
		return discoveryImpl;
	}

	private String getDiscoveryUrl() {
		return collaboraModule.getBaseUrl() + discoveryClient.getRegularDiscoveryPath();
	}
	
//	public void refreshDiscovery() {
//		deleteDiscovery();
//		
//		// Notify other cluster nodes to refresh the discovery
//		CollaboraRefreshDiscoveryEvent event = new CollaboraRefreshDiscoveryEvent();
//		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, REFRESH_EVENT_ORES);
//	}

	@Override
	public void event(Event event) {
		if (event instanceof CollaboraRefreshDiscoveryEvent) {
			deleteDiscovery();
		}
	}

	private void deleteDiscovery() {
		discoveryImpl = null;
		log.info("Deleted WOPI discovery. It will be refreshed with the next access.");
	}

}
