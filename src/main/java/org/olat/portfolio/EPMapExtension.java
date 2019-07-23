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
package org.olat.portfolio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.id.context.TabContext;
import org.olat.core.util.resource.OresHelper;
import org.olat.home.HomeSite;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.EPDefaultMap;
import org.olat.resource.OLATResource;

/**
 * Rewrite the business path to map
 * @author srosse
 *
 */
public class EPMapExtension {

	public EPMapExtension() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator(EPDefaultMap.class.getSimpleName(), new MapContextEntryControllerCreator());	
	}
	
	private static class MapContextEntryControllerCreator extends DefaultContextEntryControllerCreator {

		@Override
		public ContextEntryControllerCreator clone() {
			return this;
		}

		@Override
		public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
			return true;
		}

		@Override
		public String getSiteClassName(List<ContextEntry> ces, UserRequest ureq) {
			return HomeSite.class.getName();
		}

		@Override
		public TabContext getTabContext(UserRequest ureq, OLATResourceable ores, ContextEntry mainEntry, List<ContextEntry> entries) {
			Identity identity = ureq.getIdentity();
			EPFrontendManager ePFMgr = CoreSpringFactory.getImpl(EPFrontendManager.class);
			
			String nodeTypeName = null;
			OLATResource mapOres = ePFMgr.loadOlatResourceFromByKey(mainEntry.getOLATResourceable().getResourceableId());
			if(mapOres == null) {
				//nothing to do;
			} else if(ePFMgr.isMapOwner(identity, mapOres)) {
				if("EPStructuredMap".equals(mapOres.getResourceableTypeName())) {
					nodeTypeName = "EPStructuredMaps";
				} else {
					nodeTypeName = "EPMaps";
				}
			} else if(ePFMgr.isMapVisible(ureq.getIdentity(), mapOres)) {
				//go to the shared pane
				if(ePFMgr.isMapShared(mapOres)) {
					nodeTypeName = "EPSharedMaps";
				}
			}
			
			if(nodeTypeName == null) {
				OLATResourceable homeRes = OresHelper.createOLATResourceableInstance("HomeSite", identity.getKey());
				return new TabContext("", homeRes, Collections.<ContextEntry>emptyList());
			}
			
			OLATResourceable mapsRes = OresHelper.createOLATResourceableType(nodeTypeName);
			ContextEntry mapsEntry = BusinessControlFactory.getInstance().createContextEntry(mapsRes);
			List<ContextEntry> rewritedEntries = new ArrayList<>();
			rewritedEntries.add(mapsEntry);//Menu node
			rewritedEntries.add(mainEntry);//Map
			if(entries != null && !entries.isEmpty()) {
				rewritedEntries.addAll(entries);//more details
			}
			// -> HomeSite
			OLATResourceable homeRes = OresHelper.createOLATResourceableInstance("HomeSite", identity.getKey());
			return new TabContext("", homeRes, rewritedEntries);
		}	
	}
}
