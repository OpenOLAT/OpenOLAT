/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.user;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
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
public class IdentityContextEntryControllerCreator implements ContextEntryControllerCreator {
	private static final OLog log = Tracing.createLoggerFor(IdentityContextEntryControllerCreator.class);

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#createController(org.olat.core.id.context.ContextEntry,
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public Controller createController(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		Identity identity = extractIdentity(ce);
		if (identity == null) return null;
		UserInfoMainController uimc = new UserInfoMainController(ureq, wControl, identity);
		return uimc;
	}

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#getSiteClassName(org.olat.core.id.context.ContextEntry)
	 */
	public String getSiteClassName(ContextEntry ce) {
		// opened as tab not site
		return null;
	}

	/**
	 * @see org.olat.core.id.context.ContextEntryControllerCreator#getTabName(org.olat.core.id.context.ContextEntry)
	 */
	public String getTabName(ContextEntry ce) {
		Identity identity = extractIdentity(ce);
		if (identity == null) return null;
		return identity.getName();
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
		if (key == null || key.equals(0)) {
			log.error("Can not load identity with key::" + key);
			return null;
		}
		Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(key);
		if (identity == null) {
			log.error("Can not load identity with key::" + key);
		}
		return identity;
	}

	@Override
	public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		Identity identity = extractIdentity(ce);
		return identity!=null;
	}
}
