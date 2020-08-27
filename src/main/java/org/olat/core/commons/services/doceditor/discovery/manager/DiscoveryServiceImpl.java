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
package org.olat.core.commons.services.doceditor.discovery.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.commons.services.doceditor.discovery.Action;
import org.olat.core.commons.services.doceditor.discovery.App;
import org.olat.core.commons.services.doceditor.discovery.Discovery;
import org.olat.core.commons.services.doceditor.discovery.NetZone;
import org.olat.core.commons.services.doceditor.discovery.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DiscoveryServiceImpl implements DiscoveryService {
	
	@Autowired
	private DiscoveryClient dicoveryClient;

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
	public Collection<Action> getActions(Discovery discovery) {
		List<Action> allActions = new ArrayList<>();
		if (discovery != null) {
			List<NetZone> netZones = discovery.getNetZones();
			if (netZones != null && !netZones.isEmpty()) {
				List<App> apps = netZones.get(0).getApps();
				if (apps != null) {
					for (App app: apps) {
						List<Action> actions = app.getActions();
						if (actions != null) {
							for (Action action : actions) {
								allActions.add(action);
							}
						}
					}
				}
			}
		}
		return allActions;
	}

}
