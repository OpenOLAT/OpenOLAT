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
package org.olat.core.util.websocket;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

import org.olat.core.CoreSpringFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.util.UserSession;
import org.olat.core.util.http.OriginValidator;

/**
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OpenOLATWebSocketConfigurator extends ServerEndpointConfig.Configurator {
	
	public static final String HTTP_SESSION_ID = "httpSessionId";
	public static final String WINDOW_DISPATCH_ID = "windowDispatchId";
	private static final String USERSESSIONKEY = UserSession.class.getName();
	
	private OriginValidator originValidator;
	
	@Override
	public boolean checkOrigin(String originHeaderValue) {
		return originValidator().isAllowed(originHeaderValue);
	}

	@Override
	public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
		// 1. Module enabled?
		if(!CoreSpringFactory.getImpl(WebSocketModule.class).isEnabled()) {
			return;
		}
		
		List<String> oow = request.getParameterMap().get("oow");
		HttpSession session = (HttpSession) request.getHttpSession();
		if (session == null || oow == null || oow.isEmpty()) {
			return; // no session, no window -> reject in @OnOpen
		}
		// 2. Copy out the identity NOW, don't keep the HttpSession object
		// We write these informations in the configuration because Tomcat
		// has a config per session.
		config.getUserProperties().put(HTTP_SESSION_ID, session.getId());
		config.getUserProperties().put(WINDOW_DISPATCH_ID, oow.get(0));
		UserSession us = (UserSession) session.getAttribute(USERSESSIONKEY);
		config.getUserProperties().put(OpenOLATWebSocket.USER_KEY, us);
	}
	
	private synchronized OriginValidator originValidator() {
		if(originValidator == null) {
			List<String> domains = new ArrayList<>(3);
			domains.add(Settings.getServerDomainName());
			if(Settings.isContentDomainNameEnabled()) {
				domains.add(Settings.getContentDomainName());
			}
			originValidator = new OriginValidator(domains, Settings.isSecurePortAvailable());
		}
		return originValidator;
	}
}
