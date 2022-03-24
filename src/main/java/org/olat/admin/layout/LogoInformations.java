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
package org.olat.admin.layout;

import java.util.List;

import org.olat.admin.landingpages.LandingPagesModule;
import org.olat.admin.landingpages.model.Rules;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.prefs.Preferences;

/**
 * 
 * Wrapper to get the logo informations directly from the module.
 * 
 * Initial date: 21.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LogoInformations {

	private final UserSession userSession;
	private final LayoutModule layoutModule;
	private final LandingPagesModule landingPagesModule;
	
	public LogoInformations(UserRequest ureq, LayoutModule layoutModule, LandingPagesModule landingPagesModule) {
		this.layoutModule = layoutModule;
		this.landingPagesModule = landingPagesModule;
		userSession = ureq.getUserSession();
	}
	
	public boolean isLogo() {
		return layoutModule.isLogo();
	}
	
	public String getLogoAlt() {
		String logoAlt = layoutModule.getLogoAlt();
		if(!StringHelper.containsNonWhitespace(logoAlt)) {
			logoAlt = "OpenOlat - infinite learning";
		}
		return logoAlt;
	}
	
	public String getLogoUri() {
		return layoutModule.getLogoUri();
	}

	public LogoLinkURI getLogoLinkUri() {
		String logoLinkUri = null;
		String logoLinkType = layoutModule.getLogoLinkType();
		
		if(LogoURLType.landingpage.name().equals(logoLinkType)) {
			// 1) Use landing page config
			if(userSession != null && userSession.getGuiPreferences() != null) {
				Preferences prefs =  userSession.getGuiPreferences();
				String landingPage = (String)prefs.get(WindowManager.class, "landing-page");
				if(StringHelper.containsNonWhitespace(landingPage)) {
					// 1a) Use the user configured landing page
					logoLinkUri = Settings.getServerContextPathURI() + "/url/" + Rules.cleanUpLandingPath(landingPage);
				} else {
					 // 1b) or the system default landing page
					String landingBc = landingPagesModule.getRules().match(userSession);
					if(StringHelper.containsNonWhitespace(landingBc)) {
						List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(landingBc);
						logoLinkUri = BusinessControlFactory.getInstance().getAsURIString(ces, true);
					}
				}
			}
		} else if(StringHelper.containsNonWhitespace(layoutModule.getLogoLinkUri())) {
			// 2) Use the hard-coded link defined in the admin panel
			logoLinkUri = layoutModule.getLogoLinkUri();
		}
		
		if (logoLinkUri == null) {
			// 3) Fallback to loginpage
			if (userSession == null || !userSession.isAuthenticated()) {
				logoLinkUri = WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault();				
			} else {
				logoLinkUri = WebappHelper.getServletContextPath() + DispatcherModule.PATH_AUTHENTICATED;								
			}
		}
		
		// Convert to relative URL if it is an absolute URL for our server
		String serverURI = Settings.createServerURI();
		if(logoLinkUri.startsWith(serverURI)) {
			logoLinkUri = logoLinkUri.substring(serverURI.length());
			if(!logoLinkUri.startsWith("/")) {
				logoLinkUri = "/" + logoLinkUri;
			}
		}
		String target = logoLinkUri.startsWith("http") ? "_blank" : null;
		return new LogoLinkURI(logoLinkUri, target);
	}
	
	public static class LogoLinkURI {
		
		private final String uri;
		private final String target;
		
		public LogoLinkURI(String uri, String target) {
			this.uri = uri;
			this.target = target;
		}

		public String getUri() {
			return uri;
		}

		public String getTarget() {
			return target;
		}
	}
}
