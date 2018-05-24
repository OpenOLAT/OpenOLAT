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
package org.olat.user;

import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.id.context.TabContext;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.home.HomeSite;

/**
 * <h3>Description:</h3>
 * <p>
 * This class offers a way to launch the users home page (alias visiting card), home site
 * or the home of the logged in user in a new tab or (in the case of the logged in user)
 * in its user's tools.
 * <p>
 * Initial Date: 21.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class IdentityContextEntryControllerCreator extends DefaultContextEntryControllerCreator {
	private static final OLog log = Tracing.createLoggerFor(IdentityContextEntryControllerCreator.class);

	private Identity identity;
	
	@Override
	public ContextEntryControllerCreator clone() {
		return new IdentityContextEntryControllerCreator();
	}

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#createController(org.olat.core.id.context.ContextEntry,
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createController(List<ContextEntry> ces, UserRequest ureq, WindowControl wControl) {
		Identity id = getIdentity(ces.get(0), ureq);
		if (id == null) {
			return null;
		}
		return new UserInfoMainController(ureq, wControl, id, false, true);
	}

	@Override
	public String getSiteClassName(List<ContextEntry> ces, UserRequest ureq) {
		Long resId = ces.get(0).getOLATResourceable().getResourceableId();
		if(resId != null && (resId.longValue() == 0l || resId.equals(ureq.getIdentity().getKey()))) {
			return HomeSite.class.getName();
		}
		return null;
	}

	@Override
	public TabContext getTabContext(UserRequest ureq, OLATResourceable ores,
			ContextEntry mainEntry, List<ContextEntry> entries) {
		
		Long resId = mainEntry.getOLATResourceable().getResourceableId();
		if(resId != null && (resId.longValue() == 0l || resId.equals(ureq.getIdentity().getKey()))) {
			if(entries.isEmpty()) {//rewrite
				OLATResourceable homeOres = OresHelper.createOLATResourceableInstance("HomeSite", resId);
				entries.add(BusinessControlFactory.getInstance().createContextEntry(homeOres));
				OLATResourceable profileOres = OresHelper.createOLATResourceableInstance("profil", 0l);
				entries.add(BusinessControlFactory.getInstance().createContextEntry(profileOres));
			}
		}

		return super.getTabContext(ureq, ores, mainEntry, entries);
	}

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#getTabName(org.olat.core.id.context.ContextEntry)
	 */
	@Override
	public String getTabName(ContextEntry ce, UserRequest ureq) {
		Identity id = getIdentity(ce, ureq);
		if (id == null) return null;
		return UserManager.getInstance().getUserDisplayName(id);
	}

	@Override
	public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		return getIdentity(ce, ureq) != null;
	}
	
	/**
	 * Helper to get the identity that is encoded into the context entry
	 * 
	 * @param ce
	 * @return the identity or NULL if not found
	 */
	private Identity getIdentity(ContextEntry ce, UserRequest ureq) {
		if(identity == null) {
			OLATResourceable resource = ce.getOLATResourceable();
			Long key = resource.getResourceableId();
			if (key == null || key.longValue() == 0l) {
				identity = ureq.getIdentity();
			} else {
				identity = BaseSecurityManager.getInstance().loadIdentityByKey(key);
				if (identity == null) {
					log.error("Can not load identity with key::" + key);
				}
			}
		}
		return identity;
	}
}
