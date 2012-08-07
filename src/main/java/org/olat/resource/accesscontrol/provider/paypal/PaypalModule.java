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
package org.olat.resource.accesscontrol.provider.paypal;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.GenericEventListener;

/**
 * 
 * Description:<br>
 * The PaypalModule only provide the informations to access a Business Account.
 * To enable/disable the module, check the configuration of FXAccessControlModul
 * 
 * <P>
 * Initial Date:  25 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PaypalModule extends AbstractOLATModule implements GenericEventListener {
	
	private static final String X_PAYPAL_SECURITY_USERID = "paypal.security.user.id";
	private static final String X_PAYPAL_SECURITY_PASSWORD = "paypal.security.password";
	private static final String X_PAYPAL_SECURITY_SIGNATURE = "paypal.security.signature";
	private static final String X_PAYPAL_APPLICATION_ID = "paypal.application.id";
	private static final String X_PAYPAL_SANDBOX_EMAIL_ADDRESS = "paypal.sandbox.email";
	private static final String DATA_FORMAT_XML = "paypal.data.format";
	private static final String SANDBOX = "paypal.sandbox";
	private static final String FIRST_RECEIVER_EMAIL_ADDRESS = "paypal.first.receiver.email";
	private static final String DEVICE_IP_ADDRESS = "paypal.device.ip";

	private static final String PAYPAL_CURRENCY = "paypal.currency";
	private static final String DEFAULT_PAYPAL_DATA_FORMAT = "XML";

	private boolean sandbox = true;

	private String paypalSecurityUserId;
	private String paypalSecurityPassword;
	private String paypalSecuritySignature;
	private String paypalApplicationId;
	private String paypalSandboxEmailAddress;
	private String paypalFirstReceiverEmailAddress;
	private String deviceIpAddress;
	
	private String paypalCurrency;
	private String paypalDataFormat;
	
	public PaypalModule() {
		//
	}

	@Override
	public void init() {
		String paypalSecurityUserIdProp = getStringPropertyValue(X_PAYPAL_SECURITY_USERID, true);
		if(StringHelper.containsNonWhitespace(paypalSecurityUserIdProp)) {
			paypalSecurityUserId = paypalSecurityUserIdProp;
		}
		
		String paypalSecurityPasswordProp = getStringPropertyValue(X_PAYPAL_SECURITY_PASSWORD, true);
		if(StringHelper.containsNonWhitespace(paypalSecurityPasswordProp)) {
			paypalSecurityPassword = paypalSecurityPasswordProp;
		}
		
		String paypalSecuritySignatureProp = getStringPropertyValue(X_PAYPAL_SECURITY_SIGNATURE, true);
		if(StringHelper.containsNonWhitespace(paypalSecuritySignatureProp)) {
			paypalSecuritySignature = paypalSecuritySignatureProp;
		}
		
		String paypalApplicationIdProp = getStringPropertyValue(X_PAYPAL_APPLICATION_ID, true);
		if(StringHelper.containsNonWhitespace(paypalApplicationIdProp)) {
			paypalApplicationId = paypalApplicationIdProp;
		}
		
		String paypalSandboxEmailAddressProp = getStringPropertyValue(X_PAYPAL_SANDBOX_EMAIL_ADDRESS, true);
		if(StringHelper.containsNonWhitespace(paypalSandboxEmailAddressProp)) {
			paypalSandboxEmailAddress = paypalSandboxEmailAddressProp;
		}
		
		String paypalDataFormatProp = getStringPropertyValue(DATA_FORMAT_XML, true);
		if(StringHelper.containsNonWhitespace(paypalDataFormatProp)) {
			paypalDataFormat = paypalDataFormatProp;
		}
		
		String currencyAllowedProp = getStringPropertyValue(PAYPAL_CURRENCY, true);
		if(StringHelper.containsNonWhitespace(currencyAllowedProp)) {
			paypalCurrency = currencyAllowedProp;
		}
		
		String paypalFirstReceiverEmailAddressProp = getStringPropertyValue(FIRST_RECEIVER_EMAIL_ADDRESS, true);
		if(StringHelper.containsNonWhitespace(paypalFirstReceiverEmailAddressProp)) {
			paypalFirstReceiverEmailAddress = paypalFirstReceiverEmailAddressProp;
		}
		
		String sandboxObj = getStringPropertyValue(SANDBOX, true);
		if(StringHelper.containsNonWhitespace(sandboxObj)) {
			sandbox = "true".equals(sandboxObj);
		}
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	@Override
	protected void initDefaultProperties() {
		paypalSecurityUserId = getStringConfigParameter(X_PAYPAL_SECURITY_USERID, "", true);
		paypalSecurityPassword = getStringConfigParameter(X_PAYPAL_SECURITY_PASSWORD, "", true);
		paypalSecuritySignature = getStringConfigParameter(X_PAYPAL_SECURITY_SIGNATURE, "", true);
		paypalApplicationId = getStringConfigParameter(X_PAYPAL_APPLICATION_ID, "", true);
		paypalSandboxEmailAddress = getStringConfigParameter(X_PAYPAL_SANDBOX_EMAIL_ADDRESS, "", true);
		paypalDataFormat = getStringConfigParameter(DATA_FORMAT_XML, DEFAULT_PAYPAL_DATA_FORMAT, true);
		paypalFirstReceiverEmailAddress = getStringConfigParameter(FIRST_RECEIVER_EMAIL_ADDRESS, "", true);
		sandbox = getBooleanConfigParameter(SANDBOX, false);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	public boolean isSandbox() {
		return sandbox;
	}

	public void setSandbox(boolean sandbox) {
		this.sandbox = sandbox;
	}
	
	public String getDeviceIpAddress() throws UnknownHostException {
		if(StringHelper.containsNonWhitespace(deviceIpAddress)) {
			return deviceIpAddress;
		}	
		String host = Settings.getServerconfig("server_fqdn");
		InetAddress addr = InetAddress.getByName(host);
		String ipAddrStr = addr.getHostAddress();
		return ipAddrStr;
	}
	
	public void setDeviceIpAddress(String deviceIpAddress) {
		setStringProperty(DEVICE_IP_ADDRESS, deviceIpAddress, true);
	}

	public String getPaypalSecurityUserId() {
		return paypalSecurityUserId;
	}

	public void setPaypalSecurityUserId(String paypalSecurityUserId) {
		setStringProperty(X_PAYPAL_SECURITY_USERID, paypalSecurityUserId, true);
	}

	public String getPaypalSecurityPassword() {
		return paypalSecurityPassword;
	}

	public void setPaypalSecurityPassword(String paypalSecurityPassword) {
		setStringProperty(X_PAYPAL_SECURITY_PASSWORD, paypalSecurityPassword, true);
	}

	public String getPaypalSecuritySignature() {
		return paypalSecuritySignature;
	}

	public void setPaypalSecuritySignature(String paypalSecuritySignature) {
		setStringProperty(X_PAYPAL_SECURITY_SIGNATURE, paypalSecuritySignature, true);
	}

	public String getPaypalApplicationId() {
		return paypalApplicationId;
	}

	public void setPaypalApplicationId(String paypalApplicationId) {
		setStringProperty(X_PAYPAL_APPLICATION_ID, paypalApplicationId, true);
	}

	public String getPaypalSandboxEmailAddress() {
		return paypalSandboxEmailAddress;
	}

	public void setPaypalSandboxEmailAddress(String paypalSandboxEmailAddress) {
		setStringProperty(X_PAYPAL_SANDBOX_EMAIL_ADDRESS, paypalSandboxEmailAddress, true);
	}

	public String getPaypalFirstReceiverEmailAddress() {
		return paypalFirstReceiverEmailAddress;
	}

	public void setPaypalFirstReceiverEmailAddress(String paypalFirstReceiverEmailAddress) {
		setStringProperty(FIRST_RECEIVER_EMAIL_ADDRESS, paypalFirstReceiverEmailAddress, true);
	}

	public String getPaypalCurrency() {
		return paypalCurrency;
	}

	public void setPaypalCurrency(String paypalCurrency) {
		setStringProperty(PAYPAL_CURRENCY, paypalCurrency, true);
	}

	public String getPaypalDataFormat() {
		if(paypalDataFormat == null) {
			paypalDataFormat = DEFAULT_PAYPAL_DATA_FORMAT;
		}
		return paypalDataFormat;
	}

	public void setPaypalDataFormat(String paypalDataFormat) {
		this.paypalDataFormat = paypalDataFormat;
	}

	public boolean isUseProxy() {
		return false;
	}
}
