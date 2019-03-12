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
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.wopi.Access;
import org.olat.modules.wopi.Action;
import org.olat.modules.wopi.App;
import org.olat.modules.wopi.Discovery;
import org.olat.modules.wopi.NetZone;
import org.olat.modules.wopi.WopiService;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class WopiServiceImpl implements WopiService {

	@Override
	public boolean fileExists(String fileId) {
		// TODO uh real implementation
		return true;
	}

	@Override
	public File getFile(String fileId) {
		return new File("/Users/urshensler/tmp/document.docx");
	}

	@Override
	public Access getAccess(String accessToken) {
		// TODO uh real implementation
		return new Access() {
			
			@Override
			public String getToken() {
				return "123";
			}
			
			@Override
			public String getProviderType() {
				return "collabora";
			}

			@Override
			public Identity getOwner() {
				// TODO uh Auto-generated method stub
				return null;
			}

			@Override
			public Identity getAccessIdenity() {
				// TODO uh Auto-generated method stub
				return null;
			}

			@Override
			public String getFileId() {
				// TODO uh Auto-generated method stub
				return null;
			}
		};
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
