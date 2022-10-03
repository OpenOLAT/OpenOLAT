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
* <p>
*/
package org.olat.core.gui.control.generic.messages;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;

/**
 * Description:<br>
 * The place to create simple message controllers to be used in content area
 * 
 * <P>
 * Initial Date: 30.11.2007 <br>
 * 
 * @author patrickb
 */
public class MessageUIFactory {

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param title
	 * @param text
	 * @return
	 */
	public static MessageController createInfoMessage(UserRequest ureq, WindowControl wControl, String title, String text) {
		return new MessageController(ureq, wControl, MessageController.INFO, title, text);
	}

	public static MessageController createErrorMessage(UserRequest ureq, WindowControl wControl, String title, String text) {
		return new MessageController(ureq, wControl, MessageController.ERROR, title, text);
	}

	public static MessageController createWarnMessage(UserRequest ureq, WindowControl wControl, String title, String text) {
		return new MessageController(ureq, wControl, MessageController.WARN, title, text);
	}

	public static SimpleMessageController createSimpleMessage(UserRequest ureq, WindowControl wControl, String text) {
		return new SimpleMessageController(ureq, wControl, text, null);
	}

	public static SimpleMessageController createFormattedMessage(UserRequest ureq, WindowControl wControl, String text, String cssName) {
		return new SimpleMessageController(ureq, wControl, text, cssName);
	}
	
	/**
	 * Create a message to be shown for guest users when the requested feature is not allowed for guest users
	 * @param ureq
	 * @param wControl
	 * @param reason null or a reason that describes why the feature is not ok for guests.
	 * @return
	 */
	public static GuestNoAccessMessageController createGuestNoAccessMessage(UserRequest ureq, WindowControl wControl, String reason) {
		return new GuestNoAccessMessageController(ureq, wControl, reason);
	}

}
