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
 * Implements a user property handler for ICQ screen names.
 * 
 * <P>
 * Initial Date: Jul 28, 2009 <br>
 * 
 * @author twuersch
 */
public class ICQPropertyHandler extends Generic127CharTextPropertyHandler {

	public static final int ICQ_NAME_MAX_LENGTH = 16;
	public static final String ICQ_INDICATOR_URL = "http://status.icq.com/online.gif"; 
	public static final String ICQ_NAME_VALIDATION_URL = "http://www.icq.com/people/about_me.php";
	public static final String ICQ_NAME_URL_PARAMETER = "uin";

	/**
	 * @see org.olat.user.AbstractUserPropertyHandler#getUserPropertyAsHTML(org.olat.core.id.User,
	 *      java.util.Locale)
	 */
	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		String icqname = getUserProperty(user, locale);
		if (StringHelper.containsNonWhitespace(icqname)) {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("<a href=\"" + ICQ_NAME_VALIDATION_URL + "?" + ICQ_NAME_URL_PARAMETER +"=" + icqname + "\" target=\"_blank\">" + icqname + "</a>");
			stringBuffer.append("<img src=\"" + ICQ_INDICATOR_URL + "?icq=" + icqname + "&img=5\" style=\"width:10px; height:10px; margin-left:2px;\">");
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
			return value.length() <= ICQ_NAME_MAX_LENGTH;
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
		textElement.setMaxLength(ICQ_NAME_MAX_LENGTH);

		if (!UserManager.getInstance().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
			textElement.setExampleKey("form.example.icqname", null);
		}
		return textElement;
	}
	
	/**
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#isValid(org.olat.core.gui.components.form.flexible.FormItem, java.util.Map)
	 */
	@SuppressWarnings({"unchecked", "unused"})
	@Override
	public boolean isValid(FormItem formItem, Map formContext) {
		boolean result;
		TextElement textElement = (TextElement)formItem;
		OLog log = Tracing.createLoggerFor(this.getClass());
		if (StringHelper.containsNonWhitespace(textElement.getValue())) {
			
			// Use an HttpClient to fetch a profile information page from ICQ.
			HttpClient httpClient = HttpClientFactory.getHttpClientInstance();
			HttpClientParams httpClientParams = httpClient.getParams();
			httpClientParams.setConnectionManagerTimeout(2500);
			httpClient.setParams(httpClientParams);
			HttpMethod httpMethod = new GetMethod(ICQ_NAME_VALIDATION_URL);
			NameValuePair uinParam = new NameValuePair(ICQ_NAME_URL_PARAMETER, textElement.getValue());
			httpMethod.setQueryString(new NameValuePair[] {uinParam});
			// Don't allow redirects since otherwise, we won't be able to get the HTTP 302 further down.
			httpMethod.setFollowRedirects(false);
			try {
				// Get the user profile page
				httpClient.executeMethod(httpMethod);
				int httpStatusCode = httpMethod.getStatusCode();
				// Looking at the HTTP status code tells us whether a user with the given ICQ name exists.
				if (httpStatusCode == HttpStatus.SC_OK) {
					// ICQ tells us that a user name is valid if it sends an HTTP 200...
					result = true;
				} else if (httpStatusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
					// ...and if it's invalid, it sends an HTTP 302.
					textElement.setErrorKey("form.name.icq.error", null);
					result = false;
				} else {
					// For HTTP status codes other than 200 and 302 we will silently assume that the given ICQ name is valid, but inform the user about this.
					textElement.setExampleKey("form.example.icqname.notvalidated", null);
					log.warn("ICQ name validation: Expected HTTP status 200 or 301, but got " + httpStatusCode);
					result = true;
				}
			} catch (Exception e) {
				// In case of any exception, assume that the given ICQ name is valid (The opposite would block easily upon network problems), and inform the user about this.
				textElement.setExampleKey("form.example.icqname.notvalidated", null);
				log.warn("ICQ name validation: Exception: " + e.getMessage());
				result = true;
			}
		} else {
			result = true;
		}
		log = null;
		return result;
	}
}
