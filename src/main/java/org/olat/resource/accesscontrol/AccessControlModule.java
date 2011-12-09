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

package org.olat.resource.accesscontrol;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.control.Event;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.FrameworkStartedEvent;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;

/**
 * 
 * Description:<br>
 * Module for access control of OLAT Resource
 * 
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AccessControlModule extends AbstractOLATModule implements ConfigOnOff {
	
	public static final String AC_ENABLED = "resource.accesscontrol.enabled";
	public static final String AC_HOME_ENABLED = "resource.accesscontrol.home.overview";
	private static final String VAT_ENABLED = "vat.enabled";
	private static final String VAT_RATE = "vat.rate";
	private static final String VAT_NR = "vat.number";
	
	public static final String TOKEN_ENABLED = "method.token.enabled";
	public static final String FREE_ENABLED = "method.free.enabled";

	private boolean enabled;
	private boolean freeEnabled;
	private boolean tokenEnabled;
	private boolean homeOverviewEnabled;
	
	private boolean vatEnabled;
	private BigDecimal vatRate;
	private String vatNumber;
	
	private final List<AccessMethodHandler> methodHandlers = new ArrayList<AccessMethodHandler>();
	
	private ACFrontendManager acFrontendManager;

	/**
	 * [Used by Spring]
	 * @param methodManager
	 */
	public void setAcFrontendManager(ACFrontendManager acFrontendManager) {
		this.acFrontendManager = acFrontendManager;
	}

	public AccessControlModule(){
		FrameworkStartupEventChannel.registerForStartupEvent(this);
	}
	
	private void enableExtensions(boolean enabled) {
		try {
			((GenericActionExtension) CoreSpringFactory.getBean("accesscontrol.actExt")).setEnabled(enabled);
		} catch (Exception e) {
			// do nothing when extension don't exist.
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof FrameworkStartedEvent) {
			enableExtensions(isEnabled());
		} else {
			super.event(event);
		}
	}

	@Override
	public void init() {
		//module enabled/disabled
		String enabledObj = getStringPropertyValue(AC_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String tokenEnabledObj = getStringPropertyValue(TOKEN_ENABLED, true);
		if(StringHelper.containsNonWhitespace(tokenEnabledObj)) {
			tokenEnabled = "true".equals(tokenEnabledObj);
		}
		
		String freeEnabledObj = getStringPropertyValue(FREE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(freeEnabledObj)) {
			freeEnabled = "true".equals(freeEnabledObj);
		}
		
		String homeEnabledObj = getStringPropertyValue(AC_HOME_ENABLED, true);
		if(StringHelper.containsNonWhitespace(homeEnabledObj)) {
			homeOverviewEnabled = "true".equals(homeEnabledObj);
		}
		
		String vatEnabledObj = getStringPropertyValue(VAT_ENABLED, true);
		if(StringHelper.containsNonWhitespace(vatEnabledObj)) {
			vatEnabled = "true".equals(vatEnabledObj);
		}
		
		String vatRateObj = getStringPropertyValue(VAT_RATE, true);
		if(StringHelper.containsNonWhitespace(vatRateObj)) {
			try {
				vatRate = new BigDecimal(vatRateObj);
			} catch (Exception e) {
				logError("Error parsing the VAT: " + vatRateObj, e);
			}
		}
		
		String vatNrObj = getStringPropertyValue(VAT_NR, true);
		if(StringHelper.containsNonWhitespace(vatNrObj)) {
			vatNumber = vatNrObj;
		}
		enableExtensions(enabled);
		
		logInfo("Access control module is enabled: " + Boolean.toString(enabled));
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	@Override
	protected void initDefaultProperties() {
		enabled = getBooleanConfigParameter(AC_ENABLED, true);
		freeEnabled = getBooleanConfigParameter(FREE_ENABLED, true);
		tokenEnabled = getBooleanConfigParameter(TOKEN_ENABLED, true);
		homeOverviewEnabled = getBooleanConfigParameter(AC_HOME_ENABLED, true);
		vatEnabled = getBooleanConfigParameter(VAT_ENABLED, true);
		String vatRateStr = getStringConfigParameter(VAT_RATE, "", true);
		if(StringHelper.containsNonWhitespace(vatRateStr)) {
			try {
				vatRate = new BigDecimal(vatRateStr);
			} catch (Exception e) {
				logError("Error parsing the VAT: " + vatRateStr, e);
			}
		}
		vatNumber = getStringConfigParameter(VAT_NR, "", true);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		if(this.enabled != enabled) {
			setStringProperty(AC_ENABLED, Boolean.toString(enabled), true);
		}
		enableExtensions(enabled);
	}

	public boolean isTokenEnabled() {
		return tokenEnabled;
	}

	public void setTokenEnabled(boolean tokenEnabled) {
		if(this.tokenEnabled != tokenEnabled) {
			setStringProperty(TOKEN_ENABLED, Boolean.toString(tokenEnabled), true);
		}
		if(acFrontendManager != null) {
			acFrontendManager.enableMethod(TokenAccessMethod.class, tokenEnabled);
		}
	}

	public boolean isFreeEnabled() {
		return freeEnabled;
	}

	public void setFreeEnabled(boolean freeEnabled) {
		if(this.freeEnabled != freeEnabled) {
			setStringProperty(FREE_ENABLED, Boolean.toString(freeEnabled), true);
		}
		if(acFrontendManager != null) {
			acFrontendManager.enableMethod(FreeAccessMethod.class, freeEnabled);
		}
	}
	
	public boolean isHomeOverviewEnabled() {
		return homeOverviewEnabled;
	}

	public void setHomeOverviewEnabled(boolean homeOverviewEnabled) {
		if(this.homeOverviewEnabled != homeOverviewEnabled) {
			setStringProperty(AC_HOME_ENABLED, Boolean.toString(homeOverviewEnabled), true);
		}
	}
	
	public boolean isVatEnabled() {
		return vatEnabled;
	}

	public void setVatEnabled(boolean vatEnabled) {
		if(this.vatEnabled != vatEnabled) {
			setStringProperty(VAT_ENABLED, Boolean.toString(vatEnabled), true);
		}
	}

	public BigDecimal getVat() {
		return vatRate;
	}

	public void setVat(BigDecimal vatRate) {
		setStringProperty(VAT_RATE, vatRate.toPlainString(), true);
	}

	public String getVatNumber() {
		return vatNumber;
	}

	public void setVatNumber(String vatNumber) {
		setStringProperty(VAT_NR, vatNumber, true);
	}

	public List<AccessMethodHandler> getMethodHandlers() {
		return new ArrayList<AccessMethodHandler>(methodHandlers);
	}
	
	public void setMethodHandlers(List<AccessMethodHandler> handlers) {
		if(handlers != null) {
			methodHandlers.addAll(handlers);
		}
	}
	
	public void addMethodHandler(AccessMethodHandler handler) {
		if(handler != null) {
			methodHandlers.add(handler);
		}
	}
	
	public void removeMethodHandler(AccessMethodHandler handler) {
		if(handler != null) {
			methodHandlers.remove(handler);
		}
	}

	public AccessMethodHandler getAccessMethodHandler(String type) {
		for(AccessMethodHandler handler:methodHandlers) {
			if(handler.getType().equals(type)) {
				return handler;
			}
		}
		return null;
	}
}