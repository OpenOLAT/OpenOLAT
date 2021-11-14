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
package org.olat.course.nodes.basiclti;

import java.util.Enumeration;

import org.imsglobal.basiclti.BasicLTIUtil;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Encoder;
import org.olat.core.util.SortedProperties;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti.LTIManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTIDataExchangeDisclaimerController extends FormBasicController {
	
	private final boolean sendName;
	private final boolean sendMail;
	private final String customAttributes;
	
	private final String hashData;

	private SortedProperties userData = new SortedProperties(); 
	private SortedProperties customUserData = new SortedProperties(); 
	
	@Autowired
	private LTIManager ltiManager;
	
	public LTIDataExchangeDisclaimerController(UserRequest ureq, WindowControl wControl,
			boolean sendName, boolean sendMail, String customAttributes) {
		super(ureq, wControl, "accept");
		this.sendMail = sendMail;
		this.sendName = sendName;
		this.customAttributes = customAttributes;
		
		createExchangeDataProperties();
		hashData = createHashFromExchangeDataProperties();
		initForm(ureq);
	}
	
	public boolean hasData() {
		return !userData.isEmpty() || !customUserData.isEmpty();
	}
	
	public String getHashData() {
		return hashData;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer acceptPage = (FormLayoutContainer)formLayout;
			acceptPage.contextPut("userData", userData);
			acceptPage.contextPut("customUserData", customUserData);
		}
		uifactory.addFormSubmitButton("accept", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	/**
	 * Helper to read all user data that is exchanged with LTI tool and saves it
	 * to the userData and customUserData properties fields
	 */
	private void createExchangeDataProperties() {
		final User user = getIdentity().getUser();
		//user data
		if (sendName) {
			String lastName = user.getProperty(UserConstants.LASTNAME, getLocale());
			if(StringHelper.containsNonWhitespace(lastName)) {
				userData.put("lastName", lastName);
			}
			String firstName = user.getProperty(UserConstants.FIRSTNAME, getLocale());
			if(StringHelper.containsNonWhitespace(firstName)) {
				userData.put("firstName", firstName);
			}
		}
		if (sendMail) {
			String email = user.getProperty(UserConstants.EMAIL, getLocale());
			if(StringHelper.containsNonWhitespace(email)) {
				userData.put("email", email);
			}
		}
		// customUserData
		if (StringHelper.containsNonWhitespace(customAttributes)) {
			String[] params = customAttributes.split("[\n;]");
			for (int i = 0; i < params.length; i++) {
				String param = params[i];
				if (!StringHelper.containsNonWhitespace(param)) {
					continue;
				}
				
				int pos = param.indexOf("=");
				if (pos < 1 || pos + 1 > param.length()) {
					continue;
				}
				
				String key = BasicLTIUtil.mapKeyName(param.substring(0, pos));
				if(!StringHelper.containsNonWhitespace(key)) {
					continue;
				}
				
				String value = param.substring(pos + 1).trim();
				if(value.length() < 1) {
					continue;
				}
				
				if(value.startsWith(LTIManager.USER_PROPS_PREFIX)) {
					String userProp = value.substring(LTIManager.USER_PROPS_PREFIX.length(), value.length());
					if(LTIManager.USER_NAME_PROP.equals(userProp)) {
						value = ltiManager.getUsername(getIdentity());
					} else {
						value = user.getProperty(userProp, null);
					}
					if (value!= null) {
						customUserData.put(userProp, value);
					}
				}
			}
		}
	}
	

	/**
	 * Helper to create an MD5 hash from the exchanged user properties. 
	 * @return
	 */
	private String createHashFromExchangeDataProperties() {
		StringBuilder data = new StringBuilder();
		String hash = null;
		if (userData != null && userData.size() > 0) {
			Enumeration<Object> keys = userData.keys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				data.append(userData.getProperty(key));				
			}
		}
		if (customUserData != null && customUserData.size() > 0) {
			Enumeration<Object> keys = customUserData.keys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				data.append(customUserData.getProperty(key));				
			}
		}
		if (data.length() > 0) {
			hash = Encoder.md5hash(data.toString());
		}
		if (isLogDebugEnabled()) {
			logDebug("Create accept hash::" + hash + " for data::" + data);
		}
		return hash;
	}
}
