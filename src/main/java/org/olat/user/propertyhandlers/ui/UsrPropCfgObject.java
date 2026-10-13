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
import java.util.LinkedHashMap;
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
	
	public UserPropertyHandler getPropertyHandler(String name) {
		for(UserPropertyHandler handler:allHandlers) {
			if(handler.getName().equals(name)) {
				return handler;
			}
		}
		return null;
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
			}
		}
	}

	public List<String> contextsWith(UserPropertyHandler propertyHandler) {
		return contextNames(propertyHandler, true);
	}

	public List<String> contextsWithout(UserPropertyHandler propertyHandler) {
		return contextNames(propertyHandler, false);
	}

	private List<String> contextNames(UserPropertyHandler propertyHandler, boolean contained) {
		return allContexts.entrySet().stream()
				.filter(entry -> entry.getValue().contains(propertyHandler) == contained)
				.map(Entry::getKey).toList();
	}

	public List<UserPropertyHandler> activeHandlersOf(UserPropertyUsageContext context) {
		return context.getPropertyHandlers().stream().filter(activeHandlers::contains).toList();
	}

	public int activeHandlerCount(UserPropertyUsageContext context) {
		return activeHandlersOf(context).size();
	}

	/**
	 * @return copies of all contexts without the inactive handlers, for the UserManager
	 */
	public Map<String, UserPropertyUsageContext> createEffectiveContexts() {
		List<UserPropertyHandler> inactive = allHandlers.stream().filter(handler -> !activeHandlers.contains(handler)).toList();
		Map<String, UserPropertyUsageContext> effective = new LinkedHashMap<>();
		allContexts.forEach((name, context) -> effective.put(name, context.copyWithout(inactive)));
		return effective;
	}

}
