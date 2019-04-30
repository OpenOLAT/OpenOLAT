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
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * <h3>Description:</h3>
 * <p>
 * This class offers a way to launch the users homepage (alias visiting card)
 * controller in a new tab
 * <p>
 * Initial Date: 21.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class HomePageContextEntryControllerCreator extends DefaultContextEntryControllerCreator {
	private static final OLog log = Tracing.createLoggerFor(HomePageContextEntryControllerCreator.class);

	@Override
	public ContextEntryControllerCreator clone() {
		return this;
	}

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#createController(org.olat.core.id.context.ContextEntry,
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createController(List<ContextEntry> ces, UserRequest ureq, WindowControl wControl) {
		Identity identity = extractIdentity(ces.get(0));
		if (identity == null) return null;
		return new UserInfoMainController(ureq, wControl, identity, false, true);
	}

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#getTabName(org.olat.core.id.context.ContextEntry)
	 */
	@Override
	public String getTabName(ContextEntry ce, UserRequest ureq) {
		Identity identity = extractIdentity(ce);
		if (identity == null) return null;
		return UserManager.getInstance().getUserDisplayName(identity);
	}

	/**
	 * Helper to get the identity that is encoded into the context entry
	 * 
	 * @param ce
	 * @return the identity or NULL if not found
	 */
	private Identity extractIdentity(ContextEntry ce) {
		OLATResourceable resource = ce.getOLATResourceable();
		Long key = resource.getResourceableId();
		if (key == null || key.equals(0l)) {
			log.error("Can not load identity with key::" + key);
			return null;
		}
		StateEntry state = ce.getTransientState();
		if(state instanceof HomePageStateEntry) {
			HomePageStateEntry homeState = (HomePageStateEntry)state;
			if(homeState.same(key)) {
				return homeState.getIdentity();
			}
		}
		
		Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(key);
		if (identity == null) {
			log.error("Can not load identity with key::" + key);
		}
		ce.setTransientState(new HomePageStateEntry(identity));
		return identity;
	}

	@Override
	public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		Identity identity = extractIdentity(ce);
		return identity != null;
	}
	
	public static class HomePageStateEntry implements StateEntry {
		private static final long serialVersionUID = -8949620136046652588L;
		private final Identity identity;
		
		public HomePageStateEntry(Identity identity) {
			this.identity = identity;
		}
		
		public boolean same(Long key) {
			return identity != null && identity.getKey().equals(key);
		}
		
		public Identity getIdentity() {
			return identity;
		}

		@Override
		public HomePageStateEntry clone()  {
			return new HomePageStateEntry(identity);
		}
	}
}
