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

import java.io.IOException;

import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;

/**
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OpenOLATWebSocket {
	
	private static final Logger log = Tracing.createLoggerFor(OpenOLATWebSocket.class);
	
	public static final String USER_KEY = "userKey";
	
	@OnOpen
	public void onOpen(Session wsSession, EndpointConfig config)
	throws IOException {
        Object userKey = config.getUserProperties().get(USER_KEY);
        if (userKey == null) {
        	wsSession.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "Not authenticated"));
            return;
        }
    	Object windowDispatchId = config.getUserProperties().get(OpenOLATWebSocketConfigurator.WINDOW_DISPATCH_ID);
        // carry it on the ws session itself, available in @OnMessage/@OnClose
        wsSession.getUserProperties().putAll(config.getUserProperties());
        if(userKey instanceof UserSession usess && usess.isAuthenticated()) {
            Window window = Windows.getWindows(usess).getWindowByDispatchId(windowDispatchId.toString());
            if(window != null) {
            	window.getWindowBackOffice().registerWebSocketSession(wsSession);
                log.debug("Open websocket: {}", windowDispatchId);
            } else {
            	wsSession.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "Window not found"));
            }	
        } else {
        	wsSession.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "Not authenticated"));
        }
    }

	/**
	 * 
	 * @param wsSession The WebSocket session
	 */
    @OnClose
    public void onClose(Session wsSession) {
    	Object windowDispatchId = wsSession.getUserProperties().get(OpenOLATWebSocketConfigurator.WINDOW_DISPATCH_ID);
    	Object userKey = wsSession.getUserProperties().get(USER_KEY);
    	if(userKey instanceof UserSession usess && windowDispatchId != null) {
    		Window window = Windows.getWindows(usess).getWindowByDispatchId(windowDispatchId.toString());
    		if(window != null) {
            	window.getWindowBackOffice().deregisterWebSocketSession(wsSession);
    		}
    	}
    	wsSession.getUserProperties().remove(USER_KEY);
    }
    
    /**
     * 
     * @param message A message
     * @param wsSession The WebSocket session
     */
    @OnMessage
    public void onMessage(String message, Session wsSession) {
    	log.debug("Message: {}", message);
    }
    
    @OnError
    public void onError(Throwable e) {
    	log.debug("", e);
    }
}
