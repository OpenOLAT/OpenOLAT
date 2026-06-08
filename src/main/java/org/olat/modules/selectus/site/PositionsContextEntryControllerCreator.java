/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.site;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.id.context.TabContext;

/**
 * 
 * Initial date: 05.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionsContextEntryControllerCreator extends DefaultContextEntryControllerCreator {

	@Override
	public ContextEntryControllerCreator clone() {
		return this;
	}

	@Override
	public String getSiteClassName(List<ContextEntry> ces, UserRequest ureq) {
		return SelectusSite.class.getName();
	}

	@Override
	public TabContext getTabContext(UserRequest ureq, OLATResourceable ores, ContextEntry mainEntry, List<ContextEntry> entries) {
		if(entries != null && !entries.isEmpty()
				&& "Positions".equalsIgnoreCase(entries.get(0).getOLATResourceable().getResourceableTypeName())
				&& entries.get(0).getOLATResourceable().getResourceableId().longValue() > 0) {
			//expand Positions/2344456 in Positions/0/Position/2344456	
		}
		return super.getTabContext(ureq, ores, mainEntry, entries);
	}
}
