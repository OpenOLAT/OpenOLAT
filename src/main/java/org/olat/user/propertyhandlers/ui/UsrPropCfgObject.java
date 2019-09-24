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
* frentix GmbH, Switzerland, http://www.frentix.com
* <p>
*/

package org.olat.user.propertyhandlers.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyUsageContext;

/**
 * 
 * Description:<br>
 * Wrapper Object that holds all UserPropertyHandlers and Contexts.
 * It keeps also track of all activeHandlers
 * 
 * <P>
 * Initial Date: 26.08.2011 <br>
 * 
 * @author strentini
 */
public class UsrPropCfgObject {

	private List<UserPropertyHandler> allHandlers = new ArrayList<>();
	private Set<UserPropertyHandler> activeHandlers = new HashSet<>();

	private Map<String, UserPropertyUsageContext> allContexts = new HashMap<>();

	public UsrPropCfgObject(List<UserPropertyHandler> allHandlers, Map<String, UserPropertyUsageContext> allContexts) {
		this.allHandlers = allHandlers;
		this.allContexts = allContexts;
	}

	/**
	 * checks whether the given handler is activated.<br/>
	 * note: this is the system-wide active/non-active!  
	 * 
	 * 
	 * @param propertyHandler
	 * @return
	 */
	public boolean isActiveHandler(UserPropertyHandler propertyHandler) {
		return activeHandlers.contains(propertyHandler);
	}

	public List<UserPropertyHandler> getPropertyHandlers() {
		return allHandlers;
	}

	public Map<String, UserPropertyUsageContext> getUsageContexts() {
		return allContexts;
	}

	public void setHandlerAsActive(UserPropertyHandler propertyHandler, boolean isActive) {
		if (isActive) {
			if (allHandlers.contains(propertyHandler)) {
				if (!activeHandlers.contains(propertyHandler)) activeHandlers.add(propertyHandler);
			}
		} else {
			if (UsrPropCfgManager.canBeDeactivated(propertyHandler)) {
				activeHandlers.remove(propertyHandler);
				// user disabled a propertyHandler, remove it from all contexts!
				removeHandlerFromAllContexts(propertyHandler);
			}
		}
	}

	/**
	 * removes the given PropertyHandler from all contexts.
	 * 
	 * @param propertyHandler
	 */
	private void removeHandlerFromAllContexts(UserPropertyHandler propertyHandler) {
		for (Entry<String, UserPropertyUsageContext> ctxEntry : allContexts.entrySet()) {
			ctxEntry.getValue().removePropertyHandler(propertyHandler);
		}
	}

}
