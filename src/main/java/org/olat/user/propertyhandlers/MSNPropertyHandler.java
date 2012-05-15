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
 * Copyright (c) 2009 frentix GmbH, Switzerland<br>
 * <p>
 */

package org.olat.user.propertyhandlers;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.id.User;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientFactory;
import org.olat.user.UserManager;

/**
 * Implements a user property handler for MSN screen names.
 * 
 * <P>
 * Initial Date: Jul 28, 2009 <br>
 * 
 * @author twuersch
 */
public class MSNPropertyHandler extends Generic127CharTextPropertyHandler {

	public static final int MSN_NAME_MAX_LENGTH = 64;
	
	public static final String MSN_NAME_VALIDATION_URL = "http://spaces.live.com/profile.aspx";
	
	public static final String MSN_NAME_URL_PARAMETER = "mem";

	/**
	 * @see org.olat.user.AbstractUserPropertyHandler#getUserPropertyAsHTML(org.olat.core.id.User,
	 *      java.util.Locale)
	 */
	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		String msnname = getUserProperty(user, locale);
		if (StringHelper.containsNonWhitespace(msnname)) {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("<a href=\"" + MSN_NAME_VALIDATION_URL + "?" + MSN_NAME_URL_PARAMETER + "=" + msnname + "\" target=\"_blank\">" + msnname + "</a>");
			return stringBuffer.toString();
		} else {
			return null;
		}
	}
	
	/**
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#isValidValue(java.lang.String,
	 *      org.olat.core.gui.components.form.ValidationError, java.util.Locale)
	 */
	@Override
	public boolean isValidValue(String value, ValidationError validationError, Locale locale) {
		if (!super.isValidValue(value, validationError, locale)) {
			return false;
		}
		if (StringHelper.containsNonWhitespace(value)) {
			boolean valid = value.length() <= MSN_NAME_MAX_LENGTH;
			if(!valid && validationError != null) {
				validationError.setErrorKey("form.name.msn.error");
			}
			return valid;
		}
		return true;
	}

	/**
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#addFormItem(java.util.Locale,
	 *      org.olat.core.id.User, java.lang.String, boolean,
	 *      org.olat.core.gui.components.form.flexible.FormItemContainer)
	 */
	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser,
			FormItemContainer formItemContainer) {
		TextElement textElement = (TextElement) super.addFormItem(locale, user, usageIdentifyer, isAdministrativeUser, formItemContainer);
		textElement.setMaxLength(MSN_NAME_MAX_LENGTH);

		if (!UserManager.getInstance().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
			textElement.setExampleKey("form.example.msnname", null);
		}
		return textElement;
	}
	
	/**
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#isValid(org.olat.core.gui.components.form.flexible.FormItem, java.util.Map)
	 */
	@SuppressWarnings({"unchecked"})
	@Override
	public boolean isValid(FormItem formItem, Map formContext) {
		boolean result;
		TextElement textElement = (TextElement)formItem;
		OLog log = Tracing.createLoggerFor(this.getClass());
		if (StringHelper.containsNonWhitespace(textElement.getValue())) {
			
			// Use an HttpClient to fetch a profile information page from MSN.
			HttpClient httpClient = HttpClientFactory.getHttpClientInstance();
			HttpClientParams httpClientParams = httpClient.getParams();
			httpClientParams.setConnectionManagerTimeout(2500);
			httpClient.setParams(httpClientParams);
			HttpMethod httpMethod = new GetMethod(MSN_NAME_VALIDATION_URL);
			NameValuePair idParam = new NameValuePair(MSN_NAME_URL_PARAMETER, textElement.getValue());
			httpMethod.setQueryString(new NameValuePair[] {idParam});
			// Don't allow redirects since otherwise, we won't be able to get the correct status
			httpMethod.setFollowRedirects(false);
			try {
				// Get the user profile page
				httpClient.executeMethod(httpMethod);
				int httpStatusCode = httpMethod.getStatusCode();
				// Looking at the HTTP status code tells us whether a user with the given MSN name exists.
				if (httpStatusCode == HttpStatus.SC_MOVED_PERMANENTLY) {
					// If the user exists, we get a 301...
					result = true;
				} else if (httpStatusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
					// ...and if the user doesn't exist, MSN sends a 500.
					textElement.setErrorKey("form.name.msn.error", null);
					result = false;
				} else {
					// For HTTP status codes other than 301 and 500 we will assume that the given MSN name is valid, but inform the user about this.
					textElement.setExampleKey("form.example.msnname.notvalidated", null);
					log.warn("MSN name validation: Expected HTTP status 301 or 500, but got " + httpStatusCode);
					result = true;
				}
			} catch (Exception e) {
				// In case of any exception, assume that the given MSN name is valid (The opposite would block easily upon network problems), and inform the user about this.
				textElement.setExampleKey("form.example.msnname.notvalidated", null);
				log.warn("MSN name validation: Exception: " + e.getMessage());
				result = true;
			}
		} else {
			result = true;
		}
		log = null;
		return result;
	}
}
