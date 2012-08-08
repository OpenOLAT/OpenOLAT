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
package org.olat.instantMessaging;

import org.jivesoftware.smack.XMPPConnection;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CloseConnectionTask implements Runnable {
	
	private OLog log = Tracing.createLoggerFor(CloseConnectionTask.class);
	
	private final XMPPConnection connectionToClose;
	
	protected CloseConnectionTask(XMPPConnection connectionToClose) {
		this.connectionToClose = connectionToClose;
	}
	
	public void run() {
		try {
			if (connectionToClose != null && connectionToClose.isConnected()){
				connectionToClose.disconnect();
			}
		} catch (RuntimeException e) {
			log.warn("Error while trying to close instant messaging connection", e);
		}
	}

}
