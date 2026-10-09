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

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpointConfig;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OpenOLATWebSocketContextListener implements ServletContextListener {
	
	private static final Logger log = Tracing.createLoggerFor(OpenOLATWebSocketContextListener.class);
	
    @Override
    public void contextInitialized(ServletContextEvent event) {
		try {
			ServerEndpointConfig config = ServerEndpointConfig.Builder
					.create(OpenOLATWebSocket.class, "/ws/notifications")
					.configurator(new OpenOLATWebSocketConfigurator())
					.build();
			Object container = event.getServletContext().getAttribute("jakarta.websocket.server.ServerContainer");
			if(container instanceof ServerContainer serverContainer) {
				serverContainer.addEndpoint(config);
			} else {
				log.warn("Cannot initialize WebSocket endpoint");
			}
		} catch (DeploymentException e) {
			log.error("", e);
		}
    }
}
