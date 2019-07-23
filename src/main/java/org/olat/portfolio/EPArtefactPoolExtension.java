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
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.id.context.TabContext;
import org.olat.core.util.resource.OresHelper;
import org.olat.home.HomeSite;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * Description:<br>
 * Load a context entry creator
 * 
 * <P>
 * Initial Date: 02.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPArtefactPoolExtension {

	public EPArtefactPoolExtension() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator(AbstractArtefact.class.getSimpleName(), new ArtefactContextEntryControllerCreator());	
	}
	
	private static class ArtefactContextEntryControllerCreator extends DefaultContextEntryControllerCreator {

		@Override
		public ContextEntryControllerCreator clone() {
			return this;
		}

		@Override
		public String getSiteClassName(List<ContextEntry> ces, UserRequest ureq) {
			return HomeSite.class.getName();
		}

		@Override
		public TabContext getTabContext(UserRequest ureq, OLATResourceable ores, ContextEntry mainEntry, List<ContextEntry> entries) {
			Identity identity = ureq.getIdentity();

			OLATResourceable mapsRes = OresHelper.createOLATResourceableType("EPArtefacts");
			ContextEntry mapsEntry = BusinessControlFactory.getInstance().createContextEntry(mapsRes);
			List<ContextEntry> rewritedEntries = new ArrayList<>();
			rewritedEntries.add(mapsEntry);//Menu node
			rewritedEntries.add(mainEntry);//Map
			OLATResourceable homeRes = OresHelper.createOLATResourceableInstance("HomeSite", identity.getKey());
			return new TabContext("", homeRes, rewritedEntries);
		}
	}
}
