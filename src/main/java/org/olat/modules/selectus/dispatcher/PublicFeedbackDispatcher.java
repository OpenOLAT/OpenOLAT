/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.dispatcher;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.session.UserSessionManager;
import org.olat.login.oauth.OAuthConstants;
import org.olat.login.oauth.OAuthResource;
import org.olat.login.oauth.spi.MicrosoftAzureADFSProvider;

/**
 * 
 * Initial date: 30 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PublicFeedbackDispatcher extends AbstractRecruitingDispatcher {
	
	public static final String PUBLIC_FEEDBACK_PATH = "publicfeedback/";
	public static final String PUBLIC_FEEDBACK_BPATH = "publicfeedback/0";
	public static final String PUBLIC_FEEDBACK_SOURCE = "publicfeedback";
	public static final String PUBLIC_FEEDBACK_ID = "publicfeedback-id";

	public PublicFeedbackDispatcher() {
		super(PUBLIC_FEEDBACK_SOURCE);
	}
	
	@Override
	protected Locale getLang(UserRequest ureq) {
		return I18nModule.getDefaultLocale();
	}

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		MicrosoftAzureADFSProvider azureProvider = CoreSpringFactory.getImpl(MicrosoftAzureADFSProvider.class);
		if(azureProvider.isEnabled()) {
			UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
			if(usess != null && usess.isAuthenticated()) {
				super.execute(request, response);
			} else if(usess != null) {
				HttpSession session = request.getSession();
				usess.putEntryInNonClearedStore(OAuthConstants.OAUTH_TRANSIENT_IDENTITY, Boolean.TRUE);
				usess.putEntryInNonClearedStore("redirect-bc", PUBLIC_FEEDBACK_BPATH);
				String feedbackId = getPublichFeedbackId(request);
				usess.putEntryInNonClearedStore(PUBLIC_FEEDBACK_ID, feedbackId);
				OAuthResource.redirect(azureProvider, response, session);
			}	
		}
	}
	
	private String getPublichFeedbackId(HttpServletRequest request) {	
		String uri = request.getRequestURI();
		int index = uri.indexOf(PUBLIC_FEEDBACK_PATH);
		if(index >= 0) {
			String id = uri.substring(index + PUBLIC_FEEDBACK_PATH.length());
			if(id.startsWith("/")) {
				id = id.substring(1);
			}
			return id;
		}
		return "";
	}
}
