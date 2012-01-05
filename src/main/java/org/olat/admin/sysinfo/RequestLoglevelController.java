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
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <hr>
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * This file has been modified by the OpenOLAT community. Changes are licensed
 * under the Apache 2.0 license as the original file.
 */
package org.olat.admin.sysinfo;

import org.apache.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.threadlog.RequestBasedLogLevelManager;
import org.olat.core.util.threadlog.UserBasedLogLevelManager;

/**
 * A rather simply kept controller used for the requestloglevel feature which
 * was added to debug special cases with 'slow requests'.
 * <p>
 * It allows to mark particular requests (based on ip address or username - dont
 * overlap those two though!) with a specific loglevel and even an appender.
 * <p>
 * That way you can have all requests from say user 'administrator' logged with
 * log level DEBUG and sent to appender 'DebugLog' (which is a standard log4j
 * appender and can therefore for example be writing to a different file than
 * the rest of the log events).
 * <P>
 * Initial Date: 13.09.2010 <br>
 * 
 * @author Stefan
 */
public class RequestLoglevelController extends FormBasicController {

	// private RequestLoglevelForm form;

	private final RequestBasedLogLevelManager requestBasedLogLevelManager;

	private final UserBasedLogLevelManager userBasedLogLevelManager;

	private TextElement txeRemoteIps;
	private TextElement txeUsernames;

	protected RequestLoglevelController(UserRequest ureq, WindowControl control) {
		super(ureq, control);

		requestBasedLogLevelManager = RequestBasedLogLevelManager.getInstance();
		userBasedLogLevelManager = UserBasedLogLevelManager.getInstance();

		if (requestBasedLogLevelManager == null && userBasedLogLevelManager == null) {
			VelocityContainer requestlogleveldisabled = createVelocityContainer("requestlogleveldisabled");
			putInitialPanel(requestlogleveldisabled);
		} else {
			initForm(ureq);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("requestloglevel.format.label", getTranslator().translate("requestloglevel.format.text"), formLayout);
		txeRemoteIps = uifactory.addTextAreaElement("requestloglevel.ips", 10, 60, "", formLayout);
		txeUsernames = uifactory.addTextAreaElement("requestloglevel.usernames", 10, 60, "", formLayout);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

	private String[] getUsernamesAndLevels() {
		String[] retVal = txeUsernames.getValue().split("\r\n");
		return retVal;
	}

	private String getRawUsernames() {
		return txeUsernames.getValue();
	}

	private String[] getIpsAndLevels() {
		String[] retVal = txeRemoteIps.getValue().split("\r\n");
		return retVal;
	}

	private String getRawIpsAndLevels() {
		return txeRemoteIps.getValue();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
		String[] usernamesALevels = getUsernamesAndLevels();
		String[] ipsALevels = getIpsAndLevels();
		
		if (requestBasedLogLevelManager != null) {
			requestBasedLogLevelManager.reset();

			requestBasedLogLevelManager.storeIpsAndLevels(getRawIpsAndLevels());

			for (int i = 0; i < ipsALevels.length; i++) {
				String ip = ipsALevels[i];
				if (ip != null && ip.length() > 0 && ip.contains("=")) {
					try {
						requestBasedLogLevelManager.setLogLevelAndAppender(ip);
					} catch (Exception e) {
						Logger.getLogger(getClass()).warn("Couldnt set loglevel for remote address: " + ip, e);
					}
				}
			}
		}

		if (userBasedLogLevelManager != null) {
			userBasedLogLevelManager.storeUsernameAndLevels(getRawUsernames());

			userBasedLogLevelManager.reset();
			for (int i = 0; i < usernamesALevels.length; i++) {
				String username = usernamesALevels[i];
				if (username != null && username.length() > 0 && username.contains("=")) {
					try {
						userBasedLogLevelManager.setLogLevelAndAppender(username);
					} catch (Exception e) {
						Logger.getLogger(getClass()).warn("Couldnt set loglevel for username: " + username, e);
					}
				}
			}
		}

	}

}