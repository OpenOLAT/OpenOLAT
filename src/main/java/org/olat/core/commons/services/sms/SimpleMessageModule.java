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
package org.olat.core.commons.services.sms;

import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyUsageContext;
import org.olat.user.propertyhandlers.ui.UsrPropCfgManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SimpleMessageModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String SMS_ENABLED = "message.enabled";
	private static final String PROVIDER_ID = "message.provider.id";
	private static final String ASK_BY_FIRST_LOGIN_ENABLED = "message.ask.by.first.login";
	private static final String RESET_ENABLED = "message.reset.password.enabled";
	
	@Value("${message.enabled:false}")
	private boolean enabled;
	@Value("${message.reset.password.enabled:true}")
	private boolean resetPassword;
	@Value("${message.ask.by.first.login:true}")
	private boolean askByFirstLogin;
	@Value("${message.provider:WebSMS}")
	private String providerId;
	

	@Autowired
	private UsrPropCfgManager usrPropCfgMng;
	
	@Autowired
	public SimpleMessageModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(SMS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String resetEnabledObj = getStringPropertyValue(RESET_ENABLED, true);
		if(StringHelper.containsNonWhitespace(resetEnabledObj)) {
			resetPassword = "true".equals(resetEnabledObj);
		}
		
		String askByFirstLoginEnabledObj = getStringPropertyValue(ASK_BY_FIRST_LOGIN_ENABLED, true);
		if(StringHelper.containsNonWhitespace(askByFirstLoginEnabledObj)) {
			askByFirstLogin = "true".equals(askByFirstLoginEnabledObj);
		}
		
		providerId = getStringPropertyValue(PROVIDER_ID, providerId);
		
		if(enabled) {//check
			enableSmsUserProperty();
		}
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
		this.enabled = enabled;
		setStringProperty(SMS_ENABLED, Boolean.toString(enabled), true);
	}
	
	private void enableSmsUserProperty() {
		List<UserPropertyHandler> handlers = usrPropCfgMng.getUserPropertiesConfigObject().getPropertyHandlers();
		
		UserPropertyHandler smsHandler = null;
		UserPropertyHandler mobileHandler = null;
		for(UserPropertyHandler handler:handlers) {
			if(UserConstants.SMSTELMOBILE.equals(handler.getName())) {
				smsHandler = handler;
			} else if(UserConstants.TELMOBILE.equals(handler.getName())) {
				mobileHandler = handler;
			}
		}
		
		if(smsHandler != null) {
			UserPropertyUsageContext context = usrPropCfgMng.getUserPropertiesConfigObject()
					.getUsageContexts().get("org.olat.user.ProfileFormController");
			if(!context.contains(smsHandler)) {
				if(mobileHandler == null) {
					context.addPropertyHandler(smsHandler);
				} else {
					int index = context.getPropertyHandlers().indexOf(mobileHandler);
					context.addPropertyHandler(index + 1, smsHandler);
				}
			}
		}
	}

	public boolean isResetPasswordEnabled() {
		return resetPassword;
	}

	public void setResetPasswordEnabled(boolean resetPassword) {
		this.resetPassword = resetPassword;
		setStringProperty(RESET_ENABLED, Boolean.toString(resetPassword), true);
	}
	
	public boolean isAskByFirstLogin() {
		return askByFirstLogin;
	}

	public void setAskByFirstLogin(boolean askByFirstLogin) {
		this.askByFirstLogin = askByFirstLogin;
		setStringProperty(ASK_BY_FIRST_LOGIN_ENABLED, Boolean.toString(askByFirstLogin), true);
	}

	public String getProviderId() {
		return providerId == null ? null : providerId.toLowerCase();
	}
	
	public void setProviderId(String providerId) {
		this.providerId = providerId;
		setStringProperty(PROVIDER_ID, providerId, true);
	}
}
