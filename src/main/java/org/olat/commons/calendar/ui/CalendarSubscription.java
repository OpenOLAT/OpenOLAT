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

package org.olat.commons.calendar.ui;

import org.olat.core.gui.control.Controller;

public interface CalendarSubscription {

	/**
	 * Tells wether this subscription is active or not.
	 * @return
	 */
	public boolean isSubscribed();
	
	/**
	 * Trigger a subscribe action. If the implementation needs
	 * a GUI workflow, then a controller should be returned.
	 * If no GUI workflow is needed, null may be returned.
	 * If a controller is returned, the controller should
	 * issue an Event.DONE_EVENT if the task finished or
	 * a Event.CANCELLED_EVENT if the task was cancelled.
	 * 
	 * @return
	 */
	public Controller triggerSubscribeAction();
	
	/**
	 * Subscribe to the calendar, but if the user as say no,
	 * it will not subscribe. Use force = true to always subscribe.
	 * @param force
	 */
	public void subscribe(boolean force);
	
	/**
	 * Unsubscribe
	 */
	public void unsubscribe();
}
