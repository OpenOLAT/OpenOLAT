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

import java.util.ArrayDeque;
import java.util.Deque;

import jakarta.websocket.SendHandler;
import jakarta.websocket.SendResult;
import jakarta.websocket.Session;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Wrap a WebSocket session and serialize the asynchronous sends. The
 * WebSocket specification allows only one outstanding asynchronous send
 * per session, the next message is sent by the completion callback. Messages
 * already waiting in the queue are not queued twice.
 *
 * Initial date: 9 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class WebSocketChannel implements SendHandler {

	private static final Logger log = Tracing.createLoggerFor(WebSocketChannel.class);

	private static final int MAX_QUEUE_SIZE = 32;

	private final Session session;
	private final Deque<String> queue = new ArrayDeque<>();
	private boolean sending;

	public WebSocketChannel(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}

	public boolean isOpen() {
		return session.isOpen();
	}

	public void send(String message) {
		synchronized(this) {
			if(sending) {
				if(!queue.contains(message) && queue.size() < MAX_QUEUE_SIZE) {
					queue.add(message);
				}
				return;
			}
			sending = true;
		}
		// never hold the lock while sending, the callback can be called by the same thread
		doSend(message);
	}

	private void doSend(String message) {
		try {
			session.getAsyncRemote().sendText(message, this);
		} catch (Exception e) {
			onResult(new SendResult(e));
		}
	}

	@Override
	public void onResult(SendResult result) {
		String next;
		synchronized(this) {
			if(!result.isOK() || !session.isOpen()) {
				if(!result.isOK()) {
					log.debug("Send message via WebSocket failed", result.getException());
				}
				queue.clear();
				sending = false;
				return;
			}

			next = queue.poll();
			if(next == null) {
				sending = false;
				return;
			}
		}
		doSend(next);
	}
}
