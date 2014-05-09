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
package org.olat.group;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.group.ui.homepage.GroupInfoMainController;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupCardContextEntryControllerCreator extends DefaultContextEntryControllerCreator {

	private BusinessGroup group;
	
	@Override
	public ContextEntryControllerCreator clone() {
		return new BusinessGroupCardContextEntryControllerCreator();
	}

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#createController(org.olat.core.id.context.ContextEntry,
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createController(List<ContextEntry> ces, UserRequest ureq, WindowControl wControl) {
		BusinessGroup bgroup = getBusinessGroup(ces.get(0));
		if(bgroup != null) {
			return new GroupInfoMainController(ureq, wControl, bgroup);
		}
		return null;
	}

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#getTabName(org.olat.core.id.context.ContextEntry)
	 */
	@Override
	public String getTabName(ContextEntry ce, UserRequest ureq) {
		BusinessGroup bgroup = getBusinessGroup(ce);
		if(bgroup != null) {
			return bgroup.getName();
		}
		return null;
	}

	@Override
	public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		return getBusinessGroup(ce) != null;
	}
	
	private BusinessGroup getBusinessGroup(ContextEntry ce) {
		if(group == null) {
			OLATResourceable ores = ce.getOLATResourceable();
			Long gKey = ores.getResourceableId();
			BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
			group = bgs.loadBusinessGroup(gKey);
		}
		return group;
	}
}
