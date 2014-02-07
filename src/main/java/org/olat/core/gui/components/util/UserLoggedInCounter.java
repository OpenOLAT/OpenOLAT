/**
 * <a href=“http://www.openolat.org“>
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
 * 19.10.2012 by frentix GmbH, http://www.frentix.com
 * <p>
 **/


package org.olat.core.gui.components.util;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.util.session.UserSessionManager;

/**
 * <h3>Description:</h3>
 * <p>
 * This component shows the number of logged in users via web UI
 * <p>
 * Initial Date: 19.10.2012 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class UserLoggedInCounter extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new UserLoggedInCounterRenderer();
	private int currentCount = 0;

	/**
	 * Default constructor. Will initialize with the list of current web users.
	 */
	public UserLoggedInCounter() {
		super("UserCounter");
		this.currentCount = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSessionWebCounter();
		this.setSpanAsDomReplaceable(true);
	}
	
	/**
	 * Will return true whenever the user count has been changed
	 */
	@Override
	public boolean isDirty() {
		int lastCount = currentCount;
		currentCount = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSessionWebCounter();
		return (currentCount != lastCount);
	}

	/**
	 * @return The number of users logged in using the web client, excluding
	 *         webdav and rest users.
	 */
	public int getSessionCount() {
		return this.currentCount ;			
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// nothing to dispatch
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

}