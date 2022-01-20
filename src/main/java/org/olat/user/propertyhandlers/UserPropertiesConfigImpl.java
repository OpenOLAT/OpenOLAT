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
package org.olat.user.propertyhandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.user.UserPropertiesConfig;

/**
 * <h3>Description:</h3>
 * This class implements the user properties configuration 
 * <p>
 * Initial Date: 31.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class UserPropertiesConfigImpl implements UserPropertiesConfig {
	private static final Logger log = Tracing.createLoggerFor(UserPropertiesConfigImpl.class);
	public static final String PACKAGE = UserPropertiesConfigImpl.class.getPackage().getName(); 

	
	private Map<String, UserPropertyHandler> userPropertyNameLookupMap;
	private ConcurrentMap<String, List<UserPropertyHandler>> userPropertyUsageContextsLookupMap = new ConcurrentHashMap<>();
	
	private List<UserPropertyHandler> userPropertyHandlers;
	private Map<String, UserPropertyUsageContext> userPropertyUsageContexts;
	private Map<String, UserPropertyUsageContext> defaultUserPropertyUsageContexts;

	private int maxNumOfInterests;

	@Override
	public int getMaxNumOfInterests() {
		return maxNumOfInterests;
	}

	/**
	 * [used by Spring]
	 * @param maxNumOfInterests
	 */
	public void setMaxNumOfInterests(int maxNumOfInterests) {
		this.maxNumOfInterests = maxNumOfInterests;
	}

	/**
	 * Spring setter
	 * @param userPropertyUsageContexts
	 */
	public void setUserPropertyUsageContexts(Map<String,UserPropertyUsageContext> userPropertyUsageContexts) {
		this.userPropertyUsageContexts = userPropertyUsageContexts;
		
		defaultUserPropertyUsageContexts = new HashMap<>();
		for(Map.Entry<String,UserPropertyUsageContext> entry:userPropertyUsageContexts.entrySet()) {
			defaultUserPropertyUsageContexts.put(entry.getKey(), entry.getValue().copy());
		}
	}

	@Override
	public Map<String,UserPropertyUsageContext> getUserPropertyUsageContexts() {
		return userPropertyUsageContexts;
	}
	
	public Map<String,UserPropertyUsageContext> getDefaultUserPropertyUsageContexts() {
		return defaultUserPropertyUsageContexts;
	}

	/**
	 * Spring setter
	 * @param userPropertyHandlers
	 */
	@Override
	public void setUserPropertyHandlers(List<UserPropertyHandler> userPropertyHandlers) {
		this.userPropertyHandlers = userPropertyHandlers;
		// populate name lookup map for faster lookup service
		Map<String,UserPropertyHandler> lookupMap = new HashMap<>(userPropertyHandlers.size());
		for (UserPropertyHandler propertyHandler : userPropertyHandlers) {
			String name = propertyHandler.getName();
			lookupMap.put(name, propertyHandler);
		}
		userPropertyNameLookupMap = Map.copyOf(lookupMap);
	}

	@Override
	public UserPropertyHandler getPropertyHandler(String handlerName) {
		if(!StringHelper.containsNonWhitespace(handlerName)) return null;
		
		UserPropertyHandler handler = userPropertyNameLookupMap.get(handlerName);
		if (handler == null && log.isDebugEnabled()) {
			log.debug("UserPropertyHander for handlerName::{} not found, check your configuration.", handlerName);
		}
		return handler;
	}

	@Override
	public Translator getTranslator(Translator fallBack) {
		return new PackageTranslator(PACKAGE, fallBack.getLocale(), fallBack); 
	}

	@Override
	public List<UserPropertyHandler> getAllUserPropertyHandlers() {
		return userPropertyHandlers;
	}

	@Override
	public List<UserPropertyHandler> getUserPropertyHandlersFor(String usageIdentifyer, boolean isAdministrativeUser) {
		String key = usageIdentifyer + "_" + isAdministrativeUser;
		List<UserPropertyHandler> currentUsageHandlers = userPropertyUsageContextsLookupMap.get(key);
		if (currentUsageHandlers == null) {
			List<UserPropertyHandler> newUsageHandlers = new ArrayList<>();
			UserPropertyUsageContext currentUsageConfig = getCurrentUsageConfig(usageIdentifyer);			
			// add all handlers that are accessible for this user
			for (UserPropertyHandler propertyHandler : currentUsageConfig.getPropertyHandlers()) {
				// if configured for this class and if isAdministrativeUser
				if (currentUsageConfig.isForAdministrativeUserOnly(propertyHandler) && !isAdministrativeUser) {
					// don't add this handler for this user
					continue;
				}
				newUsageHandlers.add(propertyHandler);								
			}
			
			currentUsageHandlers = userPropertyUsageContextsLookupMap.putIfAbsent(key, newUsageHandlers);
			if(currentUsageHandlers == null) {
				currentUsageHandlers = newUsageHandlers;
			}
		}
		return currentUsageHandlers;
	}

	@Override
	public boolean isMandatoryUserProperty(String usageIdentifyer, UserPropertyHandler propertyHandler) {
		UserPropertyUsageContext currentUsageConfig = getCurrentUsageConfig(usageIdentifyer);
		return currentUsageConfig.isMandatoryUserProperty(propertyHandler);
	}

	@Override
	public boolean isUserViewReadOnly(String usageIdentifyer, UserPropertyHandler propertyHandler) {
		UserPropertyUsageContext currentUsageConfig = getCurrentUsageConfig(usageIdentifyer);
		return currentUsageConfig.isUserViewReadOnly(propertyHandler);
	}

	/**
	 * Internal helper to get the usage configuration for this identifyer
	 * @param usageIdentifyer
	 * @return
	 */
	private UserPropertyUsageContext getCurrentUsageConfig(String usageIdentifyer) {
		UserPropertyUsageContext currentUsageConfig = userPropertyUsageContexts.get(usageIdentifyer);
		if (currentUsageConfig == null) {
			currentUsageConfig = userPropertyUsageContexts.get("default");
			log.warn("Could not find user property usage configuration for usageIdentifyer::{}, please check yout olat_userconfig.xml file. Using default configuration instead.", usageIdentifyer);
			if (currentUsageConfig == null) {
				throw new OLATRuntimeException("Missing default user property usage configuration in olat_userconfig.xml", null);
			}
		}
		return currentUsageConfig;
	}
}
