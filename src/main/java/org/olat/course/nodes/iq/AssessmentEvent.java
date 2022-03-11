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

package org.olat.course.nodes.iq;

import org.olat.core.gui.Windows;
import org.olat.core.util.UserSession;
import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Description:<br>
 * This is a MultiUserEvent that must be instantiated just before fireEventToListenersOf() is called,
 * in case an assessment is started or stopped. 
 * <p> 
 * It also have a "helper" responsability, namely it stores and retrieves the isAssessmentStarted 
 * information via the userSession.
 * 
 * <P>
 * Initial Date:  23.06.2009 <br>
 * @author Lavinia Dumitrescu
 */
public class AssessmentEvent extends MultiUserEvent {

	private static final long serialVersionUID = -4619743031390573124L;

	public enum TYPE {STARTED, STOPPED}
	
	private TYPE eventType = TYPE.STARTED;
	
	/**
	 * Create a new assessment event at start/stop assessment and disable/enable chat. <p>
	 * The information about assessment started/stopped is stored as windows attribute.
	 * @param command
	 */
	public AssessmentEvent(TYPE type, UserSession userSession) {
		super("");

		this.eventType = type; 
		if(TYPE.STARTED.equals(type)) {			
			Windows.getWindows(userSession).getAssessmentStarted().incrementAndGet();
		} else if(TYPE.STOPPED.equals(type)) {
			Windows.getWindows(userSession).getAssessmentStarted().decrementAndGet();
		}
	}

	/**
	 * 
	 * @return the event type
	 */
	public TYPE getEventType() {
		return eventType;
	}
	
	/**
	 * This is a static method! 
	 * The reason why this method resides here is "encapsulation" 
	 * (only this class knows where the info about isAssessmentStarted is stored.)
	 * @param userSession
	 * @return
	 */
	public static boolean isAssessmentStarted(UserSession userSession) {
		int count = Windows.getWindows(userSession).getAssessmentStarted().get();
		return count > 0;
	}
}
