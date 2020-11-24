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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.resource.accesscontrol.provider.auto.manager.AdvanceOrderDAO;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalAccessMethod;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.PaypalCheckoutAccessMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * Description:<br>
 * Module for access control of OLAT Resource
 *
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("acModule")
public class AccessControlModule extends AbstractSpringModule implements ConfigOnOff {

	private static final Logger log = Tracing.createLoggerFor(AccessControlModule.class);

	public static final String AC_ENABLED = "resource.accesscontrol.enabled";
	public static final String AC_HOME_ENABLED = "resource.accesscontrol.home.overview";
	private static final String VAT_ENABLED = "vat.enabled";
	private static final String VAT_RATE = "vat.rate";
	private static final String VAT_NR = "vat.number";

	private static final String TOKEN_ENABLED = "method.token.enabled";
	private static final String FREE_ENABLED = "method.free.enabled";
	private static final String PAYPAL_ENABLED = "method.paypal.enabled";
	private static final String PAYPAL_CHECKOUT_ENABLED = "method.paypal.checkout.enabled";
	private static final String AUTO_ENABLED = "method.auto.enabled";
	private static final String AUTO_EXTERNAL_REF_DELIMITER = "method.auto.external.ref.delimiter";
	private static final String AUTO_MULTI_BOOKING = "method.auto.multi.booking";
	private static final String AUTO_RESET_TO_PENDING = "method.auto.reset.to.pending";
	private static final String AUTO_CANCELATION = "method.auto.cancelation";

	@Value("${resource.accesscontrol.enabled:true}")
	private boolean enabled;
	@Value("${resource.accesscontrol.home.overview:true}")
	private boolean homeOverviewEnabled;
	@Value("${method.free.enabled:true}")
	private boolean freeEnabled;
	@Value("${method.auto.enabled:false}")
	private boolean autoEnabled;
	@Value("${method.auto.externalRef.delimiter}")
	private String autoExternalRefDelimiter;
	@Value("${method.auto.multi.booking}")
	private boolean autoMultiBooking;
	@Value("${method.auto.reset.to.pending}")
	private boolean autoResetToPending;
	@Value("${method.auto.cancelation}")
	private boolean autoCancelation;
	@Value("${method.token.enabled:true}")
	private boolean tokenEnabled;
	@Value("${method.paypal.enabled:false}")
	private boolean paypalEnabled;
	@Value("${method.paypal.checkout.enabled:false}")
	private boolean paypalCheckoutEnabled;
	@Value("${vat.enabled:false}")
	private boolean vatEnabled;
	@Value("${vat.rate:7.0}")
	private BigDecimal vatRate;
	@Value("${vat.number:1}")
	private String vatNumber;

	@Autowired
	private DB dbInstance;
	private final ACMethodDAO acMethodManager;
	@Autowired
	private AdvanceOrderDAO advanceOrderDao;
	@Autowired
	private List<AccessMethodHandler> methodHandlers;

	@Autowired
	public AccessControlModule(CoordinatorManager coordinatorManager, ACMethodDAO acMethodManager) {
		super(coordinatorManager);
		this.acMethodManager = acMethodManager;
	}

	@Override
	public void init() {
		//module enabled/disabled
		updateProperties();
		updateAccessMethods();
		resetResetAutoStatusToPending();
		log.info("Access control module is enabled: " + Boolean.toString(enabled));
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue(AC_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}

		String tokenEnabledObj = getStringPropertyValue(TOKEN_ENABLED, true);
		if(StringHelper.containsNonWhitespace(tokenEnabledObj)) {
			tokenEnabled = "true".equals(tokenEnabledObj);
		}

		String paypalEnabledObj = getStringPropertyValue(PAYPAL_ENABLED, true);
		if(StringHelper.containsNonWhitespace(paypalEnabledObj)) {
			paypalEnabled = "true".equals(paypalEnabledObj);
		}
		
		String paypalCheckoutEnabledObj = getStringPropertyValue(PAYPAL_CHECKOUT_ENABLED, true);
		if(StringHelper.containsNonWhitespace(paypalCheckoutEnabledObj)) {
			paypalCheckoutEnabled = "true".equals(paypalCheckoutEnabledObj);
		}

		String freeEnabledObj = getStringPropertyValue(FREE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(freeEnabledObj)) {
			freeEnabled = "true".equals(freeEnabledObj);
		}

		String autoEnabledObj = getStringPropertyValue(AUTO_ENABLED, true);
		if(StringHelper.containsNonWhitespace(autoEnabledObj)) {
			autoEnabled = "true".equals(autoEnabledObj);
		}
		
		String autoExternalRefDelimiterObj = getStringPropertyValue(AUTO_EXTERNAL_REF_DELIMITER, true);
		if(StringHelper.containsNonWhitespace(autoExternalRefDelimiterObj)) {
			autoExternalRefDelimiter = autoExternalRefDelimiterObj;
		}

		String autoMultiBookingObj = getStringPropertyValue(AUTO_MULTI_BOOKING, true);
		if(StringHelper.containsNonWhitespace(autoMultiBookingObj)) {
			autoMultiBooking = "true".equals(autoMultiBookingObj);
		}

		String autoResetToPendingObj = getStringPropertyValue(AUTO_RESET_TO_PENDING, true);
		if(StringHelper.containsNonWhitespace(autoResetToPendingObj)) {
			autoResetToPending = "true".equals(autoResetToPendingObj);
		}
		
		String autoCancelationObj = getStringPropertyValue(AUTO_CANCELATION, true);
		if(StringHelper.containsNonWhitespace(autoCancelationObj)) {
			autoCancelation = "true".equals(autoCancelationObj);
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
				log.error("Error parsing the VAT: " + vatRateObj, e);
			}
		}

		String vatNrObj = getStringPropertyValue(VAT_NR, true);
		if(StringHelper.containsNonWhitespace(vatNrObj)) {
			vatNumber = vatNrObj;
		}
	}
	
	private void updateAccessMethods() {
		acMethodManager.enableMethod(TokenAccessMethod.class, isTokenEnabled());
		acMethodManager.enableMethod(FreeAccessMethod.class, isFreeEnabled());
		acMethodManager.enableMethod(PaypalAccessMethod.class, isPaypalEnabled());
		acMethodManager.enableMethod(PaypalCheckoutAccessMethod.class, isPaypalCheckoutEnabled());
		acMethodManager.enableAutoMethods(isAutoEnabled());
		dbInstance.commitAndCloseSession();
	}

	private void resetResetAutoStatusToPending() {
		if (autoResetToPending) {
			advanceOrderDao.resetStatusPending();
			dbInstance.commitAndCloseSession();
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(AC_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isTokenEnabled() {
		return tokenEnabled;
	}

	public void setTokenEnabled(boolean tokenEnabled) {
		this.tokenEnabled = tokenEnabled;
		setStringProperty(TOKEN_ENABLED, Boolean.toString(tokenEnabled), true);
		acMethodManager.enableMethod(TokenAccessMethod.class, tokenEnabled);
	}

	public boolean isFreeEnabled() {
		return freeEnabled;
	}

	public void setFreeEnabled(boolean freeEnabled) {
		this.freeEnabled = freeEnabled;
		setStringProperty(FREE_ENABLED, Boolean.toString(freeEnabled), true);
		if(acMethodManager != null) {
			acMethodManager.enableMethod(FreeAccessMethod.class, freeEnabled);
		}
	}

	public boolean isAutoEnabled() {
		return autoEnabled;
	}

	public void setAutoEnabled(boolean autoEnabled) {
		this.autoEnabled = autoEnabled;
		setStringProperty(AUTO_ENABLED, Boolean.toString(autoEnabled), true);
		acMethodManager.enableAutoMethods(autoEnabled);
	}

	public String getAutoExternalRefDelimiter() {
		return autoExternalRefDelimiter;
	}

	public void setAutoExternalRefDelimiter(String autoExternalRefDelimiter) {
		this.autoExternalRefDelimiter = autoExternalRefDelimiter;
		setStringProperty(AUTO_EXTERNAL_REF_DELIMITER, autoExternalRefDelimiter, true);
	}

	public boolean isAutoMultiBooking() {
		return autoMultiBooking;
	}

	public void setAutoMultiBooking(boolean autoMultiBooking) {
		this.autoMultiBooking = autoMultiBooking;
		setStringProperty(AUTO_MULTI_BOOKING, Boolean.toString(autoMultiBooking), true);
	}

	public boolean isAutoCancelation() {
		return autoCancelation;
	}

	public void setAutoCancelation(boolean autoCancelation) {
		this.autoCancelation = autoCancelation;
		setStringProperty(AUTO_CANCELATION, Boolean.toString(autoCancelation), true);
	}

	public boolean isPaypalEnabled() {
		return paypalEnabled;
	}

	public void setPaypalEnabled(boolean paypalEnabled) {
		this.paypalEnabled = paypalEnabled;
		setStringProperty(PAYPAL_ENABLED, Boolean.toString(paypalEnabled), true);
		acMethodManager.enableMethod(PaypalAccessMethod.class, paypalEnabled);
	}
	
	public boolean isPaypalCheckoutEnabled() {
		return paypalCheckoutEnabled;
	}

	public void setPaypalCheckoutEnabled(boolean enabled) {
		this.paypalCheckoutEnabled = enabled;
		setStringProperty(PAYPAL_CHECKOUT_ENABLED, Boolean.toString(enabled), true);
		acMethodManager.enableMethod(PaypalCheckoutAccessMethod.class, enabled);
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
		return new ArrayList<>(methodHandlers);
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