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
package org.olat.core.commons.services.license;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.license.manager.DefaultModuleValues;
import org.olat.core.commons.services.license.manager.LicensorCreator;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LicenseModule extends AbstractSpringModule {
	
	private static final String ENABLED_HANDLERS = "enabled-";
	private static final String DEFAULT_LICENSE_TYPE = "default.license.type-";
	private static final String LICENSOR_CREATOR_TYPE = "licensor.creator.type-";
	private static final String LICENSOR_CREATOR_CONSTANT = "licensor.creator.constant-";
	
	@Autowired
	private DefaultModuleValues defaultModuleValues;
	@Autowired
	private List<LicensorCreator> licensorCreators;
	@Autowired
    private List<LicenseHandler> handlers;
	private Map<String, Boolean> enabledHandlers = new HashMap<>();
	private Map<String, String> defaultLicenseTypeKeys = new HashMap<>();
	private Map<String, String> licensorCreatorTypes = new HashMap<>();
	private Map<String, String> licensorConstantValues = new HashMap<>();
	private boolean handlerInitialized = false;


	public LicenseModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		List<LicenseHandler> initHandlers = new ArrayList<>(handlers);
		for (LicenseHandler handler: initHandlers) {
			String handlerType = handler.getType();
			
			String enabledObj = getStringPropertyValue(ENABLED_HANDLERS + handlerType, true);
			if (StringHelper.containsNonWhitespace(enabledObj)) {
				Boolean enabled = Boolean.valueOf(enabledObj);
				enabledHandlers.put(handlerType, enabled);
			} else {
				// New license handler was never initialized
				initLicenseHandler(handler);
			}
			
			String defaultLicenseTypeKey = getStringPropertyValue(DEFAULT_LICENSE_TYPE + handlerType, true);
			if (StringHelper.containsNonWhitespace(defaultLicenseTypeKey)) {
				defaultLicenseTypeKeys.put(handlerType, defaultLicenseTypeKey);
			}
			
			String licensorCreatorType = getStringPropertyValue(LICENSOR_CREATOR_TYPE + handlerType, true);
			if (StringHelper.containsNonWhitespace(licensorCreatorType)) {
				licensorCreatorTypes.put(handlerType, licensorCreatorType);
			}
			
			String licensorCreatorConstant = getStringPropertyValue(LICENSOR_CREATOR_CONSTANT + handlerType, true);
			if (StringHelper.containsNonWhitespace(licensorCreatorConstant)) {
				licensorConstantValues.put(handlerType, licensorCreatorConstant);
			}
		}
		if (handlerInitialized) {
			savePropertiesAndFireChangedEvent();
		}
	}
	
	private void initLicenseHandler(LicenseHandler handler) {
		String handlerType = handler.getType();
		
		enabledHandlers.put(handlerType, defaultModuleValues.isEnabled());
		setStringProperty(ENABLED_HANDLERS + handlerType, String.valueOf(defaultModuleValues.isEnabled()), false);

		defaultModuleValues.activateLicenseTypes(handler);
		
		String licenseTypeKey = String.valueOf(defaultModuleValues.getLicenseType().getKey());
		defaultLicenseTypeKeys.put(handlerType, licenseTypeKey);
		setStringProperty(DEFAULT_LICENSE_TYPE + handlerType, licenseTypeKey, false);
		
		handlerInitialized = true;
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	/**
	 * @return A copy of the list of license handlers
	 */
	public List<LicenseHandler> getHandlers() {
		List<LicenseHandler> sortedHandlers = new ArrayList<>(handlers);
		sortedHandlers.sort((LicenseHandler h1, LicenseHandler h2) -> h1.getType().compareTo(h2.getType()));
		return sortedHandlers;
	}
	
	public List<LicensorCreator> getLicenseCreators() {
		return licensorCreators;
	}
	
	public boolean isAnyHandlerEnabled() {
		for (LicenseHandler handler: handlers) {
			if (isEnabled(handler)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isEnabled(LicenseHandler handler) {
		Boolean enabled = enabledHandlers.get(handler.getType());
		return enabled != null && enabled.booleanValue();
	}
	
	public void setEnabled(String handlerType, boolean enabled) {
		enabledHandlers.put(handlerType, Boolean.valueOf(enabled));
		setStringProperty(ENABLED_HANDLERS + handlerType, String.valueOf(enabled), true);
	}
	
	public String getDefaultLicenseTypeKey(LicenseHandler handler) {
		return defaultLicenseTypeKeys.get(handler.getType());
	}
	
	public void setDefaultLicenseTypeKey(LicenseHandler handler, String key) {
		defaultLicenseTypeKeys.put(handler.getType(), key);
		setStringProperty(DEFAULT_LICENSE_TYPE + handler.getType(), key, true);
	}

	public String getLicensorCreatorType(LicenseHandler handler) {
		return licensorCreatorTypes.get(handler.getType());
	}
	
	public void setLicensorCreatorType(LicenseHandler handler, String creatorType) {
		licensorCreatorTypes.put(handler.getType(), creatorType);
		setStringProperty(LICENSOR_CREATOR_TYPE + handler.getType(), creatorType, true);
	}

	public String getConstantLicensor(LicenseHandler handler) {
		return licensorConstantValues.get(handler.getType());
	}
	
	public void setConstantLicensor(LicenseHandler handler, String licensor) {
		String constantValue = StringHelper.containsNonWhitespace(licensor)? licensor: "";
		licensorConstantValues.put(handler.getType(), constantValue);
		setStringProperty(LICENSOR_CREATOR_CONSTANT + handler.getType(), constantValue, true);
	}

}
