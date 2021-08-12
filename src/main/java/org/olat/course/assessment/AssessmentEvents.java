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
package org.olat.course.assessment;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 12 Aug 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEvents {
	
	/** Many course node run controllers send a Event.CHANGED_EVENT.
	 * The RunMainController does rebuild the menu tree, progress bar, navigation AND does recreate the current controller.
	 * This Event does meant to initiate the rebuild of the menu tree etc but does not recreate the current controller.
	 * 
	 */
	public static final Event CHANGED_EVENT = new Event("assessment-changed");

	private AssessmentEvents() {
		//
	}

}
