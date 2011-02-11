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
 * Implements a user property handler for XING screen names.
 * 
 * <P>
 * Initial Date: Jul 28, 2009 <br>
 * 
 * @author twuersch
 */
public class XingPropertyHandler extends Generic127CharTextPropertyHandler {

	public static final int XING_NAME_MAX_LENGTH = 320;
	public static final String XING_NAME_VALIDATION_URL = "http://www.xing.com/profile/";

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
			return value.length() <= XING_NAME_MAX_LENGTH;
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
		textElement.setMaxLength(XING_NAME_MAX_LENGTH);

		if (!UserManager.getInstance().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
			textElement.setExampleKey("form.example.xingname", null);
		}
		return textElement;
	}
	
	/**
	 * @see org.olat.user.AbstractUserPropertyHandler#getUserPropertyAsHTML(org.olat.core.id.User, java.util.Locale)
	 */
	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		String xingname = getUserProperty(user, locale);
		if (StringHelper.containsNonWhitespace(xingname)) {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("<a href=\"" + XING_NAME_VALIDATION_URL + xingname + "\" target=\"_blank\">" + xingname + "</a>");
			return stringBuffer.toString();
		} else {
			return null;
		}
	}
	
	/**
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#isValid(org.olat.core.gui.components.form.flexible.FormItem, java.util.Map)
	 */
	@SuppressWarnings({"unused", "unchecked"})
	@Override
	public boolean isValid(FormItem formItem, Map formContext) {
		boolean result;
		TextElement textElement = (TextElement)formItem;
		OLog log = Tracing.createLoggerFor(this.getClass());
		if (StringHelper.containsNonWhitespace(textElement.getValue())) {
			HttpClient httpClient = HttpClientFactory.getHttpClientInstance();
			HttpClientParams httpClientParams = httpClient.getParams();
			httpClientParams.setConnectionManagerTimeout(2500);
			httpClient.setParams(httpClientParams);
			try {
				// Could throw IllegalArgumentException if argument is not a valid url
				// (e.g. contains whitespaces)
				HttpMethod httpMethod = new GetMethod(XING_NAME_VALIDATION_URL + textElement.getValue());
				// Don't allow redirects since otherwise, we won't be able to get the correct status
				httpMethod.setFollowRedirects(false);
				// Get the user profile page
				httpClient.executeMethod(httpMethod);
				int httpStatusCode = httpMethod.getStatusCode();
				// Looking at the HTTP status code tells us whether a user with the given Xing name exists.
				if (httpStatusCode == HttpStatus.SC_OK) {
					// If the user exists, we get a 200...
					result = true;
				} else if (httpStatusCode == HttpStatus.SC_MOVED_PERMANENTLY) {
					// ... and if he doesn't exist, we get a 301. 
					textElement.setErrorKey("form.name.xing.error", null);
					result = false;
				} else {
					// In case of any exception, assume that the given MSN name is valid (The opposite would block easily upon network problems), and inform the user about this.
					textElement.setExampleKey("form.example.xingname.notvalidated", null);
					log.warn("Xing name validation: Expected HTTP status 200 or 301, but got " + httpStatusCode);
					result = true;
				}
			} catch (IllegalArgumentException e) {
				// The xing name is not url compatible (e.g. contains whitespaces)
				textElement.setErrorKey("form.xingname.notvalid", null);
				result = false;
			} catch (Exception e) {
				// In case of any exception, assume that the given MSN name is valid (The opposite would block easily upon network problems), and inform the user about this.
				textElement.setExampleKey("form.example.xingname.notvalidated", null);
				log.warn("Xing name validation: Exception: " + e.getMessage());
				result = true;
			}
		} else {
			result = true;
		}
		log = null;
		return result;
	}
}
